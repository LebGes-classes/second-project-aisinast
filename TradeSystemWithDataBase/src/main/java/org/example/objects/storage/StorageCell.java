package org.example.objects.storage;

import org.example.appсontrol.database.DataBase;
import org.example.appсontrol.services.SafeInput;

import java.sql.*;

public class StorageCell {

    // метод для добавления новой ячейки склада
    public static void addNewCell() {
        // устанавливаем статус по умолчанию
        String status = "ячейка склада";

        int warehouseId = 0;

        // выводим список складов для выбора
        System.out.println("На какой склад добавить ячейку?");
        // получаем выбранный склад
        String warehouse = Warehouse.printWarehousesAndChoose();
        // получаем id склада
        warehouseId = DataBase.getId("warehouses", "name", warehouse);

        // запрашиваем вместимость ячейки
        int capacity = SafeInput.safeIntInput("Введите вместимость: ");

        // проверяем корректность введенной вместимости
        if (capacity <= 0) {
            System.out.println("Ячейка не может иметь отрицательную или нулевую вместимость");
        }

        // добавляем ячейку в базу данных
        addIntoTable(capacity, warehouseId, status);
    }

    // метод для изменения заполненности ячейки
    public static void changeOccupancy(int storageCellId, int occupancyChange) {
        // запрос для обновления занятости
        String sqlQuery = "UPDATE storage_cells SET occupancy = ? WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            // получаем текущую заполненность и добавляем изменение
            int newOccupancy = getCurrentOccupancy(connection, storageCellId) + occupancyChange;

            // подготавливаем запрос
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);

            // устанавливаем параметры
            preparedStatement.setInt(1, newOccupancy);
            preparedStatement.setInt(2, storageCellId);

            // выполняем обновление
            preparedStatement.executeUpdate();

            // закрываем ресурсы
            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    // метод для получения свободного места в ячейке
    public static int getCellFreeSpace(int storageCellId) {
        int freeSpace = 0;

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            // запрос для получения вместимости и занятости
            String sqlQuery = "SELECT capacity, occupancy FROM storage_cells WHERE id = ?";

            // подготавливаем запрос
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);

            // устанавливаем параметр
            preparedStatement.setInt(1, storageCellId);

            // выполняем запрос
            ResultSet resultSet = preparedStatement.executeQuery();

            // вычисляем свободное место
            while (resultSet.next()) {
                int capacity = resultSet.getInt("capacity");
                int occupancy = resultSet.getInt("occupancy");
                freeSpace = capacity - occupancy;
            }

            // закрываем ресурсы
            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }

        return freeSpace;
    }

    // метод для получения текущей заполненности
    public static int getCurrentOccupancy(Connection connection, int storageCellId) throws SQLException {
        // запрос для получения текущей занятости
        String sqlQuery = "SELECT occupancy FROM storage_cells WHERE id = ?";
        int occupancy = 0;
        // подготавливаем и выполняем запрос
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

    // метод для добавления новой ячейки в бд
    public static void addIntoTable(int capacity, int warehouseId, String status) {
        // запрос для добавления ячейки
        String sqlQuery = "INSERT INTO storage_cells (capacity, occupancy, storage_id, status) VALUES (?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            // подготавливаем запрос
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);

            // устанавливаем параметры
            preparedStatement.setInt(1, capacity);
            preparedStatement.setInt(2, 0); // начальная занятость = 0
            preparedStatement.setInt(3, warehouseId);
            preparedStatement.setString(4, status);

            // выполняем запрос
            preparedStatement.executeUpdate();

            // закрываем ресурсы
            preparedStatement.close();

            System.out.println("Ячейка успешно добавлена!");
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении ячейки склада: " + e.getMessage());
        }
    }
}
