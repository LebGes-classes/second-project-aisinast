package org.example.objects.buyer;

import org.example.appсontrol.database.DataBase;
import org.example.appсontrol.services.Check;
import org.example.appсontrol.services.SafeInput;

import java.sql.*;

public class Buyer {

    // метод для добавления нового покупателя
    public static void addNewBuyer() {

        // запрос имени пользователя
        String name = SafeInput.stringInput("Введите имя пользователя: ");

        try {
            // проверка, что имя не пустое
            Check.stringNotEmpty(name);
        } catch (IllegalArgumentException e) {
            System.err.println("Имя пользователя введено некорректно: " + e.getMessage());
            return;
        }

        // запрос фамилии пользователя
        String surname = SafeInput.stringInput("Введите фамилию пользователя: ");

        try {
            // проверка, что фамилия не пустая
            Check.stringNotEmpty(surname);
        } catch (IllegalArgumentException e) {
            System.err.println("Фамилия пользователя введена некорректно: " + e.getMessage());
            return;
        }

        // запрос номера телефона
        String phoneNumber = SafeInput.stringInput("Введите номер телефона (начиная с \"+7\", 12 символов) : ");

        try {
            // проверка корректности номера телефона
            Check.phoneNumberIsCorrect(phoneNumber);
        } catch (IllegalArgumentException e) {
            System.out.println("Номер телефона введен некорректно: " + e.getMessage());
        }

        // запрос города пользователя
        String city = SafeInput.stringInput("Введите город пользователя: ");

        try {
            // проверка, что город не пустой
            Check.stringNotEmpty(city);
        } catch (IllegalArgumentException e) {
            System.err.println("Город пользователя введен некорректно: " + e.getMessage());
            return;
        }

        // создание новой корзины для покупателя
        int shoppingCardId = ShoppingCard.createNewShoppingCard();

        // добавление покупателя в таблицу
        addBuyerIntoTable(name, surname, phoneNumber, city, shoppingCardId);
    }

    // метод для удаления покупателя
    public static void removeBuyer() {

        // запрос имени пользователя для удаления
        String name = SafeInput.stringInput("Введите имя пользователя: ");

        // запрос фамилии пользователя для удаления
        String surname = SafeInput.stringInput("Введите фамилию пользователя: ");

        int id = 0;
        try {
            // получение id покупателя по имени и фамилии
            id = (int) DataBase.getCellValueByTwoConditions("buyers", "id", "name",
                    name, "surname", surname);
        } catch (NullPointerException e) {
            System.err.println("Ошибка при получении id пользователя: такого пользователя не существует" +
                    " или данные введены некорректно");
            return;
        }

        // удаление покупателя и его корзины
        DataBase.removeRaw("buyers", id);
        DataBase.removeRaw("shopping_cards", id);
        System.out.println("Пользователь " + name + " " + surname + " удален");
    }

    // метод для получения информации о покупателе
    public static void getBuyerInfo() {

        // запрос имени покупателя
        String name = SafeInput.stringInput("Введите имя покупателя: ");

        try {
            // проверка, что имя не пустое
            Check.stringNotEmpty(name);
        } catch (IllegalArgumentException e) {
            System.err.println("Имя покупателя введено некорректно: " + e.getMessage());
            return;
        }

        // запрос фамилии покупателя
        String surname = SafeInput.stringInput("Введите фамилию покупателя: ");

        // подключение к базе данных
        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            // sql запрос для получения информации о покупателе
            String sqlQuery = "SELECT phone_number, city, shopping_card_id FROM buyers WHERE name = ? AND surname = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                // установка параметров в запрос
                preparedStatement.setString(1, name);
                preparedStatement.setString(2, surname);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        // получение данных из результата запроса
                        String phoneNumber = resultSet.getString("phone_number");
                        String city = resultSet.getString("city");
                        int shoppingCardId = resultSet.getInt("shopping_card_id");

                        // вывод информации о покупателе
                        System.out.println("Информация о покупателе:");
                        System.out.println("Имя: " + name);
                        System.out.println("Фамилия: " + surname);
                        System.out.println("Номер телефона: " + phoneNumber);
                        System.out.println("Город: " + city);
                        System.out.println("ID корзины: " + shoppingCardId);
                    } else {
                        System.out.println("Покупатель с именем " + name + " и фамилией " + surname + " не найден");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении информации о покупателе: " + e.getMessage());
        }
    }

    // приватный метод для добавления покупателя в таблицу
    private static void addBuyerIntoTable(String name, String surname, String phoneNumber,
                                          String city, int shoppingCardId) {
        // подключение к базе данных
        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            // sql запрос для вставки данных
            String sqlQuery = "INSERT INTO buyers (name, surname, phone_number, city, shopping_card_id)" +
                    " VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                // установка параметров в запрос
                preparedStatement.setString(1, name);
                preparedStatement.setString(2, surname);
                preparedStatement.setString(3, phoneNumber);
                preparedStatement.setString(4, city);
                preparedStatement.setInt(5, shoppingCardId);

                // выполнение запроса
                preparedStatement.executeUpdate();

                System.out.println("Пользователь " + surname + " " + name + " успешно добавлен!");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении пользователя: " + e.getMessage());
        }
    }
}