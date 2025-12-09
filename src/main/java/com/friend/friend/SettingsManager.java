package com.friend.friend;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.io.File;

/**
 * SettingsManager persists user preferences: voice, volume, theme, telemetry, and first-launch state.
 */
public class SettingsManager {
    private static final Logger logger = LoggerFactory.getLogger(SettingsManager.class);
    private static final String SETTINGS_FILE = "settings.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private Settings settings;

    public SettingsManager() {
        loadSettings();
    }

    private void loadSettings() {
        try {
            if (Files.exists(Paths.get(SETTINGS_FILE))) {
                String json = new String(Files.readAllBytes(Paths.get(SETTINGS_FILE)));
                settings = gson.fromJson(json, Settings.class);
                logger.info("Settings loaded from " + SETTINGS_FILE);
            } else {
                settings = new Settings();
                saveSettings();
                logger.info("New settings created with defaults.");
            }
        } catch (Exception ex) {
            logger.error("Failed to load settings; using defaults.", ex);
            settings = new Settings();
        }
    }

    private void saveSettings() {
        try {
            String json = gson.toJson(settings);
            Files.write(Paths.get(SETTINGS_FILE), json.getBytes());
            logger.info("Settings saved to " + SETTINGS_FILE);
        } catch (IOException ex) {
            logger.error("Failed to save settings.", ex);
        }
    }

    public String getVoiceName() {
        return settings.voiceName;
    }

    public void setVoiceName(String voiceName) {
        settings.voiceName = voiceName;
        saveSettings();
        logger.info("Voice changed to: " + voiceName);
    }

    public float getVolume() {
        return settings.volume;
    }

    public void setVolume(float volume) {
        settings.volume = Math.max(0, Math.min(1.0f, volume));
        saveSettings();
        logger.info("Volume set to: " + settings.volume);
    }

    public String getTheme() {
        return settings.theme;
    }

    public void setTheme(String theme) {
        settings.theme = theme;
        saveSettings();
        logger.info("Theme set to: " + theme);
    }

    public boolean isEnableTelemetry() {
        return settings.enableTelemetry;
    }

    public void setEnableTelemetry(boolean enable) {
        settings.enableTelemetry = enable;
        saveSettings();
        logger.info("Telemetry " + (enable ? "enabled" : "disabled"));
    }

    public String getWakeWord() {
        return settings.wakeWord;
    }

    public void setWakeWord(String wakeWord) {
        settings.wakeWord = wakeWord;
        saveSettings();
        logger.info("Wake word set to: " + wakeWord);
    }

    public boolean isFirstLaunch() {
        return settings.firstLaunch;
    }

    public void setFirstLaunchCompleted() {
        settings.firstLaunch = false;
        saveSettings();
        logger.info("First launch marked as completed.");
    }

    public String getPreferredBrowser() {
        return settings.preferredBrowser;
    }

    public void setPreferredBrowser(String browser) {
        settings.preferredBrowser = browser;
        saveSettings();
        logger.info("Preferred browser set to: " + browser);
    }

    public Map<String, String> getFolderLocations() {
        return settings.folderLocations;
    }

    public void setFolderLocation(String folderType, String path) {
        settings.folderLocations.put(folderType, path);
        saveSettings();
        logger.info("Folder location set - " + folderType + ": " + path);
    }

    public String getFolderLocation(String folderType) {
        return settings.folderLocations.getOrDefault(folderType, System.getProperty("user.home"));
    }

    public float getSpeechRate() {
        return settings.speechRate;
    }

    public void setSpeechRate(float speechRate) {
        settings.speechRate = Math.max(0.5f, Math.min(2.0f, speechRate));
        saveSettings();
        logger.info("Speech rate set to: " + settings.speechRate);
    }

    public String getVoiceGender() {
        return settings.voiceGender;
    }

    public void setVoiceGender(String gender) {
        settings.voiceGender = gender;
        saveSettings();
        logger.info("Voice gender set to: " + gender);
    }

    public int getLogLevel() {
        return settings.logLevel;
    }

    public void setLogLevel(int level) {
        settings.logLevel = level;
        saveSettings();
        logger.info("Log level set to: " + level);
    }

    public Map<String, Object> getSettingsAsMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("voiceName", settings.voiceName);
        map.put("volume", settings.volume);
        map.put("theme", settings.theme);
        map.put("enableTelemetry", settings.enableTelemetry);
        map.put("wakeWord", settings.wakeWord);
        map.put("firstLaunch", settings.firstLaunch);
        map.put("preferredBrowser", settings.preferredBrowser);
        map.put("folderLocations", settings.folderLocations);
        map.put("speechRate", settings.speechRate);
        map.put("voiceGender", settings.voiceGender);
        map.put("logLevel", settings.logLevel);
        return map;
    }

    /**
     * Settings POJO for JSON serialization.
     */
    public static class Settings {
        public String voiceName = "default";
        public float volume = 0.8f;
        public String theme = "light";
        public boolean enableTelemetry = false;
        public String wakeWord = "friend";
        public boolean firstLaunch = true;
        public String preferredBrowser = "chrome";
        public Map<String, String> folderLocations = new HashMap<>();
        public float speechRate = 1.0f;
        public String voiceGender = "male";
        public int logLevel = 2; // 0=TRACE, 1=DEBUG, 2=INFO, 3=WARN, 4=ERROR

        public Settings() {
            // Initialize folder locations with default user directories
            folderLocations.put("Desktop", System.getProperty("user.home") + File.separator + "Desktop");
            folderLocations.put("Documents", System.getProperty("user.home") + File.separator + "Documents");
            folderLocations.put("Downloads", System.getProperty("user.home") + File.separator + "Downloads");
            folderLocations.put("Music", System.getProperty("user.home") + File.separator + "Music");
        }
    }
}
