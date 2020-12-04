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
package org.aoju.bus.puppeteer.kernel.page;

import org.aoju.bus.puppeteer.Variables;

/**
 * 请求超时
 *
 * @author Kimi Liu
 * @version 6.1.3
 * @since JDK 1.8+
 */
public class Timeout {

    private int defaultNavigationTimeout;

    private int defaultTimeout;

    public Timeout() {
        this.defaultTimeout = 0;
        this.defaultNavigationTimeout = 0;
    }

    public int navigationTimeout() {
        if (this.defaultNavigationTimeout != 0)
            return this.defaultNavigationTimeout;
        if (this.defaultTimeout != 0)
            return this.defaultTimeout;
        return Variables.DEFAULT_TIMEOUT;
    }

    public int timeout() {
        if (this.defaultTimeout != 0)
            return this.defaultTimeout;
        return Variables.DEFAULT_TIMEOUT;
    }

    public int getDefaultNavigationTimeout() {
        return defaultNavigationTimeout;
    }

    public void setDefaultNavigationTimeout(int defaultNavigationTimeout) {
        this.defaultNavigationTimeout = defaultNavigationTimeout;
    }

    public int getDefaultTimeout() {
        return defaultTimeout;
    }

    public void setDefaultTimeout(int defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

}
