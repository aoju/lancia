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
package org.aoju.lancia.kernel.page;

import org.aoju.bus.core.toolkit.CollKit;
import org.aoju.lancia.Builder;
import org.aoju.lancia.events.BrowserListenerWrapper;
import org.aoju.lancia.events.DefaultBrowserListener;
import org.aoju.lancia.events.Events;
import org.aoju.lancia.worker.exception.TerminateException;

import java.util.ArrayList;
import java.util.List;

/**
 * 生命周期
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class LifecycleWatcher {

    private final List<String> expectedLifecycle = new ArrayList<>();

    private FrameManager frameManager;

    private Frame frame;

    private int timeout;

    private Request navigationRequest;

    private List<BrowserListenerWrapper> eventListeners;

    private String initialLoaderId;

    private boolean hasSameDocumentNavigation;


    private Object lifecyclePromise = null;
    private Object sameDocumentNavigationPromise = null;
    private Object newDocumentNavigationPromise = null;


    public LifecycleWatcher() {
        super();
    }

    public LifecycleWatcher(FrameManager frameManager, Frame frame, List<String> waitUntil, int timeout) {
        super();
        this.frameManager = frameManager;
        this.frame = frame;
        this.initialLoaderId = frame.getLoaderId();
        this.timeout = timeout;
        this.navigationRequest = null;
        waitUntil.forEach(value -> {
            if ("domcontentloaded".equals(value)) {
                this.expectedLifecycle.add("DOMContentLoaded");
            } else if ("networkidle0".equals(value)) {
                this.expectedLifecycle.add("networkIdle");
            } else if ("networkidle2".equals(value)) {
                this.expectedLifecycle.add("networkAlmostIdle");
            } else if ("load".equals(value)) {
                this.expectedLifecycle.add("load");
            } else {
                throw new IllegalArgumentException("Unknown value for options.waitUntil: " + value);
            }

        });

        this.eventListeners = new ArrayList<>();
        DefaultBrowserListener<Object> disconnecteListener = new DefaultBrowserListener<Object>() {
            @Override
            public void onBrowserEvent(Object event) {
                LifecycleWatcher watcher = (LifecycleWatcher) this.getTarget();
                watcher.terminate(new TerminateException("Navigation failed because browser has disconnected!"));
            }
        };
        disconnecteListener.setTarget(this);
        disconnecteListener.setMethod(Events.CDPSESSION_DISCONNECTED.getName());

        DefaultBrowserListener<Object> lifecycleEventListener = new DefaultBrowserListener<Object>() {
            @Override
            public void onBrowserEvent(Object event) {
                LifecycleWatcher watcher = (LifecycleWatcher) this.getTarget();
                watcher.checkLifecycleComplete();
            }
        };
        lifecycleEventListener.setTarget(this);
        lifecycleEventListener.setMethod(Events.FRAME_MANAGER_LIFECYCLE_EVENT.getName());

        DefaultBrowserListener<Frame> documentListener = new DefaultBrowserListener<Frame>() {
            @Override
            public void onBrowserEvent(Frame event) {
                LifecycleWatcher watcher = (LifecycleWatcher) this.getTarget();
                watcher.navigatedWithinDocument(event);
            }
        };
        documentListener.setTarget(this);
        documentListener.setMethod(Events.FRAME_MANAGER_FRAME_NAVIGATED_WITHIN_DOCUMENT.getName());

        DefaultBrowserListener<Frame> detachedListener = new DefaultBrowserListener<Frame>() {
            @Override
            public void onBrowserEvent(Frame event) {
                LifecycleWatcher watcher = (LifecycleWatcher) this.getTarget();
                watcher.onFrameDetached(event);
            }
        };
        detachedListener.setTarget(this);
        detachedListener.setMethod(Events.FRAME_MANAGER_FRAME_DETACHED.getName());

        DefaultBrowserListener<Request> requestListener = new DefaultBrowserListener<Request>() {
            @Override
            public void onBrowserEvent(Request event) {
                LifecycleWatcher watcher = (LifecycleWatcher) this.getTarget();
                watcher.onRequest(event);
            }
        };
        requestListener.setTarget(this);
        requestListener.setMethod(Events.NETWORK_MANAGER_REQUEST.getName());
        eventListeners.add(Builder.addEventListener(this.frameManager.getClient(), disconnecteListener.getMethod(), disconnecteListener));
        eventListeners.add(Builder.addEventListener(this.frameManager, lifecycleEventListener.getMethod(), lifecycleEventListener));
        eventListeners.add(Builder.addEventListener(frameManager, documentListener.getMethod(), documentListener));
        eventListeners.add(Builder.addEventListener(frameManager, detachedListener.getMethod(), detachedListener));
        eventListeners.add(Builder.addEventListener(frameManager.getNetworkManager(), requestListener.getMethod(), requestListener));
        this.checkLifecycleComplete();
    }

    public Object sameDocumentNavigationPromise() {
        return this.sameDocumentNavigationPromise;
    }

    public Object newDocumentNavigationPromise() {
        return this.newDocumentNavigationPromise;
    }

    public void lifecycleCallback() {
        this.lifecyclePromise = new Object();
        if (this.frameManager.getContentLatch() != null) {
            this.frameManager.setNavigateResult("Content-success");
            this.frameManager.getContentLatch().countDown();
        }
    }

    private void onFrameDetached(Frame frame) {
        if (this.frame.equals(frame)) {
            terminationCallback();
            return;
        }
        this.checkLifecycleComplete();
    }

    private void onRequest(Request request) {
        if (request.frame() != this.frame || !request.isNavigationRequest())
            return;
        this.navigationRequest = request;
    }

    public void navigatedWithinDocument(Frame frame) {
        if (this.frame != frame)
            return;
        this.hasSameDocumentNavigation = true;
        this.checkLifecycleComplete();
    }

    private void checkLifecycleComplete() {
        // We expect navigation to commit.
        if (!checkLifecycle(this.frame, this.expectedLifecycle)) return;
        this.lifecycleCallback();
        if (this.frame.getLoaderId().equals(this.initialLoaderId) && !this.hasSameDocumentNavigation)
            return;
        if (this.hasSameDocumentNavigation)
            this.sameDocumentNavigationCompleteCallback();
        if (!this.frame.getLoaderId().equals(this.initialLoaderId))
            this.newDocumentNavigationCompleteCallback();
    }

    /**
     * @param frame             frame
     * @param expectedLifecycle 生命周期集合
     * @return boolean
     */
    private boolean checkLifecycle(Frame frame, List<String> expectedLifecycle) {
        if (CollKit.isNotEmpty(expectedLifecycle)) {
            for (String event : expectedLifecycle) {
                if (!frame.getLifecycleEvents().contains(event)) return false;
            }
        }
        if (CollKit.isNotEmpty(frame.childFrames())) {
            for (Frame child : frame.childFrames()) {
                if (!checkLifecycle(child, expectedLifecycle)) return false;
            }
        }
        return true;
    }

    public Object lifecyclePromise() {
        return this.lifecyclePromise;
    }

    private void terminate(TerminateException e) {
        terminationCallback();
    }

    public void terminationCallback() {
        setNavigateResult("termination");
    }

    public String createTimeoutPromise() {
        return null;
    }

    public void dispose() {
        Builder.removeEventListeners(this.eventListeners);
    }

    public Response navigationResponse() {
        return this.navigationRequest != null ? this.navigationRequest.response() : null;
    }

    public void newDocumentNavigationCompleteCallback() {
        this.newDocumentNavigationPromise = new Object();
        if ("new".equals(this.frameManager.getDocumentNavigationPromiseType()) || "all".equals(this.frameManager.getDocumentNavigationPromiseType()))
            setNavigateResult("success");
    }

    public void sameDocumentNavigationCompleteCallback() {
        this.sameDocumentNavigationPromise = new Object();
        if ("same".equals(this.frameManager.getDocumentNavigationPromiseType()) || "all".equals(this.frameManager.getDocumentNavigationPromiseType()))
            setNavigateResult("success");
    }

    private void setNavigateResult(String result) {
        if (this.frameManager.getDocumentLatch() != null && !"Content-success".equals(result)) {
            this.frameManager.setNavigateResult(result);
            this.frameManager.getDocumentLatch().countDown();
        }
    }

}

