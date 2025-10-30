package com.mycompany.library;

import java.util.Scanner;

public class consoleUtil {
    private static final Scanner scanner = new Scanner(System.in);

    public static boolean confirmAction(String message) {
        System.out.print(Ansi.ORANGE + message + " (y/n): " + Ansi.RESET);
        String input = scanner.nextLine().trim().toLowerCase();

        while (!input.equals("y") && !input.equals("n")) {
            System.out.print(Ansi.RED + "Invalid input. Please enter 'y' or 'n': " + Ansi.RESET);
            input = scanner.nextLine().trim().toLowerCase();
        }

        return input.equals("y");
    }
}
