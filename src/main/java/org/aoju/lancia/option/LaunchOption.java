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
package org.aoju.lancia.option;

import org.aoju.lancia.Variables;
import org.aoju.lancia.kernel.Standard;

import java.util.List;

/**
 * 启动选项参数
 *
 * @author Kimi Liu
 * @version 6.1.3
 * @since JDK 1.8+
 */
public class LaunchOption extends BrowserOption {

    /**
     * 设置chrome浏览器的路径
     */
    private String executablePath;
    /**
     * 如果是true，代表忽略所有默认的启动参数，默认的启动参数见{@link Variables#DEFAULT_ARGS}，默认是false
     */
    private boolean ignoreAllDefaultArgs;
    /**
     * 忽略指定的默认启动参数
     */
    private List<String> ignoreDefaultArgs;
    /**
     * Close chrome process on Ctrl-C.
     * 默认是true
     */
    private boolean handleSIGINT = true;
    /**
     * Close chrome process on SIGTERM.
     * 默认是 true
     */
    private boolean handleSIGTERM = true;
    /**
     * Close chrome process on SIGHUP.
     * 默认是 true
     */
    private boolean handleSIGHUP = true;
    /**
     * ָSystem.getEnv()
     * Specify environment variables that will be visible to Chromium.
     * 默认是 `process.env`.
     */
    private Standard env;
    /**
     * false代表使用websocket通讯，true代表使用websocket通讯
     * Connects to the browser over a pipe instead of a WebSocket.
     * 默认是  false
     */
    private boolean pipe;
    /**
     * chrome or firefox
     */
    private String product;

    public LaunchOption() {
        super();
    }

    public String getExecutablePath() {
        return executablePath;
    }

    public void setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
    }


    public boolean getIgnoreAllDefaultArgs() {
        return ignoreAllDefaultArgs;
    }

    public void setIgnoreAllDefaultArgs(boolean ignoreAllDefaultArgs) {
        this.ignoreAllDefaultArgs = ignoreAllDefaultArgs;
    }

    public List<String> getIgnoreDefaultArgs() {
        return ignoreDefaultArgs;
    }

    public void setIgnoreDefaultArgs(List<String> ignoreDefaultArgs) {
        this.ignoreDefaultArgs = ignoreDefaultArgs;
    }

    public boolean getHandleSIGINT() {
        return handleSIGINT;
    }

    public void setHandleSIGINT(boolean handleSIGINT) {
        this.handleSIGINT = handleSIGINT;
    }

    public boolean getHandleSIGTERM() {
        return handleSIGTERM;
    }

    public void setHandleSIGTERM(boolean handleSIGTERM) {
        this.handleSIGTERM = handleSIGTERM;
    }

    public boolean getHandleSIGHUP() {
        return handleSIGHUP;
    }

    public void setHandleSIGHUP(boolean handleSIGHUP) {
        this.handleSIGHUP = handleSIGHUP;
    }

    public Standard getEnv() {
        return env;
    }

    public void setEnv(Standard env) {
        this.env = env;
    }

    public boolean getPipe() {
        return pipe;
    }

    public void setPipe(boolean pipe) {
        this.pipe = pipe;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

}
