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
package org.aoju.lancia.nimble;

import org.aoju.lancia.nimble.runtime.AuxData;
import org.aoju.lancia.nimble.runtime.StackTrace;

/**
 * 当虚拟机解析脚本时触发。在启用调试器时，也会为所有已知的和未收集的脚本触发此事件
 *
 * @author Kimi Liu
 * @version 6.1.3
 * @since JDK 1.8+
 */
public class ScriptParsedPayload {

    /**
     * 已解析脚本的标识符
     */
    private String scriptId;
    /**
     * 解析的脚本的URL或名称
     */
    private String url;
    /**
     * 具有给定URL的脚本在资源中的行偏移量
     */
    private int startLine;
    /**
     * 具有给定URL的资源内脚本的列偏移量
     */
    private int startColumn;
    /**
     * 脚本的最后一行
     */
    private int endLine;
    /**
     * 脚本最后一行的长度
     */
    private int endColumn;
    /**
     * 指定脚本创建上下文
     */
    private int executionContextId;
    /**
     * 脚本的内容散列
     */
    private String hash;
    /**
     * Embedder-specific辅助数据.
     */
    private AuxData executionContextAuxData;
    /**
     * 如果该脚本是由实时编辑操作生成的，则为true.
     */
    private boolean isLiveEdit;
    /**
     * 与脚本关联的源映射的URL
     */
    private String sourceMapURL;
    /**
     * 如果该脚本具有sourceURL，则为true
     */
    private boolean hasSourceURL;
    /**
     * 如果该脚本是ES6模块，则为true
     */
    private boolean isModule;
    /**
     * 这个脚本的长度
     */
    private int length;
    /**
     * 如果可用，用于触发脚本解析事件的JavaScript顶部堆栈框架
     */
    private StackTrace stackTrace;

    public String getScriptId() {
        return scriptId;
    }

    public void setScriptId(String scriptId) {
        this.scriptId = scriptId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public int getExecutionContextId() {
        return executionContextId;
    }

    public void setExecutionContextId(int executionContextId) {
        this.executionContextId = executionContextId;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public AuxData getExecutionContextAuxData() {
        return executionContextAuxData;
    }

    public void setExecutionContextAuxData(AuxData executionContextAuxData) {
        this.executionContextAuxData = executionContextAuxData;
    }

    public boolean getIsLiveEdit() {
        return isLiveEdit;
    }

    public void setIsLiveEdit(boolean isLiveEdit) {
        this.isLiveEdit = isLiveEdit;
    }

    public String getSourceMapURL() {
        return sourceMapURL;
    }

    public void setSourceMapURL(String sourceMapURL) {
        this.sourceMapURL = sourceMapURL;
    }

    public boolean getHasSourceURL() {
        return hasSourceURL;
    }

    public void setHasSourceURL(boolean hasSourceURL) {
        this.hasSourceURL = hasSourceURL;
    }

    public boolean getIsModule() {
        return isModule;
    }

    public void setIsModule(boolean isModule) {
        this.isModule = isModule;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public StackTrace getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(StackTrace stackTrace) {
        this.stackTrace = stackTrace;
    }

}
