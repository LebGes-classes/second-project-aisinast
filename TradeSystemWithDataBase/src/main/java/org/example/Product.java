package org.example;

import org.example.accounting.SalesAccounting;
import org.example.database.DataBase;
import org.example.services.Check;
import org.example.services.SafeInput;
import org.example.terminal.OutputController;

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
        if (DataBase.countRowsWithCondition("warehouses", "name", warehouse) == 0) {
            System.out.println("Ячейки на этом складе отсутствуют");
            return;
        }

        int warehouseId = DataBase.getId("warehouses", "name", warehouse);

        String name = SafeInput.stringInput("Введите название товара: ");

        try {
            Check.stringNotEmpty(name);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
            return;
        }

        int sellPrice = SafeInput.safeIntInput("Введите цену продажи: ");

        if (sellPrice < 0) {
            System.out.println("Цена не может быть меньше 0");
        }

        int buyPrice = SafeInput.safeIntInput("Введите цену покупки: ");

        if (buyPrice < 0) {
            System.out.println("Цена не может быть меньше 0");
        }

        int quantity = SafeInput.safeIntInput("Введите количество: ");

        if (quantity < 0) {
            System.out.println("Количество не может быть меньше 0");
            return;
        } else if (quantity > getFreeSpaceInWarehouse(warehouseId)) {
            System.out.println("Недостаточно свободного места");
            return;
        }

        System.out.println("Выберите производство из списка ниже: ");
        DataBase.printAll("manufactures", 3);

        int manufactureId = SafeInput.safeIntInput("Ваш выбор (введите id) : ");

        List<Integer> cells = getCellsList(warehouseId);

        Iterator<Integer> cellIterator = cells.iterator();
        while (cellIterator.hasNext()) {
            if (getCellFreeSpace(cellIterator.next()) == 0) {
                cellIterator.remove();
            }
        }

        if (getCellFreeSpace(cells.getFirst()) >= quantity) {
            addIntoTable(name, sellPrice, buyPrice, cells.getFirst(), quantity, manufactureId);
            changeOccupancy(cells.getFirst(), quantity);
        } else {
            putInSeveralCells(name, buyPrice, sellPrice, quantity, manufactureId, warehouseId);
        }

        SalesAccounting.registerSale("sale_point", buyPrice * quantity, manufactureId, name);
    }

    public static void printProductsInfo(String warehouse) {
        OutputController.clearConsole();

        int warehouseId = DataBase.getId("warehouses", "name", warehouse);
        List<Integer> cells = getCellsList(warehouseId);
        String params = String.join(",", Collections.nCopies(cells.size(), "?"));
        String sqlQuery = "SELECT * FROM products WHERE storage_cell_id IN (" + params + ")";

        try (Connection conn = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
             PreparedStatement preparedStatement = conn.prepareStatement(sqlQuery);

             for (int i = 0; i < cells.size(); i++) {
                 preparedStatement.setInt(i + 1, cells.get(i));
             }

             ResultSet resultSet = preparedStatement.executeQuery();

             while (resultSet.next()) {
                 System.out.println(resultSet.getString("name") +
                         ": \nКоличество: " + resultSet.getInt("quantity")
                         + "\nЗакупочная цена: " + resultSet.getInt("buy_price") +
                         ", цена продажи: " + resultSet.getInt("sell_price"));
                 System.out.println();
             }

             resultSet.close();
             preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    private static void putInSeveralCells(String name, int buyPrice, int sellPrice, int quantity,
                                          int manufactureId, int warehouseId) {
        List<Integer> cells = getCellsList(warehouseId);

        Iterator<Integer> cellIterator = cells.iterator();
        while (cellIterator.hasNext()) {
            if (getCellFreeSpace(cellIterator.next()) == 0) {
                cellIterator.remove();
            }
        }

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

    public static int getCurrentOccupancy(Connection connection, int storageCellId) throws SQLException {
        String sqlQuery = "SELECT occupancy FROM storage_cells WHERE id = ?";
        int occupancy = 0;
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setInt(1, storageCellId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    occupancy = resultSet.getInt("occupancy");
                }
            }
        }
        return occupancy;
    }

    public static void changeOccupancy(int storageCellId, int occupancyChange) {
        String sqlQuery = "UPDATE storage_cells SET occupancy = ? WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            int newOccupancy = getCurrentOccupancy(connection, storageCellId) + occupancyChange;

            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);

            preparedStatement.setInt(1, newOccupancy);
            preparedStatement.setInt(2, storageCellId);

            preparedStatement.executeUpdate();

            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    private static int getFreeSpaceInWarehouse(int warehouseId) {
        int capacitySum = 0;
        int occupancySum = 0;

        int delta = 0;

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            String sqlQuery1 = "SELECT SUM(capacity) AS total_capacity FROM storage_cells WHERE storage_id = ? AND " +
                    "status = 'ячейка склада'";
            String sqlQuery2 = "SELECT SUM(occupancy) AS total_occupancy FROM storage_cells WHERE " +
                    "storage_id = ? AND status = 'ячейка склада'";

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

            resultSet1.close();
            resultSet2.close();
            preparedStatement1.close();
            preparedStatement2.close();
        } catch (SQLException e) {
            System.err.println("Ошибка2: " + e.getMessage());
        }

        return delta;
    }

    public static int getCellFreeSpace(int storageCellId) {
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

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }

        return freeSpace;
    }

    public static List<Integer> getCellsList(int warehouseId) {
        List<Integer> cells = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            String sqlQuery = "SELECT id FROM storage_cells WHERE storage_id = ? AND status = 'ячейка склада'";

            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
            preparedStatement.setInt(1, warehouseId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int data = resultSet.getInt("id");
                cells.add(data);
            }

            resultSet.close();
            preparedStatement.close();
        }catch (SQLException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }

        return cells;
    }

    static void addIntoTable(String name, int sellPrice, int buyPrice, int storageCellId,
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

            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении товара: " + e.getMessage());
        }
    }
}
