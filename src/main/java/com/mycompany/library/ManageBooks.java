package com.mycompany.library;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Date;
import java.util.EnumSet;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Iterator;

public class ManageBooks {

    Scanner scanner = new Scanner(System.in);
    FileHandling fileHandling = new FileHandling();
    consoleUtil ConsoleUtils = new consoleUtil();

    // Define columns as an enum
    public enum Column {
        INDEX, ID, TITLE, AUTHOR, YEAR, LAST_EDITED
    }

    public static void printTable(ArrayList<Book> books, EnumSet<Column> columns) {
        if (books == null || books.isEmpty()) {
            System.out.println("\u001B[31mNo books available.\u001B[0m"); // ANSI red
            return;
        }

        boolean supportsUnicode = false;
 
        
        // Choose border characters based on support
        final String TL = supportsUnicode ? "╔" : "+";
        final String TR = supportsUnicode ? "╗" : "+";
        final String BL = supportsUnicode ? "╚" : "+";
        final String BR = supportsUnicode ? "╝" : "+";
        final String TSEP = supportsUnicode ? "╦" : "+";
        final String BSEP = supportsUnicode ? "╩" : "+";
        final String HSEP = supportsUnicode ? "╠" : "+";
        final String VSEP = supportsUnicode ? "║" : "|";
        final String CROSS = supportsUnicode ? "╬" : "+";
        final String HLINE = supportsUnicode ? "═" : "-";

        // Column widths
        final int indexWidth = 5;
        final int idWidth = 12;
        final int titleWidth = 25;
        final int authorWidth = 15;
        final int yearWidth = 6;
        final int dateWidth = 17;

        // Prepare dynamic parts
        StringBuilder top = new StringBuilder(TL);
        StringBuilder header = new StringBuilder(VSEP + " ");
        StringBuilder sep = new StringBuilder(HSEP);
        StringBuilder bottom = new StringBuilder(BL);

        // Helper lambda to add a column
        java.util.function.BiConsumer<String[], Integer> addCol = (info, width) -> {
            top.append(HLINE.repeat(width)).append(TSEP);
            header.append(String.format("%-" + (width - 1) + "s" + VSEP + " ", info[0]));
            sep.append(HLINE.repeat(width)).append(CROSS);
            bottom.append(HLINE.repeat(width)).append(BSEP);
        };

        // Add selected columns
        if (columns.contains(Column.INDEX)) addCol.accept(new String[]{"Index"}, indexWidth);
        if (columns.contains(Column.ID)) addCol.accept(new String[]{"Book ID"}, idWidth);
        if (columns.contains(Column.TITLE)) addCol.accept(new String[]{"Title"}, titleWidth);
        if (columns.contains(Column.AUTHOR)) addCol.accept(new String[]{"Author"}, authorWidth);
        if (columns.contains(Column.YEAR)) addCol.accept(new String[]{"Year"}, yearWidth);
        if (columns.contains(Column.LAST_EDITED)) addCol.accept(new String[]{"Last Edited"}, dateWidth);

        // Replace final borders properly
        top.setCharAt(top.length() - 1, TR.charAt(0));
        sep.setCharAt(sep.length() - 1, HSEP.equals("+") ? '+' : '╣');
        bottom.setCharAt(bottom.length() - 1, BR.charAt(0));

        // Print header section
        System.out.println(top);
        System.out.println(header);
        System.out.println(sep);

        int x = 1;
        // Print each row dynamically
        for (Book book : books) {
            StringBuilder row = new StringBuilder(VSEP + " ");
            
            if (columns.contains(Column.INDEX))
                row.append(String.format("%-" + (indexWidth - 1) + "s" + VSEP + " ", x++));

            if (columns.contains(Column.ID))
                row.append(String.format("%-" + (idWidth - 1) + "s" + VSEP + " ", book.getBookId()));
            if (columns.contains(Column.TITLE))
                row.append(String.format("%-" + (titleWidth - 1) + "s" + VSEP + " ",
                        truncateString(book.getTitle(), titleWidth - 1)));
            if (columns.contains(Column.AUTHOR))
                row.append(String.format("%-" + (authorWidth - 1) + "s" + VSEP + " ",
                        truncateString(book.getAuthor(), authorWidth - 1)));
            if (columns.contains(Column.YEAR))
                row.append(String.format("%-" + (yearWidth - 1) + "s" + VSEP + " ", book.getYearPublished()));
            if (columns.contains(Column.LAST_EDITED))
                row.append(String.format("%-" + (dateWidth - 1) + "s" + VSEP,
                        formatDate(book.getLastEdited())));
            System.out.println(row);
        }

        // Print bottom border
        System.out.println(bottom);
    }


    public void printLibrarianTable(Boolean promptDes) {
        ArrayList<Book> books = getAllBooks();
        printTable(books, EnumSet.allOf(Column.class));

        if(promptDes){
            System.out.println("\nEnter a Book ID to view its description (or press Enter to skip)");
            System.out.println("==============================================");

            String input = scanner.nextLine().trim();

            // If user just presses Enter, skip
            if (input.isEmpty()) {
                return;
            }

            // Look for a matching Book ID
            for (Book book : books) {
                if (book.getBookId().equalsIgnoreCase(input)) {
                    System.out.println("Description: " + book.getDescription());
                    return;
                }
            }

            // If no match found
            System.out.println("\u001B[31mBook ID not found.\u001B[0m");
        }

        
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
        System.out.println(Ansi.RED + "Enter 0 to go back" + Ansi.RESET);
        System.out.println("==============================================");

        while (!inputComplete) {
            switch (step) {
                case 0: // Title
                    System.out.print("Enter book title: ");
                    title = scanner.nextLine().trim();
                    if (title.equals("0")) {
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
                    if (author.equals("0")) {
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
                    if (year.equals("0")) {
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
                    if (description.equals("0")) {
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
        fileHandling.appendToFile("books.ser", newBook, Book.class);
        System.out.println("==============================================");
        System.out.println(Ansi.ORANGE + newBook + " successfully added!" + Ansi.RESET);
    }

    // Remove a book by title
    public void removeBook() {
        System.out.println("==============================================\n");
        printLibrarianTable(false);
        System.out.println("\n" + Ansi.RED + "Enter 0 to go back" + Ansi.RESET);
        System.out.println("==============================================");

        System.out.print(Ansi.YELLOW + "Enter the Book ID to remove: " );
        String bookID = scanner.nextLine().trim();
        System.out.print(Ansi.RESET);

        
        if (bookID.equals("0")) {
            return;
        }


        ArrayList<Book> books = getAllBooks();

        if (books == null || books.isEmpty()) {
            System.out.println(Ansi.RED + "No books available." + Ansi.RESET);
            return;
        }

        // Find and remove book safely
        boolean found = false;
        Iterator<Book> iterator = books.iterator();
        while (iterator.hasNext()) {
            Book book = iterator.next();
            if (book.getBookId().equalsIgnoreCase(bookID)) {
                iterator.remove(); // safe removal during iteration

                found = true;

                if (ConsoleUtils.confirmAction("Are you sure? this action cannot be undone.")) {
                    fileHandling.overrideFile("books.ser", books);
                    System.out.println(Ansi.ORANGE + "Book with ID " + bookID + " has been removed." + Ansi.RESET);
                }
                else{
                    System.out.println(Ansi.ORANGE + "Cancelled." + Ansi.RESET);
                }

                break;
            }
        }

        if (!found) {
            System.out.println(Ansi.RED + "Book ID not found." + Ansi.RESET);
        }
        System.out.println("==============================================");
    }


    // Edit a book's details
    public void editBook() {
        System.out.println("==============================================");
        printLibrarianTable(false);
        System.out.println(Ansi.RED + "Enter 0 to go back" + Ansi.RESET);
        System.out.println("==============================================");

        System.out.print(Ansi.YELLOW + "Enter the Book ID to edit: " );
        String bookID = scanner.nextLine().trim();
        System.out.print(Ansi.RESET);

        
        if (bookID.equals("0")) {
            return;
        }


        ArrayList<Book> books = getAllBooks();

        if (books == null || books.isEmpty()) {
            System.out.println(Ansi.RED + "No books available." + Ansi.RESET);
            return;
        }

        // Find and remove book safely
        boolean found = false;
        Iterator<Book> iterator = books.iterator();
        while (iterator.hasNext()) {
            Book book = iterator.next();
            if (book.getBookId().equalsIgnoreCase(bookID)) {
                System.out.println(Ansi.ORANGE + "Editing Book: " + book.getTitle() + Ansi.RESET);
                found = true;
                while (true) {
                    System.out.println("==============================================");
                    System.out.println("\nSelect field to edit:\n");
                    
                    System.out.println(Ansi.ORANGE + "1." + Ansi.RESET + " Title");
                    System.out.println(Ansi.ORANGE + "2." + Ansi.RESET + " Author");
                    System.out.println(Ansi.ORANGE + "3." + Ansi.RESET + " Year Published");
                    System.out.println(Ansi.ORANGE + "4." + Ansi.RESET + " Description");
                    System.out.println(Ansi.ORANGE + "5." + Ansi.RESET + " Save and Exit");
                    System.out.println(Ansi.ORANGE + "6." + Ansi.RESET + " Back without saving");

                    System.out.print(Ansi.YELLOW + "\nEnter choice: " + Ansi.RESET);
                    
                    String choice = scanner.nextLine().trim();

                    switch (choice) {
                        case "1":
                            System.out.print("Enter new title: ");
                            String newTitle = scanner.nextLine().trim();
                            if (!newTitle.isEmpty()) {
                                book.setTitle(newTitle);
                                System.out.println(Ansi.ORANGE + "Title updated." + Ansi.RESET);
                            } else {
                                System.out.println(Ansi.RED + "Title cannot be empty!" + Ansi.RESET);
                            }
                            break;
                        case "2":
                            System.out.print("Enter new author: ");
                            String newAuthor = scanner.nextLine().trim();
                            if (!newAuthor.isEmpty()) {
                                book.setAuthor(newAuthor);
                                System.out.println(Ansi.ORANGE + "Author updated." + Ansi.RESET);
                            } else {
                                System.out.println(Ansi.RED + "Author cannot be empty!" + Ansi.RESET);
                            }
                            break;
                        case "3":
                            System.out.print("Enter new year published: ");
                            String newYear = scanner.nextLine().trim();
                            if (!newYear.isEmpty()) {
                                book.setYearPublished(newYear);
                                System.out.println(Ansi.ORANGE + "Year Published updated." + Ansi.RESET);
                            } else {
                                System.out.println(Ansi.RED + "Year Published cannot be empty!" + Ansi.RESET);
                            }
                            break;
                        case "4":
                            System.out.print("Enter new description: ");
                            String newDescription = scanner.nextLine().trim();
                            if (!newDescription.isEmpty()) {
                                book.setDescription(newDescription);
                                System.out.println(Ansi.ORANGE + "Description updated." + Ansi.RESET);
                            } else {
                                System.out.println(Ansi.RED + "Description cannot be empty!" + Ansi.RESET);
                            }
                            break;
                        case "5":
                            fileHandling.overrideFile("books.ser", books);
                            System.out.println(Ansi.ORANGE + "Changes saved." + Ansi.RESET);
                            return;
                        case "6":
                        case "0":
                            if (ConsoleUtils.confirmAction("Are you sure you want to exit?")) {
                                System.out.println(Ansi.RED + "Exiting without saving changes." + Ansi.RESET);
                                return;
                            }
                            else{
                                System.out.println(Ansi.ORANGE + "Cancelled." + Ansi.RESET);
                                break;
                            }

                        default:
                            System.out.println(Ansi.RED + "Invalid choice. Please try again." + Ansi.RESET);
                    }
                }
            }
        }

        if (!found) {
            System.out.println(Ansi.RED + "Book ID not found." + Ansi.RESET);
        }
        System.out.println("==============================================");
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
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM/dd/yy HH:mm");
        return sdf.format(date);
    }
    
    public static boolean supportsUnicode() {
    // Check the reported file encoding
    String encoding = System.getProperty("file.encoding", "").toLowerCase();

    // If it’s not UTF, we already know it's unsafe
    if (!encoding.contains("utf")) {
        return false;
    }

    // Try to print a test character and see if it's preserved
    try {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream testOut = new PrintStream(baos, true, "UTF-8");

        // Print a box character to our test stream
        testOut.print("╔");
        testOut.flush();

        String result = baos.toString("UTF-8");

        // If the character was replaced with '?' or missing, Unicode not supported visually
        if (result.contains("?") || result.isBlank()) {
            return false;
        }

        // Otherwise, likely supported
        return true;
    } catch (Exception e) {
        return false; // fallback safe default
    }
}

}
