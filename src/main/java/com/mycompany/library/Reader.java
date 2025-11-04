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
        manageBooks.setAuth(auth);
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n====================" + Ansi.BOLD + " Menu " + Ansi.RESET +"====================");
        System.out.println(Ansi.ORANGE + "1." + Ansi.RESET + " Browse all books");
        System.out.println("   -> View and borrow from the library's collection of books.");
        System.out.println(Ansi.ORANGE + "2." + Ansi.RESET + " Manage Borrowed Books");
        System.out.println("   -> View or renew your borrowed books.");
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
                manageBooks.printReaderTable(true);
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


}