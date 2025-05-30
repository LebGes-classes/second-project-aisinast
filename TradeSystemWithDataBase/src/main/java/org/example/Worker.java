package org.example;

import org.example.database.DataBase;
import org.example.services.Check;
import org.example.services.SafeInput;

import java.sql.*;
import java.util.List;
import java.util.Scanner;

public class Worker {
    int id;
    String name;
    String surname;
    String phoneNumber;
    int workPlaceId;
    String status;

    private static String tableName = "workers";

    static Scanner scanner = new Scanner(System.in);

    public static void hireWorker() {
]        String name = SafeInput.stringInput("Введите имя сотрудника: ");

        try {
            Check.stringNotEmpty(name);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
            return;
        }

        String surname = SafeInput.stringInput("Введите фамилию сотрудника: ");

        try {
            Check.stringNotEmpty(surname);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
            return;
        }

        String number = SafeInput.stringInput("Введите номер телефона сотрудника (должен начинаться " +
                "с \"+7\", 12 символов): ");

        try {
            Check.phoneNumberIsCorrect(number);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
            return;
        }

        int choice = SafeInput.safeIntInput("Куда нанимается сотрудник? (1 - склад, 2 - пункт " +
                "выдачи заказов)\nВаш выбор: ");

        String status = null;
        switch (choice) {
            case 1:
                status = "работает на складе";

                System.out.println("Выберите номер склада из списка ниже: ");

                List<String> warehouses = DataBase.columnToList("warehouses", "name");
                String[] warehousesArray = warehouses.toArray(new String[0]);

                for (int i = 0; i < warehousesArray.length; i++) {
                    System.out.println(i + 1 + ". " + warehousesArray[i]);
                }

                int warehouseNumber = SafeInput.safeIntInput("Ваш выбор: ");

                String warehouseName = warehousesArray[warehouseNumber - 1];
                int warehouseId = DataBase.getId("warehouses", "name", warehouseName);

                addWorkerIntoTable(name, surname, number, warehouseId, status);

                break;
            case 2:
                status = "работает на пункте продаж";

                List<String> salePoints = DataBase.columnToList("sale_points", "address");

                for (int i = 0; i < salePoints.size(); i++) {
                    System.out.println(i + 1 + ". " + salePoints.get(i));
                }

                int salePointNumber = SafeInput.safeIntInput("Ваш выбор: ");

                String salePointAddress = salePoints.get(salePointNumber - 1);
                int salePointId = DataBase.getId("sale_points", "address", salePointAddress);

                addWorkerIntoTable(name, surname, number, salePointId, status);

                break;
            default:
                System.out.println("Некорректный ввод! Повторите попытку");
                break;
        }
    }

    public static void dismissWorker() {

        String name = SafeInput.stringInput("Введите имя сотрудника: ");

        try {
            Check.stringNotEmpty(name);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
            return;
        }

        String surname = SafeInput.stringInput("Введите фамилию сотрудника: ");

        try {
            Check.stringNotEmpty(surname);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
            return;
        }

        int id = (int) DataBase.getCellValueByTwoConditions("workers", "id",
                "name", name, "surname", surname);

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

    public static void printAllWorkers() {
        System.out.println("id\tname\tsurname\tphone_number\twork_place_id\tstatus");
        DataBase.printAll("workers", 6);
    }

    private static void addWorkerIntoTable(String name, String surname, String phoneNumber,
                                           int workPlaceId, String status) {
        String sqlQuery = "INSERT INTO workers (name, surname, phone_number, work_place_id, " +
                "status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DataBase.getDatabaseUrl())){

            PreparedStatement pstm = conn.prepareStatement(sqlQuery);

            pstm.setString(1, name);
            pstm.setString(2, surname);
            pstm.setString(3, phoneNumber);
            pstm.setInt(4, workPlaceId);
            pstm.setString(5, status);

            pstm.executeUpdate();

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
