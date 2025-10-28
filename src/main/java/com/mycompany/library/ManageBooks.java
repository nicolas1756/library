package com.mycompany.library;

import java.util.ArrayList;

public class ManageBooks {

    public static void printTable(ArrayList<Book> books) {
        /*if (books == null || books.isEmpty()) {
            System.out.println("No books available.");
            return;
        }

        System.out.println("╔════════════════════════╦══════════════════╦═══════╗");
        System.out.printf ("║ %-22s ║ %-16s ║ %-5s ║%n", "Title", "Author", "Year");
        System.out.println("╠════════════════════════╬══════════════════╬═══════╣");

        for (Book book : books) {
            System.out.printf("║ %-22s ║ %-16s ║ %-5d ║%n",
                    book.getTitle(),
                    book.getAuthor(),
                    book.getYear());
        }

        */System.out.println("╚════════════════════════╩══════════════════╩═══════╝");
    }
}
