package com.example.tn3270api.emulator;

import com.example.tn3270api.command.AsciiCommand;
import com.github.filipesimoes.j3270.Command;
import com.github.filipesimoes.j3270.Emulator;
import com.github.filipesimoes.j3270.TerminalCommander;
import com.github.filipesimoes.j3270.command.MoveCursorCommand;
import com.github.filipesimoes.j3270.command.SendKeysCommand;
import com.github.filipesimoes.j3270.command.WaitCommand;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Extended 3270 emulator with:
 * - Japanese charset support (via custom s3270 runner)
 * - Reliable screen reading (via custom AsciiCommand)
 * - moveCursorAndType (no DeleteField)
 * - waitForOutput (only after Enter/function keys)
 */
public class ExtendedEmulator extends Emulator {

    private int scriptPort;
    private String charset;
    private CustomEmulatorRunner customRunner;

    public ExtendedEmulator() {
        super();
        this.scriptPort = 3270;
    }

    public ExtendedEmulator(int scriptPort) {
        super(scriptPort);
        this.scriptPort = scriptPort;
    }

    public ExtendedEmulator(int scriptPort, String charset) {
        super(scriptPort);
        this.scriptPort = scriptPort;
        this.charset = charset;
    }

    /**
     * Override start() to use our CustomEmulatorRunner when charset is specified.
     * This replaces j3270's default Emulator3270Runner which doesn't support -charset.
     */
    @Override
    public void start() throws IOException, TimeoutException {
        System.out.println("[ExtendedEmulator] Starting CustomEmulatorRunner. charset=" + charset + ", port=" + scriptPort);

        // charsetの有無に関わらず常にCustomEmulatorRunnerを使う
        customRunner = new CustomEmulatorRunner(scriptPort, charset);
        Thread runnerThread = new Thread(customRunner, "s3270-runner");
        runnerThread.setDaemon(true);
        runnerThread.start();

        // s3270 プロセスの起動を待つ（最大5秒）
        int attempts = 0;
        while (!customRunner.isStarted() && attempts < 50) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            attempts++;
        }

        if (!customRunner.isStarted()) {
            throw new TimeoutException("s3270 process failed to start within 5 seconds");
        }

        System.out.println("[ExtendedEmulator] s3270 started. Connecting commander...");

        // commander に接続（reflection）
        try {
            Field commanderField = Emulator.class.getDeclaredField("commander");
            commanderField.setAccessible(true);
            TerminalCommander commander = (TerminalCommander) commanderField.get(this);
            commander.connect();
            System.out.println("[ExtendedEmulator] Commander connected successfully");
        } catch (Exception e) {
            throw new IOException("Failed to connect commander: " + e.getMessage(), e);
        }
    }

    /**
     * Override close() to also stop our custom runner.
     */
    @Override
    public void close() {
        if (customRunner != null) {
            customRunner.stop();
        }
        super.close();
    }

    /**
     * Wait for host to send new screen data.
     * Call ONLY after Enter / function keys.
     */
    public void waitForOutput(int timeoutSeconds) {
        try {
            execute(new WaitCommand(timeoutSeconds, "Output"));
        } catch (Exception e) {
            System.err.println("Wait(Output) failed: " + e.getMessage());
        }
    }

    public void waitForOutput() {
        waitForOutput(10);
    }

    /**
     * Get full screen content.
     * Returns all rows (24 for Model 2, 43 for Model 4).
     */
    public List<String> getScreenLines() throws IOException {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<String> lines;
        try {
            List<String> result = execute(new AsciiCommand());
            lines = (result != null) ? new ArrayList<>(result) : new ArrayList<>();
        } catch (Exception e) {
            System.err.println("getScreenLines error: " + e.getMessage());
            lines = new ArrayList<>();
        }

        return lines;
    }

    public <V> V executeCommand(Command<V> command) {
        return execute(command);
    }

    public void sendKey(String keyName) {
        execute(new SendKeysCommand(keyName));
    }

    public void sendString(String text) {
        execute(new com.github.filipesimoes.j3270.command.SendStringCommand(text));
    }

    /**
     * Move cursor to position and type text.
     * Does NOT send DeleteField (unlike fillField).
     */
    public void moveCursorAndType(int row, int col, String text) {
        execute(new MoveCursorCommand(row, col));
        execute(new com.github.filipesimoes.j3270.command.SendStringCommand(text));
    }
}
