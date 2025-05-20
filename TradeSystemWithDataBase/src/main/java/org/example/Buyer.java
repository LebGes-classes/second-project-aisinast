package org.example;

import org.example.database.DataBase;
import org.example.services.Check;

import javax.xml.transform.Source;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

public class Buyer {
    int id;
    String name;
    String surname;
    String city;
    int shoppingCardId;

    static Scanner scanner = new Scanner(System.in);

    public static void addNewBuyer() {
        System.out.print("Введите имя пользователя: ");
        String name = scanner.nextLine();

        try {
            Check.stringNotEmpty(name);
        } catch (IllegalArgumentException e) {
            System.err.println("Имя пользователя введено некорректно: " + e.getMessage());
            return;
        }

        System.out.print("Введите фамилию пользователя: ");
        String surname = scanner.nextLine();

        try {
            Check.stringNotEmpty(surname);
        } catch (IllegalArgumentException e) {
            System.err.println("Фамилия пользователя введена некорректно: " + e.getMessage());
            return;
        }

        System.out.print("Введите номер телефона (начиная с \"+7\", 12 символов) : ");
        String phoneNumber = scanner.nextLine();

        try {
            Check.phoneNumberIsCorrect(phoneNumber);
        } catch (IllegalArgumentException e) {
            System.out.println("Номер телефона введен некорректно: " + e.getMessage());
        }

        System.out.print("Введите город пользователя: ");
        String city = scanner.nextLine();

        try {
            Check.stringNotEmpty(city);
        } catch (IllegalArgumentException e) {
            System.err.println("Город пользователя введен некорректно: " + e.getMessage());
            return;
        }

        int shoppingCardId = ShoppingCard.createNewShoppingCard();

        addBuyerIntoTable(name, surname, phoneNumber, city, shoppingCardId);
    }

    public static void removeBuyer() {
        System.out.print("Введите имя пользователя: ");
        String name = scanner.nextLine();

        System.out.print("Введите фамилию пользователя: ");
        String surname = scanner.nextLine();

        int id = 0;
        try {
            id = (int) DataBase.getCellValueByTwoConditions("buyers", "id", "name",
                    name, "surname", surname);
        } catch (NullPointerException e) {
            System.err.println("Ошибка при получении id пользователя: такого пользователя не существует" +
                    " или данные введены некорректно");
            return;
        }

        DataBase.removeRaw("buyers", id);
        System.out.println("Пользователь " + name + " " + surname + " удален");
    }

    private static void addBuyerIntoTable(String name, String surname, String phoneNumber,
                                          String city, int shoppingCardId) {
        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            String sqlQuery = "INSERT INTO buyers (name, surname, phone_number, city, shopping_card_id)" +
                    " VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                preparedStatement.setString(1, name);
                preparedStatement.setString(2, surname);
                preparedStatement.setString(3, phoneNumber);
                preparedStatement.setString(4, city);
                preparedStatement.setInt(5, shoppingCardId);

                preparedStatement.executeUpdate();

                System.out.println("Пользователь " + surname + " " + name + " успешно добавлен!");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении пользователя: " + e.getMessage());
        }
    }
}
