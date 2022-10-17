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
package org.aoju.lancia.events;

/**
 * 要监听的事件的名字枚举类
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public enum Events {

    PAGE_CLOSE("close"),
    PAGE_CONSOLE("console"),
    PAGE_DIALOG("dialog"),
    PAGE_DOMContentLoaded("domcontentloaded"),
    PAGE_ERROR("error"),
    PAGE_PageError("pageerror"),
    PAGE_REQUEST("request"),
    PAGE_RESPONSE("response"),
    PAGE_REQUESTFAILED("requestfailed"),
    PAGE_REQUESTFINISHED("requestfinished"),
    PAGE_FRAMEATTACHED("frameattached"),
    PAGE_FRAMEDETACHED("framedetached"),
    PAGE_FRAMENAVIGATED("framenavigated"),
    PAGE_LOAD("load"),
    PAGE_METRICS("metrics"),
    PAGE_POPUP("popup"),
    PAGE_WORKERCREATED("workercreated"),
    PAGE_WORKERDESTROYED("workerdestroyed"),

    BROWSER_TARGETCREATED("targetcreated"),
    BROWSER_TARGETDESTROYED("targetdestroyed"),
    BROWSER_TARGETCHANGED("targetchanged"),
    BROWSER_DISCONNECTED("disconnected"),

    BROWSERCONTEXT_TARGETCREATED("targetcreated"),
    BROWSERCONTEXT_TARGETDESTROYED("targetdestroyed"),
    BROWSERCONTEXT_TARGETCHANGED("targetchanged"),

    NETWORK_MANAGER_REQUEST("Events.NetworkManager.Request"),
    NETWORK_MANAGER_RESPONSE("Events.NetworkManager.Response"),
    NETWORK_MANAGER_REQUEST_FAILED("Events.NetworkManager.RequestFailed"),
    NETWORK_MANAGER_REQUEST_FINISHED("Events.NetworkManager.RequestFinished"),

    FRAME_MANAGER_FRAME_ATTACHED("Events.FrameManager.FrameAttached"),
    FRAME_MANAGER_FRAME_NAVIGATED("Events.FrameManager.FrameNavigated"),
    FRAME_MANAGER_FRAME_DETACHED("Events.FrameManager.FrameDetached"),
    FRAME_MANAGER_LIFECYCLE_EVENT("Events.FrameManager.LifecycleEvent"),
    FRAME_MANAGER_FRAME_NAVIGATED_WITHIN_DOCUMENT("Events.FrameManager.FrameNavigatedWithinDocument"),
    FRAME_MANAGER_EXECUTION_CONTEXTCREATED("Events.FrameManager.ExecutionContextCreated"),
    FRAME_MANAGER_EXECUTION_CONTEXTDESTROYED("Events.FrameManager.ExecutionContextDestroyed"),

    CONNECTION_DISCONNECTED("Events.Connection.Disconnected"),
    CDPSESSION_DISCONNECTED("Events.CDPSession.Disconnected");

    private String name;

    Events(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
