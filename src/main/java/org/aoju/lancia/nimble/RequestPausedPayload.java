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
package org.aoju.lancia.nimble;

import org.aoju.lancia.nimble.network.RequestPayload;

import java.util.List;

/**
 * 当域启用且请求URL匹配时发出
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class RequestPausedPayload {

    /**
     * 页面发出的每个请求都有一个惟一的id
     */
    private String requestId;
    /**
     * 请求的细节
     */
    private RequestPayload request;
    /**
     * 发起请求的框架的id.
     */
    private String frameId;
    /**
     * 请求的资源将如何使用
     */
    private String resourceType;
    /**
     * 如果在响应阶段被拦截，则会出现响应错误
     * "Failed"|"Aborted"|"TimedOut"|"AccessDenied"|"ConnectionClosed"|"ConnectionReset"|"ConnectionRefused"|"ConnectionAborted"|"ConnectionFailed"|"NameNotResolved"|"InternetDisconnected"|"AddressUnreachable"|"BlockedByClient"|"BlockedByResponse";
     */
    private String responseErrorReason;
    /**
     * 响应代码
     */
    private int responseStatusCode;
    /**
     * 响应头信息
     */
    private List<HeaderEntry> responseHeaders;
    /**
     * 如果拦截的请求有相应的网络
     * 为其触发requestWillBeSent事件，那么这个networkId将与requestWillBeSent事件中出现的requestId相同。
     */
    private String networkId;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public RequestPayload getRequest() {
        return request;
    }

    public void setRequest(RequestPayload request) {
        this.request = request;
    }

    public String getFrameId() {
        return frameId;
    }

    public void setFrameId(String frameId) {
        this.frameId = frameId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResponseErrorReason() {
        return responseErrorReason;
    }

    public void setResponseErrorReason(String responseErrorReason) {
        this.responseErrorReason = responseErrorReason;
    }

    public int getResponseStatusCode() {
        return responseStatusCode;
    }

    public void setResponseStatusCode(int responseStatusCode) {
        this.responseStatusCode = responseStatusCode;
    }

    public List<HeaderEntry> getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(List<HeaderEntry> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

}
