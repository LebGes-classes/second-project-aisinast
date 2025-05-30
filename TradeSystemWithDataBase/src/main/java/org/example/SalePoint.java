package org.example;

import org.example.database.DataBase;
import org.example.menu.AppMenu;
import org.example.services.Check;
import org.example.services.SafeInput;
import org.example.terminal.OutputController;

import java.sql.*;
import java.util.Scanner;

public class SalePoint {
    private int id;
    private String city;
    private String address;
    private int managerId;

    static Scanner scanner = new Scanner(System.in);

    public static void openNewSellPoint() {

        String city = SafeInput.stringInput("Введите город: ");

        Check.stringNotEmpty(city);

        String address = SafeInput.stringInput("Введите адрес: ");

        Check.stringNotEmpty(address);

        addIntoTable(city, address);

        int warehouseId = (int) DataBase.getCellValueByTwoConditions("sale_points", "id",
                "city", city, "address", address);

        int capacity = SafeInput.safeIntInput("Введите вместительность пункта продаж: ");

        StorageCell.addIntoTable(capacity, warehouseId, "ячейка пункта продаж");
    }

    public static void closeSalePoint() {
        System.out.println("Выберите id пункта продаж, который нужно закрыть, из списка ниже: ");

        DataBase.printAll("sale_points", 4);

        int salePointId = SafeInput.safeIntInput("Ваш выбор: ");

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            String sqlQuery = String.format("UPDATE workers SET status = 'уволен' WHERE +" +
                    "work_place_id = %s AND status = 'работает на пункте продаж'", salePointId);

            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(sqlQuery);

                DataBase.removeRaw("sale_points", salePointId);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при закрытии склада: " + e.getMessage());
        }

        System.out.println("Пункт продаж закрыт");
    }

    public static void printSalePointsInfo() {
        int salePointId = printSalePoints();

        if (salePointId == 0) {
            return;
        }

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            String sqlQuery = "SELECT city, address, manager_id FROM sale_points WHERE id = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                preparedStatement.setInt(1, salePointId);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        String address = resultSet.getString("address");
                        String city = resultSet.getString("city");
                        int managerId = resultSet.getInt("manager_id");

                        if (resultSet.wasNull()) {
                            System.out.println("Пункт продаж: " + city + ", " + address +
                                    ". Ответственное лицо не назначено");
                        } else {
                            System.out.println("Пункт продаж: " + city + ", " + address +
                                    ". Ответственное лицо: " + Warehouse.getManagerName(managerId));
                        }
                    } else {
                        System.out.println("Запись не найдена");

                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при выводе информации о пункте продаж: " + e.getMessage());
        }
    }

    public static void changeManager() {
        int salePointId = printSalePoints();

        if (salePointId == 0) {
            return;
        }

        int rsLength = 0;

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            String sqlQuery = "SELECT * FROM workers WHERE work_place_id = ? AND status = 'работает на пункте продаж'";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                preparedStatement.setInt(1, salePointId);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        for (int i = 1; i <= 6; i++) {
                            System.out.print(resultSet.getString(i) + "\t");
                        }

                        rsLength ++;

                        System.out.println();
                    }
                }
            }

            if (rsLength == 0) {
                System.out.println("В выбранном пункте продаж нет ни одного работника");
                return;
            }

            int managerId = SafeInput.safeIntInput("Введите номер работника, которого вы хотите назначить " +
                    "ответственным лицом: ");

            String newSqlQuery = "UPDATE sale_points SET manager_id = ? WHERE id = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(newSqlQuery)) {
                preparedStatement.setInt(1, managerId);
                preparedStatement.setInt(2, salePointId);

                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при смене ответственного лица: " + e.getMessage());
        }

        System.out.println("Смена ответственного лица произошла успешно");
    }

    public static int printSalePoints() {
        System.out.println("Выберите пункт продаж из списка ниже: ");

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            String sqlQuery = "SELECT city, address FROM sale_points";

            try (Statement statement = connection.createStatement()) {
                try (ResultSet resultSet = statement.executeQuery(sqlQuery)) {
                    while (resultSet.next()) {
                        String city = resultSet.getString("city");
                        String address = resultSet.getString("address");

                        System.out.println(city + ", " + address);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при выводе пунктов продаж: " + e.getMessage());
        }

        String city = SafeInput.stringInput("Введите город подходящего пункта выдачи: ");

        String address = SafeInput.stringInput("Введите адрес подходящего пункта продаж: ");

        int salePointId = 0;

        if (DataBase.getCellValueByTwoConditions("sale_points", "id",
                "city", city, "address", address) == null) {
            System.out.println("Некорректно введены данные!");
        } else {
            salePointId = (int) DataBase.getCellValueByTwoConditions("sale_points", "id",
                    "city", city, "address", address);
        }

        return salePointId;
    }

    public static void printReadyToOrderProducts(int salePointId) {
        int count = 0;

        int storageCellId = (int) DataBase.getCellValueByTwoConditions("storage_cells", "id",
                "status", "ячейка пункта продаж", "storage_id", salePointId);

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            String sqlQuery = "SELECT name, quantity FROM products WHERE storage_cell_id = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                preparedStatement.setInt(1, storageCellId);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String name = resultSet.getString("name");
                        int quantity = resultSet.getInt("quantity");

                        System.out.println(name + " (" + quantity + " шт.)");
                        count++;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при выводе товаров: " + e.getMessage());
        }

        if (count == 0) {
            System.out.println("Товары в выбранном пункте продаж отсутствуют");
            OutputController.waitForEnter();
            AppMenu.showAppMenu();
        }
    }

    public static int getCellId(int salePointId) {
        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            String sqlQuery = "SELECT id FROM storage_cells WHERE storage_id = ? AND status = 'ячейка пункта продаж'";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                preparedStatement.setInt(1, salePointId);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении ячейки: " + e.getMessage());
        }

        return 0;
    }

    private static void addIntoTable(String city, String address) {
        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            String sqlQuery = "INSERT INTO sale_points (city, address) VALUES (?, ?)";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                preparedStatement.setString(1, city);
                preparedStatement.setString(2, address);
                preparedStatement.executeUpdate();
            }

            System.out.println("Пункт продаж добавлен успешно!");
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении пункта продаж: " + e.getMessage());
        }
    }
}
