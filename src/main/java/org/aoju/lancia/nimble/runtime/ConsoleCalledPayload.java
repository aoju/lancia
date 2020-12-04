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

import java.util.List;

/**
 * 在调用控制台API时发出
 *
 * @author Kimi Liu
 * @version 6.1.3
 * @since JDK 1.8+
 */
public class ConsoleCalledPayload {

    /**
     * 类型：
     * "log"|"debug"|"info"|"error"|"warning"|"dir"|"dirxml"|"table"
     * |"trace"|"clear"|"startGroup"|"startGroupCollapsed"|"endGroup"
     * |"assert"|"profile"|"profileEnd"|"count"|"timeEnd".
     */
    private String type;
    /**
     * 调用参数
     */
    private List<RemoteObject> args;
    /**
     * 进行调用的上下文的标识符
     */
    private int executionContextId;
    /**
     * 时间戳
     */
    private long timestamp;
    /**
     * 调用时捕获的堆栈跟踪。对于以下调用类型，
     * 将自动报告异步堆栈链：`assert`, `error`, `trace`, `warning` 对于其他类型，
     * 可以使用`Debugger.getStackTrace` 和 `stackTrace.parentId`字段检索异步调用链
     */
    private StackTrace stackTrace;
    /**
     * 非默认控制台上下文（非console）上调用的控制台上下文描述符
     * ：'anonymous#unique-logger-id'代表未命名上下文的调用，
     * 'name#unique-logger-id'代表命名上下文上的调用
     */
    private String context;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<RemoteObject> getArgs() {
        return args;
    }

    public void setArgs(List<RemoteObject> args) {
        this.args = args;
    }

    public int getExecutionContextId() {
        return executionContextId;
    }

    public void setExecutionContextId(int executionContextId) {
        this.executionContextId = executionContextId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public StackTrace getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(StackTrace stackTrace) {
        this.stackTrace = stackTrace;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

}
