package com.friend.friend;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;

public class MediaCommands {

    private final SpeechEngine tts;
    private final EchoPilotRecognizer recognizer;
    private final EchoPilotGUI gui; 

    private final String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
    private final String downloadsPath = System.getProperty("user.home") + File.separator + "Downloads";
    private final String videosPath = System.getProperty("user.home") + File.separator + "Videos";
    
    private static final String[] VIDEO_EXTENSIONS = {".mp4", ".mkv", ".avi", ".mov", ".wmv"};

    public MediaCommands(Map<String, Runnable> map, SpeechEngine tts, EchoPilotRecognizer recognizer, EchoPilotGUI gui) {
        this.tts = tts;
        this.recognizer = recognizer;
        this.gui = gui;

        // Command Registration
        map.put("play music", () -> exec("start wmplayer"));
        map.put("pause music", () -> exec("nircmd mediaplay pause"));
        map.put("stop music", () -> exec("nircmd mediaplay stop"));
        map.put("next track", this::nextTrack);
        map.put("previous track", this::previousTrack);
        map.put("clear", this::clearChat);
        
        map.put("what is the time", this::currentTime);
        map.put("what is the date", this::todayDate); 
        
        map.put("video", this::playVideo);
        
        // FIX: Removed map.put("start", ...) and map.put("stop", ...) as they conflict 
        // with FolderCommands and LifecycleCommands.
    }
    
    private void nextTrack() {
        exec("nircmd mediaplay next");
    }

    private void previousTrack() {
        exec("nircmd mediaplay prev");
    }

    private void clearChat() {
        recognizer.pause();
        
        Runnable confirmationAction = () -> {
            try {
                tts.speakInternal("Are you sure you want to clear the chat?");
                
                String response = recognizer.listenOnce().trim().toLowerCase();
                
                if (response.contains("yes")) {
                    // gui.clearChatArea(); // Assuming this method exists
                    System.out.println("GUI Chat Area Cleared (Mock).");
                    tts.speakInternal("Chat cleared");
                } else {
                    tts.speakInternal("Clear chat cancelled");
                }
            } catch (Exception e) {
                System.err.println("Error during clearChat interaction: " + e.getMessage());
                tts.speakInternal("An error occurred during confirmation.");
            } finally {
                recognizer.resume(); 
            }
        };
        
        tts.speak("Are you sure you want to clear the chat?", confirmationAction);
    }

    private void playVideo() {
        recognizer.pause();
        
        Runnable locationPrompt = () -> {
            try {
                tts.speakInternal("Where is your video located? Say desktop, downloads, or videos.");
                
                String location = recognizer.listenOnce().trim().toLowerCase();

                if (location.isEmpty()) {
                    tts.speakInternal("I didn't catch that. Please try again.");
                    return;
                }

                String folderPath = null;
                if (location.contains("desktop")) folderPath = desktopPath;
                else if (location.contains("downloads")) folderPath = downloadsPath;
                else if (location.contains("videos")) folderPath = videosPath;

                if (folderPath != null) {
                    File[] videoFiles = new File(folderPath).listFiles(
                        (dir, name) -> Arrays.stream(VIDEO_EXTENSIONS).anyMatch(name.toLowerCase()::endsWith)
                    );

                    if (videoFiles != null && videoFiles.length > 0) {
                        String videoPath = videoFiles[0].getAbsolutePath();
                        tts.speakInternal("Playing " + videoFiles[0].getName());
                        exec("start vlc \"" + videoPath + "\"");
                    } else {
                        tts.speakInternal("No supported video files found in " + location);
                    }
                } else {
                    tts.speakInternal("Invalid location. Please say desktop, downloads, or videos.");
                }
            } catch (Exception e) {
                 System.err.println("Error during playVideo interaction: " + e.getMessage());
                 tts.speakInternal("An unexpected error occurred while processing the video request.");
            } finally {
                recognizer.resume();
            }
        };
        
        tts.speak("Initializing video search.", locationPrompt);
    }

    private void todayDate() {
        String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy"));
        tts.speak("Today's date is: " + formattedDate, recognizer.getRecognitionRestartCallback());
    }

    private void currentTime() {
        String formattedTime = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));
        tts.speak("Current time is: " + formattedTime, recognizer.getRecognitionRestartCallback());
    }

    private void exec(String cmd) {
        String[] commandArray = {"cmd", "/c", cmd};
        try {
            Runtime.getRuntime().exec(commandArray);
            System.out.printf("[MediaCommands]: Executed successfully: %s%n", cmd);
        } catch (IOException e) {
            System.err.printf("[MediaCommands]: Failed to execute: %s%n", cmd);
            e.printStackTrace();
        }
    }
}