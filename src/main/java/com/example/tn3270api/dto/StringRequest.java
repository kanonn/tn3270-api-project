package com.example.tn3270api.dto;

/**
 * Simple string request (no position specified)
 */
public class StringRequest {
    private String text;

    public StringRequest() {
    }

    public StringRequest(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
