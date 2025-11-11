/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.library;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Scanner;

import com.mycompany.library.ManageBooks.Column;
/**
 *
 * @author nic
 */
public class Librarian extends User {
    private static final long serialVersionUID = 1L;
        
    public Librarian(String username, String password, String fullName, ArrayList <String> favoriteBooks) {
        super(username, password, fullName, "Librarian");
    }
    

    @Override
    public void displayMainMenu(Auth auth) {
        ManageBooks manageBooks = new ManageBooks();
        ManageBorrowRecords manageBorrowRecords = new ManageBorrowRecords();
        manageBorrowRecords.setAuth(auth);
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n====================" + Ansi.BOLD + " Menu " + Ansi.RESET +"====================");
        System.out.println(Ansi.ORANGE + "1." + Ansi.RESET + " Manage Books");
        System.out.println("   -> Add/remove/edit/view the collection of books.");
        System.out.println(Ansi.ORANGE + "2." + Ansi.RESET + " Manage Borrow Records");
        System.out.println("   -> View record of borrowed books and manage book returning.");
        System.out.println(Ansi.ORANGE + "3." + Ansi.RESET + " Generate Report");
        System.out.println("   -> Generate library usage report.");
        System.out.println(Ansi.ORANGE + "0." + Ansi.RESET + " Logout");
        System.out.println("   -> Exit your account.");
        System.out.println("==============================================\n");
        System.out.print(Ansi.YELLOW + "Enter your choice: " + Ansi.RESET);


        String input = scanner.next();

        switch (input) {
            case "1":
                System.out.println(Ansi.ORANGE + "Fetching books..." + Ansi.RESET);
                displayManageBooksMenu(auth, scanner, manageBooks);
                break;

            case "2":
                System.out.println(Ansi.ORANGE + "Fetching borrow records..." + Ansi.RESET);
                manageBorrowRecords.getAllBorrowDetails();
                break;

            case "3":
                System.out.println(Ansi.ORANGE + "Generating report..." + Ansi.RESET);
                ManageReport report = new ManageReport();
                report.setAuth(auth);
                report.getValues();
                report.generateReport();
                break;

            case "0":
                System.out.println(Ansi.RED + "Logging out..." + Ansi.RESET);
                auth.logout();
                break;

            default:
                System.out.println(Ansi.RED + "Invalid choice. Please try again." + Ansi.RESET);
        }
    }

    public void displayManageBooksMenu(Auth auth, Scanner scanner, ManageBooks manageBooks) {
        boolean untilExit = true;
        manageBooks.setAuth(auth);
        
        while (untilExit) {
            System.out.println("\n================" + Ansi.BOLD + " Manage Books " + Ansi.RESET +"================");
            System.out.println(Ansi.ORANGE + "1." + Ansi.RESET + " Add a new Book");
            System.out.println("   -> Adds a new book to the library.");
            System.out.println(Ansi.ORANGE + "2." + Ansi.RESET + " Remove a Book");
            System.out.println("   -> Removes a book from the library.");
            System.out.println(Ansi.ORANGE + "3." + Ansi.RESET + " Edit a Book");
            System.out.println("   -> Modifies an existing book.");
            System.out.println(Ansi.ORANGE + "4." + Ansi.RESET + " Print Table");
            System.out.println("   -> Displays the collection of books and metadata.");
            System.out.println(Ansi.ORANGE + "0." + Ansi.RESET + " Back");
            System.out.println("   -> Goes back to main menu.");
            System.out.println("==============================================\n");
            System.out.print(Ansi.YELLOW + "Enter your choice: " + Ansi.RESET);

            String input = scanner.next();


            
            switch (input) {
                case "1":
                    System.out.println(Ansi.ORANGE + "Add a book..." + Ansi.RESET + "\n");
                    manageBooks.addBook();
                    break;

                case "2":
                    System.out.println(Ansi.ORANGE + "Remove a book..." + Ansi.RESET + "\n");
                    manageBooks.removeBook();
                    break;

                case "3":
                    System.out.println(Ansi.ORANGE + "Edit a book..." + Ansi.RESET + "\n");
                    manageBooks.editBook();
                    break;

                case "4":
                    System.out.println(Ansi.ORANGE + "Printing table..." + Ansi.RESET + "\n");
                    manageBooks.displayTable(true);
                    break;

                case "0":
                    untilExit = false;
                    System.out.println(Ansi.ORANGE + "Going back..." + Ansi.RESET + "\n");
                    auth.getCurrentUser().displayMainMenu(auth);
                    break;

                default:
                    System.out.println(Ansi.RED + "Invalid choice. Please try again." + Ansi.RESET);
            }
        
        }

    }

  

        
}