package com.mycompany.library;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import com.mycompany.library.BorrowDetails.BorrowStatus;

public class ManageBorrowRecords {
// Initialize necessary components
    Scanner scanner = new Scanner(System.in);
    FileHandling fileHandling = new FileHandling();
    ManageBooks manageBooks = new ManageBooks();
    consoleUtil ConsoleUtils = new consoleUtil();
    
    static HashMap<String, String> bookName = new HashMap<String,String>();
    
    private Auth auth;

    Double overdueFee = 1.0;

    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    public Auth getAuth() {
        return auth;
    }

    public void getBorrowDetailsByUserID(){
        ArrayList<BorrowDetails> records = getAllRecordFromBooks();
        String username = auth.getCurrentUser().getUsername();

        records.sort(
            Comparator
                .comparingInt((BorrowDetails b) -> switch (b.getStatus()) {
                    case OVERDUE -> 0;
                    case BORROWED -> 1;
                    case RETURNED -> 2;
                })
                .thenComparing(BorrowDetails::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()))
        );


        Iterator<BorrowDetails> record = records.iterator();  
        while (record.hasNext()) {
            BorrowDetails nextRecord = record.next();

            if(!nextRecord.getUsername().equals(username)){
                record.remove();
            }
        }

        printTable(records, EnumSet.of(Column.INDEX, Column.TITLE, Column.DATEBORROWED, Column.DUEDATE, Column.DATERETURNED, Column.STATUS));

        if(!records.isEmpty()){
            System.out.println(Ansi.ORANGE + "\nPlease approach a libarian to return a book" + Ansi.RESET);
            System.out.println("Books can only be returned by library staff.");
            System.out.println("Book returned overdue will result in a RM 1 charge for each day");
        }

        System.out.println("==============================================");
        System.out.println("Press Enter to continue...");
        scanner.nextLine();

    }

    
    public void getAllBorrowDetails() {
        ArrayList<BorrowDetails> records = getAllRecordFromBooks();


        if (records.isEmpty()) {
            System.out.println(Ansi.RED + "No books available." + Ansi.RESET);
            return;
        }

        boolean filter = true;

        while (true) {

            if(filter){
                ArrayList<BorrowDetails> filteredRecords = filterSearchBorrowRecords(records);
                filter = false;
            }
            



            if (filteredRecords.isEmpty()) {
                break;
            }

            filteredRecords.sort(
                Comparator
                    .comparingInt((BorrowDetails b) -> switch (b.getStatus()) {
                        case OVERDUE -> 0;
                        case BORROWED -> 1;
                        case RETURNED -> 2;
                    })
                    .thenComparing(BorrowDetails::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()))
            );

            printTable(filteredRecords, EnumSet.of(
                    Column.INDEX, Column.USERNAME, Column.TITLE,
                    Column.BOOKID, Column.DATEBORROWED,
                    Column.DUEDATE, Column.DATERETURNED, Column.STATUS
            ));

            System.out.println("\nEnter a book index to view more options.");
            System.out.println("Press 'F' to filter again, or '0' to exit");
            System.out.println("==============================================");

            String input = scanner.nextLine().trim();

            // Handle different input types
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
                        System.out.println(Ansi.RED + "Invalid input. Please enter a valid number, 'F' to filter, or '0' to exit." + Ansi.RESET);
                        continue;
                    }

                    int index = Integer.parseInt(input) - 1;
                    if (index < 0 || index >= filteredRecords.size()) {
                        System.out.println(Ansi.RED + "Invalid index. Please select a number from the table." + Ansi.RESET);
                        continue;
                    }

                    // Valid book selected
                    BorrowDetails selectedRecord = filteredRecords.get(index);
                    selectRecord(selectedRecord);
                    records = getAllRecordFromBooks();
                    break;
            }
        }
        
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

                    if (selectedRecord.getStatus() == BorrowStatus.OVERDUE || selectedRecord.getStatus() == BorrowStatus.BORROWED) {
                        long daysOverdue = getDaysOverdue(selectedRecord.getDueDate());
                        
                        // Only show fee information if actually overdue
                        if (daysOverdue > 0) {
                            double totalFee = daysOverdue * overdueFee;
                            System.out.println("This book is being returned " + daysOverdue + " days overdue.");
                            System.out.printf("The total fee is: RM %.2f%n", totalFee);
                            System.out.println("\n================================");

                            if (!consoleUtil.confirmAction("Has payment been accepted?")) {
                                break; // Exit if payment not confirmed
                            }
                        } else {
                            // On-time return
                            System.out.println("This book is being returned on time.");
                            System.out.println("\n================================");
                            
                            if (!consoleUtil.confirmAction("Confirm return?")) {
                                break;
                            }
                        }

                        // Process the return (same for both overdue and on-time)
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

                        fileHandling.overrideFile("books.ser", books);
                        System.out.println(Ansi.ORANGE + "\nBook has been marked as returned" + (daysOverdue > 0 ? " and payment recorded." : "." + Ansi.RESET));
                    }

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

    public ArrayList<BorrowDetails> getAllRecordFromBooks() {
        ArrayList<Book> books = fileHandling.readFromFile("books.ser", Book.class);

        if (books == null || books.isEmpty()) {
            return new ArrayList<>();
        }

        ArrayList<BorrowDetails> allDetails = new ArrayList<>();
        Iterator<Book> iterator = books.iterator();

        while (iterator.hasNext()) {
            Book book = iterator.next();
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



    //table methods

    public enum Column {
        INDEX, ID, USERNAME, TITLE, BOOKID, DATEBORROWED, DUEDATE, DATERETURNED, STATUS
    }

    public void printTable(ArrayList<BorrowDetails> records, EnumSet<Column> columns) {

        System.out.print("\n");

        if (records == null || records.isEmpty()) {
            System.out.println(Ansi.RED + "No records available." + Ansi.RESET);
            return;
        }

        boolean supportsUnicode = consoleUtil.detectUnicodeSupport();

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

        // Column headers
        Map<Column, String> headers = Map.of(
            Column.INDEX, "Index",
            Column.ID, "Record ID",
            Column.USERNAME, "Username",
            Column.TITLE, "Title",
            Column.BOOKID, "Book ID",
            Column.DATEBORROWED, "Date Borrowed",
            Column.DUEDATE, "Due Date",
            Column.DATERETURNED, "Date Returned",
            Column.STATUS, "Status"
        );

        // Calculate widths dynamically
        Map<Column, Integer> colWidths = new HashMap<>();
        for (Column col : columns) {
            int max = headers.get(col).length();
            for (BorrowDetails record : records) {
                String value = switch (col) {
                    case INDEX -> String.valueOf(records.indexOf(record) + 1);
                    case ID -> String.valueOf(record.getBorrowID());
                    case USERNAME -> record.getUsername();
                    case TITLE -> bookName.get(record.getBookId());
                    case BOOKID -> record.getBookId();
                    case DATEBORROWED -> formatDate(record.getDateBorrowed());
                    case DUEDATE -> formatDate(record.getDueDate());
                    case DATERETURNED -> formatDate(record.getDateReturned());
                    case STATUS -> String.valueOf(record.getStatus() + " ");
                };
                max = Math.max(max, value == null ? 0 : value.length());
            }
            colWidths.put(col, max + 2);
        }

        // Borders and header
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

        // Fix end borders
        top.setCharAt(top.length() - 1, TR.charAt(0));
        sep.setCharAt(sep.length() - 1, supportsUnicode ? '╣' : '+');
        bottom.setCharAt(bottom.length() - 1, BR.charAt(0));

        // Print table
        System.out.println(top);
        System.out.println(header);
        System.out.println(sep);

        int i = 1;
        for (BorrowDetails record : records) {
            StringBuilder row = new StringBuilder(VSEP + " ");
            for (Column col : columns) {

                String status;

                if(record.getStatus() == BorrowStatus.BORROWED && record.getUsername().equals(auth.getCurrentUser().getUsername())){
                    status = String.valueOf(record.getStatus()) + "*";
                }
                else{
                    status = String.valueOf(record.getStatus());
                }

                int width = colWidths.get(col);
                String value = switch (col) {
                    case INDEX -> String.valueOf(i);
                    case ID -> String.valueOf(record.getBorrowID());
                    case USERNAME -> record.getUsername();
                    case TITLE -> consoleUtil.truncateString(bookName.get(record.getBookId()), 30);
                    case BOOKID -> record.getBookId();
                    case DATEBORROWED -> formatDate(record.getDateBorrowed());
                    case DUEDATE -> formatDate(record.getDueDate());
                    case DATERETURNED -> record.getDateReturned() == null ? "-" : formatDate(record.getDateReturned());
                    case STATUS -> String.valueOf(status);
                };

                String displayValue = value == null ? "" : value;
                String paddedValue = String.format("%-" + (width - 1) + "s", displayValue);
                
                // Only color the STATUS column based on status
                if (col == Column.STATUS) {
                    String color = switch (record.getStatus()) {
                        case OVERDUE -> Ansi.ORANGE;
                        case BORROWED -> Ansi.YELLOW;
                        case RETURNED -> Ansi.GREEN;
                        default -> "";
                    };
                    paddedValue = color + paddedValue + Ansi.RESET;
                }
                
                row.append(paddedValue + VSEP + " ");
            }
            System.out.println(row);
            i++;
        }

        System.out.println(bottom);

        if(!auth.isAdmin()){
            System.out.println("\n* Already Borrowing");
        }
    }

    String searchQuery = "";
    HashMap<String, String> activeFilters = new HashMap<>();
    ArrayList<BorrowDetails> filteredRecords = new ArrayList<>();

    public ArrayList<BorrowDetails> filterSearchBorrowRecords(ArrayList<BorrowDetails> records) {

        if(filteredRecords.isEmpty()){
            filteredRecords = new ArrayList<>(records);
        }

        filteredRecords.sort(
            Comparator
                .comparingInt((BorrowDetails b) -> switch (b.getStatus()) {
                    case OVERDUE -> 0;
                    case BORROWED -> 1;
                    case RETURNED -> 2;
                })
                .thenComparing(BorrowDetails::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()))
        );

        while (true) {
            System.out.println("\n=============== Filter & Search ==============");

            // Print results
            printTable(filteredRecords, EnumSet.of(
                    Column.INDEX, Column.USERNAME, Column.TITLE,
                    Column.BOOKID, Column.DATEBORROWED,
                    Column.DUEDATE, Column.DATERETURNED, Column.STATUS
            ));

            System.out.println("\nCurrent results: " + filteredRecords.size() + " record(s).\n");

            System.out.println(Ansi.ORANGE + "1." + Ansi.RESET + " Clear filters");
            System.out.println("   -> Reset all filters and search queries.");

            System.out.print(Ansi.ORANGE + "2." + Ansi.RESET + " Search records");
            if (!searchQuery.isEmpty()) {
                System.out.print(Ansi.BLUE + " (Searching: " + searchQuery + ")" + Ansi.RESET);
            }
            System.out.println("\n   -> Search by title, username, or book ID.");

            System.out.print(Ansi.ORANGE + "3." + Ansi.RESET + " Filter records");
            if (!activeFilters.isEmpty()) {
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

                        System.out.print(Ansi.BLUE + " (DueRange: " + displayText + ")" + Ansi.RESET);
                    }

                }
            }
            System.out.println("\n   -> Filter by status or due date range.");

            System.out.print(Ansi.ORANGE + "0." + Ansi.RESET + " Go back");
            System.out.println("\n   -> go back to main menu.\n");

            System.out.print("Select option (1-3) or" + Ansi.ORANGE + " press Enter to continue" + Ansi.RESET + ": ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    filteredRecords = new ArrayList<>(records);
                    searchQuery = "";
                    activeFilters.clear();
                    System.out.println(Ansi.GREEN + "Filters cleared." + Ansi.RESET);
                }

                case "2" -> {
                    System.out.print("\nEnter search keyword (title/username/book ID): ");
                    searchQuery = scanner.nextLine().trim().toLowerCase();

                    // Reapply all filters with new search
                    filteredRecords = applyAllFilters(records);

                    if (filteredRecords.isEmpty()) {
                        System.out.println(Ansi.YELLOW + "No results found for: " + searchQuery + Ansi.RESET);
                    } else {
                        System.out.println(Ansi.GREEN + filteredRecords.size() + " result(s) found." + Ansi.RESET);
                    }
                }

                case "3" -> {
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
                                        startDate = sdf.parse(start);
                                    }
                                    
                                    if (!end.isEmpty()) {
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
                                    System.out.println(Ansi.RED + "Invalid date format. Please use dd/MM/yy (e.g., 25/12/24)" + Ansi.RESET);
                                } catch (Exception e) {
                                    System.out.println(Ansi.RED + "An error occurred. Please try again." + Ansi.RESET);
                                }
                            }
                        }

                        default -> System.out.println(Ansi.RED + "Invalid option." + Ansi.RESET);
                    }

                    // === Reapply ALL filters (including search) ===
                    filteredRecords = applyAllFilters(records);
                    
                    // Sort nicely after filtering
                    filteredRecords.sort(
                        Comparator
                            .comparingInt((BorrowDetails b) -> switch (b.getStatus()) {
                                case OVERDUE -> 0;
                                case BORROWED -> 1;
                                case RETURNED -> 2;
                                default -> 3;
                            })
                            .thenComparing(BorrowDetails::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()))
                    );
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
            
        }
    }

    private ArrayList<BorrowDetails> applyAllFilters(ArrayList<BorrowDetails> records) {
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
        
        return result;
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

}
