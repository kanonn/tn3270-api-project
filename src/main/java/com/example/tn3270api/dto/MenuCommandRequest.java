package com.example.tn3270api.dto;

/**
 * 菜单命令请求（带位置参数）
 */
public class MenuCommandRequest {
    private int row;        // 行号
    private int column;     // 列号
    private String command; // 要输入的字符串

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