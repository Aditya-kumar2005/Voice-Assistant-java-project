package com.friend.friend;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.net.URL; // Required for getResource() check
import javax.swing.ImageIcon;

/**
 * Placeholder for the System Tray icon management.
 */
public class TrayController {

    private final EchoPilotRecognizer recognizer;

    public TrayController(EchoPilotRecognizer recognizer, Runnable shutdownAction) throws AWTException {
        this.recognizer = recognizer;

        if (!SystemTray.isSupported()) {
            System.err.println("[TrayController] SystemTray is not supported. Skipping.");
            return;
        }

        final PopupMenu popup = new PopupMenu();
        final TrayIcon trayIcon;
        final SystemTray tray = SystemTray.getSystemTray();

        // --- Resource Loading and Null Check Fix ---
        // 1. Get the resource URL (should be /path/from/classpath/root)
        URL iconURL = TrayController.class.getResource("/images/_mic.png"); 
        
        if (iconURL == null) {
            // 2. Fallback if the icon is missing (prevents NullPointerException)
            System.err.println("[TrayController] Using text fallback as /images/_mic.png is missing from classpath.");
            // Use an empty image object, relying on the text fallback
            trayIcon = new TrayIcon(new ImageIcon("").getImage(), "EchoPilot", popup);
        } else {
            // 3. Use the successfully loaded image
            ImageIcon icon = new ImageIcon(iconURL);
            
            // NOTE: Removed the MediaTracker check as it's often unreliable 
            // and the ImageIcon(URL) constructor usually handles basic loading.
            trayIcon = new TrayIcon(icon.getImage(), "EchoPilot", popup);
        }
        
        trayIcon.setImageAutoSize(true);

        // --- Menu items ---
        MenuItem resumeItem = new MenuItem("Resume Listening");
        MenuItem pauseItem = new MenuItem("Pause Listening");
        MenuItem exitItem = new MenuItem("Exit");

        resumeItem.addActionListener(e -> recognizer.resume());
        pauseItem.addActionListener(e -> recognizer.pause());
        exitItem.addActionListener(e -> shutdownAction.run());

        // Add components to popup menu
        popup.add(resumeItem);
        popup.add(pauseItem);
        popup.addSeparator();
        popup.add(exitItem);

        try {
            tray.add(trayIcon);
            System.out.println("[TrayController] Tray icon initialized.");
        } catch (AWTException e) {
            System.err.println("[TrayController] Failed to add icon to system tray: " + e.getMessage());
            throw e;
        }
    }
}