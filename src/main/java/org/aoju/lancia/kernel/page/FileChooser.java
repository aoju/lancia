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
import org.aoju.lancia.nimble.page.FileChooserPayload;
import org.aoju.lancia.worker.CDPSession;

import java.util.List;

/**
 * FileChooser对象通过“ page.waitForFileChooser”方法返回
 * 通过文件选择器，您可以对请求文件的页面做出反应
 *
 * @author Kimi Liu
 * @version 1.2.2
 * @since JDK 1.8+
 */
public class FileChooser {

    private CDPSession client;

    private ElementHandle element;

    private boolean handled;

    private boolean multiple;

    public FileChooser() {
    }

    public FileChooser(CDPSession client, ElementHandle element, FileChooserPayload event) {
        this.client = client;
        this.element = element;
        this.multiple = !"selectSingle".equals(event.getMode());
        this.handled = false;
    }

    public boolean isMultiple() {
        return this.multiple;
    }

    /**
     * @param filePaths 选择的文件路径
     */
    public void accept(List<String> filePaths) {
        Assert.isTrue(!this.handled, "Cannot accept FileChooser which is already handled!");
        this.handled = true;
        this.element.uploadFile(filePaths);
    }

    public void cancel() {
        Assert.isTrue(!this.handled, "Cannot cancel FileChooser which is already handled!");
        this.handled = true;
    }

}
