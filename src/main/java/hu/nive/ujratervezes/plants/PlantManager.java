package hu.nive.ujratervezes.plants;

public class PlantManager {

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    public PlantManager(String dbUrl, String dbUser, String dbPassword) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }
}
