package com.friend.friend;

import java.awt.AWTException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.SwingUtilities;

public class Friend {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FileSearcher fileSearcher = null;
            GoogleSearcher googleSearcher = null;
            SpeechEngine tts = null;
            EchoPilotRecognizer recognizer = null;
            EchoPilotGUI gui = null;
            CommandDispatcher dispatcher = null;
            SystemCommands systemCommands = null;

            try {
                System.out.println("--- EchoPilot Startup ---");
                
                // 1. Initialize GUI
                gui = new EchoPilotGUI();
                gui.setVisible(true); 
    
                // 2. Initialize core services (TTS, Searchers)
                tts = new SpeechEngine();
                fileSearcher = new FileSearcher();
                googleSearcher = new GoogleSearcher();
                
                // 3. Initialize Command Map
                Map<String, Runnable> commandMap = new HashMap<>();
                
                // 4. Initialize Dispatcher and Recognizer with placeholder dependencies.
                // We use null for the CommandMap and SystemCommands for now.
                dispatcher = new CommandDispatcher(tts, null); 
                
                // The Recognizer needs the dispatcher, even if it's incomplete.
                recognizer = new EchoPilotRecognizer(dispatcher, gui, tts); 
                
                // 5. Inject the missing link into the Dispatcher to resolve the circle.
                // Now the Dispatcher knows the Recognizer!
                dispatcher.setRecognizer(recognizer); 
                
                // 6. Initialize SystemCommands (SystemCommands needs the finished Recognizer/Searchers)
                systemCommands = new SystemCommands(commandMap, tts, recognizer, googleSearcher, fileSearcher);
                
                // 7. Inject the finished SystemCommands back into the Dispatcher
                // (Assuming a setSystemCommands method exists or is handled by a Command Map).
                // NOTE: We MUST re-register the dispatcher with the correct search logic.
                // For simplicity, let's assume the CommandDispatcher has a setter for SystemCommands.
                dispatcher.setSystemCommands(systemCommands);

                // 8. Instantiate command groups and register their commands
                try {
                    new AppCommands(commandMap); 
                    
                    // FIX 1: Use the final 'dispatcher' instance instead of 'gui'
                    new MediaCommands(commandMap, tts, recognizer, dispatcher); 
                    
                    new LifecycleCommands(commandMap, recognizer);
                    new FolderCommands(commandMap, fileSearcher, dispatcher); 
                } catch (Throwable t) {
                    System.err.println("[Friend] FATAL: Failed to initialize a Command Group. Check dependencies: " + t.getMessage());
                    throw t;
                }

                // 9. Finalize Dispatcher and Command Map
                dispatcher.registerCommands(commandMap);
                
                // 10. Define Shutdown Action
                final EchoPilotRecognizer finalRecognizer = recognizer;
                final SpeechEngine finalTts = tts;
                
                Runnable shutdownAction = () -> {
                    System.out.println("Shutting down...");
                    if (finalRecognizer != null) { 
                        try { finalRecognizer.stop(); } catch (Throwable t) { System.err.println("Recognizer stop failed: " + t.getMessage()); } 
                    }
                    if (finalTts != null) { 
                        try { finalTts.shutdown(); } catch (Throwable t) { System.err.println("TTS shutdown failed: " + t.getMessage()); } 
                    }
                    System.exit(0);
                };

                // 11. Connect GUI controls
                gui.setRecognizerControls(finalRecognizer::resume, finalRecognizer::pause, finalRecognizer.isListening());

                // 12. Initialize the Tray Controller
                try {
                    new TrayController(recognizer, shutdownAction); 
                } catch (AWTException e) {
                    System.err.println("[Friend] Warning: System Tray is not supported or failed to initialize.");
                }

                // 13. Add shutdown hook for graceful exit
                Runtime.getRuntime().addShutdownHook(new Thread(shutdownAction, "Shutdown-Hook"));

                // 14. Start the main recognition loop
                recognizer.startListening();
                System.out.println("[Friend] EchoPilot Ready. Running in background.");

            } catch (Exception e) {
                System.err.println("Fatal error during EchoPilot startup. Exiting.");
                e.printStackTrace();
                // Safe shutdown on initialization failure
                if (tts != null) { try { tts.shutdown(); } catch (Throwable t) {} }
                if (recognizer != null) { try { recognizer.stop(); } catch (Throwable t) {} }
                System.exit(1);
            }
        });
    }
}