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
package org.aoju.lancia;

import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.lancia.kernel.Variables;
import org.aoju.lancia.kernel.browser.Fetcher;
import org.aoju.lancia.launch.ChromeLauncher;
import org.aoju.lancia.launch.FirefoxLauncher;
import org.aoju.lancia.option.*;
import org.aoju.lancia.worker.Transport;

import java.io.IOException;
import java.util.List;

/**
 * Puppeteer 也可以用来控制 Chrome 浏览器， 但它与绑定的 Chromium
 * 版本在一起使用效果最好。不能保证它可以与任何其他版本一起使用。谨慎地使用 executablePath 选项。 如果 Google
 * Chrome（而不是Chromium）是首选，一个 Chrome Canary 或 Dev Channel 版本是建议的
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class Puppeteer {

    private String productName = null;

    private Launcher launcher;

    private Variables variables = null;

    private String projectRoot = System.getProperty("user.dir");

    private String preferredRevision = Builder.VERSION;

    private boolean isPuppeteerCore;

    public Puppeteer() {

    }

    public Puppeteer(String projectRoot, String preferredRevision, boolean isPuppeteerCore, String productName) {
        this.projectRoot = projectRoot;
        this.preferredRevision = StringKit.isEmpty(preferredRevision) ? Builder.VERSION : preferredRevision;
        this.isPuppeteerCore = isPuppeteerCore;
        this.productName = productName;
    }

    /**
     * 以默认参数启动浏览器
     * launch Browser by default options
     *
     * @return 浏览器
     * @throws IOException 异常
     */
    public static Browser launch() throws IOException {
        return Puppeteer.rawLaunch();
    }

    public static Browser launch(boolean headless) throws IOException {
        return Puppeteer.rawLaunch(headless);
    }

    public static Browser launch(LaunchOptions options) throws IOException {
        Puppeteer puppeteer = new Puppeteer();
        return Puppeteer.rawLaunch(options, puppeteer);
    }

    private static Browser rawLaunch() throws IOException {
        return Puppeteer.rawLaunch(true);
    }

    private static Browser rawLaunch(boolean headless) throws IOException {
        Puppeteer puppeteer = new Puppeteer();
        return Puppeteer.rawLaunch(new LaunchOptionsBuilder().headless(headless).build(), puppeteer);
    }

    /**
     * 连接一个已经存在的浏览器实例
     * browserWSEndpoint、browserURL、transport有其中一个就行了
     * browserWSEndpoint:类似 UUID 的字符串，可通过{@link Browser#wsEndpoint()}获取
     * browserURL: 类似 localhost:8080 这个地址
     * transport: 之前已经创建好的 Transport
     *
     * @param options           连接的浏览器选项
     * @param browserWSEndpoint websocket http transport 三选一
     * @param browserURL        websocket http transport 三选一
     * @param transport         websocket http transport 三选一
     * @param product           谷歌还是火狐
     * @return 浏览器实例
     */
    private static Browser connect(BrowserOptions options, String browserWSEndpoint, String browserURL, Transport transport, String product) {
        Puppeteer puppeteer = new Puppeteer();

        if (StringKit.isNotEmpty(product))
            puppeteer.setProductName(product);
        adapterLauncher(puppeteer);
        return puppeteer.getLauncher().connect(options, browserWSEndpoint, browserURL, transport);
    }

    /**
     * 连接一个已经存在的 Browser 实例
     * browserWSEndpoint、browserURL、transport有其中一个就行了
     * browserWSEndpoint:类似 UUID 的字符串，可通过{@link Browser#wsEndpoint()}获取
     * browserURL: 类似 localhost:8080 这个地址
     * transport: 之前已经创建好的 Transport
     *
     * @param options           连接的浏览器选项
     * @param browserWSEndpoint websocket http transport 三选一
     * @param browserURL        websocket http transport 三选一
     * @param transport         websocket http transport 三选一
     * @return 浏览器实例
     */
    public static Browser connect(BrowserOptions options, String browserWSEndpoint, String browserURL, Transport transport) {
        return Puppeteer.connect(options, browserWSEndpoint, browserURL, transport, null);
    }

    /**
     * 连接一个已经存在的 Browser 实例
     * browserWSEndpoint、browserURL、transport有其中一个就行了
     * browserWSEndpoint:类似 UUID 的字符串，可通过{@link Browser#wsEndpoint()}获取
     * browserURL: 类似 localhost:8080 这个地址
     * transport: 之前已经创建好的 Transport
     *
     * @param browserWSEndpoint websocket http transport 三选一
     * @param browserURL        websocket http transport 三选一
     * @param transport         websocket http transport 三选一
     * @return 浏览器实例
     */
    public static Browser connect(String browserWSEndpoint, String browserURL, Transport transport) {
        return Puppeteer.connect(new BrowserOptions(), browserWSEndpoint, browserURL, transport, null);
    }

    /**
     * 连接一个已经存在的 Browser 实例
     * browserWSEndpoint:类似 UUID 的字符串，可通过{@link Browser#wsEndpoint()}获取
     * browserURL: 类似 localhost:8080 这个地址
     *
     * @param browserWSEndpointOrURL 一个Browser实例对应一个browserWSEndpoint
     * @return 浏览器实例
     */
    public static Browser connect(String browserWSEndpointOrURL) {
        if (browserWSEndpointOrURL.contains(":")) {
            return Puppeteer.connect(null, browserWSEndpointOrURL, null);
        } else {
            return Puppeteer.connect(browserWSEndpointOrURL, null, null);
        }
    }

    /**
     * 连接一个已经存在的 Browser 实例
     * transport: 之前已经创建好的 Transport
     *
     * @param transport websocket http transport 三选一
     * @return 浏览器实例
     */
    public static Browser connect(Transport transport) {
        return Puppeteer.connect(null, null, transport);
    }

    /**
     * The method launches a browser instance with given arguments. The browser will
     * be closed when the parent java process is closed.
     */
    private static Browser rawLaunch(LaunchOptions options, Puppeteer puppeteer) throws IOException {
        if (StringKit.isNotBlank(options.getProduct())) {
            puppeteer.setProductName(options.getProduct());
        }
        adapterLauncher(puppeteer);
        return puppeteer.getLauncher().launch(options);
    }

    /**
     * 适配chrome or firefox 浏览器
     */
    private static void adapterLauncher(Puppeteer puppeteer) {
        String productName;

        Variables variables;
        if (StringKit.isEmpty(productName = puppeteer.getProductName())
                && !puppeteer.getIsPuppeteerCore()) {
            if ((variables = puppeteer.getContext()) == null) {
                puppeteer.setContext(variables = System::getenv);
            }
            for (int i = 0; i < Builder.PRODUCT_ENV.length; i++) {
                String envProductName = Builder.PRODUCT_ENV[i];
                productName = variables.getEnv(envProductName);
                if (StringKit.isNotEmpty(productName)) {
                    puppeteer.setProductName(productName);
                    break;
                }
            }
        }
        if (StringKit.isEmpty(productName)) {
            productName = "chrome";
            puppeteer.setProductName(productName);
        }
        Launcher launcher;
        switch (productName) {
            case "firefox":
                launcher = new FirefoxLauncher(puppeteer.getIsPuppeteerCore());
            case "chrome":
            default:
                launcher = new ChromeLauncher(puppeteer.getProjectRoot(), puppeteer.getPreferredRevision(), puppeteer.getIsPuppeteerCore());
        }
        puppeteer.setLauncher(launcher);
    }

    /**
     * 返回默认的运行的参数
     *
     * @param options 可自己添加的选项
     * @return 默认参数集合
     */
    public List<String> defaultArgs(ChromeArgOptions options) {
        return this.getLauncher().defaultArgs(options);
    }

    public String executablePath() throws IOException {
        return this.getLauncher().executablePath();
    }

    public Fetcher createBrowserFetcher() {
        return new Fetcher(this.getProjectRoot(), new FetcherOptions());
    }

    public Fetcher createBrowserFetcher(FetcherOptions options) {
        return new Fetcher(this.getProjectRoot(), options);
    }

    private String getProductName() {
        return productName;
    }

    private void setProductName(String productName) {
        this.productName = productName;
    }

    private boolean getIsPuppeteerCore() {
        return isPuppeteerCore;
    }

    private Launcher getLauncher() {
        return launcher;
    }

    private void setLauncher(Launcher launcher) {
        this.launcher = launcher;
    }

    private Variables getContext() {
        return this.variables;
    }

    private void setContext(Variables variables) {
        this.variables = variables;
    }

    public String getProjectRoot() {
        return projectRoot;
    }

    public void setProjectRoot(String projectRoot) {
        this.projectRoot = projectRoot;
    }

    public String getPreferredRevision() {
        return preferredRevision;
    }

    public void setPreferredRevision(String preferredRevision) {
        this.preferredRevision = preferredRevision;
    }

}
