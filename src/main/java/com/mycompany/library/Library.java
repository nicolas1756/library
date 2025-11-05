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
        System.out.println(Ansi.BOLD + Ansi.BLUE + "Admin account" + Ansi.RESET);
        System.out.println(Ansi.BLUE + "Username: admin" + Ansi.RESET);
        System.out.println(Ansi.BLUE + "Password: admin123" + Ansi.RESET);

        while (true) {
            if (!auth.isLoggedIn()) {
                auth.authMenu(); // login/register
            } else {
                
                auth.getCurrentUser().displayMainMenu(auth);
            }
        }
    }



    
    
}
