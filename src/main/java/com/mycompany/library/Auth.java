package com.mycompany.library;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Handles user authentication, login, and registration
 * @author nic
 */
public class Auth {
    
    //================================================
    // constants
    //================================================
    private static final String ACCOUNTS_FILE = "accounts.ser";
    private static final String LIBRARIAN_ROLE = "Librarian";
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final String EXIT_COMMAND = "0";
    
    //================================================
    // instance fields
    //================================================
    private final Scanner scanner = new Scanner(System.in);
    private final FileHandling fileHandling = new FileHandling();
    
    // Encapsulated variable to store current logged in user
    private User currentUser;

    //================================================
    // getters & setters
    //================================================
    
    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User newUser) {
        this.currentUser = newUser;
    }

    //================================================
    // status checks
    //================================================
    
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && LIBRARIAN_ROLE.equals(currentUser.getRole());
    }
    
    //================================================
    // authentication menu
    //================================================
    
    public void authMenu() {
        while (true) {
            System.out.println("\n=============" + Ansi.BOLD + " Library Management " + Ansi.RESET + "=============");
            System.out.println(Ansi.ORANGE + "1:" + Ansi.RESET + " Returning user");
            System.out.println(Ansi.ORANGE + "2:" + Ansi.RESET + " New user");   
            System.out.println("==============================================");
            System.out.print("Enter: ");
            
            String menuOption = scanner.nextLine().trim();
            
            switch (menuOption) {
                case "1" -> login();
                case "2" -> registerAcc();
                default -> {
                    System.out.println(Ansi.warn("Please try again: Enter only 1 or 2"));
                    continue;
                }
            }
            break;
        }
    }
    
    //================================================
    // logout
    //================================================
    
    public void logout() {
        currentUser = null;
    }
    
    //================================================
    // login
    //================================================
    

    public void login() {
        System.out.println("====================" + Ansi.BOLD + " Login " + Ansi.RESET + "===================");
        System.out.println(Ansi.info("Enter 0 to exit"));
        System.out.println("==============================================");
        
        while (true) {
            String username = promptForInput("Enter your username: ", "Username");
            if (username == null) return; // User cancelled

            String password = promptForInput("Enter your password: ", "Password");
            if (password == null) return; // User cancelled
            
            if (authenticate(username, password)) {
                System.out.println("==============================================\n");
                System.out.println(Ansi.info("Login successful!"));
                System.out.println(Ansi.info("Hello, " + getCurrentUser().getFullName()));
                break;
            } else {
                System.out.println(Ansi.error("Incorrect username or password. Please try again.\n"));
            }
        }
    }
    

    public boolean authenticate(String username, String password) {
        ArrayList<User> accounts = fileHandling.readFromFile(ACCOUNTS_FILE, User.class);

        if (accounts == null) {
            return false;
        }

        for (User account : accounts) {
            if (username.equals(account.getUsername()) 
                && password.equals(account.getPassword())) {
                setCurrentUser(account);
                return true;
            }
        }

        return false;
    }
    
    //================================================
    // registration
    //================================================
    
    public boolean registerAcc() {
        System.out.println("============" + Ansi.BOLD + " Register New Account " + Ansi.RESET + "============");
        System.out.println(Ansi.info("Enter 0 at any time to cancel"));
        System.out.println("==============================================");

        String fullName = promptForInput("Enter your full name: ", "Fullname");
        if (fullName == null) return false;

        String username = promptForUniqueUsername();
        if (username == null) return false;

        String password = promptForPassword();
        if (password == null) return false;

        User newUser = new Reader(username, password, fullName);
        fileHandling.appendToFile(ACCOUNTS_FILE, newUser, User.class);

        System.out.println(Ansi.GREEN + "\nAccount successfully created!" + Ansi.RESET);
        
        return true;
    }
    
    //================================================
    // input validation helpers
    //================================================
    
    private String promptForInput(String prompt, String fieldName) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.equals(EXIT_COMMAND)) {
                return null; // User cancelled
            }

            if (input.isEmpty()) {
                System.out.println(Ansi.error(fieldName + " can't be empty, please try again."));
                continue;
            }

            return input;
        }
    }

    private String promptForPassword() {
        while (true) {
            String password = promptForInput("Enter your password (min " + MIN_PASSWORD_LENGTH + " char): ", "Password");
            
            if (password == null) {
                return null; // User cancelled
            }

            if (password.length() < MIN_PASSWORD_LENGTH) {
                System.out.println(Ansi.error("Password length must be at least " + MIN_PASSWORD_LENGTH + " characters."));
                continue;
            }

            return password;
        }
    }


    private String promptForUniqueUsername() {
        ArrayList<User> accounts = fileHandling.readFromFile(ACCOUNTS_FILE, User.class);
        
        while (true) {
            String username = promptForInput("Enter your username: ", "Username");
            
            if (username == null) {
                return null; // User cancelled
            }

            if (accounts != null && isUsernameTaken(accounts, username)) {
                System.out.println(Ansi.error("Username is already taken, please try again."));
                continue;
            }

            return username;
        }
    }


    private boolean isUsernameTaken(ArrayList<User> accounts, String username) {
        return accounts.stream()
            .anyMatch(account -> account.getUsername().equals(username));
    }
}