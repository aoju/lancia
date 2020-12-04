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
package org.aoju.bus.puppeteer.nimble;

/**
 * CSS样式表的源信息
 *
 * @author Kimi Liu
 * @version 6.1.3
 * @since JDK 1.8+
 */
public class StyleSheetHeader {

    /**
     * 样式表标识符
     */
    private String styleSheetId;
    /**
     * 主人帧标识符
     */
    private String frameId;
    /**
     * 样式表资源的URL
     */
    private String sourceURL;
    /**
     * 与样式表关联的源映射的URL.
     */
    private String sourceMapURL;
    /**
     * 样式表的起源.
     * /**
     * *Stylesheet type: "injected" for stylesheets injected via extension, "user-agent" for user-agent
     * stylesheets, "inspector" for stylesheets created by the inspector (i.e. those holding the "via
     * inspector" rules), "regular" for regular stylesheets.
     * "injected"|"user-agent"|"inspector"|"regular"
     */
    private String origin;
    /**
     * 样式表标题
     */
    private String title;
    /**
     * 样式表所有者节点的后端id
     */
    private Number ownerNode;
    /**
     * 表示是否禁用样式表
     */
    private boolean disabled;
    /**
     * sourceURL字段值是否来自sourceURL注释
     */
    private boolean hasSourceURL;
    /**
     * 解析器是否为样式标记创建样式表。没有为文档设置此标志。写样式标签.
     */
    private boolean isInline;
    /**
     * 样式表在资源中的行偏移量
     */
    private int startLine;
    /**
     * 样式表在资源中的列偏移量
     */
    private int startColumn;
    /**
     * 内容的大小
     */
    private int length;
    /**
     * 资源中样式表末尾的行偏移量
     */
    private int endLine;
    /**
     * 资源中样式表末尾的列偏移量
     */
    private int endColumn;

    public String getStyleSheetId() {
        return styleSheetId;
    }

    public void setStyleSheetId(String styleSheetId) {
        this.styleSheetId = styleSheetId;
    }

    public String getFrameId() {
        return frameId;
    }

    public void setFrameId(String frameId) {
        this.frameId = frameId;
    }

    public String getSourceURL() {
        return sourceURL;
    }

    public void setSourceURL(String sourceURL) {
        this.sourceURL = sourceURL;
    }

    public String getSourceMapURL() {
        return sourceMapURL;
    }

    public void setSourceMapURL(String sourceMapURL) {
        this.sourceMapURL = sourceMapURL;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Number getOwnerNode() {
        return ownerNode;
    }

    public void setOwnerNode(Number ownerNode) {
        this.ownerNode = ownerNode;
    }

    public boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean getHasSourceURL() {
        return hasSourceURL;
    }

    public void setHasSourceURL(boolean hasSourceURL) {
        this.hasSourceURL = hasSourceURL;
    }

    public boolean getIsInline() {
        return isInline;
    }

    public void setInline(boolean inline) {
        isInline = inline;
    }

    public int getStartLine() {
        return startLine;
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    public int getStartColumn() {
        return startColumn;
    }

    public void setStartColumn(int startColumn) {
        this.startColumn = startColumn;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public int getEndColumn() {
        return endColumn;
    }

    public void setEndColumn(int endColumn) {
        this.endColumn = endColumn;
    }

}
