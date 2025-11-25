package com.example.tn3270api.controller;

import com.example.tn3270api.dto.ApiResponse;
import com.example.tn3270api.dto.MenuCommandRequest;
import com.example.tn3270api.dto.StringRequest;
import com.example.tn3270api.dto.ScreenResponse;
import com.example.tn3270api.service.Tn3270Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * TN3270 REST API 控制器
 */
@RestController
@RequestMapping("/api/tn3270")
@CrossOrigin(origins = "*") // 允许跨域，方便 React 调用
public class Tn3270Controller {

    private static final Logger logger = LoggerFactory.getLogger(Tn3270Controller.class);

    @Autowired
    private Tn3270Service tn3270Service;

    /**
     * API1: 登录到 TSO 主菜单
     * POST /api/tn3270/login
     */
    @PostMapping("/login")
    public ApiResponse<ScreenResponse> login() {
        try {
            logger.info("收到登录请求");
            String sessionId = tn3270Service.login();
            ScreenResponse screen = tn3270Service.getScreen(sessionId);

            ApiResponse<ScreenResponse> response = ApiResponse.success("登录成功", screen);
            response.setSessionId(sessionId);

            logger.info("登录成功，会话ID: {}", sessionId);
            return response;

        } catch (Exception e) {
            logger.error("登录失败", e);
            return ApiResponse.error("登录失败: " + e.getMessage());
        }
    }

    /**
     * API2: 发送菜单命令（可配置位置，不自动回车）
     * POST /api/tn3270/menu/command
     * Body: { "row": 4, "column": 13, "command": "2" }
     * Header: X-Session-Id: {sessionId}
     */
    @PostMapping("/menu/command")
    public ApiResponse<ScreenResponse> sendMenuCommand(
            @RequestHeader("X-Session-Id") String sessionId,
            @RequestBody MenuCommandRequest request) {
        try {
            logger.info("会话 {} 收到菜单命令: 行={}, 列={}, 命令={}", 
                sessionId, request.getRow(), request.getColumn(), request.getCommand());

            ScreenResponse screen = tn3270Service.sendMenuCommand(
                sessionId, 
                request.getRow(), 
                request.getColumn(), 
                request.getCommand()
            );

            ApiResponse<ScreenResponse> response = ApiResponse.success("命令输入成功", screen);
            response.setSessionId(sessionId);

            return response;

        } catch (Exception e) {
            logger.error("命令输入失败", e);
            return ApiResponse.error("命令输入失败: " + e.getMessage());
        }
    }

    /**
     * API8: 发送字符串（不指定位置）
     * POST /api/tn3270/send/string
     * Body: { "text": "Hello" }
     * Header: X-Session-Id: {sessionId}
     * 
     * 在当前光标位置输入字符串
     */
    @PostMapping("/send/string")
    public ApiResponse<ScreenResponse> sendString(
            @RequestHeader("X-Session-Id") String sessionId,
            @RequestBody StringRequest request) {
        try {
            logger.info("会话 {} 发送字符串: {}", sessionId, request.getText());

            ScreenResponse screen = tn3270Service.sendString(sessionId, request.getText());

            ApiResponse<ScreenResponse> response = ApiResponse.success("字符串输入成功", screen);
            response.setSessionId(sessionId);

            return response;

        } catch (Exception e) {
            logger.error("字符串输入失败", e);
            return ApiResponse.error("字符串输入失败: " + e.getMessage());
        }
    }

    /**
     * API5: 发送回车键
     * POST /api/tn3270/key/enter
     * Header: X-Session-Id: {sessionId}
     */
    @PostMapping("/key/enter")
    public ApiResponse<ScreenResponse> sendEnter(
            @RequestHeader("X-Session-Id") String sessionId) {
        try {
            logger.info("会话 {} 发送回车键", sessionId);

            ScreenResponse screen = tn3270Service.sendEnter(sessionId);

            ApiResponse<ScreenResponse> response = ApiResponse.success("回车键发送成功", screen);
            response.setSessionId(sessionId);

            return response;

        } catch (Exception e) {
            logger.error("回车键发送失败", e);
            return ApiResponse.error("回车键发送失败: " + e.getMessage());
        }
    }

    /**
     * API6: 发送 Reset
     * POST /api/tn3270/key/reset
     * Header: X-Session-Id: {sessionId}
     */
    @PostMapping("/key/reset")
    public ApiResponse<ScreenResponse> sendReset(
            @RequestHeader("X-Session-Id") String sessionId) {
        try {
            logger.info("会话 {} 发送 Reset", sessionId);

            ScreenResponse screen = tn3270Service.sendReset(sessionId);

            ApiResponse<ScreenResponse> response = ApiResponse.success("Reset 发送成功", screen);
            response.setSessionId(sessionId);

            return response;

        } catch (Exception e) {
            logger.error("Reset 发送失败", e);
            return ApiResponse.error("Reset 发送失败: " + e.getMessage());
        }
    }

    /**
     * API7: 发送 Tab 键
     * POST /api/tn3270/key/tab
     * Header: X-Session-Id: {sessionId}
     */
    @PostMapping("/key/tab")
    public ApiResponse<ScreenResponse> sendTab(
            @RequestHeader("X-Session-Id") String sessionId) {
        try {
            logger.info("会话 {} 发送 Tab", sessionId);

            ScreenResponse screen = tn3270Service.sendTab(sessionId);

            ApiResponse<ScreenResponse> response = ApiResponse.success("Tab 键发送成功", screen);
            response.setSessionId(sessionId);

            return response;

        } catch (Exception e) {
            logger.error("Tab 键发送失败", e);
            return ApiResponse.error("Tab 键发送失败: " + e.getMessage());
        }
    }

    /**
     * API4: 进入第2级菜单（专用接口）
     * POST /api/tn3270/menu/enter-level2
     * Header: X-Session-Id: {sessionId}
     * 
     * 固定在第4行第13列输入"2"并按回车
     * @deprecated 建议使用 API2 + API5 组合
     */
    @Deprecated
    @PostMapping("/menu/enter-level2")
    public ApiResponse<ScreenResponse> enterSecondLevelMenu(
            @RequestHeader("X-Session-Id") String sessionId) {
        try {
            logger.info("会话 {} 收到进入第2级菜单请求", sessionId);

            ScreenResponse screen = tn3270Service.enterSecondLevelMenu(sessionId);

            ApiResponse<ScreenResponse> response = ApiResponse.success("成功进入第2级菜单", screen);
            response.setSessionId(sessionId);

            return response;

        } catch (Exception e) {
            logger.error("进入第2级菜单失败", e);
            return ApiResponse.error("进入第2级菜单失败: " + e.getMessage());
        }
    }

    /**
     * API3: 执行 LOGOFF
     * POST /api/tn3270/logoff
     * Header: X-Session-Id: {sessionId}
     */
    @PostMapping("/logoff")
    public ApiResponse<ScreenResponse> logoff(@RequestHeader("X-Session-Id") String sessionId) {
        try {
            logger.info("会话 {} 收到 LOGOFF 请求", sessionId);

            ScreenResponse screen = tn3270Service.logoff(sessionId);

            ApiResponse<ScreenResponse> response = ApiResponse.success("LOGOFF 成功", screen);
            response.setSessionId(sessionId);

            return response;

        } catch (Exception e) {
            logger.error("LOGOFF 失败", e);
            return ApiResponse.error("LOGOFF 失败: " + e.getMessage());
        }
    }

    /**
     * 额外接口: 获取当前屏幕内容
     * GET /api/tn3270/screen
     * Header: X-Session-Id: {sessionId}
     */
    @GetMapping("/screen")
    public ApiResponse<ScreenResponse> getScreen(@RequestHeader("X-Session-Id") String sessionId) {
        try {
            logger.info("会话 {} 获取屏幕内容", sessionId);

            ScreenResponse screen = tn3270Service.getScreen(sessionId);

            ApiResponse<ScreenResponse> response = ApiResponse.success(screen);
            response.setSessionId(sessionId);

            return response;

        } catch (Exception e) {
            logger.error("获取屏幕内容失败", e);
            return ApiResponse.error("获取屏幕内容失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查
     * GET /api/tn3270/health
     */
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("TN3270 API 运行正常");
    }
}
