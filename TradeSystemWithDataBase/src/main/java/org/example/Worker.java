package org.example;

import org.example.database.DataBase;
import org.example.services.Check;

import javax.xml.crypto.Data;
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
        System.out.print("Введите имя сотрудника: ");
        String name = scanner.nextLine();

        try {
            Check.stringNotEmpty(name);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
            return;
        }

        System.out.print("Введите фамилию сотрудника: ");
        String surname = scanner.nextLine();

        try {
            Check.stringNotEmpty(surname);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
            return;
        }

        System.out.print("Введите номер телефона сотрудника (должен начинаться с \"+7\", 12 символов): ");
        String number = scanner.nextLine();

        try {
            Check.phoneNumberIsCorrect(number);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
            return;
        }

        System.out.print("Куда нанимается сотрудник? (1 - склад, 2 - пункт выдачи заказов)\nВаш выбор: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

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

                System.out.print("Ваш выбор: ");
                int warehouseNumber = scanner.nextInt();
                scanner.nextLine();

                String warehouseName = warehousesArray[warehouseNumber - 1];
                int warehouseId = DataBase.getId("warehouses", "name", warehouseName);

                addWorkerIntoTable(name, surname, number, warehouseId, status);

                break;
            case 2: status = "работает на пункте выдачи";
            default:
                System.out.println("Некорректный ввод! Повторите попытку");
                break;
        }
    }

    public static void dismissWorker() {
        System.out.print("Введите имя сотрудника: ");
        String name = scanner.nextLine();

        try {
            Check.stringNotEmpty(name);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
            return;
        }

        System.out.print("Введите фамилию сотрудника: ");
        String surname = scanner.nextLine();

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

            System.out.println("Работник " + name + " " + surname + " успешно добавлен! ID: " + id);

        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении работника: " + e.getMessage());
        }
    }
}
