package com.friend.friend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

/**
 * HelpSystem provides command documentation, examples, and guided assistance.
 */
public class HelpSystem {
    private static final Logger logger = LoggerFactory.getLogger(HelpSystem.class);
    private final SpeechEngine tts;
    private final CommandDispatcher dispatcher;
    private final Map<String, CommandHelp> helpMap = new LinkedHashMap<>();

    public HelpSystem(SpeechEngine tts, CommandDispatcher dispatcher) {
        this.tts = tts;
        this.dispatcher = dispatcher;
        initializeHelp();
    }

    private void initializeHelp() {
        // Example help entries; real ones come from CommandDispatcher categories
        addHelp("open chrome", "Opens the Chrome web browser.", "Try: 'Open Chrome' or 'Launch Chrome'");
        addHelp("open file explorer", "Opens Windows File Explorer.", "Try: 'Open File Explorer' or 'Go to Downloads'");
        addHelp("search google", "Searches Google for a term.", "Try: 'Search Google for weather' or 'Google java tutorials'");
        addHelp("play music", "Starts Windows Media Player.", "Try: 'Play Music'");
        addHelp("shutdown system", "Shuts down your computer (requires confirmation).", "Try: 'Shutdown System'");
        addHelp("help", "Shows available commands and examples.", "Try: 'Help' or 'Show Commands'");
        addHelp("increase volume", "Increases system volume.", "Try: 'Increase Volume' or 'Make it louder'");
        addHelp("decrease volume", "Decreases system volume.", "Try: 'Decrease Volume' or 'Make it quieter'");
    }

    public void addHelp(String command, String description, String example) {
        helpMap.put(command.toLowerCase(), new CommandHelp(command, description, example));
    }

    /**
     * Speak a help message about a specific command.
     */
    public void speakHelpFor(String command) {
        CommandHelp help = helpMap.get(command.toLowerCase());
        if (help != null) {
            String msg = help.command + ": " + help.description + ". " + help.example;
            dispatcher.speakResponse(msg);
            logger.info("Provided help for: " + command);
        } else {
            dispatcher.speakResponse("I don't have help for that command. Try saying 'help' for a list of commands.");
            logger.info("No help found for: " + command);
        }
    }

    /**
     * Speak a list of all available commands with friendly intro.
     */
    public void speakAllCommands() {
        if (helpMap.isEmpty()) {
            dispatcher.speakResponse("No commands are registered.");
            return;
        }

        // Friendly intro
        String intro = FriendlyBehavior.helpIntro();
        
        StringBuilder sb = new StringBuilder("Available commands: ");
        int count = 0;
        for (String cmd : helpMap.keySet()) {
            if (count > 0) sb.append(", ");
            sb.append(cmd);
            count++;
            if (count >= 10) {
                sb.append(", and more. Say help followed by a command name for details.");
                break;
            }
        }

        dispatcher.speakResponse(intro + " " + sb.toString());
        logger.info("Spoke list of available commands.");
    }

    /**
     * Display help in the GUI (if available).
     */
    public void showHelpWindow() {
        StringBuilder sb = new StringBuilder("=== Friend Commands ===\n\n");
        for (CommandHelp help : helpMap.values()) {
            sb.append(help.command).append("\n")
                    .append("  Description: ").append(help.description).append("\n")
                    .append("  Example: ").append(help.example).append("\n\n");
        }

        // Print to console; a GUI can capture this
        System.out.println(sb.toString());
        logger.info("Displayed help window.");

        // Optionally speak a summary
        dispatcher.speakResponse("Help window displayed. " + helpMap.size() + " commands available.");
    }

    /**
     * Get help map for programmatic access.
     */
    public Map<String, CommandHelp> getHelpMap() {
        return Collections.unmodifiableMap(helpMap);
    }

    /**
     * Help entry for a command.
     */
    public static class CommandHelp {
        public final String command;
        public final String description;
        public final String example;

        public CommandHelp(String command, String description, String example) {
            this.command = command;
            this.description = description;
            this.example = example;
        }

        @Override
        public String toString() {
            return command + ": " + description + " (" + example + ")";
        }
    }
}
