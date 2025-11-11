package com.mycompany.library;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import com.mycompany.library.BorrowDetails.BorrowStatus;

public class consoleUtil {
    
    //================================================
    // INSTANCE FIELDS
    //================================================
    private static final Scanner scanner = new Scanner(System.in);
    private final FileHandling fileHandling = new FileHandling();
    
    // Static cache for book titles (shared across all instances)
    private static HashMap<String, String> bookTitleCache = new HashMap<>();

    //================================================
    // ENUMS
    //================================================
    enum BookColumn {
        INDEX, ID, TITLE, AUTHOR, GENRE, YEAR, LAST_EDITED, AVAILABLE
    }

    enum BorrowColumn {
        INDEX, ID, USERNAME, TITLE, BOOKID, DATEBORROWED, DUEDATE, DATERETURNED, STATUS
    }

    enum ReportColumn {
        BORROWED, RETURNED, OVERDUE, TOTALBOOKS, TOTALUSERS, TOTALBORROWS
    }

    //================================================
    // BOOK TITLE CACHING
    //================================================
    
    /**
     * Loads book titles from file into cache for quick lookup
     */
    public static void loadBookTitle() {
        FileHandling fileHandling = new FileHandling();
        ArrayList<Book> books = fileHandling.readFromFile("books.ser", Book.class);

        if (books == null || books.isEmpty()) {
            return;
        }

        bookTitleCache.clear();
        for (Book book : books) {
            bookTitleCache.put(book.getBookId(), book.getTitle());
        }
    }

    //================================================
    // BORROW STATUS CHECK
    //================================================
    
    /**
     * Checks if a user currently has an active borrow for a specific book
     * 
     * @param book The book to check
     * @param username The username to check against
     * @return true if user has an active (BORROWED) record for this book
     */
    public static boolean hasActiveBorrow(Book book, String username) {
        return book != null 
            && username != null
            && book.getBorrowDetails() != null
            && book.getBorrowDetails().stream()
                .anyMatch(record -> record.getUsername().equals(username) 
                    && record.getStatus() == BorrowStatus.BORROWED);
    }

    /**
     * Returns indicator string for display in table
     * 
     * @param book The book to check
     * @param username The username to check against
     * @return "*" if user has active borrow, empty string otherwise
     */
    private static String getActiveBorrowIndicator(Book book, String username) {
        return hasActiveBorrow(book, username) ? "*" : "";
    }

    //================================================
    // TABLE PRINTING
    //================================================

    /**
     * Generic method to print formatted table for books, borrow records, or reports
     */
    public static void printTable(ArrayList<?> array, String type, Auth auth) {
        loadBookTitle();
        
        EnumSet<?> columns = getColumnsForType(type);
        Map<Enum<?>, String> headers = getHeadersForType(type, columns);
        
        // Handle empty item list
        if (array == null || array.isEmpty()) {
            System.out.println(Ansi.RED + "No items available." + Ansi.RESET);
            return;
        }

        // Detect Unicode support to decide border style
        boolean supportsUnicode = detectUnicodeSupport();

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

        // Calculate width needed for each column
        Map<Enum<?>, Integer> colWidths = calculateColumnWidths(columns, headers, array, auth);

        // Build the borders and header dynamically
        String[] borders = buildTableBorders(columns, colWidths, TL, TR, BL, BR, TSEP, BSEP, HSEP, CROSS, HLINE);
        String headerRow = buildHeaderRow(columns, headers, colWidths, VSEP);

        // Print header
        System.out.println(borders[0]); // top
        System.out.println(headerRow);
        System.out.println(borders[1]); // separator

        // Print rows
        printTableRows(array, type, columns, colWidths, VSEP, auth);

        // Print bottom border
        System.out.println(borders[2]); // bottom

        // Print footnote for books view
        if (type.equals("books") && auth != null && !auth.isAdmin()) {
            System.out.println("\n* Already Borrowing");
        }
    }

    private static EnumSet<?> getColumnsForType(String type) {
        return switch (type) {
            case "books" -> EnumSet.allOf(BookColumn.class);
            case "borrow" -> EnumSet.allOf(BorrowColumn.class);
            case "report" -> EnumSet.allOf(ReportColumn.class);
            default -> EnumSet.allOf(BookColumn.class);
        };
    }

    private static Map<Enum<?>, String> getHeadersForType(String type, EnumSet<?> columns) {
        Map<Enum<?>, String> headers = new HashMap<>();
        
        for (Enum<?> col : columns) {
            String header = switch (type) {
                case "books" -> getBookColumnHeader((BookColumn) col);
                case "borrow" -> getBorrowColumnHeader((BorrowColumn) col);
                case "report" -> getReportColumnHeader((ReportColumn) col);
                default -> getBookColumnHeader((BookColumn) col);
            };
            headers.put(col, header);
        }
        
        return headers;
    }

    private static String getBookColumnHeader(BookColumn col) {
        return switch (col) {
            case INDEX -> "Index";
            case ID -> "Book ID";
            case TITLE -> "Title";
            case AUTHOR -> "Author";
            case GENRE -> "Genre";
            case YEAR -> "Year";
            case LAST_EDITED -> "Last Edited";
            case AVAILABLE -> "Available";
        };
    }

    private static String getBorrowColumnHeader(BorrowColumn col) {
        return switch (col) {
            case INDEX -> "Index";
            case ID -> "Record ID";
            case USERNAME -> "Username";
            case TITLE -> "Title";
            case BOOKID -> "Book ID";
            case DATEBORROWED -> "Date Borrowed";
            case DUEDATE -> "Due Date";
            case DATERETURNED -> "Date Returned";
            case STATUS -> "Status";
        };
    }

    private static String getReportColumnHeader(ReportColumn col) {
        return switch (col) {
            case BORROWED -> "Borrowed";
            case RETURNED -> "Returned";
            case OVERDUE -> "Overdue";
            case TOTALBOOKS -> "Total Books";
            case TOTALUSERS -> "Total Users";
            case TOTALBORROWS -> "Total Borrows";
        };
    }

    private static Map<Enum<?>, Integer> calculateColumnWidths(EnumSet<?> columns, Map<Enum<?>, String> headers, 
                                                         ArrayList<?> array, Auth auth) {
        Map<Enum<?>, Integer> colWidths = new HashMap<>();
        
        for (Enum<?> col : columns) {
            int max = headers.get(col).length();
            int index = 1;
            
            for (Object item : array) {
                String value = getColumnValue(col, item, index, auth);
                max = Math.max(max, value == null ? 0 : value.length());
                index++;
            }
            
            colWidths.put(col, max + 2); // +2 for padding
        }
        
        return colWidths;
    }

    private static String[] buildTableBorders(EnumSet<?> columns, Map<Enum<?>, Integer> colWidths,
                                       String TL, String TR, String BL, String BR, 
                                       String TSEP, String BSEP, String HSEP, 
                                       String CROSS, String HLINE) {
        StringBuilder top = new StringBuilder(TL);
        StringBuilder sep = new StringBuilder(HSEP);
        StringBuilder bottom = new StringBuilder(BL);

        for (Enum<?> col : columns) {
            int width = colWidths.get(col);
            top.append(HLINE.repeat(width)).append(TSEP);
            sep.append(HLINE.repeat(width)).append(CROSS);
            bottom.append(HLINE.repeat(width)).append(BSEP);
        }

        // Fix last border characters
        top.setCharAt(top.length() - 1, TR.charAt(0));
        sep.setCharAt(sep.length() - 1, HSEP.equals("+") ? '+' : '╣');
        bottom.setCharAt(bottom.length() - 1, BR.charAt(0));

        return new String[]{top.toString(), sep.toString(), bottom.toString()};
    }

    private static String buildHeaderRow(EnumSet<?> columns, Map<Enum<?>, String> headers, 
                                  Map<Enum<?>, Integer> colWidths, String VSEP) {
        StringBuilder header = new StringBuilder(VSEP + " ");
        
        for (Enum<?> col : columns) {
            int width = colWidths.get(col);
            header.append(String.format("%-" + (width - 1) + "s" + VSEP + " ", headers.get(col)));
        }
        
        return header.toString();
    }

    private static void printTableRows(ArrayList<?> array, String type, EnumSet<?> columns, 
                                Map<Enum<?>, Integer> colWidths, String VSEP, Auth auth) {
        int index = 1;
        
        for (Object item : array) {
            StringBuilder row = new StringBuilder(VSEP + " ");

            for (Enum<?> col : columns) {
                int width = colWidths.get(col);
                String value = getColumnValue(col, item, index, auth);
                String displayValue = (value == null) ? "" : value;
                String paddedValue = String.format("%-" + (width - 1) + "s", displayValue);

                // Apply color based on column and type
                paddedValue = applyColumnColor(type, col, item, paddedValue);

                row.append(paddedValue).append(VSEP).append(" ");
            }
            
            System.out.println(row);
            index++;
        }
    }

    private static String applyColumnColor(String type, Enum<?> col, Object item, String paddedValue) {
        if (type.equals("books") && col.name().equals("AVAILABLE")) {
            Book book = (Book) item;
            String color = book.getAvailable() ? Ansi.GREEN : Ansi.ORANGE;
            return color + paddedValue + Ansi.RESET;
        } else if (type.equals("borrow") && col.name().equals("STATUS")) {
            BorrowDetails record = (BorrowDetails) item;
            String color = switch (record.getStatus()) {
                case BORROWED -> Ansi.YELLOW;
                case RETURNED -> Ansi.GREEN;
                case OVERDUE -> Ansi.RED;
                default -> Ansi.RESET;
            };
            return color + paddedValue + Ansi.RESET;
        }
        
        return paddedValue;
    }

    //================================================
    // COLUMN VALUE EXTRACTION
    //================================================

    private static String getColumnValue(Enum<?> col, Object item, int index, Auth auth) {
        if (col instanceof BookColumn) {
            return getBookColumnValue((BookColumn) col, item, index, auth);
        } else if (col instanceof BorrowColumn) {
            return getBorrowColumnValue((BorrowColumn) col, item, index);
        } else if (col instanceof ReportColumn) {
            return getReportColumnValue((ReportColumn) col, item);
        }
        return "";
    }

    private static String getBookColumnValue(BookColumn col, Object item, int index, Auth auth) {
        if (!(item instanceof Book)) return "";
        Book book = (Book) item;
        
        return switch (col) {
            case INDEX -> String.valueOf(index);
            case ID -> book.getBookId();
            case TITLE -> truncateString(book.getTitle(), 30);
            case AUTHOR -> book.getAuthor();
            case GENRE -> book.getStringGenre();
            case YEAR -> String.valueOf(book.getYearPublished());
            case LAST_EDITED -> formatDate(book.getLastEdited());
            case AVAILABLE -> {
                String availability = String.valueOf(book.getAvailable());
                if (auth != null && auth.getCurrentUser() != null) {
                    availability += getActiveBorrowIndicator(book, auth.getCurrentUser().getUsername());
                }
                yield availability;
            }
        };
    }

    private static String getBorrowColumnValue(BorrowColumn col, Object item, int index) {
        if (!(item instanceof BorrowDetails)) return "";
        BorrowDetails record = (BorrowDetails) item;
        
        return switch (col) {
            case INDEX -> String.valueOf(index);
            case ID -> record.getBorrowID();
            case USERNAME -> record.getUsername();
            case TITLE -> bookTitleCache.getOrDefault(record.getBookId(), "Unknown Title");
            case BOOKID -> record.getBookId();
            case DATEBORROWED -> formatDate(record.getDateBorrowed());
            case DUEDATE -> formatDate(record.getDueDate());
            case DATERETURNED -> record.getDateReturned() != null ? formatDate(record.getDateReturned()) : "-";
            case STATUS -> record.getStatus().toString();
        };
    }

    private static String getReportColumnValue(ReportColumn col, Object item) {
        if (!(item instanceof Report)) return "";
        Report report = (Report) item;
        
        return switch (col) {
            case TOTALBOOKS -> String.valueOf(report.getTotalBooks());
            case TOTALUSERS -> String.valueOf(report.getTotalUsers());
            case TOTALBORROWS -> String.valueOf(report.getTotalBorrows());
            case BORROWED -> String.valueOf(report.getBorrowed());
            case RETURNED -> String.valueOf(report.getReturned());
            case OVERDUE -> String.valueOf(report.getOverdue());
        };
    }

    //================================================
    // UTILITY METHODS
    //================================================

    private static String formatDate(Date date) {
        if (date == null) return "";
        return new java.text.SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    public static String truncateString(String str, int length) {
        if (str == null) return "";
        if (str.length() <= length) return str;
        return str.substring(0, length - 3) + "...";
    }

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
        // Check for IDE detection via various methods
        boolean isIDE = detectIDE();

        if (isIDE) {
            return false;
        }

        String os = System.getProperty("os.name").toLowerCase();
        String term = System.getenv("TERM");
        String conEmu = System.getenv("ConEmuANSI");
        String wtSession = System.getenv("WT_SESSION");
        String psModulePath = System.getenv("PSModulePath");

        // Detect old Windows Command Prompt
        boolean isOldWindowsCmd = os.contains("win") && 
                                  term == null && 
                                  wtSession == null && 
                                  conEmu == null &&
                                  psModulePath == null;

        if (isOldWindowsCmd) {
            return false;
        }

        // Known good environments
        return (wtSession != null) ||  // Windows Terminal
               (conEmu != null && conEmu.equalsIgnoreCase("ON")) ||  // ConEmu
               (psModulePath != null && os.contains("win")) ||  // PowerShell
               (term != null && (term.contains("xterm") || 
                                term.contains("ansi") || 
                                term.contains("vt100") ||
                                term.contains("screen") ||
                                term.contains("tmux"))) ||
               (os.contains("mac") || os.contains("nix") || os.contains("nux"));
    }

    private static boolean detectIDE() {
        // Check if System.console() is null (IDEs typically don't have a console)
        if (System.console() == null) {
            return true;
        }

        // Check classpath for common IDEs
        String classPath = System.getProperty("java.class.path", "").toLowerCase();
        String[] ideMarkers = {"netbeans", "idea", "eclipse", "vscode"};

        for (String marker : ideMarkers) {
            if (classPath.contains(marker)) {
                return true;
            }
        }

        // Check system properties
        if (System.getProperty("netbeans.home") != null ||
            System.getProperty("idea.home") != null ||
            System.getProperty("eclipse.home.location") != null) {
            return true;
        }

        // Check environment variables
        if (System.getenv("NETBEANS_HOME") != null ||
            System.getenv("IDEA_HOME") != null ||
            System.getenv("ECLIPSE_HOME") != null) {
            return true;
        }

        return false;
    }

    //================================================
    // SAMPLE DATA LOADER
    //================================================

    public void loadBooks() {
        ArrayList<Book> books = new ArrayList<>();
        Random random = new Random();

        String[] sampleUsers = {
            "alice", "bob", "charlie", "diana", "eric", "fiona", "george"
        };

        Object[][] realBooks = {
            {"Harry Potter and the Sorcerer's Stone", "J.K. Rowling", "1997",
                new String[]{"Fantasy", "Adventure"},
                "A young boy discovers he is a wizard and attends Hogwarts School of Witchcraft and Wizardry."},

            {"A Game of Thrones", "George R.R. Martin", "1996",
                new String[]{"Fantasy", "Drama"},
                "Noble families vie for control of the Iron Throne in the Seven Kingdoms of Westeros."},

            {"The Fellowship of the Ring", "J.R.R. Tolkien", "1954",
                new String[]{"Fantasy", "Adventure"},
                "A hobbit named Frodo begins a journey to destroy the One Ring."},

            {"Murder on the Orient Express", "Agatha Christie", "1934",
                new String[]{"Mystery", "Crime"},
                "Detective Hercule Poirot investigates a murder aboard a luxury train."},

            {"Foundation", "Isaac Asimov", "1951",
                new String[]{"Science Fiction"},
                "A mathematician predicts the fall of the Galactic Empire and establishes the Foundation to preserve knowledge."},

            {"Kafka on the Shore", "Haruki Murakami", "2002",
                new String[]{"Magical Realism", "Fiction"},
                "A surreal coming-of-age story intertwining the lives of a teenage runaway and an aging simpleton."},

            {"The Shining", "Stephen King", "1977",
                new String[]{"Horror", "Psychological"},
                "A family becomes isolated in a haunted hotel, where supernatural forces drive the father to madness."},

            {"Mistborn: The Final Empire", "Brandon Sanderson", "2006",
                new String[]{"Fantasy", "Adventure"},
                "In a world of ash and mist, rebels use metal-based magic to overthrow an immortal tyrant."},

            {"Good Omens", "Neil Gaiman & Terry Pratchett", "1990",
                new String[]{"Comedy", "Fantasy"},
                "An angel and a demon team up to prevent the apocalypse."},

            {"Pride and Prejudice", "Jane Austen", "1813",
                new String[]{"Romance", "Classic"},
                "Elizabeth Bennet navigates issues of manners, morality, and marriage in 19th-century England."},

            {"The Da Vinci Code", "Dan Brown", "2003",
                new String[]{"Thriller", "Mystery"},
                "A symbologist uncovers secrets hidden in Leonardo da Vinci's works while investigating a murder."},

            {"The Hound of the Baskervilles", "Arthur Conan Doyle", "1902",
                new String[]{"Mystery", "Detective"},
                "Sherlock Holmes investigates the legend of a supernatural hound haunting a wealthy family."},

            {"Dune", "Frank Herbert", "1965",
                new String[]{"Science Fiction", "Adventure"},
                "A young nobleman becomes embroiled in a struggle over a desert planet that produces a valuable spice."},

            {"The Hobbit", "J.R.R. Tolkien", "1937",
                new String[]{"Fantasy", "Adventure"},
                "Bilbo Baggins joins a group of dwarves on a quest to reclaim their homeland from a dragon."},

            {"1984", "George Orwell", "1949",
                new String[]{"Dystopian", "Political Fiction"},
                "A man living under a totalitarian regime struggles for freedom and truth."},

            {"To Kill a Mockingbird", "Harper Lee", "1960",
                new String[]{"Classic", "Drama"},
                "A young girl in the racially charged American South learns about justice and empathy through her father's example."},

            {"The Catcher in the Rye", "J.D. Salinger", "1951",
                new String[]{"Classic", "Coming-of-Age"},
                "Holden Caulfield recounts his experiences in New York after being expelled from prep school."},

            {"Brave New World", "Aldous Huxley", "1932",
                new String[]{"Dystopian", "Science Fiction"},
                "A futuristic society engineered for stability hides dark truths about control and conformity."},

            {"The Great Gatsby", "F. Scott Fitzgerald", "1925",
                new String[]{"Classic", "Tragedy"},
                "A mysterious millionaire pursues a dream of love and wealth in 1920s America."},

            {"Frankenstein", "Mary Shelley", "1818",
                new String[]{"Horror", "Science Fiction"},
                "A scientist creates life, only to be horrified by his monstrous creation."},

            {"Dracula", "Bram Stoker", "1897",
                new String[]{"Horror", "Gothic"},
                "Count Dracula moves from Transylvania to England, spreading terror and bloodlust."},

            {"The Picture of Dorian Gray", "Oscar Wilde", "1890",
                new String[]{"Classic", "Philosophical"},
                "A man remains youthful while his portrait ages, reflecting his moral decay."},

            {"The Hunger Games", "Suzanne Collins", "2008",
                new String[]{"Dystopian", "Adventure"},
                "In a post-apocalyptic nation, children are forced to fight to the death for entertainment."},

            {"The Martian", "Andy Weir", "2011",
                new String[]{"Science Fiction", "Adventure"},
                "An astronaut stranded on Mars uses ingenuity to survive alone on the planet."},

            {"The Alchemist", "Paulo Coelho", "1988",
                new String[]{"Adventure", "Philosophical"},
                "A shepherd embarks on a journey to find treasure and fulfill his personal legend."},

            {"Life of Pi", "Yann Martel", "2001",
                new String[]{"Adventure", "Philosophical"},
                "A boy stranded on a lifeboat with a Bengal tiger must rely on faith and wit to survive."},

            {"The Road", "Cormac McCarthy", "2006",
                new String[]{"Post-Apocalyptic", "Drama"},
                "A father and son travel through a desolate world after a cataclysmic event."},

            {"The Girl with the Dragon Tattoo", "Stieg Larsson", "2005",
                new String[]{"Thriller", "Mystery"},
                "A journalist and a hacker uncover dark family secrets while investigating a disappearance."},

            {"Gone Girl", "Gillian Flynn", "2012",
                new String[]{"Thriller", "Mystery"},
                "A man becomes the prime suspect when his wife mysteriously disappears."},

            {"The Kite Runner", "Khaled Hosseini", "2003",
                new String[]{"Drama", "Historical"},
                "A man reflects on his childhood in Afghanistan and his search for redemption."},

            {"The Book Thief", "Markus Zusak", "2005",
                new String[]{"Historical", "Drama"},
                "A young girl finds solace in books during Nazi Germany, narrated by Death itself."},

            {"The Fault in Our Stars", "John Green", "2012",
                new String[]{"Romance", "Drama"},
                "Two teenagers with cancer fall in love and grapple with life and loss."},

            {"The Handmaid's Tale", "Margaret Atwood", "1985",
                new String[]{"Dystopian", "Political Fiction"},
                "In a totalitarian society, fertile women are forced into servitude as child-bearers."},

            {"It", "Stephen King", "1986",
                new String[]{"Horror", "Thriller"},
                "A group of friends confront an ancient evil that takes the form of a killer clown."},

            {"Ready Player One", "Ernest Cline", "2011",
                new String[]{"Science Fiction", "Adventure"},
                "A gamer competes in a virtual world contest to win control of a vast digital universe."},

            {"The Name of the Wind", "Patrick Rothfuss", "2007",
                new String[]{"Fantasy", "Adventure"},
                "A gifted young man recounts his life as a musician, magician, and legend."},

            {"The Chronicles of Narnia: The Lion, the Witch and the Wardrobe", "C.S. Lewis", "1950",
                new String[]{"Fantasy", "Adventure"},
                "Four siblings discover a magical world ruled by an evil witch and join a great battle for freedom."},

            {"The Shadow of the Wind", "Carlos Ruiz Zafón", "2001",
                new String[]{"Mystery", "Historical Fiction"},
                "A boy discovers a forgotten book and uncovers a dark literary mystery in post-war Barcelona."},

            {"The Girl on the Train", "Paula Hawkins", "2015",
                new String[]{"Thriller", "Mystery"},
                "An alcoholic woman becomes entangled in a missing person investigation she thinks she witnessed."},

            {"Educated", "Tara Westover", "2018",
                new String[]{"Memoir", "Drama"},
                "A woman raised in a strict and isolated family escapes to learn about the wider world through education."},

            {"The Night Circus", "Erin Morgenstern", "2011",
                new String[]{"Fantasy", "Romance"},
                "Two rival magicians are bound by a magical competition set within a mysterious traveling circus."}
        };

        // Create books
        for (Object[] b : realBooks) {
            String title = (String) b[0];
            String author = (String) b[1];
            String year = (String) b[2];
            ArrayList<String> genres = new ArrayList<>(Arrays.asList((String[]) b[3]));
            String description = (String) b[4];

            Book book = new Book(title, author, year, description, genres, true);

            // 30% chance the book is borrowed
            if (random.nextDouble() < 0.3) {
                String username = sampleUsers[random.nextInt(sampleUsers.length)];
                Calendar cal = Calendar.getInstance();

                // 20% of borrowed books will be overdue
                if (random.nextDouble() < 0.2) {
                    cal.add(Calendar.DATE, -1 * (1 + random.nextInt(14)));
                } else {
                    cal.add(Calendar.DATE, 7 + random.nextInt(14));
                }

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