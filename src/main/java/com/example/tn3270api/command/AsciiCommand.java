package com.example.tn3270api.command;

import com.github.filipesimoes.j3270.command.AbstractCommand;

import java.util.ArrayList;
import java.util.List;

/**
 * Plain Ascii() command with no parameters.
 * Reads the entire screen content (all 24 rows x 80 cols).
 *
 * j3270 only provides AsciiRCLCommand and AsciiRCRCCommand (with coordinates),
 * but s3270's plain "Ascii()" (no args) is the most reliable way to read
 * the full screen. This class fills that gap.
 *
 * Uses j3270's AbstractCommand base class, so it goes through
 * Commander's proper writer/reader pipeline — no reflection, no socket conflict.
 */
public class AsciiCommand extends AbstractCommand<List<String>> {

    private List<String> lines;

    @Override
    protected void processData(String data) {
        if (lines == null) {
            lines = new ArrayList<>();
        }
        lines.add(data);
    }

    @Override
    protected List<String> getOutput() {
        return lines;
    }

    @Override
    protected String getCommand() {
        return "Ascii()";
    }
}
