package com.friend.friend;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Objects;

/**
 * Simplified GUI using a JavaFX WebView to render an offline HTML interface.
 * Communication with HTML is done via the WebEngine's JSObject bridge.
 */
public class WebGui extends Application {
    
    private static final Logger logger = LoggerFactory.getLogger(WebGui.class);
    private static WebGui instance;
    private WebView webView;
    private WebEngine webEngine;
    private Stage primaryStage;
    
    // Commands passed to this class from Friend.java
    private Runnable resumeAction, pauseAction, startAction, stopAction;

    // --- Public access for the Friend class to get the instance ---
    public static WebGui getInstance() {
        return instance;
    }

    // --- Entry point for JavaFX launch ---
    @Override
    public void start(Stage primaryStage) {
        instance = this;
        this.primaryStage = primaryStage;
        primaryStage.setTitle("⭐ My Friend - Web Interface");
        
        // 1. Setup the WebView
        webView = new WebView();
        webEngine = webView.getEngine();
        
        // 2. Load the local HTML file (must be compiled into resources)
        // NOTE: Ensure your HTML file is saved under src/main/resources/html/FriendInterface.html
        try {
            String htmlPath = Objects.requireNonNull(getClass().getResource("/html/FriendInterface.html")).toExternalForm();
            webEngine.load(htmlPath);
        } catch (NullPointerException e) {
            logger.error("FATAL: Could not find /html/FriendInterface.html. Please ensure file exists in resources.", e);
            primaryStage.close();
            return;
        }

        // 3. Set up the Scene
        Scene scene = new Scene(webView, 600, 450);
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();

        // 4. CRITICAL: Set up the bridge AFTER the page is loaded (when the engine is ready)
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                setupJavascriptBridge();
                logger.info("[WebGui] HTML page loaded and JS bridge established.");
            }
        });
    }

    /**
     * Exposes a Java object to the JavaScript environment under the name 'java'.
     * JS can then call 'java.handleCommand(command)'.
     */
    private void setupJavascriptBridge() {
        try {
            JSObject window = (JSObject) webEngine.executeScript("window");
            
            // Expose a Java instance (this GUI class) to JS as the object named 'java'
            window.setMember("java", new WebBridge()); 
            
            // Define the JavaScript function 'javaCommand' that the buttons call.
            webEngine.executeScript(
                "var javaCommand = function(command) { java.handleCommand(command); };"
            );
            
            // Initial log message
            updateStatus("Web GUI ready. Waiting for core initialization...");
        } catch (Exception e) {
            logger.error("[WebGui] Failed to setup JavaScript bridge.", e);
        }
    }

    // --- Communication Bridge Class ---
    /**
     * This inner class is what JavaScript interacts with. 
     * It must be public to be accessible by the JS bridge.
     */
    public class WebBridge {
        /**
         * Receives commands (like 'pause', 'resume') from the HTML buttons.
         */
        public void handleCommand(String command) {
            logger.info("[WebBridge] Received command from JS: " + command);
            // Must run the command actions on the JavaFX Application Thread
            Platform.runLater(() -> {
                switch (command.toLowerCase()) {
                    case "pause":
                        if (pauseAction != null) pauseAction.run();
                        break;
                    case "resume":
                        if (resumeAction != null) resumeAction.run();
                        break;
                    case "settings":
                        // Call the static method to show the settings dialog
                        // NOTE: showPreferencesDialog must now exist in this class or a utility class.
                        // Assuming you move that static method into WebGui or a utility.
                        // For this example, we'll log it.
                        logger.warn("Settings command received. Implementation pending.");
                        break;
                    default:
                        logger.warn("[WebBridge] Unknown command: " + command);
                }
            });
        }
    }

    // --- Communication from Java Core to GUI (Updates the HTML log) ---

    /**
     * Updates the HTML command log area from the Java backend.
     * This replaces the old updateStatus() method.
     */
    public void updateStatus(String status) {
        // Run on the JavaFX thread for safety
        Platform.runLater(() -> {
            // Check if the engine is ready
            if (webEngine != null && webEngine.getLoadWorker().getState() == javafx.concurrent.Worker.State.SUCCEEDED) {
                // Call the JavaScript function 'updateLog' and pass the status message
                // We use replace to handle quotes safely in the string passed to JS
                String safeStatus = status.replace("'", "\\'");
                webEngine.executeScript("updateLog('" + safeStatus + "');");
            } else {
                // This happens during initial startup, before the HTML is fully loaded.
                System.out.println("[Status Queue] " + status);
            }
        });
    }

    /**
     * Sets the actions for the GUI controls from the core logic.
     */
    public void setRecognizerControls(Runnable resume, Runnable pause, Runnable start, Runnable stop, boolean initialState) {
        this.resumeAction = resume;
        this.pauseAction = pause;
        this.startAction = start;
        this.stopAction = stop;
        // In a real WebView, you would add a JS call here to update button styles based on initialState
    }
    
    // Minimal required methods, replacing the complex ones from MergedEchoPilotApp
    public void showLoadingAnimation() {
        // In a real WebView, you would execute JS to show a spinner element:
        // webEngine.executeScript("document.getElementById('loadingSpinner').style.display='block'");
    }
    
    public void hideLoadingAnimation() {
        // In a real WebView, you would execute JS to hide the spinner element:
        // webEngine.executeScript("document.getElementById('loadingSpinner').style.display='none'");
    }

    // Assuming we remove other unnecessary methods like switchToMiniView/toHex/etc.
    // NOTE: If you still need PreferencesDialog, you must move its static methods here.

    /**
     * Static method to launch the JavaFX application. Called from Friend.java
     */
    public static void launchGui(String[] args) {
       launch(WebGui.class, args);
    }
}