package org.example.menu;

import org.example.Buyer;
import org.example.terminal.OutputController;
import org.example.terminal.Printer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class BuyerMenu {

    static Scanner scanner = new Scanner(System.in);
    static Printer printer = new Printer();

    public static void showBuyerMenu() {
        try {
            printer.printTextFile(new File("src/main/java/org/example/menu/texts/buyer.txt"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        System.out.print("\nВаш выбор: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

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
            case 4:
            case 5:
            case 0:
            default:
        }
    }
}
