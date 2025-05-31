package org.example.appсontrol.menu;

import org.example.objects.manufacture.Manufacture;
import org.example.appсontrol.services.SafeInput;
import org.example.appсontrol.terminal.OutputController;
import org.example.appсontrol.terminal.Printer;

import java.io.File;
import java.io.FileNotFoundException;

public class ManufactureMenu {

    static Printer printer = new Printer();

    public static void showManufactureMenu(){

        // выводим меню
        try {
            printer.printTextFile(new File("src/main/java/org/example/menu/texts/manufacture_menu.txt"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        int choice = SafeInput.safeIntInput("\nВаш выбор: ");

        // в зависимости от выбора, переходим в соответствующее меню
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
            case 4:
                OutputController.clearConsole();

                Manufacture.printManufactureProfit();

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
