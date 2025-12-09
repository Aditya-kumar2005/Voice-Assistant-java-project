package com.friend.friend;

import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.scene.Node;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxAssert;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MergedEchoPilotApp using TestFX to manage the JavaFX thread.
 * * Note: Since MergedEchoPilotApp.start() launches a core application thread,
 * we must ensure tests don't interfere with that. For simplicity, we test 
 * the visible state changes after the FX component is ready.
 */
@ExtendWith(ApplicationExtension.class)
class MergedEchoPilotAppTest extends ApplicationTest {

    // The instance of the GUI class we are testing
    private MergedEchoPilotApp app;
    
    // Store the original primary stage reference.
    private Stage originalPrimaryStage; 

    /**
     * Start method for TestFX. Initializes the MergedEchoPilotApp instance.
     * @param stage The primary stage provided by the TestFX framework.
     */
    @Override
    public void start(Stage stage) throws Exception {
        this.originalPrimaryStage = stage; // 👈 Storing the Stage
        // Initialize the app and call its start method
        app = new MergedEchoPilotApp();
        app.start(stage); 
        // Wait for the UI components to be fully laid out before starting tests
        WaitForAsyncUtils.waitForFxEvents();
    }
    
    // --- Helper to find components by CSS ID or Text ---
    private Node findNodeByText(String text) {
        return lookup(text).query();
    }
    
    /**
     * Uses the simple inherited lookup() which should automatically search the 
     * active stage's scene graph.
     * @return The TextArea command area.
     */
    private TextArea getCommandArea() {
        // FIX: This lookup is now only called when the element is expected to be visible.
        return (TextArea) lookup("#commandArea").query(); 
    }
    
    // =======================================================
    // 1. State Management Tests (updateListeningIndicator)
    // =======================================================

    /**
     * Story: When listening is TRUE (active), the Pause button should be ENABLED
     * and the Resume button should be DISABLED. The loader should be visible.
     */
    @Test
    void updateListeningIndicator_whenActive_setsButtonStatesCorrectly() {
        // ARRANGE: Get the buttons
        Button pauseButton = (Button) findNodeByText("Pause");
        Button resumeButton = (Button) findNodeByText("Resume");
        
        // ACT: Set listening to TRUE
        interact(() -> app.updateListeningIndicator(true));
        WaitForAsyncUtils.waitForFxEvents();

        // ASSERT 1: Button states
        assertFalse(pauseButton.isDisable(), "Pause button should be enabled when listening.");
        assertTrue(resumeButton.isDisable(), "Resume button should be disabled when listening.");
        
        // ASSERT 2: Visual state (Loader visible, mic icon hidden)
        Node loaderContainer = lookup("#loaderContainer").query(); 
        assertTrue(loaderContainer.isVisible(), "Loader container should be visible when listening.");

        assertTrue(app.isListening, "Internal state flag should be true.");
    }
    
    /**
     * Story: When listening is FALSE (paused), the Pause button should be DISABLED
     * and the Resume button should be ENABLED. The mic icon should be visible.
     */
    @Test
    void updateListeningIndicator_whenPaused_setsButtonStatesCorrectly() {
        // ARRANGE: Get the buttons
        Button pauseButton = (Button) findNodeByText("Pause");
        Button resumeButton = (Button) findNodeByText("Resume");

        // ACT: Set listening to FALSE
        interact(() -> app.updateListeningIndicator(false));
        WaitForAsyncUtils.waitForFxEvents();

        // ASSERT 1: Button states
        assertTrue(pauseButton.isDisable(), "Pause button should be disabled when paused.");
        assertFalse(resumeButton.isDisable(), "Resume button should be enabled when paused.");
        
        // ASSERT 2: Visual state (Loader hidden, mic icon visible)
        Node loaderContainer = lookup("#loaderContainer").query(); 
        assertFalse(loaderContainer.isVisible(), "Loader container should be hidden when paused.");

        assertFalse(app.isListening, "Internal state flag should be false.");
    }

    // =======================================================
    // 2. Command Log Test (updateStatus)
    // =======================================================

    /**
     * Story: updateStatus must correctly append new text to the command area.
     */
    @Test
    void updateCommand_appendsTextCorrectly() {
        // Get the command area while it is visible
        TextArea commandArea = getCommandArea();
        
        // ACT 1: Send the first command (This checks the header logic)
        interact(() -> app.updateStatus("Hello, Friend!"));
        WaitForAsyncUtils.waitForFxEvents();
        
        // ASSERT 1: First command includes the header
        assertTrue(commandArea.getText().contains("Command History:\nHello, Friend!"), "First command should include the header.");
        
        // ACT 2: Send the second command
        interact(() -> app.updateStatus("How are you?"));
        WaitForAsyncUtils.waitForFxEvents();
        
        // ASSERT 2: Second command is appended on a new line
        assertTrue(commandArea.getText().contains("Hello, Friend!\nHow are you?"), "Second command should be appended with a newline.");
    }
    
    // =======================================================
    // 3. View Switch Test (Full View)
    // =======================================================
    
    /**
     * Story: The application should be initially in Full View.
     */
    @Test
    void initialView_isFullView() {
        // ASSERT: We check for the presence of a key element that is only in the Full View.
        FxAssert.verifyThat(lookup("Stop"), NodeMatchers.isVisible());
        assertTrue(app.getRootNode().isVisible());
    }
    
    /**
     * Story: The switch to Mini View should hide the primary stage.
     */
    @Test
    void switchToMiniView_hidesPrimaryStage() {
        // ARRANGE: Get the command area reference BEFORE the stage is hidden.
        TextArea commandArea = getCommandArea();
        
        // ACT: Click the button that triggers the view switch
        clickOn("Mini");
        
        // ASSERT 1: The original primary stage should now be hidden
        assertFalse(originalPrimaryStage.isShowing(), "Primary stage should be hidden after switching to Mini View.");
        
        // ASSERT 2: The command area, which is only on the primary stage, should be hidden
        // We use the reference we grabbed earlier.
        assertFalse(commandArea.isVisible(), "Command area should be hidden because it is part of the hidden primary stage.");
    }
}