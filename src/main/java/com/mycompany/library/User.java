/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.library;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Scanner;
import java.util.ArrayList;
/**
 *
 * @author nic
 */



public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String password;
    private String fullName;
    private String role;
    private ArrayList<String> favoriteBooks;

    public User(String username, String password, String fullName, String role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.favoriteBooks = new ArrayList<>();
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
    public ArrayList<String> getFavoriteBooks() { return favoriteBooks; }

    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setRole(String role) { this.role = role; }

    public void addFavoriteBook(String bookId) {
        if (!favoriteBooks.contains(bookId)) {
            favoriteBooks.add(bookId);
        }
    }

    @Override
    public String toString() {
    
    return "\n==============================================" +
           "\n User Information" +
           "\n==============================================" +
           "\n Username : " + username +
           "\n Full Name: " + fullName +
           "\n Role     : " + role +
           "\n Favourites : " + favoriteBooks +
           "\n==============================================\n";
    }
    
    // Each role will have its own mainmenu
    public abstract void displayMainMenu(Auth auth);



}
