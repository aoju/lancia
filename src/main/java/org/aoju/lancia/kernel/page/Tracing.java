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

import com.fasterxml.jackson.databind.JsonNode;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.Normal;
import org.aoju.lancia.Builder;
import org.aoju.lancia.Variables;
import org.aoju.lancia.worker.BrowserListener;
import org.aoju.lancia.worker.CDPSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * You can use [`tracing.start`](#tracingstartoptions) and [`tracing.stop`](#tracingstop)to create a trace file
 * which can be opened in Chrome DevTools or [timeline viewer](https://chromedevtools.github.io/timeline-viewer/)
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class Tracing {

    /**
     * 当前要trace的 chrome devtools protocol session
     */
    private CDPSession client;

    /**
     * 判断是否已经在追踪中
     */
    private boolean recording;

    /**
     * 追踪到的信息要保存的文件路径
     */
    private String path;


    public Tracing(CDPSession client) {
        this.client = client;
        this.recording = false;
        this.path = Normal.EMPTY;
    }

    public void start(String path) {
        this.start(path, false, null);
    }

    /**
     * 每个浏览器一次只能激活一条跟踪
     *
     * @param path        跟踪文件写入的路径
     * @param screenshots 捕获跟踪中的屏幕截图
     * @param categories  指定要使用的自定义类别替换默认值
     */
    public void start(String path, boolean screenshots, Set<String> categories) {
        Assert.isTrue(!this.recording, "Cannot start recording trace while already recording trace.");
        if (categories == null)
            categories = new HashSet<>(Variables.DEFAULTCATEGORIES);
        if (screenshots)
            categories.add("disabled-by-default-devtools.screenshot");
        this.path = path;
        this.recording = true;
        Map<String, Object> params = new HashMap<>();
        params.put("transferMode", "ReturnAsStream");
        params.put("categories", String.join(",", categories));
        this.client.send("Tracing.start", params, true);
    }

    /**
     * 停止追踪
     */
    public void stop() {
        BrowserListener<JsonNode> traceListener = new BrowserListener<JsonNode>() {
            @Override
            public void onBrowserEvent(JsonNode event) {
                Tracing tracing;
                try {
                    tracing = (Tracing) this.getTarget();
                    Builder.readProtocolStream(tracing.getClient(), event.get(Variables.RECV_MESSAGE_STREAM_PROPERTY).asText(), tracing.getPath(), true);
                } catch (IOException ignored) {

                }
            }
        };
        traceListener.setTarget(this);
        traceListener.setMethod("Tracing.tracingComplete");
        this.client.addListener(traceListener.getMethod(), traceListener, true);
        this.client.send("Tracing.end", null, true);
        this.recording = false;
    }

    public CDPSession getClient() {
        return client;
    }

    public void setClient(CDPSession client) {
        this.client = client;
    }

    public boolean isRecording() {
        return recording;
    }

    public void setRecording(boolean recording) {
        this.recording = recording;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
