package com.example.tn3270api.emulator;

import com.github.filipesimoes.j3270.Command;
import com.github.filipesimoes.j3270.Emulator;
import com.github.filipesimoes.j3270.command.SendKeysCommand;

import java.io.*;
import java.lang.reflect.Field;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Extended 3270 emulator with reliable screen reading.
 *
 * Key improvements over base Emulator:
 * - Caches socket reader/writer (no re-creation on each call)
 * - Sends Wait(Output) before Ascii() to ensure screen stability
 * - Retry logic for transient read failures
 * - Properly drains s3270 response until "ok" marker
 */
public class ExtendedEmulator extends Emulator {

    private int scriptPort;

    // Cached socket I/O — initialized once, reused for all commands
    private BufferedReader cachedReader;
    private PrintWriter cachedWriter;
    private boolean ioInitialized = false;

    public ExtendedEmulator() {
        super();
        this.scriptPort = 3270;
    }

    public ExtendedEmulator(int scriptPort) {
        super(scriptPort);
        this.scriptPort = scriptPort;
    }

    /**
     * Initialize (or retrieve cached) socket reader and writer via reflection.
     * The reader/writer are cached so that the BufferedReader's internal buffer
     * doesn't consume data meant for subsequent reads.
     */
    private synchronized void initIO() throws Exception {
        if (ioInitialized && cachedReader != null && cachedWriter != null) {
            return;
        }

        // Access commander via reflection
        Field commanderField = Emulator.class.getDeclaredField("commander");
        commanderField.setAccessible(true);
        Object commander = commanderField.get(this);

        // Get socket
        Field socketField = commander.getClass().getDeclaredField("socket");
        socketField.setAccessible(true);
        Socket socket = (Socket) socketField.get(commander);

        if (socket == null || !socket.isConnected()) {
            throw new IOException("Socket not connected");
        }

        // Set socket read timeout to prevent infinite blocking
        socket.setSoTimeout(10000); // 10 seconds

        cachedReader = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), "ASCII")
        );
        cachedWriter = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), "ASCII"),
                true
        );
        ioInitialized = true;
    }

    /**
     * Send a raw command to s3270 and read the response lines.
     * Reads until "ok" or "error" marker is found.
     *
     * @param command The s3270 scripting command (e.g. "Ascii()", "Wait(Output)")
     * @return list of data lines (with "data: " prefix stripped), or empty on error
     */
    private synchronized List<String> sendRawCommand(String command) throws Exception {
        initIO();

        List<String> dataLines = new ArrayList<>();

        cachedWriter.println(command);
        cachedWriter.flush();

        String line;
        while ((line = cachedReader.readLine()) != null) {
            if (line.equals("ok")) {
                break;
            } else if (line.startsWith("error")) {
                System.err.println("s3270 command [" + command + "] returned: " + line);
                return dataLines; // Return whatever we got so far
            } else if (line.startsWith("data: ")) {
                dataLines.add(line.substring(6));
            }
            // Skip any other lines (e.g. status lines)
        }

        return dataLines;
    }

    /**
     * Wait for screen output to stabilize.
     * Sends Wait(Output) which blocks until s3270 receives data from host.
     */
    private void waitForOutput() {
        try {
            sendRawCommand("Wait(Output)");
        } catch (Exception e) {
            System.err.println("Wait(Output) failed: " + e.getMessage());
        }
    }

    /**
     * Get full screen content (24 rows x 80 cols).
     *
     * Sends Wait(Output) first to ensure screen is stable,
     * then reads via Ascii() command. Retries once on failure.
     */
    public List<String> getScreenLines() throws IOException {
        // Brief pause to let any pending screen update arrive
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Attempt with retry
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                // Wait for screen to be stable before reading
                waitForOutput();

                // Read screen content
                List<String> lines = sendRawCommand("Ascii()");

                // Validate: should have exactly 24 rows for a standard 3270 screen
                if (lines.size() >= 24) {
                    return lines.subList(0, 24);
                }

                // If we got some lines but less than 24, pad with empty lines
                if (!lines.isEmpty()) {
                    while (lines.size() < 24) {
                        lines.add(String.format("%-80s", ""));
                    }
                    return lines;
                }

                // Got zero lines — retry
                if (attempt < 2) {
                    System.err.println("getScreenLines: got 0 lines, retrying (attempt " + attempt + ")...");
                    Thread.sleep(1000);
                }

            } catch (Exception e) {
                System.err.println("getScreenLines attempt " + attempt + " failed: " + e.getMessage());
                if (attempt < 2) {
                    // Reset IO cache and retry
                    ioInitialized = false;
                    cachedReader = null;
                    cachedWriter = null;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        // Final fallback: return 24 empty lines
        System.err.println("getScreenLines: all attempts failed, returning empty screen");
        List<String> empty = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            empty.add(String.format("%-80s", ""));
        }
        return empty;
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
