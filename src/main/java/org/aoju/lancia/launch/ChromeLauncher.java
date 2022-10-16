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
package org.aoju.lancia.launch;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import org.aoju.bus.core.toolkit.CollKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.logger.Logger;
import org.aoju.lancia.Browser;
import org.aoju.lancia.Builder;
import org.aoju.lancia.Launcher;
import org.aoju.lancia.kernel.browser.Fetcher;
import org.aoju.lancia.kernel.browser.Revision;
import org.aoju.lancia.kernel.browser.Runner;
import org.aoju.lancia.option.BrowserOptions;
import org.aoju.lancia.option.ChromeArgOptions;
import org.aoju.lancia.option.FetcherOptions;
import org.aoju.lancia.option.LaunchOptions;
import org.aoju.lancia.worker.Connection;
import org.aoju.lancia.worker.Transport;
import org.aoju.lancia.worker.TransportFactory;
import org.aoju.lancia.worker.exception.LaunchException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * Chrome启动支持
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class ChromeLauncher implements Launcher {

    private boolean isPuppeteerCore;

    private String projectRoot;

    private String preferredRevision;

    public ChromeLauncher() {

    }

    public ChromeLauncher(String projectRoot, String preferredRevision, boolean isPuppeteerCore) {
        super();
        this.projectRoot = projectRoot;
        this.preferredRevision = preferredRevision;
        this.isPuppeteerCore = isPuppeteerCore;
    }

    @Override
    public Browser launch(LaunchOptions options) throws IOException {
        String temporaryUserDataDir = null;
        List<String> chromeArguments = defaultArgs(options);

        List<String> ignoreDefaultArgs;
        if (CollKit.isNotEmpty(ignoreDefaultArgs = options.getIgnoreDefaultArgs())) {
            chromeArguments.removeAll(ignoreDefaultArgs);
        }
        boolean isCustomUserDir = false;
        boolean isCustomRemoteDebugger = false;
        for (String arg : chromeArguments) {
            if (arg.startsWith("--remote-debugging-")) {
                isCustomRemoteDebugger = true;
            } else if (arg.startsWith("--user-data-dir")) {
                isCustomUserDir = true;
            }
        }
        if (!isCustomUserDir) {
            temporaryUserDataDir = Files.createTempDirectory(Builder.PROFILE_PREFIX).toRealPath().toString();
            chromeArguments.add("--user-data-dir=" + temporaryUserDataDir);
        }
        if (!isCustomRemoteDebugger) {
            chromeArguments.add(options.getPipe() ? "--remote-debugging-pipe" : "--remote-debugging-port=0");
        }

        String chromeExecutable = resolveExecutablePath(options.getExecutablePath());
        boolean usePipe = chromeArguments.contains("--remote-debugging-pipe");

        Logger.trace("Calling " + chromeExecutable + String.join(" ", chromeArguments));
        Runner runner = new Runner(chromeExecutable, chromeArguments, temporaryUserDataDir);//
        try {
            runner.start(options);
            Connection connection = runner.setUpConnection(usePipe, options.getTimeout(), options.getSlowMo(), options.getDumpio(), options.getConnectionOptions());
            Function<Object, Object> closeCallback = (s) -> {
                runner.closeQuietly();
                return null;
            };
            Browser browser = Browser.create(connection, null, options.getIgnoreHTTPSErrors(), options.getViewport(), runner.getProcess(), closeCallback);
            browser.waitForTarget(t -> "page".equals(t.type()), options);
            return browser;
        } catch (IOException | InterruptedException e) {
            runner.kill();
            throw new LaunchException("Failed to launch the browser process:" + e.getMessage(), e);
        }
    }

    /**
     * 返回默认的启动参数
     *
     * @param options 自定义的参数
     * @return 默认的启动参数
     */
    @Override
    public List<String> defaultArgs(ChromeArgOptions options) {
        List<String> chromeArguments = new ArrayList<>();
        LaunchOptions launchOptions;
        if (StringKit.isNotEmpty(options.getUserDataDir())) {
            chromeArguments.add("--user-data-dir=" + options.getUserDataDir());
        }
        boolean devtools = options.getDevtools();
        boolean headless = options.getHeadless();
        if (devtools) {
            chromeArguments.add("--auto-open-devtools-for-tabs");
            headless = false;
        }
        if (headless) {
            chromeArguments.add("--headless");
            chromeArguments.add("--hide-scrollbars");
            chromeArguments.add("--mute-audio");
        }
        List<String> args;
        if (CollKit.isNotEmpty(args = options.getArgs())) {
            chromeArguments.add("about:blank");
            chromeArguments.addAll(args);
        }
        if (options instanceof LaunchOptions) {
            launchOptions = (LaunchOptions) options;
            if (!launchOptions.getIgnoreAllDefaultArgs()) {
                chromeArguments.addAll(Builder.DEFAULT_ARGS);
            }
        }
        return chromeArguments;
    }

    /**
     * 解析可执行的chrome路径
     *
     * @param chromeExecutable 指定的可执行路径
     * @return 返回解析后的可执行路径
     */
    @Override
    public String resolveExecutablePath(String chromeExecutable) throws IOException {
        boolean puppeteerCore = getIsPuppeteerCore();
        FetcherOptions fetcherOptions = new FetcherOptions();
        fetcherOptions.setProduct(this.product());
        Fetcher fetcher = new Fetcher(this.projectRoot, fetcherOptions);
        if (!puppeteerCore) {
            // 指定了启动路径，则启动指定路径的chrome
            if (StringKit.isNotEmpty(chromeExecutable)) {
                boolean assertDir = Builder.assertExecutable(Paths.get(chromeExecutable).normalize().toAbsolutePath().toString());
                if (!assertDir) {
                    throw new IllegalArgumentException("given chromeExecutable \"" + chromeExecutable + "\" is not executable");
                }
                return chromeExecutable;
            }
            // 环境变量中配置了chromeExecutable，就使用环境变量中的路径
            for (int i = 0; i < Builder.EXECUTABLE_ENV.length; i++) {
                chromeExecutable = VARIABLES.getEnv(Builder.EXECUTABLE_ENV[i]);
                if (StringKit.isNotEmpty(chromeExecutable)) {
                    boolean assertDir = Builder.assertExecutable(chromeExecutable);
                    if (!assertDir) {
                        throw new IllegalArgumentException("given chromeExecutable is not is not executable");
                    }
                    return chromeExecutable;
                }
            }

            // 环境变量中配置了chrome版本，就用环境变量中的版本
            String revision = VARIABLES.getEnv(Builder.PUPPETEER_CHROMIUM_REVISION_ENV);
            if (StringKit.isNotEmpty(revision)) {
                Revision revisionInfo = fetcher.revisionInfo(revision);
                if (!revisionInfo.isLocal()) {
                    throw new LaunchException(
                            "Tried to use PUPPETEER_CHROMIUM_REVISION env variable to launch browser but did not find executable at: "
                                    + revisionInfo.getExecutablePath());
                }
                return revisionInfo.getExecutablePath();
            }
            // 如果下载了chrome，就使用下载的chrome
            List<String> localRevisions = fetcher.localRevisions();
            if (CollKit.isNotEmpty(localRevisions)) {
                localRevisions.sort(Comparator.reverseOrder());
                Revision revisionInfo = fetcher.revisionInfo(localRevisions.get(0));
                if (!revisionInfo.isLocal()) {
                    throw new LaunchException(
                            "Tried to use PUPPETEER_CHROMIUM_REVISION env variable to launch browser but did not find executable at: "
                                    + revisionInfo.getExecutablePath());
                }
                return revisionInfo.getExecutablePath();
            }

            // 寻找可能存在的启动路径
            for (int i = 0; i < Builder.PROBABLE_CHROME_EXECUTABLE_PATH.length; i++) {
                chromeExecutable = Builder.PROBABLE_CHROME_EXECUTABLE_PATH[i];
                if (StringKit.isNotEmpty(chromeExecutable)) {
                    boolean assertDir = Builder.assertExecutable(chromeExecutable);
                    if (assertDir) {
                        return chromeExecutable;
                    }
                }
            }
        }

        Revision revision = fetcher.revisionInfo(this.preferredRevision);
        if (!revision.isLocal())
            throw new LaunchException(MessageFormat.format("Could not find browser revision {0}. Pleaze download a browser binary.", this.preferredRevision));
        return revision.getExecutablePath();
    }

    @Override
    public Browser connect(BrowserOptions options, String browserWSEndpoint, String browserURL, Transport transport) {
        final Connection connection;
        try {
            if (transport != null) {
                connection = new Connection("", transport, options.getSlowMo(), options.getConnectionOptions());
            } else if (StringKit.isNotEmpty(browserWSEndpoint)) {
                connection = new Connection(browserWSEndpoint, TransportFactory.create(browserWSEndpoint), options.getSlowMo(), options.getConnectionOptions());
            } else if (StringKit.isNotEmpty(browserURL)) {
                String connectionURL = getWSEndpoint(browserURL);
                connection = new Connection(connectionURL, TransportFactory.create(connectionURL), options.getSlowMo(), options.getConnectionOptions());
            } else {
                throw new IllegalArgumentException("Exactly one of browserWSEndpoint, browserURL or transport must be passed to puppeteer.connect");
            }
            JsonNode result = connection.send("Target.getBrowserContexts", null, true);

            JavaType javaType = Builder.OBJECTMAPPER.getTypeFactory().constructParametricType(ArrayList.class, String.class);
            List<String> browserContextIds;
            Function<Object, Object> closeFunction = (t) -> {
                connection.send("Browser.close", null, false);
                return null;
            };

            browserContextIds = Builder.OBJECTMAPPER.readerFor(javaType).readValue(result.get("browserContextIds"));
            return Browser.create(connection, browserContextIds, options.getIgnoreHTTPSErrors(), options.getViewport(), null, closeFunction);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 通过格式为 http://${host}:${port} 的地址发送 GET 请求获取浏览器的 Sockets 连接端点
     *
     * @param browserURL 浏览器地址
     * @return Sockets 连接端点
     * @throws IOException 请求出错
     */
    private String getWSEndpoint(String browserURL) throws IOException {
        URI uri = URI.create(browserURL).resolve("/json/version");
        URL url = uri.toURL();

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("BrowserURL: " + browserURL + ",HTTP " + responseCode);
        }
        String result = Builder.toString(conn.getInputStream());
        JsonNode jsonNode = Builder.OBJECTMAPPER.readTree(result);

        return jsonNode.get("webSocketDebuggerUrl").asText();
    }

    public boolean getIsPuppeteerCore() {
        return isPuppeteerCore;
    }

    public void setIsPuppeteerCore(boolean isPuppeteerCore) {
        this.isPuppeteerCore = isPuppeteerCore;
    }

    @Override
    public String executablePath() throws IOException {
        return resolveExecutablePath(null);
    }

    public String product() {
        return "chrome";
    }

}
