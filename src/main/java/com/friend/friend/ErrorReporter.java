package com.friend.friend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * ErrorReporter captures and persists error information for debugging and diagnostics.
 * Supports opt-in telemetry reporting.
 */
public class ErrorReporter {
    private static final Logger logger = LoggerFactory.getLogger(ErrorReporter.class);
    private static final String LOG_DIR = "logs";
    private static final String ERROR_REPORT_FILE = "logs/error-report.log";
    private boolean enableTelemetry;

    public ErrorReporter(boolean enableTelemetry) {
        this.enableTelemetry = enableTelemetry;
        ensureLogDir();
    }

    public void setEnableTelemetry(boolean enable) {
        this.enableTelemetry = enable;
        logger.info("Error telemetry reporting " + (enable ? "enabled" : "disabled"));
    }

    /**
     * Report an exception with context information.
     */
    public void reportException(String context, Throwable ex) {
        logger.error("[" + context + "] Exception occurred", ex);

        if (enableTelemetry) {
            persistErrorReport(context, ex);
        }
    }

    /**
     * Persist error report to file for later analysis.
     */
    private void persistErrorReport(String context, Throwable ex) {
        try {
            ensureLogDir();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
            String errorEntry = String.format(
                "[%s] Context: %s\nException: %s\nMessage: %s\nStackTrace: %s\n---\n",
                timestamp,
                context,
                ex.getClass().getCanonicalName(),
                ex.getMessage(),
                formatStackTrace(ex)
            );

            try (FileWriter fw = new FileWriter(ERROR_REPORT_FILE, true)) {
                fw.write(errorEntry);
                fw.flush();
            }
        } catch (IOException ioEx) {
            logger.warn("Failed to persist error report", ioEx);
        }
    }

    /**
     * Format stack trace for logging.
     */
    private String formatStackTrace(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : ex.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        if (ex.getCause() != null) {
            sb.append("Caused by: ").append(ex.getCause().toString()).append("\n");
        }
        return sb.toString();
    }

    private void ensureLogDir() {
        File dir = new File(LOG_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Return the path to the error report file for manual inspection.
     */
    public String getErrorReportPath() {
        return ERROR_REPORT_FILE;
    }
}
