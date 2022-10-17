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
package org.aoju.lancia.events;

/**
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class DefaultBrowserListener<T> implements BrowserListener<T> {

    private String method;

    private Class<T> resolveType;

    private EventHandler<T> handler;

    private Object target;

    private boolean isOnce;

    private boolean isAvaliable = true;

    //是否异步
    private boolean isSync;

    public Class<?> getResolveType() {
        return resolveType;
    }

    public void setResolveType(Class<T> resolveType) {
        this.resolveType = resolveType;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public EventHandler<T> getHandler() {
        return handler;
    }

    public void setHandler(EventHandler<T> handler) {
        this.handler = handler;
    }

    public boolean getIsOnce() {
        return isOnce;
    }

    public void setIsOnce(boolean isOnce) {
        this.isOnce = isOnce;
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public boolean getIsAvaliable() {
        return isAvaliable;
    }

    public void setIsAvaliable(boolean isAvaliable) {
        this.isAvaliable = isAvaliable;
    }

    public boolean getIsSync() {
        return isSync;
    }

    public void setIsSync(boolean isSync) {
        this.isSync = isSync;
    }

    @Override
    public void onBrowserEvent(T event) {
        this.getHandler().onEvent(event);
    }
}
