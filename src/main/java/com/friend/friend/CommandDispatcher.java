package com.friend.friend;

import java.util.Collections;
import java.util.Map;

public class CommandDispatcher {

    private final SpeechEngine tts;
    private volatile Map<String, Runnable> commandMap = Collections.emptyMap(); 
    
    // NEW: Reference to the recognizer to get the restart callback
    private EchoPilotRecognizer recognizer; 

    public CommandDispatcher(SpeechEngine tts) {
        this.tts = tts;
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
        this.commandMap = Collections.unmodifiableMap(map);
    }

    /**
     * Executes the command and provides NON-BLOCKING audio feedback.
     * The microphone listening process is synchronized to restart only after the speech output completes.
     * @param command The voice command string received from the recognizer.
     */
    public void dispatch(String command) {
        String normalizedCommand = command.toLowerCase().trim();
        Runnable action = commandMap.get(normalizedCommand);
        
        // CRITICAL: Get the callback to restart the recognition engine.
        // The EchoPilotRecognizer is responsible for stopping the mic BEFORE calling dispatch,
        // so the dispatcher's job is to speak and then use this callback to restart it.
        if (recognizer == null) {
            System.err.println("[Dispatcher Error]: Recognizer not set. Cannot synchronize speech.");
            return;
        }
        Runnable restartCallback = recognizer.getRecognitionRestartCallback();

        if (action != null) {
            
            try {
                // 1. Run the command action
                action.run();
                
                // 2. Provide non-blocking feedback, synchronized with mic restart
                // The SpeechEngine will handle its own locking and call the restartCallback upon completion.
                tts.speak("Done", restartCallback); 
                
            } catch (Exception e) {
                System.err.println("[Dispatcher Error]: Failed to execute command: " + normalizedCommand);
                e.printStackTrace();
                
                // Provide non-blocking error feedback, synchronized with mic restart
                tts.speak("Sorry, I failed to execute that command.", restartCallback);
            }
            
        } else {
            // Command not recognized
            System.out.println("[Dispatcher]: Command not recognized: " + command);
            
            // Provide non-blocking "not recognized" feedback, synchronized with mic restart
            tts.speak("Command not recognized. Please try again.", restartCallback);
        }
    }
}