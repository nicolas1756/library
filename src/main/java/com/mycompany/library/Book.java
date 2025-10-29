package com.mycompany.library;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
import java.util.ArrayList;

public class Book implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String bookId;
    private String title;
    private String author;
    private String yearPublished;
    private String description;
    private Date lastEdited;
    private ArrayList<BorrowDetails> borrowDetails;

    public Book(String title, String author, String yearPublished, String description) {
        this.bookId = "B-" + UUID.randomUUID().toString().substring(0, 8);
        this.title = title;
        this.author = author;
        this.yearPublished = yearPublished;
        this.description = description;
        this.lastEdited = new Date();
        this.borrowDetails = new ArrayList<>();
    }

    // Getters
    public String getBookId() { return bookId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getYearPublished() { return yearPublished; }
    public String getDescription() { return description; }
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
        return "Book{" +
                "bookId='" + bookId + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", yearPublished='" + yearPublished + '\'' +
                ", description='" + description + '\'' +
                ", lastEdited=" + lastEdited +
                ", borrowDetails=" + borrowDetails +
                '}';
    }

}
