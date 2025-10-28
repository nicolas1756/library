package com.mycompany.library;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.HashMap;
/**
 *
 * @author nic
 */
public class Auth {
    
    //encapsulated varible to store current logged in user
    private String currentUser = ""; 
    
    // Getter
    public String getCurrentUser() {
      return currentUser;
    }

    // Setter
    public void setCurrentUser(String newName) {
      this.currentUser = newName;
    }

    
    
    Scanner scanner = new Scanner(System.in);
    FileHandling fileHandling = new FileHandling();
    
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
        
    public void login() {
        System.out.println("--Login--");
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
                System.out.println("Login successful!");
            } else {
                //if invalid username and password
                System.out.println("Incorrect username or password. Please try again.\n");
            }
        }
    }
    
    public boolean authenticate(String username, String password) {
        // Retrieve list of accounts
        ArrayList<HashMap<String, ?>> accounts = fileHandling.readFromFile("account.ser");

        for (HashMap<String, ?> account : accounts) { //loops through accounts
            String storedUsername = (String) account.get("username");//get stored username and password for each account
            String storedPassword = (String) account.get("password");

            if (storedUsername != null && storedPassword != null) { //ensure stored username and password is not null
                if (storedUsername.equals(username) && storedPassword.equals(password)) { //checks if username and password match
                    setCurrentUser(username);
                    return true;
                }
            }
        }

        return false;
    }
        
    public boolean registerAcc() {
        System.out.println("-- Register New Account --");
        System.out.println("Enter -1 at any time to cancel\n");

        while (true) {
            System.out.print("Enter your full name: ");
            String fullName = scanner.next();

            if (fullName.equals("-1")) {
                authMenu();
                return false; // Exit method
            }

            String username;
            while (true) {
                System.out.print("Enter your username: ");
                username = scanner.next();

                if (username.equals("-1")) {
                    authMenu();
                    return false;
                }

                ArrayList<HashMap<String, ?>> accounts = fileHandling.readFromFile("account.ser"); //get list of accounts

                boolean foundUsername = false; //check for repeating usernam to ensure uniqueness
                for (HashMap<String, ?> account : accounts) { //loop through accounts
                    String storedUsername = (String) account.get("username"); //username for current looped through account

                    if (storedUsername != null && storedUsername.equals(username)) { //if username is not unique
                        System.out.println("Username is already taken, please try again.\n");
                        foundUsername = true;
                        break;
                    }
                }

                if (!foundUsername) {
                    break; // Username is available
                }
            }

            System.out.print("Enter your password: ");
            String password = scanner.next();

            if (password.equals("-1")) {
                authMenu();
                return false;
            }

            HashMap<String, String> newUser = new HashMap<>(); //formats new user data into a hashmap
            newUser.put("fullName", fullName);
            newUser.put("username", username);
            newUser.put("password", password);
            
            fileHandling.writetoFile("account.ser", newUser); //write new account to file account.ser
        
            break;
        }
        
        return true;
    }
}

