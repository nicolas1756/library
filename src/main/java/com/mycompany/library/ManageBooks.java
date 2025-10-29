package com.mycompany.library;

import java.util.ArrayList;
import java.util.Scanner;

public class ManageBooks {

    Scanner scanner = new Scanner(System.in);
    FileHandling fileHandling = new FileHandling();

    public static void printTable(ArrayList<Book> books) {
        /*if (books == null || books.isEmpty()) {
            System.out.println("No books available.");
            return;
        }

        System.out.println("╔════════════════════════╦══════════════════╦═══════╗");
        System.out.printf ("║ %-22s ║ %-16s ║ %-5s ║%n", "Title", "Author", "Year");
        System.out.println("╠════════════════════════╬══════════════════╬═══════╣");

        for (Book book : books) {
            System.out.printf("║ %-22s ║ %-16s ║ %-5d ║%n",
                    book.getTitle(),
                    book.getAuthor(),
                    book.getYear());
        }

        */System.out.println("╚════════════════════════╩══════════════════╩═══════╝");
    }


    // Get all books
    public ArrayList<Book> getAllBooks() {
        // Method implementation here
        return null; // Placeholder return
    }

    //Librarian book management methods

    // Add a new book
    public void addBook() {
        Book newBook = new Book("Sample Title", "Sample Author", "2024", "Sample Description");
        System.out.println("New book added: " + newBook);
        fileHandling.writeToFile("books.ser", newBook, Book.class);
    }

    // Remove a book by title
    public void removeBook(String bookID) {
        // Method implementation here
    }

    // Edit a book's details
    public void editBook(String bookID) {
        // Method implementation here
    }



    //General book methods

    // Search for a book by title
    public Book searchBook(String title) {

        return null; 
    }

    // Filter books by author
    public ArrayList<Book> filterBooksByAuthor(String author) {
        // Method implementation here
        return null; // Placeholder return
    }

    // Filter books by year
    public ArrayList<Book> filterBooksByYear(int year) {
        // Method implementation here
        return null; // Placeholder return
    }




}
