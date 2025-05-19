package org.example;

import org.example.database.DataBase;
import org.example.services.Check;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.sql.*;
import java.util.Scanner;

import static org.example.Product.getCellsList;

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

        if (DataBase.sqliteCountRowsWithCondition("warehouses", "name", warehouse) == 0) {
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

    public static String printWarehousesAndChoose() throws IndexOutOfBoundsException {
        List<String> warehouses = DataBase.columnToList(tableName, "name");

        String[] warehousesArray = warehouses.toArray(new String[0]);

        System.out.println("Выберите номер склада из списка: ");

        for (int i = 0; i < warehousesArray.length; i++) {
            System.out.println(i + 1 + ". " + warehousesArray[i]);
        }

        System.out.print("Ваш выбор: ");
        int warehouseNumber = scanner.nextInt() - 1;
        scanner.nextLine();

        return warehousesArray[warehouseNumber];
    }

    private static String createWarehouseName(String city) {
        return city + "-" + (DataBase.sqliteCountRowsWithCondition(tableName, "city", city) + 1);
    }

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

    public static void moveToSellPoint() {
        String warehouse = null;
        List<Node> products = new ArrayList<>();

        try {
            warehouse = printWarehousesAndChoose();
        } catch (IndexOutOfBoundsException e) {
            System.err.println("Ошибка при выборе склада: " + e.getMessage());
            return;
        }
        int warehouseId = DataBase.getId("warehouses", "name", warehouse);

        System.out.println("Выберите товар, который нужно переместить, и его количество:");

        List<Integer> cells = getCellsList(warehouseId);
        if (cells.isEmpty()) {
            System.out.println("Ошибка: На складе с ID " + warehouseId + " нет ячеек хранения");
            return;
        }

        String params = String.join(",", Collections.nCopies(cells.size(), "?"));

        ArrayList<Node> nodes = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            String sqlQuery = "SELECT * FROM products WHERE storage_cell_id IN (" + params + ")";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                for (int i = 0; i < cells.size(); i++) {
                    preparedStatement.setInt(i + 1, cells.get(i));
                }
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    int i = 1;
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

        System.out.print("Введите id товара: ");
        int productId = scanner.nextInt();
        scanner.nextLine();

        String productName = (String) DataBase.getCellValue("products",
                "name", "id", productId);

        int sellPrice = (int) DataBase.getCellValue("products", "sell_price",
                "id", productId);
        int buyPrice = (int) DataBase.getCellValue("products", "buy_price",
                "id", productId);
        int manufactureId = (int) DataBase.getCellValue("products", "manufacture_id",
                "id", productId);

        System.out.print("Введите количество: ");
        int quantity = scanner.nextInt();
        scanner.nextLine();

        if (quantity <= 0) {
            System.out.println("Количество не может быть меньше или равно нулю");
            return;
        } else if (quantity > countProductsInWarehouse(warehouseId, productName)) {
            System.out.println("Количество не может быть больше имеющегося на складе");
            return;
        }

        int salePointId = SalePoint.printSalePoints();
        int salePointCellId = SalePoint.getCellId(salePointId);

        if (quantity > Product.getCellFreeSpace(salePointCellId)) {
            System.out.println("На выбранном пункте продаж не достаточно места");
            return;
        }

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            int remainingQuantity = quantity;
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

                            String updateQuery = "UPDATE products SET quantity = quantity - ? WHERE name = ? " +
                                    "AND storage_cell_id = ?";
                            try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                                updateStmt.setInt(1, qtyToMove);
                                updateStmt.setString(2, productName);
                                updateStmt.setInt(3, cellId);
                                updateStmt.executeUpdate();
                            }

                            Product.changeOccupancy(cellId, -qtyToMove);
                            remainingQuantity -= qtyToMove;
                        }
                    }
                }
            }

            Product.addIntoTable(productName, sellPrice, buyPrice, salePointCellId, quantity, manufactureId);
            Product.changeOccupancy(salePointCellId, quantity);

            System.out.println(productName + " (" + quantity + " шт.) перемещен на пункт продаж");

        } catch (SQLException e) {
            System.err.println("Ошибка при перемещении товара: " + e.getMessage());
        }

        deleteProductsWhereQuantityNull(productName);
    }

    public static String getManagerName(int managerId) {
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

    private static int countProductsInWarehouse(int warehouseId, String productName) {
        List<Integer> cells = getCellsList(warehouseId);
        String params = String.join(",", Collections.nCopies(cells.size(), "?"));

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
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

    private static void deleteProductsWhereQuantityNull (String productName) {
        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())){
            String deleteQuery = "DELETE FROM products WHERE name = ? AND quantity = 0";
            try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
                deleteStmt.setString(1, productName);
                deleteStmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
