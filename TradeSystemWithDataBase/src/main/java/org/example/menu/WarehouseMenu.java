package org.example.menu;

import org.example.Product;
import org.example.StorageCell;
import org.example.Warehouse;
import org.example.services.SafeInput;
import org.example.terminal.OutputController;
import org.example.terminal.Printer;

import java.io.File;
import java.io.FileNotFoundException;


public class WarehouseMenu {

    static Printer printer = new Printer();

    public static void showWarehouseMenu() {
        OutputController.clearConsole();

        try {
            printer.printTextFile(new File("src/main/java/org/example/menu/texts/warehouse_menu.txt"));
        } catch (FileNotFoundException e) {
             throw new RuntimeException(e);
        }

        int choice = SafeInput.safeIntInput("\nВаш выбор: ");

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

                StorageCell.addNewCell();

                OutputController.waitForEnter();
                showWarehouseMenu();
                break;
            case 7:
                OutputController.clearConsole();

                Warehouse.changeManager();
                OutputController.waitForEnter();
                showWarehouseMenu();
                break;
            case 8:
                OutputController.clearConsole();

                Warehouse.moveToSellPoint();

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
