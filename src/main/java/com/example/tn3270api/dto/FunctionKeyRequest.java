package com.example.tn3270api.dto;

/**
 * Function key request (PF1-PF24, PA1-PA3, Clear, etc.)
 */
public class FunctionKeyRequest {
    private String keyName; // e.g. "PF2", "PF3", "PA1", "Clear"

    public FunctionKeyRequest() {
    }

    public FunctionKeyRequest(String keyName) {
        this.keyName = keyName;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }
}
