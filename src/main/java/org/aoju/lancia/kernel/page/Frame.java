/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.lancia.kernel.page;

import org.aoju.bus.core.lang.Normal;
import org.aoju.lancia.Builder;
import org.aoju.lancia.Variables;
import org.aoju.lancia.nimble.page.FramePayload;
import org.aoju.lancia.option.*;
import org.aoju.lancia.worker.CDPSession;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

/**
 * 框架信息
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class Frame {

    private String id;

    private String loaderId;

    private FrameManager frameManager;

    private CDPSession client;

    private Frame parentFrame;

    private String url;

    private boolean detached;

    private Set<String> lifecycleEvents;

    private DOMWorld mainWorld;

    private DOMWorld secondaryWorld;

    private Set<Frame> childFrames;

    private String name;

    private String navigationURL;

    public Frame(FrameManager frameManager, CDPSession client, Frame parentFrame, String frameId) {
        this.frameManager = frameManager;
        this.client = client;
        this.parentFrame = parentFrame;
        this.url = Normal.EMPTY;
        this.id = frameId;
        this.detached = false;
        this.loaderId = Normal.EMPTY;
        this.lifecycleEvents = new HashSet<>();
        this.mainWorld = new DOMWorld(frameManager, this, frameManager.getTimeout());
        this.secondaryWorld = new DOMWorld(frameManager, this, frameManager.getTimeout());
        this.childFrames = new CopyOnWriteArraySet<>();
        if (this.parentFrame != null)
            this.parentFrame.getChildFrames().add(this);
    }


    public Set<Frame> getChildFrames() {
        return this.childFrames;
    }

    public void setChildFrames(Set<Frame> childFrames) {
        this.childFrames = childFrames;
    }

    public void detach() {
        this.detached = true;
        this.mainWorld.detach();
        this.secondaryWorld.detach();
        if (this.parentFrame != null)
            this.parentFrame.childFrames.remove(this);
        this.parentFrame = null;
    }

    public void navigatedWithinDocument(String url) {
        this.url = url;
    }

    public Response waitForNavigation(NavigateOption options, CountDownLatch reloadLatch) {
        return this.frameManager.waitForFrameNavigation(this, options, reloadLatch);
    }

    public ExecutionContext executionContext() {
        return this.mainWorld.executionContext();
    }

    public JSHandle evaluateHandle(String pageFunction, List<Object> args) {
        return this.mainWorld.evaluateHandle(pageFunction, args);
    }

    public Object evaluate(String pageFunction, List<Object> args) {
        return this.mainWorld.evaluate(pageFunction, args);
    }

    public ElementHandle $(String selector) {
        return this.mainWorld.$(selector);
    }

    public List<ElementHandle> $x(String expression) {
        return this.mainWorld.$x(expression);
    }

    public Object $eval(String selector, String pageFunction, List<Object> args) {
        return this.mainWorld.$eval(selector, pageFunction, args);
    }

    public Object $$eval(String selector, String pageFunction, List<Object> args) {
        return this.mainWorld.$$eval(selector, pageFunction, args);
    }

    public List<ElementHandle> $$(String selector) {
        return this.mainWorld.$$(selector);
    }

    public ElementHandle addScriptTag(ScriptTagOption options) throws IOException {
        return this.mainWorld.addScriptTag(options);
    }

    public ElementHandle addStyleTag(StyleTagOption options) throws IOException {
        return this.mainWorld.addStyleTag(options);
    }

    public void click(String selector, ClickOption options, boolean isBlock) throws InterruptedException, ExecutionException {
        this.secondaryWorld.click(selector, options, isBlock);
    }

    public void focus(String selector) {
        this.secondaryWorld.focus(selector);
    }

    public void hover(String selector) {
        this.secondaryWorld.hover(selector);
    }

    public List<String> select(String selector, List<String> values) {
        return this.secondaryWorld.select(selector, values);
    }

    public void tap(String selector, boolean isBlock) {
        this.secondaryWorld.tap(selector, isBlock);
    }

    public void type(String selector, String text, int delay) throws InterruptedException {
        this.mainWorld.type(selector, text, delay);
    }

    /**
     * @param selectorOrFunctionOrTimeout 元素选择器，函数或者超时时间
     * @param options                     可配置等待选项
     * @param args                        functions时对应的function参数
     * @return 元素处理器
     * @throws InterruptedException 打断异常
     */
    public JSHandle waitFor(String selectorOrFunctionOrTimeout, WaitForOption options, List<Object> args) throws InterruptedException {
        String xPathPattern = "//";

        if (Builder.isFunction(selectorOrFunctionOrTimeout)) {
            return this.waitForFunction(selectorOrFunctionOrTimeout, options, args);
        } else if (Builder.isNumber(selectorOrFunctionOrTimeout)) {
            Thread.sleep(Long.parseLong(selectorOrFunctionOrTimeout));
            return null;
        } else {
            if (selectorOrFunctionOrTimeout.startsWith(xPathPattern)) {
                return this.waitForXPath(selectorOrFunctionOrTimeout, options);
            }
            return this.waitForSelector(selectorOrFunctionOrTimeout, options);
        }
    }

    public ElementHandle waitForSelector(String selector, WaitForOption options) throws InterruptedException {
        ElementHandle handle = this.secondaryWorld.waitForSelector(selector, options);
        if (handle == null)
            return null;
        ExecutionContext mainExecutionContext = this.mainWorld.executionContext();
        ElementHandle result = mainExecutionContext.adoptElementHandle(handle);
        handle.dispose();
        return result;
    }

    public JSHandle waitForFunction(String pageFunction, WaitForOption options, List<Object> args) throws InterruptedException {
        return this.mainWorld.waitForFunction(pageFunction, Builder.isFunction(pageFunction) ? Variables.PageEvaluateType.FUNCTION : Variables.PageEvaluateType.STRING, options, args);
    }

    public String title() {
        return this.secondaryWorld.title();
    }

    public void navigated(FramePayload framePayload) {
        this.name = framePayload.getName();
        this.url = framePayload.getUrl();
    }

    public JSHandle waitForXPath(String xpath, WaitForOption options) throws InterruptedException {
        ElementHandle handle = this.secondaryWorld.waitForXPath(xpath, options);
        if (handle == null)
            return null;
        ExecutionContext mainExecutionContext = this.mainWorld.executionContext();
        ElementHandle result = mainExecutionContext.adoptElementHandle(handle);
        handle.dispose();
        return result;
    }

    public void onLoadingStopped() {
        this.lifecycleEvents.add("DOMContentLoaded");
        this.lifecycleEvents.add("load");
    }

    public Response goTo(String url, NavigateOption options, boolean isBlock) throws InterruptedException {
        return this.frameManager.navigateFrame(this, url, options, isBlock);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLoaderId() {
        return loaderId;
    }

    public void setLoaderId(String loaderId) {
        this.loaderId = loaderId;
    }

    public String content() {
        return this.secondaryWorld.content();
    }

    public void setContent(String html, NavigateOption options) {
        this.secondaryWorld.setContent(html, options);
    }

    public FrameManager getFrameManager() {
        return frameManager;
    }

    public void setFrameManager(FrameManager frameManager) {
        this.frameManager = frameManager;
    }

    public CDPSession getClient() {
        return client;
    }

    public void setClient(CDPSession client) {
        this.client = client;
    }

    public Frame getParentFrame() {
        return parentFrame;
    }

    public void setParentFrame(Frame parentFrame) {
        this.parentFrame = parentFrame;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean getDetached() {
        return detached;
    }

    public Set<String> getLifecycleEvents() {
        return lifecycleEvents;
    }

    public void setLifecycleEvents(Set<String> lifecycleEvents) {
        this.lifecycleEvents = lifecycleEvents;
    }

    public DOMWorld getMainWorld() {
        return mainWorld;
    }

    public void setMainWorld(DOMWorld mainWorld) {
        this.mainWorld = mainWorld;
    }

    public DOMWorld getSecondaryWorld() {
        return secondaryWorld;
    }

    public void setSecondaryWorld(DOMWorld secondaryWorld) {
        this.secondaryWorld = secondaryWorld;
    }

    public void onLifecycleEvent(String loaderId, String name) {
        if ("init".equals(name)) {
            this.loaderId = loaderId;
            this.lifecycleEvents.clear();
        }
        this.lifecycleEvents.add(name);
    }

    public String getName() {
        if (this.name == null) {
            return Normal.EMPTY;
        }
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDetached() {
        return this.detached;
    }

    public void setDetached(boolean detached) {
        this.detached = detached;
    }

    public String url() {
        return this.url;
    }

    public String getNavigationURL() {
        return navigationURL;
    }

    public void setNavigationURL(String navigationURL) {
        this.navigationURL = navigationURL;
    }

    public String name() {
        return this.getName();
    }


    public Frame parentFrame() {
        return this.getParentFrame();
    }

    public Set<Frame> childFrames() {
        return this.getChildFrames();
    }

}
