package com.friend.friend;

import java.util.Map;

/**
 * Handles commands related to the application's listening state and lifecycle.
 */
public class LifecycleCommands {

    // These words trigger a background pause (no system termination)
    private static final String[] PAUSE_TRIGGERS = {
        "bye", "goodbye", "go to sleep", "sleep", "stop listening"
    };

    /**
     * @param map The command map to populate.
     * @param recognizer The instance of the EchoPilotRecognizer to control.
     */
    public LifecycleCommands(Map<String, Runnable> map, EchoPilotRecognizer recognizer) {
        
        // Map PAUSE TRIGGERS to the recognizer's pause action.
        for (String trigger : PAUSE_TRIGGERS) {
            map.put(trigger, recognizer::pause); 
        }

        // Map general resume triggers (aside from the wake phrase handled in the Recognizer loop)
        map.put("start listening", recognizer::resume);
        map.put("wake up", recognizer::resume);
    }
}