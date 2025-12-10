// package com.friend.friend;

// import javafx.application.Application;
// import javafx.application.Platform;
// import javafx.scene.Scene;
// import javafx.scene.web.WebEngine;
// import javafx.scene.web.WebView;
// import javafx.stage.Stage;
// import netscape.javascript.JSObject;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import java.util.Objects;

// /**
//  * Simplified GUI using a JavaFX WebView to render an offline HTML interface.
//  * Communication with HTML is done via the WebEngine's JSObject bridge.
//  */
// public class WebGui extends Application {
    
//     private static final Logger logger = LoggerFactory.getLogger(WebGui.class);
//     private static WebGui instance;
//     private WebView webView;
//     private WebEngine webEngine;
//     private Stage primaryStage;
//     private boolean isJsReady = false;
    
//     // Commands passed to this class from Friend.java
//     private Runnable resumeAction, pauseAction, startAction, stopAction;

//     // --- Public access for the Friend class to get the instance ---
//     public static WebGui getInstance() {
//         return instance;
//     }

//     // --- Entry point for JavaFX launch ---
//     @Override
//     public void start(Stage primaryStage) {
//         instance = this;
//         this.primaryStage = primaryStage;
//         primaryStage.setTitle("⭐ My Friend - Web Interface");
        
//         // 1. Setup the WebView
//         webView = new WebView();
//         webEngine = webView.getEngine();
        
//         // 2. Load the local HTML file (must be compiled into resources)
//         // Analogy: Giving the WebView the map to find the HTML page.
//         try {
//             // NOTE: Ensure your HTML file is saved under src/main/resources/html/FriendInterface.html
//             String htmlPath = Objects.requireNonNull(getClass().getResource("/html/FriendInterface.html")).toExternalForm();
//             webEngine.load(htmlPath);
//         } catch (NullPointerException e) {
//             logger.error("FATAL: Could not find /html/FriendInterface.html. Please ensure file exists in resources.", e);
//             primaryStage.close();
//             return;
//         }

//         // 3. Set up the Scene
//         Scene scene = new Scene(webView, 600, 450);
//         primaryStage.setScene(scene);
//         primaryStage.setResizable(true);
//         primaryStage.show();

//         // 4. CRITICAL: Set up the bridge AFTER the page is loaded (when the engine is ready)
//         // Analogy: Waiting for the translator to be ready before starting the conversation.
//         webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
//             if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
//                 setupJavascriptBridge();
//                 this.isJsReady = true;
//                 logger.info("[WebGui] HTML page loaded and JS bridge established.");
//             }
//         });
//     }

//     /**
//      * Exposes a Java object to the JavaScript environment.
//      * This allows JavaScript to call Java methods.
//      */
//     private void setupJavascriptBridge() {
//         try {
//             JSObject window = (JSObject) webEngine.executeScript("window");
            
//             // Expose a Java instance (this GUI class) to JS as the object named 'java'
//             window.setMember("java", new WebBridge()); 
            
//             // Define the JavaScript function 'javaCommand' that the buttons call.
//             // This is a safety layer to ensure the function exists in JS.
//             webEngine.executeScript(
//                 "var javaCommand = function(command) { java.handleCommand(command); };"
//             );
            
//             // Initial log message
//             updateStatus("Web GUI ready. Waiting for core initialization...");
//         } catch (Exception e) {
//             logger.error("[WebGui] Failed to setup JavaScript bridge.", e);
//         }
//     }

//     // --- Communication Bridge Class ---
//     /**
//      * This inner class is what JavaScript interacts with. 
//      * It must be public to be accessible by the JS bridge.
//      */
//     public class WebBridge {
//         /**
//          * Receives commands (like 'pause', 'resume') from the HTML buttons.
//          */
//         public void handleCommand(String command) {
//             logger.info("[WebBridge] Received command from JS: " + command);
//             // Must run the command actions on the JavaFX Application Thread
//             Platform.runLater(() -> {
//                 switch (command.toLowerCase()) {
//                     case "pause":
//                         if (pauseAction != null) pauseAction.run();
//                         break;
//                     case "resume":
//                         if (resumeAction != null) resumeAction.run();
//                         break;
//                     case "settings":
//                         logger.warn("Settings command received. Implementation pending.");
//                         // You can call your static showPreferencesDialog here if needed.
//                         break;
//                     default:
//                         logger.warn("[WebBridge] Unknown command: " + command);
//                 }
//             });
//         }
//     }

//     // --- Communication from Java Core to GUI (Updates the HTML log) ---

//     /**
//      * Updates the HTML command log area from the Java backend.
//      * Calls the JavaScript function updateLog().
//      */
//     public void updateStatus(String status) {
//         // Run on the JavaFX thread for safety
//         Platform.runLater(() -> {
//             // Check if the engine is ready
//             if (webEngine != null && webEngine.getLoadWorker().getState() == javafx.concurrent.Worker.State.SUCCEEDED) {
//                 // Call the JavaScript function 'updateLog'
//                 // Analogy: The elephant telling the translator what to say to the parrot.
//                 String safeStatus = status.replace("'", "\\'"); // Escape single quotes
//                 webEngine.executeScript("updateLog('" + safeStatus + "');");
//             } else {
//                 System.out.println("[Status Queue] " + status);
//             }
//         });
//     }

//     /**
//      * Sets the actions for the GUI controls from the core logic.
//      */
//     public void setRecognizerControls(Runnable resume, Runnable pause, Runnable start, Runnable stop, boolean initialState) {
//         this.resumeAction = resume;
//         this.pauseAction = pause;
//         this.startAction = start;
//         this.stopAction = stop;
//         // You could add a JS call here to visually update the HTML buttons' style (e.g., enable/disable).
//     }
    
//     // Minimal required animation controls (now controlled by JS)
//     public void showLoadingAnimation() {
//         Platform.runLater(() -> {
//             // CHECK THE FLAG BEFORE EXECUTING!
//             if (isJsReady) { // <--- ADD THIS CHECK
//                 webEngine.executeScript("showLoadingAnimation();");
//             } else {
//                 logger.warn("[WebGui] JS not ready. Skipping showLoadingAnimation call.");
//             }
//         });
//     }

//     // Apply the same check to hideLoadingAnimation() and clearChatArea()
//     public void hideLoadingAnimation() {
//         Platform.runLater(() -> {
//             if (isJsReady) { // <--- ADD THIS CHECK
//                 webEngine.executeScript("hideLoadingAnimation();");
//             }
//         });
//     }
//     public void clearChatArea(){
//         Platform.runLater(() -> {
//             if (isJsReady) { // <--- ADD THIS CHECK
//                 webEngine.executeScript("clearChat();"); 
//                 logger.info("[WebGui] Called JS function to clear chat.");
//             } else {
//                 logger.warn("[WebGui] JS not ready. Skipping clearChat call.");
//             }
//         });
//     }


//     /**
//      * Static method to launch the JavaFX application. Called from Friend.java
//      */
//     public static void launchGui(String[] args) {
//        launch(WebGui.class, args);
//     }
// }
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
        // Analogy: Giving the WebView the map to find the HTML page.
        try {
            // NOTE: Ensure your HTML file is saved under src/main/resources/html/FriendInterface.html
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
        // Analogy: Waiting for the translator to be ready before starting the conversation.
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                setupJavascriptBridge();
                logger.info("[WebGui] HTML page loaded and JS bridge established.");
            }
        });
    }

    /**
     * Exposes a Java object to the JavaScript environment.
     * This allows JavaScript to call Java methods.
     */
    private void setupJavascriptBridge() {
        try {
            JSObject window = (JSObject) webEngine.executeScript("window");
            
            // Expose a Java instance (this GUI class) to JS as the object named 'java'
            window.setMember("java", new WebBridge()); 
            
            // Define the JavaScript function 'javaCommand' that the buttons call.
            // This is a safety layer to ensure the function exists in JS.
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
         * Receives commands (like 'pause', 'resume', 'start', 'stop') from the HTML buttons.
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
                    case "start": // FIX 1: Added handler
                        if (startAction != null) startAction.run();
                        break;
                    case "stop": // FIX 1: Added handler
                        if (stopAction != null) stopAction.run();
                        break;
                    case "settings":
                        logger.warn("Settings command received. Implementation pending.");
                        // You can call your static showPreferencesDialog here if needed.
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
     * Calls the JavaScript function updateLog().
     */
    public void updateStatus(String status) {
        // Run on the JavaFX thread for safety
        Platform.runLater(() -> {
            // Check if the engine is ready
            if (webEngine != null && webEngine.getLoadWorker().getState() == javafx.concurrent.Worker.State.SUCCEEDED) {
                // Call the JavaScript function 'updateLog'
                // Analogy: The elephant telling the translator what to say to the parrot.
                String safeStatus = status.replace("'", "\\'"); // Escape single quotes
                webEngine.executeScript("updateLog('" + safeStatus + "');");
            } else {
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
        // You could add a JS call here to visually update the HTML buttons' style (e.g., enable/disable).
    }
    
    // Minimal required animation controls (now controlled by JS)
    public void showLoadingAnimation() {
        // You'll implement this by executing a JavaScript function
        Platform.runLater(() -> webEngine.executeScript("showLoadingAnimation();"));
    }
    
    public void hideLoadingAnimation() {
        Platform.runLater(() -> webEngine.executeScript("hideLoadingAnimation();"));
    }
    public void clearChatArea(){
        Platform.runLater(() -> {
            // FIX: Ensure you call the JavaScript function name you chose!
            webEngine.executeScript("clearChat();"); 
            logger.info("[WebGui] Called JS function to clear chat.");
        });
    }

    /**
     * Static method to launch the JavaFX application. Called from Friend.java
     */
    public static void launchGui(String[] args) {
       launch(WebGui.class, args);
    }
}