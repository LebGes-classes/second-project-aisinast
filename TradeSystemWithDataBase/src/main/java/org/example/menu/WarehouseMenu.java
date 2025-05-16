package org.example.menu;

import org.example.Product;
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
        OutputController.clearConsole();

        try {
            printer.printTextFile(new File("src/main/java/org/example/menu/texts/warehouse_menu.txt"));
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
                showWarehouseMenu();
                break;
            case 2:
                OutputController.clearConsole();

                Product.printProductsInfo(Warehouse.printWarehousesAndChoose());

                OutputController.waitForEnter();
                showWarehouseMenu();
                break;
            case 3:
                OutputController.clearConsole();

                Warehouse.addNewWarehouse();

                OutputController.waitForEnter();
                showWarehouseMenu();
                break;
            case 4:
                OutputController.clearConsole();

                Warehouse.closeWarehouse();

                OutputController.waitForEnter();
                showWarehouseMenu();
                break;
            case 5:
                OutputController.clearConsole();

                Product.addNewProduct(Warehouse.printWarehousesAndChoose());

                OutputController.waitForEnter();
                showWarehouseMenu();
                break;
            case 6:
                OutputController.clearConsole();

                OutputController.waitForEnter();
                break;
            case 7:
                OutputController.clearConsole();

                Warehouse.changeManager();
                OutputController.waitForEnter();
                showWarehouseMenu();
                break;
            case 0:
                OutputController.clearConsole();
                AppMenu.showAppMenu();
                break;
            default:
                OutputController.clearConsole();
                System.out.println("Некорректный ввод! Повторите попытку");
                showWarehouseMenu();
                OutputController.waitForEnter();
                break;
        }
    }
}
