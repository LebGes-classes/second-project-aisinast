package org.example;

import org.example.database.DataBase;
import org.example.services.Check;

import java.sql.*;
import java.util.*;

public class Product {
    private int id;
    private String name;
    private int sellPrice;
    private int buyPrice;
    private int storageCellId;
    private int quantity;
    private int manufactureId;

    static Scanner scanner = new Scanner(System.in);

    public static void addNewProduct(String warehouse) {
        if (DataBase.sqliteCountRows("warehouses", "name", warehouse) == 0) {
            System.out.println("Ячейки на этом складе отсутствуют");
            return;
        }

        int warehouseId = DataBase.getId("warehouses", "name", warehouse);

        System.out.print("Введите название товара: ");
        String name = scanner.nextLine();

        try {
            Check.stringNotEmpty(name);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка1: " + e.getMessage());
            return;
        }

        System.out.print("Введите цену продажи: ");
        int sellPrice = scanner.nextInt();
        scanner.nextLine();

        if (sellPrice < 0) {
            System.out.println("Цена не может быть меньше 0");
        }

        System.out.print("Введите цену покупки: ");
        int buyPrice = scanner.nextInt();
        scanner.nextLine();

        if (buyPrice < 0) {
            System.out.println("Цена не может быть меньше 0");
        }

        System.out.print("Введите количество: ");
        int quantity = scanner.nextInt();
        scanner.nextLine();

        if (quantity < 0) {
            System.out.println("Количество не может быть меньше 0");
            return;
        } else if (quantity > getFreeSpaceInWarehouse(warehouseId)) {
            System.out.println("Недостаточно свободного места");
            return;
        }

        System.out.println("Выберите производство из списка ниже: ");
        DataBase.printAll("manufactures", 3);
        System.out.print("Ваш выбор (введите id) : ");
        int manufactureId = scanner.nextInt();
        scanner.nextLine();

        List<Integer> cells = getCellsList(warehouseId);

        if (getCellFreeSpace(cells.getFirst()) >= quantity) {
            addIntoTable(name, sellPrice, buyPrice, cells.getFirst(), quantity, manufactureId);
            changeOccupancy(cells.getFirst(), quantity);
        } else {
            putInSeveralCells(name, buyPrice, sellPrice, quantity, manufactureId, warehouseId);
        }
    }

    private static void putInSeveralCells(String name, int buyPrice, int sellPrice, int quantity,
                                          int manufactureId, int warehouseId) {
        List<Integer> cells = getCellsList(warehouseId);

        int remainingQuantity = quantity;

        for (int cellId : cells) {
            if (remainingQuantity <= 0) break;

            int cellFreeSpace = getCellFreeSpace(cellId);
            if (cellFreeSpace <= 0) continue;

            int qtyToAdd = Math.min(cellFreeSpace, remainingQuantity);
            addIntoTable(name, sellPrice, buyPrice, cellId, qtyToAdd, manufactureId);
            changeOccupancy(cellId, qtyToAdd);
            remainingQuantity -= qtyToAdd;
        }
    }

    private static int getCurrentOccupancy(int storageCellId) {
        String sqlQuery = "SELECT occupancy FROM storage_cells WHERE id = ?";

        int occupancy = 0;

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);

            preparedStatement.setInt(1, storageCellId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                occupancy = resultSet.getInt("occupancy");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }

        return occupancy;
    }

    private static void changeOccupancy(int storageCellId, int occupancyChange) {
        String sqlQuery = "UPDATE storage_cells SET occupancy = ? WHERE id = ?";

        int newOccupancy = getCurrentOccupancy(storageCellId) + occupancyChange;

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);

            preparedStatement.setInt(1, newOccupancy);
            preparedStatement.setInt(2, storageCellId);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    private static int getFreeSpaceInWarehouse(int warehouseId) {
        int capacitySum = 0;
        int occupancySum = 0;

        int delta = 0;

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            String sqlQuery1 = "SELECT SUM(capacity) AS total_capacity FROM storage_cells WHERE warehouse_id = ?";
            String sqlQuery2 = "SELECT SUM(occupancy) AS total_occupancy FROM storage_cells WHERE warehouse_id = ?";

            PreparedStatement preparedStatement1 = connection.prepareStatement(sqlQuery1);
            PreparedStatement preparedStatement2 = connection.prepareStatement(sqlQuery2);

            preparedStatement1.setInt(1, warehouseId);
            preparedStatement2.setInt(1, warehouseId);

            ResultSet resultSet1 = preparedStatement1.executeQuery();
            ResultSet resultSet2 = preparedStatement2.executeQuery();

            if (resultSet1.next()) {
                capacitySum = resultSet1.getInt("total_capacity");
            }

            if (resultSet2.next()) {
                occupancySum = resultSet2.getInt("total_occupancy");
            }

            delta = capacitySum - occupancySum;
        } catch (SQLException e) {
            System.err.println("Ошибка2: " + e.getMessage());
        }

        return delta;
    }

    private static int getCellFreeSpace(int storageCellId) {
        int freeSpace = 0;

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            String sqlQuery = "SELECT capacity, occupancy FROM storage_cells WHERE id = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);

            preparedStatement.setInt(1, storageCellId);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int capacity = resultSet.getInt("capacity");
                int occupancy = resultSet.getInt("occupancy");
                freeSpace = capacity - occupancy;
            }

        } catch (SQLException e) {
            System.err.println("Ошибка3: " + e.getMessage());
        }

        return freeSpace;
    }

    private static List<Integer> getCellsList(int warehouseId) {
        List<Integer> cells = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            String sqlQuery = "SELECT id FROM storage_cells WHERE warehouse_id = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
            preparedStatement.setInt(1, warehouseId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int data = resultSet.getInt("id");
                cells.add(data);
            }

            connection.close();
            resultSet.close();
            preparedStatement.close();
        }catch (SQLException e) {
            System.err.println("Ошибка4: " + e.getMessage());
        }

        Iterator<Integer> cellIterator = cells.iterator();
        while (cellIterator.hasNext()) {
            if (getCellFreeSpace(cellIterator.next()) == 0) {
                cellIterator.remove();
            }
        }

        return cells;
    }

    private static void addIntoTable(String name, int sellPrice, int buyPrice, int storageCellId,
                                     int quantity, int manufactureId) {
        String sqlQuery = "INSERT INTO products (name, sell_price, buy_price, storage_cell_id, quantity, " +
                "manufacture_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);

            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, sellPrice);
            preparedStatement.setInt(3, buyPrice);
            preparedStatement.setInt(4, storageCellId);
            preparedStatement.setInt(5, quantity);
            preparedStatement.setInt(6, manufactureId);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении товара: " + e.getMessage());
        }
    }
}
