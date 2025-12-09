package com.friend.friend;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.FloatControl.Type;
import javax.sound.sampled.Mixer.Info;

public class AudioResourceManager {
    private static final Lock micLock = new ReentrantLock();
    private static final Lock speakerLock = new ReentrantLock();
    private static volatile boolean micLocked = false;
    private static volatile boolean speakerLocked = false;

    /**
     * Attempts to acquire exclusive access to the microphone.
     * @return true if access was granted, false if already locked.
     */
    public static boolean requestMicAccess() {
        micLock.lock();
        boolean granted;

        try {
            if (micLocked) {
                granted = false;
                return granted;
            }

            micLocked = true;
            granted = true;
        } finally {
            micLock.unlock();
        }

        return granted;
    }

    /**
     * Releases the microphone lock.
     */
    public static void releaseMic() {
        micLock.lock();
        try {
            micLocked = false;
        } finally {
            micLock.unlock();
        }
    }

    /**
     * Attempts to acquire exclusive access to the speaker.
     * @return true if access was granted, false if already locked.
     */
    public static boolean requestSpeakerAccess() {
        speakerLock.lock();
        boolean granted;

        try {
            if (speakerLocked) {
                granted = false;
                return granted;
            }

            speakerLocked = true;
            granted = true;
        } finally {
            speakerLock.unlock();
        }

        return granted;
    }

    /**
     * Releases the speaker lock.
     */
    public static void releaseSpeaker() {
        speakerLock.lock();
        try {
            speakerLocked = false;
        } finally {
            speakerLock.unlock();
        }
    }

    /**
     * Checks if the speaker is currently in use, either because 
     * this application has locked it (speakerLocked == true) or 
     * because other system audio is currently playing.
     * @return true if speaker is active, false otherwise.
     */
    public static boolean isSpeakerActive() {
        return speakerLocked || isSystemAudioPlaying();
    }

    /**
     * Iterates through all available mixers and SourceDataLines to check 
     * if any audio is actively playing through the system speakers.
     * @return true if system audio is playing, false otherwise.
     */
    public static boolean isSystemAudioPlaying() {
        try {
            Info[] mixerInfos = AudioSystem.getMixerInfo();
            
            for(Info mixerInfo : mixerInfos) {
                Mixer mixer = AudioSystem.getMixer(mixerInfo);
                javax.sound.sampled.Line.Info[] sourceLines = mixer.getSourceLineInfo();
                
                for(javax.sound.sampled.Line.Info info : sourceLines) {
                    Line line = null;

                    try {
                        line = mixer.getLine(info);
                        
                        if (line instanceof SourceDataLine) {
                            SourceDataLine dataLine = (SourceDataLine)line;
                            
                            // Check if line is open and actively playing/processing audio
                            if (dataLine.isOpen() && dataLine.isActive()) {
                                
                                // Check volume control, if supported
                                if (!dataLine.isControlSupported(Type.VOLUME)) {
                                    return true; // Assume active if we can't check volume
                                }
                                //=======================
                                try {
                                    FloatControl volume = (FloatControl) dataLine.getControl(Type.VOLUME);
                                    float min = volume.getMinimum();
                                    float max = volume.getMaximum();
                                    float val = volume.getValue();
                                    float epsilon = (max - min) * 0.01f; // 1% threshold
                                    if (val > min + epsilon) {
                                        return true;
                                    }
                                } catch (IllegalArgumentException iae) {
                                    return true; // assume active if control lookup unexpectedly fails
                                }
                                //=========================
                                //               Original
                                // FloatControl volume = (FloatControl)dataLine.getControl(Type.VOLUME);
                                // // Check if volume is above a minimal threshold
                                // if (volume.getValue() > 0.01F) { 
                                //     return true;
                                // }
                                //========================================
                            }
                        }
                    } catch (LineUnavailableException e) {
                        // Ignore lines that are unavailable
                    } catch (Exception e) {
                        System.err.printf("[AudioResourceManager]: Error inspecting line: %s, \nMessage: %s\n", info.toString(), e.getMessage());
                    } finally {
                        // Note: Generally, you shouldn't close the line here 
                        // if it belongs to another application or is managed by the system.
                        // However, if getLine() opens it, it should be closed. 
                        // In this scenario, we trust the Mixers to manage their lines.
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[AudioResourceManager]: General Audio check failed: " + e.getMessage());
        }

        return false;
    }
}