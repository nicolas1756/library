package com.mycompany.library;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

public class consoleUtil {
    private static final Scanner scanner = new Scanner(System.in);
    FileHandling fileHandling = new FileHandling();


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
                "A symbologist uncovers secrets hidden in Leonardo da Vinci’s works while investigating a murder."},

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
                "A young girl in the racially charged American South learns about justice and empathy through her father’s example."},

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
            if (random.nextDouble() < 0.3) { // 30% chance this book is borrowed
                String username = sampleUsers[random.nextInt(sampleUsers.length)];
                Calendar cal = Calendar.getInstance();

                // 20% of these borrowed books will be overdue
                if (random.nextDouble() < 0.2) {
                    // overdue: due date 1–14 days ago
                    cal.add(Calendar.DATE, -1 * (1 + random.nextInt(14)));
                } else {
                    // normal: due date 7–21 days in the future
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
