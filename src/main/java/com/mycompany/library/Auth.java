package com.mycompany.library;

import java.util.Scanner;

/**
 *
 * @author nic
 */
public class Auth {
        public boolean login(){
        
        Scanner scanner = new Scanner(System.in);
        
        System.out.print("Enter your username: ");
        String username = scanner.nextLine();
        
        System.out.print("Enter your password: ");
        String password = scanner.nextLine();
        
        System.out.print(username + password);
        
        
        
        return false;
    }
}
