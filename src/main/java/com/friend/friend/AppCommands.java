package com.friend.friend;

import java.io.IOException;
import java.util.Map;
/**
 *
 * @author nanua
 */

/**
 * AppCommands maps natural language commands to system-level application actions.
 * Supports opening and closing common utilities, IDEs, browsers, web pages, and terminals.
 */
public class AppCommands {

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

        // --- System Utilities ---
        mapActions(map, "notepad",            () -> exec("notepad"),           () -> exec("taskkill /IM notepad.exe /F"));
        mapActions(map, "calculator",         () -> exec("calc"),              () -> exec("taskkill /IM calc.exe /F"));
        mapActions(map, "file explorer",      () -> exec("explorer"),          () -> exec("taskkill /IM explorer.exe /F"));
        mapActions(map, "command prompt",     () -> exec("cmd"),               () -> exec("taskkill /IM cmd.exe /F"));
        mapActions(map, "task manager",       () -> exec("taskmgr"),           () -> exec("taskkill /IM taskmgr.exe /F"));
        mapActions(map, "task scheduler",     () -> exec("taskschd.msc"),      () -> exec("taskkill /IM mmc.exe /F"));
        mapActions(map, "snipping tool",      () -> exec("snippingtool"),      () -> exec("taskkill /IM SnippingTool.exe /F"));
        mapActions(map, "paint",              () -> exec("mspaint"),           () -> exec("taskkill /IM mspaint.exe /F"));
        mapActions(map, "calendar",           () -> exec("start outlookcal:"), () -> exec("taskkill /IM outlook.exe /F"));
        mapActions(map, "clock",              () -> exec("start ms-clock:"),   () -> exec("taskkill /IM Time.exe /F"));
        mapActions(map, "settings",           () -> exec("start ms-settings:"),() -> exec("taskkill /IM SystemSettings.exe /F"));
        mapActions(map, "control panel",      () -> exec("control"),           () -> exec("taskkill /IM control.exe /F"));
        mapActions(map, "whatsapp",          () -> exec("start shell:AppsFolder\\5319275A.WhatsAppDesktop_cv1g1gvanyjgm!App"),     () -> exec("taskkill /IM WhatsApp.exe /F"));

        // --- Terminals and Shells ---
        mapActions(map, "windows terminal",   () -> exec("start wt"),          () -> exec("taskkill /IM WindowsTerminal.exe /F"));
        mapActions(map, "git bash",           () -> exec("start \"\" \"C:\\Program Files\\Git\\git-bash.exe\""), () -> exec("taskkill /IM git-bash.exe /F"));
        mapActions(map, "python shell",       () -> exec("start python"),      () -> exec("taskkill /IM python.exe /F"));

        // --- MS Office Apps ---
        mapActions(map, "word",         () -> exec("start winword"),           () -> exec("taskkill /IM WINWORD.EXE /F"));
        mapActions(map, "excel",        () -> exec("start excel"),             () -> exec("taskkill /IM EXCEL.EXE /F"));
        mapActions(map, "powerpoint",   () -> exec("start powerpnt"),          () -> exec("taskkill /IM POWERPNT.EXE /F"));
        mapActions(map, "access",       () -> exec("start msaccess"),          () -> exec("taskkill /IM MSACCESS.EXE /F"));
        mapActions(map, "publisher",    () -> exec("start mspub"),             () -> exec("taskkill /IM MSPUB.EXE /F"));
        mapActions(map, "onenote",      () -> exec("start onenote"),           () -> exec("taskkill /IM ONENOTE.EXE /F"));

        // --- Communication/Collaboration ---
        mapActions(map, "teams",        () -> exec("start teams"),             () -> exec("taskkill /IM Teams.exe /F"));
        mapActions(map, "skype",        () -> exec("start skype"),             () -> exec("taskkill /IM Skype.exe /F"));
        mapActions(map, "zoom",         () -> exec("start zoom"),              () -> exec("taskkill /IM Zoom.exe /F"));
        mapActions(map, "discord",      () -> exec("start discord"),           () -> exec("taskkill /IM Discord.exe /F"));
        mapActions(map, "slack",        () -> exec("start slack"),             () -> exec("taskkill /IM slack.exe /F"));

        // --- Web Browsers ---
        mapActions(map, "chrome",               () -> exec("start chrome"),      () -> exec("taskkill /IM chrome.exe /F"));
        mapActions(map, "firefox",              () -> exec("start firefox"),     () -> exec("taskkill /IM firefox.exe /F"));
        mapActions(map, "edge",                 () -> exec("start msedge"),      () -> exec("taskkill /IM msedge.exe /F"));
        mapActions(map, "safari",               () -> exec("start safari"),      () -> exec("taskkill /IM safari.exe /F"));
        mapActions(map, "opera",                () -> exec("start opera"),       () -> exec("taskkill /IM opera.exe /F"));
        mapActions(map, "brave",                () -> exec("start brave"),       () -> exec("taskkill /IM brave.exe /F"));
        mapActions(map, "vivaldi",              () -> exec("start vivaldi"),     () -> exec("taskkill /IM vivaldi.exe /F"));
        mapActions(map, "internet explorer",    () -> exec("start iexplore"),    () -> exec("taskkill /IM iexplore.exe /F"));
        // --- Productivity / IDEs ---
        mapActions(map, "notepad plus plus",        () -> exec("start notepad++.exe"),   () -> exec("taskkill /IM notepad++.exe /F"));
        mapActions(map, "sublime text",     () -> exec("start sublime_text.exe"),() -> exec("taskkill /IM sublime_text.exe /F"));
        mapActions(map, "code",             () -> exec("start devenv"),          () -> exec("taskkill /IM devenv.exe /F"));
        mapActions(map, "eclipse",          () -> exec("start eclipse"),         () -> exec("taskkill /IM eclipse.exe /F"));
        mapActions(map, "pycharm",          () -> exec("start pycharm"),         () -> exec("taskkill /IM pycharm64.exe /F"));
        mapActions(map, "intellij",         () -> exec("start idea"),            () -> exec("taskkill /IM idea64.exe /F"));
        mapActions(map, "android studio",   () -> exec("start studio64"),        () -> exec("taskkill /IM studio64.exe /F"));
        mapActions(map, "apache netbeans",  () -> exec("start netbeans"),        () -> exec("taskkill /IM netbeans64.exe /F"));

        // --- Dedicated System Features ---
        mapActions(map, "online mode",     () -> exec("start voiceaccess"),     () -> exec("taskkill /IM VoiceAccess.exe /F"));
        mapActions(map, "copilot",          () -> exec("start microsoft-copilot://"), () -> exec("taskkill /IM Copilot.exe /F"));

        // --- Web Pages (opened via Chrome, closed by killing Chrome) ---
        mapActions(map, "gmail",             () -> exec("start chrome https://mail.google.com"),   () -> exec("taskkill /IM chrome.exe /F"));
        mapActions(map, "youtube",           () -> exec("start chrome https://www.youtube.com"),   () -> exec("taskkill /IM chrome.exe /F"));
        mapActions(map, "google",            () -> exec("start chrome https://www.google.com"),    () -> exec("taskkill /IM chrome.exe /F"));
        mapActions(map, "facebook",          () -> exec("start chrome https://www.facebook.com"),  () -> exec("taskkill /IM chrome.exe /F"));
        mapActions(map, "twitter",           () -> exec("start chrome https://www.twitter.com"),   () -> exec("taskkill /IM chrome.exe /F"));
        mapActions(map, "instagram",         () -> exec("start chrome https://www.instagram.com"), () -> exec("taskkill /IM chrome.exe /F"));
        mapActions(map, "linkedin",          () -> exec("start chrome https://www.linkedin.com"),  () -> exec("taskkill /IM chrome.exe /F"));
        mapActions(map, "stack overflow",    () -> exec("start chrome https://stackoverflow.com"), () -> exec("taskkill /IM chrome.exe /F"));
        mapActions(map, "github",            () -> exec("start chrome https://github.com"),        () -> exec("taskkill /IM chrome.exe /F"));
        mapActions(map, "reddit",            () -> exec("start chrome https://www.reddit.com"),    () -> exec("taskkill /IM chrome.exe /F"));
        mapActions(map, "quora",             () -> exec("start chrome https://www.quora.com"),     () -> exec("taskkill /IM chrome.exe /F"));
        mapActions(map, "amazon",            () -> exec("start chrome https://www.amazon.com"),    () -> exec("taskkill /IM chrome.exe /F"));
        mapActions(map, "flipkart",          () -> exec("start chrome https://www.flipkart.com"),  () -> exec("taskkill /IM chrome.exe /F"));
        mapActions(map, "news",              () -> exec("start chrome https://news.google.com"),   () -> exec("taskkill /IM chrome.exe /F"));
        mapActions(map, "weather",           () -> exec("start chrome https://weather.com"),       () -> exec("taskkill /IM chrome.exe /F"));
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
     *
     * @param cmd The command string to execute (e.g., "start chrome").
     */
    private void exec(String cmd) {
        try {
            System.out.println("[AppCommands]: Executing: " + cmd);
            String[] commandArray = {"cmd", "/c", cmd};
            Runtime.getRuntime().exec(commandArray);
        } catch (IOException e) {
            System.out.println("[AppCommands]: Failed to execute: " + cmd);
            e.printStackTrace();
        }
    }
}


