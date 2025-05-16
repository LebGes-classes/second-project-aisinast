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

    public static void closeWarehouse() {
        String warehouse = printWarehousesAndChoose();

        if (DataBase.sqliteCountRows("warehouses", "name", warehouse) == 0) {
            System.out.println("Такого склада не существует");
            return;
        }

        int warehouseId = DataBase.getId("warehouses", "name", warehouse);

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            String sqlQuery = String.format("UPDATE workers SET status = 'уволен' " +
                    "WHERE work_place_id = %s AND status = 'работает на складе'", warehouseId);
            Statement statement = connection.createStatement();
            statement.executeUpdate(sqlQuery);

            DataBase.removeRaw("warehouses", warehouseId);

            statement.close();

            System.out.println("Склад " + warehouse + " закрыт");
        } catch (SQLException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    public static void printWarehouseInfo() {
        String warehouse = printWarehousesAndChoose();

        getWarehouseInfo(warehouse);
    }

    public static void changeManager() {
        String warehouse = printWarehousesAndChoose();
        int warehouseId = DataBase.getId("warehouses", "name", warehouse);

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            String sqlQuery = "SELECT * FROM workers WHERE work_place_id = ? AND status = 'работает на складе'";

            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
            preparedStatement.setInt(1, warehouseId);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                for (int i = 1; i <= 6; i++) {
                    System.out.print(resultSet.getString(i) + "\t");
                }
                System.out.println();
            }

            System.out.print("Введите номер работника, которого вы хотите назначить ответственным лицом: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            String newSqlQuery = "UPDATE warehouses SET manager_id = ? WHERE name = ?";
            PreparedStatement preparedStatement1 = connection.prepareStatement(newSqlQuery);
            preparedStatement1.setInt(1, choice);
            preparedStatement1.setString(2, warehouse);

            preparedStatement1.executeUpdate();

            resultSet.close();
            preparedStatement.close();
            preparedStatement1.close();

            System.out.println("Смена ответственного лица склада " + warehouse + " произошла успешно");
        } catch (SQLException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }

        String sqlQuery = "UPDATE warehouses SET manager_id = ? WHERE name = ?";
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
                int managerId = resultSet.getInt("manager_id");

                if (resultSet.wasNull()) {
                    System.out.println("Склад: " + name + ", " + address + ". Ответственное лицо не назначено");
                } else {
                    System.out.println("Склад: " + name + ", " + address +
                            ". Ответственное лицо: " + getManagerName(managerId));
                }
            } else {
                System.out.println("Запись не найдена");
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    public static String printWarehousesAndChoose() {
        List<String> warehouses = DataBase.columnToList(tableName, "name");

        String[] warehousesArray = warehouses.toArray(new String[0]);

        System.out.println("Выберите номер склада из списка: ");

        for (int i = 0; i < warehousesArray.length; i++) {
            System.out.println(i + 1 + ". " + warehousesArray[i]);
        }

        System.out.print("Ваш выбор: ");
        int warehouseNumber = scanner.nextInt() - 1;
        scanner.nextLine();

        String warehouseName = warehousesArray[warehouseNumber];

        return warehouseName;
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

            resultSet.close();
            preparedStatement.close();
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

            pstmt.close();
            System.out.println("Склад " + name + " успешно добавлен! ID: " + id);
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении склада: " + e.getMessage());
        }
    }
}
