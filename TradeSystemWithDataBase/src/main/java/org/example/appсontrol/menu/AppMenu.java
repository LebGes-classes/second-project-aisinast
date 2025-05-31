package org.example.appсontrol.menu;

import org.example.appсontrol.services.SafeInput;
import org.example.appсontrol.terminal.OutputController;
import org.example.appсontrol.terminal.Printer;

import java.io.File;
import java.io.FileNotFoundException;


public class AppMenu {

    static Printer printer = new Printer();

    // метод для вывода главного меню приложения
    public static void showAppMenu() {

        // выводим меню
        try {
            printer.printTextFile(new File("src/main/java/org/example/menu/texts/app_menu.txt"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        int choice = SafeInput.safeIntInput("\nВаш выбор: ");

        // в зависимости от выбора переходим в соответствующее меню
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
