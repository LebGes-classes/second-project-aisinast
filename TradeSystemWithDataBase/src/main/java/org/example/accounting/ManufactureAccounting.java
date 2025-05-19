package org.example.accounting;

import org.example.database.DataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ManufactureAccounting {
    int id;
    int sale;
    int manufacture_id;
    String date;
    String productName;

    String tableName = "manufacture_sales";

    public static void registerSale(int sale, int manufacture_id, String productName) {
        String date = getDate();

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            String sqlQuery = "INSERT INTO manufacture_sales (sale, manufacture_id, date, product_name)" +
                    " VALUES (?, ?, ?, ?)";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                preparedStatement.setInt(1, sale);
                preparedStatement.setInt(2, manufacture_id);
                preparedStatement.setString(3, date);
                preparedStatement.setString(4, productName);

                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при регистрации продажи: " + e.getMessage());
        }
    }

    public static int getProfitInfo(int manufactureId) {
        int profit = 0;
        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            String sqlQuery = "SELECT SUM(sale) AS total_sale FROM manufacture_sales WHERE manufacture_id = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                preparedStatement.setInt(1, manufactureId);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        profit = resultSet.getInt("total_sale");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении информации о прибыли: " + e.getMessage());
        }

        return profit;
    }

    private static String getDate() {
        LocalDateTime localDateTime = LocalDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm");
        String formattedDateTime = localDateTime.format(formatter);

        return formattedDateTime;
    }
}
