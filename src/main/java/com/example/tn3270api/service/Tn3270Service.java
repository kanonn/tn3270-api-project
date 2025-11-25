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
 * TN3270 服务
 */
@Service
public class Tn3270Service {

    private static final Logger logger = LoggerFactory.getLogger(Tn3270Service.class);

    @Autowired
    private Tn3270Properties properties;

    // 会话管理
    private final Map<String, Tn3270Session> sessions = new ConcurrentHashMap<>();

    /**
     * 登录并建立连接（API1）
     */
    public String login() throws Exception {
        String sessionId = UUID.randomUUID().toString();
        logger.info("创建新会话: {}", sessionId);

        ExtendedEmulator emulator = new ExtendedEmulator(properties.getScriptPort());
        Tn3270Session session = new Tn3270Session(sessionId, emulator);

        try {
            // 启动模拟器
            logger.info("启动 3270 模拟器...");
            emulator.start();
            Thread.sleep(3000); // 等待模拟器启动

            // 连接到主机
            String connectionString = properties.getHost() + ":" + properties.getPort();
            logger.info("连接到主机: {}", connectionString);
            boolean connected = emulator.connect(connectionString);

            if (!connected) {
                throw new IOException("连接主机失败");
            }

            session.setConnected(true);
            logger.info("连接成功，等待主机响应...");

            // 等待解锁
            logger.info("等待键盘解锁...");
            emulator.waitUnlock(30);
            Thread.sleep(5000); // 等待屏幕数据

            // 检查 LOGON
            if (!screenContains(emulator, "LOGON")) {
                logger.warn("未找到 LOGON 文字，执行 CLEAR 和 RESET...");
                emulator.sendKey("Clear");
                Thread.sleep(1000);
                emulator.sendKey("Reset");
                Thread.sleep(1000);
            }

            // 登录流程
            logger.info("开始登录流程...");

            // 输入用户名
            logger.info("输入用户名: {}", properties.getUsername());
            emulator.fillField(22, 13, properties.getUsername());
            Thread.sleep(1000);

            // 提交用户名
            logger.info("提交用户名");
            emulator.sendEnter();
            Thread.sleep(5000);

            try {
                emulator.waitUnlock(10);
            } catch (Exception e) {
                logger.error("waitUnlock 失败", e);
            }

            Thread.sleep(2000);

            // 输入密码
            logger.info("输入密码");
            emulator.fillField(10, 20, properties.getPassword());
            Thread.sleep(1000);

            // 提交密码
            logger.info("提交密码");
            emulator.sendEnter();
            Thread.sleep(3000);

            // 提交登录信息
            logger.info("提交登录信息");
            emulator.sendEnter();
            Thread.sleep(3000);

            // 进入主菜单
            logger.info("进入主菜单");
            emulator.sendEnter();
            Thread.sleep(2000);

            // 保存会话
            sessions.put(sessionId, session);
            logger.info("登录成功，会话已保存: {}", sessionId);

            return sessionId;

        } catch (Exception e) {
            logger.error("登录失败", e);
            // 清理资源
            try {
                emulator.disconnect();
                emulator.close();
            } catch (Exception ex) {
                logger.error("清理资源失败", ex);
            }
            throw e;
        }
    }

    /**
     * 发送菜单命令（API2） - 不自动回车
     */
    public ScreenResponse sendMenuCommand(String sessionId, int row, int column, String command) throws Exception {
        Tn3270Session session = getSession(sessionId);
        ExtendedEmulator emulator = session.getEmulator();

        logger.info("会话 {} 在位置 [行:{}, 列:{}] 输入命令: {}", sessionId, row, column, command);

        // 在指定位置输入命令（不按回车）
        emulator.fillField(row, column, command);
        Thread.sleep(1000);

        // 更新访问时间
        session.updateAccessTime();

        // 返回屏幕内容
        return getScreen(sessionId);
    }

    /**
     * 发送字符串（API8） - 不指定位置，在当前光标位置输入
     */
    public ScreenResponse sendString(String sessionId, String text) throws Exception {
        Tn3270Session session = getSession(sessionId);
        ExtendedEmulator emulator = session.getEmulator();

        logger.info("会话 {} 在当前光标位置输入字符串: {}", sessionId, text);

        // 在当前光标位置输入字符串
        emulator.sendString(text);
        Thread.sleep(500);

        // 更新访问时间
        session.updateAccessTime();

        // 返回屏幕内容
        return getScreen(sessionId);
    }

    /**
     * 发送回车键（API5）
     */
    public ScreenResponse sendEnter(String sessionId) throws Exception {
        Tn3270Session session = getSession(sessionId);
        ExtendedEmulator emulator = session.getEmulator();

        logger.info("会话 {} 发送回车键", sessionId);

        // 按回车
        emulator.sendEnter();
        Thread.sleep(2000);

        // 等待解锁
        try {
            emulator.waitUnlock(10);
        } catch (Exception e) {
            logger.warn("waitUnlock 超时");
        }

        Thread.sleep(1000);

        // 更新访问时间
        session.updateAccessTime();

        // 返回屏幕内容
        return getScreen(sessionId);
    }

    /**
     * 发送 Reset（API6）
     */
    public ScreenResponse sendReset(String sessionId) throws Exception {
        Tn3270Session session = getSession(sessionId);
        ExtendedEmulator emulator = session.getEmulator();

        logger.info("会话 {} 发送 Reset", sessionId);

        // 发送 Reset 键
        emulator.sendKey("Reset");
        Thread.sleep(1000);

        // 等待解锁
        try {
            emulator.waitUnlock(10);
        } catch (Exception e) {
            logger.warn("waitUnlock 超时");
        }

        Thread.sleep(500);

        // 更新访问时间
        session.updateAccessTime();

        // 返回屏幕内容
        return getScreen(sessionId);
    }

    /**
     * 发送 Tab 键（API7）
     */
    public ScreenResponse sendTab(String sessionId) throws Exception {
        Tn3270Session session = getSession(sessionId);
        ExtendedEmulator emulator = session.getEmulator();

        logger.info("会话 {} 发送 Tab", sessionId);

        // 发送 Tab 键
        emulator.sendKey("Tab");
        Thread.sleep(500);

        // 更新访问时间
        session.updateAccessTime();

        // 返回屏幕内容
        return getScreen(sessionId);
    }

    /**
     * 进入第2级菜单 - 专用接口（API4）
     * 固定在第4行第13列输入"2"
     * @deprecated 建议使用 sendMenuCommand(sessionId, 4, 13, "2") + sendEnter(sessionId) 代替
     */
    @Deprecated
    public ScreenResponse enterSecondLevelMenu(String sessionId) throws Exception {
        logger.info("会话 {} 进入第2级菜单 [使用固定位置]", sessionId);
        sendMenuCommand(sessionId, 4, 13, "2");
        return sendEnter(sessionId);
    }

    /**
     * 执行 LOGOFF（API3）
     */
    public ScreenResponse logoff(String sessionId) throws Exception {
        Tn3270Session session = getSession(sessionId);
        ExtendedEmulator emulator = session.getEmulator();

        logger.info("会话 {} 执行 LOGOFF", sessionId);

        // 发送 LOGOFF
        emulator.sendString("LOGOFF");
        Thread.sleep(1000);

        emulator.sendEnter();
        Thread.sleep(3000);

        // 等待解锁
        try {
            emulator.waitUnlock(10);
        } catch (Exception e) {
            logger.warn("waitUnlock 超时");
        }

        Thread.sleep(2000);

        // 获取屏幕内容
        ScreenResponse response = getScreen(sessionId);

        // 清理会话
        closeSession(sessionId);

        return response;
    }

    /**
     * 获取屏幕内容
     */
    public ScreenResponse getScreen(String sessionId) throws Exception {
        Tn3270Session session = getSession(sessionId);
        ExtendedEmulator emulator = session.getEmulator();

        List<String> lines = emulator.getScreenLines();

        StringBuilder fullScreen = new StringBuilder();
        int row = 1;
        for (String line : lines) {
            fullScreen.append(String.format("行%02d: %s\n", row++, line));
        }

        session.updateAccessTime();

        return new ScreenResponse(lines, fullScreen.toString());
    }

    /**
     * 获取会话
     */
    private Tn3270Session getSession(String sessionId) throws Exception {
        Tn3270Session session = sessions.get(sessionId);
        if (session == null) {
            throw new Exception("会话不存在或已过期: " + sessionId);
        }
        return session;
    }

    /**
     * 关闭会话
     */
    public void closeSession(String sessionId) {
        Tn3270Session session = sessions.remove(sessionId);
        if (session != null) {
            try {
                ExtendedEmulator emulator = session.getEmulator();
                emulator.disconnect();
                emulator.close();
                logger.info("会话 {} 已关闭", sessionId);
            } catch (Exception e) {
                logger.error("关闭会话失败: {}", sessionId, e);
            }
        }
    }

    /**
     * 检查屏幕是否包含指定文本
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
            logger.error("screenContains 出错", e);
            return false;
        }
    }

    /**
     * 定期清理过期会话
     */
    @PostConstruct
    public void init() {
        // 可以添加定时任务清理过期会话
        logger.info("TN3270 服务已初始化");
    }

    /**
     * 应用关闭时清理所有会话
     */
    @PreDestroy
    public void cleanup() {
        logger.info("清理所有会话...");
        for (String sessionId : sessions.keySet()) {
            closeSession(sessionId);
        }
        logger.info("所有会话已清理");
    }
}
