package org.example.objects.storage;

import org.example.appсontrol.database.DataBase;
import org.example.appсontrol.menu.AppMenu;
import org.example.appсontrol.services.Check;
import org.example.appсontrol.services.SafeInput;
import org.example.appсontrol.terminal.OutputController;

import java.sql.*;

public class SalePoint {

    // метод для создания нового пункта продаж
    public static void openNewSellPoint() {
        // запрашиваем город
        String city = SafeInput.stringInput("Введите город: ");
        // проверяем что город не пустой
        Check.stringNotEmpty(city);

        // запрашиваем адрес
        String address = SafeInput.stringInput("Введите адрес: ");
        // проверяем что адрес не пустой
        Check.stringNotEmpty(address);

        // добавляем пункт продаж в базу
        addIntoTable(city, address);

        // получаем id нового пункта продаж
        int warehouseId = (int) DataBase.getCellValueByTwoConditions("sale_points", "id",
                "city", city, "address", address);

        // запрашиваем вместимость пункта
        int capacity = SafeInput.safeIntInput("Введите вместительность пункта продаж: ");

        // создаем ячейку для пункта продаж
        StorageCell.addIntoTable(capacity, warehouseId, "ячейка пункта продаж");
    }

    // метод для закрытия пункта продаж
    public static void closeSalePoint() {
        // выводим список пунктов продаж
        System.out.println("Выберите id пункта продаж, который нужно закрыть, из списка ниже: ");
        DataBase.printAll("sale_points", 4);

        // получаем id пункта для закрытия
        int salePointId = SafeInput.safeIntInput("Ваш выбор: ");

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            // увольняем всех работников этого пункта
            String sqlQuery = String.format("UPDATE workers SET status = 'уволен' WHERE +" +
                    "work_place_id = %s AND status = 'работает на пункте продаж'", salePointId);

            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(sqlQuery);

                // удаляем пункт продаж из базы
                DataBase.removeRaw("sale_points", salePointId);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при закрытии склада: " + e.getMessage());
        }

        System.out.println("Пункт продаж закрыт");
    }

    // метод для вывода информации о пункте продаж
    public static void printSalePointsInfo() {
        // получаем id пункта продаж
        int salePointId = printSalePoints();

        if (salePointId == 0) {
            return;
        }

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            // запрос для получения информации о пункте
            String sqlQuery = "SELECT city, address, manager_id FROM sale_points WHERE id = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                preparedStatement.setInt(1, salePointId);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        String address = resultSet.getString("address");
                        String city = resultSet.getString("city");
                        int managerId = resultSet.getInt("manager_id");

                        // проверяем назначено ли ответственное лицо
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

    // метод для смены ответственного лица
    public static void changeManager() {
        // получаем id пункта продаж
        int salePointId = printSalePoints();

        if (salePointId == 0) {
            return;
        }

        int rsLength = 0;

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            // получаем список работников пункта
            String sqlQuery = "SELECT * FROM workers WHERE work_place_id = ? AND status = 'работает на пункте продаж'";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                preparedStatement.setInt(1, salePointId);

                // выводим информацию о работниках
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        for (int i = 1; i <= 6; i++) {
                            System.out.print(resultSet.getString(i) + "\t");
                        }

                        rsLength++;
                        System.out.println();
                    }
                }
            }

            // проверяем есть ли работники в пункте
            if (rsLength == 0) {
                System.out.println("В выбранном пункте продаж нет ни одного работника");
                return;
            }

            // запрашиваем id нового ответственного
            int managerId = SafeInput.safeIntInput("Введите номер работника, которого вы хотите назначить " +
                    "ответственным лицом: ");

            // обновляем ответственного в базе
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

    // метод для вывода списка пунктов продаж
    public static int printSalePoints() {
        System.out.println("Выберите пункт продаж из списка ниже: ");

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            // запрос для получения всех пунктов продаж
            String sqlQuery = "SELECT city, address FROM sale_points";

            try (Statement statement = connection.createStatement()) {
                // выводим список пунктов
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

        // запрашиваем город и адрес для поиска
        String city = SafeInput.stringInput("Введите город подходящего пункта выдачи: ");
        String address = SafeInput.stringInput("Введите адрес подходящего пункта продаж: ");

        int salePointId = 0;

        // проверяем существование пункта
        if (DataBase.getCellValueByTwoConditions("sale_points", "id",
                "city", city, "address", address) == null) {
            System.out.println("Некорректно введены данные!");
        } else {
            // получаем id пункта
            salePointId = (int) DataBase.getCellValueByTwoConditions("sale_points", "id",
                    "city", city, "address", address);
        }

        return salePointId;
    }

    // метод для вывода товаров готовых к заказу
    public static void printReadyToOrderProducts(int salePointId) {
        int count = 0;

        // получаем id ячейки пункта продаж
        int storageCellId = (int) DataBase.getCellValueByTwoConditions("storage_cells", "id",
                "status", "ячейка пункта продаж", "storage_id", salePointId);

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            // запрос для получения товаров в ячейке
            String sqlQuery = "SELECT name, quantity FROM products WHERE storage_cell_id = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                preparedStatement.setInt(1, storageCellId);

                // выводим список товаров
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

        // если товаров нет
        if (count == 0) {
            System.out.println("Товары в выбранном пункте продаж отсутствуют");
            OutputController.waitForEnter();
            AppMenu.showAppMenu();
        }
    }

    // метод для получения id ячейки пункта продаж
    public static int getCellId(int salePointId) {
        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            // запрос для получения id ячейки
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

    // метод для добавления пункта продаж в базу
    private static void addIntoTable(String city, String address) {
        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            // запрос для добавления пункта
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