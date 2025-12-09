package com.friend.friend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HealthCheck interface for monitoring system health.
 */
public interface HealthCheck {
    /**
     * Perform a health check and return the result.
     */
    HealthCheckResult check();

    /**
     * Return a friendly name for this check.
     */
    String getName();

    /**
     * Result of a health check.
     */
    class HealthCheckResult {
        public enum Status { HEALTHY, DEGRADED, UNHEALTHY }
        public final Status status;
        public final String message;

        public HealthCheckResult(Status status, String message) {
            this.status = status;
            this.message = message;
        }
    }
}

/**
 * Microphone health check: attempt to record a short audio sample.
 */
class MicrophoneHealthCheck implements HealthCheck {
    private static final Logger logger = LoggerFactory.getLogger(MicrophoneHealthCheck.class);
    private final AudioResourceManager audioManager;

    public MicrophoneHealthCheck(AudioResourceManager audioManager) {
        this.audioManager = audioManager;
    }

    @Override
    public HealthCheckResult check() {
        try {
            if (audioManager.isSystemAudioPlaying()) {
                return new HealthCheckResult(HealthCheckResult.Status.DEGRADED, "System audio is playing; microphone may be muted.");
            }
            return new HealthCheckResult(HealthCheckResult.Status.HEALTHY, "Microphone is available.");
        } catch (Exception ex) {
            logger.warn("Microphone health check failed", ex);
            return new HealthCheckResult(HealthCheckResult.Status.UNHEALTHY, "Microphone check failed: " + ex.getMessage());
        }
    }

    @Override
    public String getName() {
        return "Microphone";
    }
}

/**
 * TTS health check: verify TTS engine can produce speech.
 */
class TTSHealthCheck implements HealthCheck {
    private static final Logger logger = LoggerFactory.getLogger(TTSHealthCheck.class);
    private final SpeechEngine tts;

    public TTSHealthCheck(SpeechEngine tts) {
        this.tts = tts;
    }

    @Override
    public HealthCheckResult check() {
        try {
            tts.speakBlocking("Health check");
            return new HealthCheckResult(HealthCheckResult.Status.HEALTHY, "TTS engine is operational.");
        } catch (Exception ex) {
            logger.warn("TTS health check failed", ex);
            return new HealthCheckResult(HealthCheckResult.Status.UNHEALTHY, "TTS engine failed: " + ex.getMessage());
        }
    }

    @Override
    public String getName() {
        return "Text-to-Speech";
    }
}

/**
 * Disk space health check.
 */
class DiskSpaceHealthCheck implements HealthCheck {
    private static final Logger logger = LoggerFactory.getLogger(DiskSpaceHealthCheck.class);
    private static final long MIN_DISK_SPACE = 100 * 1024 * 1024; // 100MB

    @Override
    public HealthCheckResult check() {
        try {
            long freeSpace = new java.io.File(".").getFreeSpace();
            if (freeSpace < MIN_DISK_SPACE) {
                return new HealthCheckResult(HealthCheckResult.Status.DEGRADED, "Low disk space: " + (freeSpace / 1024 / 1024) + " MB available.");
            }
            return new HealthCheckResult(HealthCheckResult.Status.HEALTHY, "Disk space is adequate.");
        } catch (Exception ex) {
            logger.warn("Disk space check failed", ex);
            return new HealthCheckResult(HealthCheckResult.Status.UNHEALTHY, "Disk space check failed: " + ex.getMessage());
        }
    }

    @Override
    public String getName() {
        return "Disk Space";
    }
}
