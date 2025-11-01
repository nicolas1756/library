package com.mycompany.library;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Map;

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
            System.out.println("\u001B[31mNo books available.\u001B[0m");
            return;
        }

        boolean supportsUnicode = detectUnicodeSupport();

        // Borders
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

        // Step 1: Determine column headers
        Map<Column, String> headers = Map.of(
            Column.INDEX, "Index",
            Column.ID, "Book ID",
            Column.TITLE, "Title",
            Column.AUTHOR, "Author",
            Column.YEAR, "Year",
            Column.LAST_EDITED, "Last Edited"
        );

        // Step 2: Calculate max width for each column
        Map<Column, Integer> colWidths = new HashMap<>();
        for (Column col : columns) {
            int max = headers.get(col).length();
            for (Book book : books) {
                String value = switch (col) {
                    case INDEX -> String.valueOf(books.indexOf(book) + 1);
                    case ID -> String.valueOf(book.getBookId());
                    case TITLE -> book.getTitle();
                    case AUTHOR -> book.getAuthor();
                    case YEAR -> String.valueOf(book.getYearPublished());
                    case LAST_EDITED -> formatDate(book.getLastEdited());
                };
                max = Math.max(max, value == null ? 0 : value.length());
            }
            colWidths.put(col, max + 2); // +2 padding
        }

        // Step 3: Build the borders and header dynamically
        StringBuilder top = new StringBuilder(TL);
        StringBuilder header = new StringBuilder(VSEP + " ");
        StringBuilder sep = new StringBuilder(HSEP);
        StringBuilder bottom = new StringBuilder(BL);

        for (Column col : columns) {
            int width = colWidths.get(col);
            top.append(HLINE.repeat(width)).append(TSEP);
            header.append(String.format("%-" + (width - 1) + "s" + VSEP + " ", headers.get(col)));
            sep.append(HLINE.repeat(width)).append(CROSS);
            bottom.append(HLINE.repeat(width)).append(BSEP);
        }

        // Fix last border characters
        top.setCharAt(top.length() - 1, TR.charAt(0));
        sep.setCharAt(sep.length() - 1, HSEP.equals("+") ? '+' : '╣');
        bottom.setCharAt(bottom.length() - 1, BR.charAt(0));

        // Step 4: Print header
        System.out.println(top);
        System.out.println(header);
        System.out.println(sep);

        // Step 5: Print rows
        int i = 1;
        for (Book book : books) {
            StringBuilder row = new StringBuilder(VSEP + " ");
            for (Column col : columns) {
                int width = colWidths.get(col);
                String value = switch (col) {
                    case INDEX -> String.valueOf(i);
                    case ID -> String.valueOf(book.getBookId());
                    case TITLE -> book.getTitle();
                    case AUTHOR -> book.getAuthor();
                    case YEAR -> String.valueOf(book.getYearPublished());
                    case LAST_EDITED -> formatDate(book.getLastEdited());
                };
                row.append(String.format("%-" + (width - 1) + "s" + VSEP + " ", value == null ? "" : value));
            }
            System.out.println(row);
            i++;
        }

        // Step 6: Print bottom border
        System.out.println(bottom);
    }


    public void printLibrarianTable(Boolean promptDes) {
        ArrayList<Book> books = getAllBooks();

        //menu to select filters and add search query could be added here
        ArrayList<Book> filteredbooks = searchFilterMenu(books);
        printTable(filteredbooks, EnumSet.allOf(Column.class));

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

    public ArrayList<Book> searchFilterMenu(ArrayList<Book> books) {
        ArrayList<Book> filteredBooks = new ArrayList<>(books);

        while (true) {
            System.out.println("\n================" + Ansi.BOLD + " Filter & Search " + Ansi.RESET +"================");
            System.out.println("1. Show all books");
            System.out.println("2. Search books");
            System.out.println("3. Filter books");
            System.out.println("4. Clear filters");
            System.out.println("5. Exit menu and show table");
            System.out.print("Select an option (1-5): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    filteredBooks = new ArrayList<>(books);
                    System.out.println("Showing all books.");
                    break;

                case "2":
                    System.out.print("Enter search keyword (title/author/year): ");
                    String keyword = scanner.nextLine().trim().toLowerCase();
                    filteredBooks = new ArrayList<>();
                    for (Book b : books) {
                        if (b.getTitle().toLowerCase().contains(keyword)
                                || b.getAuthor().toLowerCase().contains(keyword)
                                || b.getYearPublished().toLowerCase().contains(keyword)) {
                            filteredBooks.add(b);
                        }
                    }
                    if (filteredBooks.isEmpty()) {
                        System.out.println("\u001B[33mNo results found for: " + keyword + "\u001B[0m");
                    } else {
                        System.out.println(filteredBooks.size() + " result(s) found.");
                    }
                    break;

                case "3":
                    System.out.println("Filter by:");
                    System.out.println("   1. Author");
                    System.out.println("   2. Year Published");
                    System.out.print("Choose filter type: ");
                    String filterChoice = scanner.nextLine().trim();

                    if (filterChoice.equals("1")) {
                        System.out.print("Enter author name: ");
                        String author = scanner.nextLine().trim().toLowerCase();
                        filteredBooks.removeIf(b -> !b.getAuthor().toLowerCase().contains(author));
                        System.out.println("Filtered by author.");
                    } else if (filterChoice.equals("2")) {
                        System.out.print("Enter year: ");
                        String year = scanner.nextLine().trim();
                        filteredBooks.removeIf(b -> !b.getYearPublished().equalsIgnoreCase(year));
                        System.out.println("Filtered by year.");
                    } else {
                        System.out.println("\u001B[31mInvalid filter option.\u001B[0m");
                    }
                    break;

                case "4":
                    filteredBooks = new ArrayList<>(books);
                    System.out.println("Filters cleared.");
                    break;

                case "5":
                    // Exit the filter/search menu and return results
                    return filteredBooks;

                default:
                    System.out.println("\u001B[31mInvalid choice.\u001B[0m");
                    break;
            }

            // Optional: preview results each time
            System.out.println("\nCurrent results: " + filteredBooks.size() + " book(s).");
            // You can call your printTable(filteredBooks, EnumSet.allOf(Column.class)) here if you want live preview
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
        String title = "", author = "", year = "", description = "", genreInput = "";
        ArrayList<String> genres = new ArrayList<>();
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
                        step++;
                    } else {
                        System.out.println(Ansi.RED + "Description cannot be empty!" + Ansi.RESET);
                    }
                    break;
                case 4: //genre
                    while(true){
                        System.out.print("Enter book genre (comma-separated for multiple): ");
                        genreInput = scanner.nextLine().trim();
                        if (genreInput.equals("0")) {
                            step--;
                            break;
                        }
                        if (!genreInput.isEmpty()) {
                            String[] genreArray = genreInput.split(",");
                            for(String g : genreArray){
                                genres.add(g.trim());
                            }
                            inputComplete = true;
                            break;
                        } else {                            System.out.println(Ansi.RED + "Genre cannot be empty!" + Ansi.RESET);
                        }
                    }
            }
        }

        Book newBook = new Book(title, author, year, description, genres);
        fileHandling.appendToFile("books.ser", newBook, Book.class);
        System.out.println("==============================================");
        System.out.println(Ansi.ORANGE + newBook + " successfully added!" + Ansi.RESET);
    }

    // Remove a book by index 
    public void removeBook() {
        System.out.println("==============================================\n");
        printLibrarianTable(false); // shows index column
        System.out.println("\n" + Ansi.RED + "Enter 0 to go back" + Ansi.RESET);
        System.out.println("==============================================");

        ArrayList<Book> books = getAllBooks();

        if (books == null || books.isEmpty()) {
            System.out.println(Ansi.RED + "No books available." + Ansi.RESET);
            return;
        }

        System.out.print(Ansi.YELLOW + "Enter the book index to remove: " + Ansi.RESET);
        String input = scanner.nextLine().trim();

        if (input.equals("0")) {
            System.out.println(Ansi.ORANGE + "Cancelled." + Ansi.RESET);
            return;
        }

        // Validate numeric input
        int index;
        try {
            index = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println(Ansi.RED + "Invalid input. Please enter a number." + Ansi.RESET);
            return;
        }

        // Convert to zero-based index
        index -= 1;

        if (index < 0 || index >= books.size()) {
            System.out.println(Ansi.RED + "Invalid index. Please select a valid number from the table." + Ansi.RESET);
            return;
        }

        Book selectedBook = books.get(index);

        System.out.println(Ansi.YELLOW + "Selected book: " + Ansi.RESET + selectedBook.getTitle() + " (" + selectedBook.getBookId() + ")");
        if (ConsoleUtils.confirmAction("Are you sure? This action cannot be undone.")) {
            books.remove(index);
            fileHandling.overrideFile("books.ser", books);
            System.out.println(Ansi.ORANGE + "Book \"" + selectedBook.getTitle() + "\" (ID: " + selectedBook.getBookId() + ") has been removed." + Ansi.RESET);
        } else {
            System.out.println(Ansi.ORANGE + "Cancelled." + Ansi.RESET);
        }

        System.out.println("==============================================");
    }



    // Edit a book's details
    public void editBook() {
        System.out.println("==============================================");
        printLibrarianTable(false);
        System.out.println(Ansi.RED + "Enter 0 to go back" + Ansi.RESET);
        System.out.println("==============================================");

        ArrayList<Book> books = getAllBooks();

        if (books == null || books.isEmpty()) {
            System.out.println(Ansi.RED + "No books available." + Ansi.RESET);
            return;
        }

        System.out.print(Ansi.YELLOW + "Enter the book index to edit: " + Ansi.RESET);
        String input = scanner.nextLine().trim();

        if (input.equals("0")) {
            System.out.println(Ansi.ORANGE + "Cancelled." + Ansi.RESET);
            return;
        }

        // Validate numeric input
        int index;
        try {
            index = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println(Ansi.RED + "Invalid input. Please enter a number." + Ansi.RESET);
            return;
        }

        // Convert to zero-based index
        index -= 1;

        if (index < 0 || index >= books.size()) {
            System.out.println(Ansi.RED + "Invalid index. Please select a valid number from the table." + Ansi.RESET);
            return;
        }

        Book book = books.get(index);

        System.out.println(Ansi.ORANGE + "Editing Book: " + book.getTitle() + " (ID: " + book.getBookId() + ")" + Ansi.RESET);

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
                    System.out.println(Ansi.ORANGE + "Changes saved for \"" + book.getTitle() + "\"." + Ansi.RESET);
                    return;

                case "6":
                case "0":
                    if (ConsoleUtils.confirmAction("Are you sure you want to exit without saving?")) {
                        System.out.println(Ansi.RED + "Exiting without saving changes." + Ansi.RESET);
                        return;
                    } else {
                        System.out.println(Ansi.ORANGE + "Cancelled." + Ansi.RESET);
                    }
                    break;

                default:
                    System.out.println(Ansi.RED + "Invalid choice. Please try again." + Ansi.RESET);
            }
        }
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
    
    private static boolean detectUnicodeSupport() {
    String os = System.getProperty("os.name").toLowerCase();
    String term = System.getenv("TERM");
    String conEmu = System.getenv("ConEmuANSI");
    String wtSession = System.getenv("WT_SESSION");

    // Heuristics for terminals that usually support Unicode
    return (wtSession != null) || // Windows Terminal
           (conEmu != null && conEmu.equalsIgnoreCase("ON")) || // ConEmu
           (term != null && (term.contains("xterm") || term.contains("ansi") || term.contains("vt100"))) ||
           (!os.contains("win")); // Assume true for macOS/Linux
}


}
