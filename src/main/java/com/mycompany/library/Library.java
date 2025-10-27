package com.mycompany.library;

/**
 *
 * @author nic
 */




public class Library {

    public static void main(String[] args) {
        System.out.println("--Library Management--");
        
        FileHandling fileHandling = new FileHandling();
        System.out.print(fileHandling.readFromFile("account.ser"));
        
        Auth auth = new Auth();
        auth.authMenu();
                
    }
    
    
    
}
