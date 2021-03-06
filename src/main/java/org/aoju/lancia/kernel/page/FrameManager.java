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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.CollKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.lancia.Page;
import org.aoju.lancia.Variables;
import org.aoju.lancia.nimble.page.*;
import org.aoju.lancia.nimble.runtime.ExecutionCreatedPayload;
import org.aoju.lancia.nimble.runtime.ExecutionDescription;
import org.aoju.lancia.nimble.runtime.ExecutionDestroyedPayload;
import org.aoju.lancia.option.NavigateOption;
import org.aoju.lancia.worker.BrowserListener;
import org.aoju.lancia.worker.CDPSession;
import org.aoju.lancia.worker.EventEmitter;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 框架管理
 *
 * @author Kimi Liu
 * @version 1.2.2
 * @since JDK 1.8+
 */
public class FrameManager extends EventEmitter {

    private static final String UTILITY_WORLD_NAME = "__puppeteer_utility_world__";

    private final CDPSession client;
    private final Timeout timeout;
    private final NetworkManager networkManager;
    private final Map<String, Frame> frames;
    private final Map<Integer, ExecutionContext> contextIdToContext;
    private final Set<String> isolatedWorlds;
    private Page page;
    private Frame mainFrame;

    /**
     * 给导航到新的网页用
     */
    private CountDownLatch documentLatch;
    private CountDownLatch contentLatch;

    /**
     * 导航到新的网页的结果
     * "success" "timeout" "termination"
     */
    private String navigateResult;

    private boolean ensureNewDocumentNavigation;

    private String documentNavigationPromiseType = null;

    public FrameManager(CDPSession client, Page page, boolean ignoreHTTPSErrors, Timeout timeout) {
        super();
        this.client = client;
        this.page = page;
        this.networkManager = new NetworkManager(client, ignoreHTTPSErrors, this);
        this.timeout = timeout;
        this.frames = new HashMap<>();
        this.contextIdToContext = new HashMap<>();
        this.isolatedWorlds = new HashSet<>();
        // 1 Page.frameAttached
        BrowserListener<FrameAttachedPayload> frameAttachedListener = new BrowserListener<FrameAttachedPayload>() {
            @Override
            public void onBrowserEvent(FrameAttachedPayload event) {
                FrameManager frameManager = (FrameManager) this.getTarget();
                frameManager.onFrameAttached(event.getFrameId(), event.getParentFrameId());
            }
        };
        frameAttachedListener.setTarget(this);
        frameAttachedListener.setMothod("Page.frameAttached");
        this.client.addListener(frameAttachedListener.getMothod(), frameAttachedListener);
        // 2 Page.frameNavigated
        BrowserListener<FrameNavigatedPayload> frameNavigatedListener = new BrowserListener<FrameNavigatedPayload>() {
            @Override
            public void onBrowserEvent(FrameNavigatedPayload event) {
                FrameManager frameManager = (FrameManager) this.getTarget();
                frameManager.onFrameNavigated(event.getFrame());
            }
        };
        frameNavigatedListener.setTarget(this);
        frameNavigatedListener.setMothod("Page.frameNavigated");
        this.client.addListener(frameNavigatedListener.getMothod(), frameNavigatedListener);

        // 3 Page.navigatedWithinDocument
        BrowserListener<WithinDocumentPayload> navigatedWithinDocumentListener = new BrowserListener<WithinDocumentPayload>() {
            @Override
            public void onBrowserEvent(WithinDocumentPayload event) {
                FrameManager frameManager = (FrameManager) this.getTarget();
                frameManager.onFrameNavigatedWithinDocument(event.getFrameId(), event.getUrl());
            }
        };
        navigatedWithinDocumentListener.setTarget(this);
        navigatedWithinDocumentListener.setMothod("Page.navigatedWithinDocument");
        this.client.addListener(navigatedWithinDocumentListener.getMothod(), navigatedWithinDocumentListener);

        // 4 Page.frameDetached
        BrowserListener<FrameDetachedPayload> frameDetachedListener = new BrowserListener<FrameDetachedPayload>() {
            @Override
            public void onBrowserEvent(FrameDetachedPayload event) {
                FrameManager frameManager = (FrameManager) this.getTarget();
                frameManager.onFrameDetached(event.getFrameId());
            }
        };
        frameDetachedListener.setTarget(this);
        frameDetachedListener.setMothod("Page.frameDetached");
        this.client.addListener(frameDetachedListener.getMothod(), frameDetachedListener);

        // 5 Page.frameStoppedLoading
        BrowserListener<FrameStoppedPayload> frameStoppedLoadingListener = new BrowserListener<FrameStoppedPayload>() {
            @Override
            public void onBrowserEvent(FrameStoppedPayload event) {
                FrameManager frameManager = (FrameManager) this.getTarget();
                frameManager.onFrameStoppedLoading(event.getFrameId());
            }
        };
        frameStoppedLoadingListener.setTarget(this);
        frameStoppedLoadingListener.setMothod("Page.frameStoppedLoading");
        this.client.addListener(frameStoppedLoadingListener.getMothod(), frameStoppedLoadingListener);

        // 6 Runtime.executionContextCreated
        BrowserListener<ExecutionCreatedPayload> executionContextCreatedListener = new BrowserListener<ExecutionCreatedPayload>() {
            @Override
            public void onBrowserEvent(ExecutionCreatedPayload event) {
                FrameManager frameManager = (FrameManager) this.getTarget();
                frameManager.onExecutionContextCreated(event.getContext());
            }
        };
        executionContextCreatedListener.setTarget(this);
        executionContextCreatedListener.setMothod("Runtime.executionContextCreated");
        this.client.addListener(executionContextCreatedListener.getMothod(), executionContextCreatedListener);

        // 7 Runtime.executionContextDestroyed
        BrowserListener<ExecutionDestroyedPayload> executionContextDestroyedListener = new BrowserListener<ExecutionDestroyedPayload>() {
            @Override
            public void onBrowserEvent(ExecutionDestroyedPayload event) {
                FrameManager frameManager = (FrameManager) this.getTarget();
                frameManager.onExecutionContextDestroyed(event.getExecutionContextId());
            }
        };
        executionContextDestroyedListener.setTarget(this);
        executionContextDestroyedListener.setMothod("Runtime.executionContextDestroyed");
        this.client.addListener(executionContextDestroyedListener.getMothod(), executionContextDestroyedListener);

        // 8 Runtime.executionContextsCleared
        BrowserListener<Object> executionContextsClearedListener = new BrowserListener<Object>() {
            @Override
            public void onBrowserEvent(Object event) {
                FrameManager frameManager = (FrameManager) this.getTarget();
                frameManager.onExecutionContextsCleared();
            }
        };
        executionContextsClearedListener.setTarget(this);
        executionContextsClearedListener.setMothod("Runtime.executionContextsCleared");
        this.client.addListener(executionContextsClearedListener.getMothod(), executionContextsClearedListener);

        // 9 Page.lifecycleEvent
        BrowserListener<LifecycleEventPayload> lifecycleEventListener = new BrowserListener<LifecycleEventPayload>() {
            @Override
            public void onBrowserEvent(LifecycleEventPayload event) {
                FrameManager frameManager = (FrameManager) this.getTarget();
                frameManager.onLifecycleEvent(event);
            }
        };
        lifecycleEventListener.setTarget(this);
        lifecycleEventListener.setMothod("Page.lifecycleEvent");
        this.client.addListener(lifecycleEventListener.getMothod(), lifecycleEventListener);
    }

    private void onLifecycleEvent(LifecycleEventPayload event) {
        Frame frame = this.frames.get(event.getFrameId());
        if (frame == null)
            return;
        frame.onLifecycleEvent(event.getLoaderId(), event.getName());
        this.emit(Variables.Event.FRAME_MANAGER_LIFECYCLE_EVENT.getName(), frame);
    }

    private void onExecutionContextsCleared() {
        for (ExecutionContext context : this.contextIdToContext.values()) {
            if (context.getWorld() != null) {
                context.getWorld().setContext(null);
            }
        }
        this.contextIdToContext.clear();
    }

    private void onExecutionContextDestroyed(int executionContextId) {
        ExecutionContext context = this.contextIdToContext.get(executionContextId);
        if (context == null)
            return;
        this.contextIdToContext.remove(executionContextId);
        if (context.getWorld() != null) {
            context.getWorld().setContext(null);
        }
    }

    public ExecutionContext executionContextById(int contextId) {
        ExecutionContext context = this.contextIdToContext.get(contextId);
        Assert.isTrue(context != null, "INTERNAL ERROR: missing context with id = " + contextId);
        return context;
    }

    private void onExecutionContextCreated(ExecutionDescription contextPayload) {
        String frameId = contextPayload.getAuxData() != null ? contextPayload.getAuxData().getFrameId() : null;
        Frame frame = this.frames.get(frameId);
        DOMWorld world = null;
        if (frame != null) {
            if (contextPayload.getAuxData() != null && contextPayload.getAuxData().getIsDefault()) {
                world = frame.getMainWorld();
            } else if (contextPayload.getName().equals(UTILITY_WORLD_NAME) && !frame.getSecondaryWorld().hasContext()) {
                world = frame.getSecondaryWorld();
            }
        }
        if (contextPayload.getAuxData() != null && "isolated".equals(contextPayload.getAuxData().getType()))
            this.isolatedWorlds.add(contextPayload.getName());
        ExecutionContext context = new ExecutionContext(this.client, contextPayload, world);
        if (world != null)
            world.setContext(context);
        this.contextIdToContext.put(contextPayload.getId(), context);
    }

    /**
     * @param frameId frame id
     */
    private void onFrameStoppedLoading(String frameId) {
        Frame frame = this.frames.get(frameId);
        if (frame == null)
            return;
        frame.onLoadingStopped();
        this.emit(Variables.Event.FRAME_MANAGER_LIFECYCLE_EVENT.getName(), frame);
    }

    /**
     * @param frameId frame id
     */
    private void onFrameDetached(String frameId) {
        Frame frame = this.frames.get(frameId);
        if (frame != null)
            this.removeFramesRecursively(frame);
    }

    /**
     * @param frameId frame id
     * @param url     url
     */
    private void onFrameNavigatedWithinDocument(String frameId, String url) {
        Frame frame = this.frames.get(frameId);
        if (frame == null) {
            return;
        }
        frame.navigatedWithinDocument(url);
        this.emit(Variables.Event.FRAME_MANAGER_FRAME_NAVIGATED_WITHIN_DOCUMENT.getName(), frame);
        this.emit(Variables.Event.FRAME_MANAGER_FRAME_NAVIGATED.getName(), frame);
    }


    public void initialize() {

        this.client.send("Page.enable", null, false);
        /* @type Protocol.Page.getFrameTreeReturnValue*/
        JsonNode result = this.client.send("Page.getFrameTree", null, true);

        FrameTree frameTree;
        try {
            frameTree = Variables.OBJECTMAPPER.treeToValue(result.get("frameTree"), FrameTree.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        this.handleFrameTree(frameTree);

        Map<String, Object> params = new HashMap<>();
        params.put("enabled", true);
        this.client.send("Page.setLifecycleEventsEnabled", params, false);

        this.client.send("Runtime.enable", null, true);
        this.ensureIsolatedWorld(UTILITY_WORLD_NAME);
        this.networkManager.initialize();

    }

    private void ensureIsolatedWorld(String name) {
        if (this.isolatedWorlds.contains(name))
            return;
        this.isolatedWorlds.add(name);
        Map<String, Object> params = new HashMap<>();
        params.put("source", "//# sourceURL=" + ExecutionContext.EVALUATION_SCRIPT_URL);
        params.put("worldName", name);
        this.client.send("Page.addScriptToEvaluateOnNewDocument", params, true);
        this.frames().forEach(frame -> {
            Map<String, Object> param = new HashMap<>();
            param.put("frameId", frame.getId());
            param.put("grantUniveralAccess", true);
            param.put("worldName", name);
            this.client.send("Page.createIsolatedWorld", param, true);
        });
    }

    private void handleFrameTree(FrameTree frameTree) {
        if (StringKit.isNotEmpty(frameTree.getFrame().getParentId())) {
            this.onFrameAttached(frameTree.getFrame().getId(), frameTree.getFrame().getParentId());
        }

        this.onFrameNavigated(frameTree.getFrame());
        if (CollKit.isEmpty(frameTree.getChildFrames()))
            return;

        for (FrameTree child : frameTree.getChildFrames()) {
            this.handleFrameTree(child);
        }
    }

    /**
     * @param frameId       frame id
     * @param parentFrameId parent frame id
     */
    private void onFrameAttached(String frameId, String parentFrameId) {
        if (this.frames.get(frameId) != null)
            return;
        Assert.isTrue(StringKit.isNotEmpty(parentFrameId), "parentFrameId is null");
        Frame parentFrame = this.frames.get(parentFrameId);
        Frame frame = new Frame(this, this.client, parentFrame, frameId);
        this.frames.put(frame.getId(), frame);
        this.emit(Variables.Event.FRAME_MANAGER_FRAME_ATTACHED.getName(), frame);
    }

    /**
     * @param framePayload frame荷载
     */
    private void onFrameNavigated(FramePayload framePayload) {
        boolean isMainFrame = StringKit.isEmpty(framePayload.getParentId());
        Frame frame = isMainFrame ? this.mainFrame : this.frames.get(framePayload.getId());
        Assert.isTrue(isMainFrame || frame != null, "We either navigate top level or have old version of the navigated frame");

        // Detach all child frames first.
        if (frame != null) {
            if (CollKit.isNotEmpty(frame.getChildFrames())) {
                for (Frame childFrame : frame.getChildFrames()) {
                    this.removeFramesRecursively(childFrame);
                }
            }
        }

        if (isMainFrame) {
            if (frame != null) {
                this.frames.remove(frame.getId());
                frame.setId(framePayload.getId());
            } else {
                frame = new Frame(this, this.client, null, framePayload.getId());
            }
            this.frames.put(framePayload.getId(), frame);
            this.mainFrame = frame;
        }
        frame.navigated(framePayload);

        this.emit(Variables.Event.FRAME_MANAGER_FRAME_NAVIGATED.getName(), frame);
    }

    public List<Frame> frames() {
        if (this.frames.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(this.frames.values());
    }

    /**
     * @param childFrame 子frame
     */
    private void removeFramesRecursively(Frame childFrame) {
        if (CollKit.isNotEmpty(childFrame.getChildFrames())) {
            for (Frame frame : childFrame.getChildFrames()) {
                this.removeFramesRecursively(frame);
            }
        }
        childFrame.detach();
        this.frames.remove(childFrame.getId());
        this.emit(Variables.Event.FRAME_MANAGER_FRAME_DETACHED.getName(), childFrame);
    }

    public CDPSession getClient() {
        return client;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public Timeout getTimeoutSettings() {
        return timeout;
    }

    public NetworkManager getNetworkManager() {
        return networkManager;
    }

    public Map<String, Frame> getFrames() {
        return frames;
    }

    public Map<Integer, ExecutionContext> getContextIdToContext() {
        return contextIdToContext;
    }

    public Set<String> getIsolatedWorlds() {
        return isolatedWorlds;
    }

    public Frame getMainFrame() {
        return mainFrame;
    }

    public Response navigateFrame(Frame frame, String url, NavigateOption options, boolean isBlock) throws InterruptedException {
        String referrer;
        List<String> waitUntil;
        int timeout;
        if (options == null) {
            referrer = this.networkManager.extraHTTPHeaders().get("referer");
            waitUntil = new ArrayList<>();
            waitUntil.add("load");
            timeout = this.timeout.navigationTimeout();
        } else {
            if (StringKit.isEmpty(referrer = options.getReferer())) {
                referrer = this.networkManager.extraHTTPHeaders().get("referer");
            }
            if (CollKit.isEmpty(waitUntil = options.getWaitUntil())) {
                waitUntil = new ArrayList<>();
                waitUntil.add("load");
            }
            if ((timeout = options.getTimeout()) <= 0) {
                timeout = this.timeout.navigationTimeout();
            }
            assertNoLegacyNavigationOptions(waitUntil);
        }
        if (!isBlock) {
            Map<String, Object> params = new HashMap<>();
            params.put("url", url);
            params.put("referrer", referrer);
            params.put("frameId", frame.getId());
            this.client.send("Page.navigate", params, false);
            return null;
        }
        LifecycleWatcher watcher = new LifecycleWatcher(this, frame, waitUntil, timeout);
        long start = System.currentTimeMillis();
        try {
            this.ensureNewDocumentNavigation = navigate(this.client, url, referrer, frame.getId(), timeout);
            if (Variables.Result.SUCCESS.getResult().equals(navigateResult)) {
                if (this.ensureNewDocumentNavigation) {
                    documentNavigationPromiseType = "new";
                    if (watcher.newDocumentNavigationPromise() != null) {
                        return watcher.navigationResponse();
                    }
                } else {
                    documentNavigationPromiseType = "same";
                    if (watcher.sameDocumentNavigationPromise() != null) {
                        return watcher.navigationResponse();
                    }
                }
                this.navigateResult = Normal.EMPTY;
                this.documentLatch = new CountDownLatch(1);
                long end = System.currentTimeMillis();
                boolean await = documentLatch.await(timeout - (end - start), TimeUnit.MILLISECONDS);
                if (!await) {
                    throw new InstrumentException("Navigation timeout of " + timeout + " ms exceeded at " + url);
                }
                if (Variables.Result.SUCCESS.getResult().equals(navigateResult)) {
                    return watcher.navigationResponse();
                }
            }
            if (Variables.Result.TIMEOUT.getResult().equals(navigateResult)) {
                throw new InstrumentException("Navigation timeout of " + timeout + " ms exceeded at " + url);
            } else if (Variables.Result.TERMINATION.getResult().equals(navigateResult)) {
                throw new InstrumentException("Navigating frame was detached");
            } else {
                throw new InstrumentException("Unkown result " + navigateResult);
            }
        } finally {
            watcher.dispose();
        }
    }

    private boolean navigate(CDPSession client, String url, String referrer, String frameId, int timeout) {
        Map<String, Object> params = new HashMap<>();
        params.put("url", url);
        params.put("referrer", referrer);
        params.put("frameId", frameId);
        try {
            JsonNode response = client.send("Page.navigate", params, true, null, timeout);
            this.setNavigateResult("success");
            if (response == null) {
                return false;
            }
            if (response.get("errorText") != null) {
                throw new InstrumentException(response.get("errorText").toString() + " at " + url);
            }
            if (response.get("loaderId") != null) {
                return true;
            }

        } catch (InstrumentException e) {
            this.setNavigateResult("timeout");
        }
        return false;
    }

    public String getNavigateResult() {
        return navigateResult;
    }

    public void setNavigateResult(String navigateResult) {
        this.navigateResult = navigateResult;
    }

    public Frame getFrame(String frameId) {
        return this.frames.get(frameId);
    }

    public Frame frame(String frameId) {
        return this.frames.get(frameId);
    }

    public Response waitForFrameNavigation(Frame frame, NavigateOption options, CountDownLatch reloadLatch) {
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
            assertNoLegacyNavigationOptions(waitUntil);
        }

        this.documentNavigationPromiseType = "all";
        this.setNavigateResult(null);

        LifecycleWatcher watcher = new LifecycleWatcher(this, frame, waitUntil, timeout);
        if (watcher.newDocumentNavigationPromise() != null) {
            return watcher.navigationResponse();
        }
        if (watcher.sameDocumentNavigationPromise() != null) {
            return watcher.navigationResponse();
        }
        try {

            this.documentLatch = new CountDownLatch(1);

            //可以发出reload的信号
            if (reloadLatch != null) {
                reloadLatch.countDown();
            }

            boolean await = documentLatch.await(timeout, TimeUnit.MILLISECONDS);
            if (!await) {
                throw new InstrumentException("Navigation timeout of " + timeout + " ms exceeded");
            }
            if (Variables.Result.SUCCESS.getResult().equals(navigateResult)) {
                return watcher.navigationResponse();
            } else if (Variables.Result.TIMEOUT.getResult().equals(navigateResult)) {
                throw new InstrumentException("Navigation timeout of " + timeout + " ms exceeded");
            } else if (Variables.Result.TERMINATION.getResult().equals(navigateResult)) {
                throw new InstrumentException("Navigating frame was detached");
            } else {
                throw new InstrumentException("UnNokwn result " + navigateResult);
            }
        } catch (InterruptedException e) {
            throw new InstrumentException("UnNokwn result " + e.getMessage());
        } finally {
            watcher.dispose();
        }
    }

    private void assertNoLegacyNavigationOptions(List<String> waitUtil) {
        Assert.isTrue(!"networkidle".equals(waitUtil.get(0)), "ERROR: \"networkidle\" option is no longer supported. Use \"networkidle2\" instead");
    }

    public Frame mainFrame() {
        return this.mainFrame;
    }

    public NetworkManager networkManager() {
        return this.networkManager;
    }

    public String getDocumentNavigationPromiseType() {
        return documentNavigationPromiseType;
    }

    public void setDocumentNavigationPromiseType(String documentNavigationPromiseType) {
        this.documentNavigationPromiseType = documentNavigationPromiseType;
    }

    public CountDownLatch getDocumentLatch() {
        return documentLatch;
    }

    public void setDocumentLatch(CountDownLatch documentLatch) {
        this.documentLatch = documentLatch;
    }

    public CountDownLatch getContentLatch() {
        return contentLatch;
    }

    public void setContentLatch(CountDownLatch contentLatch) {
        this.contentLatch = contentLatch;
    }

}
