package org.example;

import org.example.database.DataBase;
import org.example.services.Check;

import java.sql.*;
import java.util.Scanner;

public class Manufacture {
    private int id;
    private String name;
    private String country;

    private static String tableName = "manufactures";

    public Manufacture(int id, String name, String country) {
        this.id = id;
        this.name = name;
        this.country = country;
    }

    public static String getTableName() {
        return tableName;
    }

    static Scanner scanner = new Scanner(System.in);

    public static void addManufacture() {
        System.out.print("Введите название производителя: ");
        String name = scanner.nextLine();

        try {
            Check.stringNotEmpty(name);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
            return;
        }

        if (DataBase.sqliteCountRowsWithCondition(getTableName(), "name", name) != 0) {
            System.out.println("Производитель с таким названием уже существует!");
            return;
        }

        System.out.print("Введите страну производителя: ");
        String country = scanner.nextLine();

        try {
            Check.stringNotEmpty(country);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
            return;
        }

        addManufactureIntoTable(name, country);
    }

    public static void removeManufacture() {
        System.out.print("Введите название производителя: ");
        String name = scanner.nextLine();

        int id = DataBase.getId(tableName, "name", name);

        if (id == 0) {
            System.out.println("Производства с таким названием не существует!");
            return;
        }

        DataBase.removeRaw(tableName, id);
    }

    public static void printAllManufactures() {
        DataBase.printAll(getTableName(), 3);
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

            pstmt.close();

            System.out.println("Производитель " + name + " успешно добавлен! ID: " + id);

        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении производителя: " + e.getMessage());
        }
    }
}