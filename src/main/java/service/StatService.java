package service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import db.DBConnection;
import db.JsonParser;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class StatService {

    public void processStat(JsonObject inputJson, String outputFile) throws IOException {
        String startDate = inputJson.get("startDate").getAsString();
        String endDate = inputJson.get("endDate").getAsString();

        JsonObject result = new JsonObject();
        result.addProperty("type", "stat");

        try (Connection connection = DBConnection.getConnection()) {
            int totalDays = calculateTotalDays(startDate, endDate);
            result.addProperty("totalDays", totalDays);

            JsonArray customersArray = getCustomerStatistics(connection, startDate, endDate);
            result.add("customers", customersArray);

            writeOutput(outputFile, result);
        } catch (SQLException e) {
            handleError(result, e);
            writeOutput(outputFile, result);
        }
    }


    private int calculateTotalDays(String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        int totalDays = 0;

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            if (date.getDayOfWeek().getValue() < 6) {
                totalDays++;
            }
        }
        return totalDays;
    }

    private JsonArray getCustomerStatistics(Connection connection, String startDate, String endDate) throws SQLException {
        String query = "SELECT c.firstname, c.lastname, SUM(p.price) AS total_expenses " +
                "FROM purchase p JOIN customer c ON p.customer_id = c.id " +
                "WHERE p.date BETWEEN CAST(? AS DATE) AND CAST(? AS DATE) " +
                "GROUP BY c.firstname, c.lastname ORDER BY total_expenses DESC";;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            ResultSet rs = stmt.executeQuery();

            JsonArray customersArray = new JsonArray();
            while (rs.next()) {
                JsonObject customerJson = new JsonObject();
                customerJson.addProperty("firstName", rs.getString("firstname"));
                customerJson.addProperty("lastName", rs.getString("lastname"));
                customerJson.addProperty("totalExpenses", rs.getDouble("total_expenses"));
                customersArray.add(customerJson);
            }
            return customersArray;
        }
    }

    private void writeOutput(String outputFile, JsonObject result) throws IOException {
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write(result.toString());
        }
    }

    private void handleError(JsonObject result, Exception e) {
        e.printStackTrace();
        result.addProperty("error", e.getMessage());
    }
}

