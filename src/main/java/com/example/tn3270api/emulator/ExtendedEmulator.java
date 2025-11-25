package com.example.tn3270api.emulator;

import com.github.filipesimoes.j3270.Command;
import com.github.filipesimoes.j3270.Emulator;
import com.github.filipesimoes.j3270.command.SendKeysCommand;

import java.io.*;
import java.lang.reflect.Field;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * 扩展的 3270 模拟器
 */
public class ExtendedEmulator extends Emulator {

    private int scriptPort;

    public ExtendedEmulator() {
        super();
        this.scriptPort = 3270;
    }

    public ExtendedEmulator(int scriptPort) {
        super(scriptPort);
        this.scriptPort = scriptPort;
    }

    /**
     * 获取整个屏幕内容（使用 Ascii 命令），限制为 24 行
     */
    public List<String> getScreenLines() throws IOException {
        // 先等待一下，让屏幕稳定
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<String> lines = new ArrayList<>();

        try {
            // 通过反射获取 commander
            Field commanderField = Emulator.class.getDeclaredField("commander");
            commanderField.setAccessible(true);
            Object commander = commanderField.get(this);

            // 获取 socket 的 reader 和 writer
            Field socketField = commander.getClass().getDeclaredField("socket");
            socketField.setAccessible(true);
            Socket socket = (Socket) socketField.get(commander);

            if (socket == null || !socket.isConnected()) {
                throw new IOException("Socket 未连接");
            }

            // 发送 Ascii() 命令
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), "ASCII")
            );
            PrintWriter writer = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), "ASCII"),
                    true
            );

            writer.println("Ascii()");
            writer.flush();

            // 读取响应，最多 24 行
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals("ok")) {
                    break;
                } else if (line.startsWith("error")) {
                    System.err.println("Ascii 命令返回 error，可能屏幕正在更新");
                    return lines;
                } else if (line.startsWith("data: ")) {
                    // 去掉 "data: " 前缀
                    lines.add(line.substring(6));

                    // 限制最多 24 行
                    if (lines.size() >= 24) {
                        break;
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("getScreenLines 异常: " + e.getMessage());
            return lines;
        }

        return lines;
    }

    public <V> V executeCommand(Command<V> command) {
        return execute(command);
    }

    public void sendKey(String keyName) {
        execute(new SendKeysCommand(keyName));
    }

    /**
     * 发送字符串
     */
    public void sendString(String text) {
        execute(new com.github.filipesimoes.j3270.command.SendStringCommand(text));
    }
}
