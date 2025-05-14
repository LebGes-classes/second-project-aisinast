package org.example.menu;

import org.example.Manufacture;
import org.example.terminal.OutputController;
import org.example.terminal.Printer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ManufactureMenu {

    static Printer printer = new Printer();
    static Scanner scanner = new Scanner(System.in);

    public static void showManufactureMenu(){
        try {
            printer.printTextFile(new File("/Users/mac/code/Java/ИиП/second-project-aisinast/" +
                                "TradeSystemWithDataBase/src/main/java/org/example/menu/texts/manufacture_menu.txt"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        System.out.print("\nВаш выбор: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                OutputController.clearConsole();

                Manufacture.addManufacture();

                OutputController.waitForEnter();
                showManufactureMenu();
                break;
            case 2:
                OutputController.clearConsole();

                Manufacture.removeManufacture();

                OutputController.waitForEnter();
                showManufactureMenu();
                break;
            case 3:
                OutputController.clearConsole();

                Manufacture.printAllManufactures();

                OutputController.waitForEnter();
                showManufactureMenu();
                break;
            case 0:
                OutputController.clearConsole();
                AppMenu.showAppMenu();
                break;
            default:
                OutputController.clearConsole();
                System.out.println("Некорректный ввод! Повторите попытку");
                showManufactureMenu();
                OutputController.waitForEnter();
                break;

        }
    }
}
