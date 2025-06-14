package org.example.objects.buyer;

import org.example.objects.product.Product;
import org.example.appсontrol.accounting.SalesAccounting;
import org.example.appсontrol.database.DataBase;
import org.example.appсontrol.services.SafeInput;
import org.example.objects.storage.SalePoint;
import org.example.objects.storage.StorageCell;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Order {

    // метод для создания нового заказа
    public static void createOrder() {
        // вывод и выбор подходящего пункта продаж
        int salePointId = SalePoint.printSalePoints();

        // получение id ячейки пункта продаж
        int storageCellId = 0;
        try {
            storageCellId = (int) DataBase.getCellValueByTwoConditions("storage_cells", "id",
                    "status", "ячейка пункта продаж", "storage_id", salePointId);
        } catch (NullPointerException e) {
            System.err.println("Ошибка при получении id ячейки пункта продаж");
            return;
        }

        System.out.println("Список товаров доступных к заказу:");
        // вывод списка товаров доступных к заказу
        SalePoint.printReadyToOrderProducts(salePointId);

        String productName = SafeInput.stringInput("Введите название товара: ");

        if (DataBase.countRowsWithCondition("products", "name", productName) == 0) {
            System.out.println("Похоже, такой товар отсутствует. Повторите попытку");
            return;
        }

        int quantity = SafeInput.safeIntInput("Введите количество: ");

        // проверка количества товара на корректность
        if (quantity <= 0) {
            System.out.println("Количество не может быть меньше или равно нулю");
            return;
        } else if (quantity > countProducts(productName, storageCellId)) {
            System.out.println("Количество не может быть больше имеющегося на пункте продаж!");
            return;
        }

        // получение имени и фамилии покупателя для получения id его корзины
        String name = SafeInput.stringInput("Введите имя покупателя: ");
        String surname = SafeInput.stringInput("Введите фамилию покупателя: ");

        int shoppingCardId = 0;
        try {
            // у покупателя и его корзины одинаковые id
            shoppingCardId = (int) DataBase.getCellValueByTwoConditions("buyers", "id",
                    "name", name, "surname", surname);
        } catch (NullPointerException e) {
            System.err.println("Ошибка при получении id пользователя: данные введены некорректно или" +
                    " такого пользователя не существует. Сначала нужно добавить пользователя");
            return;
        }

        // получение стоимости товара
        int sellPrice = (int) DataBase.getCellValueByTwoConditions("products", "sell_price",
                "storage_cell_id", storageCellId, "name", productName);
        int buyPrice = (int) DataBase.getCellValueByTwoConditions("products", "buy_price",
                "storage_cell_id", storageCellId, "name", productName);

        // регистрируем продажу
        SalesAccounting.registerSale("sale_point", (sellPrice - buyPrice) * quantity, salePointId,
                productName);
        int saleId = DataBase.getLastRowId("sale_point_sales");

        // добавляем заказ в таблицу orders
        addOrderIntoTable(productName, quantity, sellPrice, shoppingCardId, salePointId, saleId);

        // уменьшаем количество товара в пункте продаж
        StorageCell.changeOccupancy(storageCellId, -quantity);
    }

    // метод для возврата товара
    public static void returnOrder() {

        // получение имени и фамилии покупателя
        String name = SafeInput.stringInput("Введите имя покупателя: ");
        String surname = SafeInput.stringInput("Введите фамилию покупателя: ");

        int shoppingCardId = 0;
        try {
            // у покупателя и его корзины одинаковые id
            shoppingCardId = (int) DataBase.getCellValueByTwoConditions("buyers", "id",
                    "name", name, "surname", surname);
        } catch (NullPointerException e) {
            System.err.println("Ошибка при получении id пользователя: данные введены некорректно или" +
                    " такого пользователя не существует. Сначала нужно добавить пользователя");
            return;
        }

        if (DataBase.countRowsWithCondition("orders", "shopping_card_id",
                shoppingCardId) == 0) {
           System.out.println("У покупателя еще нет заказов");
           return;
        }

        List<Integer> ordersId = new ArrayList<>();

        // запрос в бд для вывода списка заказов
        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())){
            String sqlQuery = "SELECT * FROM orders WHERE shopping_card_id = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                preparedStatement.setInt(1, shoppingCardId);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        int id = resultSet.getInt(1);
                        String productName = resultSet.getString(2);
                        int quantity = resultSet.getInt(3);
                        int price = resultSet.getInt(4);

                        System.out.println("ID: " + id + "; " + productName + " (" + quantity +
                                " шт.), стоимость: " + price);

                        ordersId.add(id);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении списка заказов: " + e.getMessage());
        }

        int chosenId = SafeInput.safeIntInput("Введите id товара, который нужно вернуть: ");

        if (!ordersId.contains(chosenId)) {
            System.out.println("Некорректно введен id товара");
            return;
        }

        // получаем  данные о товаре
        int salePointId = (int) DataBase.getCellValue("orders", "sale_point_id",
                "id", chosenId);
        int salePointCellId = (int) DataBase.getCellValueByTwoConditions("storage_cells", "id",
                "status", "ячейка пункта продаж", "storage_id", salePointId);

        int quantity = (int) DataBase.getCellValue("orders", "quantity",
                "id", chosenId);
        String productName = (String) DataBase.getCellValue("orders", "product_name",
                "id", chosenId);
        int sellPrice = ((int) DataBase.getCellValue("orders", "price",
                "id", chosenId));

        // проверяем, достаточно ли места в ячейке пункта продаж
        if (StorageCell.getCellFreeSpace(salePointCellId) < quantity) {
            System.out.println("Недостаточно места в ячейке пункта продаж для возврата товара");
            return;
        }

        // получаем дополнительные данные о товаре
        int buyPrice = (int) DataBase.getCellValueByTwoConditions("products", "buy_price",
                "name", productName, "storage_cell_id", salePointCellId);
        int manufactureId = (int) DataBase.getCellValueByTwoConditions("products", "manufacture_id",
                "name", productName, "storage_cell_id", salePointCellId);

        // добавляем товар обратно в ячейку пункта продаж
        Product.addIntoTable(productName, sellPrice, buyPrice, salePointCellId, quantity, manufactureId);

        // обновляем занятость ячейки
        StorageCell.changeOccupancy(salePointCellId, quantity);

        // удаляем заказ из таблицы orders
        DataBase.removeRaw("orders", chosenId);

        // регистрируем возврат в учете продаж (отрицательная прибыль)
        SalesAccounting.registerSale("sale_point", -(sellPrice - buyPrice) * quantity,
                salePointId, productName);

        System.out.println("Товар " + productName + " (" + quantity + " шт.) успешно возвращен на пункт продаж");
    }

    // метод для получения количества товаров в ячейке
    private static int countProducts(String name, int storageCellId) {
        int sum = 0;

        // запрос в бд для подсчета суммы значений ячеек
        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            String sqlQuery = "SELECT SUM(quantity) FROM products WHERE name = ? AND storage_cell_id = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                preparedStatement.setString(1, name);
                preparedStatement.setInt(2, storageCellId);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        sum = resultSet.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при подсчете товара: " + e.getMessage());
        }

        return sum;
    }

    // метод для добавления заказа в бд
    private static void addOrderIntoTable(String productName, int quantity, int price,
                                         int shoppingCardId, int salePointId, int saleId) {

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            String sqlQuery = "INSERT INTO orders (product_name, quantity, price, shopping_card_id, " +
                    "sale_point_id, sale_id, status) VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                preparedStatement.setString(1, productName);
                preparedStatement.setInt(2, quantity);
                preparedStatement.setInt(3, price);
                preparedStatement.setInt(4, shoppingCardId);
                preparedStatement.setInt(5, salePointId);
                preparedStatement.setInt(6, saleId);
                preparedStatement.setString(7, "получен");

                preparedStatement.executeUpdate();

            }
        } catch (SQLException e) {
                System.out.println("Ошибка при добавлении заказа: " + e.getMessage());
        }

    }
}