package com.mycompany.library;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;

import com.mycompany.library.BorrowDetails.BorrowStatus;

import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class ManageBooks {

    // Initialize necessary components
    Scanner scanner = new Scanner(System.in);
    FileHandling fileHandling = new FileHandling();
    consoleUtil ConsoleUtils = new consoleUtil();
    
    private Auth auth;

    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    public Auth getAuth() {
        return auth;
    }

    // Define table columns
    public enum Column {
        INDEX, ID, TITLE, AUTHOR, GENRE, YEAR, LAST_EDITED, AVAILABLE
    }


    //=================================================
    //table printing methods
    //=================================================

    // Generic method to print table
    public void printTable(ArrayList<Book> books, EnumSet<Column> columns) {
        
        // Handle empty book list
        if (books == null || books.isEmpty()) {
            System.out.println("\u001B[31mNo books available.\u001B[0m");
            return;
        }


        // Detect Unicode support to decide border style
        boolean supportsUnicode = consoleUtil.detectUnicodeSupport();

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

        // Define headers title
        Map<Column, String> headers = Map.of(
            Column.INDEX, "Index",
            Column.ID, "Book ID",
            Column.TITLE, "Title",
            Column.AUTHOR, "Author",
            Column.GENRE, "Genre",
            Column.YEAR, "Year",
            Column.LAST_EDITED, "Last Edited",
            Column.AVAILABLE, "Available"
        );

        // calculate width needed for each column
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
                    case AVAILABLE -> String.valueOf(book.getAvailable());
                };
                max = Math.max(max, value == null ? 0 : value.length());
            }
            colWidths.put(col, max + 2); // +2 padding
        }

        //Build the borders and header dynamically
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

        //Print header
        System.out.println(top);
        System.out.println(header);
        System.out.println(sep);

        //Print rows
        int i = 1;
        for (Book book : books) {
            StringBuilder row = new StringBuilder(VSEP + " ");

            boolean isCurrentUserBorrowing = book.getBorrowDetails().stream()
                .anyMatch(record -> record.getStatus() == BorrowStatus.BORROWED && 
                        record.getUsername().equals(auth.getCurrentUser().getUsername()));

            String available = isCurrentUserBorrowing ? book.getAvailable() + "*" : String.valueOf(book.getAvailable());

            for (Column col : columns) {
                int width = colWidths.get(col);
                String value = switch (col) {
                    case INDEX -> String.valueOf(i);
                    case ID -> book.getBookId();
                    case TITLE -> truncateString(book.getTitle(), 30);
                    case AUTHOR -> book.getAuthor();
                    case GENRE -> book.getStringGenre();
                    case YEAR -> String.valueOf(book.getYearPublished());
                    case LAST_EDITED -> formatDate(book.getLastEdited());
                    case AVAILABLE -> String.valueOf(available);
                };

                String displayValue = (value == null) ? "" : value;
                String paddedValue = String.format("%-" + (width - 1) + "s", displayValue);

                // Apply color only to AVAILABLE column
                if (col == Column.AVAILABLE) {
                    String color = book.getAvailable() ? Ansi.GREEN : Ansi.ORANGE; // ORANGE not standard in ANSI
                    paddedValue = color + paddedValue + Ansi.RESET;
                }

                row.append(paddedValue).append(VSEP).append(" ");
            }
            System.out.println(row);
            i++;
        }

        //Print bottom border
        System.out.println(bottom);

        if(!auth.isAdmin()){
            System.out.println("\n* Already Borrowing");
        }


    }

   // Handles printing the table of books
    public void displayTable(boolean promptSelect) {
        ArrayList<Book> books = getAllBooks();


        boolean filter = true;

        boolean loop = true;

        while (loop) {

            
            if(filter){
                ArrayList<Book> filteredBooks = searchFilterMenu(books);
                filter = false;
            }

            if(filteredBooks.isEmpty()){
                break;
            }

            // Print table with selected columns

            if(auth.getCurrentUser().getRole() == "Librarian"){
                printTable(filteredBooks, EnumSet.of(
                    Column.INDEX, Column.TITLE, Column.AUTHOR, Column.GENRE, Column.YEAR, Column.LAST_EDITED, Column.AVAILABLE
                ));
            }
            else{
                printTable(filteredBooks, EnumSet.of(
                    Column.INDEX, Column.TITLE, Column.AUTHOR, Column.GENRE, Column.YEAR, Column.AVAILABLE
                ));
            }

            if (!promptSelect) break;

            
            if(auth.getCurrentUser().getRole() == "Librarian"){
                System.out.println("\nEnter a Book index to view its description");
                System.out.println("Press 'F' to filter again, or '0' to exit");
                System.out.println("==============================================");
            }
            else{
                System.out.println("\nEnter a book index to view more options.");
                System.out.println("Press 'F' to filter again, or '0' to exit");
                System.out.println("==============================================");
            }



            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue; 
            }

            // Handle different input types
            switch (input.toUpperCase()) {
                case "":
                    filteredBooks = new ArrayList<>();
                    continue; // Exit the method
                case "0":
                    filteredBooks = new ArrayList<>();
                    continue; // Exit the method
                    
                case "F":
                    filter = true;
                    continue; // Go back to filter menu
                    
                default:
                    // Validate numeric input for book selection
                    if (!input.matches("\\d+")) {
                        System.out.println(Ansi.RED + "Invalid input. Please enter a valid number, 'F' to filter, or '0' to exit." + Ansi.RESET);
                        continue;
                    }

                    int index = Integer.parseInt(input) - 1;
                    
                    if (index < 0 || index >= filteredBooks.size()) {
                        System.out.println(Ansi.RED + "Invalid index. Please select a number from the table." + Ansi.RESET);
                        continue;
                    }

                    // Valid book selected

                    if(auth.getCurrentUser().getRole().equals("Librarian")){
                        Book selectedBook = filteredBooks.get(index);

                        System.out.println("Title: " + selectedBook.getTitle());
                        System.out.println("Description: " + selectedBook.getDescription());
                        System.out.println("==============================================");
                        System.out.println("Press Enter to continue...");
                        scanner.nextLine();

                    }
                    if(auth.getCurrentUser().getRole().equals("Reader")){
                        //get book from index and display description
                        Book selectedBook = filteredBooks.get(index);

                        System.out.println("Title: " + selectedBook.getTitle());
                        System.out.println("Description: " + selectedBook.getDescription());
                        System.out.println("==============================================");

                        if(selectBookReader(selectedBook)){
                            loop = false;
                        }
                        
                        
                    }
            }     
        }   
    }

    // Menu shown after a reader selects a specific book
    public boolean selectBookReader(Book selectedBook) {
        boolean stayInMenu = true;
        boolean loop = true;

        boolean alreadyBorrowing = false;
        boolean alreadyFav = false;

        // Check if user is already borrowing this book
        alreadyBorrowing = selectedBook.getBorrowDetails().stream()
            .anyMatch(record -> record.getStatus() == BorrowStatus.BORROWED && 
                    record.getUsername().equals(auth.getCurrentUser().getUsername()));

        // Check if book is already in favorites
        alreadyFav = auth.getCurrentUser().getFavoriteBooks().contains(selectedBook.getBookId());
        

        while (loop) {
            System.out.println("\n==============================================");
            System.out.println(Ansi.BOLD + "Selected Book:" + Ansi.RESET + " " + selectedBook.getTitle());
            System.out.println("Author: " + selectedBook.getAuthor());
            System.out.println("Genre: " + selectedBook.getStringGenre());
            System.out.println("Year: " + selectedBook.getYearPublished());
            System.out.println("Available: " + (selectedBook.getAvailable() ? Ansi.GREEN + "Yes" : Ansi.RED + "No") + Ansi.RESET);
            System.out.println("==============================================");
            
            if (selectedBook.getAvailable()) {
                System.out.println(Ansi.ORANGE + "1." + Ansi.RESET + " Borrow Book");
            } 

            else if(alreadyBorrowing){
                System.out.println(Ansi.ORANGE + "1." + Ansi.RESET + " " + "\u001B[9mBorrow Book\u001B[0m" + " (already borrowing)");
            }
            
            else {
                System.out.println(Ansi.ORANGE + "1." + Ansi.RESET + " " + "\u001B[9mBorrow Book\u001B[0m" + " (Unavailable)");
            }

            if(alreadyFav){
                System.out.println(Ansi.ORANGE + "2." + Ansi.RESET + " Remove from Favorites");
            }
            else{
                System.out.println(Ansi.ORANGE + "2." + Ansi.RESET + " Add to Favorites");
            }
            
            System.out.println(Ansi.ORANGE + "0." + Ansi.RESET + " Back to Book List");
            System.out.println("==============================================");
            System.out.print(Ansi.YELLOW + "Enter your choice: " + Ansi.RESET);

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    if(alreadyBorrowing){
                        System.out.println(Ansi.RED + "You have already borrowed this book." + Ansi.RESET);
                        break;
                    }

                    if (!selectedBook.getAvailable()) {
                        System.out.println(Ansi.RED + "This book is currently borrowed and unavailable." + Ansi.RESET);
                        System.out.println("You can still add it to your favourites and borrow it once it becomes available.");

                        break;
                    }

                    int days = 0;

                        while (true) {
                            System.out.println("\n==============================================");
                            System.out.println(Ansi.ORANGE + "\nA fee of RM1.00 will be charged for each day a book is overdue.\n" + Ansi.RESET);
                            System.out.println(Ansi.BOLD + "Choose Borrow Duration" + Ansi.RESET);
                            System.out.println(Ansi.ORANGE + "1." + Ansi.RESET + " 7 days");
                            System.out.println(Ansi.ORANGE + "2." + Ansi.RESET + " 14 days");
                            System.out.println(Ansi.ORANGE + "3." + Ansi.RESET + " 30 days");
                            System.out.println(Ansi.ORANGE + "0." + Ansi.RESET + " Cancel");
                            System.out.println("==============================================");
                            System.out.print(Ansi.YELLOW + "Enter your choice: " + Ansi.RESET);

                            String borrowFor = scanner.nextLine().trim();

                            // Check for blank input
                            if (borrowFor.isEmpty()) {
                                System.out.println(Ansi.RED + "Input cannot be empty. Please enter a number (0–3)." + Ansi.RESET);
                                continue;
                            }

                            // Check if numeric
                            if (!borrowFor.matches("\\d+")) {
                                System.out.println(Ansi.RED + "Invalid input. Please enter only numbers (0–3)." + Ansi.RESET);
                                continue;
                            }

                            switch (borrowFor) {
                                case "1":
                                    days = 7;
                                    break;
                                case "2":
                                    days = 14;
                                    break;
                                case "3":
                                    days = 30;
                                    break;
                                case "0":
                                    System.out.println(Ansi.ORANGE + "Borrowing cancelled." + Ansi.RESET);
                                default:
                                    System.out.println(Ansi.RED + "Invalid option. Please choose between 0–3." + Ansi.RESET);
                            }

                            break;
                        }

                    if (ConsoleUtils.confirmAction("Borrow book?")) {
                        stayInMenu = false;
                        loop = false;
                        borrowBook(selectedBook, days) ;
                        break;
                    } else {
                        System.out.println(Ansi.ORANGE + "Cancelled." + Ansi.RESET);
                        stayInMenu = true;
                        loop = true;
                        break;
                    }


                case "2":
                        if(alreadyFav){
                            removeFromFavorites(selectedBook);
                            alreadyFav = false;
                            break;
                        }
                        
                        stayInMenu = true;
                        loop = true;
                        alreadyFav = true;
                        addToFavorites(selectedBook);
                        break;

                case "0":
                    loop = false;
                    System.out.println(Ansi.ORANGE + "Returning to book list..." + Ansi.RESET);
                    break;

                default:
                    System.out.println(Ansi.RED + "Invalid choice. Please try again." + Ansi.RESET);
            }
        }

        if(stayInMenu){
            return false;
        }
        else{
            return true;
        }
    }


    public void getFavourites(ArrayList<String> favoriteBooks) {
        ArrayList<Book> books = getAllBooks();
        Iterator<Book> bookIt = books.iterator();  
        while (bookIt.hasNext()) {
            Book book = bookIt.next();
            if (!favoriteBooks.contains(book.getBookId())) {
                bookIt.remove();
            }
        }

        ArrayList<Book> filteredBooks = books;

        if (filteredBooks.isEmpty()) {
            System.out.println(Ansi.RED + "\nNo books favourited." + Ansi.RESET);
            return;
        }

        while (true) {
            filteredBooks.sort((a, b) -> Boolean.compare(!a.getAvailable(), !b.getAvailable()));

            // Print table with selected columns
            printTable(filteredBooks, EnumSet.of(
                Column.INDEX, Column.TITLE, Column.AUTHOR, Column.GENRE, Column.YEAR, Column.AVAILABLE
            ));


            System.out.println("\nEnter a book index to view more options.");
            System.out.println("(Press 0 to exit)");
            System.out.println("==============================================");

            String input = scanner.nextLine().trim();
            
            if(input.equals("0")) break;

            // If user pressed Enter, exit
            if (input.isEmpty()) continue;



            // Validate numeric input
            if (!input.matches("\\d+")) {
                System.out.println(Ansi.RED + "Invalid input. Please enter a valid number or press Enter to exit." + Ansi.RESET);
                continue;
            }

            int index = Integer.parseInt(input) - 1;
            if (index < 0 || index >= filteredBooks.size()) {
                System.out.println(Ansi.RED + "Invalid index. Please select a number from the table." + Ansi.RESET);
                continue;
            }

            // Valid book selected
            
            Book selectedBook = filteredBooks.get(index);
            if(selectBookReader(selectedBook)){
                break;
            }
        }
    }

    private void borrowBook(Book book, int days) {
        

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, days);
        Date dueDate = calendar.getTime();

        BorrowDetails record = new BorrowDetails(
            auth.getCurrentUser().getUsername(),
            book.getBookId(),
            dueDate
        );

        book.setAvailable(false);
        book.addBorrowRecord(record);

        ArrayList<Book> books = getAllBooks();

        for (int i = 0; i < books.size(); i++) {
            Book b = books.get(i);
            if (b.getBookId().equals(book.getBookId())) {
                books.set(i, book);
                break;
            }
        }

        fileHandling.overrideFile("books.ser", books);

        System.out.println(Ansi.GREEN + "\nSuccessfully borrowed \"" + book.getTitle() + "\"!" + Ansi.RESET);
        System.out.println("Due Date: " + Ansi.CYAN + formatDateTime(dueDate) + Ansi.RESET);
    }

    private void addToFavorites(Book book) {
        ArrayList<User> accounts = fileHandling.readFromFile("accounts.ser", User.class);

        String bookID = book.getBookId();
        auth.getCurrentUser().addFavoriteBook(bookID);


        for (int i = 0; i < accounts.size(); i++) {
            User u = accounts.get(i);
            if (u.getUsername().equals(auth.getCurrentUser().getUsername())) {
                accounts.set(i, auth.getCurrentUser());
                break;
            }
        }

        fileHandling.overrideFile("accounts.ser", accounts);

        System.out.println(Ansi.GREEN + book.getTitle() + " added to favourites"+ Ansi.RESET);
    }

    private void removeFromFavorites(Book book) {
        ArrayList<User> accounts = fileHandling.readFromFile("accounts.ser", User.class);
        String bookID = book.getBookId();

        ArrayList<String> favorites = auth.getCurrentUser().getFavoriteBooks();

        if (favorites.contains(bookID)) {
            favorites.remove(bookID);
            System.out.println(Ansi.GREEN + book.getTitle() + " has been removed from your favourites." + Ansi.RESET);
        }

        for (int i = 0; i < accounts.size(); i++) {
            User u = accounts.get(i);
            if (u.getUsername().equals(auth.getCurrentUser().getUsername())) {
                accounts.set(i, auth.getCurrentUser());
                break;
            }
        }

        // Save updated accounts back to file
        fileHandling.overrideFile("accounts.ser", accounts);
    }


    
    String searchQuery = "";
    HashMap<String, String> activeFilters = new HashMap<>();
    ArrayList<Book> filteredBooks = new ArrayList<>();
    
    //handle searching and filtering of books
    public ArrayList<Book> searchFilterMenu(ArrayList<Book> books) {

        if(filteredBooks.isEmpty()){
            filteredBooks = new ArrayList<>(books);
        }


        //loop until user decides to exit
        while (true) {
            System.out.println("============== Filter & Search ===============");

            //display number of current results        
            String role = getAuth().getCurrentUser().getRole();

            if(role.equals("Librarian")){
                printTable(filteredBooks, EnumSet.of(Column.INDEX, Column.TITLE, Column.AUTHOR, Column.GENRE, Column.YEAR, Column.LAST_EDITED, Column.AVAILABLE));
            }
            else{
                printTable(filteredBooks, EnumSet.of(Column.INDEX, Column.TITLE, Column.AUTHOR, Column.GENRE, Column.YEAR, Column.AVAILABLE));
            };

            System.out.println("\nCurrent results: " + filteredBooks.size() + " book(s).\n");
            

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
                if(activeFilters.containsKey("Availability")){
                    System.out.print(Ansi.BLUE + " (Availability: " + activeFilters.get("Availability") + ")" + Ansi.RESET);
                }
                
            }
            System.out.println("\n   -> Narrow results by author, year, or genre.");

            System.out.print(Ansi.ORANGE + "0." + Ansi.RESET + " Go back");
            System.out.println("\n   -> go back to main menu.\n");

            

            System.out.print("Select an option (1-3) or" + Ansi.ORANGE + " press Enter to continue" + Ansi.RESET + ": ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    filteredBooks = new ArrayList<>(books);
                    searchQuery = "";
                    activeFilters.clear();
                    System.out.println("Showing all books.");
                    break;

                case "2":
                    System.out.print("\nEnter search keyword (title/author/year/id): ");
                    searchQuery = scanner.nextLine().trim().toLowerCase();
                    filteredBooks = new ArrayList<>();
                    for (Book b : books) {
                        if (b.getTitle().toLowerCase().contains(searchQuery)
                                || b.getAuthor().toLowerCase().contains(searchQuery)
                                || b.getBookId().toLowerCase().contains(searchQuery)
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
                System.out.println(Ansi.ORANGE + "4. " + Ansi.RESET + "Availability");
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
                } else if (filterChoice.equals("4")) {
                    System.out.println("\nSelect availability type:");
                    System.out.println(Ansi.ORANGE + "1. " + Ansi.RESET + "Available");
                    System.out.println(Ansi.ORANGE + "2. " + Ansi.RESET + "Unavailable");
                    System.out.print(Ansi.YELLOW + "Select option (1-2, leave empty to clear availability filter): " + Ansi.RESET );
                    String availabilityChoice = scanner.nextLine().trim();

                    if (availabilityChoice.isEmpty()) {
                        activeFilters.remove("Availability");
                        System.out.println("Availability filter cleared.");
                    } else {
                        switch (availabilityChoice) {
                            case "1" -> activeFilters.put("Availability", "Available");
                            case "2" -> activeFilters.put("Availability", "Unavailable");
                            default -> System.out.println("Invalid option. Availability filter canceled.");
                        }
                    }
                }
                    

                else {
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
                        case "Availability" -> {

                            if(value.equals("Available")){
                                filteredBooks.removeIf(b -> b.getAvailable() == false);
                            }
                            else{
                                filteredBooks.removeIf(b -> b.getAvailable() == true);
                            }              
                            

                        }
                        
                    }
                }

                break;

                case "0":
                    searchQuery = "";
                    activeFilters = new HashMap<>();
                    return filteredBooks = new ArrayList<>();

                case "":
                    if(filteredBooks.size() > 0){
                        return filteredBooks;
                    }
                    else{
                        System.out.println(Ansi.RED + "No books available." + Ansi.RESET);
                        continue;
                    }
                    

                default:
                    System.out.println("\u001B[31mInvalid choice.\u001B[0m");
                    continue;
            }


        }
    }


    //=================================================
    //To retrive, add, remove, edit books
    //=================================================

    // Get all books
    public ArrayList<Book> getAllBooks() {
        ArrayList<Book> books = fileHandling.readFromFile("books.ser", Book.class);
        books.sort( (a, b) -> { return -1 * a.getTitle().compareTo(b.getTitle()); } );
        return books != null ? books : new ArrayList<>();
    }

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
                        } else {                            
                            System.out.println(Ansi.RED + "Genre cannot be empty!" + Ansi.RESET);
                        }
                    }
            }
        }

        Book newBook = new Book(title, author, year, description, genres, true);
        fileHandling.appendToFile("books.ser", newBook, Book.class);
        System.out.println("==============================================");
        System.out.println(Ansi.ORANGE + newBook + " successfully added!" + Ansi.RESET);
    }

    // Remove a book by index 
    public void removeBook() {
        String input;
        ArrayList<Book> books = getAllBooks();

        if (books == null || books.isEmpty()) {
            System.out.println(Ansi.RED + "No books available." + Ansi.RESET);
            return;
        }

        while(true){
            System.out.println("==============================================\n");
            displayTable(false); // shows index column
            System.out.println("\n" + Ansi.RED + "Press 'F' to filter again, or '0' to exit" + Ansi.RESET);
            System.out.println("==============================================");

            System.out.print(Ansi.YELLOW + "Enter the book index to remove: " + Ansi.RESET);
            input = scanner.nextLine().trim();

            if(input.toLowerCase().equals("f")){
                continue;
            }
            
            break;
        }
        

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
        String input;
        ArrayList<Book> books = getAllBooks();

        if (books == null || books.isEmpty()) {
            System.out.println(Ansi.RED + "No books available." + Ansi.RESET);
            return;
        }

        while(true){
            System.out.println("==============================================\n");
            displayTable(false); // shows index column
            System.out.println("\n" + Ansi.RED + "Press 'F' to filter again, or '0' to exit" + Ansi.RESET);
            System.out.println("==============================================");

            System.out.print(Ansi.YELLOW + "Enter the book index to edit: " + Ansi.RESET);
            input = scanner.nextLine().trim();

            if(input.toLowerCase().equals("f")){
                continue;
            }
            
            break;
        }

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



    //=================================================
    //helper methods
    //=================================================

    // Helper method to truncate long strings
    private static String truncateString(String str, int length) {
        if (str == null) return "";
        if (str.length() <= length) return str;
        return str.substring(0, length - 3) + "...";
    }

    // Helper method to format dates
    private static String formatDate(Date date) {
        if (date == null) return "";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yy");
        return sdf.format(date);
    }

    private static String formatDateTime(Date date) {
        if (date == null) return "";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yy HH:mm");
        return sdf.format(date);
    }

    


}

