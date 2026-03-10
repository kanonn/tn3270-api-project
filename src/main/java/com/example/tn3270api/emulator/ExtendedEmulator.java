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
 * CRITICAL: We must use Commander's own reader/writer for raw commands.
 * Creating a separate BufferedReader on the same socket InputStream
 * causes data corruption (two readers fighting over the same bytes).
 *
 * This class auto-detects Commander's internal field names via reflection.
 */
public class ExtendedEmulator extends Emulator {

    private int scriptPort;

    /** Cached references to Commander's reader/writer */
    private BufferedReader cmdReader;
    private PrintWriter cmdWriter;
    private boolean ioResolved = false;

    public ExtendedEmulator() {
        super();
        this.scriptPort = 3270;
    }

    public ExtendedEmulator(int scriptPort) {
        super(scriptPort);
        this.scriptPort = scriptPort;
    }

    /**
     * Resolve Commander's internal reader and writer via reflection.
     * Tries multiple possible field names, and if none match,
     * falls back to creating reader/writer from the socket.
     * Prints all Commander fields for debugging if detection fails.
     */
    private synchronized void resolveIO() throws Exception {
        if (ioResolved) return;

        // Step 1: Get Commander object
        Field commanderField = Emulator.class.getDeclaredField("commander");
        commanderField.setAccessible(true);
        Object commander = commanderField.get(this);

        if (commander == null) {
            throw new IOException("Commander is null — emulator not started?");
        }

        // Step 2: Log all Commander fields for debugging
        System.out.println("=== Commander class: " + commander.getClass().getName() + " ===");
        Field[] allFields = commander.getClass().getDeclaredFields();
        for (Field f : allFields) {
            f.setAccessible(true);
            Object val = f.get(commander);
            System.out.println("  Field: " + f.getName()
                + " | Type: " + f.getType().getSimpleName()
                + " | Value: " + (val == null ? "null" : val.getClass().getSimpleName()));
        }
        System.out.println("=== End Commander fields ===");

        // Step 3: Try to find reader (BufferedReader or similar)
        cmdReader = (BufferedReader) findField(commander,
            BufferedReader.class,
            "reader", "in", "bufferedReader", "br", "input");

        // Step 4: Try to find writer (PrintWriter or similar)
        cmdWriter = (PrintWriter) findField(commander,
            PrintWriter.class,
            "writer", "out", "printWriter", "pw", "output");

        // Step 5: If reader/writer not found, fall back to socket
        if (cmdReader == null || cmdWriter == null) {
            System.out.println("Commander reader/writer fields not found, falling back to socket...");

            Socket socket = (Socket) findField(commander, Socket.class, "socket", "sock", "s");

            if (socket == null || !socket.isConnected()) {
                throw new IOException("Cannot find usable socket in Commander");
            }

            // Create our own reader/writer from socket
            // WARNING: This may conflict with Commander's internal readers.
            // But it's our best fallback.
            if (cmdReader == null) {
                cmdReader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), "ASCII"));
                System.out.println("Created BufferedReader from socket (fallback)");
            }
            if (cmdWriter == null) {
                cmdWriter = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), "ASCII"), true);
                System.out.println("Created PrintWriter from socket (fallback)");
            }
        } else {
            System.out.println("Using Commander's own reader/writer (optimal)");
        }

        ioResolved = true;
    }

    /**
     * Find a field of a given type in the target object, trying multiple names.
     * Returns null if no matching field found.
     */
    private Object findField(Object target, Class<?> expectedType, String... names) {
        // First: try by name
        for (String name : names) {
            try {
                Field f = target.getClass().getDeclaredField(name);
                f.setAccessible(true);
                Object val = f.get(target);
                if (val != null && expectedType.isAssignableFrom(val.getClass())) {
                    System.out.println("  Found " + expectedType.getSimpleName()
                        + " in field '" + name + "'");
                    return val;
                }
            } catch (NoSuchFieldException e) {
                // try next name
            } catch (Exception e) {
                System.err.println("  Error reading field '" + name + "': " + e.getMessage());
            }
        }

        // Second: try by type (scan all fields)
        try {
            for (Field f : target.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                Object val = f.get(target);
                if (val != null && expectedType.isAssignableFrom(val.getClass())) {
                    System.out.println("  Found " + expectedType.getSimpleName()
                        + " in field '" + f.getName() + "' (by type scan)");
                    return val;
                }
            }
        } catch (Exception e) {
            System.err.println("  Type scan error: " + e.getMessage());
        }

        System.out.println("  Could not find " + expectedType.getSimpleName() + " in Commander");
        return null;
    }

    /**
     * Send a raw command to s3270 using Commander's reader/writer.
     */
    private synchronized List<String> sendRawCommand(String command) throws Exception {
        resolveIO();

        List<String> dataLines = new ArrayList<>();

        cmdWriter.println(command);
        cmdWriter.flush();

        String line;
        while ((line = cmdReader.readLine()) != null) {
            if (line.equals("ok")) {
                break;
            } else if (line.startsWith("error")) {
                System.err.println("s3270 [" + command + "] returned: " + line);
                break;
            } else if (line.startsWith("data: ")) {
                dataLines.add(line.substring(6));
            }
        }

        return dataLines;
    }

    /**
     * Wait for host to send new screen data.
     * Call ONLY after Enter/function keys, NOT after fillField/sendString.
     */
    public void waitForOutput() {
        try {
            sendRawCommand("Wait(Output)");
        } catch (Exception e) {
            System.err.println("Wait(Output) failed: " + e.getMessage());
        }
    }

    /**
     * Get full screen content (24 rows x 80 cols).
     *
     * Reads s3270's current buffer via Ascii(). Does NOT wait for host output.
     * After fillField: the text you entered will appear in the result.
     * After Enter/PF: caller should call waitForOutput() first.
     */
    public List<String> getScreenLines() throws IOException {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<String> lines;
        try {
            lines = sendRawCommand("Ascii()");
        } catch (Exception e) {
            System.err.println("getScreenLines failed: " + e.getMessage());
            lines = new ArrayList<>();
        }

        // Pad to exactly 24 rows
        while (lines.size() < 24) {
            lines.add(String.format("%-80s", ""));
        }
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

    public void sendString(String text) {
        execute(new com.github.filipesimoes.j3270.command.SendStringCommand(text));
    }
}
