// package com.friend.friend;

// import java.util.concurrent.locks.Lock;
// import java.util.concurrent.locks.ReentrantLock;
// import javax.sound.sampled.AudioSystem;
// import javax.sound.sampled.FloatControl;
// import javax.sound.sampled.Line;
// import javax.sound.sampled.LineUnavailableException;
// import javax.sound.sampled.Mixer;
// import javax.sound.sampled.SourceDataLine;
// import javax.sound.sampled.FloatControl.Type;
// import javax.sound.sampled.Mixer.Info;

// /**
//  * Manages exclusive access to audio resources (Microphone and Speaker)
//  * using ReentrantLocks to prevent concurrent access by different threads
//  * and provides a helper to check system audio status.
//  * Story: This is the traffic director 🚦 for all sound coming in and out.
//  */
// public class AudioResourceManager {
    
//     private static final Lock micLock = new ReentrantLock();
//     private static final Lock speakerLock = new ReentrantLock();
    
//     // Volatile flags ensure visibility across threads
//     private static volatile boolean micLocked = false;
//     private static volatile boolean speakerLocked = false;

//     // --- Microphone Management ---

//     /**
//      * Attempts to acquire exclusive access to the microphone.
//      * @return true if access was granted, false if already locked.
//      */
//     public static boolean requestMicAccess() {
//         micLock.lock();
//         try {
//             if (micLocked) {
//                 return false; // Already locked
//             }
//             micLocked = true;
//             return true; // Granted
//         } finally {
//             micLock.unlock();
//         }
//     }

//     /**
//      * Releases the microphone lock.
//      */
//     public static void releaseMic() {
//         micLock.lock();
//         try {
//             micLocked = false;
//         } finally {
//             micLock.unlock();
//         }
//     }

//     // --- Speaker Management ---

//     /**
//      * Attempts to acquire exclusive access to the speaker.
//      * @return true if access was granted, false if already locked.
//      */
//     public static boolean requestSpeakerAccess() {
//         speakerLock.lock();
//         try {
//             // FIX: Add check for system audio before granting lock 
//             // to ensure we don't speak over external audio.
//             if (speakerLocked || isSystemAudioPlaying()) { 
//                  return false; // Already locked by us or by system audio
//             }
//             speakerLocked = true;
//             return true; // Granted
//         } finally {
//             speakerLock.unlock();
//         }
//     }

//     /**
//      * Releases the speaker lock.
//      */
//     public static void releaseSpeaker() {
//         speakerLock.lock();
//         try {
//             speakerLocked = false;
//         } finally {
//             speakerLock.unlock();
//         }
//     }

//     // --- Status Checks ---

//     /**
//      * Checks if the speaker is currently in use by this application.
//      * (We omit the complex system audio check here to avoid re-running it constantly).
//      * @return true if this app has locked the speaker, false otherwise.
//      */
//     public static boolean isSpeakerLockedByApp() {
//         return speakerLocked;
//     }
    
//     /**
//      * Checks if the speaker is currently in use, either by this app or the system.
//      * @return true if speaker is active, false otherwise.
//      */
//     public static boolean isSpeakerActive() {
//         return speakerLocked || isSystemAudioPlaying();
//     }

//     /**
//      * Iterates through all available mixers and SourceDataLines to check 
//      * if any audio is actively playing through the system speakers.
//      * * NOTE: This is inherently unreliable across all OS/Java versions but 
//      * is the standard approach using javax.sound.sampled.
//      * @return true if system audio is playing, false otherwise.
//      */
//     public static boolean isSystemAudioPlaying() {
//         try {
//             Info[] mixerInfos = AudioSystem.getMixerInfo();
            
//             for(Info mixerInfo : mixerInfos) {
//                 Mixer mixer = AudioSystem.getMixer(mixerInfo);
//                 javax.sound.sampled.Line.Info[] sourceLines = mixer.getSourceLineInfo();
                
//                 for(javax.sound.sampled.Line.Info info : sourceLines) {
//                     Line line = null;

//                     try {
//                         // Attempt to open or retrieve the line
//                         line = mixer.getLine(info); 
                        
//                         if (line instanceof SourceDataLine) {
//                             SourceDataLine dataLine = (SourceDataLine)line;
                            
//                             // Check if line is open and actively running (streaming data)
//                             if (dataLine.isOpen() && dataLine.isActive()) {
                                
//                                 // FIX: More robust volume check logic
//                                 if (dataLine.isControlSupported(Type.VOLUME)) {
//                                     FloatControl volume = (FloatControl) dataLine.getControl(Type.VOLUME);
//                                     float min = volume.getMinimum();
//                                     float max = volume.getMaximum();
//                                     float val = volume.getValue();
//                                     float epsilon = (max - min) * 0.01f; // 1% threshold
                                    
//                                     // Only return true if volume is detectably above minimum
//                                     if (val > min + epsilon) { 
//                                         return true; 
//                                     }
//                                 } else {
//                                     // If volume control isn't supported, we assume it's active.
//                                     return true; 
//                                 }
//                             }
//                         }
//                     } catch (LineUnavailableException e) {
//                         // Ignore lines that are currently unavailable (often due to being in use)
//                     } catch (Exception e) {
//                         System.err.printf("[AudioResourceManager]: Error inspecting line: %s, \nMessage: %s\n", info.toString(), e.getMessage());
//                     } 
//                     // No need to close the line if we only used getLine(info) for inspection
//                 }
//             }
//         } catch (Exception e) {
//             System.err.println("[AudioResourceManager]: General Audio check failed: " + e.getMessage());
//         }

//         return false;
//     }
// }
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

/**
 * Manages exclusive access to audio resources (Microphone and Speaker)
 * using ReentrantLocks to prevent concurrent access by different threads
 * and provides a helper to check system audio status.
 * Story: This is the traffic director 🚦 for all sound coming in and out.
 */
public class AudioResourceManager {
    
    private static final Lock micLock = new ReentrantLock();
    private static final Lock speakerLock = new ReentrantLock();
    
    // We only need the volatile flag for the speaker lock because of the 
    // custom check (isSystemAudioPlaying()) inside the critical section.
    private static volatile boolean speakerLocked = false; 

    // --- Microphone Management ---

    /**
     * Attempts to acquire exclusive access to the microphone.
     * @return true if access was granted, false if already locked.
     */
    public static boolean requestMicAccess() {
        // FIX: Use ReentrantLock.tryLock() directly for non-blocking acquisition.
        // It returns true if the lock was acquired, false otherwise.
        return micLock.tryLock(); 
    }

    /**
     * Releases the microphone lock.
     */
    public static void releaseMic() {
        // FIX: Assume the caller owns the lock and unlock it.
        // If the caller does not own it, it will throw an IllegalMonitorStateException,
        // which is the standard way Java handles this error.
        if (micLock instanceof ReentrantLock && ((ReentrantLock) micLock).isHeldByCurrentThread()) {
            micLock.unlock();
        } else {
            // Log a warning if a thread tried to release a lock it didn't hold
            System.err.println("[Audio Lock] Warning: Thread tried to release micLock it did not hold.");
        }
    }

    // --- Speaker Management (Kept the original robust logic for the system check) ---

    /**
     * Attempts to acquire exclusive access to the speaker.
     * @return true if access was granted, false if already locked.
     */
    public static boolean requestSpeakerAccess() {
        speakerLock.lock();
        try {
            // FIX: Check for system audio before granting lock
            if (speakerLocked || isSystemAudioPlaying()) { 
                 return false; // Already locked by us or by system audio
            }
            speakerLocked = true;
            return true; // Granted
        } finally {
            speakerLock.unlock();
        }
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

    // --- Status Checks ---

    /**
     * Checks if the speaker is currently in use by this application.
     * @return true if this app has locked the speaker, false otherwise.
     */
    public static boolean isSpeakerLockedByApp() {
        return speakerLocked;
    }
    
    /**
     * Checks if the speaker is currently in use, either by this app or the system.
     * @return true if speaker is active, false otherwise.
     */
    public static boolean isSpeakerActive() {
        return speakerLocked || isSystemAudioPlaying();
    }

    /**
     * Iterates through all available mixers and SourceDataLines to check 
     * if any audio is actively playing through the system speakers.
     * * NOTE: This is inherently unreliable across all OS/Java versions but 
     * * is the standard approach using javax.sound.sampled.
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
                        // Attempt to open or retrieve the line
                        line = mixer.getLine(info); 
                        
                        if (line instanceof SourceDataLine) {
                            SourceDataLine dataLine = (SourceDataLine)line;
                            
                            // Check if line is open and actively running (streaming data)
                            if (dataLine.isOpen() && dataLine.isActive()) {
                                
                                // FIX: More robust volume check logic
                                if (dataLine.isControlSupported(Type.VOLUME)) {
                                    FloatControl volume = (FloatControl) dataLine.getControl(Type.VOLUME);
                                    float min = volume.getMinimum();
                                    float max = volume.getMaximum();
                                    float val = volume.getValue();
                                    float epsilon = (max - min) * 0.01f; // 1% threshold
                                    
                                    // Only return true if volume is detectably above minimum
                                    if (val > min + epsilon) { 
                                        return true; 
                                    }
                                } else {
                                    // If volume control isn't supported, we assume it's active.
                                    return true; 
                                }
                            }
                        }
                    } catch (LineUnavailableException e) {
                        // Ignore lines that are currently unavailable (often due to being in use)
                    } catch (Exception e) {
                        System.err.printf("[AudioResourceManager]: Error inspecting line: %s, \nMessage: %s\n", info.toString(), e.getMessage());
                    } 
                    // No need to close the line if we only used getLine(info) for inspection
                }
            }
        } catch (Exception e) {
            System.err.println("[AudioResourceManager]: General Audio check failed: " + e.getMessage());
        }

        return false;
    }
}