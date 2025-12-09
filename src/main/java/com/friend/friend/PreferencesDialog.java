package com.friend.friend;

import java.io.File;
import java.util.*;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PreferencesDialog provides a modern JavaFX-based UI for user settings.
 * Includes tabs for General, Audio, Browser, Folders, and Advanced settings.
 */
public class PreferencesDialog {
    private static final Logger logger = LoggerFactory.getLogger(PreferencesDialog.class);
    private SettingsManager settingsManager;
    private Stage stage;
    private Scene scene;

    public PreferencesDialog(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
        initializeUI();
    }

    private void initializeUI() {
        stage = new Stage();
        stage.setTitle("Friend - Preferences");
        stage.setWidth(600);
        stage.setHeight(500);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Create tabs
        Tab generalTab = createGeneralTab();
        Tab audioTab = createAudioTab();
        Tab browserTab = createBrowserTab();
        Tab foldersTab = createFoldersTab();
        Tab advancedTab = createAdvancedTab();

        tabPane.getTabs().addAll(generalTab, audioTab, browserTab, foldersTab, advancedTab);

        // Button bar
        HBox buttonBar = new HBox(10);
        buttonBar.setPadding(new Insets(15));
        buttonBar.setStyle("-fx-alignment: center-right;");

        Button resetButton = new Button("Reset to Defaults");
        resetButton.setStyle("-fx-padding: 8px 20px;");
        resetButton.setOnAction(e -> resetToDefaults());

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-padding: 8px 20px;");
        cancelButton.setOnAction(e -> stage.close());

        Button saveButton = new Button("Save");
        saveButton.setStyle("-fx-padding: 8px 20px; -fx-font-weight: bold;");
        saveButton.setOnAction(e -> saveSettings());

        buttonBar.getChildren().addAll(resetButton, cancelButton, saveButton);

        // Main layout
        VBox mainLayout = new VBox(tabPane, buttonBar);
        VBox.setVgrow(tabPane, javafx.scene.layout.Priority.ALWAYS);

        scene = new Scene(mainLayout);
        applyTheme();
        stage.setScene(scene);
    }

    private Tab createGeneralTab() {
        Tab tab = new Tab();
        tab.setText("General");
        tab.setClosable(false);

        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        // Wake Word
        HBox wakeWordBox = new HBox(10);
        Label wakeWordLabel = new Label("Wake Word:");
        wakeWordLabel.setMinWidth(120);
        TextField wakeWordField = new TextField();
        wakeWordField.setText(settingsManager.getWakeWord());
        wakeWordField.setPrefWidth(250);
        wakeWordBox.getChildren().addAll(wakeWordLabel, wakeWordField);

        // Theme Selector
        HBox themeBox = new HBox(10);
        Label themeLabel = new Label("Theme:");
        themeLabel.setMinWidth(120);
        ComboBox<String> themeCombo = new ComboBox<>();
        themeCombo.getItems().addAll("light", "dark");
        themeCombo.setValue(settingsManager.getTheme());
        themeCombo.setPrefWidth(250);
        themeBox.getChildren().addAll(themeLabel, themeCombo);

        // Telemetry
        HBox telemetryBox = new HBox(10);
        Label telemetryLabel = new Label("Enable Telemetry:");
        telemetryLabel.setMinWidth(120);
        CheckBox telemetryCheck = new CheckBox();
        telemetryCheck.setSelected(settingsManager.isEnableTelemetry());
        telemetryBox.getChildren().addAll(telemetryLabel, telemetryCheck);

        content.getChildren().addAll(
            new Label("General Settings"),
            new Separator(),
            wakeWordBox,
            themeBox,
            telemetryBox
        );

        // Store references for saving
        content.setUserData(new HashMap<String, Object>() {{
            put("wakeWord", wakeWordField);
            put("theme", themeCombo);
            put("telemetry", telemetryCheck);
        }});

        tab.setContent(content);
        return tab;
    }

    private Tab createAudioTab() {
        Tab tab = new Tab();
        tab.setText("Audio");
        tab.setClosable(false);

        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        // Voice Selection
        HBox voiceBox = new HBox(10);
        Label voiceLabel = new Label("Voice:");
        voiceLabel.setMinWidth(120);
        ComboBox<String> voiceCombo = new ComboBox<>();
        voiceCombo.getItems().addAll("default", "male", "female");
        voiceCombo.setValue(settingsManager.getVoiceName());
        voiceCombo.setPrefWidth(250);
        voiceBox.getChildren().addAll(voiceLabel, voiceCombo);

        // Voice Gender
        HBox genderBox = new HBox(10);
        Label genderLabel = new Label("Voice Gender:");
        genderLabel.setMinWidth(120);
        ComboBox<String> genderCombo = new ComboBox<>();
        genderCombo.getItems().addAll("male", "female");
        genderCombo.setValue(settingsManager.getVoiceGender());
        genderCombo.setPrefWidth(250);
        genderBox.getChildren().addAll(genderLabel, genderCombo);

        // Volume Slider
        HBox volumeBox = new HBox(10);
        Label volumeLabel = new Label("Volume:");
        volumeLabel.setMinWidth(120);
        Slider volumeSlider = new Slider(0, 1.0, settingsManager.getVolume());
        volumeSlider.setShowTickLabels(true);
        volumeSlider.setShowTickMarks(true);
        volumeSlider.setMajorTickUnit(0.2);
        volumeSlider.setPrefWidth(250);
        Label volumeValue = new Label(String.format("%.0f%%", settingsManager.getVolume() * 100));
        volumeSlider.valueProperty().addListener((obs, old, val) ->
            volumeValue.setText(String.format("%.0f%%", val.doubleValue() * 100))
        );
        volumeBox.getChildren().addAll(volumeLabel, volumeSlider, volumeValue);

        // Speech Rate Slider
        HBox rateBox = new HBox(10);
        Label rateLabel = new Label("Speech Rate:");
        rateLabel.setMinWidth(120);
        Slider rateSlider = new Slider(0.5, 2.0, settingsManager.getSpeechRate());
        rateSlider.setShowTickLabels(true);
        rateSlider.setShowTickMarks(true);
        rateSlider.setMajorTickUnit(0.25);
        rateSlider.setPrefWidth(250);
        Label rateValue = new Label(String.format("%.2fx", settingsManager.getSpeechRate()));
        rateSlider.valueProperty().addListener((obs, old, val) ->
            rateValue.setText(String.format("%.2fx", val.doubleValue()))
        );
        rateBox.getChildren().addAll(rateLabel, rateSlider, rateValue);

        content.getChildren().addAll(
            new Label("Audio Settings"),
            new Separator(),
            voiceBox,
            genderBox,
            volumeBox,
            rateBox
        );

        // Store references for saving
        content.setUserData(new HashMap<String, Object>() {{
            put("voice", voiceCombo);
            put("gender", genderCombo);
            put("volume", volumeSlider);
            put("rate", rateSlider);
        }});

        tab.setContent(content);
        return tab;
    }

    private Tab createBrowserTab() {
        Tab tab = new Tab();
        tab.setText("Browser");
        tab.setClosable(false);

        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        // Preferred Browser
        HBox browserBox = new HBox(10);
        Label browserLabel = new Label("Preferred Browser:");
        browserLabel.setMinWidth(120);
        ComboBox<String> browserCombo = new ComboBox<>();
        browserCombo.getItems().addAll("chrome", "msedge", "firefox", "iexplore");
        browserCombo.setValue(settingsManager.getPreferredBrowser());
        browserCombo.setPrefWidth(250);
        browserBox.getChildren().addAll(browserLabel, browserCombo);

        // Test button
        Button testButton = new Button("Test Browser");
        testButton.setStyle("-fx-padding: 8px 15px;");
        testButton.setOnAction(e -> {
            String browser = browserCombo.getValue();
            logger.info("Testing browser: " + browser);
            try {
                String cmd = "start " + browser + " https://www.google.com";
                ProcessRunner.run(cmd, 5);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Browser Test");
                alert.setHeaderText("Browser Test Successful");
                alert.setContentText("Launching " + browser + "...");
                alert.showAndWait();
            } catch (Exception ex) {
                logger.error("Failed to test browser: " + browser, ex);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Browser Test Failed");
                alert.setHeaderText("Error");
                alert.setContentText("Failed to launch " + browser + ": " + ex.getMessage());
                alert.showAndWait();
            }
        });

        content.getChildren().addAll(
            new Label("Browser Settings"),
            new Separator(),
            browserBox,
            testButton,
            new Separator(),
            new Label("Note: Restart the application for browser preference changes to take effect.")
        );

        // Store references for saving
        content.setUserData(new HashMap<String, Object>() {{
            put("browser", browserCombo);
        }});

        tab.setContent(content);
        return tab;
    }

    private Tab createFoldersTab() {
        Tab tab = new Tab();
        tab.setText("Folders");
        tab.setClosable(false);

        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        Map<String, Object> folderControls = new HashMap<>();

        String[] folderTypes = {"Desktop", "Documents", "Downloads", "Music"};

        for (String folderType : folderTypes) {
            HBox folderBox = new HBox(10);
            Label folderLabel = new Label(folderType + ":");
            folderLabel.setMinWidth(120);

            TextField folderField = new TextField();
            folderField.setText(settingsManager.getFolderLocation(folderType));
            folderField.setPrefWidth(200);
            folderField.setEditable(false);

            Button browseButton = new Button("Browse...");
            browseButton.setStyle("-fx-padding: 5px 15px;");
            browseButton.setOnAction(e -> {
                DirectoryChooser chooser = new DirectoryChooser();
                chooser.setTitle("Select " + folderType + " Folder");
                chooser.setInitialDirectory(new File(folderField.getText()));
                File selectedDir = chooser.showDialog(stage);
                if (selectedDir != null) {
                    folderField.setText(selectedDir.getAbsolutePath());
                }
            });

            folderBox.getChildren().addAll(folderLabel, folderField, browseButton);
            content.getChildren().add(folderBox);
            folderControls.put(folderType, folderField);
        }

        content.getChildren().add(0, new Label("Default Folder Locations"));
        content.getChildren().add(1, new Separator());

        // Store references for saving
        content.setUserData(folderControls);

        tab.setContent(content);
        return tab;
    }

    private Tab createAdvancedTab() {
        Tab tab = new Tab();
        tab.setText("Advanced");
        tab.setClosable(false);

        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        // Log Level
        HBox logLevelBox = new HBox(10);
        Label logLevelLabel = new Label("Log Level:");
        logLevelLabel.setMinWidth(120);
        ComboBox<String> logLevelCombo = new ComboBox<>();
        logLevelCombo.getItems().addAll("TRACE (0)", "DEBUG (1)", "INFO (2)", "WARN (3)", "ERROR (4)");
        logLevelCombo.setValue("INFO (2)");
        logLevelCombo.setPrefWidth(250);
        logLevelBox.getChildren().addAll(logLevelLabel, logLevelCombo);

        content.getChildren().addAll(
            new Label("Advanced Settings"),
            new Separator(),
            logLevelBox,
            new Separator(),
            new Label("Language Model Path:"),
            new TextField(),
            new Label("Grammar File Path:"),
            new TextField()
        );

        // Store references for saving
        content.setUserData(new HashMap<String, Object>() {{
            put("logLevel", logLevelCombo);
        }});

        tab.setContent(content);
        return tab;
    }
    @SuppressWarnings("unchecked")
    private void saveSettings() {
        try {
            TabPane tabPane = (TabPane) scene.getRoot().getChildrenUnmodifiable().get(0);

            // General Tab
            Tab generalTab = tabPane.getTabs().get(0);
            Map<String, Object> generalData = (Map<String, Object>) generalTab.getContent().getUserData();
            settingsManager.setWakeWord(((TextField) generalData.get("wakeWord")).getText());
            settingsManager.setTheme(((ComboBox<String>) generalData.get("theme")).getValue());
            settingsManager.setEnableTelemetry(((CheckBox) generalData.get("telemetry")).isSelected());

            // Audio Tab
            Tab audioTab = tabPane.getTabs().get(1);
            Map<String, Object> audioData = (Map<String, Object>) audioTab.getContent().getUserData();
            settingsManager.setVoiceName(((ComboBox<String>) audioData.get("voice")).getValue());
            settingsManager.setVoiceGender(((ComboBox<String>) audioData.get("gender")).getValue());
            settingsManager.setVolume((float) ((Slider) audioData.get("volume")).getValue());
            settingsManager.setSpeechRate((float) ((Slider) audioData.get("rate")).getValue());

            // Browser Tab
            Tab browserTab = tabPane.getTabs().get(2);
            Map<String, Object> browserData = (Map<String, Object>) browserTab.getContent().getUserData();
            settingsManager.setPreferredBrowser(((ComboBox<String>) browserData.get("browser")).getValue());

            // Folders Tab
            Tab foldersTab = tabPane.getTabs().get(3);
            Map<String, Object> folderData = (Map<String, Object>) foldersTab.getContent().getUserData();
            for (String folderType : new String[]{"Desktop", "Documents", "Downloads", "Music"}) {
                String path = ((TextField) folderData.get(folderType)).getText();
                settingsManager.setFolderLocation(folderType, path);
            }

            // Advanced Tab
            Tab advancedTab = tabPane.getTabs().get(4);
            Map<String, Object> advancedData = (Map<String, Object>) advancedTab.getContent().getUserData();
            String logLevelStr = ((ComboBox<String>) advancedData.get("logLevel")).getValue();
            int logLevel = Integer.parseInt(logLevelStr.split(" ")[1].replace("(", "").replace(")", ""));
            settingsManager.setLogLevel(logLevel);

            logger.info("Preferences saved successfully.");
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Settings Saved");
            alert.setHeaderText("Success");
            alert.setContentText("Your preferences have been saved.");
            alert.showAndWait();

            stage.close();
        } catch (Exception ex) {
            logger.error("Error saving preferences", ex);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Save Error");
            alert.setHeaderText("Error Saving Preferences");
            alert.setContentText("An error occurred while saving: " + ex.getMessage());
            alert.showAndWait();
        }
    }

    private void resetToDefaults() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Reset to Defaults");
        confirmAlert.setHeaderText("Reset All Settings?");
        confirmAlert.setContentText("Are you sure you want to reset all settings to defaults? This action cannot be undone.");
        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            settingsManager.setVoiceName("default");
            settingsManager.setVolume(0.8f);
            settingsManager.setTheme("light");
            settingsManager.setEnableTelemetry(false);
            settingsManager.setWakeWord("friend");
            settingsManager.setPreferredBrowser("chrome");
            settingsManager.setSpeechRate(1.0f);
            settingsManager.setVoiceGender("male");
            settingsManager.setLogLevel(2);

            logger.info("Settings reset to defaults.");
            initializeUI();
            scene = stage.getScene();
            applyTheme();
        }
    }

    private void applyTheme() {
        String theme = settingsManager.getTheme();
        if ("dark".equals(theme)) {
            scene.getStylesheets().add(getClass().getResource("/theme-dark.css").toExternalForm());
        } else {
            scene.getStylesheets().add(getClass().getResource("/theme-light.css").toExternalForm());
        }
    }

    public void show() {
        stage.show();
    }

    public void close() {
        stage.close();
    }

    public Stage getStage() {
        return stage;
    }
}
