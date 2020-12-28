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
package org.aoju.lancia.launch;

import org.aoju.lancia.Browser;
import org.aoju.lancia.Launcher;
import org.aoju.lancia.option.BrowserOption;
import org.aoju.lancia.option.ChromeOption;
import org.aoju.lancia.option.LaunchOption;
import org.aoju.lancia.worker.Transport;

import java.util.List;

/**
 * Firefox启动支持
 *
 * @author Kimi Liu
 * @version 1.2.1
 * @since JDK 1.8+
 */
public class FirefoxLauncher implements Launcher {

    private final boolean isPuppeteerCore;

    public FirefoxLauncher(boolean isPuppeteerCore) {
        super();
        this.isPuppeteerCore = isPuppeteerCore;
    }

    @Override
    public Browser launch(LaunchOption options) {
        return null;
    }

    @Override
    public List<String> defaultArgs(ChromeOption options) {
        return null;
    }


    @Override
    public String resolveExecutablePath(String chromeExecutable) {
        return null;
    }

    @Override
    public Browser connect(BrowserOption options, String browserWSEndpoint, String browserURL, Transport transport) {
        return null;
    }

    @Override
    public String executablePath() {
        return null;
    }

}
