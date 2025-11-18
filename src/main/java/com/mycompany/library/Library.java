package com.mycompany.library;

/**
 *
 * @author nic
 */

import java.util.Scanner;


public class Library { //main class where the program executes from

    public static void main(String[] args) {



        //Uncomment to fix book data file
        //consoleUtil util = new consoleUtil();
        //util.loadBooks();


        //Initialize necessary components
        //Scanner scanner = new Scanner(System.in);
        //FileHandling fileHandling = new FileHandling();
        Auth auth = new Auth();
        
        //Display admin account info just for demonstration purposes
        System.out.println(Ansi.info("Admin account:"));
        System.out.println(Ansi.info("Username: admin"));
        System.out.println(Ansi.info("Password: admin123"));

        while (true) {
            if (!auth.isLoggedIn()) {
                // login/register menu
                auth.authMenu(); 
            } else {    
                // Main menu after logging in
                auth.getCurrentUser().displayMainMenu(auth);
            }
        }
    }



    
    
}
