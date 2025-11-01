package com.mycompany.library;

import java.io.Serializable;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.ArrayList;

public class Book implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final SecureRandom random = new SecureRandom(); 
    private final String bookId;
    private String title;
    private String author;
    private String yearPublished;
    private String description;
    private Date lastEdited;
    private ArrayList<String> genre;
    private ArrayList<BorrowDetails> borrowDetails;

    public Book(String title, String author, String yearPublished, String description, ArrayList<String> genre) {
        //Pad short inputs with 'X' characters if they're less than 3 characters
        String BookIDTitle = title.length() < 3 ? title + "XXX".substring(0, 3 - title.length()) : title;
        
        this.bookId = generateBookId(title);
        this.title = title;
        this.author = author;
        this.yearPublished = yearPublished;
        this.description = description;
        this.lastEdited = new Date();
        this.genre = genre;
        this.borrowDetails = new ArrayList<>();
    }

    private String generateBookId(String title) {
        //Extract up to 3 letters from the title (ignore spaces)
        String prefix = title.replaceAll("\\s+", "").toUpperCase();
        if (prefix.length() < 3) prefix = String.format("%-3s", prefix).replace(' ', 'X');
        else prefix = prefix.substring(0, 3);

        //Generate a random 4-character alphanumeric string
        String randomCode = generateRandomCode(5);

        //Combine them into one ID (e.g., B-MET-39A15)
        String id = "B-" + prefix + "-" + randomCode;

        return id;
    }

    private String generateRandomCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // Getters
    public String getBookId() { return bookId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getYearPublished() { return yearPublished; }
    public String getDescription() { return description; }
    public ArrayList<String> getGenre() { return genre; }
    public Date getLastEdited() { return lastEdited; }
    public ArrayList<BorrowDetails> getBorrowDetails() { return borrowDetails; }

    // Setters
    public void setTitle(String title) { 
        this.title = title; 
        updateLastEdited();
    }

    public void setAuthor(String author) { 
        this.author = author; 
        updateLastEdited();
    }

    public void setYearPublished(String yearPublished) { 
        this.yearPublished = yearPublished; 
        updateLastEdited();
    }

    public void setDescription(String description) { 
        this.description = description; 
        updateLastEdited();
    }

    public void setGenre(ArrayList<String> genre) { 
        this.genre = genre; 
        updateLastEdited();
    }

    public void setBorrowDetails(ArrayList<BorrowDetails> borrowDetails) { 
        this.borrowDetails = borrowDetails; 
        updateLastEdited();
    }

    // Automatically update last edited timestamp
    private void updateLastEdited() {
        this.lastEdited = new Date();
    }
    
    public void addBorrowRecord(BorrowDetails record) {
        this.borrowDetails.add(record);
        updateLastEdited();
    }
    
    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String genreList = (genre != null && !genre.isEmpty())
                ? String.join(", ", genre)
                : "N/A";

        return "\n==============================================" +
               "\n Book Information" +
               "\n==============================================" +
               "\n ID            : " + bookId +
               "\n Title         : " + title +
               "\n Author        : " + author +
               "\n Year Published: " + yearPublished +
               "\n Genres        : " + genreList +
               "\n Description   : " + description +
               "\n Last Edited   : " + (lastEdited != null ? sdf.format(lastEdited) : "N/A") +
               "\n Borrow Records: " + (borrowDetails != null ? borrowDetails.size() : 0) +
               "\n==============================================";
    }

}
