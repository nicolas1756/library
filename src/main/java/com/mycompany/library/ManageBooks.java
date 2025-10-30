package com.mycompany.library;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Date;
import java.util.EnumSet;

public class ManageBooks {

    Scanner scanner = new Scanner(System.in);
    FileHandling fileHandling = new FileHandling();

    // Define columns as an enum
    public enum Column {
        ID, TITLE, AUTHOR, YEAR, LAST_EDITED
    }

    public static void printTable(ArrayList<Book> books, EnumSet<Column> columns) {
        if (books == null || books.isEmpty()) {
            System.out.println("\u001B[31mNo books available.\u001B[0m"); // ANSI red
            return;
        }

        // Column widths
        final int idWidth = 12;
        final int titleWidth = 25;
        final int authorWidth = 15;
        final int yearWidth = 6;
        final int dateWidth = 25;

        // Prepare dynamic parts
        StringBuilder top = new StringBuilder("╔");
        StringBuilder header = new StringBuilder("║ ");
        StringBuilder sep = new StringBuilder("╠");
        StringBuilder bottom = new StringBuilder("╚");

        // Helper lambda to add a column
        java.util.function.BiConsumer<String[], Integer> addCol = (info, width) -> {
            top.append("═".repeat(width)).append("╦");
            header.append(String.format("%-" + (width - 1) + "s║ ", info[0]));
            sep.append("═".repeat(width)).append("╬");
            bottom.append("═".repeat(width)).append("╩");
        };

        // Add selected columns
        if (columns.contains(Column.ID)) addCol.accept(new String[]{"Book ID"}, idWidth);
        if (columns.contains(Column.TITLE)) addCol.accept(new String[]{"Title"}, titleWidth);
        if (columns.contains(Column.AUTHOR)) addCol.accept(new String[]{"Author"}, authorWidth);
        if (columns.contains(Column.YEAR)) addCol.accept(new String[]{"Year"}, yearWidth);
        if (columns.contains(Column.LAST_EDITED)) addCol.accept(new String[]{"Last Edited"}, dateWidth);

        // Replace final borders properly
        top.setCharAt(top.length() - 1, '╗');
        sep.setCharAt(sep.length() - 1, '╣');
        bottom.setCharAt(bottom.length() - 1, '╝');

        // Print header section
        System.out.println(top);
        System.out.println(header);
        System.out.println(sep);

        // Print each row dynamically
        for (Book book : books) {
            StringBuilder row = new StringBuilder("║ ");
            if (columns.contains(Column.ID))
                row.append(String.format("%-" + (idWidth - 1) + "s║ ", book.getBookId()));
            if (columns.contains(Column.TITLE))
                row.append(String.format("%-" + (titleWidth - 1) + "s║ ", truncateString(book.getTitle(), titleWidth - 1)));
            if (columns.contains(Column.AUTHOR))
                row.append(String.format("%-" + (authorWidth - 1) + "s║ ", truncateString(book.getAuthor(), authorWidth - 1)));
            if (columns.contains(Column.YEAR))
                row.append(String.format("%-" + (yearWidth - 1) + "s║ ", book.getYearPublished()));
            if (columns.contains(Column.LAST_EDITED))
                row.append(String.format("%-" + (dateWidth - 1) + "s║", formatDate(book.getLastEdited())));
            System.out.println(row);
        }

        // Print bottom border
        System.out.println(bottom);
        System.out.println("\nDescription (Press D + BookID to view full description)");
        System.out.println("=========================================================");
    }

    // Get all books
    public ArrayList<Book> getAllBooks() {
        ArrayList<Book> books = fileHandling.readFromFile("books.ser", Book.class);
        return books != null ? books : new ArrayList<>();
    }

    //Librarian book management methods

    // Add a new book
    public void addBook() {
        String title = "", author = "", year = "", description = "";
        boolean inputComplete = false;
        int step = 0;

        System.out.println("=================" + Ansi.BOLD + " Add a Book " + Ansi.RESET +"=================");
        System.out.println(Ansi.RED + "Enter -1 to go back" + Ansi.RESET);
        System.out.println("==============================================");

        while (!inputComplete) {
            switch (step) {
                case 0: // Title
                    System.out.print("Enter book title: ");
                    title = scanner.nextLine().trim();
                    if (title.equals("-1")) {
                        return;
                    }
                    if (!title.isEmpty()) {
                        step++;
                    } else {
                        System.out.println(Ansi.RED + "Title cannot be empty!" + Ansi.RESET);
                    }
                    break;

                case 1: // Author
                    System.out.print("Enter author name: ");
                    author = scanner.nextLine().trim();
                    if (author.equals("-1")) {
                        step--;
                        continue;
                    }
                    if (!author.isEmpty()) {
                        step++;
                    } else {
                        System.out.println(Ansi.RED + "Author cannot be empty!" + Ansi.RESET);
                    }
                    break;

                case 2: // Year
                    System.out.print("Enter publication year: ");
                    year = scanner.nextLine().trim();
                    if (year.equals("-1")) {
                        step--;
                        continue;
                    }
                    if (!year.isEmpty()) {
                        step++;
                    } else {
                        System.out.println(Ansi.RED + "Year cannot be empty!" + Ansi.RESET);
                    }
                    break;

                case 3: // Description
                    System.out.print("Enter book description: ");
                    description = scanner.nextLine().trim();
                    if (description.equals("-1")) {
                        step--;
                        continue;
                    }
                    if (!description.isEmpty()) {
                        inputComplete = true;
                    } else {
                        System.out.println(Ansi.RED + "Description cannot be empty!" + Ansi.RESET);
                    }
                    break;
            }
        }

        Book newBook = new Book(title, author, year, description);
        fileHandling.writeToFile("books.ser", newBook, Book.class);
        System.out.println("==============================================");
        System.out.println(Ansi.PURPLE + newBook + " successfully added!" + Ansi.RESET);
        printTable(getAllBooks(), EnumSet.allOf(Column.class));
    }

    // Remove a book by title
    public void removeBook(String bookID) {
        // Method implementation here
    }

    // Edit a book's details
    public void editBook(String bookID) {
        // Method implementation here
    }



    //General book methods

    // Search for a book by title
    public Book searchBook(String title) {

        return null; 
    }

    // Filter books by author
    public ArrayList<Book> filterBooksByAuthor(String author) {
        // Method implementation here
        return null; // Placeholder return
    }

    // Filter books by year
    public ArrayList<Book> filterBooksByYear(int year) {
        // Method implementation here
        return null; // Placeholder return
    }

    // Helper method to truncate long strings
    private static String truncateString(String str, int length) {
        if (str == null) return "";
        if (str.length() <= length) return str;
        return str.substring(0, length - 3) + "...";
    }

    // Helper method to format dates
    private static String formatDate(Date date) {
        if (date == null) return "";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }
}
