package org.example;

import org.example.database.DataBase;
import org.example.services.Check;

import java.util.List;
import java.sql.*;
import java.util.Scanner;

public class Warehouse {
    int id;
    String name;
    String city;
    String address;
    int managerId;

    private static final String tableName = "warehouses";
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

    public static void printWarehouseInfo() {
        List<String> warehouses = DataBase.columnToList(tableName, "name");

        String[] warehousesArray = warehouses.toArray(new String[0]);

        System.out.println("Выберите номер склада из списка: ");

        for (int i = 0; i < warehousesArray.length; i++) {
            System.out.println(i + 1 + ". " + warehousesArray[i]);
        }

        System.out.print("Ваш выбор: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        getWarehouseInfo(warehousesArray[choice - 1]);
    }

    private static void getWarehouseInfo(String name) {
        int id = DataBase.getId(tableName, "name", name);
        String sqlQuery = "SELECT name, address, manager_id FROM warehouses WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())){
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);

            preparedStatement.setInt(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String address = resultSet.getString("address");

                if (resultSet.wasNull()) {
                    System.out.println("Склад: " + name + ", " + address + ". Ответственное лицо не назначено");
                } else {
                    int managerId = resultSet.getInt("manager_id");
                    System.out.println("Склад: " + name + ", " + address +
                            ". Ответственное лицо: " + getManagerName(managerId));
                }
            } else {
                System.out.println("Запись не найдена");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    private static String createWarehouseName(String city) {
        return city + "-" + (DataBase.sqliteCountRows(tableName, "city", city) + 1);
    }

    private static String getManagerName(int managerId) {
        String sqlQuery = "SELECT name, surname FROM workers WHERE id = ?";

        String name = "";
        String surname = "";

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())){
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
            preparedStatement.setInt(1, managerId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                name = resultSet.getString("name");
                surname = resultSet.getString("surname");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }

        return name + " " + surname;
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
