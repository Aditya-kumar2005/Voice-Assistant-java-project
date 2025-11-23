package com.friend.friend;

import java.io.IOException;
import java.util.Map;

/**
 * Provides folder access and system control commands mapped to natural language verbs.
 * Designed for integration with voice assistants or command interpreters.
 */
public class FolderCommands {

    /**
     * List of verbs that imply an "open" or "launch" action.
     * Used to map multiple natural language triggers to the same folder or system command.
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
     * @param map A mutable command map where keys are natural language phrases
     * and values are Runnable actions to execute.
     */
    public FolderCommands(Map<String, Runnable> map) {
        String userHome = System.getProperty("user.home");

        // --- Folder Access Commands ---
        mapActions(map, "downloads", () -> open(userHome + "\\Downloads"));
        mapActions(map, "documents", () -> open(userHome + "\\Documents"));
        mapActions(map, "desktop", () -> open(userHome + "\\Desktop"));
        mapActions(map, "pictures", () -> open(userHome + "\\Pictures"));
        mapActions(map, "music", () -> open(userHome + "\\Music"));
        mapActions(map, "videos", () -> open(userHome + "\\Videos"));
        mapActions(map, "project", () -> open(userHome + "\\PROJECT"));

        // Optional fallback for OneDrive paths (customized for another user)
        // NOTE: The path assumes a Windows structure and may need adjustments if the application runs on a different OS.
        String oneDrive = userHome.replace("Aditya", "nanua") + "\\OneDrive";
        mapActions(map, "onedrive documents", () -> open(oneDrive + "\\Documents"));
        mapActions(map, "onedrive desktop", () -> open(oneDrive + "\\Desktop"));
        mapActions(map, "onedrive project", () -> open(oneDrive + "\\Desktop\\PROJECT"));

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
     * Maps all opening verbs to a given target phrase and action.
     *
     * @param map    The command map to populate.
     * @param target The noun or phrase to be triggered (e.g., "downloads").
     * @param action The Runnable to execute when triggered.
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
     * Opens a folder using Windows Explorer.
     *
     * @param path Absolute path to the folder.
     */
    private void open(String path) {
        try {
            // Use cmd /c explorer to ensure compatibility across different Windows environments
            String command = "explorer \"" + path + "\"";
            String[] commandArray = {"cmd", "/c", command};
            Runtime.getRuntime().exec(commandArray);
            System.out.println("[FolderCommands]: Successfully executed: " + command);
        } catch (IOException e) {
            System.out.println("[FolderCommands]: Failed to open: " + path);
            e.printStackTrace();
        }
    }

    // --- System Control Methods ---

    /**
     * Enables Windows hotspot using netsh.
     */
    private void enableHotspot() {
        runPowerShell("netsh wlan set hostednetwork mode=allow ssid=MyHotspot key=12345678; netsh wlan start hostednetwork");
    }

    /**
     * Disables Windows hotspot.
     */
    private void disableHotspot() {
        runPowerShell("netsh wlan stop hostednetwork");
    }

    /**
     * Switches to the Power Saver plan using its GUID.
     */
    private void enableEnergySaver() {
        runPowerShell("powercfg /setactive a1841308-3541-4fab-bc81-f71556f20b4a");
    }

    /**
     * Switches to the Balanced power plan using its GUID.
     */
    private void disableEnergySaver() {
        runPowerShell("powercfg /setactive 381b4222-f694-41f0-9685-ff5bb260df2e");
    }

    /**
     * Enables the Wi-Fi adapter.
     */
    private void enableWiFi() {
        runPowerShell("Enable-NetAdapter -Name \"Wi-Fi\" -Confirm:$false");
    }

    /**
     * Disables the Wi-Fi adapter.
     */
    private void disableWiFi() {
        runPowerShell("Disable-NetAdapter -Name \"Wi-Fi\" -Confirm:$false");
    }

    /**
     * Adjusts system volume using NirCmd.
     *
     * @param increase If true, increases volume; otherwise decreases.
     */
    private void adjustVolume(boolean increase) {
        // Requires NirCmd to be in the system PATH
        String script = increase ? "nircmd.exe changesysvolume 5000" : "nircmd.exe changesysvolume -5000";
        runCommand(script);
    }

    /**
     * Adjusts screen brightness using WMI.
     *
     * @param increase If true, increases brightness; otherwise decreases.
     */
    private void adjustBrightness(boolean increase) {
        // Uses WMI via PowerShell to interact with the display adapter
        int value = increase ? 10 : -10;
        runPowerShell("(Get-WmiObject -Namespace root\\wmi -Class WmiMonitorBrightnessMethods).WmiSetBrightness(1, [Math]::Max(0, [Math]::Min(100, ((Get-WmiObject -Namespace root\\wmi -Class WmiMonitorBrightness).CurrentBrightness + " + value + "))))");
    }

    /**
     * Executes a PowerShell command.
     *
     * @param command The PowerShell command to run.
     */
    private void runPowerShell(String command) {
        try {
            String[] cmd = {"powershell", "-Command", command};
            Runtime.getRuntime().exec(cmd);
            System.out.println("[FolderCommands]: PowerShell executed: " + command);
        } catch (IOException e) {
            System.out.println("[FolderCommands]: PowerShell command failed: " + command);
            e.printStackTrace();
        }
    }

    /**
     * Executes a standard CMD command.
     *
     * @param command The command to run.
     */
    private void runCommand(String command) {
        try {
            String[] cmd = {"cmd", "/c", command};
            Runtime.getRuntime().exec(cmd);
            System.out.println("[FolderCommands]: CMD command executed: " + command);
        } catch (IOException e) {
            System.out.println("[FolderCommands]: Command failed: " + command);
            e.printStackTrace();
        }
    }
}