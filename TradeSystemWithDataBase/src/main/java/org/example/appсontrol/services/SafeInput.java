package org.example.appсontrol.services;

import java.util.Scanner;

public class SafeInput {
    private static Scanner scanner = new Scanner(System.in);

    // метод для чтения данных
    // считываем, пока не получим int
    public static int safeIntInput(String prompt) {
        System.out.print(prompt);
        while (true) {
            if (scanner.hasNextInt()) {
                int value = scanner.nextInt();
                scanner.nextLine();
                return value;
            } else {
                System.out.println("Ошибка: это не целое число!");
                System.out.print("Попробуйте ещё раз: ");
                scanner.next();
            }
        }
    }

    // метод, который считывает данные, удаляет пробелы слева и справа и приводит к нижнему регистру
    public static String stringInput(String prompt) {
        System.out.print(prompt);

        String str = scanner.nextLine().trim().toLowerCase();

        return str;
    }
}
