package com.example.tn3270api.service;

import com.example.tn3270api.config.Tn3270Properties;
import com.example.tn3270api.dto.ScreenResponse;
import com.example.tn3270api.emulator.ExtendedEmulator;
import com.example.tn3270api.model.Tn3270Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TN3270 Service
 */
@Service
public class Tn3270Service {

    private static final Logger logger = LoggerFactory.getLogger(Tn3270Service.class);

    @Autowired
    private Tn3270Properties properties;

    // Session management
    private final Map<String, Tn3270Session> sessions = new ConcurrentHashMap<>();

    /**
     * Login and establish connection (API1)
     */
    public String login() throws Exception {
        String sessionId = UUID.randomUUID().toString();
        logger.info("Creating new session: {}", sessionId);

        ExtendedEmulator emulator = new ExtendedEmulator(properties.getScriptPort());
        Tn3270Session session = new Tn3270Session(sessionId, emulator);

        try {
            // Start emulator
            logger.info("Starting 3270 emulator...");
            emulator.start();
            Thread.sleep(3000); // Wait for emulator to start

            // Connect to host
            String connectionString = properties.getHost() + ":" + properties.getPort();
            logger.info("Connecting to host: {}", connectionString);
            boolean connected = emulator.connect(connectionString);

            if (!connected) {
                throw new IOException("Failed to connect to host");
            }

            session.setConnected(true);
            logger.info("Connected successfully, waiting for host response...");

            // Wait for unlock
            logger.info("Waiting for keyboard unlock...");
            emulator.waitUnlock(30);
            Thread.sleep(5000); // Wait for screen data

            // Check for LOGON
            if (!screenContains(emulator, "LOGON")) {
                logger.warn("LOGON text not found, sending CLEAR and RESET...");
                emulator.sendKey("Clear");
                Thread.sleep(1000);
                emulator.sendKey("Reset");
                Thread.sleep(1000);
            }

            // Login sequence
            logger.info("Starting login sequence...");

            // Enter username
            logger.info("Entering username: {}", properties.getUsername());
            emulator.fillField(22, 13, properties.getUsername());
            Thread.sleep(1000);

            // Submit username
            logger.info("Submitting username");
            emulator.sendEnter();
            Thread.sleep(5000);

            try {
                emulator.waitUnlock(10);
            } catch (Exception e) {
                logger.error("waitUnlock failed", e);
            }

            Thread.sleep(2000);

            // Enter password
            logger.info("Entering password");
            emulator.fillField(10, 20, properties.getPassword());
            Thread.sleep(1000);

            // Submit password
            logger.info("Submitting password");
            emulator.sendEnter();
            Thread.sleep(3000);

            // Submit login credentials
            logger.info("Submitting login credentials");
            emulator.sendEnter();
            Thread.sleep(3000);

            // Enter main menu
            logger.info("Entering main menu");
            emulator.sendEnter();
            Thread.sleep(2000);

            // Save session
            sessions.put(sessionId, session);
            logger.info("Login successful, session saved: {}", sessionId);

            return sessionId;

        } catch (Exception e) {
            logger.error("Login failed", e);
            // Clean up resources
            try {
                emulator.disconnect();
                emulator.close();
            } catch (Exception ex) {
                logger.error("Failed to clean up resources", ex);
            }
            throw e;
        }
    }

    /**
     * Send menu command (API2) - no auto enter
     */
    public ScreenResponse sendMenuCommand(String sessionId, int row, int column, String command) throws Exception {
        Tn3270Session session = getSession(sessionId);
        ExtendedEmulator emulator = session.getEmulator();

        logger.info("Session {} entering command at [row:{}, col:{}]: {}", sessionId, row, column, command);

        // Enter command at specified position (no enter)
        emulator.fillField(row, column, command);
        Thread.sleep(1000);

        // Update access time
        session.updateAccessTime();

        // Return screen content
        return getScreen(sessionId);
    }

    /**
     * Send string (API8) - no position, input at current cursor
     */
    public ScreenResponse sendString(String sessionId, String text) throws Exception {
        Tn3270Session session = getSession(sessionId);
        ExtendedEmulator emulator = session.getEmulator();

        logger.info("Session {} entering string at current cursor: {}", sessionId, text);

        // Input string at current cursor position
        emulator.sendString(text);
        Thread.sleep(500);

        // Update access time
        session.updateAccessTime();

        // Return screen content
        return getScreen(sessionId);
    }

    /**
     * Send Enter key (API5)
     */
    public ScreenResponse sendEnter(String sessionId) throws Exception {
        Tn3270Session session = getSession(sessionId);
        ExtendedEmulator emulator = session.getEmulator();

        logger.info("Session {} sending Enter key", sessionId);

        // Press Enter
        emulator.sendEnter();
        Thread.sleep(2000);

        // Wait for unlock
        try {
            emulator.waitUnlock(10);
        } catch (Exception e) {
            logger.warn("waitUnlock timed out");
        }

        Thread.sleep(1000);

        // Update access time
        session.updateAccessTime();

        // Return screen content
        return getScreen(sessionId);
    }

    /**
     * Send Reset (API6)
     */
    public ScreenResponse sendReset(String sessionId) throws Exception {
        Tn3270Session session = getSession(sessionId);
        ExtendedEmulator emulator = session.getEmulator();

        logger.info("Session {} sending Reset", sessionId);

        // Send Reset key
        emulator.sendKey("Reset");
        Thread.sleep(1000);

        // Wait for unlock
        try {
            emulator.waitUnlock(10);
        } catch (Exception e) {
            logger.warn("waitUnlock timed out");
        }

        Thread.sleep(500);

        // Update access time
        session.updateAccessTime();

        // Return screen content
        return getScreen(sessionId);
    }

    /**
     * Send Tab key (API7)
     */
    public ScreenResponse sendTab(String sessionId) throws Exception {
        Tn3270Session session = getSession(sessionId);
        ExtendedEmulator emulator = session.getEmulator();

        logger.info("Session {} sending Tab", sessionId);

        // Send Tab key
        emulator.sendKey("Tab");
        Thread.sleep(500);

        // Update access time
        session.updateAccessTime();

        // Return screen content
        return getScreen(sessionId);
    }

    /**
     * Enter level-2 menu - dedicated (API4)
     * Fixed: inputs "2" at row 4 column 13
     * @deprecated Use sendMenuCommand(sessionId, 4, 13, "2") + sendEnter(sessionId) instead
     */
    @Deprecated
    public ScreenResponse enterSecondLevelMenu(String sessionId) throws Exception {
        logger.info("Session {} entering level-2 menu [using fixed position]", sessionId);
        sendMenuCommand(sessionId, 4, 13, "2");
        return sendEnter(sessionId);
    }

    /**
     * Execute LOGOFF (API3)
     */
    public ScreenResponse logoff(String sessionId) throws Exception {
        Tn3270Session session = getSession(sessionId);
        ExtendedEmulator emulator = session.getEmulator();

        logger.info("Session {} executing LOGOFF", sessionId);

        // Send LOGOFF
        emulator.sendString("LOGOFF");
        Thread.sleep(1000);

        emulator.sendEnter();
        Thread.sleep(3000);

        // Wait for unlock
        try {
            emulator.waitUnlock(10);
        } catch (Exception e) {
            logger.warn("waitUnlock timed out");
        }

        Thread.sleep(2000);

        // Get screen content
        ScreenResponse response = getScreen(sessionId);

        // Clean up session
        closeSession(sessionId);

        return response;
    }

    /**
     * Get screen content
     */
    public ScreenResponse getScreen(String sessionId) throws Exception {
        Tn3270Session session = getSession(sessionId);
        ExtendedEmulator emulator = session.getEmulator();

        List<String> lines = emulator.getScreenLines();

        StringBuilder fullScreen = new StringBuilder();
        int row = 1;
        for (String line : lines) {
            fullScreen.append(String.format("Row%02d: %s\n", row++, line));
        }

        session.updateAccessTime();

        return new ScreenResponse(lines, fullScreen.toString());
    }

    /**
     * Get session
     */
    private Tn3270Session getSession(String sessionId) throws Exception {
        Tn3270Session session = sessions.get(sessionId);
        if (session == null) {
            throw new Exception("Session not found or expired: " + sessionId);
        }
        return session;
    }

    /**
     * Close session
     */
    public void closeSession(String sessionId) {
        Tn3270Session session = sessions.remove(sessionId);
        if (session != null) {
            try {
                ExtendedEmulator emulator = session.getEmulator();
                emulator.disconnect();
                emulator.close();
                logger.info("Session {} closed", sessionId);
            } catch (Exception e) {
                logger.error("Failed to close session: {}", sessionId, e);
            }
        }
    }

    /**
     * Check if screen contains specified text
     */
    private boolean screenContains(ExtendedEmulator emulator, String text) {
        try {
            List<String> lines = emulator.getScreenLines();
            String searchText = text.toLowerCase();

            for (String line : lines) {
                if (line.toLowerCase().contains(searchText)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            logger.error("screenContains error", e);
            return false;
        }
    }

    /**
     * Initialize service
     */
    @PostConstruct
    public void init() {
        // Can add scheduled task to clean up expired sessions
        logger.info("TN3270 service initialized");
    }

    /**
     * Clean up all sessions on application shutdown
     */
    @PreDestroy
    public void cleanup() {
        logger.info("Cleaning up all sessions...");
        for (String sessionId : sessions.keySet()) {
            closeSession(sessionId);
        }
        logger.info("All sessions cleaned up");
    }
}
