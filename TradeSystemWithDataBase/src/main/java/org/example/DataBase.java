package org.example;

import java.sql.*;

public class DataBase {
    private static final String DATABASE_URL = "jdbc:sqlite:trade-system.db";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            if (conn != null) {
                createTables(conn);
            }
        } catch (SQLException e) {
            System.out.println("Ошибка подключения: " + e.getMessage());
        }
    }

    public static String getDatabaseUrl() {
        return DATABASE_URL;
    }

    // метод для выполнения SQL-запроса
    public static void makeSQLQuery(String sqlQuery) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            if (conn != null) {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate(sqlQuery);
            }
        } catch (SQLException e) {
            System.out.println("Ошибка подключения: " + e.getMessage());
        }
    }

    // метод для создания таблиц в базе данных
    private static void createTables(Connection conn) throws SQLException {
        String createWarehousesTable = """
                        CREATE TABLE IF NOT EXISTS warehouses (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            address TEXT NOT NULL
                        );
                        """;
        makeSQLQuery(createWarehousesTable);

        String createStorageCellsTable = """
                        CREATE TABLE IF NOT EXISTS storage_cells (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            capacity INTEGER NOT NULL,
                            occupancy INTEGER,
                            storage_id INTEGER NOT NULL
                        );
                        """;
        makeSQLQuery(createStorageCellsTable);

        String createEmployeesTable = """
                        CREATE TABLE IF NOT EXISTS workers (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            name TEXT NOT NULL,
                            surname TEXT NOT NULL,
                            phone_number TEXT NOT NULL,
                            work_place_id INTEGER NOT NULL,
                            status TEXT NOT NULL
                        );
                        """;
        makeSQLQuery(createEmployeesTable);

        String createBuyersTable = """
                        CREATE TABLE IF NOT EXISTS buyers (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            name TEXT NOT NULL,
                            surname TEXT NOT NULL,
                            phone_number TEXT NOT NULL,
                            city TEXT NOT NULL,
                            shopping_card_id INTEGER NOT NULL
                        );
                        """;
        makeSQLQuery(createBuyersTable);

        String createManufacturesTable = """
                        CREATE TABLE IF NOT EXISTS manufactures (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            name TEXT NOT NULL,
                            country TEXT NOT NULL
                        );
                        """;
        makeSQLQuery(createManufacturesTable);

        String createProductsTable = """
                        CREATE TABLE IF NOT EXISTS products (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            name TEXT NOT NULL,
                            sell_price INTEGER NOT NULL,
                            buy_price INTEGER NOT NULL,
                            warehouse_id INTEGER NOT NULL
                        );
                        """;
        makeSQLQuery(createProductsTable);

        String createOrdersTable = """
                        CREATE TABLE IF NOT EXISTS orders (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            product_id INTEGER NOT NULL,
                            amount INTEGER NOT NULL,
                            price INTEGER NOT NULL
                        );
                        """;
        makeSQLQuery(createOrdersTable);

        String createShoppingCardsTable = """
                        CREATE TABLE IF NOT EXISTS shopping_cards (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            price INTEGER NOT NULL,
                            storage_id INTEGER NOT NULL
                        );
                        """;
        makeSQLQuery(createShoppingCardsTable);

        String createOrderPickUpPointsTable = """
                        CREATE TABLE IF NOT EXISTS order_pick_up_points (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            address TEXT NOT NULL,
                            profit TEXT NOT NULL
                        );
                        """;
        makeSQLQuery(createOrderPickUpPointsTable);
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

             Statement stmt = conn.createStatement();

             String sqlQuery = "DELETE FROM " + tableName + " WHERE id " + " = ?";

             PreparedStatement pstmt = conn.prepareStatement(sqlQuery);
             pstmt.setInt(1, id);
             pstmt.executeUpdate();

        } catch (SQLException e)  {
            System.out.println("Ошибка подключения: " + e.getMessage());
        }
    }

}
