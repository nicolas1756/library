package com.mycompany.library;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author nic
 */

public class FileHandling {


    public <T extends Serializable> boolean overrideFile(String path, ArrayList<T> value) {

        ArrayList<T> dataList = value;
        path = "Data/" + path;

        // Write updated list safely
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(path))) {
            outputStream.writeObject(dataList);
            return true;
        } catch (IOException e) {
            System.out.println(Ansi.RED + "Error writing to file: " + e.getMessage() + Ansi.RESET);
            return false;
        }
    }

    public <T extends Serializable> boolean appendToFile(String path, T value, Class<T> type) {

        ArrayList<T> dataList = readFromFile(path, type);
        path = "Data/" + path;
        
        // Add new object
        dataList.add(value);

        // Write updated list safely
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(path))) {
            outputStream.writeObject(dataList);
            return true;
        } catch (IOException e) {
            System.out.println(Ansi.RED + "Error writing to file: " + e.getMessage() + Ansi.RESET);
            return false;
        }
    }


    public <T extends Serializable> ArrayList<T> readFromFile(String path, Class<T> type) {
        path = "Data/" + path;
        File file = new File(path);
        ArrayList<T> dataList = new ArrayList<>();

        // Create empty file if missing
        if (!file.exists()) {
            try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(path))) {
                outputStream.writeObject(dataList);
            } catch (IOException e) {
                System.out.println(Ansi.RED + "Error creating new file: " + e.getMessage() + Ansi.RESET);
            }
            return dataList;
        }

        // Attempt to read
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(path))) {
            Object obj = inputStream.readObject();

            if (obj instanceof ArrayList<?>) {
                ArrayList<?> rawList = (ArrayList<?>) obj;

                for (Object item : rawList) {
                    if (!type.isInstance(item)) {
                        System.out.println(Ansi.RED + "Warning: File contains unexpected type: " + item.getClass().getName() + Ansi.RESET);
                        return new ArrayList<>(); // return empty if mismatch
                    }
                }

                dataList = (ArrayList<T>) rawList;
            } else {
                System.out.println(Ansi.RED + "Warning: File does not contain an ArrayList." + Ansi.RESET);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(Ansi.RED + "Failed to read file: " + e.getMessage() + Ansi.RESET);
        }

        return dataList;
    }

    //==============================================
    //report handling methods
    //==============================================

    public void saveReportToTextFile(String fileName, Report currentReport, ArrayList<Book> topBooks, Map<String, Integer> borrowCount, int TITLE_MAX_LENGTH, int TOP_BOOKS_LIMIT) {
        String filePath = "Reports/" + fileName;
        try (java.io.FileWriter writer = new java.io.FileWriter(filePath);
            java.io.BufferedWriter bw = new java.io.BufferedWriter(writer)) {
            
            // Header
            bw.write("==============================================\n");
            bw.write("               Library Report                 \n");
            bw.write("==============================================\n\n");
            bw.write("Date: " + java.time.LocalDate.now() + "\n");
            bw.write("Time: " + java.time.LocalTime.now().withNano(0) + "\n\n");    

            // Statistics
            bw.write(String.format("Total Books:        %d\n", currentReport.getTotalBooks()));
            bw.write(String.format("Total Users:        %d\n", currentReport.getTotalUsers()));
            bw.write(String.format("Total Borrows:      %d\n", currentReport.getTotalBorrows()));
            bw.write(String.format("Currently Borrowed: %d\n", currentReport.getBorrowed()));
            bw.write(String.format("Returned Books:     %d\n", currentReport.getReturned()));
            bw.write(String.format("Overdue Books:      %d\n\n", currentReport.getOverdue()));
            

            
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
            System.out.println(Ansi.success("Report saved successfully to " + fileName + " in Reports folder."));
            
        } catch (java.io.IOException e) {
            System.err.println(Ansi.error("Error saving report: " + e.getMessage()));
        }
    }
}
