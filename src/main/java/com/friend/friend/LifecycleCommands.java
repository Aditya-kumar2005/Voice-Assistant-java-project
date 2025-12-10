package com.friend.friend;

import java.util.Map;

/**
 * Handles commands related to the application's listening state and lifecycle.
 * Uses FriendlyBehavior to provide empathetic responses for pause/resume actions.
 */
public class LifecycleCommands {

    // These words trigger a background pause (no system termination)
    private static final String[] PAUSE_TRIGGERS = {
        "bye", "goodbye", "go to sleep", "sleep","stop listening"
    };
    private static final String[] START_TRIGGERS = {
        "wake up" ,"start listening" ,"my friend" ,"friend" ,"wake up my friend"
    };
    /**
     * @param map The command map to populate.
     * @param recognizer The instance of the EchoPilotRecognizer to control.
     */
    // Inside your LifecycleCommands constructor
    public LifecycleCommands(Map<String, Runnable> map,EchoPilotRecognizer recognizer,WebGui gui,CommandDispatcher dispatcher) {

    for (String trigger : PAUSE_TRIGGERS) {
            map.put(trigger, () -> {
                recognizer.pause();
                String friendlyPause = FriendlyBehavior.paused();
                recognizer.getSpeechEngine().speakBlocking(friendlyPause);
            });
        }
    for (String trigger : START_TRIGGERS) {
            map.put(trigger, () -> {
                String friendlyResume = FriendlyBehavior.resumed();
                recognizer.getSpeechEngine().speak(friendlyResume);
                recognizer.resume();
            });
        }
    // Switch to Language Model
    map.put("change to language model", () -> {
        System.out.println("[Recognizer]: COMMAND DETECTED: Switch to LM Mode.");
        try{
            recognizer.switchToLanguageModel();
        }catch(Exception e){}
        
    });

    // Switch to Grammar Model
    map.put("change to grammar model", () -> {
        System.out.println("[Recognizer]: COMMAND DETECTED: Switch to Grammar Mode.");
        try{
            recognizer.switchToGrammar();
        }catch(Exception e){}    
    });
}
}