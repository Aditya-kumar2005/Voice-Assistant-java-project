package com.friend.friend;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Provides folder access and system control commands mapped to natural language verbs.
 * Designed for integration with voice assistants or command interpreters.
 */
public class FolderCommands {
    private final CommandDispatcher dispatcher; // Injected dependency for verbal feedback

    /**
     * List of verbs that imply an "open" or "launch" action.
     */
    private static final String[] OPENING_ACTIONS = {
        "open", "start", "launch", "run", "execute", "play", "use",
        "access", "go", "browse", "visit", "search", "find", "show",
        "display", "activate"
    };

    /**
     * Constructs a FolderCommands instance and populates the provided map
     * with folder access and system control actions.
     *
     * @param map A mutable command map.
     * @param dispatcher The CommandDispatcher for providing verbal feedback.
     */
    public FolderCommands(Map<String, Runnable> map,CommandDispatcher dispatcher) {
        this.dispatcher = dispatcher;
        
        String userHome = System.getProperty("user.home");

        // --- Folder Access Commands ---
        addFolderMapping(map, "downloads", userHome + "\\Downloads");
        addFolderMapping(map, "documents", userHome + "\\Documents");
        addFolderMapping(map, "desktop", userHome + "\\Desktop");
        addFolderMapping(map, "pictures", userHome + "\\Pictures");
        addFolderMapping(map, "music", userHome + "\\Music");
        addFolderMapping(map, "videos", userHome + "\\Videos");
        addFolderMapping(map, "project", userHome + "\\PROJECT");

        // Prefer the OneDrive environment variable when available
        String oneDriveEnv = System.getenv("OneDrive");
        if (oneDriveEnv != null && new File(oneDriveEnv).exists()) {
            addFolderMapping(map, "onedrive documents", oneDriveEnv + "\\Documents");
            addFolderMapping(map, "onedrive desktop", oneDriveEnv + "\\Desktop");
            addFolderMapping(map, "onedrive project", oneDriveEnv + "\\Desktop\\PROJECT");
        } else {
            // fallback heuristic: try a OneDrive path under userHome
            String oneDriveFallback = userHome + "\\OneDrive";
            if (new File(oneDriveFallback).exists()) {
                addFolderMapping(map, "onedrive documents", oneDriveFallback + "\\Documents");
                addFolderMapping(map, "onedrive desktop", oneDriveFallback + "\\Desktop");
                addFolderMapping(map, "onedrive project", oneDriveFallback + "\\Desktop\\PROJECT");
            } else {
                System.out.println("[FolderCommands]: No OneDrive path found; skipping OneDrive mappings.");
            }
        }

        // --- System Control Commands ---
        mapActions(map, "enable hotspot", this::enableHotspot);
        mapActions(map, "disable hotspot", this::disableHotspot);
        mapActions(map, "enable energy saver", this::enableEnergySaver);
        mapActions(map, "disable energy saver", this::disableEnergySaver);
        mapActions(map, "enable wifi", this::enableWiFi);
        mapActions(map, "disable wifi", this::disableWiFi);
        mapActions(map, "increase volume", () -> adjustVolume(true));
        mapActions(map, "decrease volume", () -> adjustVolume(false));
        mapActions(map, "increase brightness", () -> adjustBrightness(true));
        mapActions(map, "decrease brightness", () -> adjustBrightness(false));
    }

    /**
     * Adds a folder mapping only if the path exists on disk.
     */
    private void addFolderMapping(Map<String, Runnable> map, String target, String path) {
        File f = new File(path);
        if (f.exists() && f.isDirectory()) {
            mapActions(map, target, () -> open(path));
        } else {
            System.out.println("[FolderCommands]: Skipping mapping for '" + target + "' because path not found: " + path);
        }
    }

    /**
     * Maps all opening verbs to a given target phrase and action.
     */
    private void mapActions(Map<String, Runnable> map, String target, Runnable action) {
        for (String verb : OPENING_ACTIONS) {
            map.put(verb + " " + target, action);
        }
        // Also map the target command alone for simple system controls
        if (!target.contains(" ")) {
            map.put(target, action);
        }
    }

    /**
     * Opens a folder using Windows Explorer and provides verbal feedback with friendly responses.
     */
    private void open(String path) {
        File f = new File(path);
        if (!f.exists() || !f.isDirectory()) {
            String friendlyError = FriendlyBehavior.fileNotFound(f.getName());
            dispatcher.speakResponse(friendlyError);
            System.out.println("[FolderCommands]: Attempted to open missing folder: " + path);
            return;
        }

        // Use ProcessRunner to run explorer with the path argument (avoids quoting issues)
        try {
            int rc = ProcessRunner.run(List.of("cmd", "/c", "explorer", path), 5);
            if (rc == 0) {
                String friendlySuccess = FriendlyBehavior.taskComplete("Opening " + f.getName());
                dispatcher.speakResponse(friendlySuccess);
                System.out.println("[FolderCommands]: Successfully executed explorer for: " + path);
            } else {
                String friendlyError = FriendlyBehavior.apologize() + " " + FriendlyBehavior.appFailedToLaunch("File Explorer");
                dispatcher.speakResponse(friendlyError);
                System.out.println("[FolderCommands]: Explorer returned exit=" + rc + " for: " + path);
            }
        } catch (IOException | InterruptedException e) {
            String friendlyError = FriendlyBehavior.appFailedToLaunch("File Explorer");
            dispatcher.speakResponse(friendlyError);
            System.out.println("[FolderCommands]: Failed to open: " + path);
            e.printStackTrace();
        }
    }

    // --- System Control Methods (All now use dispatcher for verbal confirmation) ---

    private void enableHotspot() {
        runPowerShell("netsh wlan set hostednetwork mode=allow ssid=MyHotspot key=12345678; netsh wlan start hostednetwork", "Hotspot enabled.");
    }

    private void disableHotspot() {
        runPowerShell("netsh wlan stop hostednetwork", "Hotspot disabled.");
    }

    private void enableEnergySaver() {
        runPowerShell("powercfg /setactive a1841308-3541-4fab-bc81-f71556f20b4a", "Power saver enabled.");
    }

    private void disableEnergySaver() {
        runPowerShell("powercfg /setactive 381b4222-f694-41f0-9685-ff5bb260df2e", "Energy saver disabled.");
    }

    private void enableWiFi() {
        runPowerShell("Enable-NetAdapter -Name \"Wi-Fi\" -Confirm:$false", "Wi-Fi enabled.");
    }

    private void disableWiFi() {
        runPowerShell("Disable-NetAdapter -Name \"Wi-Fi\" -Confirm:$false", "Wi-Fi disabled.");
    }

    private void adjustVolume(boolean increase) {
        String script = increase ? "nircmd.exe changesysvolume 5000" : "nircmd.exe changesysvolume -5000";
        String response = increase ? "Increasing volume." : "Decreasing volume.";
        runCommand(script, response);
    }

    private void adjustBrightness(boolean increase) {
        int value = increase ? 10 : -10;
        String script = "(Get-WmiObject -Namespace root\\wmi -Class WmiMonitorBrightnessMethods).WmiSetBrightness(1, [Math]::Max(0, [Math]::Min(100, ((Get-WmiObject -Namespace root\\wmi -Class WmiMonitorBrightness).CurrentBrightness + " + value + "))))";
        String response = increase ? "Increasing brightness." : "Decreasing brightness.";
        runPowerShell(script, response);
    }

    /**
     * Executes a PowerShell command and provides verbal feedback.
     */
    private void runPowerShell(String command, String successResponse) {
        try {
            int rc = ProcessRunner.run(List.of("powershell", "-Command", command), 8);
            if (rc == 0) {
                String friendlySuccess = FriendlyBehavior.taskComplete(successResponse);
                dispatcher.speakResponse(friendlySuccess);
                System.out.println("[FolderCommands]: PowerShell executed: " + command);
            } else {
                String friendlyError = FriendlyBehavior.apologize();
                dispatcher.speakResponse(friendlyError);
                System.out.println("[FolderCommands]: PowerShell returned exit=" + rc + " for: " + command);
            }
        } catch (IOException | InterruptedException e) {
            String friendlyError = FriendlyBehavior.appFailedToLaunch("system command");
            dispatcher.speakResponse(friendlyError);
            System.out.println("[FolderCommands]: PowerShell command failed: " + command);
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Executes a standard CMD command and provides verbal feedback with friendly responses.
     */
    private void runCommand(String command, String successResponse) {
        try {
            int rc = ProcessRunner.run(List.of("cmd", "/c", command), 8);
            if (rc == 0) {
                String friendlySuccess = FriendlyBehavior.taskComplete(successResponse);
                dispatcher.speakResponse(friendlySuccess);
                System.out.println("[FolderCommands]: CMD command executed: " + command);
            } else {
                String friendlyError = FriendlyBehavior.apologize();
                dispatcher.speakResponse(friendlyError);
                System.out.println("[FolderCommands]: CMD returned exit=" + rc + " for: " + command);
            }
        } catch (IOException | InterruptedException e) {
            String friendlyError = FriendlyBehavior.appFailedToLaunch("system command");
            dispatcher.speakResponse(friendlyError);
            System.out.println("[FolderCommands]: Command failed: " + command);
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
}