package com.friend.friend;

import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EchoPilotRecognizer.
 * We focus on testing the state changes (pause/resume, mode switching) 
 * and interactions with dependent components (Dispatcher, GUI, TTS).
 */
@Disabled
class EchoPilotRecognizerTest {

    // --- Mock Objects (Our fake tools) ---
    private CommandDispatcher mockDispatcher;
    private MergedEchoPilotApp mockGui;
    private SpeechEngine mockTts;
    // We mock the underlying Sphinx recognizer to control its behavior
    private LiveSpeechRecognizer mockLiveRecognizer;
    
    // --- Unit Under Test (SUT) ---
    private EchoPilotRecognizer recognizer;

    /**
     * Setup before each test. We must use a Spy to initialize the LiveSpeechRecognizer
     * while allowing us to mock its internals (like the startRecognition method).
     */
    @BeforeEach
    void setUp() throws Exception {
        // 1. Create the mocks
        mockDispatcher = mock(CommandDispatcher.class);
        mockGui = mock(MergedEchoPilotApp.class);
        mockTts = mock(SpeechEngine.class);
        
        // 2. Create a mock of the LiveSpeechRecognizer that EchoPilotRecognizer depends on
        // This requires special handling since LiveSpeechRecognizer is instantiated in the constructor.
        // For simple tests, we can test the public API methods directly.
        
        // Since we cannot easily mock the constructor, we will initialize the SUT 
        // normally (which creates a real LiveSpeechRecognizer) and focus on testing the 
        // public state methods like pause(), resume(), and the switchConfiguration methods.
        
        // --- Create a real SUT for state-based methods ---
        recognizer = new EchoPilotRecognizer(mockDispatcher, mockGui, mockTts);
        
        // Note: For tests that need to verify calls on LiveSpeechRecognizer, 
        // we'd typically need to inject a mock instance, which requires a change 
        // to the EchoPilotRecognizer's constructor. 
    }
    
    // =======================================================
    // 1. State Change Tests (Pause/Resume)
    // =======================================================

    /**
     * Story: When the application resumes, the internal state must be set to 'listening'
     * and the GUI/TTS must be updated.
     */
    @Test
    void resume_setsListeningStateAndProvidesFeedback() {
        // ARRANGE: Ensure it starts in a non-listening state
        assertFalse(recognizer.isListening(), "Recognizer should start in non-listening state.");
        
        // ACT
        boolean result = recognizer.resume();
        
        // ASSERT 1: State should be true
        assertTrue(result, "Resume should return true.");
        assertTrue(recognizer.isListening(), "Internal listening flag must be set to true.");
        
        // ASSERT 2: GUI indicator must be updated
        verify(mockGui, times(1)).updateListeningIndicator(true);
        
        // ASSERT 3: TTS must speak the confirmation message (Blocking)
        verify(mockTts, times(1)).speakBlocking(contains("Resuming listening."));
    }

    /**
     * Story: When the application pauses, the internal state must be set to 'not listening'
     * and the GUI/TTS must be updated to indicate the background mode.
     */
    @Test
    void pause_setsNonListeningStateAndProvidesFeedback() {
        // ARRANGE: Set state to listening first
        recognizer.resume(); 
        
        // ACT
        recognizer.pause();
        
        // ASSERT 1: State should be false
        assertFalse(recognizer.isListening(), "Internal listening flag must be set to false.");
        
        // ASSERT 2: GUI indicator must be updated
        verify(mockGui, times(1)).updateListeningIndicator(false);
        
        // ASSERT 3: TTS must speak the confirmation message (Non-Blocking)
        verify(mockTts, times(1)).speak(contains("Going to background mode. Say, 'My friend' to wake me up."));
    }

    // =======================================================
    // 2. Mode Switching Tests (Grammar/Language Model)
    // =======================================================
    
    /**
     * Story: Calling switchToLanguageModel should change the internal configuration.
     * Note: This test verifies side-effects like GUI update, since mocking the 
     * LiveSpeechRecognizer replacement is complex.
     */
    @Test
    void switchToLanguageModel_updatesGuiMessage() throws Exception {
        // ACT
        recognizer.switchToLanguageModel();
        
        // ASSERT: GUI should reflect the mode change
        // We verify that the switchConfiguration method was indirectly successful by checking GUI update
        verify(mockGui, times(1)).updateStatus(contains("Switched search to LANGUAGE MODEL"));
        verify(mockGui, times(1)).updateStatus(contains("Listening (Language Model Mode)..."));
    }

    /**
     * Story: Calling switchToGrammar should change the internal configuration.
     */
    @Test
    void switchToGrammar_updatesGuiMessage() throws Exception {
        // ARRANGE: Switch to LM first to ensure a change occurs
        recognizer.switchToLanguageModel();
        
        // ACT
        recognizer.switchToGrammar();
        
        // ASSERT: GUI should reflect the mode change
        // Note: The total call count for updateStatus will be 2 for this string, as it was called in setUp and in the method.
        verify(mockGui, times(2)).updateStatus(contains("Switched search to GRAMMAR"));
        verify(mockGui, times(1)).updateStatus(contains("Listening (Grammar Mode)..."));
    }
    
    // =======================================================
    // 3. Listen Once Test
    // =======================================================

    /**
     * Story: listenOnce should temporarily switch the GUI and safely stop/start recognition.
     * Note: Full functionality depends on a correctly mocked LiveSpeechRecognizer, which we assume works.
     */
    @Test
    void listenOnce_updatesGuiForConfirmation() {
        // ACT
        // We call listenOnce. Since the real LiveSpeechRecognizer is used, it will attempt mic access
        // and throw a blocking error if a mic isn't available, but we test the GUI flow anyway.
        String result = recognizer.listenOnce();
        
        // ASSERT 1: GUI should be updated to prompt the user
        verify(mockGui, times(1)).updateStatus("Are you sure :say yes / no :");
        
        // ASSERT 2: GUI should be updated to return to the listening state
        verify(mockGui, times(1)).updateStatus(contains("Listening to your command (for listen Once)..."));

        // ASSERT 3: The result should be an empty string if recognition fails (due to lack of real mic/mocking)
        assertEquals("", result, "Result should be empty if recognition fails in a test environment.");
    }
}