package service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import db.DBConnection;
import db.JsonParser;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SearchService {

    public void processSearch(JsonObject inputJson, String outputFile) throws IOException {
        JsonArray criterias = inputJson.getAsJsonArray("criterias");
        JsonObject result = new JsonObject();
        result.addProperty("type", "search");
        JsonArray resultsArray = new JsonArray();

        try (Connection connection = DBConnection.getConnection()) {
            for (int i = 0; i < criterias.size(); i++) {
                JsonObject criteria = criterias.get(i).getAsJsonObject();
                JsonObject criteriaResult = new JsonObject();
                criteriaResult.add("criteria", criteria);

                if (criteria.has("lastName")) {
                    String lastName = criteria.get("lastName").getAsString();
                    criteriaResult.add("results", searchByLastName(connection, lastName));
                } else if (criteria.has("productName")) {
                    String productName = criteria.get("productName").getAsString();
                    int id = getProductIdsByName(productName, connection);
                    int minTimes = criteria.get("minTimes").getAsInt();
                    criteriaResult.add("results", searchByProduct(connection, id, minTimes));
                } else if (criteria.has("minExpenses") && criteria.has("maxExpenses")) {
                    double minExpenses = criteria.get("minExpenses").getAsDouble();
                    double maxExpenses = criteria.get("maxExpenses").getAsDouble();
                    criteriaResult.add("results", searchByExpenses(connection, minExpenses, maxExpenses));
                } else if (criteria.has("badCustomers")) {
                    int limit = criteria.get("badCustomers").getAsInt();
                    criteriaResult.add("results", searchBadCustomers(connection, limit));
                }

                resultsArray.add(criteriaResult);
            }
            result.add("results", resultsArray);
            writeOutput(outputFile, result);
        } catch (SQLException e) {
            handleError(result, e);
            writeOutput(outputFile, result);
        }
    }

    private JsonArray searchByLastName(Connection connection, String lastName) throws SQLException {
        String query = "SELECT firstname FROM customer WHERE lastname = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, lastName);
            ResultSet rs = stmt.executeQuery();

            JsonArray resultsArray = new JsonArray();
            while (rs.next()) {
                JsonObject customerJson = new JsonObject();
                customerJson.addProperty("firstName", rs.getString("firstname"));
                resultsArray.add(customerJson);
            }
            return resultsArray;
        }
    }

    private JsonArray searchByProduct(Connection connection, int productId, int minTimes) throws SQLException {

        String query = "SELECT c.firstname, c.lastname FROM purchase p JOIN customer c ON p.customer_id = c.id " +
                "WHERE p.id = ? GROUP BY c.firstname, c.lastname HAVING COUNT(*) >= ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, productId);
            stmt.setInt(2, minTimes);
            ResultSet rs = stmt.executeQuery();

            JsonArray resultsArray = new JsonArray();
            while (rs.next()) {
                JsonObject customerJson = new JsonObject();
                customerJson.addProperty("firstName", rs.getString("firstname"));
                customerJson.addProperty("lastName", rs.getString("lastname"));
                resultsArray.add(customerJson);
            }
            return resultsArray;
        }
    }

    private JsonArray searchByExpenses(Connection connection, double minExpenses, double maxExpenses) throws SQLException {

        String query = "SELECT c.firstname, c.lastname FROM purchase p JOIN customer c ON p.customer_id = c.id " +
                "GROUP BY c.firstname, c.lastname HAVING SUM(p.price) BETWEEN ? AND ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDouble(1, minExpenses);
            stmt.setDouble(2, maxExpenses);
            ResultSet rs = stmt.executeQuery();

            JsonArray resultsArray = new JsonArray();
            while (rs.next()) {
                JsonObject customerJson = new JsonObject();
                customerJson.addProperty("firstName", rs.getString("first_name"));
                customerJson.addProperty("lastName", rs.getString("last_name"));
                resultsArray.add(customerJson);
            }
            return resultsArray;
        }
    }

    private JsonArray searchBadCustomers(Connection connection, int limit) throws SQLException {
        String query = "SELECT c.firstname, c.lastname FROM purchase p JOIN customer c ON p.customer_id = c.id " +
                "GROUP BY c.firstname, c.lastname ORDER BY COUNT(p.id) ASC LIMIT ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            JsonArray resultsArray = new JsonArray();
            while (rs.next()) {
                JsonObject customerJson = new JsonObject();
                customerJson.addProperty("firstName", rs.getString("firstname"));
                customerJson.addProperty("lastName", rs.getString("lastname"));
                resultsArray.add(customerJson);
            }
            return resultsArray;
        }
    }

    private void writeOutput(String outputFile, JsonObject result) throws IOException {
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write(result.toString());
        }
    }

    public Integer getProductIdsByName(String productName, Connection connection) {
        String sql = "SELECT id FROM product WHERE name = ?";
        int id = 0;
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, productName);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                 id = resultSet.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return id;
    }


    private void handleError(JsonObject result, Exception e) {
        e.printStackTrace();
        result.addProperty("error", e.getMessage());
    }
}
