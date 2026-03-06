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
 * Extended 3270 emulator
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
     * Get full screen content (using Ascii command), limited to 24 rows
     */
    public List<String> getScreenLines() throws IOException {
        // Wait briefly for screen to stabilize
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<String> lines = new ArrayList<>();

        try {
            // Access commander via reflection
            Field commanderField = Emulator.class.getDeclaredField("commander");
            commanderField.setAccessible(true);
            Object commander = commanderField.get(this);

            // Get socket reader and writer
            Field socketField = commander.getClass().getDeclaredField("socket");
            socketField.setAccessible(true);
            Socket socket = (Socket) socketField.get(commander);

            if (socket == null || !socket.isConnected()) {
                throw new IOException("Socket not connected");
            }

            // Send Ascii() command
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), "ASCII")
            );
            PrintWriter writer = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), "ASCII"),
                    true
            );

            writer.println("Ascii()");
            writer.flush();

            // Read response, up to 24 rows
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals("ok")) {
                    break;
                } else if (line.startsWith("error")) {
                    System.err.println("Ascii command returned error, screen may be updating");
                    return lines;
                } else if (line.startsWith("data: ")) {
                    // Strip "data: " prefix
                    lines.add(line.substring(6));

                    // Limit to 24 rows
                    if (lines.size() >= 24) {
                        break;
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("getScreenLines exception: " + e.getMessage());
            return lines;
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
     * Send string
     */
    public void sendString(String text) {
        execute(new com.github.filipesimoes.j3270.command.SendStringCommand(text));
    }
}
