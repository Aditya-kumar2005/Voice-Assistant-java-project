package com.friend.friend;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

public class SpeechEngine {

    private final Voice voice;
    private final AudioResourceManager audioManager = new AudioResourceManager();

    public SpeechEngine() {
        System.setProperty("freetts.voices",
                "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");

        voice = VoiceManager.getInstance().getVoice("kevin16");
        
        if (voice == null) {
            throw new IllegalStateException("Required voice 'kevin16' not found. Check FreeTTS libraries and system properties.");
        }

        voice.allocate();
        System.out.println("[SpeechEngine]: Using voice: " + voice.getName());
    }

    public void speak(String text, Runnable callback) {
        if (text == null || text.trim().isEmpty()) {
            if (callback != null) callback.run();
            return;
        }

        Thread speakThread = new Thread(() -> {
            if (!audioManager.requestSpeakerAccess()) {
                System.err.println("[SpeechEngine]: Failed to acquire speaker lock. Skipping speech.");
                if (callback != null) callback.run();
                return;
            }
            
            try {
                System.out.println("[SpeechEngine]: Speaking: " + text);
                voice.speak(text);
                
                // Increase delay for robust OS audio buffer clearance
                try {
                    Thread.sleep(500); 
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }

            } catch (Exception e) {
                System.err.println("[SpeechEngine] Error during speech: " + e.getMessage());
            } finally {
                // This call now includes the CRITICAL DEFENSIVE FIX to check and release the Mic lock
                audioManager.releaseSpeaker(); 
                if (callback != null) {
                    callback.run(); 
                }
            }
        }, "SpeechEngine-TTS-Thread");

        speakThread.start();
    }

    public void speakInternal(String text) {
        if (text == null || text.trim().isEmpty()) return;
        
        if (!audioManager.requestSpeakerAccess()) {
            System.err.println("[SpeechEngine]: Failed to acquire speaker lock for internal speech.");
            return;
        }
        
        try {
            System.out.println("[SpeechEngine-Blocking]: Speaking: " + text);
            voice.speak(text);
            
            // Minimal delay after blocking speech
            Thread.sleep(200); 
        } catch (Exception e) {
            System.err.println("[SpeechEngine-Blocking] Error: " + e.getMessage());
        } finally {
            audioManager.releaseSpeaker(); // This now includes the defensive Mic check
        }
    }

    public void shutdown() {
        if (voice != null) {
            voice.deallocate();
        }
    }
}