package com.example.tn3270api.command;

import com.github.filipesimoes.j3270.Command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Plain Ascii() command for reading the full 3270 screen.
 *
 * Implements Command directly (NOT AbstractCommand) because
 * AbstractCommand.DATA_PREFIX = " data:" (leading space)
 * but s3270 on Linux outputs "data: " (no leading space).
 *
 * Includes detailed debug logging to diagnose s3270 communication issues.
 */
public class AsciiCommand implements Command<List<String>> {

    @Override
    public List<String> execute(Writer writer, BufferedReader reader) {
        List<String> lines = new ArrayList<>();

        try {
            // Send command
            writer.write("Ascii()\n");
            writer.flush();
            System.out.println("[AsciiCommand] Sent: Ascii()");

            // Read ALL response lines until we see "ok" or "error"
            int lineCount = 0;
            String line;

            while ((line = reader.readLine()) != null) {
                lineCount++;
                System.out.println("[AsciiCommand] Line " + lineCount + ": [" + line + "]");

                if (line.equals("ok")) {
                    System.out.println("[AsciiCommand] Got 'ok', total data lines: " + lines.size());
                    break;
                } else if (line.equals("error")) {
                    System.err.println("[AsciiCommand] Got 'error', total data lines: " + lines.size());
                    break;
                } else if (line.startsWith("data: ")) {
                    // s3270 format: "data: content"
                    lines.add(line.substring(6));
                } else if (line.startsWith(" data:")) {
                    // ws3270/j3270 format: " data:content"
                    lines.add(line.substring(6));
                } else {
                    // Status line or other output — skip
                    System.out.println("[AsciiCommand] (status/other line, skipping)");
                }
            }

        } catch (IOException e) {
            System.err.println("[AsciiCommand] IO error: " + e.getMessage());
        }

        return lines;
    }
}
