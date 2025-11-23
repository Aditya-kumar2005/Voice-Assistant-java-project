package com.friend.friend;

import java.io.IOException;
import java.util.Map;

/**
 * Manages commands related to system control (shutdown, restart, lock).
 * These commands require verbal confirmation before execution.
 * * NOTE: This assumes the existence of SpeechEngine and EchoPilotRecognizer 
 * interfaces/classes with methods for blocking speech and listening.
 */
public class SystemCommands {

    private final SpeechEngine tts;
    private final EchoPilotRecognizer recognizer;

    /**
     * Constructs the SystemCommands group and registers its commands.
     * @param map The command map to populate.
     * @param tts The Text-to-Speech engine for verbal feedback.
     * @param recognizer The voice recognition engine for listening and state control.
     */
    public SystemCommands(Map<String, Runnable> map, SpeechEngine tts, EchoPilotRecognizer recognizer) {
        this.tts = tts;
        this.recognizer = recognizer;

        // Command registration, mapping to the confirmation wrapper
        map.put("shutdown system", () -> confirmAndExecute("shutdown -s -t 0"));
        map.put("restart system", () -> confirmAndExecute("shutdown -r -t 0"));
        map.put("sleep system", () -> confirmAndExecute("Rundll32.exe powrprof.dll,SetSuspendState 0,1,0"));
        map.put("hibernate system", () -> confirmAndExecute("shutdown -h"));
        map.put("lock system", () -> confirmAndExecute("rundll32.exe user32.dll,LockWorkStation"));
        map.put("log off", () -> confirmAndExecute("shutdown -l"));

        System.out.println("[SystemCommands]: Registered 6 critical commands.");
    }

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
        return "execute the command"; // Fallback
    }

    /**
     * Prompts the user for verbal confirmation before executing the command.
     * This is an interactive sequence that pauses continuous recognition.
     * * @param cmd The system command to execute (e.g., "shutdown -s -t 0").
     */
    private void confirmAndExecute(String cmd) {
        // 1. Pause continuous recognition
        recognizer.pause();
        String friendlyAction = getFriendlyAction(cmd);

        try {
            // 2. Use BLOCKING speak for the prompt
            tts.speak("Are you sure you want to " + friendlyAction + "? Say yes or no.",null);
            
            // 3. Blocking listen for user response
            // Assuming listenOnce() returns the recognized text and blocks until it gets a response.
            String response = recognizer.listenOnce().trim().toLowerCase();

            if (response.contains("yes")) {
                // 4. BLOCKING speak confirmation and execute
                tts.speak("Confirmed. Executing " + friendlyAction + " now.",null);
                exec(cmd);
            } else if (response.contains("no") || response.contains("cancel")) {
                // 4. BLOCKING speak cancellation
                tts.speak(friendlyAction + " cancelled.",null);
            } else {
                // 4. BLOCKING speak ambiguous response
                tts.speak("I didn't catch that. " + friendlyAction + " cancelled.",null);
            }
        } catch (Exception e) {
            System.err.println("[SystemCommands]: Error during voice confirmation for " + friendlyAction + ": " + e.getMessage());
            tts.speak("An error occurred during confirmation.",null);
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