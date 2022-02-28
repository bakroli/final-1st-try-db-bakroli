package hu.nive.ujratervezes.plants;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlantManager {

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    public PlantManager(String dbUrl, String dbUser, String dbPassword) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    public List<String> getPlantsToBeWatered() {
        List<String> plants = new ArrayList<>();
        String sqlQuery = "SELECT DISTINCT(plant_name) FROM plants JOIN watered ON plants.plant_id = watered.plant_id WHERE watered_at < '2021-12-31' ORDER BY plant_name ASC";

        try (Connection connection = DriverManager.getConnection(dbUrl,dbUser,dbPassword)) {
            PreparedStatement statement = connection.prepareStatement(sqlQuery);
            plants = getResult(statement.executeQuery());
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return plants;
    }

    private static List<String> getResult(ResultSet resultSet) throws SQLException {
        List<String> result = new ArrayList<>();
        while (resultSet.next()) {
            result.add(resultSet.getString(1));
        }
        return result;
    }


}
