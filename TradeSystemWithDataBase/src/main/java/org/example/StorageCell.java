package org.example;

import org.example.database.DataBase;

import java.sql.*;
import java.util.Scanner;

public class StorageCell {
    private int id;
    private int capacity;
    private int occupancy;
    private int warehouseId;

    static Scanner scanner = new Scanner(System.in);

    public static void addNewCell() {
        System.out.println("На какой склад добавить ячейку?");
        String warehouse = Warehouse.printWarehousesAndChoose();
        int warehouseId = DataBase.getId("warehouses", "name", warehouse);

        System.out.print("Введите вместимость: ");
        int capacity = scanner.nextInt();
        scanner.nextLine();

        if (capacity <= 0) {
            System.out.println("Ячейка не может иметь отрицательную или нулевую вместимость");
        }

        addIntoTable(capacity, 0, warehouseId);
    }

    private static void addIntoTable(int capacity, int occupancy, int warehouseId) {
        String sqlQuery = "INSERT INTO storage_cells (capacity, occupancy, warehouse_id) VALUES (?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);

            preparedStatement.setInt(1, capacity);
            preparedStatement.setInt(2, occupancy);
            preparedStatement.setInt(3, warehouseId);

            preparedStatement.executeUpdate();

            System.out.println("Ячейка успешно добавлена!");
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении ячейки склада: " + e.getMessage());
        }
    }
}
