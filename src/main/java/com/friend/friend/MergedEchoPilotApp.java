package com.friend.friend;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Node;
import javafx.embed.swing.JFXPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JOptionPane;
import java.util.Objects; 

/**
 * Merged JavaFX Application Class - Implements Floating Components UI.
 */
public class MergedEchoPilotApp extends Application {
//public class MergedEchoPilotApp{
    // --- Components ---
    private static MergedEchoPilotApp instance;
    private TextArea commandArea; // New container for the log
    private Button pauseButton, resumeButton, startButton, stopButton, maxBtn, minBtn, closeBtn;
    // ✅ FIX: micIcon is now initialized immediately.
    private final ImageView micIcon = new ImageView(); 
    private VBox loaderContainer;
    
    // 💥 CHANGE 1: fullRoot is now a StackPane for layering!
    private final StackPane fullRoot = new StackPane(); 
    
    private final VBox mainContainer = new VBox(); // Still used for Scene switching
    private Stage primaryStage;
    private Scene scene;
    // ✅ CHANGE: Added 'mic' Image field for clarity.
    private Image mic, listeningGifImage, loaderGifImage; 
    private HBox titleBar;

    private Runnable resumeAction, pauseAction, startAction, stopAction;

    private static final String MIC_ICON_PATH = "/images/_mic.png";
    // Java Code
    private static final String BACKGROUND_GIF_PATH = "/images/12.gif"; 
    private static final String LOADER_GIF_PATH = "/images/Spinner.gif";
    // Java Code
    private static final String DEFAULT_INITIAL_TEXT = "Command Log:";
    private static final Logger logger = LoggerFactory.getLogger(MergedEchoPilotApp.class);
    private static JFXPanel fxPanel;
    
    private static SettingsManager settingsManager;
    private static PreferencesDialog preferencesDialog;
    private static AssistantOverlay assistantOverlay;
    private EchoPilotRecognizer recognizer;

    private static boolean javafxAvailable = false;
    private static Throwable javafxInitError = null;
    boolean isListening ;
    private boolean firstCommandExecuted = false; // New variable

    // Helper class for dragging logic in MiniView
    class Delta {
        double x, y; 
    }
    public MergedEchoPilotApp() {
        // Pre-load images safely in the constructor or setup method
        try {
            // Load the mic icon separately
            mic = new Image(Objects.requireNonNull(getClass().getResource(MIC_ICON_PATH)).toExternalForm(), 700, 500, true, true);
            
            // Load the background GIF
            listeningGifImage = new Image(Objects.requireNonNull(getClass().getResource(BACKGROUND_GIF_PATH)).toExternalForm(), 700,500,true,true); 
            
            // Load the smaller loader GIF
            loaderGifImage = new Image(Objects.requireNonNull(getClass().getResource(LOADER_GIF_PATH)).toExternalForm(), 700,500,true,true);
            
        } catch (Exception e) {
            System.err.println("Failed to load core images: " + e.getMessage());
            logger.error("Failed to load core images", e);
            mic = null; 
            loaderGifImage = null;
            listeningGifImage = null; 
        }
        
        // 💥 CHANGE 3: Create the background ImageView
        ImageView backgroundImageView = new ImageView();
        if (listeningGifImage != null) {
            backgroundImageView.setImage(listeningGifImage);
        } else {
            // Fallback to a solid color if GIF fails
            fullRoot.setStyle("-fx-background-color: #202033;");
        }
        backgroundImageView.setPreserveRatio(true); // IMPORTANT: Stretch to fill
        // These will be bound to scene size in start()
        backgroundImageView.setFitWidth(800); 
        backgroundImageView.setFitHeight(600); 
        
        // Create the individual UI components
        VBox micAndLoaderPane = createMicAndLoaderPane(); // Renamed for clarity
        VBox commandLogPane = createCommandLog();
        HBox controlPanel = createControlPanel();
        
        // 💥 CHANGE 4: Group all UI elements into a single VBox.
        // This VBox will "float" on top of the background GIF.
        VBox uiContainer = new VBox(0); // Spacing 0 between elements
        uiContainer.getChildren().addAll(micAndLoaderPane, commandLogPane, controlPanel); // Center the UI block within the StackPane

        // 💥 FIX: Adjusted VGrow settings for proper vertical layout
        VBox.setVgrow(micAndLoaderPane, Priority.ALWAYS); // Mic/Loader area takes up most space
        VBox.setVgrow(commandLogPane, Priority.NEVER); // Command Log area maintains its fixed size
        VBox.setVgrow(controlPanel, Priority.NEVER); // Control panel maintains its fixed size
        
        uiContainer.setAlignment(Pos.TOP_CENTER);
        
        // 💥 CHANGE 5: Add background first, then UI container to the StackPane
        //fullRoot.getChildren().addAll(backgroundImageView, uiContainer);
        // Inside MergedEchoPilotApp constructor
        fullRoot.getChildren().addAll(backgroundImageView, uiContainer); // uiContainer is on top

        hideLoadingAnimation();
    }

    @Override
    public void start(Stage primaryStage) {
        instance = this;
        this.primaryStage = primaryStage;
        primaryStage.setTitle("⭐ My Friend");

        scene = new Scene(mainContainer, 800, 550); // Start with Full View size
        primaryStage.setScene(scene);
        primaryStage.setResizable(true); // Full view is resizable
        ImageView backgroundImageView = (ImageView) fullRoot.getChildren().get(0); 
        backgroundImageView.fitWidthProperty().bind(scene.widthProperty());
        backgroundImageView.fitHeightProperty().bind(scene.heightProperty());
        
        switchToFullView();

        primaryStage.show();
    }
    public static MergedEchoPilotApp getInstance() {
        return instance;
    }
    
    // Java Code Fix for MergedEchoPilotApp

public void updateStatus(String status) {
    
        // 1. Still prints to the terminal for debugging
        System.out.println("Possible done"+status);
        
        // 2. Hides the loading spinner
        //hideLoadingAnimation();
        
        // ✅ CRITICAL FIX: We must pass the status text to the GUI updater!
        updateCommand(status); 
        System.out.println("Possible"+status);

}

    public void updateCommand(String command) {
        Platform.runLater(() -> { 
            if (commandArea == null || command == null || command.trim().isEmpty()) {
                return; 
            }
            
            System.out.println("############## UPDATE area ##############"); 
            
            if (!firstCommandExecuted) { 
                commandArea.setText("Command History:\n" + command);
                firstCommandExecuted = true; // 🌟 FIX: We set the flag to true!
            } else {
                commandArea.appendText("\n" + command);
            }

            commandArea.setScrollTop(Double.MAX_VALUE);
            
            try {
                System.out.println("############## UPDATE 17 ##############");
                updateAssistantTranscript(command);
                System.out.println("############## UPDATE 18 ##############");
            } catch (Throwable t) {
                // Ignored.
            }
        }); 
    }
    private VBox createMicAndLoaderPane() { 
        // 1. Configure the loader image view
        ImageView loaderImage = new ImageView();
        Label loaderText = new Label("Warming up the circuits...");
        loaderText.setTextFill(Color.WHITE);

        if (loaderGifImage != null) {
            loaderImage.setImage(loaderGifImage);
            loaderImage.setFitWidth(150); // Make the loader GIF much smaller
            loaderImage.setFitHeight(150);
            loaderImage.setPreserveRatio(true);
        } else {
            loaderText.setText("Loader Image Failed to Load");
            loaderText.setTextFill(Color.RED);
        }

        loaderContainer = new VBox(5, loaderImage, loaderText);
        loaderContainer.setAlignment(Pos.CENTER);
        loaderContainer.setId("loaderContainer");
        loaderContainer.setVisible(false); // Starts hidden

        // 2. Configure the micIcon (which was already initialized)
        micIcon.setPreserveRatio(true);
        micIcon.setFitWidth(300); // Size for the static mic icon
        micIcon.setFitHeight(300); 
        
        if (mic != null) {
            micIcon.setImage(mic);
        } else {
            // Fallback for mic icon if it failed to load
            loaderText.setText("Mic Icon Failed to Load");
            loaderText.setTextFill(Color.RED);
        }

        // 3. Create the container holding both mic and loader
        StackPane overlayStack = new StackPane(micIcon, loaderContainer);
        // StackPane ensures micIcon and loaderContainer are layered on top of each other.
        // We control which one is visible using show/hideLoadingAnimation.

        VBox micHolder = new VBox(overlayStack); 
        micHolder.setAlignment(Pos.CENTER);
        VBox.setVgrow(micHolder, Priority.ALWAYS); // Let the micHolder take all vertical space
        
        // Padding for the mic/loader section
        micHolder.setPadding(new Insets(20, 20, 50, 20)); 
        micHolder.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(micHolder, Priority.ALWAYS); 

        return micHolder;
    }

    // Inside MergedEchoPilotApp.java (Replace the existing createCommandLog method)

// Inside MergedEchoPilotApp.java (Replace the existing createCommandLog method)
    private VBox createCommandLog() {
        commandArea = new TextArea("Command Log:");
        commandArea.setId("commandArea");
        commandArea.setEditable(false);
        commandArea.setFont(Font.font("Consolas", 14));
        commandArea.setStyle("-fx-control-inner-background: #000000; -fx-text-fill: #00FF00; -fx-border-color: #FFFFFF;");
        commandArea.setMinHeight(120); // Give a fixed height
        commandArea.setMaxHeight(120);
        commandArea.setWrapText(true);
        // We use Priority.NEVER here to ensure the fixed min/max height is respected
        // and doesn't conflict with the VGrow setting of the parent uiContainer.
        VBox.setVgrow(commandArea, Priority.NEVER);
        VBox container = new VBox(commandArea); // 0 spacing
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(5, 20, 0, 20)); // Top padding 5, 0 bottom padding
        container.setStyle("-fx-background-color: transparent;"); // Ensure background GIF shows through
       
        VBox.setVgrow(container, Priority.NEVER); // CRITICAL: Forces log container to maintain small size
        return container;
    }

    private HBox createControlPanel() {
        // ... (No changes here, button creation logic remains the same) ...
        pauseButton = createStyledButton("Pause", Color.web("#FFC107"));
        resumeButton = createStyledButton("Resume", Color.LIMEGREEN);
        startButton = createStyledButton("Start", Color.SKYBLUE);
        stopButton = createStyledButton("Stop", Color.RED);

        Button miniViewButton = createStyledButton("Mini", Color.WHITE);
        miniViewButton.setOnAction(e -> switchToMiniView());

        // ... (Action handlers for buttons remain the same) ...
        pauseButton.setOnAction(e -> {
            if (pauseAction != null) pauseAction.run();
            updateListeningIndicator(false);
        });

        resumeButton.setOnAction(e -> {
            if (resumeAction != null) resumeAction.run();
            updateListeningIndicator(true);
        });

        stopButton.setOnAction(e -> {
            if (stopAction != null) stopAction.run();
            updateListeningIndicator(false);
        });

        startButton.setOnAction(e -> {
            if (startAction != null) startAction.run();
            updateListeningIndicator(true);
        });

        HBox buttonPanel = new HBox(50);
        buttonPanel.setAlignment(Pos.CENTER);
        buttonPanel.setPadding(new Insets(0, 20, 30, 20)); // Top 5, Bottom 10
        buttonPanel.setStyle("-fx-background-color: transparent;"); // Ensure background GIF shows through

        buttonPanel.getChildren().addAll(stopButton, pauseButton, miniViewButton, resumeButton, startButton);

        for (Node node : buttonPanel.getChildren()) {
            HBox.setHgrow(node, Priority.ALWAYS);
            ((Button) node).setPrefWidth(200);
        }

        return buttonPanel;
    }
    
    // ... (Mini Control Layout, Color Converters, Mini/Full View switching methods are unchanged) ...
    
    private HBox createMiniControlLayout(Stage currentMiniStage) {
        HBox miniBar = new HBox(5); // Small gap between mini buttons
        miniBar.setAlignment(Pos.CENTER_LEFT); // Align to the left
        miniBar.setStyle("-fx-background-color: #2b2b2b; -fx-padding: 5;");

        // Small, subtle style for window controls
        String controlBtnStyle = "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold;";
        
        closeBtn = new Button("✕");
        closeBtn.setStyle(controlBtnStyle);
        minBtn = new Button("—");
        minBtn.setStyle(controlBtnStyle);
        // maxBtn is not typically needed in an UNDECORATED mini view, but kept for completeness
        maxBtn = new Button("⬜");
        maxBtn.setStyle(controlBtnStyle);

        Button miniPause = createStyledButton("▶", Color.web("#FFC107"));
        miniPause.setOnAction(e -> {
            if (pauseAction != null) pauseAction.run();
            updateListeningIndicator(false);
        });
        miniPause.setPrefWidth(30);
        miniPause.setPrefHeight(30);

        Button miniResume = createStyledButton("||", Color.LIMEGREEN);
        miniResume.setOnAction(e -> {
            if (resumeAction != null) resumeAction.run();
            updateListeningIndicator(true);
        });
        miniResume.setPrefWidth(30);
        miniResume.setPrefHeight(30);
        
        closeBtn.setOnAction(e -> primaryStage.close()); // Use primaryStage.close() to kill the whole app
        minBtn.setOnAction(e -> currentMiniStage.setIconified(true)); // Iconify the mini stage
        maxBtn.setOnAction(e -> {
            currentMiniStage.close(); // Hide the mini stage
            primaryStage.show();     // Show the primary stage
        });
        
        // Left controls (Pause/Resume/Full)
        HBox leftControls = new HBox(5, miniPause, miniResume);
        leftControls.setAlignment(Pos.CENTER_LEFT);

        // Right controls (Window controls)
        HBox rightControls = new HBox(5, minBtn, maxBtn, closeBtn);
        rightControls.setAlignment(Pos.CENTER_RIGHT);

        // Put them together with a spacer
        miniBar.getChildren().addAll(leftControls, new Region(), rightControls);
        HBox.setHgrow(miniBar.getChildren().get(1), Priority.ALWAYS); // Spacer grows

        return miniBar;
    }

    public void switchToFullView() {
        mainContainer.getChildren().clear();
        mainContainer.getChildren().add(fullRoot);

        if (primaryStage != null) {
            Platform.runLater(() -> {
                //primaryStage.initStyle(StageStyle.DECORATED); // Switch back to normal window frame
                primaryStage.setResizable(true);
                primaryStage.setWidth(800);
                primaryStage.setHeight(650);
                primaryStage.centerOnScreen();
                primaryStage.setAlwaysOnTop(false); // No longer always on top
            });
        }
    }

    public void switchToMiniView() {
        // 1. Create a NEW Stage for the Mini View
        Stage miniStage = new Stage();
        miniStage.setTitle(primaryStage.getTitle());
        
        // Set UNDECORATED *before* showing the stage
        miniStage.initStyle(StageStyle.UNDECORATED); 

        // 2. Prepare the Mini Layout
        titleBar = createMiniControlLayout(miniStage); // Pass the stage to the layout creation method
        VBox miniRoot = new VBox(titleBar);
        
        Scene miniScene = new Scene(miniRoot);
        miniStage.setScene(miniScene);
        
        final Delta dragDelta = new Delta();

        // 3. Set properties and drag handlers
        miniStage.setResizable(false);
        miniStage.setX(1000); // Set initial position
        miniStage.setY(0);
        miniStage.setAlwaysOnTop(true);
        
        // Drag handlers on the titleBar of the NEW stage
        titleBar.setOnMousePressed(e -> {
            dragDelta.x = miniStage.getX() - e.getScreenX();
            dragDelta.y = miniStage.getY() - e.getScreenY();
            e.consume();
        });
        titleBar.setOnMouseDragged(e -> {
            miniStage.setX(e.getScreenX() + dragDelta.x);
            miniStage.setY(e.getScreenY() + dragDelta.y);
        });

        // 4. Update the actions to close/show the correct stages
        miniStage.setOnCloseRequest(e -> primaryStage.close());
        
        // 5. Hide the Full Stage and show the Mini Stage
        primaryStage.hide(); 
        miniStage.show();

        // Update the Full View switch action to return to the primary stage
        // We need to update the expandButton's action inside createMiniControlLayout. 
        // I will adjust the signature of createMiniControlLayout.
    }

    private Button createStyledButton(String text, Color bgColor) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14)); // Slightly smaller font for buttons
        button.setTextFill(Color.BLACK); // Explicitly set text color
        button.setStyle(
                "-fx-background-color: " + toHex(bgColor) +
                        "; -fx-text-fill: black;" +
                        " -fx-border-color: white;" +
                        " -fx-border-width: 2;" +
                        //" -fx-padding: 5 10 5 10;" // Added padding for better look
                        " -fx-padding: 5 30 5 30;" 
        );

        // Add hover effect for a better feel
        button.setOnMouseEntered(e -> button.setStyle(button.getStyle() + "-fx-scale-y: 1.05; -fx-scale-x: 1.05;"));
        button.setOnMouseExited(e -> button.setStyle(button.getStyle() + "-fx-scale-y: 1.0; -fx-scale-x: 1.0;"));

        return button;
    }
    
    private String toHex(Color color) {
        return String.format(
                "#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255)
        );
    }

    public void showLoadingAnimation() {
        Platform.runLater(() -> {
            if (loaderContainer != null) {
                loaderContainer.setVisible(true);
                micIcon.setVisible(false); // Hide the mic icon when loading
            }
        });
    }

    public void hideLoadingAnimation() {
        Platform.runLater(() -> {
            if (loaderContainer != null) {
                loaderContainer.setVisible(false);
                micIcon.setVisible(true); // Show the mic icon when not loading
            }
        });
    }

    public void updateListeningIndicator(boolean isListening) {
        this.isListening = isListening;
        Platform.runLater(() -> {
            if (micIcon != null && pauseButton != null && resumeButton != null) {
                pauseButton.setDisable(!isListening);
                resumeButton.setDisable(isListening);
                
                // When listening, show the loader GIF (scaled for the mic position)
                if (isListening) {
                    showLoadingAnimation(); // Ensure loader is visible
                } else {
                    hideLoadingAnimation(); // Ensure mic is visible
                }
            }
        });
    }
    
    public void setRecognizerControls(Runnable resume, Runnable pause, Runnable start, Runnable stop, boolean initialState) {
        this.resumeAction = resume;
        this.pauseAction = pause;
        this.startAction = start;
        this.stopAction = stop;
        updateListeningIndicator(initialState);
    }
    
    // getRootNode needs to return a Node, and StackPane is a Node.
    public Node getRootNode() {
        return fullRoot; // fullRoot is now a StackPane
    }
    
    public static void initializeBridge(SettingsManager settings) {
        settingsManager = settings;
        logger.info("[MergedEchoPilotApp] Starting initialization...");

        // FIX: The core issue was blocking the FX thread or not ensuring initialization order.
        // By using Platform.runLater, we ensure all FX-related initialization happens on the FX thread.
        Platform.runLater(() -> {
            try {
                // Initialize JFXPanel (allows embedding in Swing/AWT if needed)
                // NOTE: This call is usually only needed if embedding FX into Swing.
                fxPanel = new JFXPanel();
                javafxAvailable = true;

                // Initialize the assistant overlay (a JavaFX component)
                try {
                    // NOTE: Assumes AssistantOverlay and PreferencesDialog are defined elsewhere
                    assistantOverlay = new AssistantOverlay(); 
                    assistantOverlay.hide();
                    preferencesDialog = new PreferencesDialog(settingsManager); // Pre-initialize
                } catch (Throwable t) {
                    logger.warn("Failed to init AssistantOverlay/PreferencesDialog", t);
                }
            } catch (Throwable t) {
                javafxInitError = t;
                javafxAvailable = false;
                logger.error("[MergedEchoPilotApp] ✗ Failed to initialize JavaFX components.", t);
            }
            // Log success/failure immediately on the FX thread.
            if (!javafxAvailable) {
                logger.error("[MergedEchoPilotApp] ✗ NO JavaFX available.");
            } else {
                logger.info("[MergedEchoPilotApp] √ JavaFX components initialized.");
            }
        });
    }

    public static void showPreferencesDialog() {
        if (!javafxAvailable) {
            JOptionPane.showMessageDialog(null, "JavaFX not available.", "JavaFX Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (settingsManager == null) return;

        Platform.runLater(() -> {
            try {
                // Use the existing or create a new dialog
                if (preferencesDialog == null) {
                    preferencesDialog = new PreferencesDialog(settingsManager);
                }
                
                // Show/bring to front
                if (preferencesDialog.getStage() != null) {
                    preferencesDialog.show();
                    preferencesDialog.getStage().toFront();
                }

            } catch (Exception ex) {
                logger.error("Error opening preferences dialog", ex);
            }
        });
    }
    
    public static void updateAssistantTranscript(String text) {
        if (!javafxAvailable || assistantOverlay == null) return;

        try {
            Platform.runLater(() -> assistantOverlay.updateTranscript(text));
        } catch (Throwable ignored) {
        }
    }
    

    public static void closePreferencesDialog() {
        if (preferencesDialog != null) {
            Platform.runLater(preferencesDialog::close);
        }
    }

    public static String getSettingsSummary() {
        if (settingsManager == null) return "Settings not available";

        // NOTE: This code assumes the existence of getters in SettingsManager
        return String.format(
                "Voice: %s\nVolume: %.0f%%\nTheme: %s\nWake Word: %s\nBrowser: %s\nSpeech Rate: %.2fx",
                settingsManager.getVoiceName(),
                settingsManager.getVolume() * 100,
                settingsManager.getTheme(),
                settingsManager.getWakeWord(),
                settingsManager.getPreferredBrowser(),
                settingsManager.getSpeechRate()
        );
    }

    public static boolean isInitialized() {
        return settingsManager != null && javafxAvailable;
    }

    public static JFXPanel getFXPanel() {
        return fxPanel;
    }

    public static boolean isPreferencesDialogOpen() {
        return preferencesDialog != null &&
                preferencesDialog.getStage() != null &&
                preferencesDialog.getStage().isShowing();
    }

    public static void showAssistantOverlay() {
        if (!javafxAvailable || assistantOverlay == null) return;
        Platform.runLater(() -> assistantOverlay.show());
    }

    public static void hideAssistantOverlay() {
        if (!javafxAvailable || assistantOverlay == null) return;
        Platform.runLater(() -> assistantOverlay.hide());
    }

    public static void updateAssistantConfidence(double conf) {
        if (!javafxAvailable || assistantOverlay == null) return;
        Platform.runLater(() -> assistantOverlay.updateConfidence(conf));
    }

    public static void setAssistantState(String state) {
        if (!javafxAvailable || assistantOverlay == null) return;
        Platform.runLater(() -> assistantOverlay.setState(state));
    }
    
    // public static void main(String[] args) {
    //     Friend.clearLogsOnStartup();
    //     launch(args);
    // }
}