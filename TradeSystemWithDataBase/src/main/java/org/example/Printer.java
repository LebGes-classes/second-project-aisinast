package org.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

// printTextFile(new File("TradeSystemWithDataBase/src/main/java/org/example/menu/menu.txt"))
//                                        path from repository root

public class Printer {
    public static void printTextFile(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            System.out.println(line);
        }
    } }
