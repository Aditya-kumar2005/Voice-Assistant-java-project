package com.friend.friend;

import java.awt.AWTException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.SwingUtilities;

/**
 * Friend - Main application entrypoint (Swing).
 *
 * This file links all components: GUI, TTS, Recognizer, Dispatcher, and Command Groups.
 */
public class Friend {

    public static void main(String[] args) {
        // Ensure Swing UI initializes on the Event Dispatch Thread (EDT).
        SwingUtilities.invokeLater(() -> {
            SpeechEngine tts = null;
            EchoPilotRecognizer recognizer = null;

            try {
                System.out.println("--- EchoPilot Startup ---");
                
                // 1. Initialize core components
                tts = new SpeechEngine();
                CommandDispatcher dispatcher = new CommandDispatcher(tts);

                // 2. Initialize and show the GUI
                // NOTE: EchoPilotGUI.java must be implemented.
                EchoPilotGUI gui = new EchoPilotGUI();
                gui.setVisible(true);

                // 3. Initialize the Recognizer and link to Dispatcher
                recognizer = new EchoPilotRecognizer(dispatcher, gui, tts);
                dispatcher.setRecognizer(recognizer);

                // 4. Initialize command map and register ALL commands (Crucial step)
                Map<String, Runnable> commandMap = new HashMap<>();

                // Instantiate all command groups. Use a single try/catch for cleaner startup.
                try {
                    new SystemCommands(commandMap, tts, recognizer);
                    // NOTE: AppCommands.java must be implemented, even if empty.
                    new AppCommands(commandMap); 
                    new FolderCommands(commandMap);
                    new MediaCommands(commandMap, tts, recognizer, gui);
                    new LifecycleCommands(commandMap, recognizer);
                } catch (Throwable t) {
                    System.err.println("[Friend] FATAL: Failed to initialize a Command Group. Check dependencies (e.g., FreeTTS): " + t.getMessage());
                    throw t;
                }

                // Pass the fully built command map to the dispatcher
                dispatcher.registerCommands(commandMap);

                // 5. Define Shutdown Action
                final EchoPilotRecognizer finalRecognizer = recognizer;
                final SpeechEngine finalTts = tts;
                
                Runnable shutdownAction = () -> {
                    System.out.println("Shutting down...");
                    if (finalRecognizer != null) finalRecognizer.stop();
                    if (finalTts != null) finalTts.shutdown();
                    System.exit(0);
                };

                // 6. Connect GUI buttons
                gui.setRecognizerControls(finalRecognizer::resume, finalRecognizer::pause, finalRecognizer.isListening());

                // 7. Initialize the Tray Controller
                try {
                    new TrayController(recognizer, shutdownAction); // TrayController requires the shutdownAction
                } catch (AWTException e) {
                    System.err.println("[Friend] Warning: System Tray is not supported or failed to initialize.");
                }

                // 8. Add shutdown hook for graceful exit
                Runtime.getRuntime().addShutdownHook(new Thread(shutdownAction, "Shutdown-Hook"));

                // 9. Start the main recognition loop
                recognizer.startListening();
                System.out.println("[Friend] EchoPilot Ready. Running in background.");

            } catch (Exception e) {
                System.err.println("Fatal error during EchoPilot startup. Exiting.");
                e.printStackTrace();
                // Ensure TTS is stopped on error
                if (tts != null) {
                    try { tts.shutdown(); } catch (Throwable t) {}
                }
                System.exit(1);
            }
        });
    }
}