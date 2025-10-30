/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.library;
import java.util.EnumSet;
import java.util.Scanner;

import com.mycompany.library.ManageBooks.Column;
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
    public void displayMainMenu(Auth auth) {
        ManageBooks manageBooks = new ManageBooks();
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n====================" + Ansi.BOLD + " Menu " + Ansi.RESET +"====================");
        System.out.println(Ansi.PURPLE + "1." + Ansi.RESET + " Manage Books");
        System.out.println("   -> Add/remove/edit/view the collection of books.");
        System.out.println(Ansi.PURPLE + "2." + Ansi.RESET + " View All Borrow Records");
        System.out.println("   -> View all borrowed books and their statuses.");
        System.out.println(Ansi.PURPLE + "3." + Ansi.RESET + " Logout");
        System.out.println("   -> Exit your account.");
        System.out.println("==============================================");
        System.out.print(Ansi.YELLOW + "Enter your choice: " + Ansi.RESET);


        String input = scanner.next();

        switch (input) {
            case "1":
                System.out.println(Ansi.PURPLE + "Fetching books..." + Ansi.RESET);
                displayManageBooksMenu(auth, scanner, manageBooks);
                break;

            case "2":
                System.out.println(Ansi.PURPLE + "Fetching borrow records..." + Ansi.RESET);
                break;

            case "3":
                System.out.println(Ansi.RED + "Logging out..." + Ansi.RESET);
                auth.logout();
                break;

            default:
                System.out.println(Ansi.RED + "Invalid choice. Please try again." + Ansi.RESET);
        }
    }

    public void displayManageBooksMenu(Auth auth, Scanner scanner, ManageBooks manageBooks) {
        boolean untilExit = true;
        while (untilExit) {
            System.out.println("\n================" + Ansi.BOLD + " Manage Books " + Ansi.RESET +"================");
            System.out.println(Ansi.PURPLE + "1." + Ansi.RESET + " Add a new Book");
            System.out.println("   -> Adds a new book to the library.");
            System.out.println(Ansi.PURPLE + "2." + Ansi.RESET + " Remove a Book");
            System.out.println("   -> Removes a book from the library.");
            System.out.println(Ansi.PURPLE + "3." + Ansi.RESET + " Edit a Book");
            System.out.println("   -> Modifys an existing book.");
            System.out.println(Ansi.PURPLE + "4." + Ansi.RESET + " Print table");
            System.out.println("   -> Displays the collection of books and metadata.");
            System.out.println(Ansi.PURPLE + "5." + Ansi.RESET + " Back");
            System.out.println("   -> Goes back to main menu.");
            System.out.println("==============================================");
            System.out.print(Ansi.YELLOW + "Enter your choice: " + Ansi.RESET);

            String input = scanner.next();

            switch (input) {
                case "1":
                    System.out.println(Ansi.PURPLE + "Add a book..." + Ansi.RESET);
                    manageBooks.addBook();
                    break;

                case "2":
                    System.out.println(Ansi.PURPLE + "Remove a book..." + Ansi.RESET);
                    manageBooks.removeBook();
                    break;

                case "3":
                    System.out.println(Ansi.PURPLE + "Edit a book..." + Ansi.RESET);
                    manageBooks.removeBook();
                    break;

                case "4":
                    System.out.println(Ansi.PURPLE + "Printing table..." + Ansi.RESET);
                    manageBooks.printLibrarianTable(true);
                    break;

                case "5":
                    untilExit = false;
                    System.out.println(Ansi.PURPLE + "Going back..." + Ansi.RESET);
                    auth.getCurrentUser().displayMainMenu(auth);
                    break;

                default:
                    System.out.println(Ansi.RED + "Invalid choice. Please try again." + Ansi.RESET);
            }
        
        }

    }

        
}