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
            System.out.println("\n--Welcome--"); //menu options
            System.out.println("1: Returning user");
            System.out.println("2: New user");   
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
        System.out.println("\n--Login--");
        System.out.println("\nEnter -1 to exit");
        System.out.println("----------------");
        while (true) {
            System.out.print("Enter your username: ");
            String username = scanner.next();

            if (username.equals("-1")) { // go back to auth menu if -1 entered
                authMenu();
                break;
            }

            System.out.print("Enter your password: ");
            String password = scanner.next();

            if (password.equals("-1")) { // go back to auth menu if -1 entered
                authMenu();
                break;
            }
            
            if (authenticate(username, password)) {
                //if valid username and password
                System.out.println("-----------------");
                System.out.println("Login successful!");
                System.out.println("Hello, " + (String) getCurrentUser().getFullName());
                System.out.println("-----------------");
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
        System.out.println("-- Register New Account --");
        System.out.println("Enter -1 at any time to cancel\n");

        // 🧹 Clear any leftover newline from previous Scanner inputs
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }

        while (true) {
            System.out.print("Enter your full name: ");
            String fullName = scanner.nextLine().trim();

            if (fullName.equals("-1")) {
                authMenu();
                return false;
            }
            
            if (fullName.equals("")) {
                System.out.println("Fullname can't be empty, please try again.");
                continue;
             }

            String username;
            while (true) {
                System.out.print("Enter your username: ");
                username = scanner.nextLine().trim();

                if (username.equals("-1")) {
                    authMenu();
                    return false;
                }
                
                if (username.equals("")) {
                    System.out.println("Username can't be empty, please try again.");
                    continue;
                }


                ArrayList<User> accounts = fileHandling.readFromFile("accounts.ser", User.class);

                boolean foundUsername = false;
                for (User account : accounts) {
                    if (account.getUsername().equals(username)) {
                        System.out.println("Username is already taken, please try again.");
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

                if (password.equals("-1")) {
                    authMenu();
                    return false;
                }
                
                if (password.equals("")) {
                    System.out.println("Password can't be empty, please try again.");
                    continue;
                }
                
                if (password.length() < 8) {
                    System.out.println("Password length must be at least 8 characters.");
                    continue;
                }

                break;
            }


            User newUser = new Reader(username, password, fullName);
  
            fileHandling.writeToFile("accounts.ser", newUser, User.class);

            System.out.println("\nAccount successfully created!");
            
            break;
        }

        return true;
    }


}

