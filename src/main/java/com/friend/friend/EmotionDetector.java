package com.friend.friend;

/**
 * EmotionDetector (stub)
 *
 * This is a lightweight stub that demonstrates the API for emotion detection.
 * It is intentionally minimal: real emotion/sentiment detection requires a
 * trained model or external API. The detector is provided as an opt-in service
 * and must be wired to the audio capture pipeline by the integrator.
 */
public class EmotionDetector {
    private volatile boolean enabled = false;

    public EmotionDetector() {}

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() { return enabled; }

    /**
     * Analyze raw audio (PCM) bytes and return a simple label.
     * This stub always returns "neutral". Replace with real model call.
     */
    public String analyze(byte[] audioPcm) {
        if (!enabled || audioPcm == null || audioPcm.length == 0) return "neutral";
        // TODO: integrate real model or external service here.
        return "neutral";
    }
}
