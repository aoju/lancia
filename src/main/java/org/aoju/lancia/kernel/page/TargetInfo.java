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
package org.aoju.lancia.kernel.page;

/**
 * 目标详细内容
 *
 * @author Kimi Liu
 * @version 1.2.1
 * @since JDK 1.8+
 */
public class TargetInfo {

    private String targetId;

    private String type;

    private String title;

    private String url;

    private Boolean attached;

    private String openerId;

    private String browserContextId;

    private String webSocketDebuggerUrl;

    private String devtoolsFrontendUrl;

    private String description;

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getAttached() {
        return attached;
    }

    public void setAttached(Boolean attached) {
        this.attached = attached;
    }

    public String getOpenerId() {
        return openerId;
    }

    public void setOpenerId(String openerId) {
        this.openerId = openerId;
    }

    public String getBrowserContextId() {
        return browserContextId;
    }

    public void setBrowserContextId(String browserContextId) {
        this.browserContextId = browserContextId;
    }

    public String getWebSocketDebuggerUrl() {
        return webSocketDebuggerUrl;
    }

    public void setWebSocketDebuggerUrl(String webSocketDebuggerUrl) {
        this.webSocketDebuggerUrl = webSocketDebuggerUrl;
    }

    public String getDevtoolsFrontendUrl() {
        return devtoolsFrontendUrl;
    }

    public void setDevtoolsFrontendUrl(String devtoolsFrontendUrl) {
        this.devtoolsFrontendUrl = devtoolsFrontendUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "TargetInfo{" +
                "targetId='" + targetId + '\'' +
                ", type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", attached=" + attached +
                ", openerId='" + openerId + '\'' +
                ", browserContextId='" + browserContextId + '\'' +
                ", webSocketDebuggerUrl='" + webSocketDebuggerUrl + '\'' +
                ", devtoolsFrontendUrl='" + devtoolsFrontendUrl + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

}
