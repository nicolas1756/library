/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.library;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 *
 * @author nic
 */

public class BorrowDetails implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum BorrowStatus {
        BORROWED,
        RETURNED,
        OVERDUE
    }


    private final String borrowId;
    private String username;
    private String bookId;
    private Date dateBorrowed;
    private Date dueDate;
    private Date dateReturned;
    private BorrowStatus status;  

    public BorrowDetails(String username, String bookId, Date dueDate) {
        this.borrowId = generateBorrowId();
        this.username = username;
        this.bookId = bookId;
        this.dateBorrowed = new Date();  
        this.dueDate = dueDate;
        this.dateReturned = null;
        this.status = BorrowStatus.BORROWED;  // Default status
    }

    // Getters
    public String getUsername() { return username; }
    public String getBookId() { return bookId; }
    public Date getDateBorrowed() { return dateBorrowed; }
    public Date getDueDate() { return dueDate; }
    public Date getDateReturned() { return dateReturned; }
    public BorrowStatus getStatus() { return status; }

    // Setters
    public void setUsername(String username) { this.username = username; }
    public void setBookId(String bookId) { this.bookId = bookId; }
    public void setDateBorrowed(Date dateBorrowed) { this.dateBorrowed = dateBorrowed; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }
    public void setDateReturned(Date dateReturned) { this.dateReturned = dateReturned; }
    public void setStatus(BorrowStatus status) { this.status = status; }


    private static String generateBorrowId() {
        return UUID.randomUUID().toString().substring(0, 10).toUpperCase();
    }

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
                ""+
                ", status='" + status + '\'' +
                '}' + "\n";
    }
}
