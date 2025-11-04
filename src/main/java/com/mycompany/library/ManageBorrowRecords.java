package com.mycompany.library;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ManageBorrowRecords {
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

    public void getBorrowDetailsByUserID(){

    }

    
    public ArrayList<BorrowDetails> getAllBorrowDetails() {
        ArrayList<Book> books = getAllBooks();
        ArrayList<BorrowDetails> allDetails = new ArrayList<>();

        for (Book book : books) {
            ArrayList<BorrowDetails> details = book.getBorrowDetails();
            if (details != null) {
                allDetails.addAll(details);
            }
        }

        printTable(allDetails, EnumSet.allOf(Column.class));
        return allDetails;
    }


    public ArrayList<Book> getAllBooks() {
        ArrayList<Book> books = fileHandling.readFromFile("books.ser", Book.class);
        return books != null ? books : new ArrayList<>();
    }

    //table methods

    public enum Column {
        INDEX, ID, USERNAME, BOOKID, DATEBORROWED, DUEDATE, DATERETURNED, STATUS
    }

    public static void printTable(ArrayList<BorrowDetails> records, EnumSet<Column> columns) {

        if (records == null || records.isEmpty()) {
            System.out.println("\u001B[31mNo records available.\u001B[0m");
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
                    case BOOKID -> record.getBookId();
                    case DATEBORROWED -> formatDate(record.getDateBorrowed());
                    case DUEDATE -> formatDate(record.getDueDate());
                    case DATERETURNED -> formatDate(record.getDateReturned());
                    case STATUS -> String.valueOf(record.getStatus());
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
                int width = colWidths.get(col);
                String value = switch (col) {
                    case INDEX -> String.valueOf(i);
                    case ID -> String.valueOf(record.getBorrowID());
                    case USERNAME -> record.getUsername();
                    case BOOKID -> record.getBookId();
                    case DATEBORROWED -> formatDate(record.getDateBorrowed());
                    case DUEDATE -> formatDate(record.getDueDate());
                    case DATERETURNED -> formatDate(record.getDateReturned());
                    case STATUS -> String.valueOf(record.getStatus());
                };
                row.append(String.format("%-" + (width - 1) + "s" + VSEP + " ", value == null ? "" : value));
            }
            System.out.println(row);
            i++;
        }

        System.out.println(bottom);
    }


    // Helper method to format dates
    private static String formatDate(Date date) {
        if (date == null) return "Pending";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yy");
        return sdf.format(date);
    }

    private static String formatDateTime(Date date) {
        if (date == null) return "";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yy HH:mm");
        return sdf.format(date);
    }

}
