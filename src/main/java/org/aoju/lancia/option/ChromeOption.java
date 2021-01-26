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
package org.aoju.lancia.option;

import java.util.List;

/**
 * Chrome可选项参数
 *
 * @author Kimi Liu
 * @version 1.2.2
 * @since JDK 1.8+
 */
public class ChromeOption extends Timeoutable {
    /**
     * 是否是无厘头
     * 是否以无头模式运行浏览器
     * 默认为true 除非devtools选项为true
     */
    private boolean headless = true;
    /**
     * 其他参数，在下面的连接可以看到
     * https://peter.sh/experiments/chromium-command-line-switches/
     * 传递给浏览器实例的其他参数。可以在此处找到Chromium标志的列表
     */
    private List<String> args;
    /**
     * 用户数据存储的目录
     */
    private String userDataDir;
    /**
     * 是否打开devtool,也就是F12打开的开发者工具
     * 是否为每个选项卡自动打开DevTools面板
     * 如果此选项为true，则无头选项将设置为false
     */
    private boolean devtools;

    public boolean getHeadless() {
        return headless;
    }

    public void setHeadless(boolean headless) {
        this.headless = headless;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public String getUserDataDir() {
        return userDataDir;
    }

    public void setUserDataDir(String userDataDir) {
        this.userDataDir = userDataDir;
    }

    public boolean getDevtools() {
        return devtools;
    }

    public void setDevtools(boolean devtools) {
        this.devtools = devtools;
    }

}
