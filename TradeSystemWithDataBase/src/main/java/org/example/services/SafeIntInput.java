package org.example.services;

import java.util.Scanner;

public class SafeIntInput {
    private static Scanner scanner = new Scanner(System.in);

    public static int safeInput(String prompt) {
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
}
