package com.friend.friend;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
// Removed unnecessary javax.speech imports

public class SpeechEngine {
    
    // Story: The SpeechEngine is the singing bird 🐦, and the voice is its song.
    private final Voice voice;
    private Thread currentSpeechThread;
    private final WebGui gui;

    /**
     * Initializes the SpeechEngine and allocates the desired FreeTTS voice.
     */
    public SpeechEngine(WebGui gui) {
        this.gui = gui;
        // System property must be set BEFORE VoiceManager is instantiated.
        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        
        // Use a standard FreeTTS voice name
        this.voice = VoiceManager.getInstance().getVoice("kevin16");
        
        if (this.voice != null) {
            this.voice.allocate();
            // Set properties if needed (e.g., rate or volume)
            // this.voice.setRate(150); 
            gui.updateStatus("[SpeechEngine]: Using voice: kevin16");
        } else {
            // Throw a runtime exception if a critical resource is missing
            throw new IllegalStateException("Voice 'kevin16' not found. Check FreeTTS setup and dependencies.");
        }
    }

    // --- ASYNCHRONOUS SPEAKING ---

    /**
     * ASYNC SPEAK: Plays text non-blockingly on a new thread and runs a callback afterward.
     * @param text The text to be spoken.
     * @param callback The action to run AFTER speech completes (e.g., Recognizer.resume()).
     */
    public synchronized void speak(String text, Runnable callback) { 
        // 1. Check for speaker access (Mic must be released when speaker is used)
        if (!AudioResourceManager.requestSpeakerAccess()) {
            gui.updateStatus("Speaker busy. Skipping speech.");
            // Execute callback immediately if speech is skipped to prevent deadlocks
            if (callback != null) callback.run();
            return;
        }

        // 2. Stop any existing speech
        this.stop();

        // 3. Start a new speech thread
        this.currentSpeechThread = new Thread(() -> {
            try {
                this.speakBlockingInternal(text);
            } catch (Exception e) {
                System.err.println("[SpeechEngine] Error during async speech: " + e.getMessage());
            } finally {
                // 4. Critical: Release resource and execute callback
                AudioResourceManager.releaseSpeaker();
                if (callback != null) { 
                    callback.run();
                }
            }

        }, "SpeechEngine-Async-Thread");
        
        this.currentSpeechThread.setDaemon(true);
        this.currentSpeechThread.start();
    }

    /**
     * Fallback ASYNC SPEAK without a callback.
     */
    public synchronized void speak(String text) {
        speak(text, null);
    }

    // --- BLOCKING SPEAKING ---

    /**
     * BLOCKING SPEAK: Plays text synchronously on the calling thread. 
     * Used only for critical state changes (e.g., resume/pause confirmation).
     * @param text The text to be spoken.
     */
    public synchronized void speakBlocking(String text) {
        // 1. Check for speaker access
        if (!AudioResourceManager.requestSpeakerAccess()) {
            gui.updateStatus("Speaker busy (Blocking). Skipping speech.");
            return;
        } 
        
        // 2. Stop any ongoing async speech, as blocking speech takes priority.
        this.stop(); 

        try {
            this.speakBlockingInternal(text);
        } catch (Exception e) {
            System.err.println("[SpeechEngine] Error during blocking speech: " + e.getMessage());
        } finally {
            // 3. Critical: Always release the speaker resource
            AudioResourceManager.releaseSpeaker();
        }
    }

    /**
     * Internal method containing the actual voice synthesis call.
     */
    public void speakBlockingInternal(String text) {
        System.out.println("[SpeechEngine-Internal]: Speaking: " + text);
        try {
            // The main call to the FreeTTS voice
            this.voice.speak(text);
        } catch (IllegalStateException e) {
            // Check for the known "output queue closed" error which happens during shutdown
            if (e.getMessage() == null || !e.getMessage().contains("output queue closed")) {
                throw e; // Re-throw other unexpected IllegalStateExceptions
            }

            gui.updateStatus("[SpeechEngine]: Attempted speech after shutdown. Gracefully skipping.");
        }
    }

    // --- CONTROL METHODS ---

    /**
     * Safely stops the currently running speech thread (if any).
     */
    public synchronized void stop() {
        // Safely interrupt and wait briefly for the current speech thread to finish.
        if (this.currentSpeechThread != null && this.currentSpeechThread.isAlive()) {
            // Analogy: Telling the singing bird to immediately stop its song 🛑.
            this.currentSpeechThread.interrupt();
            try {
                // Wait a moment for the thread to recognize the interrupt and close gracefully
                this.currentSpeechThread.join(300); 
            } catch (InterruptedException e) {
                // Restore the interrupted status
                Thread.currentThread().interrupt(); 
            }
            this.currentSpeechThread = null;
        }
    }

    /**
     * Releases all resources and shuts down the speech engine completely.
     */
    public void shutdown() {
        this.stop(); // Stop any pending speech first
        AudioResourceManager.releaseSpeaker(); // Ensure the resource lock is cleared
        
        if (this.voice != null) {
            try {
                // Deallocate the voice to release system resources (audio ports/memory)
                this.voice.deallocate();
                gui.updateStatus("[SpeechEngine] Voice deallocated.");
            } catch (Throwable t) {
                gui.updateStatus("[SpeechEngine] Warning during deallocation: " + t.getMessage());
            }
        }
    }
}