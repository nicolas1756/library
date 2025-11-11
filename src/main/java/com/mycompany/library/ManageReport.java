package com.mycompany.library;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Scanner;

import com.mycompany.library.BorrowDetails.BorrowStatus;

public class ManageReport {
    private Auth auth;

    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    public Auth getAuth() {
        return auth;
    }

    public Report getValues() {
        FileHandling fileHandling = new FileHandling();
        ManageBorrowRecords manageBorrowRecords = new ManageBorrowRecords();
        ManageBooks manageBooks = new ManageBooks();
        
        ArrayList<Book> books = manageBooks.getAllBooks();
        ArrayList<BorrowDetails> borrowRecords = manageBorrowRecords.getAllRecordFromBooks();
        ArrayList<User> accounts = fileHandling.readFromFile("accounts.ser", User.class);

        int totalBooks = books.size();
        int totalUsers = accounts.size();
        int totalBorrows = borrowRecords.size();
        int borrowed = 0;
        int returned = 0;
        int overdue = 0;

        for (BorrowDetails record : borrowRecords) {
            if (record.getStatus() == BorrowStatus.BORROWED) {
                borrowed++;
            } else if (record.getStatus() == BorrowStatus.RETURNED) {
                returned++;
            } else if (record.getStatus() == BorrowStatus.OVERDUE) {
                overdue++;
            }
        }

        Report report = new Report(totalBooks, totalUsers, totalBorrows, borrowed, returned, overdue);
        return report;

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

        consoleUtil.printTable(reportList, "report", auth);

        showMostBorrowedBooks();
        
        System.out.println("==============================================");
        System.out.println("Press Enter to continue...");
        scanner.nextLine();

    }

    private void showMostBorrowedBooks() {
        ManageBooks manageBooks = new ManageBooks();
        ManageBorrowRecords manageBorrowRecords = new ManageBorrowRecords();
        
        ArrayList<Book> allBooks = manageBooks.getAllBooks();
        ArrayList<BorrowDetails> allRecords = manageBorrowRecords.getAllRecordFromBooks();
        
        Map<String, Integer> borrowCount = new HashMap<>();
        for (BorrowDetails record : allRecords) {
            borrowCount.put(record.getBookId(), borrowCount.getOrDefault(record.getBookId(), 0) + 1);
        }
        
        // Get top 5 most borrowed books
        ArrayList<Book> topBooks = allBooks.stream()
            .filter(book -> borrowCount.containsKey(book.getBookId()))
            .sorted((b1, b2) -> borrowCount.get(b2.getBookId()).compareTo(borrowCount.get(b1.getBookId())))
            .limit(5)
            .collect(Collectors.toCollection(ArrayList::new));
        
        if (!topBooks.isEmpty()) {
            System.out.println("\nTop 5 Most Borrowed Books:");
            System.out.println("==============================================");
            
            for (int i = 0; i < topBooks.size(); i++) {
                Book book = topBooks.get(i);
                int count = borrowCount.get(book.getBookId());
                System.out.printf("%d. %s by %s - %d borrows\n", 
                    (i + 1), 
                    consoleUtil.truncateString(book.getTitle(), 30), 
                    book.getAuthor(), 
                    count);
            }
            System.out.println();
        }
    }
}
