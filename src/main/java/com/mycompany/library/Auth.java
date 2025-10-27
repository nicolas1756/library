package com.mycompany.library;

import java.util.Scanner;
import java.util.HashMap;
/**
 *
 * @author nic
 */
public class Auth {
    Scanner scanner = new Scanner(System.in);
    
    public void authMenu(){
       
        while(true){
            System.out.println("\n--Welcome--");
            System.out.println("1: Returning user");
            System.out.println("2: New user");   
            System.out.print("Enter: ");
            
            String menuOption = scanner.next();
            
            switch(menuOption){
                case "1":
                    login();
                    break;
                case "2":
                    registerAcc();
                    break;
                default:
                    System.out.println("Please try again: Enter only 1 or 2");
                    scanner.next();
                    continue;
            }
            break;
        }
        
    }
        
    public boolean login() {
        System.out.println("--Login--");
        
        while (true) {
            System.out.print("Enter your username: ");
            String username = scanner.next();

            System.out.print("Enter your password: ");
            String password = scanner.next();

            if (authenticate(username, password)) {
                System.out.println("Login successful!");
                return true;
            } else {
                System.out.println("Incorrect username or password. Please try again.\n");
            }
        }
    }
    
    public boolean authenticate(String username, String password){
        

        return false;
    }
        
    public boolean registerAcc(){

        System.out.println("--Register new account--");

        while(true){
            System.out.print("Enter your fullname: ");
            String fullName = scanner.next();
 
            System.out.print("Enter your username: ");
            String username = scanner.next();
            
            System.out.print("Enter your password: ");
            String password = scanner.next();
            
            HashMap<String, String> newUser = new HashMap<>();
            newUser.put("fullName", fullName);
            newUser.put("username", username);
            newUser.put("password", password);
            
            FileHandling file = new FileHandling();
            file.writetoFile("account.ser", newUser);
        
            break;
        }
        
        return true;
    }
}
