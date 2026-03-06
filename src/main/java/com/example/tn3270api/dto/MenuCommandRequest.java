package com.example.tn3270api.dto;

/**
 * Menu command request (with position parameters)
 */
public class MenuCommandRequest {
    private int row;        // row number
    private int column;     // column number
    private String command; // string to input

    public MenuCommandRequest() {
    }

    public MenuCommandRequest(int row, int column, String command) {
        this.row = row;
        this.column = column;
        this.command = command;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}