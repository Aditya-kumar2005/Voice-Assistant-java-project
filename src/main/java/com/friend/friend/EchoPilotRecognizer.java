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
    
    // 1. GRAMMAR MODE (For Wake Word and Commands)
    private static final String COMMAND_DICT_PATH = "resource:/grammars/commands.dict";
    private static final String GRAMMAR_PATH = "resource:/grammars"; // Path to JSGF files
    private static final String GRAMMAR_NAME = "commands";
    
    // 2. LANGUAGE MODEL MODE (For dictation, custom sentences)
    private static final String LANGUAGE_MODEL_PATH = "resource:/grammars/custom_sentences.lm"; // Your custom LM file
    
    // --- Instance Variables ---
    private LiveSpeechRecognizer recognizer;
    private final CommandDispatcher dispatcher;
    private final WebGui gui;
    private final SpeechEngine tts;
    
    private volatile boolean listening = false; // Is the app actively processing commands? (Wake Word Mode: false, Active Mode: true)
    private volatile boolean running = true; // Is the main recognition thread running?
    private volatile boolean isRecognizing = false; // Is the underlying CMU Sphinx engine started? (i.e., startRecognition() was called)
    private volatile boolean isGrammarMode = true; // Current recognition mode
    
    private Thread recognitionThread;
    private final ReentrantLock recognizerLock = new ReentrantLock(); // Lock for state changes
    
    // Inactivity Monitor Variables
    private long lastCommandTime = System.currentTimeMillis();
    private static final long INACTIVITY_TIMEOUT = 120000L; // 2 minutes
    private static final long TIMER_INTERVAL = 1000L; // Check every second
    private Timer inactivityTimer;

    // --- Constructor ---
    public EchoPilotRecognizer(CommandDispatcher dispatcher, WebGui gui, SpeechEngine tts) throws Exception {
        this.dispatcher = dispatcher;
        this.gui = gui;
        this.tts = tts;
        
        // Start in Grammar Mode (Wake Word)
        Configuration config = createGrammarConfiguration();
        this.recognizer = new LiveSpeechRecognizer(config);
    }
    
    // --- Configuration Methods ---
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
    
    // --- Monitor Setup ---
    private void setupInactivityMonitor() {
        // Story: This is the timer that makes sure the parrot 🦜 doesn't stay awake too long.
        if (inactivityTimer != null) {
            inactivityTimer.cancel();
        }
        inactivityTimer = new Timer(true);
        inactivityTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // If actively listening AND we've been quiet for too long
                if (listening && (System.currentTimeMillis() - lastCommandTime > INACTIVITY_TIMEOUT)) {
                    // We call the full pause to speak and update GUI
                    internalPause(false); 
                }
            }
        }, TIMER_INTERVAL, TIMER_INTERVAL);
    }

    // --- Main Recognition Loop ---

    /**
     * Starts the main recognition thread loop.
     * Story: The elephant 🐘 starts marching and listening for sounds.
     */
    public void startListening() {
        setupInactivityMonitor();
        safestStopRecognition();
        
        recognitionThread = new Thread(() -> {
            try {
                // Initial start of the underlying engine
                recognizer.startRecognition(true); 
                isRecognizing = true;
                listening = false; // Start in Wake Word Mode
                gui.updateStatus("Background Mode Ready (Say 'My friend' to wake)");
                
                while(running) {
                    // This call BLOCKS until a result is available or stopRecognition() is called.
                    String command = recognizer.getResult().getHypothesis();
                    String normalizedCommand = command != null ? command.toLowerCase().trim() : "";
                    
                    if (normalizedCommand.isEmpty()) {
                        continue; // Ignore silent results
                    }
                    
                    if (listening) {
                        // --- ACTIVE LISTENING MODE ---
                        lastCommandTime = System.currentTimeMillis(); 
                        gui.updateStatus("You said: " + normalizedCommand);
                        dispatcher.dispatch(normalizedCommand);
                        
                    } else {
                        // --- WAKE WORD MODE ---
                        // FIX: Added a check for the current mode, though in this loop, it's assumed Grammar mode.
                        if (isGrammarMode && (normalizedCommand.contains("my friend") || normalizedCommand.contains("wake up")||normalizedCommand.contains("wake up my friend")||normalizedCommand.contains("friend")||normalizedCommand.contains("wake"))) {
                            gui.updateStatus("[Recognizer]: Wake word detected: " + normalizedCommand);
                            resume(); // Switch state to active listening
                        } 
                    }
                } // End while(running)
                
            } catch (Exception var32) {
                // This catch block handles exceptions like device errors or null recognizer state
                gui.updateStatus("Fatal Error");
                tts.speakBlocking("A fatal error occurred in the voice recognition system");
                var32.printStackTrace();
            } finally {
                // Ensure the engine is stopped even if an error occurs
                safestStopRecognition();
            }
        }, "EchoPilotRecognizer-Thread");
        
        recognitionThread.setDaemon(true); // Allow application exit
        recognitionThread.start();
    }
    
    // --- Configuration Switching ---

    /**
     * Safely stops the current recognition and re-initializes the recognizer 
     * with a new grammar/language model configuration.
     */
    private void switchConfiguration(boolean useGrammarMode) throws Exception {
        recognizerLock.lock();
        try {
            if (isGrammarMode == useGrammarMode) return; // No change needed
            
            // 1. Stop the currently running engine (CRITICAL: before re-init)
            safestStopRecognition();
            
            // 2. Re-initialize the LiveSpeechRecognizer instance with the new config
            Configuration newConfig = useGrammarMode ? createGrammarConfiguration() : createLanguageModelConfiguration();
            recognizer = new LiveSpeechRecognizer(newConfig);
            isGrammarMode = useGrammarMode;

            gui.updateStatus("[Recognizer]: Switched search to " + (useGrammarMode ? "GRAMMAR" : "LANGUAGE MODEL"));

            // 3. Restart recognition if the thread is still running
            if (running && recognizer != null) {
                recognizer.startRecognition(true); 
                isRecognizing = true;
            }
        } finally {
            recognizerLock.unlock();
        }
    }
    
    public void switchToLanguageModel() throws Exception {
        switchConfiguration(false);
        // Do not auto-resume listening here; let the command handle resume() if needed.
        gui.updateStatus("Switched to Language Model Mode (dictation mode)");
    }

    public void switchToGrammar() throws Exception {
        switchConfiguration(true);
        // Do not auto-resume listening here; let the command handle resume() if needed.
        gui.updateStatus("Switched to Grammar Mode (command mode)");
    }
    
    // --- Special Recognition Mode ---

    /**
     * Executes recognition for a single phrase (for confirmation dialogs).
     * Story: A quick game of "Say Yes or No". 
     */
    public String listenOnce() {
        recognizerLock.lock();
        String result = "";
        boolean wasListening = listening; // Remember the state before listenOnce
        
        try {
            // 1. Stop the ongoing recognition stream
            safestStopRecognition();
            
            gui.updateStatus("Are you sure? Say YES or NO:");
            
            // 2. Start a fresh, non-continuos recognition stream
            recognizer.startRecognition(false); // false means recognition stops after the first phrase
            String hypothesis = this.recognizer.getResult().getHypothesis();
            recognizer.stopRecognition();
            
            result = hypothesis != null ? hypothesis.toLowerCase().trim() : "";
            gui.updateStatus("Confirmation received: " + result);
            
            // 3. Restart the continuous loop after listenOnce finishes
            if (running && !isRecognizing) {
                 startRecognition(); // Re-start the continuous engine
            }
            listening = wasListening; // Restore the listening state
            
        } catch (Exception e) {
            gui.updateStatus("ListenOnce failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            recognizerLock.unlock();
        }
        return result;
    }
    
    // --- Pause/Resume/Stop Controls ---

    /**
     * Internal pause logic used by inactivity timer and command dispatcher.
     * @param silent If true, skips the spoken confirmation message (used by inactivity monitor).
     */
    private void internalPause(boolean silent) {
        listening = false; // Stop processing results in the main thread
        
        gui.updateStatus("Going to background mode");
        System.out.println("Listening paused.");
        
        if (!silent) {
            tts.speak("Going to background mode.");
        }
    }
    
    public void pause() {
        internalPause(false); // Non-silent pause
    }

    /**
     * Resumes command processing (listening) and resets the inactivity timer.
     * Story: Waking the parrot up! 
     */
    public boolean resume() {
        listening = true;
        lastCommandTime = System.currentTimeMillis();
        System.out.println("Listening resumed My friend!");
        gui.updateStatus("###=======Bro, I am ready resuming listening ===###");
        
        // This is blocking, but often fine for a confirmation like this.
        tts.speakBlocking("Resuming listening."); 
        return true;
    }
    
    public void stop() {
        running = false; // Stop the while(running) loop
        listening = false;
        
        if (inactivityTimer != null) {
            inactivityTimer.cancel();
        }

        // Interrupt and join the recognition thread
        if (recognitionThread != null && recognitionThread.isAlive()) {
            recognitionThread.interrupt();
            try {
                recognitionThread.join(1000L);
            } catch (InterruptedException var2) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Safely stop the engine and clear resources
        safestStopRecognition();
        recognizer = null;
        System.out.println("Stopped");
    }

    /**
     * Safely starts the underlying CMU Sphinx recognition stream.
     */
    public void startRecognition() {
        recognizerLock.lock();
        try {
            // If the thread is running, the engine should be running already.
            // This is primarily used to re-start the engine after a stop.
            if (isRecognizing) return; 
            
            if (recognizer == null) {
                Configuration config = isGrammarMode ? createGrammarConfiguration() : createLanguageModelConfiguration();
                recognizer = new LiveSpeechRecognizer(config);
            }
            
            recognizer.startRecognition(true);
            isRecognizing = true;
            System.out.println("Getting ready for you");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            recognizerLock.unlock();
        }
    }
    
    /**
     * Safely stops the recognition engine by interrupting the getResult() call.
     * This is the "soft" stop necessary for LiveSpeechRecognizer.
     */
    public void safestStopRecognition() {
        if (isRecognizing) {
            recognizerLock.lock();
            try {
                if (recognizer != null) { 
                    recognizer.stopRecognition();
                    gui.updateStatus("Recognition engine stopped.");
                }
            } catch (IllegalStateException e) {
                // Occurs if stop is called when not running. Can be ignored.
            } finally {
                isRecognizing = false;
                recognizerLock.unlock();
            }
        }
    }
    
    // --- Public Utility Methods ---

    public boolean isListening(){
        return listening;
    }
    
    public SpeechEngine getSpeechEngine() {
        return this.tts;
    }
    
    /**
     * Releases the mic resource and stops the underlying engine (hard stop).
     */
    public void releaseMicAndStopRecognition() {
        // 1. Stop processing in the recognition thread
        listening = false;
        gui.updateStatus("Mic Released (Waiting for External App)");

        // 2. Call the underlying method to stop the recognizer stream and release hardware
        safestStopRecognition();
    }

    /**
     * Public method to re-acquire the mic resource and resume command listening. 
     */
    public void startRecognitionAndResume() {
        // 1. Re-acquire the microphone resource and start the recognition loop
        startRecognition();
        
        // 2. Set the application back to the active listening state
        resume();
        gui.updateStatus("I am listening again");
        tts.speakBlocking("Mic re-acquired. I'm listening again.");
    }
}