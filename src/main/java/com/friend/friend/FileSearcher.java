package com.friend.friend;

import java.io.IOException;
/**
     * Attempts to initiate a system search for the given term and provides a path 
     * for the user to manually continue the search.
     * @param term The text to be searched for.
     * @return A verbal response string.
     */

public class FileSearcher {
    // Basic example: Opens the main Desktop directory, which is often a starting point for Windows Search
    public String search(String command) {
        try {
            // Open the user's home directory 
            String[] commandArray = {"cmd", "/c", "start" , command};
            
            Runtime.getRuntime().exec(commandArray); 
            return "Opening your file explorer. Please use the search bar to find " + command + ".";
        } catch (IOException e) {
            e.printStackTrace();
            return "I couldn't open the file explorer.";
        }
    }
}