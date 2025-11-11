package com.mycompany.library;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.mycompany.library.BorrowDetails.BorrowStatus;

public class Report {



    // Instance variables to store report data
    private int totalBooks;
    private int totalUsers;
    private int totalBorrows;
    private int borrowed;
    private int returned;
    private int overdue;

    public Report(int totalBooks, int totalUsers, int totalBorrows, int borrowed, int returned, int overdue) {
        this.totalBooks = totalBooks;
        this.totalUsers = totalUsers;
        this.totalBorrows = totalBorrows;
        this.borrowed = borrowed;
        this.returned = returned;
        this.overdue = overdue; 
    }


    // Getter methods
    public int getTotalBooks() {return totalBooks;}
    public int getTotalUsers() {return totalUsers;}
    public int getTotalBorrows() {return totalBorrows;}
    public int getBorrowed() {return borrowed;}
    public int getReturned() {return returned;}
    public int getOverdue() {return overdue;}


    
}
