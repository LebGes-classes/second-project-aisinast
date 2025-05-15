package org.example.menu;

import org.example.Warehouse;
import org.example.terminal.OutputController;
import org.example.terminal.Printer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class WarehouseMenu {
    static Scanner scanner = new Scanner(System.in);
    static Printer printer = new Printer();

    public static void showWarehouseMenu() {
        try {
            printer.printTextFile(new File("/Users/mac/code/Java/ИиП/second-project-aisinast/" +
                    "TradeSystemWithDataBase/src/main/java/org/example/menu/texts/warehouse_menu.txt"));
        } catch (FileNotFoundException e) {
             throw new RuntimeException(e);
        }

        System.out.print("\nВаш выбор: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                OutputController.clearConsole();

                Warehouse.printWarehouseInfo();
                OutputController.waitForEnter();
                break;
            case 2:
                OutputController.clearConsole();

                OutputController.waitForEnter();
                break;
            case 3:
                OutputController.clearConsole();

                Warehouse.addNewWarehouse();

                showWarehouseMenu();
                OutputController.waitForEnter();
                break;
            case 4:
                OutputController.clearConsole();

                OutputController.waitForEnter();
                break;
            case 5:
                OutputController.clearConsole();

                OutputController.waitForEnter();
                break;
            case 6:
                OutputController.clearConsole();

                OutputController.waitForEnter();
                break;
            case 7:
                OutputController.clearConsole();

                OutputController.waitForEnter();
                break;
            case 0:
                OutputController.clearConsole();
                AppMenu.showAppMenu();
                break;
            default:
        }
    }
}
