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

/**
 * 请求的时间信息
 *
 * @author Kimi Liu
 * @version 1.2.1
 * @since JDK 1.8+
 */
public class ResourceTiming {

    /**
     * 请求时间毫秒计算
     */
    private int requestTime;
    /**
     * 开始解析代理
     */
    private int proxyStart;
    /**
     * 结束解析代理
     */
    private int proxyEnd;
    /**
     * 已启动DNS地址解析
     */
    private int dnsStart;
    /**
     * 完成DNS地址解析
     */
    private int dnsEnd;
    /**
     * 开始连接到远程主机
     */
    private int connectStart;
    /**
     * 连接到远程主机
     */
    private int connectEnd;
    /**
     * 开始SSL握手
     */
    private int sslStart;
    /**
     * 结束SSL握手
     */
    private int sslEnd;
    /**
     * 开始启动ServiceWorker
     */
    private int workerStart;
    /**
     * 完成ServiceWorker启动
     */
    private int workerReady;
    /**
     * 开始发送请求
     */
    private int sendStart;
    /**
     * 结束发送请求
     */
    private int sendEnd;
    /**
     * 服务器开始推送请求的时间
     */
    private int pushStart;
    /**
     * 计算服务器完成推送请求的时间
     */
    private int pushEnd;
    /**
     * 完成响应头的接收
     */
    private int receiveHeadersEnd;

    public int getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(int requestTime) {
        this.requestTime = requestTime;
    }

    public int getProxyStart() {
        return proxyStart;
    }

    public void setProxyStart(int proxyStart) {
        this.proxyStart = proxyStart;
    }

    public int getProxyEnd() {
        return proxyEnd;
    }

    public void setProxyEnd(int proxyEnd) {
        this.proxyEnd = proxyEnd;
    }

    public int getDnsStart() {
        return dnsStart;
    }

    public void setDnsStart(int dnsStart) {
        this.dnsStart = dnsStart;
    }

    public int getDnsEnd() {
        return dnsEnd;
    }

    public void setDnsEnd(int dnsEnd) {
        this.dnsEnd = dnsEnd;
    }

    public int getConnectStart() {
        return connectStart;
    }

    public void setConnectStart(int connectStart) {
        this.connectStart = connectStart;
    }

    public int getConnectEnd() {
        return connectEnd;
    }

    public void setConnectEnd(int connectEnd) {
        this.connectEnd = connectEnd;
    }

    public int getSslStart() {
        return sslStart;
    }

    public void setSslStart(int sslStart) {
        this.sslStart = sslStart;
    }

    public int getSslEnd() {
        return sslEnd;
    }

    public void setSslEnd(int sslEnd) {
        this.sslEnd = sslEnd;
    }

    public int getWorkerStart() {
        return workerStart;
    }

    public void setWorkerStart(int workerStart) {
        this.workerStart = workerStart;
    }

    public int getWorkerReady() {
        return workerReady;
    }

    public void setWorkerReady(int workerReady) {
        this.workerReady = workerReady;
    }

    public int getSendStart() {
        return sendStart;
    }

    public void setSendStart(int sendStart) {
        this.sendStart = sendStart;
    }

    public int getSendEnd() {
        return sendEnd;
    }

    public void setSendEnd(int sendEnd) {
        this.sendEnd = sendEnd;
    }

    public int getPushStart() {
        return pushStart;
    }

    public void setPushStart(int pushStart) {
        this.pushStart = pushStart;
    }

    public int getPushEnd() {
        return pushEnd;
    }

    public void setPushEnd(int pushEnd) {
        this.pushEnd = pushEnd;
    }

    public int getReceiveHeadersEnd() {
        return receiveHeadersEnd;
    }

    public void setReceiveHeadersEnd(int receiveHeadersEnd) {
        this.receiveHeadersEnd = receiveHeadersEnd;
    }

}
