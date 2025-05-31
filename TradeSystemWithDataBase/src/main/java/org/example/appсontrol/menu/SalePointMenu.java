package org.example.appсontrol.menu;

import org.example.objects.storage.SalePoint;
import org.example.appсontrol.services.SafeInput;
import org.example.appсontrol.terminal.OutputController;
import org.example.appсontrol.terminal.Printer;

import java.io.File;
import java.io.FileNotFoundException;

public class SalePointMenu {
    static Printer printer = new Printer();

    public static void showSalePointMenu() {

        // выводим меню
        try {
            printer.printTextFile(new File("src/main/java/org/example/menu/texts/sale_point_menu.txt"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        int choice = SafeInput.safeIntInput("\nВаш выбор: ");

        // в зависимости от выбора, переходим в соответствующее меню
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
