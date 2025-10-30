package com.mycompany.library;

import java.io.*;
import java.util.ArrayList;

/**
 * @author nic
 */

public class FileHandling {


    public <T extends Serializable> boolean overrideFile(String path, ArrayList<T> value) {

        ArrayList<T> dataList = value;
        path = "Data/" + path;

        // Write updated list safely
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(path))) {
            outputStream.writeObject(dataList);
            return true;
        } catch (IOException e) {
            System.out.println(Ansi.RED + "Error writing to file: " + e.getMessage() + Ansi.RESET);
            return false;
        }
    }

    // Generic write method (safe + preserves previous data)
    public <T extends Serializable> boolean appendToFile(String path, T value, Class<T> type) {

        ArrayList<T> dataList = readFromFile(path, type);
        path = "Data/" + path;
        
        // Add new object
        dataList.add(value);

        // Write updated list safely
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(path))) {
            outputStream.writeObject(dataList);
            return true;
        } catch (IOException e) {
            System.out.println(Ansi.RED + "Error writing to file: " + e.getMessage() + Ansi.RESET);
            return false;
        }
    }


    public <T extends Serializable> ArrayList<T> readFromFile(String path, Class<T> type) {
        path = "Data/" + path;
        File file = new File(path);
        ArrayList<T> dataList = new ArrayList<>();

        // Create empty file if missing
        if (!file.exists()) {
            try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(path))) {
                outputStream.writeObject(dataList);
            } catch (IOException e) {
                System.out.println(Ansi.RED + "Error creating new file: " + e.getMessage() + Ansi.RESET);
            }
            return dataList;
        }

        // Attempt to read
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(path))) {
            Object obj = inputStream.readObject();

            if (obj instanceof ArrayList<?>) {
                ArrayList<?> rawList = (ArrayList<?>) obj;

                for (Object item : rawList) {
                    if (!type.isInstance(item)) {
                        System.out.println(Ansi.RED + "Warning: File contains unexpected type: " + item.getClass().getName() + Ansi.RESET);
                        return new ArrayList<>(); // return empty if mismatch
                    }
                }

                dataList = (ArrayList<T>) rawList;
            } else {
                System.out.println(Ansi.RED + "Warning: File does not contain an ArrayList." + Ansi.RESET);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(Ansi.RED + "Failed to read file: " + e.getMessage() + Ansi.RESET);
        }

        return dataList;
    }
}
