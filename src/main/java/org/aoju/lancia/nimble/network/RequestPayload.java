/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 * HTTP请求数据
 *
 * @author Kimi Liu
 * @version 6.1.3
 * @since JDK 1.8+
 */
public class RequestPayload {
    /**
     * 请求网址
     */
    private String url;
    /**
     * 请求的URL的片段
     */
    private String urlFragment;
    /**
     * 请求方法
     */
    private String method;
    /**
     * 请求标头
     */
    private Map<String, String> headers;
    /**
     * POST请求数据
     */
    private String postData;
    /**
     * 当有POST请求数据时为True。请注意，当数据太长时，如果此标志为true，则可能仍会省略postData
     */
    private boolean hasPostData;
    /**
     * 请求的类型 "blockable"|"optionally-blockable"|"none"
     */
    private String mixedContentType;
    /**
     * 发送请求时资源请求的优先级 "VeryLow"|"Low"|"Medium"|"High"|"VeryHigh"
     */
    private String initialPriority;
    /**
     * 定义的请求的引用者策略 https://www.w3.org/TR/referrer-policy/
     * "unsafe-url"|"no-referrer-when-downgrade"|"no-referrer"|"origin"|
     * "origin-when-cross-origin"|"same-origin"|"strict-origin"|"strict-origin-when-cross-origin";
     */
    private String referrerPolicy;
    /**
     * 是否通过链接预加载加载
     */
    private boolean isLinkPreload;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrlFragment() {
        return urlFragment;
    }

    public void setUrlFragment(String urlFragment) {
        this.urlFragment = urlFragment;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getPostData() {
        return postData;
    }

    public void setPostData(String postData) {
        this.postData = postData;
    }

    public boolean getIsHasPostData() {
        return hasPostData;
    }

    public void setHasPostData(boolean hasPostData) {
        this.hasPostData = hasPostData;
    }

    public String getMixedContentType() {
        return mixedContentType;
    }

    public void setMixedContentType(String mixedContentType) {
        this.mixedContentType = mixedContentType;
    }

    public String getInitialPriority() {
        return initialPriority;
    }

    public void setInitialPriority(String initialPriority) {
        this.initialPriority = initialPriority;
    }

    public String getReferrerPolicy() {
        return referrerPolicy;
    }

    public void setReferrerPolicy(String referrerPolicy) {
        this.referrerPolicy = referrerPolicy;
    }

    public boolean getIsLinkPreload() {
        return isLinkPreload;
    }

    public void setLinkPreload(boolean linkPreload) {
        isLinkPreload = linkPreload;
    }

}
