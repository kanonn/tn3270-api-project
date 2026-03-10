package com.example.tn3270api.controller;

import com.example.tn3270api.dto.ApiResponse;
import com.example.tn3270api.dto.FunctionKeyRequest;
import com.example.tn3270api.dto.MenuCommandRequest;
import com.example.tn3270api.dto.StringRequest;
import com.example.tn3270api.dto.ScreenResponse;
import com.example.tn3270api.service.Tn3270Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * TN3270 REST API Controller
 */
@RestController
@RequestMapping("/api/tn3270")
public class Tn3270Controller {

    private static final Logger logger = LoggerFactory.getLogger(Tn3270Controller.class);

    @Autowired
    private Tn3270Service tn3270Service;

    /**
     * API10: Connect to mainframe (no auto login)
     * POST /api/tn3270/connect
     * Returns session ID and initial screen
     */
    @PostMapping("/connect")
    public ApiResponse<ScreenResponse> connect() {
        try {
            logger.info("Received connect request");
            String sessionId = tn3270Service.connect();
            ScreenResponse screen = tn3270Service.getScreen(sessionId);

            ApiResponse<ScreenResponse> response = ApiResponse.success("Connection established", screen);
            response.setSessionId(sessionId);

            logger.info("Connected successfully, session ID: {}", sessionId);
            return response;

        } catch (Exception e) {
            logger.error("Connection failed", e);
            return ApiResponse.error("Connection failed: " + e.getMessage());
        }
    }

    /**
     * API1: Login to TSO main menu
     * POST /api/tn3270/login
     */
    @PostMapping("/login")
    public ApiResponse<ScreenResponse> login() {
        try {
            logger.info("Received login request");
            String sessionId = tn3270Service.login();
            ScreenResponse screen = tn3270Service.getScreen(sessionId);

            ApiResponse<ScreenResponse> response = ApiResponse.success("Login successful", screen);
            response.setSessionId(sessionId);

            logger.info("Login successful, session ID: {}", sessionId);
            return response;

        } catch (Exception e) {
            logger.error("Login failed", e);
            return ApiResponse.error("Login failed: " + e.getMessage());
        }
    }

    /**
     * API2: Send menu command (configurable position, no auto enter)
     * POST /api/tn3270/menu/command
     * Body: { "row": 4, "column": 13, "command": "2" }
     * Header: X-Session-Id: {sessionId}
     */
    @PostMapping("/menu/command")
    public ApiResponse<ScreenResponse> sendMenuCommand(
            @RequestHeader("X-Session-Id") String sessionId,
            @RequestBody MenuCommandRequest request) {
        try {
            logger.info("Session {} received menu command: row={}, col={}, command={}",
                sessionId, request.getRow(), request.getColumn(), request.getCommand());

            ScreenResponse screen = tn3270Service.sendMenuCommand(
                sessionId,
                request.getRow(),
                request.getColumn(),
                request.getCommand()
            );

            ApiResponse<ScreenResponse> response = ApiResponse.success("Command input successful", screen);
            response.setSessionId(sessionId);

            return response;

        } catch (Exception e) {
            logger.error("Command input failed", e);
            return ApiResponse.error("Command input failed: " + e.getMessage());
        }
    }

    /**
     * API8: Send string (no position specified)
     * POST /api/tn3270/send/string
     * Body: { "text": "Hello" }
     * Header: X-Session-Id: {sessionId}
     *
     * Inputs the string at the current cursor position
     */
    @PostMapping("/send/string")
    public ApiResponse<ScreenResponse> sendString(
            @RequestHeader("X-Session-Id") String sessionId,
            @RequestBody StringRequest request) {
        try {
            logger.info("Session {} sending string: {}", sessionId, request.getText());

            ScreenResponse screen = tn3270Service.sendString(sessionId, request.getText());

            ApiResponse<ScreenResponse> response = ApiResponse.success("String input successful", screen);
            response.setSessionId(sessionId);

            return response;

        } catch (Exception e) {
            logger.error("String input failed", e);
            return ApiResponse.error("String input failed: " + e.getMessage());
        }
    }

    /**
     * API5: Send Enter key
     * POST /api/tn3270/key/enter
     * Header: X-Session-Id: {sessionId}
     */
    @PostMapping("/key/enter")
    public ApiResponse<ScreenResponse> sendEnter(
            @RequestHeader("X-Session-Id") String sessionId) {
        try {
            logger.info("Session {} sending Enter key", sessionId);

            ScreenResponse screen = tn3270Service.sendEnter(sessionId);

            ApiResponse<ScreenResponse> response = ApiResponse.success("Enter key sent successfully", screen);
            response.setSessionId(sessionId);

            return response;

        } catch (Exception e) {
            logger.error("Enter key failed", e);
            return ApiResponse.error("Enter key failed: " + e.getMessage());
        }
    }

    /**
     * API6: Send Reset
     * POST /api/tn3270/key/reset
     * Header: X-Session-Id: {sessionId}
     */
    @PostMapping("/key/reset")
    public ApiResponse<ScreenResponse> sendReset(
            @RequestHeader("X-Session-Id") String sessionId) {
        try {
            logger.info("Session {} sending Reset", sessionId);

            ScreenResponse screen = tn3270Service.sendReset(sessionId);

            ApiResponse<ScreenResponse> response = ApiResponse.success("Reset sent successfully", screen);
            response.setSessionId(sessionId);

            return response;

        } catch (Exception e) {
            logger.error("Reset failed", e);
            return ApiResponse.error("Reset failed: " + e.getMessage());
        }
    }

    /**
     * API7: Send Tab key
     * POST /api/tn3270/key/tab
     * Header: X-Session-Id: {sessionId}
     */
    @PostMapping("/key/tab")
    public ApiResponse<ScreenResponse> sendTab(
            @RequestHeader("X-Session-Id") String sessionId) {
        try {
            logger.info("Session {} sending Tab", sessionId);

            ScreenResponse screen = tn3270Service.sendTab(sessionId);

            ApiResponse<ScreenResponse> response = ApiResponse.success("Tab key sent successfully", screen);
            response.setSessionId(sessionId);

            return response;

        } catch (Exception e) {
            logger.error("Tab key failed", e);
            return ApiResponse.error("Tab key failed: " + e.getMessage());
        }
    }

    /**
     * API9: Send function key (PF1-PF24, PA1-PA3, Clear, etc.)
     * POST /api/tn3270/key/function
     * Body: { "keyName": "PF2" }
     * Header: X-Session-Id: {sessionId}
     */
    @PostMapping("/key/function")
    public ApiResponse<ScreenResponse> sendFunctionKey(
            @RequestHeader("X-Session-Id") String sessionId,
            @RequestBody FunctionKeyRequest request) {
        try {
            logger.info("Session {} sending function key: {}", sessionId, request.getKeyName());

            ScreenResponse screen = tn3270Service.sendFunctionKey(sessionId, request.getKeyName());

            ApiResponse<ScreenResponse> response = ApiResponse.success(
                "Function key " + request.getKeyName() + " sent successfully", screen);
            response.setSessionId(sessionId);

            return response;

        } catch (Exception e) {
            logger.error("Function key failed", e);
            return ApiResponse.error("Function key failed: " + e.getMessage());
        }
    }

    /**
     * API4: Enter level-2 menu (dedicated endpoint)
     * POST /api/tn3270/menu/enter-level2
     * Header: X-Session-Id: {sessionId}
     *
     * Fixed: inputs "2" at row 4 column 13 and presses Enter
     * @deprecated Use API2 + API5 combination instead
     */
    @Deprecated
    @PostMapping("/menu/enter-level2")
    public ApiResponse<ScreenResponse> enterSecondLevelMenu(
            @RequestHeader("X-Session-Id") String sessionId) {
        try {
            logger.info("Session {} received enter-level2-menu request", sessionId);

            ScreenResponse screen = tn3270Service.enterSecondLevelMenu(sessionId);

            ApiResponse<ScreenResponse> response = ApiResponse.success("Successfully entered level-2 menu", screen);
            response.setSessionId(sessionId);

            return response;

        } catch (Exception e) {
            logger.error("Failed to enter level-2 menu", e);
            return ApiResponse.error("Failed to enter level-2 menu: " + e.getMessage());
        }
    }

    /**
     * API3: Execute LOGOFF
     * POST /api/tn3270/logoff
     * Header: X-Session-Id: {sessionId}
     */
    @PostMapping("/logoff")
    public ApiResponse<ScreenResponse> logoff(@RequestHeader("X-Session-Id") String sessionId) {
        try {
            logger.info("Session {} received LOGOFF request", sessionId);

            ScreenResponse screen = tn3270Service.logoff(sessionId);

            ApiResponse<ScreenResponse> response = ApiResponse.success("LOGOFF successful", screen);
            response.setSessionId(sessionId);

            return response;

        } catch (Exception e) {
            logger.error("LOGOFF failed", e);
            return ApiResponse.error("LOGOFF failed: " + e.getMessage());
        }
    }

    /**
     * Extra endpoint: get current screen content
     * GET /api/tn3270/screen
     * Header: X-Session-Id: {sessionId}
     */
    @GetMapping("/screen")
    public ApiResponse<ScreenResponse> getScreen(@RequestHeader("X-Session-Id") String sessionId) {
        try {
            logger.info("Session {} getting screen content", sessionId);

            ScreenResponse screen = tn3270Service.getScreen(sessionId);

            ApiResponse<ScreenResponse> response = ApiResponse.success(screen);
            response.setSessionId(sessionId);

            return response;

        } catch (Exception e) {
            logger.error("Failed to get screen content", e);
            return ApiResponse.error("Failed to get screen content: " + e.getMessage());
        }
    }

    /**
     * Health check
     * GET /api/tn3270/health
     */
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("TN3270 API is running");
    }
}
