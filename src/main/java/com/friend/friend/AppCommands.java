package com.friend.friend;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AppCommands maps natural language commands to system-level application actions.
 * Supports opening and closing common utilities, IDEs, browsers, web pages, and terminals.
 * Uses FriendlyBehavior to provide empathetic responses when errors occur.
 */
public class AppCommands {
    private static final Logger logger = LoggerFactory.getLogger(AppCommands.class);
    private final SettingsManager settingsManager;
    private final SpeechEngine speechEngine; // Made final and non-static
    private final EchoPilotRecognizer recognizer; // Made final
    private final WebGui gui; // Made final

    /**
     * Verbs that imply launching or opening an application.
     */
    private static final String[] OPENING_ACTIONS = {
        "open", "start", "launch", "run", "execute", "play", "use",
        "access", "go", "browse", "visit", "show",
        "display", "activate"
    };

    /**
     * Verbs that imply closing or terminating an application.
     */
    private static final String[] CLOSING_ACTIONS = {
        "close", "exit", "quit", "stop", "terminate", "end"
    };
    
    /**
     * Constructor that populates the provided command map with open/close actions.
     * * @param map A mutable map of command phrases to Runnable actions.
     * @param settingsManager The SettingsManager for retrieving user preferences.
     * @param speechEngine The active SpeechEngine for providing audio feedback.
     * @param recognizer The active EchoPilotRecognizer instance.
     * @param gui The active WebGui instance for status updates.
     */
    public AppCommands(Map<String, Runnable> map, SettingsManager settingsManager, SpeechEngine speechEngine, EchoPilotRecognizer recognizer, WebGui gui) {
        this.settingsManager = settingsManager;
        this.speechEngine = speechEngine;
        this.recognizer = recognizer;
        this.gui = gui;
        
        // Story: This is like writing a big instruction list for a robot 🤖. 
        // For every command ("open notepad"), the robot gets two instructions: one for "open" and one for "close".

        // --- System Utilities ---
        mapActions(map, "notepad",            () -> exec("notepad"),              () -> exec("taskkill /IM notepad.exe /F"));
        mapActions(map, "calculator",         () -> exec("calc"),                 () -> exec("taskkill /IM calc.exe /F"));
        mapActions(map, "c m d",              () -> exec("cmd"),                  () -> exec("taskkill /IM cmd.exe /F"));
        mapActions(map, "task manager",       () -> exec("taskmgr"),              () -> exec("taskkill /IM taskmgr.exe /F"));
        mapActions(map, "task scheduler",     () -> exec("taskschd.msc"),         () -> exec("taskkill /IM mmc.exe /F"));
        mapActions(map, "snipping tool",      () -> exec("snippingtool"),         () -> exec("taskkill /IM SnippingTool.exe /F"));
        mapActions(map, "paint",              () -> exec("mspaint"),              () -> exec("taskkill /IM mspaint.exe /F"));
        mapActions(map, "calendar",           () -> exec("start outlookcal:"),    () -> exec("taskkill /IM outlook.exe /F"));
        mapActions(map, "clock",              () -> exec("start clock:"),         () -> exec("taskkill /IM Time.exe /F"));
        mapActions(map, "settings",           () -> exec("start settings:"),      () -> exec("taskkill /IM SystemSettings.exe /F"));
        mapActions(map, "control panel",      () -> exec("control"),              () -> exec("taskkill /IM control.exe /F"));
        mapActions(map, "whatsapp",           () -> exec("start shell:AppsFolder\\5319275A.WhatsAppDesktop_cv1g1gvanyjgm!App"), () -> exec("taskkill /IM WhatsApp.exe /F"));

        // --- Terminals and Shells ---
        mapActions(map, "windows terminal",   () -> exec("start wt"),             () -> exec("taskkill /IM WindowsTerminal.exe /F"));
        mapActions(map, "git bash",           () -> exec("start \"\" \"C:\\Program Files\\Git\\git-bash.exe\""), () -> exec("taskkill /IM git-bash.exe /F"));
        mapActions(map, "python shell",       () -> exec("start python"),         () -> exec("taskkill /IM python.exe /F"));
        
        // --- MS Office Apps ---
        // FIX: Corrected misspelled commands like "wors" to "winword"
        mapActions(map, "word",               () -> exec("start winword"),        () -> exec("taskkill /IM WINWORD.EXE /F"));
        mapActions(map, "excel",              () -> exec("start excel"),          () -> exec("taskkill /IM EXCEL.EXE /F"));
        mapActions(map, "powerpoint",         () -> exec("start powerpnt"),       () -> exec("taskkill /IM POWERPNT.EXE /F"));
        mapActions(map, "access",             () -> exec("start msaccess"),       () -> exec("taskkill /IM MSACCESS.EXE /F")); // Corrected from 'aces'
        mapActions(map, "publisher",          () -> exec("start mspub"),          () -> exec("taskkill /IM MSPUB.EXE /F")); // Corrected from 'publiser'
        mapActions(map, "onenote",            () -> exec("start onenote"),        () -> exec("taskkill /IM ONENOTE.EXE /F"));
        
        // --- Communication/Collaboration ---
        mapActions(map, "teams",              () -> exec("start teams"),          () -> exec("taskkill /IM Teams.exe /F"));
        
        // --- Web Browsers ---
        mapActions(map, "chrome",             () -> exec("start chrome"),         () -> exec("taskkill /IM chrome.exe /F"));
        mapActions(map, "firefox",            () -> exec("start firefox"),        () -> exec("taskkill /IM firefox.exe /F"));
        mapActions(map, "edge",               () -> exec("start msedge"),         () -> exec("taskkill /IM msedge.exe /F"));
        mapActions(map, "internet explorer",  () -> exec("start iexplore"),       () -> exec("taskkill /IM iexplore.exe /F"));
        
        // --- Productivity / IDEs ---
        mapActions(map, "code",               () -> exec("code"),                 () -> exec("taskkill /IM Code.exe /F")); // 'code' is the standard VS Code command
        mapActions(map, "studio",             () -> exec("start devenv"),         () -> exec("taskkill /IM devenv.exe /F")); // Visual Studio
        mapActions(map, "eclipse",            () -> exec("start eclipse"),        () -> exec("taskkill /IM eclipse.exe /F"));
        mapActions(map, "apache netbeans",    () -> exec("start netbeans"),       () -> exec("taskkill /IM netbeans64.exe /F"));
        mapActions(map, "apache",             () -> exec("start netbeans"),       () -> exec("taskkill /IM netbeans64.exe /F"));
        mapActions(map, "netbeans",           () -> exec("start netbeans"),       () -> exec("taskkill /IM netbeans64.exe /F"));

        // --- Dedicated System Features ---
        mapActions(map, "voice typing", 
            () -> {
                if (recognizer != null) recognizer.releaseMicAndStopRecognition();
                exec("start wisptis.exe"); // Windows Pen/Ink Input Service
            },
            () -> exec("taskkill /IM wisptis.exe /F"));
        
        mapActions(map, "voice access", 
            () -> exec("start ms-settings:easeofaccess-speechrecognition"),
            () -> exec("taskkill /IM SapiService.exe /F"));
        
        mapActions(map, "typing", 
            () -> {
                if (recognizer != null) recognizer.releaseMicAndStopRecognition();
                exec("start wisptis.exe");
            },
            () -> exec("taskkill /IM wisptis.exe /F"));
        
        mapActions(map, "online mode", 
            () -> exec("start ms-settings:easeofaccess-speechrecognition"),
            () -> exec("taskkill /IM SapiService.exe /F"));
            
        mapActions(map, "copilot",            () -> exec("start copilot"),        () -> exec("taskkill /IM Copilot.exe /F"));

        // --- Web Pages (opened via Preferred Browser, closed by killing the browser) ---
        // Story: This is like asking a dog 🐕 to fetch a ball (the URL). It uses the best fetch path (the preferred browser).
        mapWebActions(map, "gmail",           "https://mail.google.com");
        mapWebActions(map, "youtube",         "https://www.youtube.com");
        mapWebActions(map, "google",          "https://www.google.com");
        mapWebActions(map, "facebook",        "https://www.facebook.com");
        mapWebActions(map, "twitter",         "https://www.twitter.com");
        mapWebActions(map, "instagram",       "https://www.instagram.com");
        mapWebActions(map, "linkedin",        "https://www.linkedin.com");
        mapWebActions(map, "stack overflow",  "https://stackoverflow.com");
        mapWebActions(map, "github",          "https://github.com");
        mapWebActions(map, "reddit",          "https://www.reddit.com");
        mapWebActions(map, "quora",           "https://www.quora.com");
        mapWebActions(map, "amazon",          "https://www.amazon.com");
        mapWebActions(map, "flipkart",        "https://www.flipkart.com");
        mapWebActions(map, "news",            "https://news.google.com");
        mapWebActions(map, "weather",         "https://weather.com");
    }
    
    // Empty constructor is removed to enforce correct dependency injection.
    
    /**
     * Maps all opening and closing verbs to a given target and its corresponding actions.
     */
    private void mapActions(Map<String, Runnable> map, String target, Runnable openAction, Runnable closeAction) {
        for (String verb : OPENING_ACTIONS) {
            map.put(verb + " " + target, openAction);
        }
        for (String verb : CLOSING_ACTIONS) {
            map.put(verb + " " + target, closeAction);
        }
    }
    
    /**
     * Maps web actions using the preferred browser and the browser kill warning.
     */
    private void mapWebActions(Map<String, Runnable> map, String target, String url) {
        // OPEN action uses the preferred browser
        Runnable openAction = () -> openUrl(url); 
        
        // CLOSE action issues a warning about closing the entire browser
        String browserName = getPreferredBrowser();
        String browserKillCommand = "taskkill /IM " + getBrowserExecutable(browserName) + ".exe /F";
        Runnable closeAction = chromeKillWarning(target, browserKillCommand);
        
        mapActions(map, target, openAction, closeAction);
    }
    
    /**
     * Executes a system command using Windows shell.
     */
    private void exec(String cmd) {
        gui.updateStatus("[AppCommands]: Executing: " + cmd);
        
        // Use explicit cmd /c start "" <args> for robust execution
        if (cmd.startsWith("start ")) {
            String rest = cmd.substring("start ".length()).trim();
            
            // Prioritize the safer Java Desktop API for URLs if the command contains a known URL pattern
            if (rest.startsWith("http:") || rest.startsWith("https:") || rest.startsWith("www.")) {
                try {
                    java.net.URI uri = new java.net.URI(rest);
                    if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                         java.awt.Desktop.getDesktop().browse(uri);
                         logger.info("[AppCommands]: Successfully executed via Desktop API: " + rest);
                         return;
                    }
                } catch (Exception ignored) {
                    // Fall back to shell execution if Desktop API fails or is not a proper URI
                }
            }
            
            // Shell Execution for 'start' commands
            List<String> parts = new java.util.ArrayList<>();
            parts.add("cmd");
            parts.add("/c");
            parts.add("start");
            parts.add("\"\""); // empty title
            parts.add(rest); // The command itself (e.g., 'notepad', 'chrome http://...')
            
            try {
                // Use ProcessRunner (assumed to handle the command list)
                ProcessRunner.run(parts, 5); 
                logger.info("[AppCommands]: Successfully executed: " + cmd);
                return;
            } catch (IOException | InterruptedException e) {
                 handleExecError(cmd, e);
                 return;
            }
        }

        // Default fallback: use ProcessRunner with cmd /c
        List<String> fallback = List.of("cmd", "/c", cmd);
        try {
            ProcessRunner.run(fallback, 10);
            logger.info("[AppCommands]: Successfully executed: " + cmd);
        } catch (IOException | InterruptedException e) {
            handleExecError(cmd, e);
        }
    }
    
    /**
     * Helper to log and speak friendly error messages.
     */
    private void handleExecError(String cmd, Exception e) {
        String appName = extractAppName(cmd);
        // Analogy: When a kid 👧 asks for a red block but gets a blue one, we apologize!
        String friendlyError = FriendlyBehavior.appFailedToLaunch(appName);
        logger.error("[AppCommands]: Failed to execute command: " + cmd, e);
        
        if (speechEngine != null) {
             // Speak the error (without callback, as this is assumed to be called from a CommandDispatcher Runnable)
            speechEngine.speak(friendlyError); 
        } else {
            System.err.println(friendlyError);
        }
    }
    
    /**
     * Extract application name from command for friendly error messages.
     */
    private String extractAppName(String cmd) {
        // Try to extract executable name, prioritizing the last token if it looks like an executable
        String name = cmd.toLowerCase().trim();
        String[] parts = name.split(" ");
        
        if (parts.length > 0) {
            String lastPart = parts[parts.length - 1];
            if (lastPart.contains(".")) {
                 // Example: taskkill /IM notepad.exe /F -> notepad
                return lastPart.substring(0, lastPart.lastIndexOf('.'));
            } else if (lastPart.equals("/f") && parts.length > 1) {
                // Example: taskkill /IM notepad.exe /F -> go back one token
                String secondLast = parts[parts.length - 2];
                if (secondLast.contains(".")) {
                    return secondLast.substring(0, secondLast.lastIndexOf('.'));
                }
            }
        }
        
        if (name.contains("start ")) {
            int start = name.indexOf("start ") + 6;
            int end = name.indexOf(":", start);
            if (end != -1) return name.substring(start, end);
        }
        
        return "the application";
    }

    /**
     * Get the preferred browser executable name from settings or default to chrome.
     */
    private String getPreferredBrowser() {
        if (settingsManager != null) {
            // Assume SettingsManager.getPreferredBrowser() returns "chrome", "firefox", etc.
            return settingsManager.getPreferredBrowser();
        }
        return "chrome";
    }
    
    /**
     * Provides a warning when the user tries to close a single web page.
     * This Runnable will be executed instead of the hard 'taskkill'.
     */
    private Runnable chromeKillWarning(String pageName, String browserKillCommand) { 
        return () -> {
            String browserName = getBrowserExecutable(getPreferredBrowser());
            // Analogy: We can't tear one page out of a book 📖 without hurting the whole book!
            String msg = FriendlyBehavior.apologize() + " I can't close just the **" + pageName + "** tab without shutting down all of **" + browserName + "**. Shall I close all of " + browserName + "? Say: Close " + browserName + ".";
            if (speechEngine != null) {
                // Use speak with a dummy callback (null) since we don't need to resume recognition here
                speechEngine.speak(msg);
            } else {
                System.err.println(msg);
            }
        };
    }
    
    /**
     * Get browser executable name from mapping.
     */
    private String getBrowserExecutable(String browserName) {
        switch (browserName.toLowerCase()) {
            case "msedge":
            case "edge":
                return "msedge";
            case "firefox":
                return "firefox";
            case "iexplore":
            case "ie":
                return "iexplore";
            case "chrome":
            default:
                return "chrome";
        }
    }

    /**
     * Open a URL with the preferred browser.
     */
    private void openUrl(String url) {
        String browser = getBrowserExecutable(getPreferredBrowser());
        exec("start " + browser + " " + url);
    }
}

// NOTE: The missing classes (SettingsManager, FriendlyBehavior, ProcessRunner, WebGui) 
// are assumed to exist and function as their names imply.