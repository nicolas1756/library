/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.library;
import java.io.Serializable;
import java.util.Scanner;
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

    public User(String username, String password, String fullName, String role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }

    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setRole(String role) { this.role = role; }

    @Override
    public String toString() {
        return "AuthUser{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
    
    // Each role will have its own mainmenu
    public abstract void displayMainMenu(Auth auth);

    // Each role will be given its own menu when selecting, 
    // For librarians this will be for every book, for readers this will be for borrowed books
    public abstract void displayManageBooksMenu(Auth auth, Scanner scanner, ManageBooks manageBooks);
}
