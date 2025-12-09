package com.friend.friend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.AWTException;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.swing.SwingUtilities; 
import javafx.application.Platform;

public class Friend {
    private static final Logger logger = LoggerFactory.getLogger(Friend.class);
    private static final String LOG_DIRECTORY_PATH = "./logs/";
    
    // Using a standard main method to ensure the application starts correctly
    public static void main(String[] args) {
        // Story Step 1: Clear old notes.
        clearLogsOnStartup();
        // Story Step 2: Start the whole brain setup!
        initializeAndStartBrain(args);
    }
    
    /**
     * Clears all existing log files from the log directory on startup.
     * This is like taking all the old notes out of a notebook before starting a new project.
     */
    public static void clearLogsOnStartup() {
        File logDirectory = new File(LOG_DIRECTORY_PATH);
        // Story Step 1: Check if the log directory (the "log file box") exists
        if (logDirectory.exists() && logDirectory.isDirectory()) {
            System.out.println("[Log Cleanup] The log directory was found. Starting cleanup...");
            // Story Step 2: Get all the files (the "old notes") inside the box
            File[] logFiles = logDirectory.listFiles();
            if (logFiles != null) {
                // Story Step 3: Go through each old note and throw it away
                for (File file : logFiles) {
                    if (file.isFile()) {
                        boolean deleted = file.delete();
                        if (deleted) {
                            System.out.println("[Log Cleanup] Deleted: " + file.getName());
                        } else {
                            System.err.println("[Log Cleanup] Error: Could not delete " + file.getName());
                        }
                    }
                }
                System.out.println("[Log Cleanup] Cleanup complete! Ready for fresh logs.");
            }
        } else {
            // This is fine! It means the logs folder hasn't been created yet.
            System.out.println("[Log Cleanup] No logs folder found. Ready to start fresh.");
        }
    }
    
    /**
     * Initializes all application components.
     * 1. Launches the GUI on its own thread.
     * 2. Waits for the GUI to be ready (Singleton pattern).
     * 3. Starts the heavy services (TTS, Recognizer) on a background thread.
     */
    public static void initializeAndStartBrain(String[] args) {
        
        // --- 1. START THE GUI WINDOW (The Control Tower) ---
        // Launch the MergedEchoPilotApp on a NEW THREAD so it runs the JavaFX loop
        // without blocking the main initialization logic.
        
        // We ensure the platform is initialized early.
        try {
            Platform.startup(() -> {});
            logger.info("[Main] JavaFX Platform started successfully.");
        } catch (IllegalStateException e) {
            logger.warn("[Main] JavaFX Platform already running or failed startup check.", e);
        }
        
        Thread guiLaunchThread = new Thread(() -> {
            try {
                logger.info("=== Friend Startup ===");
                logger.info("[Main] Java version: " + System.getProperty("java.version"));
                // 💡 CRITICAL FIX: The one and only launch call!
                MergedEchoPilotApp.launch(MergedEchoPilotApp.class, args);
            } catch (Throwable t) {
                logger.error("[LaunchThread] Fatal error during JavaFX launch.", t);
            }
        }, "GUI-Launch-Thread");
        guiLaunchThread.setDaemon(true);
        guiLaunchThread.start();
        
        // --- 2. PAUSE & WAIT FOR GUI INSTANCE (Getting the keys to the Control Tower) ---
        MergedEchoPilotApp localGui = null;
        long startTime = System.currentTimeMillis();
        final long MAX_WAIT_MS = 5000;
        
        while (localGui == null && (System.currentTimeMillis() - startTime) < MAX_WAIT_MS) {
            try {
                // This pause lets the GUI-Launch-Thread run and set the instance.
                Thread.sleep(100); 
                localGui = MergedEchoPilotApp.getInstance();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        if (localGui == null) {
            logger.error("FATAL: Failed to get GUI instance after 5 seconds. Exiting.");
            System.exit(1);
            return;
        }

        // --- 3. INITIALIZE SHARED RESOURCES (The Tool Boxes) ---
        final SettingsManager settingsManager = new SettingsManager();
        final ErrorReporter errorReporter = new ErrorReporter(settingsManager.isEnableTelemetry());
        final MergedEchoPilotApp finalLocalGui = localGui; // Reference to the live GUI instance

        // --- 4. START HEAVY SERVICES (The Crew Chief) ---
        Thread heavyInitThread = new Thread(() -> {
            SpeechEngine tts = null;
            EchoPilotRecognizer recognizer = null;
            CommandDispatcher dispatcher = null;
            HelpSystem helpSystem = null;
            
            try {
                // Initial status updates must be on the JavaFX thread
                Platform.runLater(() -> {
                    finalLocalGui.updateStatus("========(<*>)======="); 
                    finalLocalGui.showLoadingAnimation(); 
                });
                logger.info("[InitThread] Starting heavy service initialization (TTS/Recognizer)...");
                
                // 3. Initialize core service: TTS
                tts = new SpeechEngine(finalLocalGui); // HEAVY LOAD #1
                // Must be runLater, but this is a status update, so let's call updateStatus directly if it handles Platform.runLater internally, as suggested in previous code.
                // Assuming updateStatus handles thread-safety internally based on previous context.
                finalLocalGui.updateStatus("[SpeechEngine]: Using voice: " + settingsManager.getVoiceName()); 
                
                // 4. Initialize Command Map
                Map<String, Runnable> commandMap = new HashMap<>();
                
                // 5. Initialize Dispatcher and Recognizer (Circular Dependency setup)
                dispatcher = new CommandDispatcher(tts, null, finalLocalGui); 
                recognizer = new EchoPilotRecognizer(dispatcher, finalLocalGui, tts); // VERY HEAVY LOAD #2
                dispatcher.setRecognizer(recognizer); 

                // 6. Initialize Command Groups
                // The MergedEchoPilotApp.initializeBridge(settingsManager) call needs to be 
                // outside the heavy init thread if it interacts with JavaFX/Swing bridge initialization 
                // that must happen on a specific thread, but keeping it here is safer 
                // if it's purely logic/data initialization.
                MergedEchoPilotApp.initializeBridge(settingsManager);
                logger.info("[InitThread] Initializing Media Commands...");
                MediaCommands mediaCommands = new MediaCommands(commandMap, tts, recognizer, dispatcher);

                logger.info("[InitThread] Initializing System Commands...");
                SystemCommands systemCommands = new SystemCommands(commandMap, tts, recognizer, mediaCommands);
                dispatcher.setSystemCommands(systemCommands);
                
                logger.info("[InitThread] Initializing other command groups...");
                new AppCommands(commandMap, settingsManager);
                new LifecycleCommands(commandMap, recognizer, finalLocalGui, dispatcher);
                new FolderCommands(commandMap, dispatcher);
                
                // 7. Initialize Help System and register commands
                helpSystem = new HelpSystem(tts, dispatcher);
                final HelpSystem finalHelpSystem = helpSystem;
                
                commandMap.put("help", () -> finalHelpSystem.speakAllCommands());
                commandMap.put("commands", () -> finalHelpSystem.showHelpWindow());
                commandMap.put("dialog settings", MergedEchoPilotApp::showPreferencesDialog);
                commandMap.put("friend settings", MergedEchoPilotApp::showPreferencesDialog);
                commandMap.put("my friend settings", MergedEchoPilotApp::showPreferencesDialog);
                commandMap.put("show dialog settings", MergedEchoPilotApp::showPreferencesDialog);
                commandMap.put("dialog box", MergedEchoPilotApp::showPreferencesDialog);
                commandMap.put("open dialog box", MergedEchoPilotApp::showPreferencesDialog);
                
                // 8. Finalize Dispatcher, Load plugins
                dispatcher.registerCommands(commandMap);
                PluginLoader loader = new PluginLoader(new java.io.File("plugins"));
                loader.loadPlugins(dispatcher);
                
                // 9. Initialize Onboarding Wizard
                OnboardingWizard onboarding = new OnboardingWizard(tts, recognizer, dispatcher, settingsManager);
                onboarding.addHealthCheck(new MicrophoneHealthCheck(new AudioResourceManager()));
                onboarding.addHealthCheck(new TTSHealthCheck(tts));
                onboarding.addHealthCheck(new DiskSpaceHealthCheck());
                onboarding.runIfFirstLaunch();

                // 10. Define Shutdown Action (needs final references)
                final EchoPilotRecognizer finalRecognizer = recognizer;
                final SpeechEngine finalTts = tts;
                final ErrorReporter finalErrorReporter = errorReporter;
                
                Runnable shutdownAction = () -> {
                    logger.info("Shutting down Friend...");
                    if (finalRecognizer != null) {
                        try {
                            finalRecognizer.stop();
                        } catch (Throwable t) {
                            logger.error("Recognizer stop failed", t);
                            finalErrorReporter.reportException("RecognizerShutdown", t);
                        }
                    }
                    if (finalTts != null){
                        try {
                            finalTts.shutdown();
                        } catch (Throwable t){ 
                            logger.error("TTS shutdown failed", t);
                            finalErrorReporter.reportException("TTSShutdown", t);
                        }
                    }
                    System.exit(0);
                };

                // 11. Final GUI Setup (MUST be on the JavaFX thread using Platform.runLater)
                Platform.runLater(() -> {
                    // Connect GUI controls
                    finalLocalGui.setRecognizerControls(finalRecognizer::resume, finalRecognizer::pause, finalRecognizer::startRecognitionAndResume, finalRecognizer::releaseMicAndStopRecognition, finalRecognizer.isListening());
                
                    // Initialize Tray Controller
                    try {
                        new TrayController(finalRecognizer, shutdownAction);
                    } catch (AWTException e) {
                        logger.warn("System Tray not supported or failed to initialize", e);
                    }

                    // Add shutdown hook
                    Runtime.getRuntime().addShutdownHook(new Thread(shutdownAction, "Shutdown-Hook"));

                    // Hide loading animation and show final status
                    finalLocalGui.hideLoadingAnimation();
                    finalLocalGui.updateStatus("System Ready: Listening for commands.");
                });
                
                // 12. Start the recognizer loop (long-running background task)
                finalRecognizer.startListening();
                logger.info("Friend is ready. Running in background.");

            } catch (Exception e) {
                logger.error("[InitThread] Fatal error during startup", e);
                errorReporter.reportException("FriendStartup", e);
                System.exit(1); 
            }
        }, "Heavy-Init-Thread");
        
        heavyInitThread.setDaemon(true);
        heavyInitThread.start(); // Start the crew!
    }
}