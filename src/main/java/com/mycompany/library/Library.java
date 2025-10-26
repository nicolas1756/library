package com.mycompany.library;

/**
 *
 * @author nic
 */




public class Library {

    public static void main(String[] args) {
        System.out.println("--Library Management--");
        File file = new File();
        file.saveToFile();
        System.out.print(file.readFromFile("book.ser"));
    }
    
    
    
}
