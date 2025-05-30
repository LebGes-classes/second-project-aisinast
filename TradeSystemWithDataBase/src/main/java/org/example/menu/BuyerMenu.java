package org.example.menu;

import org.example.Buyer;
import org.example.Order;
import org.example.services.SafeInput;
import org.example.terminal.OutputController;
import org.example.terminal.Printer;

import java.io.File;
import java.io.FileNotFoundException;

public class BuyerMenu {

    static Printer printer = new Printer();

    public static void showBuyerMenu() {
        try {
            printer.printTextFile(new File("src/main/java/org/example/menu/texts/buyer_menu.txt"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        int choice = SafeInput.safeIntInput("\nВаш выбор: ");

        switch (choice) {
            case 1:
                OutputController.clearConsole();

                Buyer.addNewBuyer();

                OutputController.waitForEnter();
                showBuyerMenu();
                break;
            case 2:
                OutputController.clearConsole();

                Buyer.removeBuyer();

                OutputController.waitForEnter();
                showBuyerMenu();
                break;
            case 3:
                OutputController.clearConsole();

                Order.createOrder();

                OutputController.waitForEnter();
                showBuyerMenu();
                break;
            case 4:
                OutputController.clearConsole();

                Order.returnOrder();

                OutputController.waitForEnter();
                showBuyerMenu();
                break;
            case 5:
                OutputController.clearConsole();

                Buyer.getBuyerInfo();

                OutputController.waitForEnter();
                showBuyerMenu();
                break;
            case 0:
                OutputController.clearConsole();
                AppMenu.showAppMenu();
                break;
            default:
                OutputController.clearConsole();
                System.out.println("Некорректный ввод! Повторите попытку");
                showBuyerMenu();
                OutputController.waitForEnter();
                break;
        }
    }
}
