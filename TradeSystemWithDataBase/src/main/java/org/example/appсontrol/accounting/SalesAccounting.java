package org.example.appсontrol.accounting;

import org.example.appсontrol.database.DataBase;
import org.example.appсontrol.menu.AppMenu;
import org.example.appсontrol.services.DateFormatter;

import java.sql.*;

public class SalesAccounting {

    // универсальный метод для регистрации продажи
    public static void registerSale(String saleLocation, int sale, int saleLocationId, String productName) {
        String date = DateFormatter.getDate();

        String tableName = null;
        String columnName = null;

        // определяем таблицу в зависимости от переданного параметра
        switch (saleLocation) {
            case "manufacture":
                tableName = "manufacture_sales";
                columnName = "manufacture_id";
                break;
            case "sale_point":
                tableName = "sale_point_sales";
                columnName = "sale_point_id";
                break;
            default:
                System.out.println("Некорректный ввод!");
                AppMenu.showAppMenu();
                break;
        }

        // запрос в бд, который создает новую запись о продаже
        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            String sqlQuery = String.format("INSERT INTO %s (sale, %s, date, product_name)" +
                    " VALUES (?, ?, ?, ?)", tableName, columnName);

            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                preparedStatement.setInt(1, sale);
                preparedStatement.setInt(2, saleLocationId);
                preparedStatement.setString(3, date);
                preparedStatement.setString(4, productName);

                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при регистрации продажи: " + e.getMessage());
        }
    }

    // универсальный метод для получения информации о прибыли
    public static int getProfitInfo(String saleLocation, int saleLocationId) {
        int profit = 0;

        String tableName = null;
        String columnName = null;

        // определяем таблицу в зависимости от переданного параметра
        switch (saleLocation) {
            case "manufacture":
                tableName = "manufacture_sales";
                columnName = "manufacture_id";
                break;
            case "sale_point":
                tableName = "sale_point_sales";
                columnName = "sale_point_id";
                break;
            default:
                System.out.println("Некорректный ввод!");
                AppMenu.showAppMenu();
                break;
        }

        // запрос в бд, который возвращает сумму продаж
        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            String sqlQuery = String.format("SELECT SUM(sale) AS total_sale FROM %s WHERE %s = ?",
                    tableName, columnName);

            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                preparedStatement.setInt(1, saleLocationId);

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
}
