package com.friend.friend;

import java.io.IOException;
import java.util.Map;
/**
 * SystemCommands registers high-impact system control actions (shutdown, restart)
 * and provides the search delegation methods for the CommandDispatcher.
 * It enforces a verbal confirmation loop for critical system actions.
 */
public class SystemCommands {

    private final SpeechEngine tts;
    private final EchoPilotRecognizer recognizer;
    private final GoogleSearcher googleSearcher; 
    private final FileSearcher fileSearcher;

    /**
     * Constructs the SystemCommands group and registers its critical commands.
     * @param map The command map to populate.
     * @param tts The Text-to-Speech engine for verbal feedback.
     * @param recognizer The voice recognition engine for listening and state control.
     * @param googleSearcher The utility for executing web searches.
     * @param fileSearcher The utility for executing local file searches.
     */
    public SystemCommands(Map<String, Runnable> map, SpeechEngine tts, EchoPilotRecognizer recognizer, GoogleSearcher googleSearcher, FileSearcher fileSearcher) {
        this.tts = tts;
        this.recognizer = recognizer;
        this.googleSearcher = googleSearcher;
        this.fileSearcher = fileSearcher;

        // Command registration, mapping to the confirmation wrapper
        map.put("shutdown system", () -> confirmAndExecute("shutdown -s -t 0"));
        map.put("restart system", () -> confirmAndExecute("shutdown -r -t 0"));
        map.put("sleep system", () -> confirmAndExecute("Rundll32.exe powrprof.dll,SetSuspendState 0,1,0"));
        map.put("hibernate system", () -> confirmAndExecute("shutdown -h"));
        map.put("lock system", () -> confirmAndExecute("rundll32.exe user32.dll,LockWorkStation"));
        map.put("log off", () -> confirmAndExecute("shutdown -l"));

        System.out.println("[SystemCommands]: Registered 6 critical commands.");
    }

    // --- Dynamic Search Methods (Executed by CommandDispatcher) ---

    /**
     * Executes a Google search in the default web browser via the injected GoogleSearcher.
     * @param term The term to search for.
     * @return The TTS response string (e.g., "Searching Google for X").
     */
    public String searchGoogle(String term) {
        // Delegate the actual action and response generation to the searcher.
        return googleSearcher.search(term);
    }

    /**
     * Executes a file search on the local system via the injected FileSearcher.
     * @param term The term to search for.
     * @return The TTS response string (e.g., "Searching your files for Y").
     */
    public String searchLocalFiles(String term) {
        // Delegate the actual action and response generation to the searcher.
        return fileSearcher.search(term);
    }
    
    // --- Confirmation and Execution Logic ---

    /**
     * Helper to get a human-friendly description of the command for TTS.
     */
    private String getFriendlyAction(String cmd) {
        if (cmd.contains("-s")) return "shut down the system";
        if (cmd.contains("-r")) return "restart the system";
        if (cmd.contains("SetSuspendState")) return "put the system to sleep";
        if (cmd.contains("-h")) return "put the system in hibernate mode";
        if (cmd.contains("LockWorkStation")) return "lock the system";
        if (cmd.contains("-l")) return "log you off";
        return "execute the command";
    }

    /**
     * Prompts the user for verbal confirmation before executing the command.
     * This is an interactive sequence that pauses continuous recognition.
     * @param cmd The system command to execute (e.g., "shutdown -s -t 0").
     */
    private void confirmAndExecute(String cmd) {
        // 1. Pause continuous recognition
        recognizer.pause();
        String friendlyAction = getFriendlyAction(cmd);

        try {
            // 2. Use BLOCKING speak for the prompt
            tts.speakBlocking("Are you sure you want to " + friendlyAction + "? Say yes or no.");
            
            // 3. Blocking listen for user response
            // FIX APPLIED: Ensure listenOnce() is executed to capture the response.
            String response = recognizer.listenOnce().trim().toLowerCase();
            
            if (response.contains("yes")) {
                // 4. BLOCKING speak confirmation and execute
                tts.speakBlocking("Confirmed. Executing " + friendlyAction + " now.");
                exec(cmd);
            } else if (response.contains("no") || response.contains("cancel")) {
                // 4. BLOCKING speak cancellation
                tts.speakBlocking(friendlyAction + " cancelled.");
            } else {
                // 4. BLOCKING speak ambiguous response
                tts.speakBlocking("I didn't catch that. " + friendlyAction + " cancelled.");
            }
        } catch (Exception e) {
            System.err.println("[SystemCommands]: Error during voice confirmation for " + friendlyAction + ": " + e.getMessage());
            tts.speakBlocking("An error occurred during confirmation.");
        } finally {
            // 5. CRITICAL: Resume continuous recognition
            recognizer.resume();
        }
    }

    /**
     * Executes a system command via cmd /c for robust handling of Windows utilities.
     * @param cmd The command string to execute.
     */
    private void exec(String cmd) {
        // Using cmd /c for execution robustness on Windows
        String[] commandArray = {"cmd", "/c", cmd};
        try {
            Runtime.getRuntime().exec(commandArray);
            System.out.printf("[SystemCommands]: Executed successfully: %s%n", cmd);
        } catch (IOException e) {
            System.err.printf("[SystemCommands]: Failed to execute: %s%n", cmd);
            e.printStackTrace();
        }
    }
}