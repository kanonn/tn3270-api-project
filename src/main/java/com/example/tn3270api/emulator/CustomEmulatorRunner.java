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
        String executable = getExecutable();

        if (!isExecutablePresent(executable)) {
            throw new RuntimeException(executable + " not found.");
        }

        List<String> args = buildArgs(executable);
        logger.info("[CustomRunner] Starting: " + String.join(" ", args));

        ProcessBuilder pb = new ProcessBuilder(args);
        try {
            pb.inheritIO();
            process = pb.start();
            started = true;
            process.waitFor();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            // Nothing to do.
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
