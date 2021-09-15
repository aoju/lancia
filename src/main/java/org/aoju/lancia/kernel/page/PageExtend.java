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

import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.lancia.Page;

import java.util.List;

/**
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class PageExtend {

    public static String html(Page page) {
        if (null == page) return null;
        ElementHandle handle = byTag(page, "html");
        if (null == handle) return null;
        JSHandle jsHandle = handle.getProperty("outerHTML");
        if (null == jsHandle) return null;
        return jsHandle.jsonValue().toString();
    }

    public static String text(Page page) {
        if (null == page) return null;
        ElementHandle handle = byTag(page, "html");
        if (null == handle) return null;
        JSHandle jsHandle = handle.getProperty("innerText");
        if (null == jsHandle) return null;
        return jsHandle.jsonValue().toString();
    }

    public static ElementHandle byId(Page page, String param) {
        if (null == page || StringKit.isEmpty(param)) {
            return null;
        }
        return page.$("#".concat(param));
    }

    public static ElementHandle byTag(Page page, String param) {
        if (null == page || StringKit.isEmpty(param)) {
            return null;
        }
        return page.$(param);
    }

    public static ElementHandle byClass(Page page, String param) {
        if (null == page || StringKit.isEmpty(param)) {
            return null;
        }
        return page.$(".".concat(param));
    }

    public static List<ElementHandle> byTagList(Page page, String param) {
        if (null == page || StringKit.isEmpty(param)) {
            return null;
        }
        return page.$$(param);
    }

    public static List<ElementHandle> byClassList(Page page, String param) {
        if (null == page || StringKit.isEmpty(param)) {
            return null;
        }
        return page.$$(".".concat(param));
    }

}
