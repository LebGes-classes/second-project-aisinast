package org.example.objects.manufacture;

import org.example.appсontrol.accounting.SalesAccounting;
import org.example.appсontrol.database.DataBase;
import org.example.appсontrol.services.Check;
import org.example.appсontrol.services.SafeInput;

import java.sql.*;

public class Manufacture {
    // название таблицы производителей в базе данных
    private static final String tableName = "manufactures";

    // метод для получения названия таблицы
    public static String getTableName() {
        return tableName;
    }

    // метод для добавления нового производителя
    public static void addManufacture() {
        // запрос названия производителя
        String name = SafeInput.stringInput("Введите название производителя: ");

        try {
            // проверка, что название не пустое
            Check.stringNotEmpty(name);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
            return;
        }

        // проверка на существование производителя с таким названием
        if (DataBase.countRowsWithCondition(getTableName(), "name", name) != 0) {
            System.out.println("Производитель с таким названием уже существует!");
            return;
        }

        // запрос страны производителя
        String country = SafeInput.stringInput("Введите страну производителя: ");

        try {
            // проверка что страна не пустая
            Check.stringNotEmpty(country);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
            return;
        }

        // добавление производителя в таблицу
        addManufactureIntoTable(name, country);
    }

    // метод для вывода прибыли производителя
    public static void printManufactureProfit() {
        System.out.println("Выберите производство из списка ниже: ");
        // вывод списка всех производителей
        DataBase.printAll("manufactures", 3);

        // запрос id производителя для просмотра прибыли
        int manufactureId = SafeInput.safeIntInput("Ваш выбор (введите id) : ");

        // получение информации о прибыли
        int profit = SalesAccounting.getProfitInfo("manufacture", manufactureId);

        System.out.println("Прибыль за все время: " + profit);
    }

    // метод для удаления производителя
    public static void removeManufacture() {
        // запрос названия производителя для удаления
        String name = SafeInput.stringInput("Введите название производителя: ");

        // получение id производителя
        int id = DataBase.getId(tableName, "name", name);

        if (id == 0) {
            System.out.println("Производства с таким названием не существует!");
            return;
        }

        // удаление производителя из таблицы
        DataBase.removeRaw(tableName, id);
    }

    // метод для вывода всех производителей
    public static void printAllManufactures() {
        DataBase.printAll(getTableName(), 3);
    }

    // приватный метод для добавления производителя в таблицу
    private static void addManufactureIntoTable(String name, String country) {
        // sql запрос для вставки данных
        String sqlQuery = "INSERT INTO manufactures (name, country) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            // подготовка запроса
            PreparedStatement pstmt = conn.prepareStatement(sqlQuery);

            // установка параметров запроса
            pstmt.setString(1, name);
            pstmt.setString(2, country);

            // выполнение запроса
            pstmt.executeUpdate();

            int id = 0;
            // получение id последней добавленной записи
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    id = rs.getInt(1);
                }
            }

            pstmt.close();

            // вывод сообщения об успешном добавлении
            System.out.println("Производитель " + name + " успешно добавлен! ID: " + id);

        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении производителя: " + e.getMessage());
        }
    }
}