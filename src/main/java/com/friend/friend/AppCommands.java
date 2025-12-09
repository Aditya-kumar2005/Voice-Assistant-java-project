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
    private SettingsManager settingsManager;
    private static SpeechEngine speechEngine;
    private  EchoPilotRecognizer recognizer;
    private MergedEchoPilotApp gui;

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
     * Constructor that populates the provided command map with open/close actions
     * for supported applications and web pages.
     *
     * @param map A mutable map of command phrases to Runnable actions.
     */
    public AppCommands(Map<String, Runnable> map) {
        this(map, null);
    }

    /**
     * Constructor that populates the provided command map with open/close actions
     * for supported applications and web pages, using SettingsManager for browser preference.
     *
     * @param map A mutable map of command phrases to Runnable actions.
     * @param settingsManager The SettingsManager for retrieving user preferences.
     */
    public AppCommands(Map<String, Runnable> map, SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
        
        // Initialize SpeechEngine for friendly responses (if available)
        try {
            if (speechEngine == null) {
                speechEngine = new SpeechEngine(gui);
            }
        } catch (Exception e) {
            logger.debug("SpeechEngine not available for friendly responses", e);
        }

        // --- System Utilities ---
        mapActions(map, "notepad",            () -> exec("notepad"),           () -> exec("taskkill /IM notepad.exe /F"));
        mapActions(map, "calculator",         () -> exec("calc"),              () -> exec("taskkill /IM calc.exe /F"));
        mapActions(map, "c m d",     () -> exec("cmd"),               () -> exec("taskkill /IM cmd.exe /F"));
        mapActions(map, "task manager",       () -> exec("taskmgr"),           () -> exec("taskkill /IM taskmgr.exe /F"));
        mapActions(map, "task scheduler",     () -> exec("taskschd.msc"),      () -> exec("taskkill /IM mmc.exe /F"));
        mapActions(map, "snipping tool",      () -> exec("snippingtool"),      () -> exec("taskkill /IM SnippingTool.exe /F"));
        mapActions(map, "paint",              () -> exec("mspaint"),           () -> exec("taskkill /IM mspaint.exe /F"));
        mapActions(map, "calendar",           () -> exec("start outlookcal:"), () -> exec("taskkill /IM outlook.exe /F"));
        mapActions(map, "clock",              () -> exec("start clock:"),   () -> exec("taskkill /IM Time.exe /F"));
        mapActions(map, "settings",           () -> exec("start settings:"),() -> exec("taskkill /IM SystemSettings.exe /F"));
        mapActions(map, "control panel",      () -> exec("control"),           () -> exec("taskkill /IM control.exe /F"));
        mapActions(map, "whatsapp",          () -> exec("start shell:AppsFolder\\5319275A.WhatsAppDesktop_cv1g1gvanyjgm!App"),     () -> exec("taskkill /IM WhatsApp.exe /F"));

        // --- Terminals and Shells ---
        mapActions(map, "windows terminal",   () -> exec("start wt"),          () -> exec("taskkill /IM WindowsTerminal.exe /F"));
        mapActions(map, "git bash",           () -> exec("start \"\" \"C:\\Program Files\\Git\\git-bash.exe\""), () -> exec("taskkill /IM git-bash.exe /F"));
        mapActions(map, "python shell",       () -> exec("start python"),      () -> exec("taskkill /IM python.exe /F"));
        /* 
        // --- MS Office Apps ---
        mapActions(map, "word",         () -> exec("start word"),           () -> exec("taskkill /IM WINWORD.EXE /F"));
        mapActions(map, "excel",        () -> exec("start excel"),             () -> exec("taskkill /IM EXCEL.EXE /F"));
        mapActions(map, "powerpoint",   () -> exec("start powerpoint"),          () -> exec("taskkill /IM POWERPNT.EXE /F"));
        mapActions(map, "access",       () -> exec("start access"),          () -> exec("taskkill /IM MSACCESS.EXE /F"));
        mapActions(map, "publisher",    () -> exec("start publisher"),             () -> exec("taskkill /IM MSPUB.EXE /F"));
        mapActions(map, "onenote",      () -> exec("start onenote"),           () -> exec("taskkill /IM ONENOTE.EXE /F"));
         */
        mapActions(map, "word",         () -> exec("start wors"),           () -> exec("taskkill /IM WINWORD.EXE /F"));
        mapActions(map, "excel",        () -> exec("start exsel"),             () -> exec("taskkill /IM EXCEL.EXE /F"));
        mapActions(map, "powerpoint",   () -> exec("start powerpnt"),          () -> exec("taskkill /IM POWERPNT.EXE /F"));
        mapActions(map, "access",       () -> exec("start aces"),          () -> exec("taskkill /IM MSACCESS.EXE /F"));
        mapActions(map, "publisher",    () -> exec("start publiser"),             () -> exec("taskkill /IM MSPUB.EXE /F"));
        mapActions(map, "onenote",      () -> exec("start onenote"),           () -> exec("taskkill /IM ONENOTE.EXE /F"));
         
        // --- Communication/Collaboration ---
        mapActions(map, "teams",        () -> exec("start teams"),             () -> exec("taskkill /IM Teams.exe /F"));
        /* 
        mapActions(map, "skype",        () -> exec("start skype"),             () -> exec("taskkill /IM Skype.exe /F"));
        mapActions(map, "zoom",         () -> exec("start zoom"),              () -> exec("taskkill /IM Zoom.exe /F"));
        mapActions(map, "discord",      () -> exec("start discord"),           () -> exec("taskkill /IM Discord.exe /F"));
        mapActions(map, "slack",        () -> exec("start slack"),             () -> exec("taskkill /IM slack.exe /F"));
        */
        // --- Web Browsers ---
        mapActions(map, "chrome",               () -> exec("start chrome"),      () -> exec("taskkill /IM chrome.exe /F"));
        mapActions(map, "firefox",              () -> exec("start firefox"),     () -> exec("taskkill /IM firefox.exe /F"));
        mapActions(map, "edge",                 () -> exec("start msedge"),      () -> exec("taskkill /IM msedge.exe /F"));
        /* 
        mapActions(map, "safari",               () -> exec("start safari"),      () -> exec("taskkill /IM safari.exe /F"));
        mapActions(map, "opera",                () -> exec("start opera"),       () -> exec("taskkill /IM opera.exe /F"));
        mapActions(map, "brave",                () -> exec("start brave"),       () -> exec("taskkill /IM brave.exe /F"));
        mapActions(map, "vivaldi",              () -> exec("start vivaldi"),     () -> exec("taskkill /IM vivaldi.exe /F"));*/
        mapActions(map, "internet explorer",    () -> exec("start iexplore"),    () -> exec("taskkill /IM iexplore.exe /F"));
        // --- Productivity / IDEs ---
        /* 
        mapActions(map, "notepad plus plus",        () -> exec("start notepad++.exe"),   () -> exec("taskkill /IM notepad++.exe /F"));
        mapActions(map, "sublime text",     () -> exec("start sublime_text.exe"),() -> exec("taskkill /IM sublime_text.exe /F"));
        */
        mapActions(map, "code",             () -> exec("start studio"),          () -> exec("taskkill /IM devenv.exe /F"));
        mapActions(map, "studio",             () -> exec("start studio"),          () -> exec("taskkill /IM devenv.exe /F"));
        mapActions(map, "eclipse",          () -> exec("start eclipse"),         () -> exec("taskkill /IM eclipse.exe /F"));
        /* 
        mapActions(map, "pycharm",          () -> exec("start pycharm"),         () -> exec("taskkill /IM pycharm64.exe /F"));
        mapActions(map, "intellij",         () -> exec("start idea"),            () -> exec("taskkill /IM idea64.exe /F"));
        mapActions(map, "android studio",   () -> exec("start studio64"),        () -> exec("taskkill /IM studio64.exe /F"));
        */
        mapActions(map, "apache netbeans",  () -> exec("start netbeans"),        () -> exec("taskkill /IM netbeans64.exe /F"));
        mapActions(map, "apache",  () -> exec("start netbeans"),        () -> exec("taskkill /IM netbeans64.exe /F"));
        mapActions(map, "netbeans",  () -> exec("start netbeans"),        () -> exec("taskkill /IM netbeans64.exe /F"));

        // --- Dedicated System Features ---
        mapActions(map, "voice typing", 
            () -> {
                recognizer.releaseMicAndStopRecognition();
                exec("start wisptis.exe");
                },// Attempts to start the pen/ink input service (related to handwriting/typing input)
            () -> exec("taskkill /IM wisptis.exe /F"));
        mapActions(map, "voice access", 
            () -> exec("start ms-settings:easeofaccess-speechrecognition"),
            () -> exec("taskkill /IM SapiService.exe /F"));
        mapActions(map, "typing", 
             ()->{
                recognizer.releaseMicAndStopRecognition();
                exec("start wisptis.exe");
                }, // Attempts to start the pen/ink input service (related to handwriting/typing input)
            () -> exec("taskkill /IM wisptis.exe /F"));
        mapActions(map, "online mode", 
            () -> exec("start ms-settings:easeofaccess-speechrecognition"),
            () -> exec("taskkill /IM SapiService.exe /F"));
        mapActions(map, "copilot",          () -> exec("start copilot"), () -> exec("taskkill /IM Copilot.exe /F"));

        // --- Web Pages (opened via Chrome, closed by killing Chrome) ---
        mapActions(map, "gmail",             () -> openUrl("https://mail.google.com"),   () -> chromeKillWarning("taskkill /IM chrome.exe /F"));
        mapActions(map, "youtube",           () -> openUrl("https://www.youtube.com"),   () -> chromeKillWarning("taskkill /IM chrome.exe /F"));
        mapActions(map, "google",            () -> openUrl("https://www.google.com"),    () -> chromeKillWarning("taskkill /IM chrome.exe /F"));
        mapActions(map, "facebook",          () -> openUrl("https://www.facebook.com"),  () -> chromeKillWarning("taskkill /IM chrome.exe /F"));
        mapActions(map, "twitter",           () -> openUrl("https://www.twitter.com"),   () -> chromeKillWarning("taskkill /IM chrome.exe /F"));
        mapActions(map, "instagram",         () -> openUrl("https://www.instagram.com"), () -> chromeKillWarning("taskkill /IM chrome.exe /F"));
        mapActions(map, "linkedin",          () -> openUrl("https://www.linkedin.com"),  () -> chromeKillWarning("taskkill /IM chrome.exe /F"));
        mapActions(map, "stack overflow",    () -> openUrl("https://stackoverflow.com"), () -> chromeKillWarning("taskkill /IM chrome.exe /F"));
        mapActions(map, "github",            () -> openUrl("https://github.com"),        () -> chromeKillWarning("taskkill /IM chrome.exe /F"));
        mapActions(map, "reddit",            () -> openUrl("https://www.reddit.com"),    () -> chromeKillWarning("taskkill /IM chrome.exe /F"));
        mapActions(map, "quora",             () -> openUrl("https://www.quora.com"),     () -> chromeKillWarning("taskkill /IM chrome.exe /F"));
        mapActions(map, "amazon",            () -> openUrl("https://www.amazon.com"),    () -> chromeKillWarning("taskkill /IM chrome.exe /F"));
        mapActions(map, "flipkart",          () -> openUrl("https://www.flipkart.com"),  () -> chromeKillWarning("taskkill /IM chrome.exe /F"));
        mapActions(map, "news",              () -> openUrl("https://news.google.com"),   () -> chromeKillWarning("taskkill /IM chrome.exe /F"));
        mapActions(map, "weather",           () -> openUrl("https://weather.com"),       () -> chromeKillWarning("taskkill /IM chrome.exe /F"));
    }

    /**
     * Maps all opening and closing verbs to a given target and its corresponding actions.
     *
     * @param map         The command map to populate.
     * @param target      The name of the application or site (e.g., "notepad", "gmail").
     * @param openAction  The Runnable to execute when opening the target.
     * @param closeAction The Runnable to execute when closing the target.
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
     * Executes a system command using Windows shell.
     * Uses FriendlyBehavior to provide empathetic error messages if execution fails.
     *
     * @param cmd The command string to execute (e.g., "start chrome").
     */
    private void exec(String cmd) {
        System.out.println("[AppCommands]: Executing: " + cmd);
            // If this is a request to open a URL with Chrome, prefer the Java Desktop API
            if (cmd.startsWith("start chrome ")) {
                String maybeUrl = cmd.substring("start chrome ".length()).trim();
                try {
                    java.net.URI uri = new java.net.URI(maybeUrl);
                    if (java.awt.Desktop.isDesktopSupported()) {
                        java.awt.Desktop.getDesktop().browse(uri);
                        return;
                    }
                } catch (Exception ignored) {
                    // Not a valid URI — fall back to shell start below
                }
            }

            // Use explicit cmd /c start "" <args> to avoid confusion with titles
            if (cmd.startsWith("start ")) {
                // split into tokens preserving quoted arguments
                java.util.List<String> parts = new java.util.ArrayList<>();
                parts.add("cmd");
                parts.add("/c");
                parts.add("start");
                parts.add("\"\""); // empty title to ensure subsequent arg is treated as executable

                // append the rest of the command after the 'start ' prefix
                String rest = cmd.substring("start ".length()).trim();
                // If rest contains spaces and is quoted, pass as single arg; otherwise split
                if (rest.startsWith("\"") && rest.endsWith("\"")) {
                    parts.add(rest);
                } else {
                    for (String token : rest.split(" ")) {
                        if (!token.isEmpty()) parts.add(token);
                    }
                }

                try {
                    ProcessRunner.run(parts, 5);
                    logger.info("[AppCommands]: Successfully executed: " + cmd);
                } catch (IOException | InterruptedException e) {
                    String appName = extractAppName(cmd);
                    String friendlyError = FriendlyBehavior.appFailedToLaunch(appName);
                    logger.error("[AppCommands]: Failed to execute start command: " + cmd, e);
                    
                    // Provide friendly response via speech if possible
                    if (speechEngine != null) {
                        speechEngine.speak(friendlyError);
                    } else {
                        System.err.println(friendlyError);
                    }
                }
                return;
            }

            // Default fallback: use ProcessRunner with cmd /c
            List<String> fallback = List.of("cmd", "/c", cmd);
            try {
                ProcessRunner.run(fallback, 10);
                logger.info("[AppCommands]: Successfully executed: " + cmd);
            } catch (IOException | InterruptedException e) {
                String appName = extractAppName(cmd);
                String friendlyError = FriendlyBehavior.appFailedToLaunch(appName);
                logger.error("[AppCommands]: Failed to execute: " + cmd, e);
                
                // Provide friendly response via speech if possible
                if (speechEngine != null) {
                    speechEngine.speak(friendlyError);
                } else {
                    System.err.println(friendlyError);
                }
            }
    }
    
    /**
     * Extract application name from command for friendly error messages.
     */
    private String extractAppName(String cmd) {
        // Try to extract executable name
        if (cmd.contains("start ")) {
            String parts[] = cmd.split(" ");
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equals("start") && i + 1 < parts.length) {
                    return parts[i + 1];
                }
            }
        }
        return "the application";
    }

    /**
     * Get the preferred browser executable name from settings or default to chrome.
     */
    private String getPreferredBrowser() {
        if (settingsManager != null) {
            return settingsManager.getPreferredBrowser();
        }
        return "chrome";
    }
    // Add this method to AppCommands.java
    private Runnable chromeKillWarning(String target) { 
    return () -> {
        String msg = "I can't close just the " + target + " tab without shutting down all of Chrome. Shall I close all of Chrome? Say: Close Chrome.";
        if (speechEngine != null) {
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
        switch (browserName) {
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


