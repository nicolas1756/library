package com.mycompany.library;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
/**
 *
 * @author nic
 */
public class File { //Handles reading and writing to files
    
    public boolean saveToFile() {
        
        HashMap<String, String> dataHashMap = new HashMap<>();
        dataHashMap.put("bookCatalog", "wadadad");


        // Tries (with resources) to write hashmap to a file called data.ser.
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("book.ser"))) {
            outputStream.writeObject(dataHashMap);
        } catch (IOException e) {
            return false;
        }

        // If data was successfully saved, returns true.
        return true;
    }
    
    
    public HashMap<String, List<?>> readFromFile(String path) {
        HashMap<String, List<?>> dataHashMap = new HashMap<>();

        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(path))) {
            // Retrieves data from file and typecasts it to correct datatype.
            dataHashMap = (HashMap<String, List<?>>) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e1) {
            // Multi catch that warns user if reading data failed.
           
        }

        // Returns hashmap, hopefully containing all necessary data.
        return dataHashMap;
    }

}
