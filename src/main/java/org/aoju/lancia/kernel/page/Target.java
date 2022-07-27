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
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.lancia.Browser;
import org.aoju.lancia.Page;
import org.aoju.lancia.Builder;
import org.aoju.lancia.kernel.browser.Context;
import org.aoju.lancia.worker.CDPSession;
import org.aoju.lancia.worker.SessionFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 目标内容
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class Target {

    private Boolean initializedPromise;

    private CountDownLatch initializedCountDown;

    private TargetInfo targetInfo;

    private Context context;

    private boolean ignoreHTTPSErrors;

    private Viewport viewport;

    private TaskQueue<String> screenshotTaskQueue;

    private String targetId;

    private Page pagePromise;

    private Worker workerPromise;

    private boolean isInitialized;

    private SessionFactory sessionFactory;

    private String sessionId;

    private CountDownLatch isClosedPromiseLatch;

    public Target() {
        super();
    }

    public Target(TargetInfo targetInfo, Context context, SessionFactory sessionFactory, boolean ignoreHTTPSErrors, Viewport defaultViewport, TaskQueue<String> screenshotTaskQueue) {
        super();
        this.targetInfo = targetInfo;
        this.context = context;
        this.targetId = targetInfo.getTargetId();
        this.sessionFactory = sessionFactory;
        this.ignoreHTTPSErrors = ignoreHTTPSErrors;
        this.viewport = defaultViewport;
        this.screenshotTaskQueue = screenshotTaskQueue;
        this.pagePromise = null;
        this.workerPromise = null;
        this.isClosedPromiseLatch = new CountDownLatch(1);
        this.isInitialized = !"page".equals(this.targetInfo.getType()) || !StringKit.isEmpty(this.targetInfo.getUrl());
        if (isInitialized) {//初始化
            this.initializedPromise = this.initializedCallback(true);
        } else {
            this.initializedPromise = true;
        }
    }

    public CDPSession createCDPSession() {
        return this.sessionFactory.create();
    }

    public Worker worker() {
        if (!"service_worker".equals(this.targetInfo.getType()) && !"shared_worker".equals(this.targetInfo.getType()))
            return null;
        if (this.workerPromise == null) {
            synchronized (this) {
                if (this.workerPromise == null) {
                    CDPSession client = this.sessionFactory.create();
                    this.workerPromise = new Worker(client, this.targetInfo.getUrl(), (arg0, arg1, arg2) -> {
                    } /* consoleAPICalled */, (arg) -> {
                    } /* exceptionThrown */);
                }
            }

        }
        return this.workerPromise;
    }

    public void closedCallback() {
        if (pagePromise != null) {
            this.pagePromise.emit(Builder.Event.PAGE_CLOSE.getName(), null);
            this.pagePromise.setClosed(true);
        }
        this.isClosedPromiseLatch.countDown();
    }

    /**
     * 如果目标不是 "page" 或 "background_page" 类型，则返回 null。
     *
     * @return Page
     */
    public Page page() {
        String type;
        if (("page".equals(type = this.targetInfo.getType()) || "background_page".equals(type)) && this.pagePromise == null) {
            try {
                this.pagePromise = Page.create(this.sessionFactory.create(), this, this.ignoreHTTPSErrors, this.viewport, this.screenshotTaskQueue);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return this.pagePromise;
    }

    /**
     * 确定目标是怎么样的类型。 可以是 "page"，"background_page"，"service_worker"，"browser" 或 "其他"。
     *
     * @return 目标类型
     */
    public String type() {
        String type = this.targetInfo.getType();
        if ("page".equals(type) || "background_page".equals(type) || "service_worker".equals(type) || "shared_worker".equals(type) || "browser".equals(type)) {
            return type;
        }
        return "other";
    }

    public boolean initializedCallback(boolean success) {
        try {
            if (!success) {
                this.initializedPromise = false;
                return false;
            }
            Target opener = this.opener();
            if (opener == null || opener.getPagePromise() == null || "page".equals(this.type())) {
                this.initializedPromise = true;
                return true;
            }
            Page openerPage = opener.getPagePromise();
            if (openerPage.getListenerCount(Builder.Event.PAGE_POPUP.getName()) <= 0) {
                this.initializedPromise = true;
                return true;
            }
            Page pupopPage = this.page();
            pupopPage.emit(Builder.Event.PAGE_POPUP.getName(), pupopPage);
            this.initializedPromise = true;
            return true;
        } finally {
            if (initializedCountDown != null) {
                initializedCountDown.countDown();
                initializedCountDown = null;
            }
        }
    }

    public boolean waitInitializedPromise() {
        if (initializedPromise == null) {
            this.initializedCountDown = new CountDownLatch(1);
            try {
                initializedCountDown.await(Builder.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException("Wait for InitializedPromise fail:", e);
            }
        }
        return this.initializedPromise;
    }

    /**
     * 获取打开此目标的目标。 顶级目标返回null。
     *
     * @return Target
     */
    public Target opener() {
        String openerId = this.targetInfo.getOpenerId();
        if (StringKit.isEmpty(openerId)) {
            return null;
        }
        return this.browser().getTargets().get(openerId);
    }

    /**
     * 返回目标的url
     *
     * @return url
     */
    public String url() {
        return this.targetInfo.getUrl();
    }

    /**
     * 获取目标所属的浏览器。
     *
     * @return Browser
     */
    public Browser browser() {
        return this.context.browser();
    }

    public Page getPagePromise() {
        return pagePromise;
    }

    public void setPagePromise(Page pagePromise) {
        this.pagePromise = pagePromise;
    }

    public Worker getWorkerPromise() {
        return workerPromise;
    }

    public void setWorkerPromise(Worker workerPromise) {
        this.workerPromise = workerPromise;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public TargetInfo getTargetInfo() {
        return targetInfo;
    }

    public void setTargetInfo(TargetInfo targetInfo) {
        this.targetInfo = targetInfo;
    }

    /**
     * 目标所属的浏览器上下文。
     *
     * @return 浏览器上下文
     */
    public Context browserContext() {
        return context;
    }

    public void setBrowserContext(Context context) {
        this.context = context;
    }

    public boolean isIgnoreHTTPSErrors() {
        return ignoreHTTPSErrors;
    }

    public void setIgnoreHTTPSErrors(boolean ignoreHTTPSErrors) {
        this.ignoreHTTPSErrors = ignoreHTTPSErrors;
    }

    public Viewport getDefaultViewport() {
        return viewport;
    }

    public void setDefaultViewport(Viewport viewport) {
        this.viewport = viewport;
    }

    public boolean getIsInitialized() {
        return isInitialized;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void targetInfoChanged(TargetInfo targetInfo) {
        this.targetInfo = targetInfo;
        if (!this.isInitialized && (!"page".equals(this.targetInfo.getType()) || !Normal.EMPTY.equals(this.targetInfo.getUrl()))) {
            this.isInitialized = true;
            this.initializedCallback(true);
        }
    }

    public boolean WaiforisClosedPromise() throws InterruptedException {
        return this.isClosedPromiseLatch.await(Builder.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
    }

}
