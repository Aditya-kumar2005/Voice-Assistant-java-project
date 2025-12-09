package com.friend.friend;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import javax.speech.synthesis.*;


public class SpeechEngine {
   private final Voice voice;
   //private Synthesizer synthesizer;
   private Thread currentSpeechThread;
   MergedEchoPilotApp gui;
   public SpeechEngine(MergedEchoPilotApp gui) {
      this.gui=gui;
      System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
      this.voice = VoiceManager.getInstance().getVoice("kevin16");
      if (this.voice != null) {
         this.voice.allocate();
         gui.updateStatus("[SpeechEngine]: Using voice: kevin16");
      } else {
         throw new IllegalStateException("Voice 'kevin16' not found. Check FreeTTS setup.");
      }
   }

   // ASYNC SPEAK (used for command response, accepts a callback)
   public synchronized void speak(String text, Runnable callback) { 
      if (!AudioResourceManager.requestSpeakerAccess()) {
         gui.updateStatus("Speaker busy. Skipping speech.");
         // Execute callback even if speech is skipped, so recognition can resume/continue
         if (callback != null) callback.run();
      } else {
         this.stop();
         this.currentSpeechThread = new Thread(() -> {
            try {
               this.speakBlockingInternal(text);
            } catch (Exception var6) {
               System.err.println("Speech error: " + var6.getMessage());
            } finally {
               AudioResourceManager.releaseSpeaker();
               // Execute callback after speech is finished and speaker resource is released
               if (callback != null) { 
                  callback.run();
               }
            }

         }, "SpeechEngine-Thread");
         this.currentSpeechThread.setDaemon(true);
         this.currentSpeechThread.start();
      }
   }

   // Fallback ASYNC SPEAK without callback
   public synchronized void speak(String text) {
        speak(text, null);
   }

   // BLOCKING SPEAK (used for state changes like pause/resume)
   public synchronized void speakBlocking(String text) {
      if (!AudioResourceManager.requestSpeakerAccess()) {
         gui.updateStatus("Speaker busy (Blocking). Skipping speech.");
      } else {
         try {
            this.speakBlockingInternal(text);
         } catch (Exception var6) {
            System.err.println("Blocking speech error: " + var6.getMessage());
         } finally {
            AudioResourceManager.releaseSpeaker();
         }

      }
   }

   public void speakBlockingInternal(String text) {
      System.out.println("[SpeechEngine-Blocking]: Speaking: " + text);
      try {
         this.voice.speak(text);
      } catch (IllegalStateException var3) {
         if (var3.getMessage() == null || !var3.getMessage().contains("output queue closed")) {
            throw var3;
         }

         gui.updateStatus("[SpeechEngine]: Attempted speech after shutdown. Gracefully skipping.");
      }

   }

   public synchronized void stop() {
      // Safely interrupt and wait briefly for the current speech thread to finish.
      // Use short-circuit && to avoid NullPointerException when thread is null.
      if (this.currentSpeechThread != null && this.currentSpeechThread.isAlive()) {
         this.currentSpeechThread.interrupt();
         try {
            this.currentSpeechThread.join(300); // wait up to 300ms
         } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
         }
         this.currentSpeechThread = null;
      }

   }

   public void shutdown() {
    this.stop();
    if (this.voice != null) {
        try {
            this.voice.deallocate();
        } catch (Throwable t) {
            gui.updateStatus("[SpeechEngine] Warning during deallocation: " + t.getMessage());
        }
    }
}
}