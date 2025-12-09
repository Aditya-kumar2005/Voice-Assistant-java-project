package com.friend.friend;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;
import java.lang.InterruptedException;

public class EchoPilotRecognizer {

   // --- Configuration Constants ---
    private static final String ACOUSTIC_MODEL_PATH = "resource:/edu/cmu/sphinx/models/en-us/en-us";
    
    // 1. GRAMMAR MODE (For Wake Word and Commands like "Change to Language Model")
    private static final String COMMAND_DICT_PATH = "resource:/grammars/commands.dict";
    //private static final String COMMAND_DICT_PATH = "resource:/grammars/test.dict"; // Assumed Dict for ALL words
    private static final String GRAMMAR_PATH = "resource:/grammars"; // Path to JSGF files
     private static final String GRAMMAR_NAME = "commands";
   // private static final String GRAMMAR_NAME = "test"; // The base JSGF grammar file name (e.g., commands.gram)
    
    // 2. LANGUAGE MODEL MODE (For dictation, music names, custom sentences)
    private static final String LANGUAGE_MODEL_PATH = "resource:/grammars/custom_sentences.lm"; // Your custom LM file
   private LiveSpeechRecognizer recognizer;
   private final CommandDispatcher dispatcher;
   private final MergedEchoPilotApp gui;
   private final SpeechEngine tts;
   private volatile boolean listening = false;
   private volatile boolean running = true;
   private Thread recognitionThread;
   private final ReentrantLock recognizerLock = new ReentrantLock();
   private long lastCommandTime = System.currentTimeMillis();
   private static final long INACTIVITY_TIMEOUT = 120000L;
   private static final long TIMER_INTERVAL = 1000L;
   private Timer inactivityTimer;
   private volatile boolean isRecognizing = false;
   private volatile boolean isGrammarMode = true;

   public EchoPilotRecognizer(CommandDispatcher dispatcher, MergedEchoPilotApp gui, SpeechEngine tts) throws Exception {
      this.dispatcher = dispatcher;
      this.gui = gui;
      this.tts = tts;
         Configuration config = createGrammarConfiguration();
         this.recognizer = new LiveSpeechRecognizer(config);
         // Ensure recognizer starts in a clean state (not allocated by us yet)
         this.isRecognizing = false;
   }
   private Configuration createGrammarConfiguration() {
        Configuration config = new Configuration();
        config.setAcousticModelPath(ACOUSTIC_MODEL_PATH);
        config.setDictionaryPath(COMMAND_DICT_PATH);
        config.setGrammarPath(GRAMMAR_PATH);
        config.setGrammarName(GRAMMAR_NAME);
        config.setUseGrammar(true);
        config.setLanguageModelPath(null); // Ensure LM is disabled
        return config;
    }

    private Configuration createLanguageModelConfiguration() {
        Configuration config = new Configuration();
        config.setAcousticModelPath(ACOUSTIC_MODEL_PATH);
        config.setDictionaryPath(COMMAND_DICT_PATH);
        config.setLanguageModelPath(LANGUAGE_MODEL_PATH);
        config.setUseGrammar(false); 
        config.setGrammarPath(null);
        config.setGrammarName(null);
        return config;
    }
    private void setupInactivityMonitor() {
        if (inactivityTimer != null) {
            inactivityTimer.cancel();
        }
        inactivityTimer = new Timer(true);
        inactivityTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Only pause if not currently speaking
                if (listening && (System.currentTimeMillis() - lastCommandTime > INACTIVITY_TIMEOUT)) {
                  listening=false;
                  //  pause(); // Silent pause
                }
            }
        }, TIMER_INTERVAL, TIMER_INTERVAL);
    }

    /**
     * Starts the main recognition thread loop.
     */
    public void startListening() {
        setupInactivityMonitor();
        
        recognitionThread = new Thread(() -> {
            try {
                recognizer.startRecognition(true); 
                isRecognizing = true;
                listening = false; 
                gui.updateStatus("Background Mode Ready (Say 'My friend' to wake)");
                gui.updateListeningIndicator(false);
                
                while(running) {
                    String command = recognizer.getResult().getHypothesis();
                    String normalizedCommand = command != null ? command.toLowerCase().trim() : "";
                    
                    if (listening) {
                        // --- LISTENING FOR REGULAR COMMANDS ---
                        if (!normalizedCommand.isEmpty()) {
                            lastCommandTime = System.currentTimeMillis(); 
                            gui.updateStatus("You said: " + normalizedCommand);
                            dispatcher.dispatch(normalizedCommand);
                        }
                        
                    } else {
                        // --- LISTENING FOR WAKE WORD (WAKE WORD MODE) ---
                        // 🐛 FIX: Simplified wake word detection
                        if (normalizedCommand.contains("friend") || normalizedCommand.contains("wake up")) {
                            gui.updateStatus("[Recognizer]: Wake word detected: " + normalizedCommand);
                            resume(); // Switch state to active listening
                        } 
                        //else {
                            // Suppress unnecessary logs when waiting for wake word
                            // gui.updateStatus("Waiting for wake word...");
                        //}
                    }
                } // End while(running)
               }
            // } catch (InterruptedException var31) {
            //     Thread.currentThread().interrupt();
            //     gui.updateStatus("Recognition interrupted");
            // } 
            catch (Exception var32) {
                gui.updateStatus("Fatal Error");
                tts.speakBlocking("A fatal error occurred in the voice recognition system");
                var32.printStackTrace();
            } finally {
                safestStopRecognition();
            }
        }, "EchoPilotRecognizer-Thread");
        
        recognitionThread.start();
    }
    
   private void switchConfiguration(boolean useGrammarMode) throws Exception {
      recognizerLock.lock();
      try {
         if (isGrammarMode == useGrammarMode) return;
         // Stop and deallocate the current recognizer to free memory and avoid illegal state transitions
         safestStopRecognition();
         // LiveSpeechRecognizer does not expose a public deallocate of its internals.
         // Rely on stopRecognition() and nulling the reference to allow GC to reclaim resources.
         try {
            recognizer.stopRecognition();
         } catch (Throwable ignored) {
         }

         Configuration newConfig = useGrammarMode ? createGrammarConfiguration() : createLanguageModelConfiguration();

         // Re-initialize the LiveSpeechRecognizer instance with the new config
         recognizer = new LiveSpeechRecognizer(newConfig);
         isGrammarMode = useGrammarMode;

         gui.updateStatus("[Recognizer]: Switched search to " + (useGrammarMode ? "GRAMMAR" : "LANGUAGE MODEL"));

         // Restart recognition after switch (only if requested)
         if (running && recognizer != null) {
                 recognizer.startRecognition(true); // Restart the background recognition
                 isRecognizing = true;
            }
         // if (running) {
         //    startRecognition();
         // }
      } finally {
         recognizerLock.unlock();
      }
    }
   public void switchToLanguageModel() throws Exception {
        switchConfiguration(false);
        gui.updateStatus("Listening (Language Model Mode)...");
    }

    /**
     * Helper to switch back to the Grammar search.
     */
    public void switchToGrammar() throws Exception {
        switchConfiguration(true);
       gui.updateStatus("Listening (Grammar Mode)...");
    }
                
    /**
     * Executes recognition for a single phrase (for confirmation dialogs).
     */
    public String listenOnce() {
      recognizerLock.lock();
      try {
         safestStopRecognition();
         gui.updateStatus("Are you sure :say yes / no :");
         recognizer.startRecognition(false);
         String result = this.recognizer.getResult().getHypothesis();
         recognizer.stopRecognition();
            gui.updateStatus("Listening to your command (for listen Once)...");
            return result != null ? result.toLowerCase().trim() : "";
      } catch (Exception var6) {
         gui.updateStatus("ListenOnce failed");
         var6.printStackTrace();
         return "";
      } finally {
         recognizerLock.unlock();
      }
   }
    
    
    public void pause() {
      listening = false;
      gui.updateStatus("Running in Background (Paused)");
      //gui.updateControlButtons(false);
      gui.updateListeningIndicator(false);
      gui.updateStatus("Going to background mode");
      System.out.println("listening paused ");
      tts.speak("Going to background mode. ");
   }

   public boolean resume() {
      listening = true;
      lastCommandTime = System.currentTimeMillis();
      System.out.println("Listening...");
      gui.updateStatus("###=======Bro ,I am ready resuming listening ===###");
      System.out.println("Listening resumed My friend'");
      //gui.updateControlButtons(true);
      gui.updateListeningIndicator(true);
      tts.speakBlocking("Resuming listening.");
      return true;
   }
    public void stop() {
      running = false;
      listening = false;
      if (inactivityTimer != null) {
         inactivityTimer.cancel();
      }

      if (recognitionThread != null && recognitionThread.isAlive()) {
         recognitionThread.interrupt();

         try {
            recognitionThread.join(1000L);
         } catch (InterruptedException var2) {
            Thread.currentThread().interrupt();
         }
      }
      safestStopRecognition();
      // Clear reference and allow GC. Explicit deallocation isn't available in this API.
      try {
         recognizer.stopRecognition();
      } catch (Throwable ignored) {
      }
      recognizer = null;
      System.out.println("Stopped");
   }

   public void startRecognition() {
      recognizerLock.lock();
      try {
         if (isRecognizing) return; // already running
         if (recognizer == null) {
                Configuration config = isGrammarMode ? createGrammarConfiguration() : createLanguageModelConfiguration();
                recognizer = new LiveSpeechRecognizer(config);
            }
         recognizer.startRecognition(true);
         isRecognizing = true;
         System.out.println("Getting ready for you");
      }catch (Exception e) {
             e.printStackTrace();
         } finally {
         recognizerLock.unlock();
      }
   }
    /**
     * Internal pause logic using non-blocking speech and callback for restart sync.
     * @param silent If true, skips the spoken confirmation message (used by inactivity monitor).
     */
    private void internalPause() {
        // 1. Stop engine to interrupt getResult()
        pause();
    }


    /**
     * Safely stops the recognition engine by interrupting the getResult() call.
     * FIX APPLIED: Removed the incorrect call to recognizer.deallocate().
     */
    public void safestStopRecognition() {
      //   // Use local flag to avoid unnecessary calls to the underlying engine
      //   if (isRecognizing) {
      //       try {
      //       recognizer.stopRecognition();
      //       // Underlying deallocate API not available; rely on stopRecognition() above.
      //       isRecognizing = false;
      //       gui.updateStatus("Recognition stopped my friend.");
      //       } catch (IllegalStateException e) {
      //           // Occurs if stop is called when not running. Can be ignored.
      //       }
      //   }
      if (isRecognizing) {
             recognizerLock.lock();
             try {
                // If recognizer is null (after full stop()), skip this
                if (recognizer != null) { 
                    recognizer.stopRecognition();
                    gui.updateStatus("Recognition stopped my friend.");
                }
             } catch (IllegalStateException e) {
                 // Occurs if stop is called when not running. Can be ignored.
             } finally {
                 isRecognizing = false;
                 recognizerLock.unlock();
             }
         }
    }
    
    public boolean isListening(){
        return listening;
    }
    
    /**
     * Returns the SpeechEngine for friendly response generation.
     */
    public SpeechEngine getSpeechEngine() {
        return this.tts;
    }
    public void releaseMicAndStopRecognition() {
        // 1. Internal state change to stop processing in the recognition thread
        listening = false;
        gui.updateStatus("Mic Released (Waiting for External App)");
        gui.updateListeningIndicator(false);

        // 2. Call the underlying method to stop the recognizer stream and release hardware
        safestStopRecognition();
        
        // 3. Optional: Speak a quick, non-blocking message (if desired)
        // tts.speak("I am releasing the microphone now."); 
    }

    /**
     * Public method to re-acquire the mic resource after it was released by 
     * releaseMicAndStopRecognition(), and resume normal command listening. This is the "hard" start.
     * * Story: The other animal is done drinking, and you step back up to the water bowl.
     */
     public void startRecognitionAndResume() {
   //      recognizerLock.lock();
   //      try {
   //          // 1. Re-allocate the microphone resource and start the recognition loop
   //          if (!isRecognizing) {
   //              startRecognition();
   //          }
   //      } finally {
   //          recognizerLock.unlock();
   //      }
        
        // 2. Set the application back to the active listening state
        startRecognition();
        resume();
        gui.updateStatus("I am listening again");
        tts.speakBlocking("Mic re-acquired. I'm listening again.");
        
    }
}