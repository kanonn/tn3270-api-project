package com.example.tn3270api.emulator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.tn3270api.service.Tn3270Service;

/**
 * Custom 3270 emulator runner with charset support.
 *
 * The original j3270 Emulator3270Runner does not support the -charset parameter,
 * which is required for Japanese mainframe screens (EBCDIC → ASCII conversion).
 *
 * This runner adds: s3270 -scriptport localhost:PORT -model MODEL -charset CHARSET
 */
public class CustomEmulatorRunner implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(CustomEmulatorRunner.class);

    private int scriptPort;
    // private String model = "3278-5-E";
    private String model = "5";
    private String charset = "cp930"; // e.g. "japanese-kana"

    private Process process = null;
    private boolean started = false;

    public CustomEmulatorRunner(int scriptPort) {
        this.scriptPort = scriptPort;
    }

    public CustomEmulatorRunner(int scriptPort, String charset) {
        this.scriptPort = scriptPort;
        this.charset = charset;
    }

    @Override
    public void run() {
        // Moved to the first line to ensure visibility in CloudWatch
        logger.info("CustomEmulatorRunner thread started. Port: {}", scriptPort);

        try {
            String executable = getExecutable();

            // Wrapped in try-catch to prevent silent thread death
            try {
                if (!isExecutablePresent(executable)) {
                    logger.error("Binary not found: {}", executable);
                    return;
                }
            } catch (Exception e) {
                logger.error("Error checking executable: ", e);
                return;
            }

            List<String> args = buildArgs(executable);

            // Crucial for Model 5 (27x132) support in Linux s3270
            if (!args.contains("-extended")) {
                args.add("-extended");
            }

            logger.info("Executing command: {}", String.join(" ", args));

            ProcessBuilder pb = new ProcessBuilder(args);
            pb.redirectErrorStream(true);

            process = pb.start();
            started = true;

            // Capture s3270 internal output (e.g., "Invalid model")
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.info("[s3270-shell] {}", line);
                }
            }

            int exitCode = process.waitFor();
            logger.info("s3270 process exited with code: {}", exitCode);

        } catch (Throwable t) {
            logger.error("Fatal error in emulator thread: ", t);
        } finally {
            stopNow();
        }
    }

    private List<String> buildArgs(String executable) {
        List<String> args = new ArrayList<>();

        args.add(executable);
        args.add("-scriptport");
        args.add("localhost:" + scriptPort);
        args.add("-model");
        args.add(model);

        // Add charset for Japanese support
        if (charset != null && !charset.isEmpty()) {
            args.add("-codepage");
            args.add(charset);
            args.add("-utf8");
        }

        return args;
    }

    private boolean isExecutablePresent(String executable) {
        try {
            ProcessBuilder pb = new ProcessBuilder(isWindows() ? "where" : "which", executable);
            Process proc = pb.start();
            int resultCode = proc.waitFor();
            return resultCode == 0;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String getExecutable() {
        if (isWindows()) {
            return "ws3270";
        } else {
            return "s3270";
        }
    }

    private boolean isWindows() {
        String osName = System.getProperty("os.name", "generic");
        return osName.toLowerCase().contains("windows");
    }

    public void stopNow() {
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
        }
    }

    public void stop() {
        if (process != null && process.isAlive()) {
            process.destroy();
        }
    }

    public boolean isStarted() {
        return started;
    }

    // --- Getters/Setters ---

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }
}
