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
package org.aoju.lancia.nimble.network;

import org.aoju.lancia.kernel.page.Request;

/**
 * 当页面即将发送HTTP请求时触发
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class RequestWillPayload {

    /**
     * 请求标识
     */
    private String requestId;
    /**
     * 加载标识符。如果请求是从worker获取的，则为空字符串
     */
    private String loaderId;
    /**
     * 加载此请求的文档的URL
     */
    private String documentURL;
    /**
     * 请求数据
     */
    private Request request;
    /**
     * 时间戳
     */
    private long timestamp;
    /**
     * 时间戳
     */
    private long wallTime;
    /**
     * 请求启动程序
     */
    private Initiator initiator;
    /**
     * 重定向响应数据
     */
    private ResponsePayload redirectResponse;
    /**
     * 此资源的类型
     * "Document"|"Stylesheet"|"Image"|"Media"|"Font"|"Script"|"TextTrack"|"XHR"|"Fetch"|"EventSource"|"WebSocket"|"Manifest"|"SignedExchange"|"Ping"|"CSPViolationReport"|"Other";
     */
    private String type;
    /**
     * 框架标识
     */
    private String frameId;
    /**
     * 请求是否由用户手势发起。默认值为false
     */
    private boolean hasUserGesture;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getLoaderId() {
        return loaderId;
    }

    public void setLoaderId(String loaderId) {
        this.loaderId = loaderId;
    }

    public String getDocumentURL() {
        return documentURL;
    }

    public void setDocumentURL(String documentURL) {
        this.documentURL = documentURL;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getWallTime() {
        return wallTime;
    }

    public void setWallTime(long wallTime) {
        this.wallTime = wallTime;
    }

    public Initiator getInitiator() {
        return initiator;
    }

    public void setInitiator(Initiator initiator) {
        this.initiator = initiator;
    }

    public ResponsePayload getRedirectResponse() {
        return redirectResponse;
    }

    public void setRedirectResponse(ResponsePayload redirectResponse) {
        this.redirectResponse = redirectResponse;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrameId() {
        return frameId;
    }

    public void setFrameId(String frameId) {
        this.frameId = frameId;
    }

    public boolean getHasUserGesture() {
        return hasUserGesture;
    }

    public void setHasUserGesture(boolean hasUserGesture) {
        this.hasUserGesture = hasUserGesture;
    }

}
