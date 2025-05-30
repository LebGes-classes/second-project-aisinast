package org.example;

import org.example.database.DataBase;
import org.example.services.SafeInput;

import java.sql.*;
import java.util.Scanner;

public class StorageCell {
    private int id;
    private int capacity;
    private int occupancy;
    private int warehouseId;

    static Scanner scanner = new Scanner(System.in);

    public static void addNewCell() {
        String status = "ячейка склада";

        int warehouseId = 0;
        System.out.println("На какой склад добавить ячейку?");
        String warehouse = Warehouse.printWarehousesAndChoose();
        warehouseId = DataBase.getId("warehouses", "name", warehouse);

        int capacity = SafeInput.safeIntInput("Введите вместимость: ");

        if (capacity <= 0) {
            System.out.println("Ячейка не может иметь отрицательную или нулевую вместимость");
        }

        addIntoTable(capacity, warehouseId, status);
    }

    public static void addIntoTable(int capacity, int warehouseId, String status) {
        String sqlQuery = "INSERT INTO storage_cells (capacity, occupancy, storage_id, status) VALUES (?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);

            preparedStatement.setInt(1, capacity);
            preparedStatement.setInt(2, 0);
            preparedStatement.setInt(3, warehouseId);
            preparedStatement.setString(4, status);

            preparedStatement.executeUpdate();

            preparedStatement.close();

            System.out.println("Ячейка успешно добавлена!");
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении ячейки склада: " + e.getMessage());
        }
    }
}
