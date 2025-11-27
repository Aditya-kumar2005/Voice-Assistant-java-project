package com.friend.friend;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

public class MediaCommands {

    private final SpeechEngine tts;
    private final EchoPilotRecognizer recognizer;
    // private final EchoPilotGUI gui; // Only needed if you want to use it
    
    // We only need the dispatcher to ensure the mic resumes after simple tts.speak() calls from the dispatcher
    private final CommandDispatcher dispatcher; 

    private final String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
    private final String downloadsPath = System.getProperty("user.home") + File.separator + "Downloads";
    private final String videosPath = System.getProperty("user.home") + File.separator + "Videos";
    
    private static final String[] VIDEO_EXTENSIONS = {".mp4", ".mkv", ".avi", ".mov", ".wmv"};

    public MediaCommands(Map<String, Runnable> map, SpeechEngine tts, EchoPilotRecognizer recognizer, CommandDispatcher dispatcher) {
        this.tts = tts;
        this.recognizer = recognizer;
        this.dispatcher = dispatcher;
        
        // Command Registration
        map.put("play music", () -> exec("start wmplayer", "Starting Windows Media Player."));
        map.put("pause music", () -> exec("nircmd mediaplay pause", "Music paused."));
        map.put("stop music", () -> exec("nircmd mediaplay stop", "Music stopped."));
        map.put("next track", this::nextTrack);
        map.put("previous track", this::previousTrack);
        map.put("clear", this::clearChat);
        
        // --- FIX 1: Removed duplicate time/date commands. They belong in UtilityCommands. ---
        
        map.put("video", this::playVideo);
    }
    
    private void nextTrack() {
        exec("nircmd mediaplay next", "Next track.");
    }

    private void previousTrack() {
        exec("nircmd mediaplay prev", "Previous track.");
    }

    // --- FIX 2 & 3: Corrected Clear Chat Synchronization ---
    private void clearChat() {
        // 1. Pause the main listening loop immediately (blocking call is often in recognizer.pause())
        recognizer.pause();
        
        // 2. Define the interactive action that MUST happen after the speech prompt.
        Runnable confirmationAction = () -> {
            // Note: listenonce() is blocking, so this is safe in the thread created by speakBlocking/speak(callback).
            try {
                // Speak the prompt using BLOCKING speech (tts.speakBlocking) for immediate feedback
                // OR ensure tts.speak() is called with the next part of the logic as the callback.
                // Since this needs immediate mic access after speaking, a sequence is better.
                
                // We will use the dispatcher's speakResponse to speak the prompt ASYNCHRONOUSLY
                // and pass the confirmation logic as the callback.
                
                tts.speak("Are you sure you want to clear the chat?", () -> {
                     try {
                        // The mic is now free, but we need to listen ONCE.
                        String response = recognizer.listenOnce().trim().toLowerCase();
                        
                        if (response.contains("yes") || response.contains("yeah")) {
                            // Assuming gui.clearChatArea() exists and is a non-blocking call
                            // gui.clearChatArea(); 
                            System.out.println("GUI Chat Area Cleared (Mock).");
                            tts.speakBlocking("Chat cleared."); // Use blocking speech to ensure confirmation is heard
                        } else {
                            tts.speakBlocking("Clear action cancelled.");
                        }
                    } catch (Exception e) {
                        System.err.println("Error during clearChat confirmation: " + e.getMessage());
                        tts.speakBlocking("An error occurred during confirmation.");
                    } finally {
                        recognizer.resume(); // Resume the main listening loop
                    }
                });

            } catch (Exception e) {
                System.err.println("Error initiating clearChat: " + e.getMessage());
                recognizer.resume(); // Always ensure resume if something fails
            }
        };
        
        // Start the process: Speak the first prompt *asynchronously* with the full logic as the callback
        confirmationAction.run();
    }
    
    // --- FIX 2 & 3: Corrected Play Video Synchronization ---
    private void playVideo() {
        recognizer.pause(); // Pause the main recognition loop

        Runnable promptForLocation = () -> {
            try {
                tts.speak("Where is your video located? Say desktop, downloads, or videos.", () -> {
                    try {
                        String location = recognizer.listenOnce().trim().toLowerCase();
        
                        if (location.isEmpty()) {
                            tts.speakBlocking("No location provided, search cancelled.");
                            return; // Exit the loop
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
                                dispatcher.speakResponse("Playing " + videoFiles[0].getName()); // Use dispatcher for standard non-blocking response
                                exec("start vlc \"" + videoPath + "\"", null); // Use null response as dispatcher handles it
                            } else {
                                tts.speakBlocking("No videos found in that folder.");
                            }
                        } else {
                            tts.speakBlocking("Location not recognized.");
                        }
                    } catch (Exception e) {
                        System.err.println("Error during location prompt: " + e.getMessage());
                        tts.speakBlocking("An error occurred during video search.");
                    } finally {
                        recognizer.resume(); // Always ensure resume
                    }
                });
            } catch (Exception e) {
                 System.err.println("Error initiating video search: " + e.getMessage());
                 recognizer.resume();
            }
        };
        
        // Start the process
        promptForLocation.run();
    }
    
    // --- Removed Time/Date methods as they belong in UtilityCommands ---

    // Updated exec to take a verbal success response
    private void exec(String cmd, String successResponse) {
        String[] commandArray = {"cmd", "/c", cmd};
        try {
            Runtime.getRuntime().exec(commandArray);
            if (successResponse != null) {
                // Use the dispatcher's method to handle non-blocking speech and mic resume
                dispatcher.speakResponse(successResponse); 
            }
            System.out.printf("[MediaCommands]: Executed successfully: %s%n", cmd);
        } catch (IOException e) {
            System.err.printf("[MediaCommands]: Failed to execute: %s%n", cmd);
            e.printStackTrace();
            // Optional: Speak error
            // dispatcher.speakResponse("Sorry, I failed to execute the command for " + cmd);
        }
    }
}