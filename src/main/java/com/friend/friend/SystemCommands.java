package com.friend.friend;

import java.io.IOException;
import java.util.List;
import java.util.Map;
/**
 * SystemCommands registers high-impact system control actions (shutdown, restart)
 * and provides search delegation methods for the CommandDispatcher.
 * It enforces a verbal confirmation loop for critical system actions.
 *
 * This class assumes a Windows environment based on the system commands used.
 */
public class SystemCommands {

    private final SpeechEngine tts;
    private final EchoPilotRecognizer recognizer;
    private final MediaCommands commands;

    /**
     * Constructs the SystemCommands group and registers its critical commands.
     * @param map The command map to populate.
     * @param tts The Text-to-Speech engine for verbal feedback.
     * @param recognizer The voice recognition engine for listening and state control.
     * @param commands The utility for executing media and browser commands.
     */
    public SystemCommands(Map<String, Runnable> map, SpeechEngine tts, EchoPilotRecognizer recognizer, MediaCommands commands) {
        this.tts = tts;
        this.recognizer = recognizer;
        this.commands = commands;

        // Command registration, mapping to the confirmation wrapper
        // NOTE: These commands are specific to Windows OS.
        map.put("shutdown system", () -> confirmAndExecute("shutdown -s -t 0"));
        map.put("restart system", () -> confirmAndExecute("shutdown -r -t 0"));
        map.put("sleep system", () -> confirmAndExecute("Rundll32.exe powrprof.dll,SetSuspendState 0,1,0"));
        map.put("hibernate system", () -> confirmAndExecute("shutdown -h"));
        map.put("lock system", () -> confirmAndExecute("rundll32.exe user32.dll,LockWorkStation"));
        map.put("log off", () -> confirmAndExecute("shutdown -l"));

        System.out.println("[SystemCommands]: Registered 6 critical commands.");
    }

    // =================================================================
    // CRITICAL COMMAND EXECUTION
    // =================================================================

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
     * This interactive sequence pauses the continuous recognition loop.
     * @param cmd The system command to execute (e.g., "shutdown -s -t 0").
     */
    private void confirmAndExecute(String cmd) {
        // 1. Pause continuous recognition
        recognizer.pause();
        String friendlyAction = getFriendlyAction(cmd);

        try {
            // 2. Use BLOCKING speak for the prompt to ensure the prompt completes before listening
            tts.speakBlocking("Are you sure you want to " + friendlyAction + "? Say yes or no.");
            String response = recognizer.listenOnce().trim().toLowerCase();
            
            if (response.contains("yes")) {
                // 3. Speak confirmation and execute
                tts.speakBlocking("Confirmed. Executing " + friendlyAction + " now.");
                exec(cmd);
            } else if (response.contains("no") || response.contains("cancel")) {
                // 4. Speak cancellation
                tts.speakBlocking(friendlyAction + " cancelled.");
            } else {
                // 5. Speak ambiguous response
                tts.speakBlocking("I didn't catch that. " + friendlyAction + " cancelled.");
            }
        } catch (Exception e) {
            System.err.println("[SystemCommands]: Error during voice confirmation for " + friendlyAction + ": " + e.getMessage());
            tts.speakBlocking("An error occurred during confirmation.");
        } finally {
            // 6. CRITICAL: Always resume continuous recognition
            recognizer.resume();
        }
    }
    
    // =================================================================
    // SEARCH DELEGATION
    // =================================================================

    /**
     * Delegates a web search to the MediaCommands utility.
     * @param command The search query.
     * @return The status message from the search operation.
     */
    public String searchweb(String command) {
        return commands.search(command);
    }

    /**
     * Delegates a local file search to the MediaCommands utility.
     * @param command The file path or command to open.
     * @return The status message from the file operation.
     */
    public String searchLocalFiles(String command) {
        return commands.findfile(command);
    }

    // =================================================================
    // LOW-LEVEL EXECUTION
    // =================================================================

    /**
     * Executes a system command via cmd /c for robust handling of Windows utilities.
     * @param cmd The command string to execute.
     */
    private void exec(String cmd) {
        // Using cmd /c for execution robustness on Windows
        List<String> parts = List.of("cmd", "/c", cmd);
        try {
            // Use a generous timeout for critical operations
            ProcessRunner.run(parts, 30); 
            System.out.printf("[SystemCommands]: Executed successfully: %s%n", cmd);
        } catch (IOException | InterruptedException e) {
            System.err.printf("[SystemCommands]: Failed to execute: %s%n", cmd);
            e.printStackTrace();
            // Note: Voice response is intentionally skipped here, as the system might be unstable or shutting down.
        }
    }
}