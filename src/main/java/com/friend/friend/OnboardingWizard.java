package com.friend.friend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

/**
 * OnboardingWizard guides the user through initial setup: microphone test, TTS test, permissions.
 */
public class OnboardingWizard {
    private static final Logger logger = LoggerFactory.getLogger(OnboardingWizard.class);
    private final SpeechEngine tts;
    private final EchoPilotRecognizer recognizer;
    private final CommandDispatcher dispatcher;
    private final SettingsManager settingsManager;
    private final List<HealthCheck> healthChecks;

    public OnboardingWizard(SpeechEngine tts, EchoPilotRecognizer recognizer, CommandDispatcher dispatcher, SettingsManager settingsManager) {
        this.tts = tts;
        this.recognizer = recognizer;
        this.dispatcher = dispatcher;
        this.settingsManager = settingsManager;
        this.healthChecks = new ArrayList<>();
    }

    public void addHealthCheck(HealthCheck check) {
        healthChecks.add(check);
    }

    /**
     * Run the onboarding wizard if this is the first launch.
     */
    public void runIfFirstLaunch() {
        if (settingsManager.isFirstLaunch()) {
            logger.info("First launch detected; running onboarding wizard.");
            runWizard();
            settingsManager.setFirstLaunchCompleted();
        }
    }

    /**
     * Run the complete onboarding wizard.
     */
    public void runWizard() {
        logger.info("Starting onboarding wizard.");
        try {
            tts.speakBlocking("Hello Friend , Let's set you up.");
            Thread.sleep(500);

            // Step 1: Health checks
            runHealthChecks();

            // Step 2: Microphone test
            testMicrophone();

            // Step 3: TTS test
            testTTS();

            // Step 4: Settings
            configureSettings();

            tts.speakBlocking("Setup complete. You can now use Friend.");
            logger.info("Onboarding wizard completed successfully.");
        } catch (InterruptedException ex) {
            logger.error("Onboarding wizard interrupted", ex);
            Thread.currentThread().interrupt();
        }
    }

    private void runHealthChecks() throws InterruptedException {
        tts.speakBlocking("Running system health checks.");
        Thread.sleep(300);

        boolean allHealthy = true;
        for (HealthCheck check : healthChecks) {
            HealthCheck.HealthCheckResult result = check.check();
            logger.info(check.getName() + " check: " + result.status + " - " + result.message);
            System.out.println("[Onboarding] " + check.getName() + ": " + result.status);

            if (result.status == HealthCheck.HealthCheckResult.Status.UNHEALTHY) {
                allHealthy = false;
                tts.speakBlocking(check.getName() + " check failed. " + result.message);
            } else if (result.status == HealthCheck.HealthCheckResult.Status.DEGRADED) {
                tts.speakBlocking(check.getName() + " is degraded. " + result.message);
            }
        }

        if (allHealthy) {
            tts.speakBlocking("All systems healthy.");
        }
        Thread.sleep(500);
    }

    private void testMicrophone() throws InterruptedException {
        tts.speakBlocking("Let's test your microphone. Please say hello after the beep.");
        Thread.sleep(500);

        recognizer.pause();
        recognizer.resume();
        try {
            String heard = recognizer.listenOnce();
            if (heard != null && !heard.isEmpty()) {
                tts.speakBlocking("I heard you say: " + heard + ". Microphone is working.");
                logger.info("Microphone test successful: heard '" + heard + "'");
            } else {
                tts.speakBlocking("I didn't hear anything. Please check your microphone.");
                logger.warn("Microphone test: no input detected.");
            }
        } catch (Exception ex) {
            logger.error("Microphone test failed", ex);
            tts.speakBlocking("Microphone test failed. Please check your audio setup.");
        }
        Thread.sleep(500);
    }

    private void testTTS() throws InterruptedException {
        tts.speakBlocking("Now testing text to speech. If you hear this, TTS is working.");
        logger.info("TTS test: spoke test message.");
        Thread.sleep(1000);
    }

    private void configureSettings() throws InterruptedException {
        tts.speakBlocking("Do you want to enable error reporting and analytics? This helps us improve. Say yes or no.");
        Thread.sleep(300);

        recognizer.pause();
        recognizer.resume();
        try {
            String response = recognizer.listenOnce();
            if (response != null && response.toLowerCase().contains("yes")) {
                settingsManager.setEnableTelemetry(true);
                tts.speakBlocking("Analytics enabled.");
                logger.info("User enabled telemetry.");
            } else {
                settingsManager.setEnableTelemetry(false);
                tts.speakBlocking("Analytics disabled.");
                logger.info("User disabled telemetry.");
            }
        } catch (Exception ex) {
            logger.error("Settings configuration failed", ex);
            settingsManager.setEnableTelemetry(false);
        }
        Thread.sleep(500);
    }

    /**
     * Optionally run just the health checks without full wizard.
     */
    public void quickHealthCheck() {
        logger.info("Running quick health check.");
        for (HealthCheck check : healthChecks) {
            HealthCheck.HealthCheckResult result = check.check();
            System.out.println("[Health] " + check.getName() + ": " + result.status + " - " + result.message);
            logger.info(check.getName() + ": " + result.status);
        }
    }
}
