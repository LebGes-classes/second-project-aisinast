package org.example.appсontrol.menu;

import org.example.objects.worker.Worker;
import org.example.appсontrol.services.SafeInput;
import org.example.appсontrol.terminal.OutputController;
import org.example.appсontrol.terminal.Printer;

import java.io.File;
import java.io.FileNotFoundException;

public class WorkerMenu {

    static Printer printer = new Printer();

    public static void showWorkerMenu()  {

        try {
            printer.printTextFile(new File("src/main/java/org/example/menu/texts/worker_menu.txt"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        int choice = SafeInput.safeIntInput("\nВаш выбор: ");

        // в зависимости от выбора, переходим в соответствующее меню
        switch (choice) {
            case 1:
                OutputController.clearConsole();

                Worker.hireWorker();

                OutputController.waitForEnter();
                showWorkerMenu();
                break;
            case 2:
                OutputController.clearConsole();

                Worker.dismissWorker();

                OutputController.waitForEnter();
                showWorkerMenu();
                break;
            case 3:
                OutputController.clearConsole();

                Worker.printAllWorkers();

                OutputController.waitForEnter();
                showWorkerMenu();
            case 0:
                OutputController.clearConsole();
                AppMenu.showAppMenu();
                break;
            default:
                OutputController.clearConsole();
                System.out.println("Некорректный ввод! Повторите попытку");
                showWorkerMenu();
                OutputController.waitForEnter();
                break;
        }
    }
}
