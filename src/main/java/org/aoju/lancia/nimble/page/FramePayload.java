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
package org.aoju.lancia.nimble.page;

/**
 * 页面上有关框架的信息
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class FramePayload {

    /**
     * 框架唯一标识符
     */
    private String id;
    /**
     * 父帧标识符
     */
    private String parentId;
    /**
     * 与此框架关联的加载程序的标识符
     */
    private String loaderId;
    /**
     * 标记中指定的框架名称
     */
    private String name;
    /**
     * 框架文档的URL，没有片段
     */
    private String url;
    /**
     * 框架文档的URL片段，包括“＃”
     */
    private String urlFragment;
    /**
     * 框架文档的安全来源
     */
    private String securityOrigin;
    /**
     * 框架文档的mimeType，由浏览器确定
     */
    private String mimeType;
    /**
     * 如果框架加载失败，则其中包含无法加载的URL
     * 请注意，与上述网址不同，该网址可能包含一个片段
     */
    private String unreachableUrl;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getLoaderId() {
        return loaderId;
    }

    public void setLoaderId(String loaderId) {
        this.loaderId = loaderId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public String getSecurityOrigin() {
        return securityOrigin;
    }

    public void setSecurityOrigin(String securityOrigin) {
        this.securityOrigin = securityOrigin;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getUnreachableUrl() {
        return unreachableUrl;
    }

    public void setUnreachableUrl(String unreachableUrl) {
        this.unreachableUrl = unreachableUrl;
    }

}
