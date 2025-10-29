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
public class Reader extends User {
    private static final long serialVersionUID = 1L;
    
    public Reader(String username, String password, String fullName) {
        super(username, password, fullName, "Reader");
    }

    @Override
    public void displayMenu(Auth auth) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("1. Browse Books");
        System.out.println("2. Borrow a Book");
        System.out.println("3. View Borrow History");
        System.out.println("4. Logout");
        System.out.print("Enter your choice: ");

        String input = scanner.next();

        switch (input) {
            case "1":
                System.out.println("Browsing books...");
                break;

            case "2":
                System.out.println("Borrowing a book...");
                break;

            case "3":
                System.out.println("Viewing borrow history...");
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