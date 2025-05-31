package org.example.appсontrol.terminal;

import java.util.Scanner;

public class OutputController {

    static Scanner scanner = new Scanner(System.in);

    public static void clearConsole() {
        try {
            new ProcessBuilder("/bin/bash", "-c", "clear").inheritIO().start().waitFor();
        } catch (Exception E) {
            System.out.println(E);
        }
    }

    public static void waitForEnter() {
        System.out.println("Нажмите \"Enter\", чтобы продолжить");
        scanner.nextLine();
        clearConsole();
    }
}
