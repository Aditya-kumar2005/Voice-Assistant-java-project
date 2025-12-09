package com.friend.friend;


import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class ProcessRunner {
    private ProcessRunner() {}

    public static int run(List<String> command, long timeoutSeconds) throws IOException, InterruptedException {
        return run(command, timeoutSeconds, null, null);
    }

    public static int run(String command, long timeoutSeconds) throws IOException, InterruptedException {
        // Convenience wrapper: run a single string command through cmd /c on Windows
        return run(List.of("cmd", "/c", command), timeoutSeconds, null, null);
    }

    public static int run(List<String> command, long timeoutSeconds, File workingDir, Map<String, String> environment) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        if (workingDir != null) pb.directory(workingDir);
        if (environment != null && !environment.isEmpty()) {
            Map<String, String> procEnv = pb.environment();
            procEnv.putAll(environment);
        }

        Process p = pb.start();
        boolean finished;
        if (timeoutSeconds <= 0) {
            p.waitFor();
            finished = true;
        } else {
            finished = p.waitFor(timeoutSeconds, TimeUnit.SECONDS);
        }

        if (!finished) {
            p.destroyForcibly();
            throw new IOException("Process timed out");
        }
        return p.exitValue();
    }
}