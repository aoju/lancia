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
 * HTTP request data.
 * /**
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class RequestPayload {

    /**
     * Request URL (without fragment).
     */
    private String url;
    /**
     * Fragment of the requested URL starting with hash, if present.
     */
    private String urlFragment;
    /**
     * HTTP request method.
     */
    private String method;
    /**
     * HTTP request headers.
     */
    private Map<String, String> headers;
    /**
     * HTTP POST request data.
     */
    private String postData;
    /**
     * True when the request has POST data. Note that postData might still be omitted when this flag is true when the data is too long.
     */
    private boolean hasPostData;
    /**
     * The mixed content type of the request.
     * "blockable"|"optionally-blockable"|"none"
     */
    private String mixedContentType;
    /**
     * Priority of the resource request at the time request is sent.
     * "VeryLow"|"Low"|"Medium"|"High"|"VeryHigh"
     */
    private String initialPriority;
    /**
     * The referrer policy of the request, as defined in https://www.w3.org/TR/referrer-policy/
     * "unsafe-url"|"no-referrer-when-downgrade"|"no-referrer"|"origin"|"origin-when-cross-origin"|"same-origin"|"strict-origin"|"strict-origin-when-cross-origin";
     */
    private String referrerPolicy;
    /**
     * Whether is loaded via link preload.
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
