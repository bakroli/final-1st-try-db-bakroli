Elég rosszul viselem gondját a növényeimnek, de úgy döntöttem 2022-ben már változtatok ezen.
Készítsünk egy programot amivel nyilván lehet tartani, hogy a szobanövényeim közül melyiket kell meglocsolni.

# Adatbázis

Az adatbázis két táblából áll amelynek nevei `plants` és `watered`. 

A `plants` tábla következő oszlopokal rendelkezik:

- plant_id SERIAL
- plant_name VARCHAR(255)
- plant_type VARCHAR(255)
- health_condition VARCHAR(255)

Például:

| plant_name        | plant_type      | health_condition  | plant_id |
|:------------------|:----------------|:------------------|:---------|
| Kövirózsa         | Pozsgás         | GOOD              | 1        |
| Kék fodros jácint | Jácint          | POOR              | 2        |
| Citrom fa         | Fa              | EXCELLENT         | 3        |
| Hortenzia         | Pozsgás         | GOOD              | 4        |
| Kisfoltos         | Orchidea        | POOR              | 5        |
| Aloevera          | Pozsgás         | GOOD              | 6        |

A `watered` tábla következő oszlopokal rendelkezik:

- plant_id SERIAL
- watered_at DATE

Például:

| plant_id  | watered_at         |
|:----------|:-------------------|
| 1         |  2021-12-30        |  
| 2         |  2022-01-15        |  
| 4         |  2022-02-24        |  
| 5         |  2022-01-21        |  
| 6         |  2021-11-17        |  
| 3         |  2022-02-15        |
| 1         |  2021-11-13        |  
| 4         |  2022-01-01        |

# Java alkalmazás

Az `PlantManager` osztály konstruktora a következő paraméterekkel rendelkezik: 
- `String dbUrl` az url amin az adatbázis elérhető.
- `String dbUser` felhasználónév amivel csatlakozhatunk az adatbázishoz.
- `String dbPassword`  A `dbUser`-hez tartozó jelszó.

Készítsd el a `PlantManager` osztály `getPlantsToBeWatered` metódusát! Abban az esetben ha az adatbázis üres a metódus térjen vissza egy üres `String` listával. Egyéb esetben a a metódus térjen vissza egy `String` listában az összes a megöntözendő növények nevével ABC szerinti növekvő sorrendben. 
Egy növény akkor számít megöntözendőnek, ha a 2022-ben még nem volt meglocsolva. Egy növény csak egyszer szerepeljen függetlenül attól hányszor lett már megöntözve.

A megoldáshoz használj `PreparedStatement`-et!

# Test-ek
```java
class PlantManagerTest {
    private static final List<String> EXPECTED_PLANTS = List.of("Aloevera", "Kövirózsa");

    private static final String DB_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    private PlantManager plantManager;

    @BeforeEach
    void init() throws SQLException {
        plantManager = new PlantManager(DB_URL, DB_USER, DB_PASSWORD);
        createTable();
    }

    @AfterEach
    void destruct() throws SQLException {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String dropTablePlants = "DROP TABLE IF EXISTS plants";
            Statement statementPlants = connection.createStatement();
            statementPlants.execute(dropTablePlants);
        }
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String dropTableWatered = "DROP TABLE IF EXISTS watered";
            Statement statementWatered = connection.createStatement();
            statementWatered.execute(dropTableWatered);
        }
    }


    private void createTable() throws SQLException {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String createTablePlants = "CREATE TABLE IF NOT EXISTS plants (" +
                    "plant_id SERIAL," +
                    " plant_name VARCHAR(255)," +
                    " plant_type VARCHAR(255)," +
                    "health_condition VARCHAR(255)" +
                    ");";
            Statement statementPlants = connection.createStatement();
            statementPlants.execute(createTablePlants);
        }
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String createTableWatered = "CREATE TABLE IF NOT EXISTS watered (" +
                    "plant_id SERIAL, " +
                    "watered_at DATE" +
                    ");";
            Statement statementWatered = connection.createStatement();
            statementWatered.execute(createTableWatered);
        }
    }

    @Test
    void test_getUniquePlants_anyOrder() throws SQLException {
        insertMultiplePlants();
        List<String> actualPlants = plantManager.getPlantsToBeWatered();

        assertEquals(EXPECTED_PLANTS.size(), actualPlants.size());
        for (String expectedPlant : EXPECTED_PLANTS) {
            assertTrue(actualPlants.contains(expectedPlant));
        }
    }


    @Test
    void test_getUniquePlants_alphabeticOrder() throws SQLException {
        insertMultiplePlants();
        List<String> actualPlants = plantManager.getPlantsToBeWatered();
        assertEquals(EXPECTED_PLANTS, actualPlants);
    }

    @Test
    void test_getUniquePlants_oneDuplicate_anyOrder() throws SQLException {
        insertMultiplePlants();
        insertDuplicateWatering();
        List<String> actualPlants = plantManager.getPlantsToBeWatered();
        assertEquals(EXPECTED_PLANTS.size(), actualPlants.size());
        for (String plant : EXPECTED_PLANTS) {
            assertTrue(actualPlants.contains(plant));
        }
    }

    @Test
    void test_getUniquePlants_emptyDatabase() {
        assertEquals(List.of(), plantManager.getPlantsToBeWatered());
    }

    private void insertMultiplePlants() throws SQLException {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String insertPlants = "INSERT INTO plants (plant_name, plant_type, health_condition, plant_id) VALUES " +
                    "('Kövirózsa', 'Pozsgás','GOOD',1), " +
                    "('Kék fodros jácint', 'Jácint' , 'POOR' ,2), " +
                    "('Hortenzia', 'Pozsgás', 'GOOD', 4), " +
                    "('Kisfoltos', 'Orchidea', 'POOR', 5), " +
                    "('Aloevera', 'Pozsgás', 'GOOD', 6), " +
                    "('Citrom fa', 'Fa', 'EXCELLENT', 3);";
            Statement statement = connection.createStatement();
            statement.execute(insertPlants);
        }

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String insertPlants = "INSERT INTO watered (plant_id, watered_at) VALUES " +
                    "(1,'2021-12-30'), " +
                    "(2,'2022-01-15'), " +
                    "(4,'2022-02-24'), " +
                    "(5,'2022-01-21'), " +
                    "(6,'2021-11-17'), " +
                    "(3,'2022-02-15');";
            Statement statement = connection.createStatement();
            statement.execute(insertPlants);
        }
    }

    private void insertDuplicateWatering() throws SQLException {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String insertPlants = "INSERT INTO watered (plant_id, watered_at) VALUES " +
                    "(1,'2021-11-13'), " +
                    "(2,'2021-11-13'), " +
                    "(4,'2022-01-01');";
            Statement statement = connection.createStatement();
            statement.execute(insertPlants);
        }
    }
}
```

# Pontozás

A feladat 0 pontot ér, bármely alábbi esetben:
- le sem fordul az adott projekt.
- teszteset sem fut le sikeresen
- ha a forráskód olvashatatlan, nem felel meg a konvencióknak, nem követi a clean code alapelveket
- ha kielégíti a teszteseteket, de a szöveges követelményeknek nem felel meg

Clean code-ra adható pontok: max 10

tesztekre adható pontok:

| Teszt | Pont |
--- | ----
test_getUniquePlants_anyOrder() | 2.5 pont
test_getUniquePlants_oneDuplicate_anyOrder() | 2.5 pont
test_getUniquePlants_alphabeticOrder() | 2.5 pont
test_getUniquePlants_emptyDatabase() | 2.5 pont
