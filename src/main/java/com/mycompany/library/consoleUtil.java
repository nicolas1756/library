package com.mycompany.library;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

public class consoleUtil {
    private static final Scanner scanner = new Scanner(System.in);
    FileHandling fileHandling = new FileHandling();

    public static boolean confirmAction(String message) {
        System.out.print(Ansi.ORANGE + message + " (y/n): " + Ansi.RESET);
        String input = scanner.nextLine().trim().toLowerCase();

        while (!input.equals("y") && !input.equals("n")) {
            System.out.print(Ansi.RED + "Invalid input. Please enter 'y' or 'n': " + Ansi.RESET);
            input = scanner.nextLine().trim().toLowerCase();
        }

        return input.equals("y");
    }

    public static boolean detectUnicodeSupport() {
        String os = System.getProperty("os.name").toLowerCase();
        String term = System.getenv("TERM");
        String conEmu = System.getenv("ConEmuANSI");
        String wtSession = System.getenv("WT_SESSION");
        String psModulePath = System.getenv("PSModulePath"); // PowerShell
        String encoding = System.getProperty("sun.stdout.encoding", "UTF-8");

        return (wtSession != null) || // Windows Terminal
            (conEmu != null && conEmu.equalsIgnoreCase("ON")) || // ConEmu
            (psModulePath != null && os.contains("win")) || // PowerShell
            (term != null && (term.contains("xterm") || term.contains("ansi") || term.contains("vt100"))) ||
            (encoding.toLowerCase().contains("utf")) || // UTF-8 encoding detected
            (!os.contains("win")); // Assume true for macOS/Linux
    }

    //reload book.ser with sample books
    public void loadBooks(){
        ArrayList<Book> books = new ArrayList<>();
        Random random = new Random();

        // Some sample authors, genres, and titles
        String[] authors = {
            "J.K. Rowling", "George R.R. Martin", "J.R.R. Tolkien", "Agatha Christie",
            "Isaac Asimov", "Haruki Murakami", "Stephen King", "Brandon Sanderson",
            "Neil Gaiman", "Jane Austen", "Dan Brown", "Arthur Conan Doyle"
        };

        String[] titlePrefixes = {
            "The Mystery of", "Chronicles of", "Tales from", "Journey to",
            "Legends of", "The Rise of", "Secrets of", "The Fall of",
            "Memoirs of", "Echoes of", "Adventures in", "Whispers from"
        };

        String[] titleNouns = {
            "the Lost Kingdom", "the Forgotten Realm", "the Dark Forest", "the Golden City",
            "the Crimson Tower", "Eldoria", "Avalon", "the Silent Sea",
            "Dreamhaven", "the Endless Desert", "Shadowvale", "the Silver Moon"
        };

        String[] genrePool = {
            "Fantasy", "Science Fiction", "Mystery", "Romance", "Thriller", "Horror", "Adventure", "Historical"
        };

        String[] sampleUsers = {
            "alice", "bob", "charlie", "diana", "eric", "fiona", "george"
        };

        // Create 50 books
        for (int i = 0; i < 50; i++) {
            String title = titlePrefixes[random.nextInt(titlePrefixes.length)] + " " + titleNouns[random.nextInt(titleNouns.length)];
            String author = authors[random.nextInt(authors.length)];
            String year = String.valueOf(1980 + random.nextInt(45)); // between 1980–2024
            String description = "A thrilling story about " + title.toLowerCase() + ".";
            
            // Each book has 1–3 genres
            ArrayList<String> genres = new ArrayList<>();
            int genreCount = 1 + random.nextInt(3);
            for (int g = 0; g < genreCount; g++) {
                String genre = genrePool[random.nextInt(genrePool.length)];
                if (!genres.contains(genre)) genres.add(genre);
            }

            Book book = new Book(title, author, year, description, genres, true);

            // 40% chance the book is already borrowed
            if (random.nextDouble() < 0.3) {
                String username = sampleUsers[random.nextInt(sampleUsers.length)];
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE, 7 + random.nextInt(14)); // due in 1–3 weeks
                Date dueDate = cal.getTime();

                BorrowDetails record = new BorrowDetails(username, book.getBookId(), dueDate);
                book.setAvailable(false);
                book.addBorrowRecord(record);
            }

            books.add(book);
        }
    
        
        fileHandling.overrideFile("books.ser", books);
    }

}
