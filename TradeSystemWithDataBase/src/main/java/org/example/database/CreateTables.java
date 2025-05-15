package org.example.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class CreateTables {
    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            if (conn != null) {
                createTables();
            }
        } catch (SQLException e) {
            System.out.println("Ошибка подключения: " + e.getMessage());
        }
    }

    // метод для создания таблиц в базе данных
    private static void createTables() throws SQLException {
        String createWarehousesTable = """
                        CREATE TABLE IF NOT EXISTS warehouses (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            name TEXT NOT NULL,
                            city TEXT NOT NULL,
                            address TEXT NOT NULL,
                            manager_id INTEGER
                        );
                        """;
        DataBase.makeSQLQuery(createWarehousesTable);

        String createStorageCellsTable = """
                        CREATE TABLE IF NOT EXISTS storage_cells (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            capacity INTEGER NOT NULL,
                            occupancy INTEGER,
                            storage_id INTEGER NOT NULL
                        );
                        """;
        DataBase.makeSQLQuery(createStorageCellsTable);

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
        DataBase.makeSQLQuery(createEmployeesTable);

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
        DataBase.makeSQLQuery(createBuyersTable);

        String createManufacturesTable = """
                        CREATE TABLE IF NOT EXISTS manufactures (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            name TEXT NOT NULL,
                            country TEXT NOT NULL
                        );
                        """;
        DataBase.makeSQLQuery(createManufacturesTable);

        String createProductsTable = """
                        CREATE TABLE IF NOT EXISTS products (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            name TEXT NOT NULL,
                            sell_price INTEGER NOT NULL,
                            buy_price INTEGER NOT NULL,
                            warehouse_id INTEGER NOT NULL
                        );
                        """;
        DataBase.makeSQLQuery(createProductsTable);

        String createOrdersTable = """
                        CREATE TABLE IF NOT EXISTS orders (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            product_id INTEGER NOT NULL,
                            amount INTEGER NOT NULL,
                            price INTEGER NOT NULL
                        );
                        """;
        DataBase.makeSQLQuery(createOrdersTable);

        String createShoppingCardsTable = """
                        CREATE TABLE IF NOT EXISTS shopping_cards (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            price INTEGER NOT NULL,
                            storage_id INTEGER NOT NULL
                        );
                        """;
        DataBase.makeSQLQuery(createShoppingCardsTable);

        String createOrderPickUpPointsTable = """
                        CREATE TABLE IF NOT EXISTS order_pick_up_points (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            city TEXT NOT NULL,
                            address TEXT NOT NULL
                        );
                        """;
        DataBase.makeSQLQuery(createOrderPickUpPointsTable);
    }
}
