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
package org.aoju.lancia;

import org.aoju.lancia.kernel.Variables;
import org.aoju.lancia.option.BrowserOptions;
import org.aoju.lancia.option.ChromeArgOptions;
import org.aoju.lancia.option.LaunchOptions;
import org.aoju.lancia.worker.Transport;

import java.io.IOException;
import java.util.List;

/**
 * 启动器(浏览器)
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public interface Launcher {

    Variables VARIABLES = System::getenv;

    Browser launch(LaunchOptions options) throws IOException;

    List<String> defaultArgs(ChromeArgOptions options);

    String resolveExecutablePath(String chromeExecutable) throws IOException;

    Browser connect(BrowserOptions options, String browserWSEndpoint, String browserURL, Transport transport);

    String executablePath() throws IOException;

}
