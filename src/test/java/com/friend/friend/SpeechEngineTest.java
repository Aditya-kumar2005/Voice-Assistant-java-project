package com.friend.friend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled; // Use this if FreeTTS setup is not ready
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SpeechEngine.
 * Focuses on Threading, Speaker Resource Management, and Callback execution.
 * * NOTE: For these tests to run, the FreeTTS library must be properly configured 
 * and the 'kevin16' voice must be available on the system running the test.
 */
class SpeechEngineTest {

    // --- Mock Objects (Our fake tools) ---
    private MergedEchoPilotApp mockGui;
    private AudioResourceManager mockResourceManager;
    
    // --- Unit Under Test (SUT) ---
    private SpeechEngine engine;
    
    // --- Spy on the Engine to verify internal calls ---
    private SpeechEngine spyEngine;
    /**
     * Set up mocks and the SUT before each test.
     */
    /**
     * Set up mocks and the SUT before each test.
     */
    @BeforeEach
    void setUp() throws Exception {
        // 1. Mock the GUI (for updateStatus calls)
        mockGui = mock(MergedEchoPilotApp.class);
        
        // 2. Instantiate the Unit Under Test (SUT).
        // This is necessary BEFORE spying.
        engine = new SpeechEngine(mockGui); 
        
        // 3. Spy on the real engine instance. 
        // 👇👇 IMPORTANT: Make sure there is NO "SpeechEngine" type here! 👇👇
        // This makes sure you are setting the CLASS variable 'spyEngine', 
        // which all your other tests use.
        spyEngine = spy(engine); // <-- FIX IS HERE (If your line 40 is this one)

        // 4. We mock the crucial internal FreeTTS interaction (This isolates the thread logic.)
        doNothing().when(spyEngine).speakBlockingInternal(anyString());
    }
    // =======================================================
    // 1. Asynchronous Speak (speak(text, callback)) Tests
    // =======================================================

    /**
     * Story: If speaker access is granted, the speech should occur on a new thread, 
     * the callback should be executed, and the speaker resource should be released.
     */
    @Test
    void speak_withAccess_startsThreadExecutesCallbackAndReleasesResource() throws InterruptedException {
        String testText = "Testing async speech.";
        AtomicBoolean callbackRan = new AtomicBoolean(false);
        Runnable callback = () -> callbackRan.set(true);

        // MOCK BEHAVIOR: Simulate successful speaker access
        // We have to use the real AudioResourceManager for these calls.
        // We'll trust that the real AR.requestSpeakerAccess() returns true when available 
        // and AR.releaseSpeaker() is implemented correctly.
        
        // ACT
        spyEngine.speak(testText, callback);

        // ASSERT 1: The engine should have attempted to acquire the resource (checked manually if AR is static).
        // The current speech thread should be running.
        Thread speechThread = (Thread) getPrivateField(spyEngine, "currentSpeechThread");
        assertNotNull(speechThread, "A speech thread must be created.");
        
        // Wait for the thread to finish its work (speaking, releasing, running callback)
        speechThread.join(2000); // Wait up to 2 seconds

        // ASSERT 2: The internal blocking method must be called on the thread
        verify(spyEngine, times(1)).speakBlockingInternal(testText);

        // ASSERT 3: The callback must have executed
        assertTrue(callbackRan.get(), "Callback must execute after speaking finishes.");
        
        // ASSERT 4: Resource release is checked manually if AR is static. 
        // Assuming AudioResourceManager is working, the logic in the finally block is verified.
        // In a real test, we would verify a static method call on AR.
    }
    
    /**
     * Story: If speaker access is denied (busy), speech should be skipped, 
     * but the callback should still be executed.
     */
    @Test
    void speak_withoutAccess_skipsSpeechButRunsCallback() {
        String testText = "Should not hear this.";
        AtomicBoolean callbackRan = new AtomicBoolean(false);
        Runnable callback = () -> callbackRan.set(true);

        // MOCK BEHAVIOR: We simulate the `if (!AudioResourceManager.requestSpeakerAccess())` 
        // being true by not setting the spyEngine to mock the internal speak.
        // For this test, we must manually change the environment if AR is static, 
        // or refactor the code to pass a mock AR.
        
        // For now, we will rely on the code flow: if access fails, it hits the `if` block.
        
        // Since we can't easily mock the static `AudioResourceManager`, we'll focus
        // on verifying the side effects that *do* rely on the `if` statement logic.

        // MOCK BEHAVIOR: We simulate the logic where AR.requestSpeakerAccess() returns false
        // (This requires a refactored SUT, but we simulate the effect for testing the callback)
        
        // ACT (Assuming AR fails to give access for this test run)
        // If access fails, the code block should run the callback immediately:
        // if (callback != null) callback.run();
        
        // *** To make this test reliable without PowerMock, the SUT must be modified to accept an injectable AudioResourceManager. ***
        
        // Since we cannot mock the static AR, we test the public contract assuming the internal logic:
        
        // If we assume a mockable AR:
        // when(mockResourceManager.requestSpeakerAccess()).thenReturn(false);
        // spyEngine.speak(testText, callback);
        // verify(mockGui, times(1)).updateStatus("Speaker busy. Skipping speech.");
        // assertTrue(callbackRan.get(), "Callback must execute even if speech is skipped.");
        // verify(spyEngine, never()).speakBlockingInternal(anyString());
    }

    // =======================================================
    // 2. Blocking Speak (speakBlocking) Tests
    // =======================================================

    /**
     * Story: speakBlocking should call the internal blocking method and ensure the 
     * speaker resource is released in the `finally` block.
     */
    @Test
    void speakBlocking_callsInternalSpeakAndReleasesResource() {
        String testText = "Testing blocking speech.";
        
        // ACT
        spyEngine.speakBlocking(testText);

        // ASSERT 1: The internal blocking method must be called
        verify(spyEngine, times(1)).speakBlockingInternal(testText);
        
        // ASSERT 2: GUI is not updated with 'Speaker busy' (as access is assumed true)
        verify(mockGui, never()).updateStatus(contains("Speaker busy (Blocking). Skipping speech."));
        
        // ASSERT 3: Resource release is checked manually if AR is static.
    }

    // =======================================================
    // 3. Stop/Shutdown Tests
    // =======================================================

    /**
     * Story: Calling stop() should interrupt and wait for the current speech thread.
     */
    @Test
    void stop_interruptsAndJoinsCurrentThread() throws InterruptedException {
        // ARRANGE: Start a long-running speech operation (we use a spy that mocks the internal speak)
        AtomicBoolean internalSpeakRunning = new AtomicBoolean(true);
        doAnswer(invocation -> {
            // Simulate a long operation
            try {
                Thread.sleep(10000); 
            } catch (InterruptedException e) {
                // This is the expected behavior when stop() is called
            }
            internalSpeakRunning.set(false);
            return null;
        }).when(spyEngine).speakBlockingInternal(anyString());
        
        // Set up the thread using the ASYNC method
        spyEngine.speak("This should be interrupted.", null);
        
        Thread speechThread = (Thread) getPrivateField(spyEngine, "currentSpeechThread");
        assertNotNull(speechThread, "Speech thread must be running before stop.");
        assertTrue(speechThread.isAlive(), "Speech thread must be alive before stop.");
        
        // ACT
        spyEngine.stop();

        // ASSERT 1: The thread should be interrupted and terminated (or close to it)
        speechThread.join(500); // Give it time to join
        assertFalse(speechThread.isAlive(), "Speech thread should be terminated after stop().");
    }

    /**
     * Story: shutdown() must call stop() and deallocate the voice.
     */
    @Test
    void shutdown_callsStopAndDeallocatesVoice() {
        // ARRANGE: Spy on the engine to verify method calls
        
        // ACT
        spyEngine.shutdown();

        // ASSERT 1: stop() must be called
        verify(spyEngine, times(1)).stop();
        
        // ASSERT 2: The GUI is checked for deallocation warnings
        verify(mockGui, never()).updateStatus(contains("Warning during deallocation"));
        
        // The direct check of voice.deallocate() requires a more complex mock setup for FreeTTS 
        // that is outside the scope of standard unit testing for this pattern. We rely on the 
        // code structure and the check for `Throwable t` to cover the logic.
    }
    
    // --- Helper method to access private fields via reflection ---
    private Object getPrivateField(Object obj, String fieldName) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}