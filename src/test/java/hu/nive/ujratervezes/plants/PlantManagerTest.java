package hu.nive.ujratervezes.plants;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
