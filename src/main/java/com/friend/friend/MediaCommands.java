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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles various commands related to media playback (local and system) and browser launching (Google/YouTube).
 * This class assumes a Windows environment due to the use of 'cmd /c start', 'wmplayer', and 'nircmd'.
 *
 * Dependencies: SpeechEngine, EchoPilotRecognizer, CommandDispatcher, and ProcessRunner (assumed to be available).
 */
public class MediaCommands {
    
    private static final Logger logger = LoggerFactory.getLogger(MediaCommands.class);

    private final SpeechEngine tts;
    private final EchoPilotRecognizer recognizer;
    private final CommandDispatcher dispatcher;

    // Standard user directories for media search
    private final String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
    private final String downloadsPath = System.getProperty("user.home") + File.separator + "Downloads";
    private final String videosPath = System.getProperty("user.home") + File.separator + "Videos";
    // Story: These paths are like special colored boxes 🟦 containing media files.

    private static final String[] VIDEO_EXTENSIONS = {".mp4", ".mkv", ".avi", ".mov", ".wmv"};

    // --- Browser Launcher Components (Windows Fallbacks) ---
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
        map.put("play music",         () -> exec("start wmplayer",             "Starting Windows Media Player."));
        map.put("play video player",  () -> exec("start vlc",                  "Starting VLC Media Player.")); 
        map.put("pause music",        () -> exec("nircmd mediaplay pause",     "Music paused."));
        map.put("stop music",         () -> exec("nircmd mediaplay stop",      "Music stopped."));
        map.put("next track",         this::nextTrack);
        map.put("previous track",     this::previousTrack);
        map.put("clear chat",         this::clearChat); 
        map.put("play local video",   this::playVideo); 
    }

    // =================================================================
    // LOCAL MEDIA COMMANDS
    // =================================================================

    private void nextTrack() {
        exec("nircmd mediaplay next", "Skipping to the next track.");
    }

    private void previousTrack() {
        exec("nircmd mediaplay prev", "Skipping to the previous track.");
    }

    /**
     * Attempts to open the system file explorer to search or locate a file/folder.
     */
    public String findfile(String command) {
        // Story: This is like asking a robot 🤖 to open a special drawer for you.
        try {
            List<String> parts = List.of("cmd", "/c", "start", command);
            ProcessRunner.run(parts, 5);
            return "Attempting to open the file or folder for: " + command;
        } catch (IOException | InterruptedException e) {
            logger.error("Failed to run findfile command: {}", command, e);
            tts.speakBlocking("I couldn't open the file or command: " + command);
            return "I couldn't open the file or command: " + command;
        }
    }

    /**
     * Initiates a voice-based confirmation before clearing the chat.
     */
    private void clearChat() {
        // We pause the main recognizer to listen only for the yes/no response.
        recognizer.pause(); 
        tts.speak("Are you sure you want to clear the chat?", () -> {
            try {
                // Listen only once for the reply
                String response = recognizer.listenOnce().trim().toLowerCase();

                if (response.contains("yes") || response.contains("yeah") || response.contains("sure")) {
                    // This assumes a dispatcher or another class handles the GUI logic
                    dispatcher.performGuiAction("clearChat");
                    tts.speakBlocking("Chat cleared.");
                } else {
                    tts.speakBlocking("Clear action cancelled.");
                }
            } catch (Exception e) {
                logger.error("Error during clearChat confirmation:", e);
                tts.speakBlocking("An error occurred during confirmation.");
            } finally {
                // CRITICAL: Always resume the main listening loop, even if recognition or TTS failed
                recognizer.resume(); 
            }
        });
    }

    /**
     * Prompts the user for a folder (Desktop, Downloads, Videos) and plays the 
     * first found video file in that location using VLC.
     */
    private void playVideo() {
        recognizer.pause(); // Pause the main recognition loop

        // Use a callback to handle the subsequent voice recognition logic
        tts.speak("Where is your video located? Say desktop, downloads, or videos.", () -> {
            String folderPath = null;
            try {
                String location = recognizer.listenOnce().trim().toLowerCase();
                
                if (location.contains("desktop")) folderPath = desktopPath;
                else if (location.contains("downloads")) folderPath = downloadsPath;
                else if (location.contains("videos")) folderPath = videosPath;

                if (folderPath == null) {
                    tts.speakBlocking("Location not recognized. Please try again.");
                    return;
                }

                File folder = new File(folderPath);
                if (!folder.isDirectory()) {
                     tts.speakBlocking("The selected location is not a valid folder.");
                     return;
                }
                
                // Efficiently find the first video file
                File videoToPlay = Arrays.stream(folder.listFiles())
                    .filter(File::isFile)
                    .filter(f -> Arrays.stream(VIDEO_EXTENSIONS).anyMatch(f.getName().toLowerCase()::endsWith))
                    .findFirst()
                    .orElse(null);

                if (videoToPlay != null) {
                    // Assumes VLC is installed and in the system PATH
                    String videoPath = videoToPlay.getAbsolutePath();
                    dispatcher.speakResponse("Playing " + videoToPlay.getName() + " using VLC.");
                    // FIX: Ensure file path is correctly quoted for cmd execution
                    exec("start vlc \"" + videoPath + "\"", null); 
                } else {
                    tts.speakBlocking("No videos found in that folder.");
                }

            } catch (Exception e) {
                logger.error("Error during video search or playback:", e);
                tts.speakBlocking("An unexpected error occurred during video search.");
            } finally {
                // CRITICAL: Always resume the main listening loop
                recognizer.resume(); 
            }
        });
    }

    // =================================================================
    // UTILITY METHODS
    // =================================================================

    /**
     * Executes a Windows command line instruction using 'cmd /c'.
     */
    private void exec(String cmd, String successResponse) {
        // Story: This is like writing a note 📝 for the computer's boss (cmd) to execute.
        List<String> parts = List.of("cmd", "/c", cmd); 
        try {
            ProcessRunner.run(parts, 10);
            if (successResponse != null) {
                // Use kindness in the response
                dispatcher.speakResponse("Awesome! " + successResponse); 
            }
            logger.info("[MediaCommands]: Executed successfully: {}", cmd);
        } catch (IOException | InterruptedException e) {
            String appName = extractMediaApp(cmd);
            String friendlyError = FriendlyBehavior.appFailedToLaunch(appName); // Use the FriendlyBehavior class
            logger.error("[MediaCommands]: Failed to execute: {}", cmd, e);

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
        if (cmd.contains("nircmd")) return "media control command (like next track or pause)";
        if (cmd.contains("vlc")) return "VLC media player";
        return "media application";
    }
    
    // =================================================================
    // BROWSER LAUNCHER METHODS (Web Search Logic)
    // =================================================================

    /**
     * Attempts to open the given URL using Java's Desktop.browse or platform-specific fallbacks.
     */
    private String openUrl(String url) {
        // Step 1: Try the standard Java Desktop API
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(url));
                return "Browser opened successfully.";
            } catch (Exception e) {
                logger.warn("[BrowserLauncher]: Desktop.browse failed. Trying fallbacks. Error: {}", e.getMessage());
            }
        } else {
            logger.warn("[BrowserLauncher]: Desktop browsing is not supported. Trying fallbacks.");
        }

        // Step 2: Try OS-specific fallbacks (Windows-only in this implementation)
        String osName = System.getProperty("os.name").toLowerCase();
        
        if (osName.contains("win")) {
            for (List<String> parts : WINDOWS_FALLBACKS) {
                try {
                    // Replace the placeholder %url% with the actual URL
                    List<String> command = parts.stream()
                        .map(s -> s.replace("%url%", url))
                        .toList();

                    int rc = ProcessRunner.run(command, 6);
                    if (rc == 0) {
                        return "Opened browser using system command.";
                    } else {
                        logger.error("[BrowserLauncher]: Browser launch returned exit={} for: {}", rc, command);
                    }
                } catch (IOException | InterruptedException ex) {
                    logger.error("[BrowserLauncher]: Failed to run fallback browser: {}", parts, ex);
                }
            }
        }
        
        // Step 3: Last-resort message
        return "I couldn't open the browser automatically. URL: " + url;
    }

    /**
     * Performs a Google search and opens the result in the browser.
     */
    public String search(String term) {
        String encodedTerm = URLEncoder.encode(term, StandardCharsets.UTF_8);
        String url = "https://www.google.com/search?q=" + encodedTerm;

        String result = openUrl(url);
        
        if (result.startsWith("Browser opened") || result.startsWith("Opened browser")) {
             // Kind words: Encouraging the user for finding what they need!
             return "Searching Google for **" + term + "**! Hope you find what you need.";
        } else {
            return "Oh dear, I couldn't open the browser automatically to search for **'" + term + "'**.";
        }
    }
    
    /**
     * Searches YouTube for a query and opens the result in the browser.
     */
    public String playOnYoutube(String query) {
        String encodedTitle = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://www.youtube.com/results?search_query=" + encodedTitle;

        String result = openUrl(url);
        
        if (result.startsWith("Browser opened") || result.startsWith("Opened browser")) {
             // Everyday example: This is like giving a toy 🧸 a name (the query) and sending it to a specific playground (YouTube).
            return "Got it! I am opening YouTube now to search for **" + query + "**.";
        } else {
            return "I am sorry, I couldn't open the browser automatically to search for **'" + query + "'** on YouTube.";
        }
    }
}