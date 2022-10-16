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

import org.aoju.lancia.Builder;
import org.aoju.lancia.kernel.Variables;
import org.aoju.lancia.kernel.page.Viewport;

import java.util.List;

/**
 * 浏览器选项构建
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class LaunchOptionsBuilder {

    private final LaunchOptions options;

    public LaunchOptionsBuilder() {
        options = new LaunchOptions();
    }

    public LaunchOptionsBuilder executablePath(String executablePath) {
        options.setExecutablePath(executablePath);
        return this;
    }

    /**
     * 是否忽略所欲的默认启动参数，默认是fasle
     *
     * @param ignoreAllDefaultArgs true为忽略所有启动参数
     * @return LaunchOptionsBuilder
     */
    public LaunchOptionsBuilder ignoreDefaultArgs(boolean ignoreAllDefaultArgs) {
        options.setIgnoreAllDefaultArgs(ignoreAllDefaultArgs);
        return this;
    }

    /**
     * 忽略指定的默认启动参数，默认的启动参数见 {@link Builder#DEFAULT_ARGS}
     *
     * @param ignoreDefaultArgs 要忽略的启动参数
     * @return LaunchOptionsBuilder
     */
    public LaunchOptionsBuilder ignoreDefaultArgs(List<String> ignoreDefaultArgs) {
        options.setIgnoreDefaultArgs(ignoreDefaultArgs);
        return this;
    }

    public LaunchOptionsBuilder handleSIGINT(boolean handleSIGINT) {
        options.setHandleSIGINT(handleSIGINT);
        return this;
    }

    public LaunchOptionsBuilder handleSIGTERM(boolean handleSIGTERM) {
        options.setHandleSIGTERM(handleSIGTERM);
        return this;
    }

    public LaunchOptionsBuilder handleSIGHUP(boolean handleSIGHUP) {
        options.setHandleSIGHUP(handleSIGHUP);
        return this;
    }

    public LaunchOptionsBuilder dumpio(boolean dumpio) {
        options.setDumpio(dumpio);
        return this;
    }

    public LaunchOptionsBuilder env(Variables env) {
        options.setEnv(env);
        return this;
    }

    public LaunchOptionsBuilder pipe(boolean pipe) {
        options.setPipe(pipe);
        return this;
    }

    public LaunchOptionsBuilder product(String product) {
        options.setProduct(product);
        return this;
    }

    public LaunchOptionsBuilder ignoreHTTPSErrors(boolean ignoreHTTPSErrors) {
        options.setIgnoreHTTPSErrors(ignoreHTTPSErrors);
        return this;
    }

    public LaunchOptionsBuilder viewport(Viewport viewport) {
        options.setViewport(viewport);
        return this;
    }

    public LaunchOptionsBuilder slowMo(int slowMo) {
        options.setSlowMo(slowMo);
        return this;
    }

    public LaunchOptionsBuilder connectionOptions(ConnectionOptions connectionOptions) {
        options.setConnectionOptions(connectionOptions);
        return this;
    }

    public LaunchOptionsBuilder headless(boolean headless) {
        options.setHeadless(headless);
        return this;
    }

    public LaunchOptionsBuilder args(List<String> args) {
        options.setArgs(args);
        return this;
    }

    public LaunchOptionsBuilder userDataDir(String userDataDir) {
        options.setUserDataDir(userDataDir);
        return this;
    }

    public LaunchOptionsBuilder devtools(boolean devtools) {
        options.setDevtools(devtools);
        return this;
    }

    public LaunchOptions build() {
        return options;
    }

}
