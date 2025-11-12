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
        
        System.out.println("==============================================\n");
        handleReportSaving();

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


    public void handleReportSaving(){
        if(consoleUtil.confirmAction("Do you want to save the report to a file? (y/n): ")){
            String fileName = "report_" + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";
            saveReportToTextFile(fileName);
        } else {
            System.out.println("Report not saved.");
        }
    }

    private void saveReportToTextFile(String fileName) {
        String filePath = "Reports/" + fileName;
        try (java.io.FileWriter writer = new java.io.FileWriter(filePath);
            java.io.BufferedWriter bw = new java.io.BufferedWriter(writer)) {
            
            // Header
            bw.write("==============================================\n");
            bw.write("               Library Report                 \n");
            bw.write("==============================================\n\n");
            bw.write("Date: " + java.time.LocalDate.now() + "\n");
            bw.write("Time: " + java.time.LocalTime.now().withNano(0) + "\n\n");    
            
            Report currentReport = getValues();

            // Statistics
            bw.write(String.format("Total Books:        %d\n", currentReport.getTotalBooks()));
            bw.write(String.format("Total Users:        %d\n", currentReport.getTotalUsers()));
            bw.write(String.format("Total Borrows:      %d\n", currentReport.getTotalBorrows()));
            bw.write(String.format("Currently Borrowed: %d\n", currentReport.getBorrowed()));
            bw.write(String.format("Returned Books:     %d\n", currentReport.getReturned()));
            bw.write(String.format("Overdue Books:      %d\n\n", currentReport.getOverdue()));
            
            // Top borrowed books
            ArrayList<Book> allBooks = manageBooks.getAllBooks();
            ArrayList<BorrowDetails> allRecords = manageBorrowRecords.getAllRecordFromBooks();
            Map<String, Integer> borrowCount = countBorrowsByBook(allRecords);
            ArrayList<Book> topBooks = getTopBorrowedBooks(allBooks, borrowCount, TOP_BOOKS_LIMIT);
            
            if (!topBooks.isEmpty()) {
                bw.write("Top " + TOP_BOOKS_LIMIT + " Most Borrowed Books:\n");
                bw.write("==============================================\n");
                for (int i = 0; i < topBooks.size(); i++) {
                    Book book = topBooks.get(i);
                    int count = borrowCount.get(book.getBookId());
                    bw.write(String.format("%d. %s by %s - %d borrows\n",
                        (i + 1),
                        consoleUtil.truncateString(book.getTitle(), TITLE_MAX_LENGTH),
                        book.getAuthor(),
                        count));
                }
            }
            
            bw.write("==============================================\n");
            System.out.println("Report saved successfully to " + fileName + " in Reports folder.");
            
        } catch (java.io.IOException e) {
            System.err.println("Error saving report: " + e.getMessage());
        }
    }
}
