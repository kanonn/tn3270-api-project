package com.example.tn3270api.model;

import com.example.tn3270api.emulator.ExtendedEmulator;

/**
 * TN3270 会话
 */
public class Tn3270Session {
    
    private String sessionId;
    private ExtendedEmulator emulator;
    private long lastAccessTime;
    private boolean connected;
    
    public Tn3270Session(String sessionId, ExtendedEmulator emulator) {
        this.sessionId = sessionId;
        this.emulator = emulator;
        this.lastAccessTime = System.currentTimeMillis();
        this.connected = false;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public ExtendedEmulator getEmulator() {
        return emulator;
    }

    public void setEmulator(ExtendedEmulator emulator) {
        this.emulator = emulator;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(long lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public void updateAccessTime() {
        this.lastAccessTime = System.currentTimeMillis();
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }
}
