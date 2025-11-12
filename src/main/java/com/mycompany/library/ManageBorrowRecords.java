package com.mycompany.library;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.text.SimpleDateFormat;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import com.mycompany.library.BorrowDetails.BorrowStatus;


public class ManageBorrowRecords {
    
    //================================================
    //Initialize necessary components
    //================================================

    private Scanner scanner = new Scanner(System.in);
    private FileHandling fileHandling = new FileHandling();
    private ManageBooks manageBooks = new ManageBooks();
    private consoleUtil consoleUtils = new consoleUtil();

    private static final String BOOKS_FILE = "books.ser";
    private static final String DATE_FORMAT = "dd/MM/yy";
    private static final double OVERDUE_FEE_PER_DAY = 1.0;

    // HashMap to store bookId to bookTitle mapping
    private HashMap<String, String> bookName = new HashMap<String,String>();
     
    // Instance variables for filter/search state
    private String searchQuery = "";
    private HashMap<String, String> activeFilters = new HashMap<>();
    private ArrayList<BorrowDetails> filteredRecords = new ArrayList<>();

    //===============================================
    //Auth, to retrive current user info
    //===============================================

    private Auth auth;
    public void setAuth(Auth auth){this.auth = auth;}
    public Auth getAuth(){return auth;}

    //===============================================
    //helper methods
    //===============================================

    //sort list by status and due date
    public ArrayList<BorrowDetails> sortList(ArrayList<BorrowDetails> listToSort) {
        listToSort.sort(
            Comparator
                .comparingInt((BorrowDetails b) -> switch (b.getStatus()) {
                    case OVERDUE -> 0;
                    case BORROWED -> 1;
                    case RETURNED -> 2;
                })
                .thenComparing(BorrowDetails::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()))
        );

        return listToSort;
    }

    // Helper method to format dates
    private static String formatDate(Date date) {
        if (date == null) return "";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(DATE_FORMAT);
        return sdf.format(date);
    }

    // Calculate days overdue
    public static long getDaysOverdue(Date dueDate) {
        if (dueDate == null) return 0;

        Date today = new Date();

        // Only calculate if it's actually overdue
        if (today.after(dueDate)) {
            long diffMillis = today.getTime() - dueDate.getTime();
            return TimeUnit.DAYS.convert(diffMillis, TimeUnit.MILLISECONDS);
        }

        return 0;
    }

    //get borrow details by current logged in user
    public ArrayList<BorrowDetails> getBorrowDetailsByUserID() {
        //get all borrow records and current username
        ArrayList<BorrowDetails> records = getAllRecordFromBooks();
        String username = auth.getCurrentUser().getUsername();

        // Filter records by username
        ArrayList<BorrowDetails> userRecords = new ArrayList<>();
        for (BorrowDetails record : records) {
            if (record.getUsername().equals(username)) {
                userRecords.add(record);
            }
        }
        return userRecords;
    }

    public ArrayList<Book> returnBook(ArrayList<Book> books, BorrowDetails selectedRecord) {

        for (Book nextBook : books) {
            if (nextBook.getBookId().equals(selectedRecord.getBookId())) {
                // Mark the book as available again
                nextBook.setAvailable(true);

                ArrayList<BorrowDetails> borrowDetails = nextBook.getBorrowDetails();

                for (BorrowDetails nextRecord : borrowDetails) {
                    if (nextRecord.getBorrowID().equals(selectedRecord.getBorrowID())) {
                        nextRecord.setStatus(BorrowStatus.RETURNED);
                        nextRecord.setDateReturned(new Date());
                    }
                }

                break;
            }
        }

        return books;
    }

    private String formatDateRange() {
        for (var entry : activeFilters.entrySet()) {
            
            if(!entry.getKey().equals("DueRange")){
                System.out.print(Ansi.BLUE + " (" + entry.getKey() + ": " + entry.getValue() + ")" + Ansi.RESET);
            }
            else{
                String[] parts = entry.getValue().split(",");
                String after = parts.length > 0 ? parts[0].trim() : "";
                String before = parts.length > 1 ? parts[1].trim() : "";

                String displayText = (after.isEmpty() ? "" : "After " + after) +
                                    (!after.isEmpty() && !before.isEmpty() ? ", " : "") +
                                    (before.isEmpty() ? "" : "Before " + before);

                return (Ansi.BLUE + " (DueRange: " + displayText + ")" + Ansi.RESET);
            }

        }
        return "";
    }
        
    //===============================================
    //ui methods
    //===============================================


    //reader methods
    public void getUserBorrowDetails() {

        ArrayList<BorrowDetails> records = getBorrowDetailsByUserID();
        records = sortList(records);
        
        consoleUtil.printTable(records, "borrow", auth);

        if(!records.isEmpty()){
            System.out.println(Ansi.ORANGE + "\nPlease approach a libarian to return a book" + Ansi.RESET);
            System.out.println("Books can only be returned by library staff.");
            System.out.println("Book returned overdue will result in a RM " + OVERDUE_FEE_PER_DAY + " charge for each day");
        }

        System.out.println("==============================================");
        System.out.println("Press Enter to continue...");
        scanner.nextLine();

    }
    
    public boolean selectRecord(BorrowDetails selectedRecord) { 
        boolean stayInMenu = true;
        boolean loop = true;

        while (loop) {
            System.out.println("\n==============================================");
            System.out.println(Ansi.BOLD + "Selected record for:" + Ansi.RESET + " " + bookName.get(selectedRecord.getBookId()));
            System.out.println("Date Borrowed: " + formatDate(selectedRecord.getDateBorrowed()));
            System.out.println("Due Date: " + formatDate(selectedRecord.getDueDate()));
            System.out.println("Status: " + selectedRecord.getStatus());      
            System.out.println("==============================================");
            System.out.println(Ansi.ORANGE + "1." + Ansi.RESET + " Return book");
            System.out.println(Ansi.ORANGE + "0." + Ansi.RESET + " Back to Book List");
            System.out.println("==============================================");
            System.out.print(Ansi.YELLOW + "Enter your choice: " + Ansi.RESET);

            String input = scanner.nextLine().trim();
            ArrayList<Book> books = manageBooks.getAllBooks();

            switch (input) {
  
                case "1":
     
                    long daysOverdue = getDaysOverdue(selectedRecord.getDueDate());
                    
                    if(!processReturn(daysOverdue, OVERDUE_FEE_PER_DAY)){
                        System.out.println(Ansi.RED + "Return process cancelled." + Ansi.RESET);
                        break;
                    }

                    returnBook(books, selectedRecord);

                    fileHandling.overrideFile(BOOKS_FILE, books);
                    System.out.println(Ansi.ORANGE + "\nBook has been marked as returned" + (daysOverdue > 0 ? " and payment recorded." : "." + Ansi.RESET));

                    filteredRecords.clear();
                    stayInMenu = false;
                    loop = false;
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

    public boolean processReturn(long daysOverdue, double overdueFee) {
        // Only show fee information if actually overdue
        if (daysOverdue > 0) {
            double totalFee = daysOverdue * overdueFee;
            System.out.println("This book is being returned " + daysOverdue + " days overdue.");
            System.out.printf("The total fee is: RM %.2f%n", totalFee);
            System.out.println("\n================================");

            if (!consoleUtils.confirmAction("Has payment been accepted?")) {
                return false;
            }
        } else {
            // On-time return
            System.out.println("This book is being returned on time.");
            System.out.println("\n================================");
            
            if (!consoleUtils.confirmAction("Confirm return?")) {
                return false;
            }
        }
        return true;
    }

    //general method
    public void getAllBorrowDetails() {

        //load all borrow records
        ArrayList<BorrowDetails> records = getAllRecordFromBooks();

        //check if no records
        if (records.isEmpty()) {
            System.out.println(Ansi.RED + "No books available." + Ansi.RESET);
            return;
        }

        //initialize filter flag
        boolean filter = true;
        filteredRecords = new ArrayList<>();

        while (true) {

            //render filter menu if needed
            if(filter){
                filteredRecords = filterSearchBorrowRecords(records);
                filter = false;
            }

            if (filteredRecords.isEmpty()) {break;}


            //sort filtered records
            filteredRecords = sortList(filteredRecords);

            //print table of filtered records
            consoleUtil.printTable(filteredRecords, "borrow", auth);

            //prompt for next action
            System.out.println("\nEnter a book index to view more options.");
            System.out.println("Press 'F' to filter again, or '0' to exit");
            System.out.println("==============================================");

            String input = scanner.nextLine().trim();
       
            switch (input.toUpperCase()) {
                case "":
                    continue;
                case "0":
                    filteredRecords = new ArrayList<>();
                    break; // Exit the method
                    
                case "F":
                    filter = true;
                    continue; // Go back to filter menu
                    
                default:
                    // Validate numeric input for book selection
                    if (!input.matches("\\d+")) {
                        System.out.println("\n" + Ansi.RED + "Invalid input. Please enter a valid number, 'F' to filter, or '0' to exit." + Ansi.RESET + "\n");
                        continue;
                    }

                    int index = Integer.parseInt(input) - 1;
                    if (index < 0 || index >= filteredRecords.size()) {
                        System.out.println("\n" + Ansi.RED + "Invalid index. Please select a number from the table." + Ansi.RESET + "\n");
                        continue;
                    }

                    // Valid book selected
                    BorrowDetails selectedRecord = filteredRecords.get(index);
                    if (selectedRecord.getStatus() != BorrowStatus.RETURNED) {
                        selectRecord(selectedRecord);       
                    }
                    else{
                        System.out.println("\n" + Ansi.ORANGE + "This book has already been returned." + Ansi.RESET + "\n");
                    }
                    
                    records = getAllRecordFromBooks();
                    break;
            }
        }
        
    }

    


    //===============================================
    //Retrieve all borrow records from all books
    //===============================================

    public ArrayList<BorrowDetails> getAllRecordFromBooks() {
        ArrayList<Book> books = fileHandling.readFromFile(BOOKS_FILE, Book.class);

        if (books == null || books.isEmpty()) {
            return new ArrayList<>();
        }

        ArrayList<BorrowDetails> allDetails = new ArrayList<>();


        for (Book book : books) {

            bookName.put(book.getBookId(), book.getTitle());

            ArrayList<BorrowDetails> details = book.getBorrowDetails();

            if (details != null) {
                for (BorrowDetails detail : details) {
                    Date dueDate = detail.getDueDate();
                    Date today = new Date();

                    if (detail.getStatus() == BorrowStatus.BORROWED) {
                        if (dueDate != null && dueDate.before(today)) {
                            detail.setStatus(BorrowStatus.OVERDUE);
                        }
                    }
                }

                allDetails.addAll(details);
            }
        }

        fileHandling.overrideFile("books.ser", books);

        return allDetails;
    }


    //===============================================
    //Filters and Search
    //===============================================

    public ArrayList<BorrowDetails> filterSearchBorrowRecords(ArrayList<BorrowDetails> records) {

        if(filteredRecords.isEmpty()){
            filteredRecords = new ArrayList<>(records);
        }

        filteredRecords = sortList(filteredRecords);

        while (true) {
            System.out.println("\n=============== Filter & Search ==============");

            // Print results
            consoleUtil.printTable(filteredRecords, "borrow", auth);

            //display number of current results
            System.out.println("\nCurrent results: " + filteredRecords.size() + " record(s).\n");

            //clear filter option
            System.out.println(Ansi.ORANGE + "1." + Ansi.RESET + " Clear filters");
            System.out.println("   -> Reset all filters and search queries.");

            //search option
            System.out.print(Ansi.ORANGE + "2." + Ansi.RESET + " Search records");
            if (!searchQuery.isEmpty()) {System.out.print(Ansi.BLUE + " (Searching: " + searchQuery + ")" + Ansi.RESET);}
            System.out.println("\n   -> Search by title, username, or book ID.");

            //filter option
            System.out.print(Ansi.ORANGE + "3." + Ansi.RESET + " Filter records");
            if (!activeFilters.isEmpty()) {System.out.print(formatDateRange());}
            System.out.println("\n   -> Filter by status or due date range.");

            //go back option
            System.out.print(Ansi.ORANGE + "0." + Ansi.RESET + " Go back");
            System.out.println("\n   -> go back to main menu.\n");
            System.out.print("Select option (1-3) or" + Ansi.ORANGE + " press Enter to continue" + Ansi.RESET + ": ");
            
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    clearFilterAndSearch();
                }

                case "2" -> {
                    setSearchQuery();
                }

                case "3" -> {
                    filterMenu(records);   
                }

                case "0" -> {
                    searchQuery = "";
                    activeFilters.clear();
                    return new ArrayList<>();
                }

                case "" -> {
                    if (filteredRecords.isEmpty()) {
                        System.out.println(Ansi.RED + "No records found." + Ansi.RESET);
                        continue;
                    }
                    return filteredRecords;
                }

                default -> System.out.println(Ansi.RED + "Invalid choice." + Ansi.RESET);
            }

            filteredRecords = applyAllFiltersAndSearch(records);
            
        }
    }

    private ArrayList<BorrowDetails> applyAllFiltersAndSearch(ArrayList<BorrowDetails> records) {
        ArrayList<BorrowDetails> result = new ArrayList<>(records);
        
        if (!searchQuery.isEmpty()) {
            result.removeIf(r -> {
                String title = bookName.get(r.getBookId());
                if (title == null) return true;
                
                return !title.toLowerCase().contains(searchQuery)
                    && !r.getUsername().toLowerCase().contains(searchQuery)
                    && !r.getBookId().toLowerCase().contains(searchQuery);
            });
        }
        
        for (var entry : activeFilters.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            switch (key) {
                case "Status" -> {
                    result.removeIf(r -> !r.getStatus().toString().equals(value));
                }
                
                case "DueRange" -> {
                    try {
                        String[] parts = value.trim().split(",", -1);
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy");

                        final LocalDate startDate = (parts.length > 0 && !parts[0].isBlank())
                            ? LocalDate.parse(parts[0].trim(), formatter)
                            : null;

                        final LocalDate endDate = (parts.length > 1 && !parts[1].isBlank())
                            ? LocalDate.parse(parts[1].trim(), formatter)
                            : null;

                        result.removeIf(r -> {
                            if (r.getDueDate() == null) return true;

                            LocalDate due = r.getDueDate()
                                .toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();

                            if (startDate != null && due.isBefore(startDate)) {
                                return true;
                            }

                            if (endDate != null && due.isAfter(endDate)) {
                                return true;
                            }

                            return false;
                        });

                    } catch (DateTimeParseException e) {
                        System.out.println(Ansi.RED + "Error parsing date range filter. Expected format: dd/MM/yy[,dd/MM/yy]" + Ansi.RESET);
                    } catch (Exception e) {
                        System.out.println(Ansi.RED + "Error applying due date range filter: " + e.getMessage() + Ansi.RESET);
                    }
                }
            }
        }
        
        result = sortList(result);
        return result;
    }

    private void clearFilterAndSearch() {
        filteredRecords = new ArrayList<>();
        searchQuery = "";
        activeFilters.clear();
        System.out.println(Ansi.GREEN + "Filters cleared." + Ansi.RESET);
    }

    private void setSearchQuery() {
        System.out.print("Enter search query (title, username, book ID) or leave empty to clear: ");
        String query = scanner.nextLine().trim().toLowerCase();

        if (query.isEmpty()) {
            searchQuery = "";
            System.out.println(Ansi.GREEN + "Search cleared." + Ansi.RESET);
        } else {
            searchQuery = query;
            System.out.println(Ansi.GREEN + "Search applied: " + searchQuery + Ansi.RESET);
        }
    }

    private void filterMenu(ArrayList<BorrowDetails> records) {
        System.out.println("\nFilter by:");
        System.out.println(Ansi.ORANGE + "1. " + Ansi.RESET + "Status");
        System.out.println(Ansi.ORANGE + "2. " + Ansi.RESET + "Due Date Range");

        System.out.print("Choose filter type: ");
        String filterChoice = scanner.nextLine().trim();

        switch (filterChoice) {
            case "1" -> {
                BorrowStatus status = null;

                while (true) {
                    System.out.println("\nStatus type:");
                    System.out.println(Ansi.ORANGE + "1. " + Ansi.RESET + "Borrowed");
                    System.out.println(Ansi.ORANGE + "2. " + Ansi.RESET + "Returned");
                    System.out.println(Ansi.ORANGE + "3. " + Ansi.RESET + "Overdue");
                    System.out.println(Ansi.ORANGE + "0. " + Ansi.RESET + "Clear status filter");
                    
                    System.out.print("Enter status: ");
                    String statusChoice = scanner.nextLine().trim();

                    switch (statusChoice) {
                        case "1":
                            status = BorrowStatus.BORROWED;
                            break;

                        case "2":
                            status = BorrowStatus.RETURNED;
                            break;

                        case "3":
                            status = BorrowStatus.OVERDUE;
                            break;

                        case "0":
                            activeFilters.remove("Status");
                            System.out.println(Ansi.GREEN + "Status filter cleared." + Ansi.RESET);
                            break;
                    
                        default:
                            System.out.println(Ansi.ORANGE + "Status filter not applied" + Ansi.RESET);
                            break;
                    }

                    break;
                }
                
                if (status != null) {
                    activeFilters.put("Status", status.toString());
                    System.out.println(Ansi.GREEN + "Status filter applied: " + status + Ansi.RESET);
                }
            }

            case "2" -> {
                while (true) {
                    try {
                        System.out.print("Enter start due date (dd/MM/yy, empty to skip): ");
                        String start = scanner.nextLine().trim();
                        System.out.print("Enter end due date (dd/MM/yy, empty to skip): ");
                        String end = scanner.nextLine().trim();

                        // If both empty, remove filter and break
                        if (start.isEmpty() && end.isEmpty()) {
                            activeFilters.remove("DueRange");
                            System.out.println(Ansi.GREEN + "Due date range filter removed." + Ansi.RESET);
                            break;
                        }

                        // Validate date format for non-empty inputs
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
                        sdf.setLenient(false);
                        
                        Date startDate = null;
                        Date endDate = null;
                        
                        if (!start.isEmpty()) {
                            // Check length before parsing
                            if (start.length() != 8) {
                                throw new ParseException("Invalid date format", 0);
                            }
                            startDate = sdf.parse(start);
                        }
                        
                        if (!end.isEmpty()) {
                            // Check length before parsing
                            if (end.length() != 8) {
                                throw new ParseException("Invalid date format", 0);
                            }
                            endDate = sdf.parse(end);
                        }
                        
                        // Check if start date is before end date (if both provided)
                        if (startDate != null && endDate != null && startDate.after(endDate)) {
                            System.out.println(Ansi.RED + "Start date must be before or equal to end date." + Ansi.RESET);
                            continue;
                        }
                        
                        // Valid input, save and break
                        activeFilters.put("DueRange", start + "," + end);
                        
                        // Better feedback message
                        if (!start.isEmpty() && !end.isEmpty()) {
                            System.out.println(Ansi.GREEN + "Due date range filter applied: " + start + " to " + end + Ansi.RESET);
                        } else if (!start.isEmpty()) {
                            System.out.println(Ansi.GREEN + "Due date filter applied: from " + start + " onwards" + Ansi.RESET);
                        } else {
                            System.out.println(Ansi.GREEN + "Due date filter applied: up to " + end + Ansi.RESET);
                        }
                        break;
                        
                    } catch (ParseException e) {
                        System.out.println(Ansi.RED + "Invalid date format. Please use dd/MM/yy (e.g., 01/12/25)" + Ansi.RESET);
                    } catch (Exception e) {
                        System.out.println(Ansi.RED + "An error occurred. Please try again." + Ansi.RESET);
                    }
                }
            }

            default -> System.out.println(Ansi.RED + "Invalid option." + Ansi.RESET);
        }
        
    }
    

}
