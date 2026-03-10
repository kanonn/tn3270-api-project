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
 * IMPORTANT: This does NOT extend AbstractCommand because AbstractCommand
 * uses DATA_PREFIX = " data:" (with leading space), but s3270 on Linux
 * actually outputs "data: " (without leading space). This mismatch causes
 * AbstractCommand to never recognize data lines, returning null.
 *
 * Instead, this implements Command directly and parses s3270 output
 * with the correct prefix "data: ".
 *
 * s3270 Ascii() response format:
 *   data: line1 content (80 chars)
 *   data: line2 content (80 chars)
 *   ... (24 lines total)
 *   U F U C(hostname) I 4 24 80 14 12 0x0 0.000    ← status line
 *   ok                                               ← result line
 */
public class AsciiCommand implements Command<List<String>> {

    @Override
    public List<String> execute(Writer writer, BufferedReader reader) {
        List<String> lines = new ArrayList<>();

        try {
            // Send Ascii() command
            writer.write("Ascii()\n");
            writer.flush();

            // Read response
            boolean statusFound = false;
            boolean finished = false;

            while (!finished) {
                String line = reader.readLine();

                if (line == null) {
                    break;
                }

                if (line.startsWith("data: ")) {
                    // s3270 data line: strip "data: " prefix
                    lines.add(line.substring(6));
                } else if (line.startsWith(" data:")) {
                    // j3270/ws3270 data line: strip " data:" prefix
                    lines.add(line.substring(6));
                } else if (!statusFound) {
                    // First non-data line = status line (skip it)
                    statusFound = true;
                } else {
                    // Second non-data line = result ("ok" or "error")
                    if (line.startsWith("error")) {
                        System.err.println("Ascii() command error: " + line);
                    }
                    finished = true;
                }
            }

        } catch (IOException e) {
            System.err.println("AsciiCommand IO error: " + e.getMessage());
        }

        return lines;
    }
}
