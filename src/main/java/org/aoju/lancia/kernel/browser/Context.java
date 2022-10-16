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

import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.lancia.Browser;
import org.aoju.lancia.Page;
import org.aoju.lancia.events.EventEmitter;
import org.aoju.lancia.events.EventHandler;
import org.aoju.lancia.events.Events;
import org.aoju.lancia.kernel.page.Target;
import org.aoju.lancia.option.ChromeArgOptions;
import org.aoju.lancia.worker.Connection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 浏览器上下文
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class Context extends EventEmitter {

    private static final Map<String, String> webPermissionToProtocol = new HashMap<>(32);

    static {
        webPermissionToProtocol.put("geolocation", "geolocation");
        webPermissionToProtocol.put("midi", "midi");
        webPermissionToProtocol.put("notifications", "notifications");
        webPermissionToProtocol.put("push", "push");
        webPermissionToProtocol.put("camera", "videoCapture");
        webPermissionToProtocol.put("microphone", "audioCapture");
        webPermissionToProtocol.put("background-sync", "backgroundSync");
        webPermissionToProtocol.put("ambient-light-sensor", "sensors");
        webPermissionToProtocol.put("accelerometer", "sensors");
        webPermissionToProtocol.put("gyroscope", "sensors");
        webPermissionToProtocol.put("magnetometer", "sensors");
        webPermissionToProtocol.put("accessibility-events", "accessibilityEvents");
        webPermissionToProtocol.put("clipboard-read", "clipboardRead");
        webPermissionToProtocol.put("payment-handler", "paymentHandler");
        webPermissionToProtocol.put("midi-sysex", "midiSysex");
    }

    /**
     * 浏览器对应的websocket client包装类，用于发送和接受消息
     */
    private Connection connection;
    /**
     * 浏览器上下文对应的浏览器，一个上下文只有一个浏览器，但是一个浏览器可能有多个上下文
     */
    private Browser browser;
    /**
     * 浏览器上下文id
     */
    private String id;

    public Context() {
        super();
    }

    public Context(Connection connection, Browser browser, String contextId) {
        super();
        this.connection = connection;
        this.browser = browser;
        this.id = contextId;
    }

    /**
     * 监听浏览器事件targetchanged
     * 浏览器一共有四种事件
     * method ="disconnected","targetchanged","targetcreated","targetdestroyed"
     *
     * @param handler 事件处理器
     */
    public void onTargetchanged(EventHandler<Target> handler) {
        this.on(Events.BROWSERCONTEXT_TARGETCHANGED.getName(), handler);
    }

    /**
     * 监听浏览器事件targetcreated
     * 浏览器一共有四种事件
     * method ="disconnected","targetchanged","targetcreated","targetdestroyed"
     *
     * @param handler 事件处理器
     */
    public void onTrgetcreated(EventHandler<Target> handler) {
        this.on(Events.BROWSERCONTEXT_TARGETCREATED.getName(), handler);
    }

    public void clearPermissionOverrides() {
        Map<String, Object> params = new HashMap<>();
        params.put("browserContextId", this.id);
        this.connection.send("Browser.resetPermissions", params, true);
    }

    public void close() {
        Assert.isTrue(StringKit.isNotEmpty(this.id), "Non-incognito profiles cannot be closed!");
        this.browser.disposeContext(this.id);
    }

    /**
     * @return {boolean}
     */
    public boolean isIncognito() {
        return StringKit.isNotEmpty(this.id);
    }

    public void overridePermissions(String origin, List<String> permissions) {
        permissions.replaceAll(item -> {
            String protocolPermission = webPermissionToProtocol.get(item);
            Assert.isTrue(protocolPermission != null, "Unknown permission: " + item);
            return protocolPermission;
        });
        Map<String, Object> params = new HashMap<>();
        params.put("origin", origin);
        params.put("browserContextId", this.id);
        params.put("permissions", permissions);
        this.connection.send("Browser.grantPermissions", params, true);
    }

    public List<Page> pages() {
        return this.targets().stream().filter(target -> "page".equals(target.type())).map(Target::page).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * @return 目标的集合
     */
    public List<Target> targets() {
        return this.browser.targets().stream().filter(target -> target.browserContext() == this).collect(Collectors.toList());
    }

    public Target waitForTarget(Predicate<Target> predicate, ChromeArgOptions options) {
        return this.browser.waitForTarget(target -> target.browserContext() == this && predicate.test(target), options);
    }

    public Browser browser() {
        return browser;
    }

    public Page newPage() {
        return browser.createPageInContext(this.id);
    }

}
