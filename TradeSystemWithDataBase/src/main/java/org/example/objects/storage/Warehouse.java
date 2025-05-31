package org.example.objects.storage;

import org.example.objects.product.Product;
import org.example.appсontrol.database.DataBase;
import org.example.appсontrol.services.Check;
import org.example.appсontrol.services.SafeInput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.sql.*;

import static org.example.objects.product.Product.getCellsList;
import static org.example.objects.storage.StorageCell.getCellFreeSpace;

public class Warehouse {

    private static final String tableName = "warehouses";

    // метод для добавления нового склада
    public static void addNewWarehouse() {
        // запрос города у пользователя
        String city = SafeInput.stringInput("Введите город: ");

        try {
            // проверка, что строка не пустая
            Check.stringNotEmpty(city);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }

        // создание имени склада
        String name = createWarehouseName(city);

        // запрос адреса у пользователя
        String address = SafeInput.stringInput("Введите адрес: ");

        try {
            // проверка, что строка не пустая
            Check.stringNotEmpty(address);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }

        // добавление склада в таблицу
        addStorageIntoTable(name, city, address);
    }

    // метод для закрытия склада
    public static void closeWarehouse() {
        // выбор склада из списка
        String warehouse = printWarehousesAndChoose();

        // проверка существования склада
        if (DataBase.countRowsWithCondition("warehouses", "name", warehouse) == 0) {
            System.out.println("Такого склада не существует");
            return;
        }

        // получение id склада
        int warehouseId = DataBase.getId("warehouses", "name", warehouse);

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            // увольнение работников склада
            String sqlQuery = String.format("UPDATE workers SET status = 'уволен' " +
                    "WHERE work_place_id = %s AND status = 'работает на складе'", warehouseId);
            Statement statement = connection.createStatement();
            statement.executeUpdate(sqlQuery);

            // удаление склада из таблицы
            DataBase.removeRaw("warehouses", warehouseId);

            statement.close();

            System.out.println("Склад " + warehouse + " закрыт");
        } catch (SQLException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    // метод для вывода информации о складе
    public static void printWarehouseInfo() {
        // выбор склада из списка
        String warehouse = printWarehousesAndChoose();

        // вывод информации о складе
        getWarehouseInfo(warehouse);
    }

    // метод для смены управляющего лица
    public static void changeManager() {
        // выбор склада из списка
        String warehouse = printWarehousesAndChoose();
        int warehouseId = DataBase.getId("warehouses", "name", warehouse);

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            // получение списка работников склада
            String sqlQuery = "SELECT * FROM workers WHERE work_place_id = ? AND status = 'работает на складе'";

            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
            preparedStatement.setInt(1, warehouseId);

            ResultSet resultSet = preparedStatement.executeQuery();

            // вывод информации о работниках
            while (resultSet.next()) {
                for (int i = 1; i <= 6; i++) {
                    System.out.print(resultSet.getString(i) + "\t");
                }
                System.out.println();
            }

            // выбор нового менеджера
            int choice = SafeInput.safeIntInput("Введите номер работника, которого вы хотите назначить " +
                    "ответственным лицом: ");

            // обновление менеджера склада
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
    }

    // метод для получения информации о складе
    private static void getWarehouseInfo(String name) {
        // получение id склада
        int id = DataBase.getId(tableName, "name", name);
        String sqlQuery = "SELECT name, address, manager_id FROM warehouses WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())){
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);

            preparedStatement.setInt(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();

            // вывод информации о складе
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

    // метод для вывода списка складов
    public static String printWarehousesAndChoose() throws IndexOutOfBoundsException {
        // получение списка складов
        List<String> warehouses = DataBase.columnToList(tableName, "name");

        String[] warehousesArray = warehouses.toArray(new String[0]);

        System.out.println("Выберите номер склада из списка: ");

        // вывод списка складов
        for (int i = 0; i < warehousesArray.length; i++) {
            System.out.println(i + 1 + ". " + warehousesArray[i]);
        }

        // выбор склада пользователем
        int warehouseNumber = SafeInput.safeIntInput("Ваш выбор: ") - 1;

        return warehousesArray[warehouseNumber];
    }

    // приватный метод для создания имени
    private static String createWarehouseName(String city) {
        // создание имени склада на основе города и количества
        return city + "-" + (DataBase.countRowsWithCondition(tableName, "city", city) + 1);
    }

    // приватный класс для удобства перемещения товаров
    private static class Node {
        int id;
        String name;
        int quantity;

        public Node(int id, String name, int quantity) {
            this.id = id;
            this.name = name;
            this.quantity = quantity;
        }

        public String toString() {
            return id + ". " + name + " (" + quantity + " шт.)";
        }

    }

    // метод для передвижения товаров
    public static void moveToSellPoint() {
        String warehouse = null;

        try {
            // выбор склада
            warehouse = printWarehousesAndChoose();
        } catch (IndexOutOfBoundsException e) {
            System.err.println("Ошибка при выборе склада: " + e.getMessage());
            return;
        }
        int warehouseId = DataBase.getId("warehouses", "name", warehouse);

        System.out.println("Выберите товар, который нужно переместить, и его количество:");

        // получение списка ячеек склада
        List<Integer> cells = getCellsList(warehouseId);
        if (cells.isEmpty()) {
            System.out.println("Ошибка: На складе с ID " + warehouseId + " нет ячеек хранения");
            return;
        }

        String params = String.join(",", Collections.nCopies(cells.size(), "?"));

        ArrayList<Node> nodes = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            // получение товаров со склада
            String sqlQuery = "SELECT * FROM products WHERE storage_cell_id IN (" + params + ")";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                for (int i = 0; i < cells.size(); i++) {
                    preparedStatement.setInt(i + 1, cells.get(i));
                }
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    int i = 1;
                    // формирование списка товаров
                    while (resultSet.next()) {
                        Node node = new Node(resultSet.getInt("id"), resultSet.
                                getString("name"), resultSet.getInt("quantity"));
                        System.out.println(node.toString());
                        nodes.add(node);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при перемещении товара: " + e.getMessage());
            return;
        }

        if (nodes.isEmpty()) {
            System.out.println("На этом складе нет товаров");
            return;
        }

        // выбор товара для перемещения
        int productId = SafeInput.safeIntInput("Введите id товара: ");

        String productName = (String) DataBase.getCellValue("products",
                "name", "id", productId);

        int sellPrice = (int) DataBase.getCellValue("products", "sell_price",
                "id", productId);
        int buyPrice = (int) DataBase.getCellValue("products", "buy_price",
                "id", productId);
        int manufactureId = (int) DataBase.getCellValue("products", "manufacture_id",
                "id", productId);

        // ввод количества товара
        int quantity = SafeInput.safeIntInput("Введите количество: ");

        if (quantity <= 0) {
            System.out.println("Количество не может быть меньше или равно нулю");
            return;
        } else if (quantity > countProductsInWarehouse(warehouseId, productName)) {
            System.out.println("Количество не может быть больше имеющегося на складе");
            return;
        }

        // выбор пункта продажи
        int salePointId = SalePoint.printSalePoints();
        int salePointCellId = SalePoint.getCellId(salePointId);

        if (quantity > getCellFreeSpace(salePointCellId)) {
            System.out.println("На выбранном пункте продаж не достаточно места");
            return;
        }

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            int remainingQuantity = quantity;
            // перемещение товара из ячеек склада
            for (int cellId : cells) {
                if (remainingQuantity <= 0) break;

                String sqlQuery = "SELECT quantity FROM products WHERE name = ? AND storage_cell_id = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                    preparedStatement.setString(1, productName);
                    preparedStatement.setInt(2, cellId);
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        if (resultSet.next()) {
                            int currentQuantity = resultSet.getInt("quantity");
                            int qtyToMove = Math.min(remainingQuantity, currentQuantity);

                            // обновление количества товара на складе
                            String updateQuery = "UPDATE products SET quantity = quantity - ? WHERE name = ? " +
                                    "AND storage_cell_id = ?";
                            try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                                updateStmt.setInt(1, qtyToMove);
                                updateStmt.setString(2, productName);
                                updateStmt.setInt(3, cellId);
                                updateStmt.executeUpdate();
                            }

                            StorageCell.changeOccupancy(cellId, -qtyToMove);
                            remainingQuantity -= qtyToMove;
                        }
                    }
                }
            }

            // добавление товара в пункт продажи
            Product.addIntoTable(productName, sellPrice, buyPrice, salePointCellId, quantity, manufactureId);
            StorageCell.changeOccupancy(salePointCellId, quantity);

            System.out.println(productName + " (" + quantity + " шт.) перемещен на пункт продаж");

        } catch (SQLException e) {
            System.err.println("Ошибка при перемещении товара: " + e.getMessage());
        }

        // удаление товаров с нулевым количеством
        deleteProductsWhereQuantityNull(productName);
    }

    // метод для получения имени управляющего
    public static String getManagerName(int managerId) {
        String sqlQuery = "SELECT name, surname FROM workers WHERE id = ?";

        String name = "";
        String surname = "";

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())){
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
            preparedStatement.setInt(1, managerId);
            ResultSet resultSet = preparedStatement.executeQuery();

            // получение имени и фамилии менеджера
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

    // метод для подсчета количества продуктов на складе
    private static int countProductsInWarehouse(int warehouseId, String productName) {
        // получение списка ячеек склада
        List<Integer> cells = getCellsList(warehouseId);
        String params = String.join(",", Collections.nCopies(cells.size(), "?"));

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            // подсчет количества товара на складе
            String sqlQuery = "SELECT SUM(quantity) FROM products WHERE name = ? AND storage_cell_id IN (" + params + ")";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                preparedStatement.setString(1, productName);
                for (int i = 0; i < cells.size(); i++) {
                    preparedStatement.setInt(i + 2, cells.get(i));
                }

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при подсчете продуктов: " + e.getMessage());
        }

        return 0;
    }

    // приватный метод для удаления продукта из таблицы, если его количество равно 0
    private static void deleteProductsWhereQuantityNull (String productName) {
        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())){
            // удаление товаров с нулевым количеством
            String deleteQuery = "DELETE FROM products WHERE name = ? AND quantity = 0";
            try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
                deleteStmt.setString(1, productName);
                deleteStmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // приватный метод для добавления склада в таблицу
    private static void addStorageIntoTable(String name, String city, String address) {
        String sqlQuery = "INSERT INTO warehouses (name, city, address) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            PreparedStatement pstmt = conn.prepareStatement(sqlQuery);

            pstmt.setString(1, name);
            pstmt.setString(2, city);
            pstmt.setString(3, address);

            pstmt.executeUpdate();

            // получение id добавленного склада
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