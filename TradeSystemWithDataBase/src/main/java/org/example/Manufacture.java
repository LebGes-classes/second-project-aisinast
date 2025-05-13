package org.example;

import org.example.database.DataBase;
import org.example.services.CheckNotEmpty;

import java.sql.*;
import java.util.Scanner;

public class Manufacture {
    private int id;
    private String name;
    private String country;

    private static String tableName = "manufactures";

    public static String getTableName() {
        return tableName;
    }

    static Scanner scanner = new Scanner(System.in);

    public static void addManufacture() {
        System.out.print("Введите название производителя: ");
        String name = scanner.nextLine();

        try {
            CheckNotEmpty.checkStringNotEmpty(name);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
            return;
        }

        if (DataBase.sqliteCountRows(getTableName(), "name", name) != 0) {
            System.out.println("Производитель с таким названием уже существует!");
            return;
        }

        System.out.print("Введите имя производителя: ");
        String country = scanner.nextLine();

        try {
            CheckNotEmpty.checkStringNotEmpty(country);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
            return;
        }

        addManufactureIntoTable(name, country);
    }

    // добавление нового производителя в таблицу "manufactures"
    private static void addManufactureIntoTable(String name, String country) {
        // запрос для вставки нового производителя
        String sqlQuery = "INSERT INTO manufactures (name, country) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(DataBase.getDatabaseUrl())) { // проверка соединения
            PreparedStatement pstmt = conn.prepareStatement(sqlQuery);

            pstmt.setString(1, name); // установка значения параметров
            pstmt.setString(2, country);

            pstmt.executeUpdate(); // запрос на вставку данных

            int id = 0;
            // получение id только что добавленного производителя
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    id = rs.getInt(1);
                }
            }

            System.out.println("Производитель " + name + " успешно добавлен! ID: " + id);

        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении производителя: " + e.getMessage());
        }
    }
}