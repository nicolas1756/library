package com.mycompany.library;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Scanner;

import com.mycompany.library.BorrowDetails.BorrowStatus;

public class ManageReport {
    
    //================================================
    // constants
    //================================================
    private static final String ACCOUNTS_FILE = "accounts.ser";
    private static final int TOP_BOOKS_LIMIT = 5;
    private static final int TITLE_MAX_LENGTH = 30;
    
    //================================================
    // instance fields
    //================================================
    private final FileHandling fileHandling = new FileHandling();
    private final ManageBorrowRecords manageBorrowRecords = new ManageBorrowRecords();
    private final ManageBooks manageBooks = new ManageBooks();
    private final consoleUtil consoleUtil = new consoleUtil();
    
    private Auth auth;

    //================================================
    // auth
    //================================================
    
    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    public Auth getAuth() {
        return auth;
    }


    //================================================
    // report generation
    //================================================
    

    public Report getValues() {
        ArrayList<Book> books = manageBooks.getAllBooks();
        ArrayList<BorrowDetails> borrowRecords = manageBorrowRecords.getAllRecordFromBooks();
        ArrayList<User> accounts = fileHandling.readFromFile(ACCOUNTS_FILE, User.class);

        int totalBooks = books.size();
        int totalUsers = accounts != null ? accounts.size() : 0;
        int totalBorrows = borrowRecords.size();

        // Use streams for counting
        long borrowed = borrowRecords.stream()
            .filter(r -> r.getStatus() == BorrowStatus.BORROWED)
            .count();
        
        long returned = borrowRecords.stream()
            .filter(r -> r.getStatus() == BorrowStatus.RETURNED)
            .count();
        
        long overdue = borrowRecords.stream()
            .filter(r -> r.getStatus() == BorrowStatus.OVERDUE)
            .count();

        return new Report(totalBooks, totalUsers, totalBorrows, 
                         (int) borrowed, (int) returned, (int) overdue);
    }

    public void generateReport() {
        Scanner scanner = new Scanner(System.in);
        consoleUtil consoleUtil = new consoleUtil();
        ArrayList<Report> reportList = new ArrayList<>(); 
        Report report = getValues();

        reportList.add(report);
        
        System.out.println("\n==============================================");
        System.out.println("               Library Report                 ");
        System.out.println("==============================================");
        
        System.out.println("\nDate: " + java.time.LocalDate.now() + "\n");

        //print report table
        consoleUtil.printTable(reportList, "report", auth);

        //show list of most borrowed books
        showMostBorrowedBooks();
        
        System.out.println("==============================================");
        System.out.println("Press Enter to continue...");
        scanner.nextLine();

    }

    private void showMostBorrowedBooks() {
        ArrayList<Book> allBooks = manageBooks.getAllBooks();
        ArrayList<BorrowDetails> allRecords = manageBorrowRecords.getAllRecordFromBooks();
        
        // Count borrows per book
        Map<String, Integer> borrowCountByBookId = countBorrowsByBook(allRecords);
        
        // Get top borrowed books
        ArrayList<Book> topBooks = getTopBorrowedBooks(allBooks, borrowCountByBookId, TOP_BOOKS_LIMIT);
        
        if (!topBooks.isEmpty()) {
            displayTopBooks(topBooks, borrowCountByBookId);
        }
    }

    private Map<String, Integer> countBorrowsByBook(ArrayList<BorrowDetails> records) {
        Map<String, Integer> borrowCount = new HashMap<>();
        for (BorrowDetails record : records) {
            borrowCount.put(record.getBookId(), 
                           borrowCount.getOrDefault(record.getBookId(), 0) + 1);
        }
        return borrowCount;
    }


    private ArrayList<Book> getTopBorrowedBooks(ArrayList<Book> books, 
        Map<String, Integer> borrowCount, int limit) {
        return books.stream()
            .filter(book -> borrowCount.containsKey(book.getBookId()))
            .sorted((b1, b2) -> borrowCount.get(b2.getBookId())
                                          .compareTo(borrowCount.get(b1.getBookId())))
            .limit(limit)
            .collect(Collectors.toCollection(ArrayList::new));
    }


    private void displayTopBooks(ArrayList<Book> topBooks, Map<String, Integer> borrowCount) {
        System.out.println("\nTop " + TOP_BOOKS_LIMIT + " Most Borrowed Books:");
        System.out.println("==============================================");
        
        for (int i = 0; i < topBooks.size(); i++) {
            Book book = topBooks.get(i);
            int count = borrowCount.get(book.getBookId());
            System.out.printf("%d. %s by %s - %d borrows%n", 
                (i + 1), 
                consoleUtil.truncateString(book.getTitle(), TITLE_MAX_LENGTH), 
                book.getAuthor(), 
                count);
        }
        System.out.println();
    }
}
