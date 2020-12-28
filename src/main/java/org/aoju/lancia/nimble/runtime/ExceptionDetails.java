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
package org.aoju.lancia.nimble.runtime;

/**
 * 有关在脚本编译或执行过程中引发的异常（或错误）的详细信息
 *
 * @author Kimi Liu
 * @version 1.2.1
 * @since JDK 1.8+
 */
public class ExceptionDetails {

    /**
     * 异常ID
     */
    private int exceptionId;
    /**
     * 异常文本，在可用时应与异常对象一起使用
     */
    private String text;
    /**
     * 异常位置的行号
     */
    private int lineNumber;
    /**
     * 异常位置的列号
     */
    private int columnNumber;
    /**
     * 异常位置的脚本ID
     */
    private String scriptId;
    /**
     * 未报告脚本时使用的异常位置的URL
     */
    private String url;
    /**
     * JavaScript堆栈跟踪
     */
    private StackTrace stackTrace;
    /**
     * 异常对象
     */
    private RemoteObject exception;
    /**
     * 发生异常的上下文的标识符
     */
    private int executionContextId;

    public int getExceptionId() {
        return exceptionId;
    }

    public void setExceptionId(int exceptionId) {
        this.exceptionId = exceptionId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }

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

    public StackTrace getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(StackTrace stackTrace) {
        this.stackTrace = stackTrace;
    }

    public RemoteObject getException() {
        return exception;
    }

    public void setException(RemoteObject exception) {
        this.exception = exception;
    }

    public int getExecutionContextId() {
        return executionContextId;
    }

    public void setExecutionContextId(int executionContextId) {
        this.executionContextId = executionContextId;
    }

}
