package com.friend.friend;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Provides folder access and system control commands mapped to natural language verbs.
 * Designed for integration with voice assistants or command interpreters.
 */
public class FolderCommands {

    private final FileSearcher fileSearcher;
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
     * @param fileSearcher The FileSearcher utility for local search logic (currently unused here, but injected).
     * @param dispatcher The CommandDispatcher for providing verbal feedback.
     */
    public FolderCommands(Map<String, Runnable> map, FileSearcher fileSearcher, CommandDispatcher dispatcher) {
        this.fileSearcher = fileSearcher;
        this.dispatcher = dispatcher;
        
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
     * Opens a folder using Windows Explorer and provides verbal feedback.
     */
    private void open(String path) {
        try {
            // Use cmd /c explorer to ensure compatibility across different Windows environments
            String command = "explorer \"" + path + "\"";
            String[] commandArray = {"cmd", "/c", command};
            Runtime.getRuntime().exec(commandArray);
            
            String friendlyName = new File(path).getName();
            dispatcher.speakResponse("Opening your " + friendlyName + " folder.");

            System.out.println("[FolderCommands]: Successfully executed: " + command);
        } catch (IOException e) {
            dispatcher.speakResponse("Sorry, I could not open that folder.");
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
            String[] cmd = {"powershell", "-Command", command};
            Runtime.getRuntime().exec(cmd);
            dispatcher.speakResponse(successResponse);
            System.out.println("[FolderCommands]: PowerShell executed: " + command);
        } catch (IOException e) {
            dispatcher.speakResponse("Sorry, I couldn't run that command.");
            System.out.println("[FolderCommands]: PowerShell command failed: " + command);
            e.printStackTrace();
        }
    }

    /**
     * Executes a standard CMD command and provides verbal feedback.
     */
    private void runCommand(String command, String successResponse) {
        try {
            String[] cmd = {"cmd", "/c", command};
            Runtime.getRuntime().exec(cmd);
            dispatcher.speakResponse(successResponse);
            System.out.println("[FolderCommands]: CMD command executed: " + command);
        } catch (IOException e) {
            dispatcher.speakResponse("Sorry, I couldn't run that command.");
            System.out.println("[FolderCommands]: Command failed: " + command);
            e.printStackTrace();
        }
    }
}