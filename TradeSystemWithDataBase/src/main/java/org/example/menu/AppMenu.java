package org.example.menu;

import org.example.Buyer;
import org.example.terminal.OutputController;
import org.example.terminal.Printer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class AppMenu {

    static Scanner scanner = new Scanner(System.in);
    static Printer printer = new Printer();

    public static void showAppMenu() {
        try {
            printer.printTextFile(new File("src/main/java/org/example/menu/texts/app_menu.txt"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        System.out.print("\nВаш выбор: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                OutputController.clearConsole();

                ManufactureMenu.showManufactureMenu();
                break;
            case 2:
                OutputController.clearConsole();

                WarehouseMenu.showWarehouseMenu();
                break;
            case 3:
                OutputController.clearConsole();

                WorkerMenu.showWorkerMenu();
                break;
            case 4:
                OutputController.clearConsole();

                SalePointMenu.showSalePointMenu();
                break;
            case 5:
                OutputController.clearConsole();

                BuyerMenu.showBuyerMenu();
            case 0:
                System.exit(-1);
            default:
                OutputController.clearConsole();
                System.out.println("Некорректный ввод! Повторите попытку");
                showAppMenu();
                OutputController.waitForEnter();
                break;
        }
    }
}
