package com.friend.friend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*; // Static imports for Mockito

/**
 * Unit tests for CommandDispatcher.
 * We use Mockito to create 'fake' versions of the other classes 
 * (SpeechEngine, Recognizer, SystemCommands) so we can check if 
 * the CommandDispatcher tells them to do the right things.
 */
class CommandDispatcherTest {

    // --- Mock Objects (Our fake tools) ---
    private SpeechEngine mockTts;
    private SystemCommands mockSystemCommands;
    private MergedEchoPilotApp mockGui;
    private EchoPilotRecognizer mockRecognizer;
    
    // --- Unit Under Test ---
    private CommandDispatcher dispatcher;

    // --- Variables for testing command execution ---
    private boolean fixedCommandExecuted = false;
    private Runnable fixedCommandAction;
    private Map<String, Runnable> initialCommandMap;

    /**
     * Set up the testing environment before each test runs.
     * This is like setting up the board game pieces every time.
     */
    @BeforeEach
    void setUp() throws Exception {
        // Create the fake objects
        mockTts = mock(SpeechEngine.class);
        mockSystemCommands = mock(SystemCommands.class);
        mockGui = mock(MergedEchoPilotApp.class);
        mockRecognizer = mock(EchoPilotRecognizer.class);
        
        // Initialize the Dispatcher with the fake tools
        dispatcher = new CommandDispatcher(mockTts, mockSystemCommands, mockGui);
        // Important: Set the Recognizer mock separately as done in the app's startup
        dispatcher.setRecognizer(mockRecognizer); 
        
        // Define a simple action for a fixed command
        fixedCommandExecuted = false;
        fixedCommandAction = () -> fixedCommandExecuted = true;
        
        // Create the map and register it
        initialCommandMap = new HashMap<>();
        initialCommandMap.put("test pause", fixedCommandAction);
        initialCommandMap.put("play music", fixedCommandAction);
        dispatcher.registerCommands(initialCommandMap);

        // Tell the mock Recognizer to not be in a paused state when dispatch() is called
        when(mockRecognizer.isListening()).thenReturn(true); 
    }

    // =======================================================
    // 1. Fixed Command Tests
    // =======================================================
    
    /**
     * Story: When I say "test pause", the dispatcher should run the corresponding action.
     */
    @Test
    void dispatch_executesRegisteredFixedCommand() {
        // ACT: Dispatch the command
        dispatcher.dispatch("Test Pause");
        
        // ASSERT 1: Check if the custom action (Runnable) was executed
        assertTrue(fixedCommandExecuted, "The fixed command's Runnable action should have been executed.");
        
        // ASSERT 2: Check if the GUI was updated (This is one of the fixed command's responsibilities)
        verify(mockGui, times(1)).updateStatus("test pause");

        // ASSERT 3: Check that no TTS (speech) or search methods were called, because the Recognizer handles the "Done" speech.
        verify(mockTts, never()).speak(anyString(), any());
        verify(mockSystemCommands, never()).searchweb(anyString());
    }

    /**
     * Story: When I say something unknown, the dispatcher should give a friendly response and pause the mic.
     */
    @Test
    void dispatch_handlesUnrecognizedCommand() {
        // ACT: Dispatch an unknown command
        String unknownCommand = "make coffee";
        dispatcher.dispatch(unknownCommand);
        
        // ASSERT 1: The custom action should NOT have been executed
        assertFalse(fixedCommandExecuted, "No fixed command action should be executed for an unknown command.");
        
        // ASSERT 2: The dispatcher must call TTS with a message
        // We capture the Runnable argument (the pause action) to check later
        ArgumentCaptor<String> ttsTextCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Runnable> pauseActionCaptor = ArgumentCaptor.forClass(Runnable.class);

        verify(mockTts, times(1)).speak(ttsTextCaptor.capture(), pauseActionCaptor.capture());
        
        // ASSERT 3: The captured pause action must correctly call recognizer.pause() when run
        Runnable capturedPauseAction = pauseActionCaptor.getValue();
        capturedPauseAction.run();
        verify(mockRecognizer, times(1)).pause();
    }
    
    // =======================================================
    // 2. Flexible Search Command Tests
    // =======================================================

    /**
     * Story: A command starting with "search" that is NOT about files should default to a web search.
     */
    @Test
    void dispatch_handlesWebSearchCommand() {
        // ARRANGE
        String webCommand = "search the highest mountain";
        String searchTerm = "the highest mountain"; // The part after "search "
        
        // ACT
        dispatcher.dispatch(webCommand);
        
        // ASSERT 1: The web search method should be called
        verify(mockSystemCommands, times(1)).searchweb(searchTerm);
        
        // ASSERT 2: The local file search method should NOT be called
        verify(mockSystemCommands, never()).searchLocalFiles(anyString());

        // ASSERT 3: TTS should be called to confirm the search and then pause the mic
        ArgumentCaptor<Runnable> pauseActionCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(mockTts, times(1)).speak(contains("Searching Google for"), pauseActionCaptor.capture());
        
        // Run the captured action to confirm it calls the recognizer's pause method
        pauseActionCaptor.getValue().run();
        verify(mockRecognizer, times(1)).pause();
    }

    /**
     * Story: A command with keywords like "file" or "computer" should trigger a local search.
     */
    @Test
    void dispatch_handlesLocalSearchCommand() {
        // ARRANGE
        String localCommand = "find my reports file on computer";
        String searchTerm = "my reports file on computer"; // The part after "find "
        
        // ACT
        dispatcher.dispatch(localCommand);
        
        // ASSERT 1: The local file search method should be called
        verify(mockSystemCommands, times(1)).searchLocalFiles(searchTerm);
        
        // ASSERT 2: The web search method should NOT be called
        verify(mockSystemCommands, never()).searchweb(anyString());

        // ASSERT 3: TTS should confirm file search and pause the mic
        verify(mockTts, times(1)).speak(contains("Searching your files for"), any());
    }

    // =======================================================
    // 3. Synchronization (Mic Restart) Test
    // =======================================================

    /**
     * Story: When an external skill calls speakResponse, the mic should restart listening 
     * (resume) *after* the TTS is done speaking.
     */
    @Test
    void speakResponse_resumesRecognizerAfterSpeech() {
        // ARRANGE: A message an external skill wants to say
        String skillResponse = "Your music is now playing.";
        
        // ACT: The skill calls the dispatcher's method
        dispatcher.speakResponse(skillResponse);
        
        // ASSERT 1: TTS speak method is called
        ArgumentCaptor<Runnable> resumeActionCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(mockTts, times(1)).speak(eq(skillResponse), resumeActionCaptor.capture());
        
        // ASSERT 2: We must check that the action passed to TTS is recognizer.resume()
        // We simulate the TTS engine finishing by running the captured Runnable
        Runnable capturedResumeAction = resumeActionCaptor.getValue();
        capturedResumeAction.run();
        
        // ASSERT 3: Check if the Recognizer's resume() method was called once
        verify(mockRecognizer, times(1)).resume();
    }
}