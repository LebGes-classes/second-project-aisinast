package org.example.appсontrol.terminal;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Printer {
    public void printTextFile(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            System.out.println(line);
        }
    } }
