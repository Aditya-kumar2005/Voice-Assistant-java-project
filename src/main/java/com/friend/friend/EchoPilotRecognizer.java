package com.friend.friend;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

public class EchoPilotRecognizer {

   // --- Configuration Constants ---
    private static final String ACOUSTIC_MODEL_PATH = "resource:/edu/cmu/sphinx/models/en-us/en-us";
    
    // 1. GRAMMAR MODE (For Wake Word and Commands like "Change to Language Model")
   // private static final String COMMAND_DICT_PATH = "resource:/grammars/commands.dict";
    private static final String COMMAND_DICT_PATH = "resource:/grammars/test.dict"; // Assumed Dict for ALL words
    private static final String GRAMMAR_PATH = "resource:/grammars"; // Path to JSGF files
    // private static final String GRAMMAR_NAME = "commands";
    private static final String GRAMMAR_NAME = "test"; // The base JSGF grammar file name (e.g., commands.gram)
    
    // 2. LANGUAGE MODEL MODE (For dictation, music names, custom sentences)
    private static final String LANGUAGE_MODEL_PATH = "resource:/grammars/custom_sentences.lm"; // Your custom LM file
   private final LiveSpeechRecognizer recognizer;
   private final CommandDispatcher dispatcher;
   private final EchoPilotGUI gui;
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
   private volatile boolean isLanguageMode = false;

   public EchoPilotRecognizer(CommandDispatcher dispatcher, EchoPilotGUI gui, SpeechEngine tts) throws Exception {
      this.dispatcher = dispatcher;
      this.gui = gui;
      this.tts = tts;
      Configuration config = isGrammarMode ? createGrammarConfiguration() : createLanguageModelConfiguration();
      this.recognizer = new LiveSpeechRecognizer(config);
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
                    pause(); // Silent pause
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
            gui.updateStatus("Background Mode Ready (Say 'My friend' to wake)");
            gui.updateControlButtons(false);
            gui.updateListeningIndicator(false);
            while(true) {
               while(running) {
                  String command;
                  String normalizedCommand;
                  if (!listening) {
                     if (AudioResourceManager.requestMicAccess()) {
                        try {
                           command = recognizer.getResult().getHypothesis();
                           normalizedCommand = command != null ? command.toLowerCase().trim() : "";
                           if (normalizedCommand.contains("friend") || normalizedCommand.contains("my friend") || normalizedCommand.contains("wake up my friend")) {
                              System.out.println("[Recognizer]: Wake word detected: " + normalizedCommand);
                              resume();
                           }
                        } catch (Exception var29) {
                        } finally {
                           AudioResourceManager.releaseMic();
                        }
                        }

                     Thread.sleep(100L);
                  } else if (!AudioResourceManager.requestMicAccess()) {
                     gui.updateStatus("Mic busy. Waiting...");
                     Thread.sleep(500L);
                  } else {
                     try {
                        if (AudioResourceManager.isSpeakerActive()) {
                           pause();
                        } else {
                           command = recognizer.getResult().getHypothesis();
                           if (command != null && !command.trim().isEmpty()) {
                              lastCommandTime = System.currentTimeMillis();
                              normalizedCommand = command.toLowerCase().trim();
                              if (!normalizedCommand.equals("start listening") && !normalizedCommand.equals("wake up")) {
                                 if (!normalizedCommand.equals("stop listening") && !normalizedCommand.equals("sleep") && !normalizedCommand.equals("bye") && !normalizedCommand.equals("goodbye")) {
                                    gui.updateCommand(command);
                                    gui.updateStatus("Executing...");
                                    dispatcher.dispatch(command);
                                    gui.updateStatus("Listening...");
                                 } else {
                                    pause();
                                 }
                              } else if (normalizedCommand.equals("change to language model")) {
                            System.out.println("[Recognizer]: COMMAND DETECTED: Switch to LM Mode.");
                            switchToLanguageModel();
                            continue;
                            } else if (normalizedCommand.equals("change to grammar")) {
                            System.out.println("[Recognizer]: COMMAND DETECTED: Switch to Grammar Mode.");
                            switchToGrammar();
                            continue;
                            } 
                              else {
                                 resume();
                              }
                           } else {
                              gui.updateStatus("Listening...");
                           }
                        }
                     } catch (Exception var27) {
                        System.err.println("[Recognizer Loop Error]: Recognition attempt failed. Continuing loop.");
                        var27.printStackTrace();
                        gui.updateStatus("Listening...");
                     } finally {
                        AudioResourceManager.releaseMic();
                     }
                  }
               }

               return;
            }
         } catch (InterruptedException var31) {
            Thread.currentThread().interrupt();
            gui.updateStatus("Recognition interrupted");
         } catch (Exception var32) {
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
            safestStopRecognition(); // Must stop the old engine

            Configuration newConfig = useGrammarMode ? createGrammarConfiguration() : createLanguageModelConfiguration();

            // Re-initialize the LiveSpeechRecognizer instance with the new config
            LiveSpeechRecognizer recognizer = new LiveSpeechRecognizer(newConfig);
            isGrammarMode = useGrammarMode;

            System.out.println("[Recognizer]: Switched search to " + (useGrammarMode ? "GRAMMAR" : "LANGUAGE MODEL"));

            // Restart recognition after switch
            if (running) {
                startRecognition(); 
            }
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

      String var2;
      try {
         safestStopRecognition();
         recognizer.startRecognition(false);
         String result = this.recognizer.getResult().getHypothesis();
         recognizer.stopRecognition();
         startRecognition();
         var2 = result != null ? result : "";
         return var2;
      } catch (Exception var6) {
         gui.updateStatus("ListenOnce failed");
         var6.printStackTrace();
         var2 = "";
      } finally {
         recognizerLock.unlock();
      }

      return var2;
   }
    
    
    public void pause() {
      listening = false;
      gui.updateStatus("Running in Background (Paused)");
      gui.updateControlButtons(false);
      gui.updateListeningIndicator(false);
      tts.speakBlocking("Going to background mode. Say, 'My friend' to wake me up.");
   }

   public void resume() {
      listening = true;
      lastCommandTime = System.currentTimeMillis();
      gui.updateStatus("Listening...");
      gui.updateControlButtons(true);
      gui.updateListeningIndicator(true);
      tts.speakBlocking("Resuming listening.");
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
      gui.updateStatus("Stopped");
   }

   public void startRecognition() {
      recognizer.startRecognition(true);
      isRecognizing = true;
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
        // Use local flag to avoid unnecessary calls to the underlying engine
        if (isRecognizing) {
            try {
                recognizer.stopRecognition();
                isRecognizing = false;
                System.out.println("Recognition engine stopped.");
            } catch (IllegalStateException e) {
                // Occurs if stop is called when not running. Can be ignored.
            }
        }
    }
    
    public boolean isListening(){
        return listening;
    }
}