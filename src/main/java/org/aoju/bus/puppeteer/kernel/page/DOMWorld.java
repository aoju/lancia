/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
package org.aoju.bus.puppeteer.kernel.page;

import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.CollKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.puppeteer.Builder;
import org.aoju.bus.puppeteer.Variables;
import org.aoju.bus.puppeteer.option.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Kimi Liu
 * @version 6.1.3
 * @since JDK 1.8+
 */
public class DOMWorld {

    private FrameManager frameManager;

    private Frame frame;

    private Timeout timeout;

    private boolean detached;

    private Set<WaitTask> waitTasks;

    private ElementHandle documentPromise;

    private boolean hasContext;

    private ExecutionContext contextPromise;

    private CountDownLatch waitForContext = null;

    public DOMWorld() {
        super();
    }

    public DOMWorld(FrameManager frameManager, Frame frame, Timeout timeout) {
        super();
        this.frameManager = frameManager;
        this.frame = frame;
        this.timeout = timeout;
        this.documentPromise = null;
        this.contextPromise = null;
        this.setContext(null);
        this.waitTasks = new HashSet<>();
        this.detached = false;
        this.hasContext = false;
    }

    /**
     * @return {!Puppeteer.Frame}
     */
    public Frame frame() {
        return this.frame;
    }

    public String content() {
        return (String) this.evaluate("() => {\n" +
                "      let retVal = '';\n" +
                "      if (document.doctype)\n" +
                "        retVal = new XMLSerializer().serializeToString(document.doctype);\n" +
                "      if (document.documentElement)\n" +
                "        retVal += document.documentElement.outerHTML;\n" +
                "      return retVal;\n" +
                "    }", new ArrayList<>());
    }

    public void setContext(ExecutionContext context) {
        if (context != null) {
            this.contextResolveCallback(context);
            hasContext = true;
            for (WaitTask waitTask : this.waitTasks) {
                Builder.commonExecutor().submit(waitTask::rerun);
            }
        } else {
            this.documentPromise = null;
            this.hasContext = false;
        }
    }

    private void contextResolveCallback(ExecutionContext context) {
        this.contextPromise = context;
        if (this.waitForContext != null) {
            this.waitForContext.countDown();
        }
    }

    public boolean hasContext() {
        return hasContext;
    }

    public ExecutionContext executionContext() {
        if (this.detached)
            throw new RuntimeException(MessageFormat.format("Execution Context is not available in detached frame {0} (are you trying to evaluate?)", this.frame.getUrl()));
        if (this.contextPromise == null) {
            this.waitForContext = new CountDownLatch(1);
            try {
                boolean await = this.waitForContext.await(Variables.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
                if (!await) {
                    throw new InstrumentException("Wait for ExecutionContext time out");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return this.contextPromise;
    }

    public JSHandle evaluateHandle(String pageFunction, List<Object> args) {
        ExecutionContext context = this.executionContext();
        return (JSHandle) context.evaluateHandle(pageFunction, args);
    }

    public Object evaluate(String pageFunction, List<Object> args) {
        ExecutionContext context = this.executionContext();
        return context.evaluate(pageFunction, args);
    }

    public ElementHandle $(String selector) {
        ElementHandle document = this.document();
        return document.$(selector);
    }

    private ElementHandle document() {
        if (this.documentPromise != null)
            return this.documentPromise;
        ExecutionContext context = this.executionContext();
        JSHandle document = (JSHandle) context.evaluateHandle("document", null);
        this.documentPromise = document.asElement();
        return this.documentPromise;
    }

    public List<ElementHandle> $x(String expression) {
        ElementHandle document = this.document();
        return document.$x(expression);
    }

    public Object $eval(String selector, String pageFunction, List<Object> args) {
        ElementHandle document = this.document();
        return document.$eval(selector, pageFunction, args);
    }

    public Object $$eval(String selector, String pageFunction, List<Object> args) {
        ElementHandle document = this.document();
        return document.$$eval(selector, pageFunction, args);
    }

    public List<ElementHandle> $$(String selector) {
        ElementHandle document = this.document();
        return document.$$(selector);
    }

    public void setContent(String html, NavigateOption options) {
        List<String> waitUntil;
        int timeout;
        if (options == null) {
            waitUntil = new ArrayList<>();
            waitUntil.add("load");
            timeout = this.timeout.navigationTimeout();
        } else {
            if (CollKit.isEmpty(waitUntil = options.getWaitUntil())) {
                waitUntil = new ArrayList<>();
                waitUntil.add("load");
            }
            if ((timeout = options.getTimeout()) <= 0) {
                timeout = this.timeout.navigationTimeout();
            }
        }
        LifecycleWatcher watcher = new LifecycleWatcher(this.frameManager, this.frame, waitUntil, timeout);
        this.evaluate("(html) => {\n" +
                "      document.open();\n" +
                "      document.write(html);\n" +
                "      document.close();\n" +
                "    }", Arrays.asList(html));
        if (watcher.lifecyclePromise() != null) {
            return;
        }
        try {
            CountDownLatch latch = new CountDownLatch(1);
            this.frameManager.setContentLatch(latch);
            this.frameManager.setNavigateResult(null);
            boolean await = latch.await(timeout, TimeUnit.MILLISECONDS);
            if (await) {
                if (Variables.Result.CONTENT_SUCCESS.getResult().equals(this.frameManager.getNavigateResult())) {

                } else if (Variables.Result.TIMEOUT.getResult().equals(this.frameManager.getNavigateResult())) {
                    throw new InstrumentException("setContent timeout :" + html);
                } else if (Variables.Result.TERMINATION.getResult().equals(this.frameManager.getNavigateResult())) {
                    throw new InstrumentException("Navigating frame was detached");
                } else {
                    throw new InstrumentException("UnNokwn result " + this.frameManager.getNavigateResult());
                }
            } else {
                throw new InstrumentException("setContent timeout " + html);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            watcher.dispose();
        }
    }

    public ElementHandle addScriptTag(ScriptTagOption options) throws IOException {
        if (StringKit.isNotEmpty(options.getUrl())) {
            try {
                ExecutionContext context = this.executionContext();
                ElementHandle handle = (ElementHandle) context.evaluateHandle(addScriptUrl(), Arrays.asList(options.getUrl(), options.getType()));
                return handle.asElement();
            } catch (Exception e) {
                throw new RuntimeException("Loading script from " + options.getUrl() + " failed", e);
            }
        }
        if (StringKit.isNotEmpty(options.getPath())) {
            List<String> contents = Files.readAllLines(Paths.get(options.getPath()), StandardCharsets.UTF_8);
            String content = String.join("\n", contents) + "//# sourceURL=" + options.getPath().replaceAll("\n", Normal.EMPTY);
            ExecutionContext context = this.executionContext();
            ElementHandle evaluateHandle = (ElementHandle) context.evaluateHandle(addScriptContent(), Arrays.asList(content, options.getType()));
            return evaluateHandle.asElement();
        }
        if (StringKit.isNotEmpty(options.getContent())) {
            ExecutionContext context = this.executionContext();
            ElementHandle elementHandle = (ElementHandle) context.evaluateHandle(addScriptContent(), Arrays.asList(options.getContent(), options.getType()));
            return elementHandle.asElement();
        }
        throw new IllegalArgumentException("Provide an object with a `url`, `path` or `content` property");
    }

    private String addScriptContent() {
        return "function addScriptContent(content, type = 'text/javascript') {\n" +
                "    const script = document.createElement('script');\n" +
                "    script.type = type;\n" +
                "    script.text = content;\n" +
                "    let error = null;\n" +
                "    script.onerror = e => error = e;\n" +
                "    document.head.appendChild(script);\n" +
                "    if (error)\n" +
                "      throw error;\n" +
                "    return script;\n" +
                "  }";
    }

    private String addScriptUrl() {
        return "async function addScriptUrl(url, type) {\n" +
                "      const script = document.createElement('script');\n" +
                "      script.src = url;\n" +
                "      if (type)\n" +
                "        script.type = type;\n" +
                "      const promise = new Promise((res, rej) => {\n" +
                "        script.onload = res;\n" +
                "        script.onerror = rej;\n" +
                "      });\n" +
                "      document.head.appendChild(script);\n" +
                "      await promise;\n" +
                "      return script;\n" +
                "    }";
    }

    public ElementHandle addStyleTag(StyleTagOption options) throws IOException {
        if (options != null && StringKit.isNotEmpty(options.getUrl())) {
            ExecutionContext context = this.executionContext();
            ElementHandle handle = (ElementHandle) context.evaluateHandle(addStyleUrl(), Arrays.asList(options.getUrl()));
            return handle.asElement();
        }

        if (options != null && StringKit.isNotEmpty(options.getPath())) {
            List<String> contents = Files.readAllLines(Paths.get(options.getPath()), StandardCharsets.UTF_8);
            String content = String.join("\n", contents) + "/*# sourceURL=" + options.getPath().replaceAll("\n", Normal.EMPTY) + "*/";
            ExecutionContext context = this.executionContext();
            ElementHandle handle = (ElementHandle) context.evaluateHandle(addStyleContent(), Arrays.asList(content));
            return handle.asElement();
        }

        if (options != null && StringKit.isNotEmpty(options.getContent())) {
            ExecutionContext context = this.executionContext();
            ElementHandle handle = (ElementHandle) context.evaluateHandle(addStyleContent(), Arrays.asList(options.getContent()));
            return handle.asElement();
        }

        throw new IllegalArgumentException("Provide an object with a `url`, `path` or `content` property");
    }

    private String addStyleContent() {
        return "async function addStyleContent(content) {\n" +
                "      const style = document.createElement('style');\n" +
                "      style.type = 'text/css';\n" +
                "      style.appendChild(document.createTextNode(content));\n" +
                "      const promise = new Promise((res, rej) => {\n" +
                "        style.onload = res;\n" +
                "        style.onerror = rej;\n" +
                "      });\n" +
                "      document.head.appendChild(style);\n" +
                "      await promise;\n" +
                "      return style;\n" +
                "    }";
    }

    private String addStyleUrl() {
        return "async function addStyleUrl(url) {\n" +
                "      const link = document.createElement('link');\n" +
                "      link.rel = 'stylesheet';\n" +
                "      link.href = url;\n" +
                "      const promise = new Promise((res, rej) => {\n" +
                "        link.onload = res;\n" +
                "        link.onerror = rej;\n" +
                "      });\n" +
                "      document.head.appendChild(link);\n" +
                "      await promise;\n" +
                "      return link;\n" +
                "    }";
    }

    public void click(String selector, ClickOption options, boolean isBlock) throws InterruptedException {
        ElementHandle handle = this.$(selector);
        Assert.isTrue(handle != null, "No node found for selector: " + selector);
        if (isBlock) {
            handle.click(options, true);
            handle.dispose();
            return;
        }
        Builder.commonExecutor().submit(() -> {
            try {
                handle.click(options, true);
                handle.dispose();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        });
    }

    public void focus(String selector) {
        ElementHandle handle = this.$(selector);
        Assert.isTrue(handle != null, "No node found for selector: " + selector);
        handle.focus();
        handle.dispose();
    }

    public void hover(String selector) {
        ElementHandle handle = this.$(selector);
        Assert.isTrue(handle != null, "No node found for selector: " + selector);
        handle.hover();
        handle.dispose();
    }

    public List<String> select(String selector, List<String> values) {
        ElementHandle handle = this.$(selector);
        Assert.isTrue(handle != null, "No node found for selector: " + selector);
        List<String> result = handle.select(values);
        handle.dispose();
        return result;
    }

    public void tap(String selector, boolean isBlock) {
        ElementHandle handle = this.$(selector);
        Assert.isTrue(handle != null, "No node found for selector: " + selector);
        if (isBlock) {
            handle.tap();
            handle.dispose();
        } else {
            Builder.commonExecutor().submit(() -> {
                handle.tap();
                handle.dispose();
            });
        }
    }

    public void type(String selector, String text, int delay) throws InterruptedException {
        ElementHandle handle = this.$(selector);
        Assert.isTrue(handle != null, "No node found for selector: " + selector);
        handle.type(text, delay);
        handle.dispose();
    }

    public ElementHandle waitForSelector(String selector, WaitForOption options) throws InterruptedException {
        return this.waitForSelectorOrXPath(selector, false, options);
    }

    private ElementHandle waitForSelectorOrXPath(String selectorOrXPath, boolean isXPath, WaitForOption options) throws InterruptedException {
        boolean waitForVisible = false;
        boolean waitForHidden = false;
        int timeout = this.timeout.timeout();
        if (options != null) {
            waitForVisible = options.getVisible();
            waitForHidden = options.getHidden();
            if (options.getTimeout() > 0) {
                timeout = options.getTimeout();
            }
        }
        String polling = waitForVisible || waitForHidden ? "raf" : "mutation";
        String title = (isXPath ? "XPath" : "selector") + " " + "\"" + selectorOrXPath + "\"" + (waitForHidden ? " to be hidden" : Normal.EMPTY);

        QuerySelector queryHandlerAndSelector = Builder.getQueryHandlerAndSelector(selectorOrXPath, "(element, selector) =>\n" +
                "      element.querySelector(selector)");
        QueryHandler queryHandler = queryHandlerAndSelector.getQueryHandler();
        String updatedSelector = queryHandlerAndSelector.getUpdatedSelector();
        String predicate = "function predicate(selectorOrXPath, isXPath, waitForVisible, waitForHidden) {\n" +
                "            const node = isXPath\n" +
                "                ? document.evaluate(selectorOrXPath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue\n" +
                "                : predicateQueryHandler\n" +
                "                    ? predicateQueryHandler(document, selectorOrXPath)\n" +
                "                    : document.querySelector(selectorOrXPath);\n" +
                "            if (!node)\n" +
                "                return waitForHidden;\n" +
                "            if (!waitForVisible && !waitForHidden)\n" +
                "                return node;\n" +
                "            const element = node.nodeType === Node.TEXT_NODE\n" +
                "                ? node.parentElement\n" +
                "                : node;\n" +
                "            const style = window.getComputedStyle(element);\n" +
                "            const isVisible = style && style.visibility !== 'hidden' && hasVisibleBoundingBox();\n" +
                "            const success = waitForVisible === isVisible || waitForHidden === !isVisible;\n" +
                "            return success ? node : null;\n" +
                "            function hasVisibleBoundingBox() {\n" +
                "                const rect = element.getBoundingClientRect();\n" +
                "                return !!(rect.top || rect.bottom || rect.width || rect.height);\n" +
                "            }\n" +
                "        }";
        List<Object> args = new ArrayList<>();
        args.addAll(Arrays.asList(updatedSelector, isXPath, waitForVisible, waitForHidden));
        WaitTask waitTask = new WaitTask(this, predicate, queryHandler.queryOne(), Variables.PageEvaluateType.FUNCTION, title, polling, timeout, args);
        JSHandle handle = waitTask.getPromise();
        if (handle == null) {
            return null;
        }
        if (handle.asElement() == null) {
            handle.dispose();
            return null;
        }
        return handle.asElement();
    }

    public ElementHandle waitForXPath(String xpath, WaitForOption options) throws InterruptedException {
        return this.waitForSelectorOrXPath(xpath, true, options);
    }

    public String title() {
        return (String) this.evaluate("() => document.title", new ArrayList<>());
    }

    public JSHandle waitForFunction(String pageFunction, Variables.PageEvaluateType type, WaitForOption options, List<Object> args) throws InterruptedException {
        String polling = "raf";
        int timeout = this.timeout.timeout();
        if (StringKit.isNotEmpty(options.getPolling())) {
            polling = options.getPolling();
        }
        if (options.getTimeout() > 0) {
            timeout = options.getTimeout();
        }
        return new WaitTask(this, pageFunction, null, type, "function", polling, timeout, args).getPromise();
    }

    public void detach() {
        this.detached = true;
        for (WaitTask waitTask : this.waitTasks)
            waitTask.terminate(new RuntimeException("waitForFunction failed: frame got detached."));
    }

    public Set<WaitTask> getWaitTasks() {
        return waitTasks;
    }

}
