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
package org.aoju.lancia.nimble.network;

import java.util.Map;

/**
 * HTTP response data.
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class ResponsePayload {

    /**
     * Response URL. This URL can be different from CachedResource.url in case of redirect.
     */
    private String url;
    /**
     * HTTP response status code.
     */
    private int status;
    /**
     * HTTP response status text.
     */
    private String statusText;
    /**
     * HTTP response headers.
     */
    private Map<String, String> headers;
    /**
     * HTTP response headers text.
     */
    private String headersText;
    /**
     * Resource mimeType as determined by the browser.
     */
    private String mimeType;
    /**
     * Refined HTTP request headers that were actually transmitted over the network.
     */
    private Map<String, Object> requestHeaders;
    /**
     * HTTP request headers text.
     */
    private String requestHeadersText;
    /**
     * Specifies whether physical connection was actually reused for this request.
     */
    private boolean connectionReused;
    /**
     * Physical connection id that was actually used for this request.
     */
    private int connectionId;
    /**
     * Remote IP address.
     */
    private String remoteIPAddress;
    /**
     * Remote port.
     */
    private int remotePort;
    /**
     * Specifies that the request was served from the disk cache.
     */
    private boolean fromDiskCache;
    /**
     * Specifies that the request was served from the ServiceWorker.
     */
    private boolean fromServiceWorker;
    /**
     * Specifies that the request was served from the prefetch cache.
     */
    private boolean fromPrefetchCache;
    /**
     * Total number of bytes received for this request so far.
     */
    private int encodedDataLength;
    /**
     * Timing information for the given request.
     */
    private ResourceTiming timing;
    /**
     * Protocol used to fetch this request.
     */
    private String protocol;
    /**
     * Security state of the request resource.
     * "unknown"|"neutral"|"insecure"|"secure"|"info"|"insecure-broken"
     */
    private String securityState;
    /**
     * Security details for the request.
     */

    private SecurityDetailsPayload securityDetails;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getHeadersText() {
        return headersText;
    }

    public void setHeadersText(String headersText) {
        this.headersText = headersText;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Map<String, Object> getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(Map<String, Object> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public String getRequestHeadersText() {
        return requestHeadersText;
    }

    public void setRequestHeadersText(String requestHeadersText) {
        this.requestHeadersText = requestHeadersText;
    }

    public boolean getConnectionReused() {
        return connectionReused;
    }

    public void setConnectionReused(boolean connectionReused) {
        this.connectionReused = connectionReused;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }

    public String getRemoteIPAddress() {
        return remoteIPAddress;
    }

    public void setRemoteIPAddress(String remoteIPAddress) {
        this.remoteIPAddress = remoteIPAddress;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public boolean getFromDiskCache() {
        return fromDiskCache;
    }

    public void setFromDiskCache(boolean fromDiskCache) {
        this.fromDiskCache = fromDiskCache;
    }

    public boolean getFromServiceWorker() {
        return fromServiceWorker;
    }

    public void setFromServiceWorker(boolean fromServiceWorker) {
        this.fromServiceWorker = fromServiceWorker;
    }

    public boolean getFromPrefetchCache() {
        return fromPrefetchCache;
    }

    public void setFromPrefetchCache(boolean fromPrefetchCache) {
        this.fromPrefetchCache = fromPrefetchCache;
    }

    public int getEncodedDataLength() {
        return encodedDataLength;
    }

    public void setEncodedDataLength(int encodedDataLength) {
        this.encodedDataLength = encodedDataLength;
    }

    public ResourceTiming getTiming() {
        return timing;
    }

    public void setTiming(ResourceTiming timing) {
        this.timing = timing;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getSecurityState() {
        return securityState;
    }

    public void setSecurityState(String securityState) {
        this.securityState = securityState;
    }

    public SecurityDetailsPayload getSecurityDetails() {
        return securityDetails;
    }

    public void setSecurityDetails(SecurityDetailsPayload securityDetails) {
        this.securityDetails = securityDetails;
    }

}
