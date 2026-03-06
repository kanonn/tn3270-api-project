package com.example.tn3270api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * TN3270 configuration properties
 */
@Component
@ConfigurationProperties(prefix = "tn3270")
public class Tn3270Properties {
    
    private String host = "127.0.0.1";
    private int port = 3270;
    private int scriptPort = 4270;
    private String username = "HERC01";
    private String password = "CUL8TR";
    private long sessionTimeout = 300000; // 5 minutes

    // Getters and Setters
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getScriptPort() {
        return scriptPort;
    }

    public void setScriptPort(int scriptPort) {
        this.scriptPort = scriptPort;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(long sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }
}
