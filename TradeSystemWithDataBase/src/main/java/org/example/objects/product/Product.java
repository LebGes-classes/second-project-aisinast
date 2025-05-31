package org.example.objects.product;

import org.example.appсontrol.accounting.SalesAccounting;
import org.example.appсontrol.database.DataBase;
import org.example.appсontrol.services.Check;
import org.example.appсontrol.services.SafeInput;

import java.sql.*;
import java.util.*;

import static org.example.objects.storage.StorageCell.changeOccupancy;
import static org.example.objects.storage.StorageCell.getCellFreeSpace;

public class Product {

    public static void addNewProduct(String warehouse) {
        // проверяем существование склада
        if (DataBase.countRowsWithCondition("warehouses", "name", warehouse) == 0) {
            System.out.println("Ячейки на этом складе отсутствуют");
            return;
        }

        // получаем id склада
        int warehouseId = DataBase.getId("warehouses", "name", warehouse);

        // запрашиваем название товара
        String name = SafeInput.stringInput("Введите название товара: ");

        try {
            // проверяем что название не пустое
            Check.stringNotEmpty(name);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
            return;
        }

        // запрашиваем цену продажи
        int sellPrice = SafeInput.safeIntInput("Введите цену продажи: ");

        // проверяем что цена не отрицательная
        if (sellPrice < 0) {
            System.out.println("Цена не может быть меньше 0");
        }

        // запрашиваем цену покупки
        int buyPrice = SafeInput.safeIntInput("Введите цену покупки: ");

        // проверяем что цена не отрицательная
        if (buyPrice < 0) {
            System.out.println("Цена не может быть меньше 0");
        }

        // запрашиваем количество товара
        int quantity = SafeInput.safeIntInput("Введите количество: ");

        // проверяем что количество корректное
        if (quantity < 0) {
            System.out.println("Количество не может быть меньше 0");
            return;
        } else if (quantity > getFreeSpaceInWarehouse(warehouseId)) {
            System.out.println("Недостаточно свободного места");
            return;
        }

        // выводим список производителей
        System.out.println("Выберите производство из списка ниже: ");
        DataBase.printAll("manufactures", 3);

        // запрашиваем id производителя
        int manufactureId = SafeInput.safeIntInput("Ваш выбор (введите id) : ");

        // получаем список ячеек на складе
        List<Integer> cells = getCellsList(warehouseId);

        // удаляем заполненные ячейки из списка
        Iterator<Integer> cellIterator = cells.iterator();
        while (cellIterator.hasNext()) {
            if (getCellFreeSpace(cellIterator.next()) == 0) {
                cellIterator.remove();
            }
        }

        // проверяем можно ли разместить в одной ячейке
        if (getCellFreeSpace(cells.getFirst()) >= quantity) {
            // добавляем товар в таблицу
            addIntoTable(name, sellPrice, buyPrice, cells.getFirst(), quantity, manufactureId);
            // обновляем занятость ячейки
            changeOccupancy(cells.getFirst(), quantity);
        } else {
            // распределяем по нескольким ячейкам
            putInSeveralCells(name, buyPrice, sellPrice, quantity, manufactureId, warehouseId);
        }

        // регистрируем продажу
        SalesAccounting.registerSale("sale_point", buyPrice * quantity, manufactureId, name);
    }

    public static void printProductsInfo(String warehouse) {

        // получаем id склада
        int warehouseId = DataBase.getId("warehouses", "name", warehouse);
        // получаем список ячеек
        List<Integer> cells = getCellsList(warehouseId);
        // формируем параметры для SQL запроса
        String params = String.join(",", Collections.nCopies(cells.size(), "?"));
        String sqlQuery = "SELECT * FROM products WHERE storage_cell_id IN (" + params + ")";

        try (Connection conn = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            // подготавливаем запрос
            PreparedStatement preparedStatement = conn.prepareStatement(sqlQuery);

            // устанавливаем параметры
            for (int i = 0; i < cells.size(); i++) {
                preparedStatement.setInt(i + 1, cells.get(i));
            }

            // выполняем запрос
            ResultSet resultSet = preparedStatement.executeQuery();

            // выводим информацию о товарах
            while (resultSet.next()) {
                System.out.println(resultSet.getString("name") +
                        ": \nКоличество: " + resultSet.getInt("quantity")
                        + "\nЗакупочная цена: " + resultSet.getInt("buy_price") +
                        ", цена продажи: " + resultSet.getInt("sell_price"));
                System.out.println();
            }

            // закрываем ресурсы
            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    private static void putInSeveralCells(String name, int buyPrice, int sellPrice, int quantity,
                                          int manufactureId, int warehouseId) {
        // получаем список ячеек
        List<Integer> cells = getCellsList(warehouseId);

        // удаляем заполненные ячейки
        Iterator<Integer> cellIterator = cells.iterator();
        while (cellIterator.hasNext()) {
            if (getCellFreeSpace(cellIterator.next()) == 0) {
                cellIterator.remove();
            }
        }

        int remainingQuantity = quantity;

        // распределяем товар по ячейкам
        for (int cellId : cells) {
            if (remainingQuantity <= 0) break;

            int cellFreeSpace = getCellFreeSpace(cellId);
            if (cellFreeSpace <= 0) continue;

            // определяем количество для текущей ячейки
            int qtyToAdd = Math.min(cellFreeSpace, remainingQuantity);
            // добавляем товар
            addIntoTable(name, sellPrice, buyPrice, cellId, qtyToAdd, manufactureId);
            // обновляем занятость
            changeOccupancy(cellId, qtyToAdd);
            // уменьшаем оставшееся количество
            remainingQuantity -= qtyToAdd;
        }
    }

    private static int getFreeSpaceInWarehouse(int warehouseId) {
        int capacitySum = 0;
        int occupancySum = 0;
        int delta = 0;

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            // запрос для получения общей вместимости
            String sqlQuery1 = "SELECT SUM(capacity) AS total_capacity FROM storage_cells WHERE storage_id = ? AND " +
                    "status = 'ячейка склада'";
            // запрос для получения общей занятости
            String sqlQuery2 = "SELECT SUM(occupancy) AS total_occupancy FROM storage_cells WHERE " +
                    "storage_id = ? AND status = 'ячейка склада'";

            PreparedStatement preparedStatement1 = connection.prepareStatement(sqlQuery1);
            PreparedStatement preparedStatement2 = connection.prepareStatement(sqlQuery2);

            preparedStatement1.setInt(1, warehouseId);
            preparedStatement2.setInt(1, warehouseId);

            ResultSet resultSet1 = preparedStatement1.executeQuery();
            ResultSet resultSet2 = preparedStatement2.executeQuery();

            // получаем общую вместимость
            if (resultSet1.next()) {
                capacitySum = resultSet1.getInt("total_capacity");
            }

            // получаем общую занятость
            if (resultSet2.next()) {
                occupancySum = resultSet2.getInt("total_occupancy");
            }

            // вычисляем свободное место
            delta = capacitySum - occupancySum;

            // закрываем ресурсы
            resultSet1.close();
            resultSet2.close();
            preparedStatement1.close();
            preparedStatement2.close();
        } catch (SQLException e) {
            System.err.println("Ошибка2: " + e.getMessage());
        }

        return delta;
    }

    public static List<Integer> getCellsList(int warehouseId) {
        List<Integer> cells = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            // запрос для получения id ячеек склада
            String sqlQuery = "SELECT id FROM storage_cells WHERE storage_id = ? AND status = 'ячейка склада'";

            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
            preparedStatement.setInt(1, warehouseId);
            ResultSet resultSet = preparedStatement.executeQuery();

            // заполняем список ячеек
            while (resultSet.next()) {
                int data = resultSet.getInt("id");
                cells.add(data);
            }

            // закрываем ресурсы
            resultSet.close();
            preparedStatement.close();
        }catch (SQLException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }

        return cells;
    }

    public static void addIntoTable(String name, int sellPrice, int buyPrice, int storageCellId,
                                    int quantity, int manufactureId) {
        // запрос для добавления товара
        String sqlQuery = "INSERT INTO products (name, sell_price, buy_price, storage_cell_id, quantity, " +
                "manufacture_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);

            // устанавливаем параметры
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, sellPrice);
            preparedStatement.setInt(3, buyPrice);
            preparedStatement.setInt(4, storageCellId);
            preparedStatement.setInt(5, quantity);
            preparedStatement.setInt(6, manufactureId);

            // выполняем запрос
            preparedStatement.executeUpdate();

            // закрываем ресурсы
            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении товара: " + e.getMessage());
        }
    }
}