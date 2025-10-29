package com.mycompany.library;

/**
 *
 * @author nic
 */

import java.util.Scanner;


public class Library {

    public static void main(String[] args) {



        //file read/write test
        FileHandling fileHandling = new FileHandling();
        Auth auth = new Auth();
        Scanner scanner = new Scanner(System.in);
        
        //System.out.println(fileHandling.readFromFile("accounts.ser", User.class));
        

        
        while (true) {
            if (!auth.isLoggedIn()) {
                auth.authMenu(); // login/register
            } else {
                auth.getCurrentUser().displayMainMenu(auth);
            }
        }
    }



    
    
}
