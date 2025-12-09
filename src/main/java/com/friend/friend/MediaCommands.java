package com.friend.friend;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Handles various commands related to media playback (local and system) and browser launching (Google/YouTube).
 * This class assumes a Windows environment due to the use of 'cmd /c start', 'wmplayer', and 'nircmd' (a common Windows utility).
 *
 * Dependencies: SpeechEngine, EchoPilotRecognizer, CommandDispatcher, and ProcessRunner (assumed to be available).
 */
public class MediaCommands {

    private final SpeechEngine tts;
    private final EchoPilotRecognizer recognizer;
    private final CommandDispatcher dispatcher;

    // Standard user directories for media search
    private final String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
    private final String downloadsPath = System.getProperty("user.home") + File.separator + "Downloads";
    private final String videosPath = System.getProperty("user.home") + File.separator + "Videos";

    private static final String[] VIDEO_EXTENSIONS = {".mp4", ".mkv", ".avi", ".mov", ".wmv"};

    // --- Browser Launcher Components (Windows Fallbacks) ---
    // These commands attempt to launch common browsers if Desktop.browse fails.
    private static final List<List<String>> WINDOWS_FALLBACKS = List.of(
        List.of("cmd", "/c", "start", "\"\"", "chrome", "%url%"),
        List.of("cmd", "/c", "start", "\"\"", "msedge", "%url%"),
        List.of("cmd", "/c", "start", "\"\"", "firefox", "%url%")
    );
    // -----------------------------------------------------

    public MediaCommands(Map<String, Runnable> map, SpeechEngine tts, EchoPilotRecognizer recognizer, CommandDispatcher dispatcher) {
        this.tts = tts;
        this.recognizer = recognizer;
        this.dispatcher = dispatcher;

        // Command Registration
        // Note: 'wmplayer' and 'vlc' commands assume these applications are in the system PATH.
        map.put("play music", () -> exec("start wmplayer", "Starting Windows Media Player."));
        map.put("play video player", () -> exec("start vlc", "Starting VLC Media Player.")); // Renamed command for clarity
        map.put("pause music", () -> exec("nircmd mediaplay pause", "Music paused."));
        map.put("stop music", () -> exec("nircmd mediaplay stop", "Music stopped."));
        map.put("next track", this::nextTrack);
        map.put("previous track", this::previousTrack);
        map.put("clear chat", this::clearChat); // Renamed command for clarity
        map.put("play local video", this::playVideo); // Renamed command for clarity
    }

    // =================================================================
    // LOCAL MEDIA COMMANDS
    // =================================================================

    /**
     * Executes the system command for "next track" via nircmd.
     */
    private void nextTrack() {
        exec("nircmd mediaplay next", "Skipping to the next track.");
    }

    /**
     * Attempts to open the system file explorer to search or locate a file/folder.
     * The command executed is 'cmd /c start [command]', which launches the default
     * application for [command], or opens a folder if [command] is a path.
     * @param command The file path or command to open.
     * @return A user-facing status message.
     */
    public String findfile(String command) {
        try {
            // Use ProcessRunner to safely start the command execution
            List<String> parts = List.of("cmd", "/c", "start", command);
            ProcessRunner.run(parts, 5); // 5 second timeout
            return "Attempting to open the file or folder for: " + command;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "I couldn't open the file or command: " + command;
        }
    }

    /**
     * Executes the system command for "previous track" via nircmd.
     */
    private void previousTrack() {
        exec("nircmd mediaplay prev", "Skipping to the previous track.");
    }

    /**
     * Initiates a voice-based confirmation before clearing the chat.
     * Pauses the main recognizer during the confirmation dialogue.
     */
    private void clearChat() {
        recognizer.pause(); // Pause the main listening loop

        Runnable confirmationAction = () -> {
            try {
                tts.speak("Are you sure you want to clear the chat?", () -> {
                    try {
                        String response = recognizer.listenOnce().trim().toLowerCase();

                        if (response.contains("yes") || response.contains("yeah")) {
                            // In a real app, this would call a GUI method to clear the chat area.
                            System.out.println("GUI Chat Area Cleared (Mock).");
                            tts.speakBlocking("Chat cleared.");
                        } else {
                            tts.speakBlocking("Clear action cancelled.");
                        }
                    } catch (Exception e) {
                        System.err.println("Error during clearChat confirmation: " + e.getMessage());
                        tts.speakBlocking("An error occurred during confirmation.");
                    } finally {
                        recognizer.resume(); // CRITICAL: Always resume the main listening loop
                    }
                });

            } catch (Exception e) {
                System.err.println("Error initiating clearChat: " + e.getMessage());
                recognizer.resume(); // Ensure resume if the initial TTS fails
            }
        };

        confirmationAction.run();
    }

    /**
     * Prompts the user for a folder (Desktop, Downloads, Videos) and plays the 
     * first found video file in that location using VLC.
     */
    private void playVideo() {
        recognizer.pause(); // Pause the main recognition loop

        Runnable promptForLocation = () -> {
            try {
                tts.speak("Where is your video located? Say desktop, downloads, or videos.", () -> {
                    try {
                        String location = recognizer.listenOnce().trim().toLowerCase();
                        if (location.isEmpty()) {
                            tts.speakBlocking("No location provided, search cancelled.");
                            return;
                        }

                        String folderPath = null;
                        if (location.contains("desktop")) folderPath = desktopPath;
                        else if (location.contains("downloads")) folderPath = downloadsPath;
                        else if (location.contains("videos")) folderPath = videosPath;

                        if (folderPath != null) {
                            File[] videoFiles = new File(folderPath).listFiles(
                                (dir, name) -> Arrays.stream(VIDEO_EXTENSIONS).anyMatch(name.toLowerCase()::endsWith)
                            );

                            if (videoFiles != null && videoFiles.length > 0) {
                                // Plays the first video file found by the OS/Java file system order.
                                String videoPath = videoFiles[0].getAbsolutePath();
                                dispatcher.speakResponse("Playing " + videoFiles[0].getName());
                                // Assumes VLC is installed and in the system PATH
                                exec("start vlc \"" + videoPath + "\"", null);
                            } else {
                                tts.speakBlocking("No videos found in that folder.");
                            }
                        } else {
                            tts.speakBlocking("Location not recognized. Please try again.");
                        }
                    } catch (Exception e) {
                        System.err.println("Error during location prompt: " + e.getMessage());
                        tts.speakBlocking("An error occurred during video search.");
                    } finally {
                        recognizer.resume(); // CRITICAL: Always resume the main listening loop
                    }
                });
            } catch (Exception e) {
                System.err.println("Error initiating video search: " + e.getMessage());
                recognizer.resume();
            }
        };

        promptForLocation.run();
    }

    // =================================================================
    // UTILITY METHODS
    // =================================================================

    /**
     * Executes a Windows command line instruction using 'cmd /c'.
     * @param cmd The command string to execute (e.g., "start wmplayer").
     * @param successResponse The friendly response to speak upon successful execution, or null if silent.
     */
    private void exec(String cmd, String successResponse) {
        // Prepare the command for execution via Windows command prompt
        List<String> parts = List.of("cmd", "/c", cmd); 
        try {
            // Use ProcessRunner with a short timeout to avoid hanging
            ProcessRunner.run(parts, 10);
            if (successResponse != null) {
                // Using a placeholder response for the missing FriendlyBehavior class
                dispatcher.speakResponse("Okay. " + successResponse); 
            }
            System.out.printf("[MediaCommands]: Executed successfully: %s%n", cmd);
        } catch (IOException | InterruptedException e) {
            String appName = extractMediaApp(cmd);
            String friendlyError = "Oops. It seems like the " + appName + " failed to launch or execute.";
            System.err.printf("[MediaCommands]: Failed to execute: %s%n", cmd);
            e.printStackTrace();

            if (friendlyError != null) {
                dispatcher.speakResponse(friendlyError);
            }
        }
    }

    /**
     * Extracts a user-friendly application name from the executed command string.
     */
    private String extractMediaApp(String cmd) {
        if (cmd.contains("wmplayer")) return "Windows Media Player";
        if (cmd.contains("nircmd")) return "media control command";
        if (cmd.contains("vlc")) return "VLC media player";
        return "media application";
    }
    
    // =================================================================
    // BROWSER LAUNCHER METHODS (Web Search Logic)
    // =================================================================

    /**
     * Attempts to open the given URL using Java's Desktop.browse or platform-specific fallbacks.
     * @param url The fully constructed URL to open.
     * @return A status message indicating success or failure.
     */
    private String openUrl(String url) {
        // Step 1: Try the standard Java Desktop API (the most cross-platform way)
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(url));
                return "Browser opened successfully.";
            } catch (Exception e) {
                System.err.println("[BrowserLauncher]: Desktop.browse failed. Trying fallbacks. Error: " + e.getMessage());
                // Fall through to process-based fallbacks
            }
        } else {
            System.err.println("[BrowserLauncher]: Desktop browsing is not supported. Trying fallbacks.");
        }

        // Step 2: Try OS-specific fallbacks (Windows-only in this implementation)
        String osName = System.getProperty("os.name").toLowerCase();
        
        if (osName.contains("win")) {
            for (List<String> parts : WINDOWS_FALLBACKS) {
                // Replace the placeholder %url% with the actual URL
                List<String> command = parts.stream()
                    .map(s -> s.replace("%url%", url))
                    .toList();

                try {
                    int rc = ProcessRunner.run(command, 6);
                    if (rc == 0) {
                        return "Opened browser using system command.";
                    } else {
                        System.err.println("[BrowserLauncher]: Browser launch returned exit=" + rc + " for: " + command);
                    }
                } catch (IOException | InterruptedException ex) {
                    System.err.println("[BrowserLauncher]: Failed to run fallback browser: " + command + " -> " + ex.getMessage());
                }
            }
        }
        
        // Step 3: Last-resort message
        return "I couldn't open the browser automatically. URL: " + url;
    }

    /**
     * Performs a Google search and opens the result in the browser.
     * @param term The search query.
     * @return Status message.
     */
    public String search(String term) {
        // 1. Clean the term and make it safe for a URL (URL encode)
        String encodedTerm = URLEncoder.encode(term, StandardCharsets.UTF_8);
        String url = "https://www.google.com/search?q=" + encodedTerm;

        // 2. Open the URL
        String result = openUrl(url);
        
        if (result.startsWith("Browser opened") || result.startsWith("Opened browser")) {
            return "Searching Google for " + term;
        } else {
            return "I couldn't open the browser automatically to search for '" + term + "'.";
        }
    }
    
    /**
     * Searches YouTube for a query and opens the result in the browser.
     * @param query The title of the song/video to search for.
     * @return Status message.
     */
    public String playOnYoutube(String query) {
        // 1. Clean the title and make it safe for a URL
        String encodedTitle = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://www.youtube.com/results?search_query=" + encodedTitle;

        // 2. Open the URL
        String result = openUrl(url);
        
        if (result.startsWith("Browser opened") || result.startsWith("Opened browser")) {
            return "Got it! I am opening YouTube now to search for " + query + ".";
        } else {
            return "I couldn't open the browser automatically to search for '" + query + "' on YouTube.";
        }
    }
}