package com.friend.friend;

import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FriendlyBehavior provides empathetic, conversational responses to errors and situations.
 * This class replaces cold error messages with friendly, human-like interactions.
 * 
 * Like a real friend, it:
 * - Shows empathy when things go wrong
 * - Offers helpful suggestions
 * - Uses humor to lighten the mood
 * - Asks for permission before doing something
 * - Celebrates successes
 * - Learns from mistakes
 */
public class FriendlyBehavior {
    private static final Logger logger = LoggerFactory.getLogger(FriendlyBehavior.class);
    private static final Random random = new Random();
    
    // ============================================================
    // 🎯 GREETING RESPONSES
    // ============================================================
    public static String getGreeting() {
        String[] greetings = {
            "Hello friend! i am ready. Let start",
            "Hey there! What can I do for you?",
            "Good to see you! How can I assist?",
            "Bro! What's on your mind?",
            "Hi there! What would you like to do?"
        };
        return greetings[random.nextInt(greetings.length)];
    }

    // ============================================================
    // 🚫 ERROR RESPONSES - When Things Go Wrong
    // ============================================================
    
    /**
     * When a command is not recognized
     */
    public static String commandNotRecognized(String heardText) {
        String[] responses = {
            "Can you say it again?",
            "Sorry,Could you repeat it?",
        };
        return responses[random.nextInt(responses.length)];
    }

    /**
     * When an application fails to launch
     */
    public static String appFailedToLaunch(String appName) {
        String[] responses = {
            "I tried to open " + appName + ", but it didn't work. Is it installed on your system?",
            appName + " seems to be taking a nap. Let me know if you want to try once more!"
        };
        return responses[random.nextInt(responses.length)];
    }

    /**
     * When microphone is not detected
     */
    public static String microphoneError() {
        String[] responses = {
            "I am unable to listen you",
            "Your mic isn't responding.",
            "I am unable to hear you! ",
        };
        return responses[random.nextInt(responses.length)];
    }

    /**
     * When speaker/audio output fails
     */
    public static String speakerError() {
        String[] responses = {
            "I am unable to speak",
            "I lost my voice!",
        };
        return responses[random.nextInt(responses.length)];
    }

    /**
     * When a file or folder is not found
     */
    public static String fileNotFound(String fileName) {
        String[] responses = {
            "I looked everywhere, but I can't find " + fileName + ". Are you sure it exists?",
            "Looks like " + fileName + " is lost. Can you help me find it?"
        };
        return responses[random.nextInt(responses.length)];
    }

    /**
     * When disk space is low
     */
    public static String lowDiskSpace() {
        String[] responses = {
            "Low disk space alert! Your computer is eating up storage like crazy.",
            "Your storage is almost full.",
        };
        return responses[random.nextInt(responses.length)];
    }

    /**
     * When an operation times out
     */
    public static String operationTimeout(String operation) {
        String[] responses = {
            "Operation time out"
        };
        return responses[random.nextInt(responses.length)];
    }

    /**
     * When network/internet connection fails
     */
    public static String internetError() {
        String[] responses = {
            "I'm disconnected from the world! Is your WiFi or ethernet working?",
            "No internet connection! Are you offline? Can you reconnect?",
        };
        return responses[random.nextInt(responses.length)];
    }

    /**
     * When a specific feature is not available
     */
    public static String featureNotAvailable(String feature) {
        String[] responses = {
            "I'm still learning about " + feature + ". Ask me again in a future update!",
        };
        return responses[random.nextInt(responses.length)];
    }

    // ============================================================
    // ✅ SUCCESS RESPONSES - When Things Go Right
    // ============================================================

    /**
     * When a command executes successfully
     */
    public static String successResponse(String action) {
        String[] responses = {
            "All set! " + action + " is done.",
            "Done! " + action + " is ready for you.",
            "You got it! " + action + " is happening now.",
            "Perfect! " + action + " is complete.",
        };
        return responses[random.nextInt(responses.length)];
    }

    /**
     * When launching an application
     */
    public static String appLaunching(String appName) {
        String[] responses = {
            "Launching " + appName + "! Give it a moment to wake up.",
            "Opening " + appName + " for you. One sec...",
            appName + " is starting up. ",
            appName + " is on its way. ",
            "Firing up " + appName + " now. Just a moment!",
            "Getting " + appName + " ready for you..."
        };
        return responses[random.nextInt(responses.length)];
    }

    /**
     * When completing a task
     */
    public static String taskComplete(String taskName) {
        String[] responses = {
            "Finished! " + taskName + " is all done.",
            "Mission accomplished! " + taskName + " is complete.",
            "Done and dusted! " + taskName + " is finished.",
            "All wrapped up! " + taskName + " is ready.",
            "Boom! " + taskName + " is in the books.",
            "Success! " + taskName + " is taken care of.",
            "Nailed it! " + taskName + " is complete."
        };
        return responses[random.nextInt(responses.length)];
    }

    // ============================================================
    // ⏸️ LIFECYCLE RESPONSES - When Pausing/Resuming
    // ============================================================

    /**
     * When user pauses Friend
     */
    public static String paused() {
        String[] responses = {
            "Got it! I'm taking a break.",
            "Taking a nap now. Wake me up when you need something!",
            "Going silent now. I'll wait for you to resume!"
        };
        return responses[random.nextInt(responses.length)];
    }

    /**
     * When user resumes Friend
     */
    public static String resumed() {
        String[] responses = {
            "I'm back! Ready to help. What do you need?",
        };
        return responses[random.nextInt(responses.length)];
    }

    // ============================================================
    // ❓ HELP & GUIDANCE RESPONSES
    // ============================================================

    /**
     * When user asks for help
     */
    public static String helpIntro() {
        String[] responses = {
            "I'm here to help! Let me tell you what I can do.",
        };
        return responses[random.nextInt(responses.length)];
    }

    /**
     * When preferences are saved
     */
    public static String preferencesSaved() {
        String[] responses = {
            "All set! Your settings are locked in.",
            "Got it! Your settings are saved and ready to go.",
            "All good! Your preferences are now my defaults."
        };
        return responses[random.nextInt(responses.length)];
    }

    /**
     * When preferences fail to save
     */
    public static String preferencesSaveFailed() {
        String[] responses = {
            "Something went wrong saving your preferences. Can we try again?",
        };
        return responses[random.nextInt(responses.length)];
    }

    // ============================================================
    // 🎯 DECISION PROMPTS - Asking for Permission
    // ============================================================

    /**
     * Ask user for permission before critical actions
     */
    public static String askPermission(String action) {
        String[] responses = {
            "Is it okay if I " + action + "?",
            "Want me to " + action + "?",
            "Should I " + action + "?",
            "May I " + action + "?",
            "Can I " + action + " for you?",
            "Shall I " + action + "?"
        };
        return responses[random.nextInt(responses.length)];
    }

    /**
     * Confirm before risky operations
     */
    public static String confirmDangerous(String action) {
        String[] responses = {
            "Sure you want to do this " + action + " is a big step. You sure?",
        };
        return responses[random.nextInt(responses.length)];
    }

    // ============================================================
    // 💬 CONVERSATIONAL FILLERS - Making it More Natural
    // ============================================================

    /**
     * Friendly acknowledgments
     */
    public static String acknowledge() {
        String[] responses = {
            "For sure!",
            "You got it!",
            "Absolutely!",
            "No problem!",
            "Sure thing!",
        };
        return responses[random.nextInt(responses.length)];
    }

    /**
     * Apologetic responses
     */
    public static String apologize() {
        String[] responses = {
            "Sorry about that!",
            "Sorry buddy!",
            "My mistake!",
        };
        return responses[random.nextInt(responses.length)];
    }

    /**
     * Waiting messages
     */
    public static String waiting() {
        String[] responses = {
            "Just a moment...",
            "One sec, working on it...",
            "Give me a second...",
        };
        return responses[random.nextInt(responses.length)];
    }

    /**
     * Encouraging messages
     */
    public static String encourage() {
        String[] responses = {
            "You're doing great!",
            "Keep it up!",
        };
        return responses[random.nextInt(responses.length)];
    }

    // ============================================================
    // 🌙 TIME-BASED FRIENDLY GREETINGS
    // ============================================================

    /**
     * Greeting based on time of day
     */
    public static String timeBasedGreeting() {
        int hour = java.time.LocalTime.now().getHour();
        
        if (hour < 6) {
            String[] earlyGreetings = {
                "Good morning what can i do for you",
                "Good morning! Good to see you?"
            };
            return earlyGreetings[random.nextInt(earlyGreetings.length)];
        } else if (hour < 12) {
            String[] morningGreetings = {
                "Good morning! Good to see you?",
                "What a beautiful morning! How can I help?"
            };
            return morningGreetings[random.nextInt(morningGreetings.length)];
        } else if (hour < 18) {
            String[] afternoonGreetings = {
                "Good afternoon! Keep going",
                "Afternoon! Still going strong I see!",
            };
            return afternoonGreetings[random.nextInt(afternoonGreetings.length)];
        } else {
            String[] eveningGreetings = {
                "Good evening!",
                "Good night time! What do you need?"
            };
            return eveningGreetings[random.nextInt(eveningGreetings.length)];
        }
    }

    // ============================================================
    // 📝 UTILITY METHODS
    // ============================================================

    /**
     * Log friendly behavior usage
     */
    public static void log(String category, String message) {
        logger.debug("[FriendlyBehavior] [" + category + "] " + message);
    }
}
