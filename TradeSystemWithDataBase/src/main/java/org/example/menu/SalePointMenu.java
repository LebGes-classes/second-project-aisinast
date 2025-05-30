package org.example.menu;

import org.example.SalePoint;
import org.example.services.SafeIntInput;
import org.example.terminal.OutputController;
import org.example.terminal.Printer;

import java.io.File;
import java.io.FileNotFoundException;

public class SalePointMenu {
    static Printer printer = new Printer();

    public static void showSalePointMenu() {
        try {
            printer.printTextFile(new File("src/main/java/org/example/menu/texts/sale_point_menu.txt"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        int choice = SafeIntInput.safeInput("\nВаш выбор: ");

        switch (choice) {
            case 1:
                OutputController.clearConsole();

                SalePoint.openNewSellPoint();

                OutputController.waitForEnter();
                showSalePointMenu();
                break;
            case 2:
                OutputController.clearConsole();

                SalePoint.closeSalePoint();

                OutputController.waitForEnter();
                showSalePointMenu();
                break;
            case 3:
                OutputController.clearConsole();

                SalePoint.changeManager();

                OutputController.waitForEnter();
                showSalePointMenu();
                break;
            case 4:
                OutputController.clearConsole();

                SalePoint.printSalePointsInfo();

                OutputController.waitForEnter();
                showSalePointMenu();
                break;
            case 5:
            case 0:
                OutputController.clearConsole();

                AppMenu.showAppMenu();
                break;
            default:
                OutputController.clearConsole();
                System.out.println("Некорректный ввод! Повторите попытку");
                showSalePointMenu();
                OutputController.waitForEnter();
                break;
        }
    }
}
