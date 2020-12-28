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

import org.aoju.lancia.nimble.runtime.RemoteObject;
import org.aoju.lancia.nimble.runtime.StackTrace;

import java.util.List;

/**
 * 日志信息
 *
 * @author Kimi Liu
 * @version 1.2.1
 * @since JDK 1.8+
 */
public class LogEntry {

    /**
     * Log entry source.
     * "xml"|"javascript"|"network"|"storage"|"appcache"|"rendering"|"security"|"deprecation"|"worker"|"violation"|"intervention"|"recommendation"|"other"
     */
    private String source;
    /**
     * Log entry severity.
     * "verbose"|"info"|"warning"|"error"
     */
    private String level;
    /**
     * Logged text.
     */
    private String text;
    /**
     * Timestamp when this entry was added.
     */
    private long timestamp;
    /**
     * URL of the resource if known.
     */
    private String url;
    /**
     * Line number in the resource.
     */
    private int lineNumber;
    /**
     * JavaScript stack trace.
     */
    private StackTrace stackTrace;
    /**
     * Identifier of the network request associated with this entry.
     */
    private String networkRequestId;
    /**
     * Identifier of the worker associated with this entry.
     */
    private String workerId;
    /**
     * Call arguments.
     */
    private List<RemoteObject> args;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public StackTrace getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(StackTrace stackTrace) {
        this.stackTrace = stackTrace;
    }

    public String getNetworkRequestId() {
        return networkRequestId;
    }

    public void setNetworkRequestId(String networkRequestId) {
        this.networkRequestId = networkRequestId;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public List<RemoteObject> getArgs() {
        return args;
    }

    public void setArgs(List<RemoteObject> args) {
        this.args = args;
    }

    @Override
    public String toString() {
        return "LogEntry{" +
                "source='" + source + '\'' +
                ", level='" + level + '\'' +
                ", text='" + text + '\'' +
                ", timestamp=" + timestamp +
                ", url='" + url + '\'' +
                ", lineNumber=" + lineNumber +
                ", stackTrace=" + stackTrace +
                ", networkRequestId='" + networkRequestId + '\'' +
                ", workerId='" + workerId + '\'' +
                ", args=" + args +
                '}';
    }

}
