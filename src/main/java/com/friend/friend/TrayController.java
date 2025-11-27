package com.friend.friend;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.net.URL; 
import javax.swing.ImageIcon;
import java.awt.Image; // Added for the updateTrayIcon method

/**
 * Manages the System Tray icon, menu, and dynamic state updates.
 */
public class TrayController {

    private final EchoPilotRecognizer recognizer;
    private final TrayIcon trayIcon; // Stored as a class field for updates
    
    // Icon paths (Must be accessible from the classpath)
    private static final String ICON_ACTIVE_PATH = "/images/_mic.png"; // Original icon
    private static final String ICON_PAUSED_PATH = "/images/_mic_off.png"; // Placeholder for a paused state icon

    public TrayController(EchoPilotRecognizer recognizer, Runnable shutdownAction) throws AWTException {
        this.recognizer = recognizer;

        if (!SystemTray.isSupported()) {
            System.err.println("[TrayController] SystemTray is not supported. Skipping.");
            this.trayIcon = null; // Set to null if not supported
            return;
        }

        final PopupMenu popup = new PopupMenu();
        final SystemTray tray = SystemTray.getSystemTray();

        // --- Resource Loading and Initialization ---
        Image initialImage = loadIcon(ICON_ACTIVE_PATH);
        
        // Initialize the class field with the created TrayIcon
        this.trayIcon = new TrayIcon(initialImage, "EchoPilot - Active", popup); 
        this.trayIcon.setImageAutoSize(true);

        // --- Menu items ---
        MenuItem resumeItem = new MenuItem("Resume Listening");
        MenuItem pauseItem = new MenuItem("Pause Listening");
        MenuItem exitItem = new MenuItem("Exit");

        // Use the new updateTrayIcon method to reflect state changes
        resumeItem.addActionListener(e -> {
            recognizer.resume();
            updateTrayIcon(true);
        });
        
        pauseItem.addActionListener(e -> {
            recognizer.pause();
            updateTrayIcon(false);
        });
        
        exitItem.addActionListener(e -> shutdownAction.run());

        // Add components to popup menu
        popup.add(resumeItem);
        popup.add(pauseItem);
        popup.addSeparator();
        popup.add(exitItem);

        try {
            tray.add(this.trayIcon);
            System.out.println("[TrayController] Tray icon initialized.");
        } catch (AWTException e) {
            System.err.println("[TrayController] Failed to add icon to system tray: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Helper to safely load an icon from the classpath.
     * @param path The resource path.
     * @return The loaded Image or a blank image if not found.
     */
    private Image loadIcon(String path) {
        URL iconURL = TrayController.class.getResource(path);
        
        if (iconURL == null) {
            System.err.println("[TrayController] Using text fallback as " + path + " is missing from classpath.");
            // Use a blank image
            return new ImageIcon("").getImage(); 
        } else {
            return new ImageIcon(iconURL).getImage();
        }
    }

    /**
     * Updates the tray icon and tooltip to reflect the current listening state.
     * This method can be called from outside (e.g., by the Recognizer state change listener).
     * @param isActive true if the recognizer is actively listening, false otherwise.
     */
    public void updateTrayIcon(boolean isActive) {
        if (this.trayIcon == null) return; // Exit if not supported or initialized

        String newTooltip;
        Image newImage;

        if (isActive) {
            newTooltip = "EchoPilot - Listening";
            newImage = loadIcon(ICON_ACTIVE_PATH);
        } else {
            newTooltip = "EchoPilot - Paused";
            newImage = loadIcon(ICON_PAUSED_PATH);
        }
        
        // Update the visual representation
        this.trayIcon.setImage(newImage);
        this.trayIcon.setToolTip(newTooltip);
        System.out.println("[TrayController] State updated: " + newTooltip);
    }
}