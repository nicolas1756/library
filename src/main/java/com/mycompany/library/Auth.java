package com.mycompany.library;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.HashMap;
/**
 *
 * @author nic
 */
public class Auth {
    
    Scanner scanner = new Scanner(System.in);
    FileHandling fileHandling = new FileHandling();
    
    //encapsulated varible to store current logged in user
    private User currentUser;


    // Getter
    public User getCurrentUser() {
        return currentUser;
    }

    // Setter
    public void setCurrentUser(User newUser) {
        this.currentUser = newUser;
    }
    
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public void authMenu(){
       
        //loops until a valid option in given
        while(true){
            System.out.println("\n=============" + Ansi.BOLD + " Library Management " + Ansi.RESET +"=============");
            System.out.println(Ansi.ORANGE + "1:" + Ansi.RESET + " Returning user");
            System.out.println(Ansi.ORANGE + "2:" + Ansi.RESET + " New user");   
            System.out.println("==============================================");
            System.out.print("Enter: ");
            
            String menuOption = scanner.next();
            
            switch(menuOption){
                case "1": //routes to login page
                    login();
                    break;
                case "2": //routes to register page
                    registerAcc();
                    break;
                default: //if invalid option
                    System.out.println("Please try again: Enter only 1 or 2");
                    scanner.next();
                    continue;
            }
            break;
        }
        
    }
    
    public void logout(){
        currentUser = null;
    }
        
    public void login() {
        scanner.nextLine();
        System.out.println("====================" + Ansi.BOLD + " Login " + Ansi.RESET +"===================");
        System.out.println(Ansi.RED + "Enter 0 to exit" + Ansi.RESET);
        System.out.println("==============================================");
        while (true) {
            System.out.print("Enter your username: ");
            String username = scanner.nextLine();

            if (username.equals("0")) { // go back to auth menu if 0 entered
                authMenu();
                break;
            }

            if (username.equals("")) {
                System.out.println(Ansi.RED + "Username can't be empty, please try again." + Ansi.RESET);
                continue;
            }

            System.out.print("Enter your password: ");
            String password = scanner.nextLine();

            if (password.equals("0")) { // go back to auth menu if 0 entered
                authMenu();
                break;
            }

            if (password.equals("")) {
                System.out.println(Ansi.RED + "Password can't be empty, please try again." + Ansi.RESET);
                continue;
            }
            
            if (authenticate(username, password)) {
                //if valid username and password
        System.out.println("==============================================\n");
                System.out.println(Ansi.ORANGE + "Login successful!" + Ansi.RESET);
                System.out.println(Ansi.ORANGE + "Hello, " + (String) getCurrentUser().getFullName() + Ansi.RESET );
                break;
            } else {
                //if invalid username and password
                System.out.println("Incorrect username or password. Please try again.\n");
            }
        }
        
        if(!isLoggedIn()){
            System.out.println("There was as error logging in. Please try again ");
        }
    }
    
    public boolean authenticate(String username, String password) {
        // Retrieve list of accounts
        ArrayList<User> accounts = fileHandling.readFromFile("accounts.ser", User.class);

        for (User account : accounts) { //loops through accounts
            String storedUsername = account.getUsername();//get stored username and password for each account
            String storedPassword = account.getPassword();;
            String storedFullname = account.getFullName();;
            String storedRole = account.getRole();;
            
            if (storedUsername != null && storedPassword != null) { //ensure stored username and password is not null
                if (storedUsername.equals(username) && storedPassword.equals(password)) { //checks if username and password match
                    
                    User user;
                    if ("Librarian".equalsIgnoreCase(storedRole)) {
                        user = new Librarian(storedUsername, storedPassword, storedFullname);
                    } else {
                        user = new Reader(storedUsername, storedPassword, storedFullname);
                    }

                    setCurrentUser(user); // store it globally
                    return true;
                }
            }
        }

        return false;
    }
        
    public boolean registerAcc() {
        System.out.println("============" + Ansi.BOLD + " Register New Account " + Ansi.RESET + "============");
        System.out.println(Ansi.RED + "Enter 0 at any time to cancel" + Ansi.RESET);
        System.out.println("==============================================");

        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }

        while (true) {
            System.out.print("Enter your full name: ");
            String fullName = scanner.nextLine().trim();

            if (fullName.equals("0")) {
                authMenu();
                return false;
            }
            
            if (fullName.equals("")) {
                System.out.println(Ansi.RED + "Fullname can't be empty, please try again." + Ansi.RESET );
                continue;
             }

            String username;
            while (true) {
                System.out.print("Enter your username: ");
                username = scanner.nextLine().trim();

                if (username.equals("0")) {
                    authMenu();
                    return false;
                }
                
                if (username.equals("")) {
                    System.out.println(Ansi.RED + "Username can't be empty, please try again." + Ansi.RESET);
                    continue;
                }


                ArrayList<User> accounts = fileHandling.readFromFile("accounts.ser", User.class);

                boolean foundUsername = false;
                for (User account : accounts) {
                    if (account.getUsername().equals(username)) {
                        System.out.println(Ansi.RED + "Username is already taken, please try again." + Ansi.RESET);
                        foundUsername = true;
                        break;
                    }
                }

                if (!foundUsername) {
                    break; // Username is available
                }
            }

            String password;
            while (true) {
                System.out.print("Enter your password (min 8 char): ");
                password = scanner.nextLine().trim();

                if (password.equals("0")) {
                    authMenu();
                    return false;
                }
                
                if (password.equals("")) {
                    System.out.println(Ansi.RED + "Password can't be empty, please try again." + Ansi.RESET);
                    continue;
                }
                
                if (password.length() < 8) {
                    System.out.println(Ansi.RED + "Password length must be at least 8 characters." + Ansi.RESET);
                    continue;
                }

                break;
            }


            User newUser = new Reader(username, password, fullName);
  
            fileHandling.appendToFile("accounts.ser", newUser, User.class);

            System.out.println("\nAccount successfully created!");
            
            break;
        }

        return true;
    }


}

