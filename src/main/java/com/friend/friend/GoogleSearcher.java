package com.friend.friend;

import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class GoogleSearcher {
    public String search(String term) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                String encodedTerm = URLEncoder.encode(term, StandardCharsets.UTF_8.toString());
                String url = "https://www.google.com/search?q=" + encodedTerm;
                Desktop.getDesktop().browse(new URI(url));
                return "Searching Google for " + term;
            } catch (Exception e) {
                e.printStackTrace();
                return "I couldn't open the browser for the search.";
            }
        }
        return "I need a desktop environment to perform a Google search.";
    }
}