package com.mycompany.library;

/**
 *
 * @author nic
 */

import java.util.Scanner;


public class Library {

    public static void main(String[] args) {
        System.out.println("--Library Management--");
        
        //file read/write test
        FileHandling fileHandling = new FileHandling();
        System.out.println(fileHandling.readFromFile("account.ser", User.class));
        
        Auth auth = new Auth();
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            if (!auth.isLoggedIn()) {
                auth.authMenu(); // login/register
            } else {
                mainMenu(auth, scanner);
            }
        }
    }

    public static void mainMenu(Auth auth, Scanner scanner) {
        auth.getCurrentUser().displayMenu();
    }

    
    
}
