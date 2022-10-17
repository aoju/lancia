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
package org.aoju.lancia.option;

import org.aoju.lancia.Page;

import java.util.List;

/**
 * ${@link Page#goTo}
 * 导航到页面的选项
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class PageNavigateOptions {

    /**
     * Referer header value. If provided it will take preference over the referer header value set by page.setExtraHTTPHeaders().
     */
    private String referer;

    /**
     * 导航到一个页面的超时事件
     */
    private int timeout;

    /**
     * 到哪个阶段才算导航完成，共有四个阶段
     * load -
     * domcontentloaded -
     * networkidle0 -
     * networkidle2 -
     */
    private List<String> waitUntil;

    public PageNavigateOptions() {
        super();
    }

    public PageNavigateOptions(String referer, List<String> waitUntil) {
        super();
        this.referer = referer;
        this.waitUntil = waitUntil;
    }

    public PageNavigateOptions(String referer, int timeout, List<String> waitUntil) {
        this.referer = referer;
        this.timeout = timeout;
        this.waitUntil = waitUntil;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public List<String> getWaitUntil() {
        return waitUntil;
    }

    public void setWaitUntil(List<String> waitUntil) {
        this.waitUntil = waitUntil;
    }

}
