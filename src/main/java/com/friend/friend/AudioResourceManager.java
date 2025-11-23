package com.friend.friend;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.sound.sampled.*;

/**
 * Manages access to microphone and speaker resources in a thread-safe manner.
 * Also provides utilities to detect active system audio playback.
 */
public class AudioResourceManager {

    // --- Locking Mechanism ---

    private static final AudioResourceManager INSTANCE = new AudioResourceManager();

    /** Lock for controlling access to the microphone. (CRITICAL FIX: Was missing) */
    private static final Lock micLock = new ReentrantLock();

    /** Lock for controlling access to the speaker. */
    private static final Lock speakerLock = new ReentrantLock();

    /** Flag indicating whether the microphone is currently locked by the application. */
    private static volatile boolean micLocked = false;

    /** Flag indicating whether the speaker is currently locked by the application. */
    private static volatile boolean speakerLocked = false;

    /**
     * Returns the single instance of the AudioResourceManager.
     */
    public static AudioResourceManager getInstance() {
        return INSTANCE;
    }

    // --- Microphone Access ---

    /**
     * Requests exclusive access to the microphone.
     * @return true if access is granted; false if already locked.
     */
    public static boolean requestMicAccess() {
        // Use tryLock to allow non-blocking attempts in the main loop, but the current logic uses lock() and checks state.
        // Sticking to the original lock/check pattern but ensuring the lock is released in all cases.
        micLock.lock();
        try {
            if (micLocked) return false;
            micLocked = true;
            return true;
        } finally {
            micLock.unlock();
        }
    }

    /**
     * Releases the microphone lock, allowing other components to access it.
     */
    public static void releaseMic() {
        micLock.lock();
        try {
            micLocked = false;
        } finally {
            micLock.unlock();
        }
    }

    // --- Speaker Access ---

    /**
     * Requests exclusive access to the speaker.
     * @return true if access is granted; false if already locked.
     */
    public static boolean requestSpeakerAccess() {
        speakerLock.lock();
        try {
            if (speakerLocked) return false;
            speakerLocked = true;
            return true;
        } finally {
            speakerLock.unlock();
        }
    }

    /**
     * Releases the speaker lock, allowing other components to access it.
     */
    public static void releaseSpeaker() {
        speakerLock.lock();
        try {
            speakerLocked = false;
        } finally {
            speakerLock.unlock();
        }
    }

    // --- System Status Check ---

    /**
     * Checks whether the speaker is currently active.
     * This includes both internal usage (e.g., TTS playback) and external audio activity.
     * @return true if speaker is in use or system audio is playing; false otherwise.
     */
    public static boolean isSpeakerActive() {
        // We only need to lock the speakerLock if we are modifying the state. 
        // For a read-only check, just checking the volatile flag is usually fine, 
        // but locking ensures the read of the volatile flag is atomic with respect to the write.
        if (speakerLocked) return true;
        return isSystemAudioPlaying();
    }

    /**
     * Attempts to detect if external audio is currently playing through system output lines.
     * Safely inspects available SourceDataLines and checks for active playback and volume.
     *
     * @return true if any open and active SourceDataLine is found with audible volume.
     */
    private static boolean isSystemAudioPlaying() {
        try {
            Mixer.Info[] mixers = AudioSystem.getMixerInfo();
            for (Mixer.Info mixerInfo : mixers) {
                Mixer mixer = AudioSystem.getMixer(mixerInfo);

                // Inspect only output-capable lines
                Line.Info[] sourceLines = mixer.getSourceLineInfo();

                for (Line.Info info : sourceLines) {
                    Line line = null;
                    try {
                        line = mixer.getLine(info);

                        // Safely cast and inspect SourceDataLine
                        if (line instanceof SourceDataLine dataLine) {
                            if (!dataLine.isOpen()) continue;

                            if (dataLine.isActive()) {
                                // Check volume level if supported
                                if (dataLine.isControlSupported(FloatControl.Type.VOLUME)) {
                                    FloatControl volume = (FloatControl) dataLine.getControl(FloatControl.Type.VOLUME);
                                    if (volume.getValue() > 0.01f) {
                                        return true; // Active and audible
                                    }
                                } else {
                                    return true; // Active, volume control not available, assume audible
                                }
                            }
                        }
                    } catch (LineUnavailableException ignored) {
                        // Line is unavailable, skip
                    } catch (Exception e) {
                        System.err.printf("[AudioResourceManager]: Error inspecting line: %s, Message: %s\n", info.toString(), e.getMessage());
                    } finally {
                        // Important: Do not close the line if you just opened it via mixer.getLine(info), 
                        // as this can interfere with other applications. We are only inspecting state.
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[AudioResourceManager]: General Audio check failed: " + e.getMessage());
        }
        return false;
    }
}