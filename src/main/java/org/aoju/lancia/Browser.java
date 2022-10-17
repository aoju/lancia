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

import com.alibaba.fastjson.JSONObject;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.toolkit.CollKit;
import org.aoju.bus.core.toolkit.ObjectKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.health.Platform;
import org.aoju.bus.logger.Logger;
import org.aoju.lancia.events.DefaultBrowserListener;
import org.aoju.lancia.events.EventEmitter;
import org.aoju.lancia.events.EventHandler;
import org.aoju.lancia.events.Events;
import org.aoju.lancia.kernel.browser.Context;
import org.aoju.lancia.kernel.browser.Fetcher;
import org.aoju.lancia.kernel.page.Target;
import org.aoju.lancia.kernel.page.TargetInfo;
import org.aoju.lancia.kernel.page.TaskQueue;
import org.aoju.lancia.kernel.page.Viewport;
import org.aoju.lancia.nimble.target.TargetCreatedPayload;
import org.aoju.lancia.nimble.target.TargetDestroyedPayload;
import org.aoju.lancia.nimble.target.TargetInfoChangedPayload;
import org.aoju.lancia.option.ChromeArgOptions;
import org.aoju.lancia.option.LaunchOptions;
import org.aoju.lancia.option.LaunchOptionsBuilder;
import org.aoju.lancia.worker.Connection;
import org.aoju.lancia.worker.exception.TimeoutException;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 浏览器实例
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class Browser extends EventEmitter {

    /**
     * 当前实例
     */
    public static Browser INSTANCE;
    /**
     * 浏览器对应的socket包装类，用于发送和接受消息
     */
    private final Connection connection;
    /**
     * 是否忽略https错误
     */
    private final boolean ignoreHTTPSErrors;
    /**
     * 浏览器内的页面视图
     */
    private final Viewport viewport;
    /**
     * 当前浏览器内的所有页面，也包括浏览器自己，{@link Page}和 {@link Browser} 都属于target
     */
    private final Map<String, Target> targets;
    /**
     * 默认浏览器上下文
     */
    private final Context defaultContext;
    /**
     * 浏览器上下文
     */
    private final Map<String, Context> contexts;
    private final Process process;
    private final TaskQueue<String> screenshotTaskQueue;
    private final Function<Object, Object> closeCallback;

    public Browser(Connection connection, List<String> contextIds, boolean ignoreHTTPSErrors,
                   Viewport defaultViewport, Process process, Function<Object, Object> closeCallback) {
        super();
        this.ignoreHTTPSErrors = ignoreHTTPSErrors;
        this.viewport = defaultViewport;
        this.process = process;
        this.screenshotTaskQueue = new TaskQueue<>();
        this.connection = connection;
        if (closeCallback == null) {
            closeCallback = o -> null;
        }
        this.closeCallback = closeCallback;
        this.defaultContext = new Context(connection, this, "");
        this.contexts = new HashMap<>();
        if (CollKit.isNotEmpty(contextIds)) {
            for (String contextId : contextIds) {
                contexts.putIfAbsent(contextId, new Context(this.connection, this, contextId));
            }
        }
        this.targets = new ConcurrentHashMap<>();
        DefaultBrowserListener<Object> disconnectedLis = new DefaultBrowserListener<>() {
            @Override
            public void onBrowserEvent(Object event) {
                Browser browser = (Browser) this.getTarget();
                browser.emit(Events.BROWSER_DISCONNECTED.getName(), null);
            }
        };
        disconnectedLis.setTarget(this);
        disconnectedLis.setMethod(Events.CONNECTION_DISCONNECTED.getName());
        this.connection.addListener(disconnectedLis.getMethod(), disconnectedLis);

        DefaultBrowserListener<TargetCreatedPayload> targetCreatedLis = new DefaultBrowserListener<>() {
            @Override
            public void onBrowserEvent(TargetCreatedPayload event) {
                Browser browser = (Browser) this.getTarget();
                browser.targetCreated(event);
            }
        };
        targetCreatedLis.setTarget(this);
        targetCreatedLis.setMethod("Target.targetCreated");
        this.connection.addListener(targetCreatedLis.getMethod(), targetCreatedLis);

        DefaultBrowserListener<TargetDestroyedPayload> targetDestroyedLis = new DefaultBrowserListener<>() {
            @Override
            public void onBrowserEvent(TargetDestroyedPayload event) {
                Browser browser = (Browser) this.getTarget();
                browser.targetDestroyed(event);
            }
        };
        targetDestroyedLis.setTarget(this);
        targetDestroyedLis.setMethod("Target.targetDestroyed");
        this.connection.addListener(targetDestroyedLis.getMethod(), targetDestroyedLis);

        DefaultBrowserListener<TargetInfoChangedPayload> targetInfoChangedLis = new DefaultBrowserListener<>() {
            @Override
            public void onBrowserEvent(TargetInfoChangedPayload event) {
                Browser browser = (Browser) this.getTarget();
                browser.targetInfoChanged(event);
            }
        };
        targetInfoChangedLis.setTarget(this);
        targetInfoChangedLis.setMethod("Target.targetInfoChanged");
        this.connection.addListener(targetInfoChangedLis.getMethod(), targetInfoChangedLis);
    }

    /**
     * 创建一个浏览器
     *
     * @param connection        浏览器对应的websocket client包装类
     * @param contextIds        上下文id集合
     * @param ignoreHTTPSErrors 是否忽略https错误
     * @param viewport          视图
     * @param closeCallback     关闭浏览器的回调
     * @param process           浏览器进程
     * @return 浏览器
     */
    public static Browser create(Connection connection, List<String> contextIds, boolean ignoreHTTPSErrors, Viewport viewport, Process process, Function<Object, Object> closeCallback) {
        Browser browser = new Browser(connection, contextIds, ignoreHTTPSErrors, viewport, process, closeCallback);
        Map<String, Object> params = new HashMap<>();
        params.put("discover", true);
        connection.send("Target.setDiscoverTargets", params, false);
        return browser;
    }

    public static synchronized Browser newInstance() {
        if (ObjectKit.isEmpty(INSTANCE)) {
            return newInstance(null);
        }
        return INSTANCE;
    }

    /**
     * 设置浏览器参数
     */
    public static synchronized Browser newInstance(LaunchOptions options) {
        try {
            Fetcher.on();
        } catch (InterruptedException | ExecutionException | IOException e) {
            throw new RuntimeException(e);
        }
        if (ObjectKit.isNotEmpty(options)) {
            options = new LaunchOptionsBuilder()
                    .args(options.getArgs())
                    .headless(options.getHeadless())
                    .viewport(options.getViewport())
                    .ignoreHTTPSErrors(options.getIgnoreHTTPSErrors())
                    .slowMo(options.getSlowMo())
                    .build();
        } else {
            List<String> list = new ArrayList<>();
            list.add("--disable-gpu");
            list.add("--disable-dev-shm-usage");
            list.add("--no-first-run");
            list.add("--no-sandbox");
            list.add("--disable-setuid-sandbox");
            list.add("--no-zygote");
            list.add("--proxy-server='direct://'");
            list.add("--proxy-bypass-list=*");

            options = new LaunchOptionsBuilder()
                    .args(list)
                    .headless(true)
                    .viewport(null)
                    .ignoreHTTPSErrors(true)
                    .slowMo(250)
                    .build();
        }

        try {
            INSTANCE = Puppeteer.launch(options);
            INSTANCE = Puppeteer.connect(INSTANCE.wsEndpoint(), null, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LaunchOptions opts = options;
        INSTANCE.onDisconnected(b -> {
            String command = "ps -ef | grep chrome | grep -v \"grep\" | awk '{print $2}' | xargs kill -9";
            if (Platform.isWindows()) {
                command = "taskkill /f /im chrome.exe";
            }
            try {
                Process process = Runtime.getRuntime().exec(command);
                process.waitFor();
            } catch (Exception e) {
                Logger.error(e);
            }
            newInstance(opts);
        });
        return INSTANCE;
    }

    private void targetDestroyed(TargetDestroyedPayload event) {
        Target target = this.targets.remove(event.getTargetId());
        target.initializedCallback(false);
        target.closedCallback();
        if (target.waitInitializedPromise()) {
            this.emit(Events.BROWSER_TARGETDESTROYED.getName(), target);
            target.browserContext().emit(Events.BROWSER_TARGETDESTROYED.getName(), target);
        }

    }

    private void targetInfoChanged(TargetInfoChangedPayload event) {
        Target target = this.targets.get(event.getTargetInfo().getTargetId());
        Assert.isTrue(target != null, "target should exist before targetInfoChanged");
        String previousURL = target.url();
        boolean wasInitialized = target.getIsInitialized();
        target.targetInfoChanged(event.getTargetInfo());
        if (wasInitialized && !previousURL.equals(target.url())) {
            this.emit(Events.BROWSER_TARGETCHANGED.getName(), target);
            target.browserContext().emit(Events.BROWSERCONTEXT_TARGETCHANGED.getName(), target);
        }
    }

    public String wsEndpoint() {
        return this.connection.url();
    }

    /**
     * 获取浏览器的所有target
     *
     * @return 所有target
     */
    public List<Target> targets() {
        return this.targets.values().stream().filter(Target::getIsInitialized).collect(Collectors.toList());
    }

    public Process process() {
        return this.process;
    }

    public Context createIncognitoBrowserContext() {
        JSONObject result = this.connection.send("Target.createBrowserContext", null, true);
        String browserContextId = result.getString("browserContextId");
        Context context = new Context(this.connection, this, browserContextId);
        this.contexts.put(browserContextId, context);
        return context;
    }

    public void disposeContext(String contextId) {
        Map<String, Object> params = new HashMap<>();
        params.put("browserContextId", contextId);
        this.connection.send("Target.disposeBrowserContext", params, true);
        this.contexts.remove(contextId);
    }

    /**
     * 当前浏览器有target创建时会调用的方法
     *
     * @param event 创建的target具体信息
     */
    protected void targetCreated(TargetCreatedPayload event) {
        Context context;
        TargetInfo targetInfo = event.getTargetInfo();
        if (StringKit.isNotEmpty(targetInfo.getBrowserContextId()) && this.contexts().containsKey(targetInfo.getBrowserContextId())) {
            context = this.contexts().get(targetInfo.getBrowserContextId());
        } else {
            context = this.defaultBrowserContext();
        }
        Target target = new Target(targetInfo, context, () -> this.getConnection().createSession(targetInfo), this.getIgnoreHTTPSErrors(), this.getViewport(), this.screenshotTaskQueue);
        if (this.targets.get(targetInfo.getTargetId()) != null) {
            throw new RuntimeException("Target should not exist befor targetCreated");
        }
        this.targets.put(targetInfo.getTargetId(), target);
        if (target.waitInitializedPromise()) {
            this.emit(Events.BROWSER_TARGETCREATED.getName(), target);
            context.emit(Events.BROWSERCONTEXT_TARGETCREATED.getName(), target);
        }
    }

    /**
     * 浏览器启动时必须初始化一个target
     *
     * @param predicate target的断言
     * @param options   浏览器启动参数
     * @return target
     */
    public Target waitForTarget(Predicate<Target> predicate, ChromeArgOptions options) {
        int timeout = options.getTimeout();
        long base = System.currentTimeMillis();
        long now = 0;
        while (true) {
            long delay = timeout - now;
            if (delay <= 0) {
                break;
            }
            Target existingTarget = find(targets(), predicate);
            if (null != existingTarget) {
                return existingTarget;
            }
            now = System.currentTimeMillis() - base;
        }
        throw new TimeoutException("waiting for target failed: timeout " + options.getTimeout() + "ms exceeded");
    }

    /**
     * @return {!Target}
     */
    public Target target() {
        for (Target target : this.targets()) {
            if ("browser".equals(target.type())) {
                return target;
            }
        }
        return null;
    }

    /**
     * 返回BrowserContext集合
     *
     * @return BrowserContext集合
     */
    public Collection<Context> browserContexts() {
        Collection<Context> contexts = new ArrayList<>();
        contexts.add(this.defaultBrowserContext());
        contexts.addAll(this.contexts().values());
        return contexts;
    }

    public List<Page> pages() {
        return this.browserContexts().stream().flatMap(context -> context.pages().stream()).collect(Collectors.toList());
    }

    public String version() {
        JSONObject version = this.getVersion();
        return version.getString("product");
    }

    public String userAgent() {
        JSONObject version = this.getVersion();
        return version.getString("userAgent");
    }

    public void close() {
        this.closeCallback.apply(null);
        this.disconnect();
    }

    public void disconnect() {
        this.connection.dispose();
    }

    private JSONObject getVersion() {
        return this.connection.send("Browser.getVersion", null, true);
    }

    public boolean isConnected() {
        return !this.connection.getClosed();
    }

    private Target find(List<Target> targets, Predicate<Target> predicate) {
        if (CollKit.isNotEmpty(targets)) {
            for (Target target : targets) {
                if (predicate.test(target)) {
                    return target;
                }
            }
        }
        return null;
    }

    /**
     * 在当前浏览器上新建一个页面
     *
     * @return 新建页面
     */
    public Page newPage() {
        return this.defaultContext.newPage();
    }

    /**
     * 在当前浏览器上下文新建一个页面
     *
     * @param contextId 上下文id 如果为空，则使用默认上下文
     * @return 新建页面
     */
    public Page createPageInContext(String contextId) {
        Map<String, Object> params = new HashMap<>();
        params.put("url", "about:blank");
        if (StringKit.isNotEmpty(contextId)) {
            params.put("browserContextId", contextId);
        }
        JSONObject recevie = this.connection.send("Target.createTarget", params, true);
        if (recevie != null) {
            Target target = this.targets.get(recevie.getString(Builder.RECV_MESSAGE_TARFETINFO_TARGETID_PROPERTY));
            Assert.isTrue(target.waitInitializedPromise(), "Failed to create target for page");
            return target.page();
        } else {
            throw new RuntimeException("can't create new page: ");
        }
    }

    /**
     * 监听浏览器事件disconnected
     * 浏览器一共有四种事件
     * method ="disconnected","targetchanged","targetcreated","targetdestroyed"
     *
     * @param handler 事件处理器
     */
    public void onDisconnected(EventHandler<Object> handler) {
        this.on(Events.BROWSER_DISCONNECTED.getName(), handler);
    }

    /**
     * 监听浏览器事件targetchanged
     * 浏览器一共有四种事件
     * method ="disconnected","targetchanged","targetcreated","targetdestroyed"
     *
     * @param handler 事件处理器
     */
    public void onTargetchanged(EventHandler<Target> handler) {
        this.on(Events.BROWSER_TARGETCHANGED.getName(), handler);
    }

    /**
     * 监听浏览器事件targetcreated
     * 浏览器一共有四种事件
     * method ="disconnected","targetchanged","targetcreated","targetdestroyed"
     *
     * @param handler 事件处理器
     */
    public void onTargetcreated(EventHandler<Target> handler) {
        this.on(Events.BROWSER_TARGETCREATED.getName(), handler);
    }

    /**
     * 监听浏览器事件targetcreated
     * 浏览器一共有四种事件
     * method ="disconnected","targetchanged","targetcreated","targetdestroyed"
     *
     * @param handler 事件处理器
     */
    public void onTargetdestroyed(EventHandler<Target> handler) {
        this.on(Events.BROWSER_TARGETDESTROYED.getName(), handler);
    }

    public Map<String, Target> getTargets() {
        return this.targets;
    }

    public Map<String, Context> contexts() {
        return contexts;
    }

    public Context defaultBrowserContext() {
        return defaultContext;
    }

    protected Connection getConnection() {
        return connection;
    }

    private boolean getIgnoreHTTPSErrors() {
        return ignoreHTTPSErrors;
    }

    protected Viewport getViewport() {
        return viewport;
    }

}
