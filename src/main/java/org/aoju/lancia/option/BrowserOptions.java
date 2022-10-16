/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
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

import org.aoju.lancia.kernel.page.Viewport;

/**
 * 浏览器选项
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class BrowserOptions extends ChromeArgOptions {

    /**
     * <br/>
     * Whether to ignore HTTPS errors during navigation.
     * <p>
     * 默认是false
     */
    private boolean ignoreHTTPSErrors;
    /**
     * 800x600
     * <br/>
     * Sets a consistent viewport for each page. Defaults to an 800x600 viewport. null disables the default viewport.
     */
    private Viewport viewport = new Viewport();
    /**
     * <br/>
     * Slows down Puppeteer operations by the specified amount of milliseconds.
     * Useful so that you can see what is going on.
     */
    private int slowMo;
    /**
     * 浏览器与CDP的连接配置
     */
    private ConnectionOptions connectionOptions = new ConnectionOptions();

    public BrowserOptions() {
        super();
    }

    public boolean getIgnoreHTTPSErrors() {
        return ignoreHTTPSErrors;
    }

    public void setIgnoreHTTPSErrors(boolean ignoreHTTPSErrors) {
        this.ignoreHTTPSErrors = ignoreHTTPSErrors;
    }

    public Viewport getViewport() {
        return viewport;
    }

    public void setViewport(Viewport viewport) {
        this.viewport = viewport;
    }

    public int getSlowMo() {
        return slowMo;
    }

    public void setSlowMo(int slowMo) {
        this.slowMo = slowMo;
    }

    public ConnectionOptions getConnectionOptions() {
        return connectionOptions;
    }

    public void setConnectionOptions(ConnectionOptions connectionOptions) {
        this.connectionOptions = connectionOptions;
    }
}
