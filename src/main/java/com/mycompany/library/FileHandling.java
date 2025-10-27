package com.mycompany.library;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author nic
 */
public class FileHandling { //Handles reading and writing to files
    
    public boolean writetoFile(String path, HashMap value) {
        
        //Intialises arrayList to add data
        ArrayList<HashMap<String, ?>> arrayListData = new ArrayList<>();


        File file = new File(path); //checks if file exists
        if (file.exists()) {
            //Retrives existing file and puts it in the variable orginalFile
            ArrayList<HashMap<String, ?>> originalFile = new ArrayList<>();
            originalFile = readFromFile(path);
            
            //appends new value to end of original file
            arrayListData = originalFile;
            originalFile.add(value);
            
            // Tries to write arrayList to a file specified by 'path' variable
            try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(path))) {
                outputStream.writeObject(arrayListData);
            } catch (IOException e) {
                return false;
            }
        }
        else{
            //pushes new value into empty arrayList
            arrayListData.add(value);
            // Tries to write arrayList to a file specified by 'path' variable
            try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(path))) {
                outputStream.writeObject(arrayListData);
            } catch (IOException e) {
                return false;
            }
        }
               


        // If data was successfully saved, returns true.
        return true;
    }
    
    
    public ArrayList<HashMap<String, ?>> readFromFile(String path) { //read serialised data of array list from specified file
        ArrayList<HashMap<String, ?>> arrayListData = new ArrayList<>();

        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(path))) {
            // Retrieves data from file and typecasts it to correct datatype.
            arrayListData = (ArrayList<HashMap<String, ?>>) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e1) {
            System.out.println("Failed to read file: " + e1.getMessage());
        }

        // Returns arrayList
        return arrayListData;
    }

}
