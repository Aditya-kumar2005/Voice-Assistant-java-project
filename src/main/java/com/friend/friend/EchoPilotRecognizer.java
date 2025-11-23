package com.friend.friend;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

public class EchoPilotRecognizer {

    private final LiveSpeechRecognizer recognizer;
    private final CommandDispatcher dispatcher;
    private final EchoPilotGUI gui;
    private final SpeechEngine tts;

    private volatile boolean listening = false; 
    private volatile boolean running = true;
    private volatile boolean isSpeaking = false; // NEW: Track speech state
    private Thread recognitionThread;
    
    // Critical lock for protecting concurrent access to the Sphinx recognizer state
    private final ReentrantLock recognizerLock = new ReentrantLock(); 

    private long lastCommandTime = System.currentTimeMillis();
    private static final long INACTIVITY_TIMEOUT = 120_000;
    private static final long TIMER_INTERVAL = 1000;
    
    private Timer inactivityTimer;
    private volatile boolean isRecognizing = false;


    public EchoPilotRecognizer(CommandDispatcher dispatcher, EchoPilotGUI gui, SpeechEngine tts) throws Exception {
        this.dispatcher = dispatcher;
        this.gui = gui;
        this.tts = tts;

        Configuration config = new Configuration();
        config.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        config.setDictionaryPath("resource:/grammars/commands.dict");
        config.setGrammarPath("resource:/grammars");
        config.setGrammarName("commands");
        config.setUseGrammar(true);

        recognizer = new LiveSpeechRecognizer(config);
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
                if (listening && (System.currentTimeMillis() - lastCommandTime > INACTIVITY_TIMEOUT) && !isSpeaking) {
                    internalPause(true); // Silent pause
                }
            }
        }, TIMER_INTERVAL, TIMER_INTERVAL);
    }

    // Runnable to restart recognition engine after TTS is finished
    private final Runnable recognitionRestartCallback = () -> {
        // This is run by the TTS thread when speech completes.
        isSpeaking = false; // Clear the speaking flag
        if (running) {
            startRecognition(); // Restart the LiveSpeechRecognizer engine
        }
    };

    /**
     * Starts the main recognition thread loop.
     */
    public void startListening() {
        setupInactivityMonitor();
        
        recognitionThread = new Thread(() -> {
            // Initial call to startRecognition outside the loop to initialize resources
            // This is protected by the try-catch for the exception, but we should make 
            // the recognizer start call safe.
            try {
                 // Initial recognition start outside the loop (for wake word mode)
                recognizerLock.lock();
                recognizer.startRecognition(true);
                isRecognizing = true;
                recognizerLock.unlock();
                
                gui.updateStatus("Background Mode Ready (Say 'My friend' to wake)"); 
                gui.updateControlButtons(false);
                gui.updateListeningIndicator(false);
                
                while (running) {
                    
                    // --- Handle Speaker Active State Check ---
                    // Wait if the speech engine is currently active
                    if (isSpeaking) {
                        Thread.sleep(100);
                        continue;
                    }
                    
                    // --- Background Mode Wake-Up Check (runs when listening is false) ---
                    if (!listening) {
                        if (AudioResourceManager.requestMicAccess()) {
                            try {
                                recognizerLock.lock();
                                String wakePhrase = recognizer.getResult().getHypothesis();
                                recognizerLock.unlock();
                                
                                String normalizedWakePhrase = wakePhrase != null ? wakePhrase.toLowerCase().trim() : "";
                                
                                if (normalizedWakePhrase.contains("friend")
                                        || normalizedWakePhrase.contains("my friend")
                                        || normalizedWakePhrase.contains("wake up my friend")) {
                                        
                                    System.out.println("[Recognizer]: Wake word detected: " + normalizedWakePhrase);
                                    
                                    // 1. Stop Recognition and release mic
                                    safestStopRecognition();
                                    AudioResourceManager.releaseMic();
                                    
                                    // 2. Resume (non-blocking speak + restart in callback)
                                    resume(); 
                                }
                            } catch (Exception ignored) {
                                // Ignored
                            } finally {
                                AudioResourceManager.releaseMic();
                                if(recognizerLock.isHeldByCurrentThread()) recognizerLock.unlock();
                            }
                        }
                        Thread.sleep(100);
                        continue;
                    }

                    // --- Standard Active Listening Mode (listening = true) ---
                    if (!AudioResourceManager.requestMicAccess()) {
                        gui.updateStatus("Mic busy. Waiting...");
                        Thread.sleep(500);
                        continue;
                    }

                    try {
                        // The blocking call to getResult() is what we interrupt upon command detection
                        recognizerLock.lock();
                        String command = recognizer.getResult().getHypothesis();
                        recognizerLock.unlock();

                        if (command != null && !command.trim().isEmpty()) {
                            lastCommandTime = System.currentTimeMillis();
                            String normalizedCommand = command.toLowerCase().trim();

                            // 1. CRITICAL: FORCE STOP RECOGNITION before any speech or command dispatch
                            safestStopRecognition();
                            AudioResourceManager.releaseMic(); // Release the mic lock immediately
                            
                            // 2. Handle internal state change commands (using non-blocking logic)
                            if (normalizedCommand.equals("start listening") || normalizedCommand.equals("wake up")) {
                                resume();
                                continue; 
                            }
                            if (normalizedCommand.equals("stop listening") || normalizedCommand.equals("sleep") || normalizedCommand.equals("bye") || normalizedCommand.equals("goodbye")) {
                                internalPause(false); // User initiated pause (with speech)
                                continue; 
                            }
                            
                            // 3. Dispatch the command (Dispatcher must handle the non-blocking TTS and callback)
                            gui.updateCommand(command);
                            gui.updateStatus("Executing (Awaiting Response)...");
                            
                            isSpeaking = true; // Set flag to prevent loop from running until callback hits
                            dispatcher.dispatch(command); // Dispatcher calls tts.speak(..., callback)
                            
                        } else {
                            // If no command recognized, release mic and continue loop
                            AudioResourceManager.releaseMic();
                            gui.updateStatus("Listening...");
                        }
                    } catch (Exception e) {
                        System.err.println("[Recognizer Loop Error]: Recognition attempt failed. Continuing loop.");
                        e.printStackTrace();
                        AudioResourceManager.releaseMic();
                        gui.updateStatus("Listening...");
                        if(recognizerLock.isHeldByCurrentThread()) recognizerLock.unlock();
                    }
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                gui.updateStatus("Recognition interrupted");
            } catch (Exception e) {
                gui.updateStatus("Fatal Error");
                // Fallback to non-blocking speak
                tts.speak("A fatal error occurred in the voice recognition system", null); 
                e.printStackTrace();
            } finally {
                safestStopRecognition();
                AudioResourceManager.releaseMic();
            }
        }, "EchoPilotRecognizer-Thread");

        recognitionThread.start();
    }
    
    /**
     * Executes recognition for a single phrase (for confirmation dialogs).
     */
    public String listenOnce() {
        // Lock protects against concurrent GUI/loop access
        recognizerLock.lock();
        safestStopRecognition(); 
        AudioResourceManager.releaseMic();
        try {
            if (!AudioResourceManager.requestMicAccess()) {
                gui.updateStatus("ListenOnce failed: Mic busy");
                return "";
            }
            
            recognizer.startRecognition(false);
            Thread.sleep(500); // Small delay needed here too for allocation
            String result = recognizer.getResult().getHypothesis();
            recognizer.stopRecognition();
            
            startRecognition(); // Resume the main continuous recognition

            return result != null ? result : "";
        } catch (Exception e) {
            gui.updateStatus("ListenOnce failed");
            e.printStackTrace();
            return "";
        } finally {
            AudioResourceManager.releaseMic();
            recognizerLock.unlock();
        }
    }
    
    public void pause() {
        // External control methods must lock
        recognizerLock.lock();
        try {
            internalPause(false); // Default to pause with speech
        } finally {
            recognizerLock.unlock();
        }
    }
    
    /**
     * Internal pause logic using non-blocking speech and callback for restart sync.
     * @param silent If true, skips the spoken confirmation message (used by inactivity monitor).
     */
    private void internalPause(boolean silent) {
        // 1. Stop engine to interrupt getResult()
        // Lock should be held by calling method (pause or internal loop)
        safestStopRecognition();
        
        // 2. Set speaking state to prevent immediate loop restart
        isSpeaking = true;
        
        // 3. Define callback to handle post-speech cleanup and state change
        Runnable stateTransitionCallback = () -> {
            listening = false;
            isSpeaking = false;
            gui.updateStatus("Running in Background (Paused)");
            gui.updateControlButtons(false); 
            gui.updateListeningIndicator(false);
            recognitionRestartCallback.run(); // Restart engine for wake-word mode
        };
        
        gui.updateStatus("Going to sleep...");
        gui.updateListeningIndicator(false); 

        if (silent) {
            stateTransitionCallback.run();
        } else {
            // Speak, and let the callback handle state transition
            tts.speak("Going to background mode. Say, 'My friend' to wake me up.", stateTransitionCallback);
        }
    }

    public void resume() {
        // External control methods must lock
        recognizerLock.lock();
        try {
            // 1. Stop engine to interrupt any current loop state
            safestStopRecognition();
            
            // 2. Set speaking state
            isSpeaking = true;
            
            // 3. Define callback to handle post-speech cleanup and state change
            Runnable stateTransitionCallback = () -> {
                listening = true;
                isSpeaking = false;
                lastCommandTime = System.currentTimeMillis();
                gui.updateStatus("Listening...");
                gui.updateControlButtons(true);
                gui.updateListeningIndicator(true);
                recognitionRestartCallback.run(); // Restart engine for active listening
            };
            
            gui.updateStatus("Resuming...");
            gui.updateListeningIndicator(true); 
            
            // Speak, and let the callback handle state transition
            tts.speak("Resuming listening.", stateTransitionCallback);
        } finally {
            recognizerLock.unlock();
        }
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
                recognitionThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        // Lock here for final shutdown
        recognizerLock.lock();
        try {
            safestStopRecognition();
        } finally {
            recognizerLock.unlock();
        }
        gui.updateStatus("Stopped");
    }
    
    /**
     * Public access to the recognition restart callback for the CommandDispatcher to use 
     * when its response TTS completes.
     */
    public Runnable getRecognitionRestartCallback() {
        return recognitionRestartCallback;
    }

    public void startRecognition() {
        // Lock here to ensure mutual exclusion when changing Sphinx state
        recognizerLock.lock();
        try {
            recognizer.startRecognition(true);
            isRecognizing = true;
            System.out.println("Recognition engine restarted.");
            // Wait briefly to allow the underlying recognition resources to fully load/allocate
            Thread.sleep(500); 
        } catch (IllegalStateException e) {
            // This is acceptable if the engine was already started (but it shouldn't be with the lock)
            System.out.println("Recognition engine start called, but already running.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            recognizerLock.unlock();
        }
    }

    public void safestStopRecognition() {
        // Lock here to ensure mutual exclusion when changing Sphinx state
        recognizerLock.lock();
        try {
            if (isRecognizing) {
                System.out.println("Attempting to stop recognition engine...");
                // Note: The recognizer.stopRecognition() call will internally try to deallocate 
                // when in the wrong state (e.g., RECOGNIZING), causing the exception.
                // We trust the `isRecognizing` flag for the main thread logic, 
                // but rely on the lock to prevent concurrency issues.
                recognizer.stopRecognition();
                isRecognizing = false;
                System.out.println("Recognition engine stopped.");
            } else {
                 System.out.println("Recognition engine was not marked as recognizing. Skip stop call.");
            }
        } catch (IllegalStateException e) {
            // Catch the specific exception (Expected state READY actual state RECOGNIZING)
            System.err.println("CRITICAL WARNING: IllegalStateException during stopRecognition. State likely RECOGNIZING/ALLOCATING.");
            e.printStackTrace();
            // We assume the engine is now stopped/broken and reset the flag.
            isRecognizing = false;
            gui.updateStatus("Recognizer State Error - Restart Recommended.");
        } catch (Exception e) {
            gui.updateStatus("Failed to stop recognizer engine");
            e.printStackTrace();
        } finally {
            recognizerLock.unlock();
        }
    }
    public boolean isListening(){
        return listening;
    }
}