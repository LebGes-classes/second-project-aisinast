package org.example;

import org.example.database.DataBase;
import org.example.services.Check;

import java.sql.*;
import java.util.Scanner;

public class Warehouse {
    int id;
    String name;
    String city;
    String address;
    int managerId;

    private static String tableName = "warehouses";
    static Scanner scanner = new Scanner(System.in);

    public static void addNewWarehouse() {
        System.out.print("Введите город: ");
        String city = scanner.nextLine();

        try {
            Check.stringNotEmpty(city);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }

        String name = createWarehouseName(city);

        System.out.print("Введите адрес: ");
        String address = scanner.nextLine();

        try {
            Check.stringNotEmpty(address);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }

        addStorageIntoTable(name, city, address);
    }

    private static String createWarehouseName(String city) {
        return city + "-" + (DataBase.sqliteCountRows(tableName, "city", city) + 1);
    }

    private static void addStorageIntoTable(String name, String city, String address) {
        String sqlQuery = "INSERT INTO warehouses (name, city, address) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            PreparedStatement pstmt = conn.prepareStatement(sqlQuery);

            pstmt.setString(1, name);
            pstmt.setString(2, city);
            pstmt.setString(3, address);

            pstmt.executeUpdate();

            int id = 0;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    id = rs.getInt(1);
                }
            }

            System.out.println("Склад " + name + " успешно добавлен! ID: " + id);
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении склада: " + e.getMessage());
        }
    }

}
