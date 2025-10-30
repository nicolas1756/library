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
    public void displayMainMenu(Auth auth) {
        ManageBooks manageBooks = new ManageBooks();
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n====================" + Ansi.BOLD + " Menu " + Ansi.RESET +"====================");
        System.out.println(Ansi.ORANGE + "1." + Ansi.RESET + " Browse all books");
        System.out.println("   -> View the library's collection of books.");
        System.out.println(Ansi.ORANGE + "2." + Ansi.RESET + " Manage Borrowed Books");
        System.out.println("   -> View, renew, or return your borrowed books.");
        System.out.println(Ansi.ORANGE + "3." + Ansi.RESET + " My Favorites");
        System.out.println("   -> View your list of favorited books.");
        System.out.println(Ansi.ORANGE + "4." + Ansi.RESET + " Logout");
        System.out.println("   -> Exit your account.");
        System.out.println("==============================================");
        System.out.print(Ansi.YELLOW + "Enter your choice: " + Ansi.RESET);


        String input = scanner.next();

        switch (input) {
            case "1":
                System.out.println(Ansi.ORANGE + "Loading the library's collection..." + Ansi.RESET);
                displayManageBooksMenu(auth, scanner, manageBooks);
                break;

            case "2":
                System.out.println(Ansi.ORANGE + "Fetching your borrowed books..." + Ansi.RESET);
                break;

            case "3":
                System.out.println(Ansi.ORANGE + "Fetching your list of favorited books..." + Ansi.RESET);
                break;

            case "4":
                System.out.println(Ansi.RED + "Logging out..." + Ansi.RESET);
                auth.logout();
                break;

            default:
                System.out.println(Ansi.RED + "Invalid choice. Please try again." + Ansi.RESET);
        }
    }

    public void displayManageBooksMenu(Auth auth, Scanner scanner, ManageBooks manageBooks) {
        System.out.println("\n================" + Ansi.BOLD + " Manage Books " + Ansi.RESET +"================");
        System.out.println(Ansi.ORANGE + "1." + Ansi.RESET + " Manage Books");
        System.out.println("   -> Add/remove/edit the collection of books.");
        System.out.println(Ansi.ORANGE + "2." + Ansi.RESET + " View All Borrow Records");
        System.out.println("   -> View all borrowed books and their statuses.");
        System.out.println(Ansi.ORANGE + "3." + Ansi.RESET + " Back");
        System.out.println("   -> Goes back to main menu.");
        System.out.println("==============================================");
        System.out.print(Ansi.YELLOW + "Enter your choice: " + Ansi.RESET);


        String input = scanner.next();

        switch (input) {
            case "1":
                System.out.println(Ansi.ORANGE + "Fetching books..." + Ansi.RESET);

                break;

            case "2":
                System.out.println(Ansi.ORANGE + "Fetching borrow records..." + Ansi.RESET);
                break;

            case "3":
                System.out.println(Ansi.ORANGE + "Going back..." + Ansi.RESET);
                auth.getCurrentUser().displayMainMenu(auth);
                break;

            default:
                System.out.println(Ansi.RED + "Invalid choice. Please try again." + Ansi.RESET);
        }
    }
}