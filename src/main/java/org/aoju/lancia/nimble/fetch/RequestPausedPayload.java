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
package org.aoju.lancia.nimble.fetch;

import org.aoju.lancia.nimble.network.RequestPayload;

import java.util.List;

/**
 * Issued when the domain is enabled and the request URL matches the
 * specified filter. The request is paused until the client responds
 * with one of continueRequest, failRequest or fulfillRequest.
 * The stage of the request can be determined by presence of responseErrorReason
 * and responseStatusCode -- the request is at the response stage if either
 * of these fields is present and in the request stage otherwise.
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class RequestPausedPayload {

    /**
     * Each request the page makes will have a unique id.
     */
    private String requestId;
    /**
     * The details of the request.
     */
    private RequestPayload request;
    /**
     * The id of the frame that initiated the request.
     */
    private String frameId;
    /**
     * How the requested resource will be used.
     */
    private String resourceType;
    /**
     * Response error if intercepted at response stage.
     * "Failed"|"Aborted"|"TimedOut"|"AccessDenied"|"ConnectionClosed"|"ConnectionReset"|"ConnectionRefused"|"ConnectionAborted"|"ConnectionFailed"|"NameNotResolved"|"InternetDisconnected"|"AddressUnreachable"|"BlockedByClient"|"BlockedByResponse";
     */
    private String responseErrorReason;
    /**
     * Response code if intercepted at response stage.
     */
    private int responseStatusCode;
    /**
     * Response headers if intercepted at the response stage.
     */
    private List<HeaderEntry> responseHeaders;
    /**
     * If the intercepted request had a corresponding Network.requestWillBeSent event fired for it,
     * then this networkId will be the same as the requestId present in the requestWillBeSent event.
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
