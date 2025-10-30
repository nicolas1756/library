package com.mycompany.library;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.Date;

public class ManageBooks {

    Scanner scanner = new Scanner(System.in);
    FileHandling fileHandling = new FileHandling();

    public static void printTable(ArrayList<Book> books) {
        if (books == null || books.isEmpty()) {
            System.out.println(Ansi.RED + "No books available." + Ansi.RESET);
            return;
        }

        // Calculate maximum widths based on content
        int idWidth = Math.max("Book ID".length(), books.stream()
                .mapToInt(b -> b.getBookId().length())
                .max().orElse(0)) + 2;

        int titleWidth = Math.max("Title".length(), books.stream()
                .mapToInt(b -> b.getTitle().length())
                .max().orElse(0)) + 2;

        int authorWidth = Math.max("Author".length(), books.stream()
                .mapToInt(b -> b.getAuthor().length())
                .max().orElse(0)) + 2;

        int yearWidth = Math.max("Year".length(), books.stream()
                .mapToInt(b -> b.getYearPublished().length())
                .max().orElse(0)) + 2;

        // Date format is fixed length: "yyyy-MM-dd HH:mm:ss"
        int dateWidth = Math.max("Last Edited".length(), 19) + 2;

        // Top border
        System.out.println("╔" + "═".repeat(idWidth) + "╦" + "═".repeat(titleWidth) + "╦" + 
                          "═".repeat(authorWidth) + "╦" + "═".repeat(yearWidth) + "╦" + 
                          "═".repeat(dateWidth) + "╗");
        
        // Headers
        System.out.printf("║ %-" + (idWidth-1) + "s║ %-" + (titleWidth-1) + "s║ %-" + 
                         (authorWidth-1) + "s║ %-" + (yearWidth-1) + "s║ %-" + (dateWidth-1) + "s║%n",
                         "Book ID", "Title", "Author", "Year", "Last Edited");

        // Header-content separator
        System.out.println("╠" + "═".repeat(idWidth) + "╬" + "═".repeat(titleWidth) + "╬" + 
                          "═".repeat(authorWidth) + "╬" + "═".repeat(yearWidth) + "╬" + 
                          "═".repeat(dateWidth) + "╣");

        // Content rows
        for (Book book : books) {
            System.out.printf("║ %-" + (idWidth-1) + "s║ %-" + (titleWidth-1) + "s║ %-" + 
                            (authorWidth-1) + "s║ %-" + (yearWidth-1) + "s║ %-" + (dateWidth-1) + "s║%n",
                            book.getBookId(),
                            truncateString(book.getTitle(), titleWidth-1),
                            truncateString(book.getAuthor(), authorWidth-1),
                            book.getYearPublished(),
                            formatDate(book.getLastEdited()));
        }

        // Bottom border
        System.out.println("╚" + "═".repeat(idWidth) + "╩" + "═".repeat(titleWidth) + "╩" + 
                          "═".repeat(authorWidth) + "╩" + "═".repeat(yearWidth) + "╩" + 
                          "═".repeat(dateWidth) + "╝");
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
        printTable(getAllBooks());
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
