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
package org.aoju.lancia.nimble.page;

import org.aoju.lancia.nimble.runtime.StackTrace;

/**
 * 当框架已附加到其父项时触发
 *
 * @author Kimi Liu
 * @version 1.2.1
 * @since JDK 1.8+
 */
public class FrameAttachedPayload {

    /**
     * 已附加框架的
     */
    private String frameId;
    /**
     * 父帧标识符
     */
    private String parentFrameId;
    /**
     * 附加框架的JavaScript堆栈跟踪，仅当框架是从脚本启动时才设置
     */
    private StackTrace stack;

    public String getFrameId() {
        return frameId;
    }

    public void setFrameId(String frameId) {
        this.frameId = frameId;
    }

    public String getParentFrameId() {
        return parentFrameId;
    }

    public void setParentFrameId(String parentFrameId) {
        this.parentFrameId = parentFrameId;
    }

    public StackTrace getStack() {
        return stack;
    }

    public void setStack(StackTrace stack) {
        this.stack = stack;
    }

}
