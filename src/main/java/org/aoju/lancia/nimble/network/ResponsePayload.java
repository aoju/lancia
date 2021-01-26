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

import java.util.Map;

/**
 * HTTP响应数据
 *
 * @author Kimi Liu
 * @version 1.2.2
 * @since JDK 1.8+
 */
public class ResponsePayload {

    /**
     * 响应的URL
     */
    private String url;
    /**
     * 响应状态代码
     */
    private int status;
    /**
     * 响应状态文本
     */
    private String statusText;
    /**
     * 响应头
     */
    private Map<String, String> headers;
    /**
     * 响应头文本
     */
    private String headersText;
    /**
     * 资源mimeType由浏览器确定
     */
    private String mimeType;
    /**
     * 实际通过网络传输的改进HTTP请求报头
     */
    private Map<String, Object> requestHeaders;
    /**
     * 请求头文本
     */
    private String requestHeadersText;
    /**
     * 指定此请求是否实际重用了物理连接
     */
    private boolean connectionReused;
    /**
     * 实际用于此请求的物理连接id
     */
    private int connectionId;
    /**
     * 远程IP地址
     */
    private String remoteIPAddress;
    /**
     * 远程端口
     */
    private int remotePort;
    /**
     * 指定从磁盘缓存提供请求
     */
    private boolean fromDiskCache;
    /**
     * 指定从ServiceWorker服务请求
     */
    private boolean fromServiceWorker;
    /**
     * 指定从预取缓存中提供请求
     */
    private boolean fromPrefetchCache;
    /**
     * 到目前为止，该请求收到的总字节数
     */
    private int encodedDataLength;
    /**
     * 给定请求的时间信息
     */
    private ResourceTiming timing;
    /**
     * 用于获取此请求的协议
     */
    private String protocol;
    /**
     * 请求资源的安全状态
     * "unknown"|"neutral"|"insecure"|"secure"|"info"|"insecure-broken"
     */
    private String securityState;
    /**
     * 请求的安全细节
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
