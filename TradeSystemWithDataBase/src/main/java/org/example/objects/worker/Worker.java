package org.example.objects.worker;

import org.example.appсontrol.database.DataBase;
import org.example.appсontrol.services.Check;
import org.example.appсontrol.services.SafeInput;

import java.sql.*;
import java.util.List;

public class Worker {

    // метод для найма нового сотрудника
    public static void hireWorker() {
        // запрос имени сотрудника
        String name = SafeInput.stringInput("Введите имя сотрудника: ");

        try {
            // проверка, что имя не пустое
            Check.stringNotEmpty(name);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
            return;
        }

        // запрос фамилии сотрудника
        String surname = SafeInput.stringInput("Введите фамилию сотрудника: ");

        try {
            // проверка, что фамилия не пустая
            Check.stringNotEmpty(surname);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
            return;
        }

        // запрос номера телефона
        String number = SafeInput.stringInput("Введите номер телефона сотрудника (должен начинаться " +
                "с \"+7\", 12 символов): ");

        try {
            // проверка корректности номера телефона
            Check.phoneNumberIsCorrect(number);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
            return;
        }

        // выбор места работы
        int choice = SafeInput.safeIntInput("Куда нанимается сотрудник? (1 - склад, 2 - пункт " +
                "выдачи заказов)\nВаш выбор: ");

        String status = null;
        switch (choice) {
            case 1:
                status = "работает на складе";

                System.out.println("Выберите номер склада из списка ниже: ");

                // получение списка складов
                List<String> warehouses = DataBase.columnToList("warehouses", "name");
                String[] warehousesArray = warehouses.toArray(new String[0]);

                // вывод списка складов
                for (int i = 0; i < warehousesArray.length; i++) {
                    System.out.println(i + 1 + ". " + warehousesArray[i]);
                }

                // выбор склада
                int warehouseNumber = SafeInput.safeIntInput("Ваш выбор: ");

                String warehouseName = warehousesArray[warehouseNumber - 1];
                int warehouseId = DataBase.getId("warehouses", "name", warehouseName);

                // добавление работника склада
                addWorkerIntoTable(name, surname, number, warehouseId, status);

                break;
            case 2:
                status = "работает на пункте продаж";

                // получение списка пунктов продаж
                List<String> salePoints = DataBase.columnToList("sale_points", "address");

                // вывод списка пунктов продаж
                for (int i = 0; i < salePoints.size(); i++) {
                    System.out.println(i + 1 + ". " + salePoints.get(i));
                }

                // выбор пункта продаж
                int salePointNumber = SafeInput.safeIntInput("Ваш выбор: ");

                String salePointAddress = salePoints.get(salePointNumber - 1);
                int salePointId = DataBase.getId("sale_points", "address", salePointAddress);

                // добавление работника пункта продаж
                addWorkerIntoTable(name, surname, number, salePointId, status);

                break;
            default:
                System.out.println("Некорректный ввод! Повторите попытку");
                break;
        }
    }

    // метод для увольнения сотрудника
    public static void dismissWorker() {
        // запрос имени сотрудника
        String name = SafeInput.stringInput("Введите имя сотрудника: ");

        try {
            // проверка, что имя не пустое
            Check.stringNotEmpty(name);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
            return;
        }

        // запрос фамилии сотрудника
        String surname = SafeInput.stringInput("Введите фамилию сотрудника: ");

        try {
            // проверка, что фамилия не пустая
            Check.stringNotEmpty(surname);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
            return;
        }

        // получение id сотрудника
        int id = (int) DataBase.getCellValueByTwoConditions("workers", "id",
                "name", name, "surname", surname);

        // запрос на обновление статуса
        String sqlQuery = "UPDATE workers SET status = 'уволен' WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(DataBase.getDatabaseUrl())) {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);

            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();

            preparedStatement.close();

            System.out.println("Работник " + name + " " + surname + " уволен");
        } catch (SQLException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    // метод для вывода всех сотрудников
    public static void printAllWorkers() {
        System.out.println("id\tname\tsurname\tphone_number\twork_place_id\tstatus");
        // вывод всех работников
        DataBase.printAll("workers", 6);
    }

    // метод для добавления работника в таблицу
    private static void addWorkerIntoTable(String name, String surname, String phoneNumber,
                                           int workPlaceId, String status) {
        String sqlQuery = "INSERT INTO workers (name, surname, phone_number, work_place_id, " +
                "status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DataBase.getDatabaseUrl())){

            PreparedStatement pstm = conn.prepareStatement(sqlQuery);

            // установка параметров запроса
            pstm.setString(1, name);
            pstm.setString(2, surname);
            pstm.setString(3, phoneNumber);
            pstm.setInt(4, workPlaceId);
            pstm.setString(5, status);

            pstm.executeUpdate();

            // получение id добавленного работника
            int id = 0;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    id = rs.getInt(1);
                }
            }

            pstm.close();

            System.out.println("Работник " + name + " " + surname + " успешно добавлен! ID: " + id);

        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении работника: " + e.getMessage());
        }
    }
}