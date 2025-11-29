package com.friend.friend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.AWTException;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.swing.SwingUtilities;

public class Friend {
    private static final Logger logger = LoggerFactory.getLogger(Friend.class);
    private static final String LOG_DIRECTORY_PATH = "logs/";
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

    public static void main(String[] args) {
        clearLogsOnStartup();
        SwingUtilities.invokeLater(() -> {
            FileSearcher fileSearcher = null;
            GoogleSearcher googleSearcher = null;
            SpeechEngine tts = null;
            EchoPilotRecognizer recognizer = null;
            EchoPilotGUI gui = null;
            CommandDispatcher dispatcher = null;
            SystemCommands systemCommands = null;
            SettingsManager settingsManager = null;
            ErrorReporter errorReporter = null;
            OnboardingWizard onboarding = null;
            HelpSystem helpSystem = null;

            try {
                logger.info("=== Friend Startup ===");
                logger.info("[Main] Java version: " + System.getProperty("java.version"));
                logger.info("[Main] JavaFX module-path: " + System.getProperty("javafx.version", "NOT SET"));
                
                // 1. Initialize settings and error reporting
                settingsManager = new SettingsManager();
                errorReporter = new ErrorReporter(settingsManager.isEnableTelemetry());
                
                // 1.5. Initialize JavaFX/Swing bridge for preferences dialog
                logger.info("[Main] Initializing JavaFX bridge...");
                JavaFXUIBridge.initialize(settingsManager);
                logger.info("[Main] JavaFX bridge initialized (overlay may be disabled).");
                
                // 2. Initialize GUI
                logger.info("[Main] Creating Swing GUI...");
                gui = new EchoPilotGUI();
                logger.info("[Main] GUI created; setting visible...");
                gui.setVisible(true);
                logger.info("[Main] GUI now visible.");
    
                // 3. Initialize core services (TTS, Searchers)
                tts = new SpeechEngine();
                fileSearcher = new FileSearcher();
                googleSearcher = new GoogleSearcher();
                
                // 4. Initialize Command Map
                Map<String, Runnable> commandMap = new HashMap<>();
                
                // 5. Initialize Dispatcher and Recognizer
                dispatcher = new CommandDispatcher(tts, null);
                recognizer = new EchoPilotRecognizer(dispatcher, gui, tts);
                dispatcher.setRecognizer(recognizer);
                
                // 6. Initialize SystemCommands
                systemCommands = new SystemCommands(commandMap, tts, recognizer, googleSearcher, fileSearcher);
                dispatcher.setSystemCommands(systemCommands);

                // 7. Initialize Help System
                helpSystem = new HelpSystem(tts, dispatcher);
                final HelpSystem finalHelpSystem = helpSystem;
                commandMap.put("help", () -> finalHelpSystem.speakAllCommands());
                commandMap.put("commands", () -> finalHelpSystem.showHelpWindow());
                commandMap.put("preferences", JavaFXUIBridge::showPreferencesDialog);
                commandMap.put("open friend settings", JavaFXUIBridge::showPreferencesDialog);

                // 8. Instantiate command groups
                try {
                    new AppCommands(commandMap, settingsManager);
                    new MediaCommands(commandMap, tts, recognizer, dispatcher);
                    new LifecycleCommands(commandMap, recognizer , gui , dispatcher);
                    new FolderCommands(commandMap, fileSearcher, dispatcher);
                } catch (Throwable t) {
                    logger.error("Failed to initialize command groups", t);
                    errorReporter.reportException("CommandGroupInitialization", t);
                    throw t;
                }

                // 9. Finalize Dispatcher
                dispatcher.registerCommands(commandMap);
                // 9.5 Load plugins (if any) from ./plugins folder
                try {
                    PluginLoader loader = new PluginLoader(new java.io.File("plugins"));
                    loader.loadPlugins(dispatcher);
                } catch (Throwable t) {
                    logger.warn("Plugin loading failed", t);
                }
                
                // 10. Initialize Onboarding Wizard with health checks
                onboarding = new OnboardingWizard(tts, recognizer, dispatcher, settingsManager);
                onboarding.addHealthCheck(new MicrophoneHealthCheck(new AudioResourceManager()));
                onboarding.addHealthCheck(new TTSHealthCheck(tts));
                onboarding.addHealthCheck(new DiskSpaceHealthCheck());
                onboarding.runIfFirstLaunch();

                // 11. Define Shutdown Action
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
                    if (finalTts != null) {
                        try {
                            finalTts.shutdown();
                        } catch (Throwable t) {
                            logger.error("TTS shutdown failed", t);
                            finalErrorReporter.reportException("TTSShutdown", t);
                        }
                    }
                    System.exit(0);
                };

                // 12. Connect GUI controls
                gui.setRecognizerControls(finalRecognizer::resume, finalRecognizer::pause, finalRecognizer.isListening());

                // 13. Initialize Tray Controller
                try {
                    new TrayController(recognizer, shutdownAction);
                } catch (AWTException e) {
                    logger.warn("System Tray not supported or failed to initialize", e);
                }

                // 14. Add shutdown hook
                Runtime.getRuntime().addShutdownHook(new Thread(shutdownAction, "Shutdown-Hook"));

                // 15. Start recognition loop
                recognizer.startListening();
                logger.info("Friend is ready. Running in background.");

            } catch (Exception e) {
                logger.error("Fatal error during startup", e);
                if (errorReporter != null) {
                    errorReporter.reportException("FriendStartup", e);
                }
                // Safe shutdown
                if (tts != null) {
                    try {
                        tts.shutdown();
                    } catch (Throwable t) {
                        logger.error("TTS shutdown during error failed", t);
                    }
                }
                if (recognizer != null) {
                    try {
                        recognizer.stop();
                    } catch (Throwable t) {
                        logger.error("Recognizer stop during error failed", t);
                    }
                }
                System.exit(1);
            }
        });
    }
}