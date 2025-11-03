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
import java.util.List;
import java.util.Map;

public class ManageBooks {

    Scanner scanner = new Scanner(System.in);
    FileHandling fileHandling = new FileHandling();
    consoleUtil ConsoleUtils = new consoleUtil();

    // Define columns as an enum
    public enum Column {
        INDEX, ID, TITLE, AUTHOR, GENRE, YEAR, LAST_EDITED
    }

    public static void printTable(ArrayList<Book> books, EnumSet<Column> columns) {
        if (books == null || books.isEmpty()) {
            System.out.println("\u001B[31mNo books available.\u001B[0m");
            return;
        }

        //boolean supportsUnicode = detectUnicodeSupport();
        boolean supportsUnicode = true;
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
            Column.GENRE, "Genre",
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
                    case TITLE -> truncateString(book.getTitle(), 30);
                    case AUTHOR -> book.getAuthor();
                    case GENRE -> book.getStringGenre();
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
                    case TITLE -> truncateString(book.getTitle(), 30);
                    case AUTHOR -> book.getAuthor();
                    case GENRE -> book.getStringGenre();
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
        ArrayList<Book> filteredBooks = searchFilterMenu(books);

        while (true) {
            printTable(filteredBooks, EnumSet.of(Column.INDEX, Column.TITLE, Column.AUTHOR, Column.GENRE, Column.YEAR, Column.LAST_EDITED));

            if (promptDes) {
                System.out.println("\nEnter a Book index to view its description (or press Enter to skip)");
                System.out.println("==============================================");

                String input = scanner.nextLine().trim(); // <-- read entire line safely

                // if user presses Enter -> skip
                if (input.isEmpty()) {
                    break; // or return; depending on your flow
                }

                // check if numeric
                if (!input.matches("\\d+")) {
                    System.out.println(Ansi.RED + "Invalid input. Please enter a number from the table or press Enter to skip." + Ansi.RESET);
                    continue;
                }

                int index = Integer.parseInt(input) - 1;

                if (index < 0 || index >= filteredBooks.size()) {
                    System.out.println(Ansi.RED + "Invalid index. Please select a valid number from the table." + Ansi.RESET);
                    continue;
                }

                Book selectedBook = filteredBooks.get(index);

                System.out.println("Title: " + selectedBook.getTitle());
                System.out.println("Description: " + selectedBook.getDescription());
                System.out.println("==============================================");
                System.out.println("Press Enter to continue...");
                scanner.nextLine(); // wait for Enter
            }

            break; // exit the while loop
        }

        
    }

    public void printReaderTable(Boolean promptSelect) {
        ArrayList<Book> books = getAllBooks();

        //menu to select filters and add search query could be added here
        ArrayList<Book> filteredBooks = searchFilterMenu(books);

        while (true) {
            printTable(filteredBooks, EnumSet.of(Column.INDEX, Column.TITLE, Column.AUTHOR, Column.GENRE, Column.YEAR, Column.LAST_EDITED));

            if (promptSelect) {
                System.out.println("\nEnter a Book index to select it to borrow or favourite. (or press Enter to skip)");
                System.out.println("==============================================");

                String input = scanner.nextLine().trim(); // <-- read entire line safely

                // if user presses Enter -> skip
                if (input.isEmpty()) {
                    break; // or return; depending on your flow
                }

                // check if numeric
                if (!input.matches("\\d+")) {
                    System.out.println(Ansi.RED + "Invalid input. Please enter a number from the table or press Enter to skip." + Ansi.RESET);
                    continue;
                }

                int index = Integer.parseInt(input) - 1;

                if (index < 0 || index >= filteredBooks.size()) {
                    System.out.println(Ansi.RED + "Invalid index. Please select a valid number from the table." + Ansi.RESET);
                    continue;
                }

                Book selectedBook = filteredBooks.get(index);

                selectBookReader(selectedBook);

            }

            break; // exit the while loop
        }

        
    }

    public void selectBookReader(Book selectedBook) {
        boolean untilExit = true;
        while (untilExit) {
            System.out.println("\nSelected: " + selectedBook.getTitle() + "\n" );
            System.out.println(Ansi.ORANGE + "1." + Ansi.RESET + " Borrow Book");
            System.out.println("   -> Adds a new book to the library.");
            System.out.println(Ansi.ORANGE + "2." + Ansi.RESET + " Add to Favorites");
            System.out.println("   -> Removes a book from the library.");
            System.out.println(Ansi.ORANGE + "0." + Ansi.RESET + " Back");
            System.out.println("   -> Goes back to main menu.");
            System.out.println("==============================================\n");
            System.out.print(Ansi.YELLOW + "Enter your choice: " + Ansi.RESET);

            String input = scanner.next();

            switch (input) {
                case "1":
                    System.out.println(Ansi.ORANGE + "Add a book..." + Ansi.RESET + "\n");
                    break;

                case "2":
                    System.out.println(Ansi.ORANGE + "Remove a book..." + Ansi.RESET + "\n");
                    break;


                case "0":
                    untilExit = false;
                    System.out.println(Ansi.ORANGE + "Going back..." + Ansi.RESET + "\n");
                    printReaderTable(true);
                    break;

                default:
                    System.out.println(Ansi.RED + "Invalid choice. Please try again." + Ansi.RESET);
            }
        
        }

    }
    


    public ArrayList<Book> searchFilterMenu(ArrayList<Book> books) {
        ArrayList<Book> filteredBooks = new ArrayList<>(books);
        String searchQuery = "";
        HashMap<String, String> activeFilters = new HashMap<>();
        while (true) {
            System.out.println("============== Filter & Search ===============");

            // Optional: preview results each time
            System.out.println("\nCurrent results: " + filteredBooks.size() + " book(s).\n");
            // You can call your printTable(filteredBooks, EnumSet.allOf(Column.class)) here if you want live preview

            System.out.println(Ansi.ORANGE + "1." + Ansi.RESET + " Clear filters");
            System.out.println("   -> Clear all active filters and search queries.");

            System.out.print(Ansi.ORANGE + "2." + Ansi.RESET + " Search books");
            if (!searchQuery.isEmpty()) {
                System.out.print(Ansi.BLUE + " (Searching: " + searchQuery + ")" + Ansi.RESET);
            }
            System.out.println("\n   -> Find by title, author, or year.");

            System.out.print(Ansi.ORANGE + "3." + Ansi.RESET + " Filter books");
            if (!activeFilters.isEmpty()) {
                if(activeFilters.containsKey("Author")){
                    System.out.print(Ansi.BLUE + " (Author: " + activeFilters.get("Author") + ")" + Ansi.RESET);
                }
                if(activeFilters.containsKey("Year")){
                    System.out.print(Ansi.BLUE + " (Year: " + activeFilters.get("Year") + ")" + Ansi.RESET);
                }
                if(activeFilters.containsKey("Genre")){
                    System.out.print(Ansi.BLUE + " (Genre: " + activeFilters.get("Genre") + ")" + Ansi.RESET);
                }
                
            }
            System.out.println("\n   -> Narrow results by author, year, or genre.\n");

            

            System.out.print("Select an option (1-3) or" + Ansi.ORANGE + " press Enter to display table" + Ansi.RESET + ": ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    filteredBooks = new ArrayList<>(books);
                    System.out.println("Showing all books.");
                    break;

                case "2":
                    System.out.print("\nEnter search keyword (title/author/year): ");
                    searchQuery = scanner.nextLine().trim().toLowerCase();
                    filteredBooks = new ArrayList<>();
                    for (Book b : books) {
                        if (b.getTitle().toLowerCase().contains(searchQuery)
                                || b.getAuthor().toLowerCase().contains(searchQuery)
                                || b.getYearPublished().toLowerCase().contains(searchQuery)) {
                            filteredBooks.add(b);
                        }
                    }
                    if (filteredBooks.isEmpty()) {
                        System.out.println("\u001B[33mNo results found for: " + searchQuery + "\u001B[0m");
                    } else {
                        System.out.println(filteredBooks.size() + " result(s) found.");
                    }
                    break;

                case "3":
                System.out.println("\nFilter by:");
                System.out.println(Ansi.ORANGE + "1. " + Ansi.RESET + "Author");
                System.out.println(Ansi.ORANGE + "2. " + Ansi.RESET + "Year Published");
                System.out.println(Ansi.ORANGE + "3. " + Ansi.RESET + "Genre");
                System.out.print("Choose filter type: ");
                String filterChoice = scanner.nextLine().trim();

                if (filterChoice.equals("1")) {
                    System.out.print(Ansi.YELLOW + "Enter author name (leave empty to clear author filter): " + Ansi.RESET);
                    String author = scanner.nextLine().trim();

                    if (author.isEmpty()) {
                        activeFilters.remove("Author");
                        System.out.println("Author filter cleared.");
                    } else {
                        activeFilters.put("Author", author.toLowerCase());
                        System.out.println("Filtered by author.");
                    }

                } else if (filterChoice.equals("2")) {
                    System.out.println("\nSelect year filter type:");
                    System.out.println(Ansi.ORANGE + "1. " + Ansi.RESET + "Before year");
                    System.out.println(Ansi.ORANGE + "2. " + Ansi.RESET + "After year");
                    System.out.println(Ansi.ORANGE + "3. " + Ansi.RESET + "Exact year");
                    
                    System.out.print(Ansi.YELLOW + "Select option (1-3, leave empty to clear year filter): " + Ansi.RESET );
                    String selection = scanner.nextLine().trim();

                    if (selection.isEmpty()) {
                        activeFilters.remove("Year");
                        System.out.println("Year filter cleared.");
                    } else {
                        System.out.print("Enter year (leave empty to clear): ");
                        String yearInput = scanner.nextLine().trim();

                        if (yearInput.isEmpty()) {
                            activeFilters.remove("Year");
                            System.out.println("Year filter cleared.");
                        } else if (!yearInput.matches("\\d{4}")) {
                            System.out.println("Invalid year format. Please enter a 4-digit year.");
                        } else {
                            int year = Integer.parseInt(yearInput);
                            switch (selection) {
                                case "1" -> activeFilters.put("Year", "Before " + year);
                                case "2" -> activeFilters.put("Year", "After " + year);
                                case "3" -> activeFilters.put("Year", "Exactly " + year);
                                default -> System.out.println("Invalid option. Year filter canceled.");
                            }
                        }
                    }

                } else if (filterChoice.equals("3")) {
                    System.out.print(Ansi.YELLOW + "Enter genre (leave empty to clear genre filter): " + Ansi.RESET);
                    String genre = scanner.nextLine().trim();

                    if (genre.isEmpty()) {
                        activeFilters.remove("Genre");
                        System.out.println("Genre filter cleared.");
                    } else {
                        activeFilters.put("Genre", genre.toLowerCase());
                        System.out.println("Filtered by genre.");
                    }

                } else {
                    System.out.println("\u001B[31mInvalid filter option.\u001B[0m");
                    break;
                }

                // === Reapply all active filters ===
                filteredBooks = new ArrayList<>(books); // reset to full list first

                for (Map.Entry<String, String> entry : activeFilters.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();

                    switch (key) {
                        case "Author" -> filteredBooks.removeIf(b -> !b.getAuthor().toLowerCase().contains(value));
                        case "Year" -> {
                            String[] parts = value.split(" ");
                            try {
                                int year = Integer.parseInt(parts[parts.length - 1]);
                                if (value.startsWith("Before"))
                                    filteredBooks.removeIf(b -> Integer.parseInt(b.getYearPublished()) >= year);
                                else if (value.startsWith("After"))
                                    filteredBooks.removeIf(b -> Integer.parseInt(b.getYearPublished()) <= year);
                                else if (value.startsWith("Exactly"))
                                    filteredBooks.removeIf(b -> !b.getYearPublished().equals(String.valueOf(year)));
                            } catch (Exception e) {
                                filteredBooks.removeIf(b -> true); // skip if invalid
                            }
                        }
                        case "Genre" -> {
                            Iterator<Book> bookIt = filteredBooks.iterator();
                            while (bookIt.hasNext()) {
                                Book book = bookIt.next();
                                boolean found = false;
                                for (String g : book.getGenre()) {
                                    if (g.toLowerCase().contains(value)) {
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) bookIt.remove();
                            }
                        }
                    }
                }

                break;

                case "":
                    // Exit the filter/search menu and return results
                    return filteredBooks;

                default:
                    System.out.println("\u001B[31mInvalid choice.\u001B[0m");
                    continue;
            }


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





    // Helper method to truncate long strings
    private static String truncateString(String str, int length) {
        if (str == null) return "";
        if (str.length() <= length) return str;
        return str.substring(0, length - 3) + "...";
    }

    // Helper method to format dates
    private static String formatDate(Date date) {
        if (date == null) return "";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM/dd/yy");
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

    public void loadBooks(){
        ArrayList<Book> books = new ArrayList<>();

        books.add(new Book("To Kill a Mockingbird", "Harper Lee", "1960", "A powerful story about racial injustice and moral growth in the Deep South.", new ArrayList<>(List.of("Classic", "Drama"))));
        books.add(new Book("1984", "George Orwell", "1949", "A chilling dystopian vision of totalitarianism and surveillance.", new ArrayList<>(List.of("Dystopian", "Political Fiction"))));
        books.add(new Book("Pride and Prejudice", "Jane Austen", "1813", "A witty romantic tale of manners and marriage in 19th-century England.", new ArrayList<>(List.of("Romance", "Classic"))));
        books.add(new Book("The Great Gatsby", "F. Scott Fitzgerald", "1925", "A tragic story of love, wealth, and the American Dream.", new ArrayList<>(List.of("Classic", "Tragedy"))));
        books.add(new Book("Moby-Dick", "Herman Melville", "1851", "An obsessive captain hunts a giant white whale across the seas.", new ArrayList<>(List.of("Adventure", "Classic"))));
        books.add(new Book("Brave New World", "Aldous Huxley", "1932", "A futuristic society engineered for stability at the cost of individuality.", new ArrayList<>(List.of("Science Fiction", "Dystopian"))));
        books.add(new Book("The Catcher in the Rye", "J.D. Salinger", "1951", "A cynical teenager narrates his disillusionment with society.", new ArrayList<>(List.of("Classic", "Coming-of-Age"))));
        books.add(new Book("The Lord of the Rings", "J.R.R. Tolkien", "1954", "A monumental fantasy saga of good versus evil in Middle-earth.", new ArrayList<>(List.of("Fantasy", "Adventure"))));
        books.add(new Book("Harry Potter and the Sorcerer's Stone", "J.K. Rowling", "1997", "A young boy discovers he is a wizard and attends a magical school.", new ArrayList<>(List.of("Fantasy", "Young Adult"))));
        books.add(new Book("The Hobbit", "J.R.R. Tolkien", "1937", "A reluctant hobbit embarks on an epic quest to reclaim treasure from a dragon.", new ArrayList<>(List.of("Fantasy", "Adventure"))));

        books.add(new Book("Frankenstein", "Mary Shelley", "1818", "A scientist creates life, only to be horrified by his own creation.", new ArrayList<>(List.of("Horror", "Science Fiction"))));
        books.add(new Book("Dracula", "Bram Stoker", "1897", "A vampire from Transylvania terrorizes Victorian England.", new ArrayList<>(List.of("Horror", "Gothic"))));
        books.add(new Book("The Alchemist", "Paulo Coelho", "1988", "A shepherd follows his dream to find treasure and discovers his destiny.", new ArrayList<>(List.of("Philosophical", "Adventure"))));
        books.add(new Book("The Kite Runner", "Khaled Hosseini", "2003", "A story of friendship, betrayal, and redemption set in Afghanistan.", new ArrayList<>(List.of("Drama", "Historical Fiction"))));
        books.add(new Book("Life of Pi", "Yann Martel", "2001", "A boy stranded on a lifeboat with a tiger must fight for survival.", new ArrayList<>(List.of("Adventure", "Philosophical"))));
        books.add(new Book("The Da Vinci Code", "Dan Brown", "2003", "A symbologist uncovers secrets hidden within Leonardo da Vinci’s works.", new ArrayList<>(List.of("Thriller", "Mystery"))));
        books.add(new Book("Angels & Demons", "Dan Brown", "2000", "A professor races to stop a secret society from destroying the Vatican.", new ArrayList<>(List.of("Thriller", "Mystery"))));
        books.add(new Book("The Girl with the Dragon Tattoo", "Stieg Larsson", "2005", "A journalist and hacker uncover dark family secrets.", new ArrayList<>(List.of("Mystery", "Crime"))));
        books.add(new Book("Gone Girl", "Gillian Flynn", "2012", "A man becomes the prime suspect in his wife’s mysterious disappearance.", new ArrayList<>(List.of("Thriller", "Psychological"))));
        books.add(new Book("The Silence of the Lambs", "Thomas Harris", "1988", "An FBI trainee seeks help from a brilliant cannibal to catch a killer.", new ArrayList<>(List.of("Crime", "Horror"))));

        books.add(new Book("The Hunger Games", "Suzanne Collins", "2008", "Teens are forced to fight to the death in a televised survival contest.", new ArrayList<>(List.of("Dystopian", "Young Adult"))));
        books.add(new Book("Dune", "Frank Herbert", "1965", "A young nobleman becomes the messiah of a desert planet.", new ArrayList<>(List.of("Science Fiction", "Adventure"))));
        books.add(new Book("The Martian", "Andy Weir", "2011", "An astronaut stranded on Mars must use ingenuity to survive.", new ArrayList<>(List.of("Science Fiction", "Survival"))));
        books.add(new Book("Ready Player One", "Ernest Cline", "2011", "A virtual reality treasure hunt decides the fate of the digital world.", new ArrayList<>(List.of("Science Fiction", "Adventure"))));
        books.add(new Book("The Fault in Our Stars", "John Green", "2012", "Two teens with cancer fall in love and confront mortality.", new ArrayList<>(List.of("Romance", "Young Adult"))));
        books.add(new Book("A Game of Thrones", "George R.R. Martin", "1996", "Noble families vie for control of a vast medieval kingdom.", new ArrayList<>(List.of("Fantasy", "Political"))));
        books.add(new Book("The Name of the Wind", "Patrick Rothfuss", "2007", "A gifted young man tells his legend of music, magic, and loss.", new ArrayList<>(List.of("Fantasy", "Adventure"))));
        books.add(new Book("The Road", "Cormac McCarthy", "2006", "A father and son struggle to survive in a post-apocalyptic world.", new ArrayList<>(List.of("Post-Apocalyptic", "Drama"))));
        books.add(new Book("The Shining", "Stephen King", "1977", "A family’s winter caretaker descends into madness at a haunted hotel.", new ArrayList<>(List.of("Horror", "Thriller"))));
        books.add(new Book("It", "Stephen King", "1986", "A group of friends faces a shape-shifting monster haunting their town.", new ArrayList<>(List.of("Horror", "Supernatural"))));

        books.add(new Book("The Outsiders", "S.E. Hinton", "1967", "Two rival gangs struggle with class conflict and brotherhood.", new ArrayList<>(List.of("Drama", "Coming-of-Age"))));
        books.add(new Book("The Book Thief", "Markus Zusak", "2005", "A young girl finds solace in books during World War II.", new ArrayList<>(List.of("Historical Fiction", "Drama"))));
        books.add(new Book("The Color Purple", "Alice Walker", "1982", "An African-American woman finds her voice in the early 20th century.", new ArrayList<>(List.of("Drama", "Feminist Fiction"))));
        books.add(new Book("Beloved", "Toni Morrison", "1987", "A former slave is haunted by the ghost of her dead daughter.", new ArrayList<>(List.of("Historical Fiction", "Supernatural"))));
        books.add(new Book("A Tale of Two Cities", "Charles Dickens", "1859", "Love and sacrifice amid the turmoil of the French Revolution.", new ArrayList<>(List.of("Classic", "Historical"))));
        books.add(new Book("Les Misérables", "Victor Hugo", "1862", "A story of redemption and justice set in revolutionary France.", new ArrayList<>(List.of("Classic", "Historical"))));
        books.add(new Book("The Picture of Dorian Gray", "Oscar Wilde", "1890", "A man’s portrait ages in his place as he lives a life of sin.", new ArrayList<>(List.of("Philosophical", "Gothic"))));
        books.add(new Book("Crime and Punishment", "Fyodor Dostoevsky", "1866", "A man wrestles with guilt after committing murder.", new ArrayList<>(List.of("Classic", "Psychological"))));
        books.add(new Book("The Brothers Karamazov", "Fyodor Dostoevsky", "1880", "A complex story of faith, morality, and patricide.", new ArrayList<>(List.of("Philosophical", "Classic"))));
        books.add(new Book("Anna Karenina", "Leo Tolstoy", "1877", "A married woman’s affair leads to social ruin and tragedy.", new ArrayList<>(List.of("Romance", "Classic"))));

        books.add(new Book("The Shadow of the Wind", "Carlos Ruiz Zafón", "2001", "A boy discovers a mysterious book that changes his life.", new ArrayList<>(List.of("Mystery", "Historical Fiction"))));
        books.add(new Book("The Night Circus", "Erin Morgenstern", "2011", "Two magicians compete in a mysterious, magical circus.", new ArrayList<>(List.of("Fantasy", "Romance"))));
        books.add(new Book("The Handmaid's Tale", "Margaret Atwood", "1985", "A woman struggles under a totalitarian theocracy that enslaves women.", new ArrayList<>(List.of("Dystopian", "Feminist Fiction"))));
        books.add(new Book("Never Let Me Go", "Kazuo Ishiguro", "2005", "Students at a boarding school slowly learn their dark purpose.", new ArrayList<>(List.of("Science Fiction", "Drama"))));
        books.add(new Book("Cloud Atlas", "David Mitchell", "2004", "Interconnected stories span centuries exploring power and rebirth.", new ArrayList<>(List.of("Science Fiction", "Philosophical"))));
        books.add(new Book("The Goldfinch", "Donna Tartt", "2013", "A boy's life is shaped by a tragic museum bombing.", new ArrayList<>(List.of("Drama", "Mystery"))));
        books.add(new Book("A Man Called Ove", "Fredrik Backman", "2012", "A grumpy old man finds purpose through unlikely friendships.", new ArrayList<>(List.of("Drama", "Humor"))));
        books.add(new Book("Educated", "Tara Westover", "2018", "A woman raised off-the-grid pursues education and self-discovery.", new ArrayList<>(List.of("Memoir", "Inspirational"))));
        books.add(new Book("The Midnight Library", "Matt Haig", "2020", "A woman explores alternate versions of her life through a magical library.", new ArrayList<>(List.of("Fantasy", "Philosophical"))));
        books.add(new Book("Project Hail Mary", "Andy Weir", "2021", "A lone astronaut must save humanity with help from an unlikely ally.", new ArrayList<>(List.of("Science Fiction", "Adventure"))));
        
        books.add(new Book("The Secret History", "Donna Tartt", "1992", "A group of elite college students are drawn into a murder after studying ancient Greek rituals.", new ArrayList<>(List.of("Mystery", "Psychological"))));
        books.add(new Book("The Invisible Man", "H.G. Wells", "1897", "A scientist discovers invisibility but descends into madness and violence.", new ArrayList<>(List.of("Science Fiction", "Classic"))));
        books.add(new Book("Rebecca", "Daphne du Maurier", "1938", "A shy woman marries a wealthy widower but is haunted by his first wife's legacy.", new ArrayList<>(List.of("Gothic", "Mystery"))));
        books.add(new Book("The Time Machine", "H.G. Wells", "1895", "A Victorian inventor travels far into the future to witness humanity's evolution.", new ArrayList<>(List.of("Science Fiction", "Adventure"))));
        books.add(new Book("The Call of the Wild", "Jack London", "1903", "A domesticated dog must embrace his primal instincts in the harsh Yukon wilderness.", new ArrayList<>(List.of("Adventure", "Classic"))));
        books.add(new Book("White Fang", "Jack London", "1906", "A wild wolf-dog learns to survive and trust in the human world.", new ArrayList<>(List.of("Adventure", "Animal Fiction"))));
        books.add(new Book("The Old Man and the Sea", "Ernest Hemingway", "1952", "An aging fisherman battles the sea and a giant marlin for survival and pride.", new ArrayList<>(List.of("Classic", "Adventure"))));
        books.add(new Book("For Whom the Bell Tolls", "Ernest Hemingway", "1940", "A young American joins guerrilla fighters in the Spanish Civil War.", new ArrayList<>(List.of("Historical Fiction", "War"))));
        books.add(new Book("Catch-22", "Joseph Heller", "1961", "A satirical look at the absurdity of war and bureaucracy.", new ArrayList<>(List.of("Satire", "War"))));
        books.add(new Book("Slaughterhouse-Five", "Kurt Vonnegut", "1969", "A soldier becomes unstuck in time, reliving moments of war and peace.", new ArrayList<>(List.of("Science Fiction", "Satire"))));

        books.add(new Book("The Sound and the Fury", "William Faulkner", "1929", "The decline of a Southern family told through multiple fragmented perspectives.", new ArrayList<>(List.of("Classic", "Modernist"))));
        books.add(new Book("Of Mice and Men", "John Steinbeck", "1937", "Two migrant workers dream of a better life during the Great Depression.", new ArrayList<>(List.of("Classic", "Drama"))));
        books.add(new Book("East of Eden", "John Steinbeck", "1952", "A modern retelling of Cain and Abel set in California’s Salinas Valley.", new ArrayList<>(List.of("Classic", "Family Saga"))));
        books.add(new Book("The Grapes of Wrath", "John Steinbeck", "1939", "A family flees the Dust Bowl seeking hope in California.", new ArrayList<>(List.of("Historical Fiction", "Drama"))));
        books.add(new Book("A Farewell to Arms", "Ernest Hemingway", "1929", "A romance between a soldier and a nurse during World War I ends in tragedy.", new ArrayList<>(List.of("Romance", "War"))));
        books.add(new Book("Wuthering Heights", "Emily Brontë", "1847", "A dark, passionate tale of love and revenge on the Yorkshire moors.", new ArrayList<>(List.of("Romance", "Gothic"))));
        books.add(new Book("Jane Eyre", "Charlotte Brontë", "1847", "An orphaned governess overcomes hardship and finds love and independence.", new ArrayList<>(List.of("Romance", "Classic"))));
        books.add(new Book("Great Expectations", "Charles Dickens", "1861", "An orphan’s life is transformed by mysterious benefactors and lost love.", new ArrayList<>(List.of("Classic", "Coming-of-Age"))));
        books.add(new Book("Oliver Twist", "Charles Dickens", "1838", "An orphan navigates poverty and crime in Victorian London.", new ArrayList<>(List.of("Classic", "Social Commentary"))));
        books.add(new Book("David Copperfield", "Charles Dickens", "1850", "A boy’s journey from hardship to success in 19th-century England.", new ArrayList<>(List.of("Classic", "Coming-of-Age"))));

        books.add(new Book("The Wind-Up Bird Chronicle", "Haruki Murakami", "1994", "A man searches for his missing wife and discovers surreal mysteries.", new ArrayList<>(List.of("Magical Realism", "Mystery"))));
        books.add(new Book("Norwegian Wood", "Haruki Murakami", "1987", "A young man struggles with love and loss in 1960s Tokyo.", new ArrayList<>(List.of("Romance", "Drama"))));
        books.add(new Book("Kafka on the Shore", "Haruki Murakami", "2002", "A runaway boy and an old man’s fates intertwine in a surreal journey.", new ArrayList<>(List.of("Magical Realism", "Fantasy"))));
        books.add(new Book("1Q84", "Haruki Murakami", "2009", "Two characters find themselves in an alternate Tokyo filled with mystery.", new ArrayList<>(List.of("Dystopian", "Magical Realism"))));
        books.add(new Book("The Wind in the Willows", "Kenneth Grahame", "1908", "Animal friends embark on whimsical adventures by the riverbank.", new ArrayList<>(List.of("Children's", "Fantasy"))));
        books.add(new Book("Charlotte’s Web", "E.B. White", "1952", "A pig named Wilbur is saved by the clever spider Charlotte.", new ArrayList<>(List.of("Children's", "Classic"))));
        books.add(new Book("Matilda", "Roald Dahl", "1988", "A gifted girl uses her intelligence and telekinesis to overcome cruelty.", new ArrayList<>(List.of("Children's", "Fantasy"))));
        books.add(new Book("Charlie and the Chocolate Factory", "Roald Dahl", "1964", "A poor boy wins a tour of a magical chocolate factory.", new ArrayList<>(List.of("Children's", "Fantasy"))));
        books.add(new Book("The BFG", "Roald Dahl", "1982", "A friendly giant teams up with a girl to stop man-eating giants.", new ArrayList<>(List.of("Children's", "Fantasy"))));
        books.add(new Book("Coraline", "Neil Gaiman", "2002", "A girl discovers a dark alternate world behind a secret door.", new ArrayList<>(List.of("Fantasy", "Horror"))));

        books.add(new Book("American Gods", "Neil Gaiman", "2001", "A man becomes entangled in a conflict between old and new deities.", new ArrayList<>(List.of("Fantasy", "Mythology"))));
        books.add(new Book("Good Omens", "Neil Gaiman & Terry Pratchett", "1990", "An angel and a demon team up to stop the apocalypse.", new ArrayList<>(List.of("Fantasy", "Comedy"))));
        books.add(new Book("The Color of Magic", "Terry Pratchett", "1983", "A bumbling wizard embarks on an absurd and dangerous journey.", new ArrayList<>(List.of("Fantasy", "Satire"))));
        books.add(new Book("Mort", "Terry Pratchett", "1987", "Death takes on an apprentice who falls in love with a princess.", new ArrayList<>(List.of("Fantasy", "Humor"))));
        books.add(new Book("Small Gods", "Terry Pratchett", "1992", "A god discovers he has only one believer left and must rebuild his faith.", new ArrayList<>(List.of("Fantasy", "Philosophical"))));
        books.add(new Book("The Lies of Locke Lamora", "Scott Lynch", "2006", "A master thief leads a group of con artists in a fantastical city.", new ArrayList<>(List.of("Fantasy", "Crime"))));
        books.add(new Book("Mistborn: The Final Empire", "Brandon Sanderson", "2006", "A street thief discovers she can harness magical metals to fight a tyrant.", new ArrayList<>(List.of("Fantasy", "Adventure"))));
        books.add(new Book("Elantris", "Brandon Sanderson", "2005", "A cursed city once filled with godlike beings hides a powerful secret.", new ArrayList<>(List.of("Fantasy", "Political"))));
        books.add(new Book("The Way of Kings", "Brandon Sanderson", "2010", "An epic tale of war, honor, and destiny on a storm-ravaged world.", new ArrayList<>(List.of("Fantasy", "Epic"))));
        books.add(new Book("Warbreaker", "Brandon Sanderson", "2009", "Two sisters become caught in a web of politics, color, and magic.", new ArrayList<>(List.of("Fantasy", "Romance"))));

        books.add(new Book("The Stand", "Stephen King", "1978", "A deadly plague wipes out humanity, leaving survivors to choose between good and evil.", new ArrayList<>(List.of("Horror", "Post-Apocalyptic"))));
        books.add(new Book("Pet Sematary", "Stephen King", "1983", "A burial ground brings the dead back—with terrible consequences.", new ArrayList<>(List.of("Horror", "Supernatural"))));
        books.add(new Book("Salem’s Lot", "Stephen King", "1975", "A writer discovers vampires have infested his hometown.", new ArrayList<>(List.of("Horror", "Thriller"))));
        books.add(new Book("Carrie", "Stephen King", "1974", "A bullied teen with telekinetic powers unleashes revenge at prom.", new ArrayList<>(List.of("Horror", "Psychological"))));
        books.add(new Book("The Green Mile", "Stephen King", "1996", "A death row guard witnesses miracles in a condemned man.", new ArrayList<>(List.of("Drama", "Supernatural"))));
        books.add(new Book("The Andromeda Strain", "Michael Crichton", "1969", "Scientists race to contain an extraterrestrial microbe threatening humanity.", new ArrayList<>(List.of("Science Fiction", "Thriller"))));
        books.add(new Book("Jurassic Park", "Michael Crichton", "1990", "Genetically resurrected dinosaurs escape an island theme park.", new ArrayList<>(List.of("Science Fiction", "Adventure"))));
        books.add(new Book("Sphere", "Michael Crichton", "1987", "A team of scientists investigates a mysterious alien spacecraft under the sea.", new ArrayList<>(List.of("Science Fiction", "Thriller"))));
        books.add(new Book("Timeline", "Michael Crichton", "1999", "Archaeologists travel back to medieval France using quantum technology.", new ArrayList<>(List.of("Science Fiction", "Historical"))));
        books.add(new Book("Prey", "Michael Crichton", "2002", "Nanorobots gain consciousness and threaten human civilization.", new ArrayList<>(List.of("Science Fiction", "Thriller"))));

        
        fileHandling.overrideFile("books.ser", books);
    }


}

