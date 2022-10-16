/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.lancia.kernel.browser;

import org.aoju.bus.core.toolkit.IoKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.health.Platform;
import org.aoju.bus.logger.Logger;
import org.aoju.lancia.Builder;
import org.aoju.lancia.events.BrowserListenerWrapper;
import org.aoju.lancia.events.DefaultBrowserListener;
import org.aoju.lancia.events.EventEmitter;
import org.aoju.lancia.option.ConnectionOptions;
import org.aoju.lancia.option.LaunchOptions;
import org.aoju.lancia.worker.Connection;
import org.aoju.lancia.worker.Transport;
import org.aoju.lancia.worker.TransportFactory;
import org.aoju.lancia.worker.exception.LaunchException;
import org.aoju.lancia.worker.exception.TimeoutException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class Runner extends EventEmitter implements AutoCloseable {

    private static final Pattern WS_ENDPOINT_PATTERN = Pattern.compile("^DevTools listening on (ws://.*)$");
    private static final List<Runner> runners = new ArrayList<>();
    private static boolean isRegisterShutdownHook = false;
    private final String executablePath;
    private final List<String> processArguments;
    private final String tempDirectory;
    private final List<BrowserListenerWrapper> listeners = new ArrayList<>();
    private Process process;
    private Connection connection;
    private boolean closed;

    public Runner(String executablePath, List<String> processArguments, String tempDirectory) {
        super();
        this.executablePath = executablePath;
        this.processArguments = processArguments;
        this.tempDirectory = tempDirectory;
        this.closed = true;
    }

    /**
     * 启动浏览器进程
     *
     * @param options 启动参数
     * @throws IOException io异常
     */
    public void start(LaunchOptions options) throws IOException {
        if (process != null) {
            throw new RuntimeException("This process has previously been started.");
        }
        List<String> arguments = new ArrayList<>();
        arguments.add(executablePath);
        arguments.addAll(processArguments);

        ProcessBuilder processBuilder = new ProcessBuilder().command(arguments).redirectErrorStream(true);
        process = processBuilder.start();
        this.closed = false;

        registerHook();
        addProcessListener(options);
    }

    /**
     * 注册钩子函数，程序关闭时，关闭浏览器
     */
    private void registerHook() {
        runners.add(this);
        if (!isRegisterShutdownHook) {
            synchronized (Runner.class) {
                if ((!isRegisterShutdownHook)) {
                    RuntimeShutdownHookRegistry hook = new RuntimeShutdownHookRegistry();
                    hook.register(new Thread(this::close));
                    isRegisterShutdownHook = true;
                }
            }
        }
    }

    /**
     * 添加浏览器的一些事件监听 退出 SIGINT等
     *
     * @param options 启动参数
     */
    private void addProcessListener(LaunchOptions options) {
        DefaultBrowserListener<Object> exitListener = new DefaultBrowserListener<>() {
            @Override
            public void onBrowserEvent(Object event) {
                Runner runner = (Runner) this.getTarget();
                runner.kill();
            }
        };
        exitListener.setMethod("exit");
        exitListener.setTarget(this);
        this.listeners.add(Builder.addEventListener(this, exitListener.getMethod(), exitListener));

        if (options.getHandleSIGINT()) {
            DefaultBrowserListener<Object> sigintListener = new DefaultBrowserListener<>() {
                @Override
                public void onBrowserEvent(Object event) {
                    Runner runner = (Runner) this.getTarget();
                    runner.kill();
                }
            };
            sigintListener.setMethod("SIGINT");
            sigintListener.setTarget(this);
            this.listeners.add(Builder.addEventListener(this, sigintListener.getMethod(), sigintListener));
        }

        if (options.getHandleSIGTERM()) {
            DefaultBrowserListener<Object> sigtermListener = new DefaultBrowserListener<>() {
                @Override
                public void onBrowserEvent(Object event) {
                    Runner runner = (Runner) this.getTarget();
                    runner.close();
                }
            };
            sigtermListener.setMethod("SIGTERM");
            sigtermListener.setTarget(this);
            this.listeners.add(Builder.addEventListener(this, sigtermListener.getMethod(), sigtermListener));
        }

        if (options.getHandleSIGHUP()) {
            DefaultBrowserListener<Object> sighubListener = new DefaultBrowserListener<>() {
                @Override
                public void onBrowserEvent(Object event) {
                    Runner runner = (Runner) this.getTarget();
                    runner.close();
                }
            };
            sighubListener.setMethod("SIGHUP");
            sighubListener.setTarget(this);
            this.listeners.add(Builder.addEventListener(this, sighubListener.getMethod(), sighubListener));
        }
    }

    /**
     * kill 掉浏览器进程
     */
    public void kill() {
        this.destroyForcibly();
        //delete user-data-dir
        try {
            if (StringKit.isNotEmpty(tempDirectory)) {
                removeFolderByCmd(tempDirectory);
//                FileUtil.removeFolder(tempDirectory);
                //同时把以前没删除干净的文件夹也重新删除一遍
//                Stream<Path> remainTempDirectories = Files.list(Paths.get(tempDirectory).getParent());
//                remainTempDirectories.forEach(path -> {
//                    if (path.getFileName().toString().startsWith(Builder.PROFILE_PREFIX)) {
////                        FileUtil.removeFolder(path.toString());
//                        try {
//                            removeFolderByCmd(path.toString());
//                        } catch (IOException | InterruptedException e) {
//
//                        }
//                    }
//                });
            }

        } catch (Exception e) {
            Logger.error("kill chrome process error ", e);
        }
    }

    /**
     * 强制结束浏览器进程
     */
    public void destroyForcibly() {
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
        }
    }

    /**
     * 通过命令行删除文件夹
     *
     * @param path 删除的路径
     * @throws IOException          异常
     * @throws InterruptedException 异常
     */
    private void removeFolderByCmd(String path) throws IOException, InterruptedException {
        if (StringKit.isEmpty(path) || "*".equals(path)) {
            return;
        }
        Process delProcess = null;
        if (Platform.isWindows()) {
            delProcess = Runtime.getRuntime().exec("cmd /c rd /s /q " + path);
        } else if (Platform.isLinux() || Platform.isMac()) {
            String[] cmd = new String[]{"/bin/sh", "-c", "rm -rf " + path};
            delProcess = Runtime.getRuntime().exec(cmd);
        }
        if (!delProcess.waitFor(10000, TimeUnit.MILLISECONDS)) {
            delProcess.destroyForcibly();
        }
    }

    /**
     * 连接浏览器
     *
     * @param usePipe           是否是pipe连接
     * @param timeout           超时时间
     * @param slowMo            放慢频率
     * @param dumpio            浏览器版本
     * @param connectionOptions 链接选项
     * @return
     */
    public Connection setUpConnection(boolean usePipe, int timeout, int slowMo, boolean dumpio, ConnectionOptions connectionOptions) throws InterruptedException {
        Connection connection = this.setUpConnection(usePipe, timeout, slowMo, dumpio);
        connection.setConnectionOptions(connectionOptions);
        return connection;
    }

    /**
     * 连接上浏览器
     *
     * @param usePipe 是否是pipe连接
     * @param timeout 超时时间
     * @param slowMo  放慢频率
     * @param dumpio  浏览器版本
     * @return 连接对象
     * @throws InterruptedException 打断异常
     */
    public Connection setUpConnection(boolean usePipe, int timeout, int slowMo, boolean dumpio) throws InterruptedException {
        if (usePipe) {
            // pipe connection
            /*
            throw new LaunchException("Temporarily not supported pipe connect to chromuim.If you have a pipe connect to chromium idea,pleaze new a issue in github:https://github.com/aoju/lancia/issues");
            InputStream pipeRead = this.getProcess().getInputStream();
            OutputStream pipeWrite = this.getProcess().getOutputStream();
            PipeTransport transport = new PipeTransport(pipeRead, pipeWrite);
            this.connection = new Connection("", transport, slowMo);
            */
        } else {
            /// websoket connection
            String waitForWSEndpoint = waitForWSEndpoint(timeout, dumpio);
            Transport transport = TransportFactory.create(waitForWSEndpoint);
            this.connection = new Connection(waitForWSEndpoint, transport, slowMo);
            Logger.info("Connect to browser by websocket url: " + waitForWSEndpoint);
        }
        return this.connection;
    }

    /**
     * 等待浏览器ws url
     *
     * @param timeout 等待超时时间
     * @param dumpio  浏览器版本
     * @return ws url
     */
    private String waitForWSEndpoint(int timeout, boolean dumpio) {
        Runner.StreamReader reader = new Runner.StreamReader(timeout, dumpio, process.getInputStream());
        reader.start();
        return reader.getResult();
    }

    public Process getProcess() {
        return process;
    }

    @Override
    public void close() {
        for (int i = 0; i < runners.size(); i++) {
            Runner runner = runners.get(i);
            if (runner.getClosed()) {
                break;
            }

            if (runner.getConnection() != null && !runner.getConnection().getClosed()) {
                runner.getConnection().send("Browser.close", null, false);
            }

            if (StringKit.isNotEmpty(runner.getTempDirectory())) {
                runner.kill();
            }
        }
    }

    /**
     * 关闭浏览器
     */
    public void closeQuietly() {
        if (this.getClosed()) {
            return;
        }
        Builder.removeEventListeners(this.listeners);

        // 先发送指令关闭
        if (this.connection != null && !this.connection.getClosed()) {
            this.connection.send("Browser.close", null, false);
        }

        // 再调用 java 的 api 去关闭，但是这个 api 成功率不是100%
        if (StringKit.isNotEmpty(this.tempDirectory)) {
            this.kill();
        }
        this.closed = true;
    }

    public boolean getClosed() {
        return closed;
    }

    public String getTempDirectory() {
        return tempDirectory;
    }

    public Connection getConnection() {
        return connection;
    }

    /**
     * 注册钩子
     */
    public interface ShutdownHookRegistry {

        /**
         * 注册一个新的关闭钩子线程
         *
         * @param thread 线程信息
         */
        default void register(Thread thread) {
            Runtime.getRuntime().addShutdownHook(thread);
        }

        /**
         * 删除关闭线程
         *
         * @param thread 线程信息
         */
        default void remove(Thread thread) {
            Runtime.getRuntime().removeShutdownHook(thread);
        }
    }

    static class StreamReader {

        private final StringBuilder ws = new StringBuilder();
        private final AtomicBoolean success = new AtomicBoolean(false);
        private final AtomicReference<String> chromeOutput = new AtomicReference<>("");

        private final int timeout;

        private final boolean dumpio;

        private final InputStream inputStream;

        private Thread readThread;

        public StreamReader(int timeout, boolean dumpio, InputStream inputStream) {
            this.timeout = timeout;
            this.dumpio = dumpio;
            this.inputStream = inputStream;
        }

        public void start() {
            readThread = new Thread(
                    () -> {
                        StringBuilder chromeOutputBuilder = new StringBuilder();
                        BufferedReader reader = null;
                        try {
                            reader = new BufferedReader(new InputStreamReader(inputStream));
                            String line;
                            while ((line = reader.readLine()) != null) {
                                if (dumpio) {
                                    System.out.println(line);
                                }
                                Matcher matcher = WS_ENDPOINT_PATTERN.matcher(line);
                                if (matcher.find()) {
                                    ws.append(matcher.group(1));
                                    success.set(true);
                                    break;
                                }

                                if (chromeOutputBuilder.length() != 0) {
                                    chromeOutputBuilder.append(System.lineSeparator());
                                }
                                chromeOutputBuilder.append(line);
                                chromeOutput.set(chromeOutputBuilder.toString());
                            }
                        } catch (Exception e) {
                            Logger.error("Failed to launch the browser process!please see TROUBLESHOOTING: https://github.com/puppeteer/puppeteer/blob/master/docs/troubleshooting.md:", e);
                        } finally {
                            IoKit.close(reader);
                        }
                    });

            readThread.start();
        }

        public String getResult() {
            try {
                readThread.join(timeout);
                if (!success.get()) {
                    if (readThread != null) {
                        readThread = null;
                    }
                    throw new TimeoutException(
                            "Timed out after " + timeout + " ms while trying to connect to the browser!"
                                    + "Chrome output: "
                                    + chromeOutput.get());
                }
            } catch (InterruptedException e) {
                if (readThread != null) {
                    readThread = null;
                }
                throw new RuntimeException("Interrupted while waiting for dev tools server.", e);
            }
            String url = ws.toString();
            if (StringKit.isEmpty(url)) {
                throw new LaunchException("Can't get WSEndpoint");
            }
            return url;
        }

    }

    /**
     * 基于运行时的关闭钩子
     */
    public static class RuntimeShutdownHookRegistry implements ShutdownHookRegistry {

    }

}


