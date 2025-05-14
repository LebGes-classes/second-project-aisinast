package org.example.database;

import java.sql.*;

public class DataBase {
    private static final String DATABASE_URL = "jdbc:sqlite:trade-system.db";

    public static String getDatabaseUrl() {
        return DATABASE_URL;
    }

    // метод для выполнения SQL-запроса
    public static void makeSQLQuery(String sqlQuery) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            if (conn != null) {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate(sqlQuery);
            }
        } catch (SQLException e) {
            System.out.println("Ошибка подключения: " + e.getMessage());
        }
    }

    // метод для подсчета количества строк, удовлетворяющих условию
    public static int sqliteCountRows(String tableName, String fieldName, String fieldValue) {
        int count = 0;

        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            String sqlQuery = String.format("SELECT COUNT(*) FROM %s WHERE %s = ?", tableName, fieldName);

            PreparedStatement pstmt = conn.prepareStatement(sqlQuery);

            pstmt.setString(1, fieldValue);

            ResultSet rs = pstmt.executeQuery();

            // возвращает id последней строки
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при работе с базой данных: " + e.getMessage());
        }

        return count;
    }

    // метод для удаления строк в некоторой таблице по id
    public static void removeRaw(String tableName, int id) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
             String sqlQuery = "DELETE FROM " + tableName + " WHERE id " + " = ?";

             PreparedStatement pstmt = conn.prepareStatement(sqlQuery);
             pstmt.setInt(1, id);
             pstmt.executeUpdate();

        } catch (SQLException e)  {
            System.out.println("Ошибка подключения: " + e.getMessage());
        }
    }

    // метод для получения айди по строковому значению некоторого столбца
    public static int getId(String tableName, String fieldName, String fieldValue) {
        int id = 0;

        try (Connection conn = DriverManager.getConnection(DATABASE_URL)){

            Statement stmt = conn.createStatement();

            String sqlQuery = String.format("SELECT id FROM %s WHERE %s = ?", tableName, fieldName);

            PreparedStatement pstmt = conn.prepareStatement(sqlQuery);
            pstmt.setString(1, fieldValue);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    id = rs.getInt("id");
                }
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при работе с базой данных: " + e.getMessage());
        }

        return id;
    }

    // метод для вывода таблицы
    public static void printAll(String tableName, int columnCount) {
        try (Connection conn = DriverManager.getConnection(DataBase.getDatabaseUrl());) {
            String sqlQuery = "SELECT * FROM " + tableName;

            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(sqlQuery);

            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(rs.getString(i) + "\t");
                }
                System.out.println();
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при выводе производителей: " + e.getMessage());
        }
    }
}
