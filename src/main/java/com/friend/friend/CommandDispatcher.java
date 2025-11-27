package com.friend.friend;

import java.util.Collections;
import java.util.Map;

public class CommandDispatcher {

    private final SpeechEngine tts;
    private SystemCommands systemCommands;
    // Map to hold fixed commands (e.g., "open music player") mapped to their actions (Runnable)
    private volatile Map<String, Runnable> commandMap = Collections.emptyMap(); 
    
    private EchoPilotRecognizer recognizer; 

    public CommandDispatcher(SpeechEngine tts, SystemCommands systemCommands) {
        this.tts = tts;
        this.systemCommands = systemCommands;
    }
    
    /**
     * Sets the recognizer instance, required for getting the restart callback.
     * This must be called during application startup.
     * @param recognizer The active EchoPilotRecognizer instance.
     */
    public void setRecognizer(EchoPilotRecognizer recognizer) {
        this.recognizer = recognizer;
    }

    public void registerCommands(Map<String, Runnable> map) {
        // Use an unmodifiable map to prevent changes after registration
        this.commandMap = Collections.unmodifiableMap(map);
    }
    
    // =======================================================
    // 📢 Speak Response for external command groups
    // =======================================================
    /**
     * Provides NON-BLOCKING audio feedback from an external command group.
     * This method passes the necessary callback to the TTS engine to restart 
     * the microphone (via resume) immediately after speech completes.
     * @param text The text to be spoken.
     */
    public void speakResponse(String text) {
        if (recognizer == null) {
            System.err.println("[Dispatcher Error]: Recognizer not set. Cannot synchronize speech.");
            // Fallback: Use simple speak without synchronization
            tts.speak(text); 
            return;
        }
        
        // FIX: Use recognizer.resume() to correctly switch back to active listening after speech.
        // This is the action that runs AFTER the speech is done.
        Runnable restartAction = () -> recognizer.resume(); 
        // ASSUMPTION: SpeechEngine.speak(String, Runnable) exists.
        tts.speak(text, restartAction); 
    }

    // =======================================================
    // 🎯 Main Command Dispatcher
    // =======================================================
    /**
     * Executes the command. 
     * NOTE: The EchoPilotRecognizer loop handles its own blocking speech ("Done") 
     * and mode switching (pause()) immediately after this method returns. 
     * This method only handles synchronization for failed/search commands where
     * the response is handled within the dispatcher.
     * @param command The voice command string received from the recognizer.
     */
    public void setSystemCommands(SystemCommands systemCommands) {
        this.systemCommands = systemCommands;
        System.out.println("[CommandDispatcher] SystemCommands dependency injected.");
    }
    public void dispatch(String command) {
        if (recognizer == null) {
            System.err.println("[Dispatcher Error]: Recognizer not set. Cannot execute command.");
            return;
        }
        
        String normalizedCommand = command.toLowerCase().trim();
        
        // Define the common PAUSE action: The recognizer should pause after error/search feedback.
        Runnable recognizerPauseAction = () -> recognizer.pause(); 
        
        // --- NEW: Handle Flexible Search Commands ---
        if (normalizedCommand.startsWith("search") || normalizedCommand.startsWith("find")) {
            handleSearchCommand(normalizedCommand, recognizerPauseAction);
            return;
        }

        // --- Handle Mapped, Fixed Commands ---
        Runnable action = commandMap.get(normalizedCommand);

        if (action != null) {
            try {
                // 1. Run the command action
                action.run();
                
                // 2. The Recognizer loop handles the TTS response ("Done") and subsequent pause.
                
            } catch (Exception e) {
                System.err.println("[Dispatcher Error]: Failed to execute command: " + normalizedCommand);
                e.printStackTrace();
                
                // Provide non-blocking error feedback, synchronized with mic pause
                tts.speak("Sorry, I failed to execute that command.", recognizerPauseAction);
            }
            
        } else {
            // Command not recognized
            System.out.println("[Dispatcher]: Command not recognized: " + command);
            
            // Provide non-blocking "not recognized" feedback, synchronized with mic pause
            tts.speak("Command not recognized. Please try again.", recognizerPauseAction);
        }
    }

    /**
     * Handles dynamic 'search' and 'find' commands, determining if it's a web or file search.
     * @param normalizedCommand The recognized command string (e.g., "search music name").
     * @param pauseAction The callback to pause the recognizer after speech completes.
     */
    private void handleSearchCommand(String normalizedCommand, Runnable pauseAction) {
        int firstSpace = normalizedCommand.indexOf(" ");
        if (firstSpace == -1 || firstSpace == normalizedCommand.length() - 1) {
            tts.speak("What should I search for?", pauseAction);
            return;
        }

        String searchTerm = normalizedCommand.substring(firstSpace + 1).trim();
        
        if (searchTerm.isEmpty()) {
            tts.speak("What should I search for?", pauseAction);
            return;
        }
        
        String ttsResponse;
        
        // Determine search type based on keywords
        if (searchTerm.contains("file") || searchTerm.contains("folder") || searchTerm.contains("laptop") || searchTerm.contains("computer")) {
            
            // --- Local File Search ---
            System.out.println("[Dispatcher]: Executing Local File Search for: " + searchTerm);
            systemCommands.searchLocalFiles(searchTerm);
            ttsResponse = "Searching your files for " + searchTerm;
            
        } else {
            
            // --- Default to Google/Web Search ---
            System.out.println("[Dispatcher]: Executing Web Search for: " + searchTerm);
            systemCommands.searchGoogle(searchTerm);
            ttsResponse = "Searching Google for " + searchTerm;
        }
        
        // Provide non-blocking feedback, synchronized with mic pause (FIX APPLIED HERE)
        // The pauseAction is now correctly defined in dispatch() to call recognizer.pause()
        tts.speak(ttsResponse, pauseAction);
    }
}