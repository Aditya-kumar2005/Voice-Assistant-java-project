package com.friend.friend;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Friend application startup class.
 * We are checking if the setup methods run as expected, like checking if a teddy bear
 * sits up straight after you wind it up!
 */
class FriendTest {

    // This is the path to the 'log file box' we check in the clearLogsOnStartup() method.
    private static final String LOG_DIRECTORY_PATH = "./logs/";
    private static final String DUMMY_LOG_FILE = LOG_DIRECTORY_PATH + "test-log.txt";

    /**
     * Story Step 1: Before each test, we create a temporary log file.
     * This ensures the log directory exists and has something to delete.
     */
    @BeforeEach
    void setUp() throws IOException {
        // Ensure the directory exists
        new File(LOG_DIRECTORY_PATH).mkdirs(); 
        // Create a dummy file that clearLogsOnStartup should find and delete
        assertTrue(new File(DUMMY_LOG_FILE).createNewFile(), "Should successfully create a dummy log file before the test.");
    }

    /**
     * Story Step 2: After each test, we clean up the logs folder completely.
     */
    @AfterEach
    void tearDown() {
        File dir = new File(LOG_DIRECTORY_PATH);
        if (dir.exists()) {
            for (File file : dir.listFiles()) {
                file.delete(); // Delete all files
            }
            dir.delete(); // Delete the folder itself
        }
    }

    /**
     * Test case for the clearLogsOnStartup method.
     * We check if our dummy log file is gone after the method runs.
     */
    @Test
    void clearLogsOnStartup_deletesExistingLogs() {
        // ACT: Run the method we are testing (the log clearing story)
        Friend.clearLogsOnStartup();
        
        // ASSERT: Check that the dummy file no longer exists.
        File dummyFile = new File(DUMMY_LOG_FILE);
        assertFalse(dummyFile.exists(), "The dummy log file should be deleted after running clearLogsOnStartup.");
        
        // ASSERT: Check that the log directory still exists (it shouldn't be deleted)
        File logDir = new File(LOG_DIRECTORY_PATH);
        assertTrue(logDir.exists() && logDir.isDirectory(), "The log directory should still exist after cleanup.");
    }

    /**
     * This test checks the main application entry point (initializeAndStartBrain).
     * Since this method starts threads, we only check that it doesn't immediately crash.
     * NOTE: A true test of threading requires more advanced tools, but this is a good start.
     */
    @Test
    void initializeAndStartBrain_doesNotThrowException() {
        // ARRANGE
        String[] args = {}; // Empty arguments array
        
        // ACT & ASSERT: We use assertDoesNotThrow to ensure the method starts successfully.
        assertDoesNotThrow(() -> {
            // We call the method and wait a short time for the threads to start.
            Friend.initializeAndStartBrain(args);
            Thread.sleep(100); // Give threads a moment to initialize before test finishes
        }, "initializeAndStartBrain should not throw any immediate exceptions during startup.");
    }
}