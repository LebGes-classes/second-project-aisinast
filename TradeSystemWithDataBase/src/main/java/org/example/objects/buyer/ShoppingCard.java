package org.example.objects.buyer;

import org.example.appсontrol.database.DataBase;

import java.sql.*;

public class ShoppingCard {
    // метод для создания новой корзины при создании покупателя
    public static int createNewShoppingCard() {
        int id = 0;
        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            String sqlQuery = "INSERT INTO shopping_cards (price) VALUES (0)";

            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(sqlQuery);
            }

            // получаем id последней добавленной строки
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
