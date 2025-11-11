package com.mycompany.library;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import com.mycompany.library.BorrowDetails.BorrowStatus;

public class ManageBooks {

    //================================================
    // CONSTANTS
    //================================================
    private static final String BOOKS_FILE = "books.ser";
    private static final String ACCOUNTS_FILE = "accounts.ser";
    private static final int BORROW_DURATION_7_DAYS = 7;
    private static final int BORROW_DURATION_14_DAYS = 14;
    private static final int BORROW_DURATION_30_DAYS = 30;
    
    //================================================
    // INSTANCE FIELDS
    //================================================
    private final Scanner scanner = new Scanner(System.in);
    private final FileHandling fileHandling = new FileHandling();

    private String searchQuery = "";
    private HashMap<String, String> activeFilters = new HashMap<>();
    private ArrayList<Book> filteredBooks = new ArrayList<>();
    
    
    //===============================================
    // Auth, to retrieve current user info
    //===============================================

    private Auth auth;
    
    public void setAuth(Auth auth) {
        this.auth = auth;
    }
    
    public Auth getAuth() {
        return auth;
    }

    //=================================================
    // helper methods
    //=================================================

    private static String formatDateTime(Date date) {
        if (date == null) return "";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yy HH:mm");
        return sdf.format(date);
    }

    private boolean alreadyBorrowing(Book selectedBook) {
        return selectedBook.getBorrowDetails().stream()
            .anyMatch(record -> record.getStatus() == BorrowStatus.BORROWED && 
                    record.getUsername().equals(auth.getCurrentUser().getUsername()));
    }

    private boolean alreadyFavorited(Book selectedBook) {
        return auth.getCurrentUser().getFavoriteBooks().contains(selectedBook.getBookId());
    }

    /**
     * Updates a book in the books list and saves to file
     */
    private void updateBookInList(Book book) {
        ArrayList<Book> books = getAllBooks();
        
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getBookId().equals(book.getBookId())) {
                books.set(i, book);
                break;
            }
        }
        
        fileHandling.overrideFile(BOOKS_FILE, books);
    }

    /**
     * Updates current user in accounts file
     */
    private void updateUserInAccounts() {
        ArrayList<User> accounts = fileHandling.readFromFile(ACCOUNTS_FILE, User.class);
        
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getUsername().equals(auth.getCurrentUser().getUsername())) {
                accounts.set(i, auth.getCurrentUser());
                break;
            }
        }
        
        fileHandling.overrideFile(ACCOUNTS_FILE, accounts);
    }

    //===============================================
    // ui methods
    //===============================================

    // Handles printing the table of books
    public boolean displayTable(boolean promptSelect) {
        ArrayList<Book> books = getAllBooks();

        boolean filter = true;
        boolean loop = true;

        while (loop) {

            if(filter){
                filteredBooks = searchFilterMenu(books);
                filter = false;
            }

            if(filteredBooks.isEmpty()){
                break;
            }

            consoleUtil.printTable(filteredBooks, "books", auth);
            if (!promptSelect) break;

            displayTablePrompt();

            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {continue;}

            // Handle different input types
            switch (input.toUpperCase()) {
                case "0":
                    loop = false;
                    System.out.println(Ansi.ORANGE + "Exiting book list..." + Ansi.RESET + "\n");
                    break;
                    
                case "F":
                    filter = true;
                    continue;
                    
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
                    if(selectorMenu(books, index)){
                        loop = false;
                    }
                    else{
                        loop = true;
                    }
            }     
        }   

        if(filteredBooks.isEmpty()){
            return false;
        }
        return true;
    }

    /**
     * Displays prompt for table navigation
     */
    private void displayTablePrompt() {
        if(auth.getCurrentUser().getRole().equals("Librarian")){
            System.out.println("\nEnter a Book index to view its description");
        }
        else{
            System.out.println("\nEnter a book index to view more options.");
        }

        System.out.println("Press 'F' to filter again, or '0' to exit");
        System.out.println("==============================================");
    }

    public boolean selectorMenu(ArrayList<Book> books, int index) {
        Book selectedBook = filteredBooks.get(index);

        System.out.println("Title: " + selectedBook.getTitle());
        System.out.println("Description: " + selectedBook.getDescription());
        System.out.println("==============================================");
        
        if(auth.getCurrentUser().getRole().equals("Librarian")){
            System.out.println("Press Enter to continue...");
            scanner.nextLine();
            return true;
        }
        
        if(auth.getCurrentUser().getRole().equals("Reader")){
            if(selectBookReader(selectedBook)){
                return false;
            }               
        }

        return true;
    }

    // Menu shown after a reader selects a specific book
    public boolean selectBookReader(Book selectedBook) {
        boolean stayInMenu = true;
        boolean loop = true;

        boolean alreadyBorrowing = alreadyBorrowing(selectedBook);
        boolean alreadyFav = alreadyFavorited(selectedBook);
        
        while (loop) {
            displayBookDetails(selectedBook, alreadyBorrowing, alreadyFav);

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    if(handleBorrowBook(selectedBook, alreadyBorrowing)){
                        filteredBooks.clear();
                        stayInMenu = false;
                        loop = false;
                    }
                    break;

                case "2":
                    if(alreadyFav){
                        removeFromFavorites(selectedBook);
                        alreadyFav = false;
                    }
                    else{
                        addToFavorites(selectedBook);
                        alreadyFav = true;
                    }
                    break;

                case "0":
                    loop = false;
                    System.out.println(Ansi.ORANGE + "Returning to book list..." + Ansi.RESET);
                    break;

                default:
                    System.out.println(Ansi.RED + "Invalid choice. Please try again." + Ansi.RESET);
            }
        }

        return stayInMenu;
    }

    /**
     * Displays book details and menu options
     */
    private void displayBookDetails(Book selectedBook, boolean alreadyBorrowing, boolean alreadyFav) {
        System.out.println("\n==============================================");
        System.out.println(Ansi.BOLD + "Selected Book:" + Ansi.RESET + " " + selectedBook.getTitle());
        System.out.println("Author: " + selectedBook.getAuthor());
        System.out.println("Genre: " + selectedBook.getStringGenre());
        System.out.println("Year: " + selectedBook.getYearPublished());
        System.out.println("Available: " + (selectedBook.getAvailable() ? Ansi.GREEN + "Yes" : Ansi.RED + "No") + Ansi.RESET);
        System.out.println("==============================================");
        
        // Borrow option
        if (selectedBook.getAvailable()) {
            System.out.println(Ansi.ORANGE + "1." + Ansi.RESET + " Borrow Book");
        } 
        else if(alreadyBorrowing){
            System.out.println(Ansi.ORANGE + "1." + Ansi.RESET + " " + "\u001B[9mBorrow Book\u001B[0m" + " (already borrowing)");
        }
        else {
            System.out.println(Ansi.ORANGE + "1." + Ansi.RESET + " " + "\u001B[9mBorrow Book\u001B[0m" + " (Unavailable)");
        }

        // Favorite option
        if(alreadyFav){
            System.out.println(Ansi.ORANGE + "2." + Ansi.RESET + " Remove from Favorites");
        }
        else{
            System.out.println(Ansi.ORANGE + "2." + Ansi.RESET + " Add to Favorites");
        }
        
        System.out.println(Ansi.ORANGE + "0." + Ansi.RESET + " Back to Book List");
        System.out.println("==============================================");
        System.out.print(Ansi.YELLOW + "Enter your choice: " + Ansi.RESET);
    }

    /**
     * Handles book borrowing logic
     * @return true if borrow completed and should exit menu
     */
    private boolean handleBorrowBook(Book selectedBook, boolean alreadyBorrowing) {
        if(alreadyBorrowing){
            System.out.println(Ansi.RED + "You have already borrowed this book." + Ansi.RESET);
            return false;
        }

        if (!selectedBook.getAvailable()) {
            System.out.println(Ansi.RED + "This book is currently borrowed and unavailable." + Ansi.RESET);
            System.out.println("You can still add it to your favourites and borrow it once it becomes available.");
            return false;
        }

        int days = promptBorrowDuration();
        if (days == 0) return false; // User cancelled

        if (consoleUtil.confirmAction("Borrow book?")) {
            borrowBook(selectedBook, days);
            return true; // Exit menu after borrowing
        } else {
            System.out.println(Ansi.ORANGE + "Cancelled." + Ansi.RESET);
            return false;
        }
    }

    /**
     * Prompts user to select borrow duration
     * @return number of days (7, 14, or 30), or 0 if cancelled
     */
    private int promptBorrowDuration() {
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
                    return BORROW_DURATION_7_DAYS;
                case "2":
                    return BORROW_DURATION_14_DAYS;
                case "3":
                    return BORROW_DURATION_30_DAYS;
                case "0":
                    System.out.println(Ansi.ORANGE + "Borrowing cancelled." + Ansi.RESET);
                    return 0;
                default:
                    System.out.println(Ansi.RED + "Invalid option. Please choose between 0–3." + Ansi.RESET);
            }
        }
    }

    public void getFavourites(ArrayList<String> favoriteBooks) {
        while (true) {
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

            filteredBooks.sort((a, b) -> Boolean.compare(!a.getAvailable(), !b.getAvailable()));

            consoleUtil.printTable(filteredBooks, "books", auth);

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

        updateBookInList(book);

        System.out.println(Ansi.GREEN + "\nSuccessfully borrowed \"" + book.getTitle() + "\"!" + Ansi.RESET);
        System.out.println("Due Date: " + Ansi.CYAN + formatDateTime(dueDate) + Ansi.RESET);
    }

    private void addToFavorites(Book book) {
        String bookID = book.getBookId();
        auth.getCurrentUser().addFavoriteBook(bookID);
        updateUserInAccounts();
        System.out.println(Ansi.GREEN + book.getTitle() + " added to favourites"+ Ansi.RESET);
    }

    private void removeFromFavorites(Book book) {
        String bookID = book.getBookId();
        ArrayList<String> favorites = auth.getCurrentUser().getFavoriteBooks();

        if (favorites.contains(bookID)) {
            favorites.remove(bookID);
            System.out.println(Ansi.GREEN + book.getTitle() + " has been removed from your favourites." + Ansi.RESET);
        }

        updateUserInAccounts();
    }

    //handle searching and filtering of books
    public ArrayList<Book> searchFilterMenu(ArrayList<Book> books) {

        if(filteredBooks.isEmpty()){
            filteredBooks = new ArrayList<>(books);
        }

        //loop until user decides to exit
        while (true) {
            displayFilterMenu();

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    clearFilters(books);
                    break;

                case "2":
                    applySearch(books);
                    break;

                case "3":
                    applyFilter(books);
                    break;

                case "0":
                    searchQuery = "";
                    activeFilters = new HashMap<>();
                    return new ArrayList<>();

                case "":
                    if(filteredBooks.size() > 0){
                        return filteredBooks;
                    }
                    else{
                        System.out.println(Ansi.RED + "No books available." + Ansi.RESET);
                    }
                    break;

                default:
                    System.out.println(Ansi.RED + "Invalid choice." + Ansi.RESET);
            }
        }
    }

    /**
     * Displays filter menu
     */
    private void displayFilterMenu() {
        System.out.println("============== Filter & Search ===============");

        consoleUtil.printTable(filteredBooks, "books", auth);

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
            displayActiveFilters();
        }
        System.out.println("\n   -> Narrow results by author, year, or genre.");

        System.out.print(Ansi.ORANGE + "0." + Ansi.RESET + " Go back");
        System.out.println("\n   -> go back to main menu.\n");

        System.out.print("Select an option (1-3) or" + Ansi.ORANGE + " press Enter to continue" + Ansi.RESET + ": ");
    }

    /**
     * Displays currently active filters
     */
    private void displayActiveFilters() {
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

    /**
     * Clears all filters and search
     */
    private void clearFilters(ArrayList<Book> books) {
        filteredBooks = new ArrayList<>(books);
        searchQuery = "";
        activeFilters.clear();
        System.out.println("Showing all books.");
    }

    /**
     * Applies search query
     */
    private void applySearch(ArrayList<Book> books) {
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
            System.out.println(Ansi.YELLOW + "No results found for: " + searchQuery + Ansi.RESET);
        } else {
            System.out.println(filteredBooks.size() + " result(s) found.");
        }
    }

    /**
     * Applies filter based on user selection
     */
    private void applyFilter(ArrayList<Book> books) {
        System.out.println("\nFilter by:");
        System.out.println(Ansi.ORANGE + "1. " + Ansi.RESET + "Author");
        System.out.println(Ansi.ORANGE + "2. " + Ansi.RESET + "Year Published");
        System.out.println(Ansi.ORANGE + "3. " + Ansi.RESET + "Genre");
        System.out.println(Ansi.ORANGE + "4. " + Ansi.RESET + "Availability");
        System.out.print("Choose filter type: ");
        
        String filterChoice = scanner.nextLine().trim();

        switch (filterChoice) {
            case "1" -> filterByAuthor();
            case "2" -> filterByYear();
            case "3" -> filterByGenre();
            case "4" -> filterByAvailability();
            default -> {
                System.out.println(Ansi.RED + "Invalid filter option." + Ansi.RESET);
                return;
            }
        }

        // Reapply all filters
        reapplyAllFilters(books);
    }

    /**
     * Filter by author
     */
    private void filterByAuthor() {
        System.out.print(Ansi.YELLOW + "Enter author name (leave empty to clear author filter): " + Ansi.RESET);
        String author = scanner.nextLine().trim();

        if (author.isEmpty()) {
            activeFilters.remove("Author");
            System.out.println("Author filter cleared.");
        } else {
            activeFilters.put("Author", author.toLowerCase());
            System.out.println("Filtered by author.");
        }
    }

    /**
     * Filter by year
     */
    private void filterByYear() {
        System.out.println("\nSelect year filter type:");
        System.out.println(Ansi.ORANGE + "1. " + Ansi.RESET + "Before year");
        System.out.println(Ansi.ORANGE + "2. " + Ansi.RESET + "After year");
        System.out.println(Ansi.ORANGE + "3. " + Ansi.RESET + "Exact year");
        
        System.out.print(Ansi.YELLOW + "Select option (1-3, leave empty to clear year filter): " + Ansi.RESET);
        String selection = scanner.nextLine().trim();

        if (selection.isEmpty()) {
            activeFilters.remove("Year");
            System.out.println("Year filter cleared.");
            return;
        }

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

    /**
     * Filter by genre
     */
    private void filterByGenre() {
        System.out.print(Ansi.YELLOW + "Enter genre (leave empty to clear genre filter): " + Ansi.RESET);
        String genre = scanner.nextLine().trim();

        if (genre.isEmpty()) {
            activeFilters.remove("Genre");
            System.out.println("Genre filter cleared.");
        } else {
            activeFilters.put("Genre", genre.toLowerCase());
            System.out.println("Filtered by genre.");
        }
    }

    /**
     * Filter by availability
     */
    private void filterByAvailability() {
        System.out.println("\nSelect availability type:");
        System.out.println(Ansi.ORANGE + "1. " + Ansi.RESET + "Available");
        System.out.println(Ansi.ORANGE + "2. " + Ansi.RESET + "Unavailable");
        System.out.print(Ansi.YELLOW + "Select option (1-2, leave empty to clear availability filter): " + Ansi.RESET);
        
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

    /**
     * Reapplies all active filters to book list
     */
    private void reapplyAllFilters(ArrayList<Book> books) {
        filteredBooks = new ArrayList<>(books);

        for (Map.Entry<String, String> entry : activeFilters.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            switch (key) {
                case "Author" -> filteredBooks.removeIf(b -> !b.getAuthor().toLowerCase().contains(value));
                case "Year" -> applyYearFilter(value);
                case "Genre" -> applyGenreFilter(value);
                case "Availability" -> applyAvailabilityFilter(value);
            }
        }
    }

    /**
     * Applies year filter logic
     */
    private void applyYearFilter(String value) {
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
            // Skip if invalid
        }
    }

    /**
     * Applies genre filter logic
     */
    private void applyGenreFilter(String value) {
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

    /**
     * Applies availability filter logic
     */
    private void applyAvailabilityFilter(String value) {
        if(value.equals("Available")){
            filteredBooks.removeIf(b -> !b.getAvailable());
        }
        else{
            filteredBooks.removeIf(b -> b.getAvailable());
        }
    }

    //=================================================
    // To retrieve, add, remove, edit books
    //=================================================

    // Get all books
    public ArrayList<Book> getAllBooks() {
        ArrayList<Book> books = fileHandling.readFromFile(BOOKS_FILE, Book.class);
        books.sort((a, b) -> -1 * a.getTitle().compareTo(b.getTitle()));
        return books != null ? books : new ArrayList<>();
    }

    // Add a new book
    public void addBook() {
        System.out.println("=================" + Ansi.BOLD + " Add a Book " + Ansi.RESET + "=================");
        System.out.println(Ansi.RED + "Enter 0 to go back" + Ansi.RESET);
        System.out.println("==============================================");
        
        String title = null;
        String author = null;
        String year = null;
        String description = null;
        ArrayList<String> genres = null;
        
        int index = 0;

        while(true) {
            switch (index) {
                case 0:
                    title = promptInput("Enter book title: ", "Title");
                    if (title == null) return;
                    break;
                case 1:
                    author = promptInput("Enter author name: ", "Author");
                    if (author == null) index -= 2;
                    break;
                case 2:
                    year = promptValidatedYear();
                    if (year == null) index -= 2;
                    break;
                case 3:
                    description = promptInput("Enter book description: ", "Description");
                    if (description == null) index -= 2;
                    break;
                case 4:
                    String genreInput = promptInput("Enter book genre (comma-separated for multiple): ", "Genre");
                    if (genreInput == null){
                        index -= 2; 
                        break;
                    } 

                    genres = parseGenres(genreInput);
                    if (genres.isEmpty()) {
                        System.out.println(Ansi.RED + "At least one valid genre is required!" + Ansi.RESET);
                        return;
                    }
                    break;
                default:
                    // Now all variables are accessible here
                    Book newBook = new Book(title, author, year, description, genres, true);
                    fileHandling.appendToFile(BOOKS_FILE, newBook, Book.class);
                    
                    System.out.println("==============================================");
                    System.out.println(Ansi.GREEN  + newBook + "\nsuccessfully added!");
                    System.out.println("==============================================" + Ansi.RESET);
                    return; // Exit after adding the book
            }
            index++;
        }
    }

    /**
     * Prompts for year with validation
     */
    private String promptValidatedYear() {
        while (true) {
            System.out.print("Enter publication year (1000-2100): ");
            String input = scanner.nextLine().trim();

            if (input.equals("0")) {
                return null;
            }

            if (input.isEmpty()) {
                System.out.println(Ansi.RED + "Year cannot be empty!" + Ansi.RESET);
                continue;
            }

            if (!input.matches("\\d{4}")) {
                System.out.println(Ansi.RED + "Invalid year format. Please enter a 4-digit year." + Ansi.RESET);
                continue;
            }

            int year = Integer.parseInt(input);
            
            if (year < 1000 || year > 2100) {
                System.out.println(Ansi.RED + "Year must be between 1000 and 2100." + Ansi.RESET);
                continue;
            }

            return input;
        }
    }

    /**
     * Parses and validates genre input
     */
    private ArrayList<String> parseGenres(String genreInput) {
        ArrayList<String> genres = new ArrayList<>();
        
        for (String g : genreInput.split(",")) {
            String genre = g.trim();
            
            if (!genre.isEmpty() && genre.matches("[a-zA-Z\\s]+")) {
                genres.add(genre);
            } else if (!genre.isEmpty()) {
                System.out.println(Ansi.YELLOW + "Warning: Skipping invalid genre '" + genre + "' (letters only)" + Ansi.RESET);
            }
        }
        
        return genres;
    }

    /**
     * Prompts for input with validation
     */
    private String promptInput(String prompt, String fieldName) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.equals("0")) {
                return null;
            }

            if (input.isEmpty()) {
                System.out.println(Ansi.RED + fieldName + " cannot be empty!" + Ansi.RESET);
                continue;
            }

            if (fieldName.equals("Author") && !input.matches("[a-zA-Z\\s\\.]+")) {
                System.out.println(Ansi.RED + "Author name can only contain letters, spaces, and periods!" + Ansi.RESET);
                continue;
            }

            if (fieldName.equals("Description") && input.length() < 10) {
                System.out.println(Ansi.RED + "Description must be at least 10 characters long!" + Ansi.RESET);
                continue;
            }

            return input;
        }
    }

    // Remove a book by index 
    public void removeBook() {
        ArrayList<Book> books = getAllBooks();

        if (books == null || books.isEmpty()) {
            System.out.println(Ansi.RED + "No books available." + Ansi.RESET);
            return;
        }

        String input = promptBookIndex("remove");
        if (input == null) return;

        int index = Integer.parseInt(input) - 1;

        if (index < 0 || index >= books.size()) {
            System.out.println(Ansi.RED + "Invalid index. Please select a valid number from the table." + Ansi.RESET);
            return;
        }

        Book selectedBook = books.get(index);

        System.out.println(Ansi.YELLOW + "Selected book: " + Ansi.RESET + selectedBook.getTitle() + " (" + selectedBook.getBookId() + ")");
        
        if (consoleUtil.confirmAction("Are you sure? This action cannot be undone.")) {
            books.remove(index);
            fileHandling.overrideFile(BOOKS_FILE, books);
            System.out.println(Ansi.ORANGE + "Book \"" + selectedBook.getTitle() + "\" (ID: " + selectedBook.getBookId() + ") has been removed." + Ansi.RESET);
            
            // Clear filtered books to force refresh
            filteredBooks.clear();
        } else {
            System.out.println(Ansi.ORANGE + "Cancelled." + Ansi.RESET);
        }

        System.out.println("==============================================");
    }

    // Edit a book's details
    public void editBook() {
        ArrayList<Book> books = getAllBooks();

        if (books == null || books.isEmpty()) {
            System.out.println(Ansi.RED + "No books available." + Ansi.RESET);
            return;
        }

        String input = promptBookIndex("edit");
        if (input == null) return;

        int index = Integer.parseInt(input) - 1;

        if (index < 0 || index >= books.size()) {
            System.out.println(Ansi.RED + "Invalid index. Please select a valid number from the table." + Ansi.RESET);
            return;
        }

        Book book = books.get(index);

        System.out.println(Ansi.ORANGE + "Editing Book: " + book.getTitle() + " (ID: " + book.getBookId() + ")" + Ansi.RESET);

        editBookMenu(book, books);
        
        filteredBooks.clear();
    }

    /**
     * Prompts for book index selection
     */
    private String promptBookIndex(String action) {
        while (true) {
            System.out.println("==============================================\n");
            if(!displayTable(false)){
                return null;
            }
            System.out.println("\n" + Ansi.RED + "Press 'F' to filter again, or '0' to exit" + Ansi.RESET);
            System.out.println("==============================================");
            System.out.print(Ansi.YELLOW + "Enter the book index to " + action + ": " + Ansi.RESET);

            String input = scanner.nextLine().trim();

            if(input.toLowerCase().equals("f")){
                continue;
            }

            if (input.equals("0")) {
                System.out.println(Ansi.ORANGE + "Cancelled." + Ansi.RESET);
                return null;
            }

            if (!input.matches("\\d+")) {
                System.out.println(Ansi.RED + "Invalid input. Please enter a number." + Ansi.RESET);
                continue;
            }

            return input;
        }
    }

    /**
     * Edit book menu
     */
    private void editBookMenu(Book book, ArrayList<Book> books) {
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
                case "1" -> editField(book, "title");
                case "2" -> editField(book, "author");
                case "3" -> editField(book, "year");
                case "4" -> editField(book, "description");
                case "5" -> {
                    fileHandling.overrideFile(BOOKS_FILE, books);
                    System.out.println(Ansi.ORANGE + "Changes saved for \"" + book.getTitle() + "\"." + Ansi.RESET);
                    return;
                }
                case "6", "0" -> {
                    if (consoleUtil.confirmAction("Are you sure you want to exit without saving?")) {
                        System.out.println(Ansi.RED + "Exiting without saving changes." + Ansi.RESET);
                        return;
                    } else {
                        System.out.println(Ansi.ORANGE + "Cancelled." + Ansi.RESET);
                    }
                }
                default -> System.out.println(Ansi.RED + "Invalid choice. Please try again." + Ansi.RESET);
            }
        }
    }

    /**
     * Edits a specific field of a book
     */
    private void editField(Book book, String field) {
        System.out.print("Enter new " + field + ": ");
        String newValue = scanner.nextLine().trim();

        if (newValue.isEmpty()) {
            System.out.println(Ansi.RED + field.substring(0, 1).toUpperCase() + field.substring(1) + " cannot be empty!" + Ansi.RESET);
            return;
        }

        switch (field) {
            case "title" -> book.setTitle(newValue);
            case "author" -> book.setAuthor(newValue);
            case "year" -> book.setYearPublished(newValue);
            case "description" -> book.setDescription(newValue);
        }

        System.out.println(Ansi.ORANGE + field.substring(0, 1).toUpperCase() + field.substring(1) + " updated." + Ansi.RESET);
    }
}