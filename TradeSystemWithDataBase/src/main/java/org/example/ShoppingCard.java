package org.example;

import org.example.database.DataBase;

import java.sql.*;

public class ShoppingCard {
    public static int createNewShoppingCard() {
        int id = 0;
        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            String sqlQuery = "INSERT INTO shopping_cards (price) VALUES (0)";

            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(sqlQuery);
            }

            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("SELECT last_insert_rowid()")) {
                if (resultSet.next()) {
                    id = resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при создании корзины: " + e.getMessage());
        }

        return id;
    }
}
