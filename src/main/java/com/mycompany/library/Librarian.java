/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.library;
import java.util.Scanner;
/**
 *
 * @author nic
 */
public class Librarian extends User {
    private static final long serialVersionUID = 1L;
        
    public Librarian(String username, String password, String fullName) {
        super(username, password, fullName, "Librarian");
    }

    @Override
    public void displayMenu(Auth auth) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("\n--- Librarian Menu ---");
        System.out.println("1. Add/Remove Books");
        System.out.println("2. View All Borrow Records");
        System.out.println("3. Manage Users");
        System.out.println("4. Logout");
        System.out.print("Enter your choice: ");

        String input = scanner.next();

        switch (input) {
            case "1":
                System.out.println("Managing books (Add/Remove)...");
                // call your manageBooks() method here
                break;

            case "2":
                System.out.println("Viewing all borrow records...");
                // call your viewAllBorrowRecords() method here
                break;

            case "3":
                System.out.println("Managing users...");
                // call your manageUsers() method here
                break;

            case "4":
                System.out.println("Logging out...");
                auth.logout();
                break;

            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }
}