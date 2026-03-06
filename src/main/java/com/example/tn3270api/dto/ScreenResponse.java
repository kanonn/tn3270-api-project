package com.example.tn3270api.dto;

import java.util.List;

/**
 * Screen content response
 */
public class ScreenResponse {
    private List<String> screenLines;
    private String fullScreen;

    public ScreenResponse() {
    }

    public ScreenResponse(List<String> screenLines, String fullScreen) {
        this.screenLines = screenLines;
        this.fullScreen = fullScreen;
    }

    public List<String> getScreenLines() {
        return screenLines;
    }

    public void setScreenLines(List<String> screenLines) {
        this.screenLines = screenLines;
    }

    public String getFullScreen() {
        return fullScreen;
    }

    public void setFullScreen(String fullScreen) {
        this.fullScreen = fullScreen;
    }
}
