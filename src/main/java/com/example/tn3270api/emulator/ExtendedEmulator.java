package com.example.tn3270api.emulator;

import com.github.filipesimoes.j3270.Command;
import com.github.filipesimoes.j3270.Emulator;
import com.github.filipesimoes.j3270.command.SendKeysCommand;
import com.github.filipesimoes.j3270.command.WaitCommand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Extended 3270 emulator with reliable screen reading.
 *
 * Uses j3270's BUILT-IN getText() method for screen reading.
 * No reflection needed — this goes through j3270's proper
 * Commander → Writer/Reader pipeline.
 *
 * Previous versions used reflection to access Commander's socket
 * and created separate BufferedReader/PrintWriter instances,
 * which caused data corruption (two readers on the same stream).
 */
public class ExtendedEmulator extends Emulator {

    private int scriptPort;

    public ExtendedEmulator() {
        super();
        this.scriptPort = 3270;
    }

    public ExtendedEmulator(int scriptPort) {
        super(scriptPort);
        this.scriptPort = scriptPort;
    }

    /**
     * Wait for host to send new screen data.
     * Uses j3270's built-in WaitCommand("Output").
     *
     * Call this ONLY after operations that trigger a host response
     * (Enter, function keys), NOT after local-only ops (fillField, sendString).
     *
     * @param timeoutSeconds max seconds to wait (0 = no timeout)
     */
    public void waitForOutput(int timeoutSeconds) {
        try {
            execute(new WaitCommand(timeoutSeconds, "Output"));
        } catch (Exception e) {
            System.err.println("Wait(Output) failed: " + e.getMessage());
        }
    }

    /**
     * Wait for host output with default 10 second timeout.
     */
    public void waitForOutput() {
        waitForOutput(10);
    }

    /**
     * Get full screen content (24 rows x 80 cols).
     *
     * Uses j3270's built-in getText(row1, col1, row2, col2) method
     * which internally uses AsciiRCRCCommand → "Ascii(0,0,24,80)".
     *
     * This goes through Commander's proper writer/reader pipeline,
     * so there's no socket conflict.
     *
     * After fillField/sendString: the text you typed will appear.
     * After Enter/PF key: caller should call waitForOutput() first.
     */
    public List<String> getScreenLines() throws IOException {
        // Brief pause to let s3270 process pending commands
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<String> lines = new ArrayList<>();

        try {
            // Use j3270's built-in getText to read full 24x80 screen
            // getText(row1, col1, row2, col2) → AsciiRCRCCommand
            // Parameters: start at (0,0), end at (24,80)
            // This generates: Ascii(0, 0, 24, 80) = 24 rows, 80 cols
            List<String> result = getText(0, 0, 24, 80);

            if (result != null && !result.isEmpty()) {
                lines.addAll(result);
            }
        } catch (Exception e) {
            System.err.println("getScreenLines error: " + e.getMessage());
        }

        // Pad to exactly 24 rows
        while (lines.size() < 24) {
            lines.add(String.format("%-80s", ""));
        }

        // Limit to 24 rows
        if (lines.size() > 24) {
            lines = new ArrayList<>(lines.subList(0, 24));
        }

        return lines;
    }

    public <V> V executeCommand(Command<V> command) {
        return execute(command);
    }

    public void sendKey(String keyName) {
        execute(new SendKeysCommand(keyName));
    }

    /**
     * Send string at current cursor position
     */
    public void sendString(String text) {
        execute(new com.github.filipesimoes.j3270.command.SendStringCommand(text));
    }
}
