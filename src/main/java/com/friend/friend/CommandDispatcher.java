package com.friend.friend;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandDispatcher {

    private final SpeechEngine tts;
    private final WebGui localGui;
    
    // Commands used for web/file searches. This dependency is now final, injected during setup.
    private SystemCommands systemCommands; 
    
    // Map to hold fixed commands (e.g., "open music player") mapped to their actions (Runnable)
    // Using ConcurrentHashMap for thread safety in case addCommand is called while dispatching
    private volatile Map<String, Runnable> commandMap = new ConcurrentHashMap<>(); 
    
    private EchoPilotRecognizer recognizer; 

    /**
     * Constructor: Takes core components needed for feedback and GUI updates.
     */
    public CommandDispatcher(SpeechEngine tts, SystemCommands systemCommands, WebGui localGui) {
        this.tts = tts;
        this.localGui = localGui;
        // systemCommands is often injected later, but we take the provided one.
        this.systemCommands = systemCommands; 
        
        // Initialize commandMap as an empty but ready-to-use ConcurrentHashMap
        this.commandMap = new ConcurrentHashMap<>();
    }

    public void performGuiAction(String action) { // <--- ADD THIS METHOD
        if (action.equals("clearChat")) {
            if (localGui != null) {
                localGui.clearChatArea(); // Assuming WebGui has this method
            } else {
                System.out.print("Cannot perform GUI action: WebGui instance is null.");
            }
        }
    }
    /**
     * Sets the recognizer instance. CRITICAL for synchronizing speech with mic use.
     * @param recognizer The active EchoPilotRecognizer instance.
     */
    public void setRecognizer(EchoPilotRecognizer recognizer) {
        this.recognizer = recognizer;
    }
    public static String apologize() {
        return "Oh no, there was a small problem.";
    }

    public static String waiting() {
        return "I am ready for your next command now.";
    }
    
    // This is used when a recognized command fails to execute (e.g., a file is missing).
    public static String commandNotRecognized(String command) {
        // We use an everyday example (a color not being on the list)
        return "Sorry, I heard you say " + command + ", but that is not in my list of commands. Did you want a different color?";
    }
    /**
     * Sets the SystemCommands dependency. Used during application startup.
     */
    public void setSystemCommands(SystemCommands systemCommands) {
        this.systemCommands = systemCommands;
        System.out.println("[CommandDispatcher] SystemCommands dependency set.");
    }

    /**
     * Registers a map of commands. Usually called once during initialization.
     */
    public void registerCommands(Map<String, Runnable> map) {
        // Clear old map and add all new entries, ensuring all keys are normalized.
        Map<String, Runnable> normalizedMap = new HashMap<>();
        map.forEach((k, v) -> normalizedMap.put(k.toLowerCase().trim(), v));
        
        // Replace the volatile map reference for a safe, atomic update.
        this.commandMap = Collections.unmodifiableMap(new ConcurrentHashMap<>(normalizedMap));
    }

    /**
     * Add or replace a single command at runtime. This is thread-safe.
     */
    public synchronized void addCommand(String key, Runnable action) {
        // Create a new map based on the current one
        Map<String, Runnable> merged = new ConcurrentHashMap<>(this.commandMap);
        // Add or replace the single key
        merged.put(key.toLowerCase().trim(), action);
        // Atomically replace the reference with the new unmodifiable map
        this.commandMap = Collections.unmodifiableMap(merged);
    }

    /**
     * Registers a Skill instance which can add commands or hooks into the dispatcher.
     */
    public void registerSkill(Skill skill) {
        try {
            skill.register(this);
        } catch (Throwable t) {
            System.err.println("Failed to register skill: " + t.getMessage());
            t.printStackTrace();
        }
    }
    
    // =======================================================
    // 📢 Speak Response (NON-BLOCKING with Mic Restart)
    // =======================================================
    
    /**
     * Provides NON-BLOCKING audio feedback from an external command group.
     * This method ensures the microphone is paused during speech and resumed 
     * immediately after the speech completes, using the recognizer's methods.
     * @param text The text to be spoken.
     */
    public void speakResponse(String text) {
        if (recognizer == null) {
            System.err.println("[Dispatcher Error]: Recognizer not set. Cannot synchronize speech.");
            // Fallback: Use simple speak without synchronization
            tts.speak(text); 
            return;
        }
        
        // FIX: Ensure the Recognizer is paused BEFORE the speech starts.
        // The recognizer should already be paused by the main dispatch loop, 
        // but it is good practice to confirm the sync action here.
        // We use recognizer.resume() as the action that runs AFTER the speech is done.
        
        // ASSUMPTION: SpeechEngine.speak(String, Runnable) exists and is NON-BLOCKING.
        tts.speak(text, () -> {
            // This runs after the speech finishes!
            recognizer.resume(); 
        }); 
    }

    // =======================================================
    // 🎯 Main Command Dispatcher
    // =======================================================
    
    /**
     * Executes the command, handling fixed commands and flexible search commands.
     * @param command The voice command string received from the recognizer.
     */
    public void dispatch(String command) {
        if (recognizer == null || systemCommands == null) {
            System.err.println("[Dispatcher Error]: Core dependencies (Recognizer/SystemCommands) not set.");
            tts.speak("I am still starting up, please wait a moment.");
            return;
        }
        
        String normalizedCommand = command.toLowerCase().trim();
        localGui.updateStatus("Command Received: " + normalizedCommand);
        
        // Define the common PAUSE action: The recognizer should pause after error/search feedback.
        // Story: This is the friendly reminder for the elephant to listen carefully after it finishes talking.
        Runnable recognizerPauseAction = () -> recognizer.pause(); 
        
        // --- 1. Handle Flexible Search Commands ---
        if (normalizedCommand.startsWith("search") || normalizedCommand.startsWith("find")) {
            handleSearchCommand(normalizedCommand, recognizerPauseAction);
            return;
        }

        // --- 2. Handle Mapped, Fixed Commands ---
        Runnable action = commandMap.get(normalizedCommand);

        if (action != null) {
            try {
                // 1. Run the command action
                action.run();
                
                // 2. The Recognizer loop (where dispatch() is called from) should handle the 
                //    TTS response ("Done") and subsequent pause/resume based on the action's outcome.
                //    Here, we assume the command's action does its own synchronized speech via speakResponse()
                //    or requires special handling. If the command runs silently, we do nothing.
                
            } catch (Exception e) {
                System.err.println("[Dispatcher Error]: Failed to execute command: " + normalizedCommand);
                e.printStackTrace();
                
                // Provide friendly error feedback, synchronized with mic pause
                // ASSUMPTION: FriendlyBehavior class is available.
                //String friendlyError = FriendlyBehavior.apologize() + " " + FriendlyBehavior.waiting();
                tts.speak(null, recognizerPauseAction);
            }
            
        } else {
            // --- 3. Command not recognized ---
            System.out.println("[Dispatcher]: Command not recognized: " + command);
            
            // Provide friendly "not recognized" feedback, synchronized with mic pause
            //String friendlyNotRecognized = FriendlyBehavior.commandNotRecognized(command);
            tts.speak(null, recognizerPauseAction);
        }
    }

    /**
     * Handles dynamic 'search' and 'find' commands, determining if it's a web or file search.
     */
    private void handleSearchCommand(String normalizedCommand, Runnable pauseAction) {
        // Remove the command prefix ("search " or "find ")
        int prefixLength = normalizedCommand.startsWith("search") ? 6 : 4;
        String searchTerm = normalizedCommand.substring(prefixLength).trim();
        
        if (searchTerm.isEmpty()) {
            tts.speak("What should I search for? Please try again with a file name or topic.", pauseAction);
            localGui.updateStatus("Search term missing.");
            return;
        }
        
        String ttsResponse;
        
        // Story: Imagine looking for a toy 🧸. Do you look in the toy box (Local Files) 
        // or ask a friend who knows everything (The Web)?
        
        // Determine search type based on keywords
        if (searchTerm.contains("file") || searchTerm.contains("folder") || searchTerm.contains("laptop") || searchTerm.contains("computer")) {
            
            // --- Local File Search ---
            System.out.println("[Dispatcher]: Executing Local File Search for: " + searchTerm);
            systemCommands.searchLocalFiles(searchTerm);
            ttsResponse = "Searching your files for " + searchTerm;
            localGui.updateStatus("Searching your files for " + searchTerm);
            
        } else {
            
            // --- Default to Google/Web Search ---
            System.out.println("[Dispatcher]: Executing Web Search for: " + searchTerm);
            systemCommands.searchweb(searchTerm);
            ttsResponse = "Searching Google for " + searchTerm;
            localGui.updateStatus("Searching Google for " + searchTerm);
        }
        
        // Provide non-blocking feedback, synchronized with mic pause
        tts.speak(ttsResponse, pauseAction);
    }
}