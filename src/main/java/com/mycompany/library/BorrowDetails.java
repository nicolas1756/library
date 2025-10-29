/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.library;

import java.io.Serializable;
import java.util.Date;


/**
 *
 * @author nic
 */

public class BorrowDetails implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;     // who borrowed the book
    private String bookId;       // ID of the borrowed book
    private Date dateBorrowed;   // when it was borrowed
    private Date dueDate;        // when it’s due
    private Date dateReturned;   // when it was returned (null if still borrowed)
    private String status;       // "Borrowed", "Returned", "Overdue", etc.

    // Constructor
    public BorrowDetails(String username, String bookId, Date dateBorrowed, Date dueDate, String status) {
        this.username = username;
        this.bookId = bookId;
        this.dateBorrowed = dateBorrowed;
        this.dueDate = dueDate;
        this.dateReturned = null; 
        this.status = status;
    }

    // Getters
    public String getUsername() { return username; }
    public String getBookId() { return bookId; }
    public Date getDateBorrowed() { return dateBorrowed; }
    public Date getDueDate() { return dueDate; }
    public Date getDateReturned() { return dateReturned; }
    public String getStatus() { return status; }

    // Setters
    public void setUsername(String username) { this.username = username; }
    public void setBookId(String bookId) { this.bookId = bookId; }
    public void setDateBorrowed(Date dateBorrowed) { this.dateBorrowed = dateBorrowed; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }
    public void setDateReturned(Date dateReturned) { this.dateReturned = dateReturned; }
    public void setStatus(String status) { this.status = status; }

    public boolean isReturned() {
        return dateReturned != null;
    }

    @Override
    public String toString() {
        return "BorrowDetails{" +
                "username='" + username + '\'' +
                ", bookId='" + bookId + '\'' +
                ", dateBorrowed=" + dateBorrowed +
                ", dueDate=" + dueDate +
                ", dateReturned=" + (dateReturned != null ? dateReturned : "Not returned") +
                ", status='" + status + '\'' +
                '}';
    }
}
