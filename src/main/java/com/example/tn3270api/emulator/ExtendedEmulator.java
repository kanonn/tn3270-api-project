package com.example.tn3270api.emulator;

import com.example.tn3270api.command.AsciiCommand;
import com.github.filipesimoes.j3270.Command;
import com.github.filipesimoes.j3270.Emulator;
import com.github.filipesimoes.j3270.command.MoveCursorCommand;
import com.github.filipesimoes.j3270.command.SendKeysCommand;
import com.github.filipesimoes.j3270.command.WaitCommand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Extended 3270 emulator with reliable screen reading.
 *
 * NO REFLECTION. All commands go through j3270's proper
 * Commander → Writer/Reader pipeline.
 *
 * Uses custom AsciiCommand to send plain "Ascii()" (no params)
 * for full-screen reading. This is the same command that worked
 * in the original reflection-based version, but now routed
 * through j3270's own communication channel.
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
     * Call ONLY after Enter / function keys (host responds with new screen).
     * Do NOT call after fillField / sendString (local buffer only, no host response).
     */
    public void waitForOutput(int timeoutSeconds) {
        try {
            execute(new WaitCommand(timeoutSeconds, "Output"));
        } catch (Exception e) {
            System.err.println("Wait(Output) failed: " + e.getMessage());
        }
    }

    public void waitForOutput() {
        waitForOutput(10);
    }

    /**
     * Get full screen content (24 rows x 80 cols).
     *
     * Sends plain "Ascii()" through j3270's command pipeline.
     * Returns the current s3270 screen buffer content.
     *
     * - After fillField/sendString: your typed text will appear
     * - After Enter/PF key: caller should call waitForOutput() first
     */
    public List<String> getScreenLines() throws IOException {
        // Brief pause for s3270 internal processing
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<String> lines;

        try {
            // Execute plain Ascii() through j3270's pipeline
            List<String> result = execute(new AsciiCommand());
            lines = (result != null) ? new ArrayList<>(result) : new ArrayList<>();
        } catch (Exception e) {
            System.err.println("getScreenLines error: " + e.getMessage());
            lines = new ArrayList<>();
        }

        // Return all lines (screen may be 24 or 43 rows depending on model)
        // Do NOT truncate — the frontend will handle display
        return lines;
    }

    public <V> V executeCommand(Command<V> command) {
        return execute(command);
    }

    public void sendKey(String keyName) {
        execute(new SendKeysCommand(keyName));
    }

    public void sendString(String text) {
        execute(new com.github.filipesimoes.j3270.command.SendStringCommand(text));
    }

    /**
     * Move cursor to position and type text.
     * Unlike fillField(), this does NOT send DeleteField,
     * which can corrupt field structures on some mainframe screens.
     * This matches the behavior of typing in a 3270 terminal client.
     */
    public void moveCursorAndType(int row, int col, String text) {
        execute(new MoveCursorCommand(row, col));
        execute(new com.github.filipesimoes.j3270.command.SendStringCommand(text));
    }
}
