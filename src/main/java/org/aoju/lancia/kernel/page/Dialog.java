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

import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.logger.Logger;
import org.aoju.lancia.Builder;
import org.aoju.lancia.Variables;
import org.aoju.lancia.worker.CDPSession;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * 提示弹框信息
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class Dialog {

    private CDPSession client;

    private String type;

    private String message;

    private String defaultValue = Normal.EMPTY;

    private boolean handled;

    public Dialog() {
        super();
    }

    public Dialog(CDPSession client, Variables.DialogType type, String message, String defaultValue) {
        super();
        this.client = client;
        this.type = type.getType();
        this.message = message;
        this.handled = false;
        if (StringKit.isNotEmpty(defaultValue))
            this.defaultValue = defaultValue;
    }

    /**
     * @return {string}
     */
    public String type() {
        return this.type;
    }

    /**
     * @return {string}
     */
    public String message() {
        return this.message;
    }

    /**
     * @return {string}
     */
    public String defaultValue() {
        return this.defaultValue;
    }

    /**
     * 接受对话框
     *
     * @param promptText 在提示中输入的文本。如果对话框type不提示，则不会引起任何影响
     * @return 对话框关闭后返回
     */
    public Future<Boolean> accept(String promptText) {
        return Builder.commonExecutor().submit(() -> {
            try {
                Assert.isTrue(!this.handled, "Cannot accept dialog which is already handled!");
                this.handled = true;
                Map<String, Object> params = new HashMap<>();
                params.put("accept", true);
                params.put("promptText", promptText);
                this.client.send("Page.handleJavaScriptDialog", params, true);
            } catch (Exception e) {
                Logger.error("Dialog accept error ", e);
                return false;
            }
            return true;
        });
    }

    /**
     * 不接受对话框的内容
     *
     * @return 对话框关闭后返回
     */
    public Future<Boolean> dismiss() {
        return Builder.commonExecutor().submit(() -> {
            try {
                Assert.isTrue(!this.handled, "Cannot dismiss dialog which is already handled!");
                this.handled = true;
                Map<String, Object> params = new HashMap<>();
                params.put("accept", false);
                this.client.send("Page.handleJavaScriptDialog", params, true);
            } catch (Exception e) {
                Logger.error("Dialog dismiss error ", e);
                return false;
            }
            return true;
        });

    }

    @Override
    public String toString() {
        return "Dialog{" +
                "type='" + type + '\'' +
                ", message='" + message + '\'' +
                ", defaultValue='" + defaultValue + '\'' +
                ", handled=" + handled +
                '}';
    }

}
