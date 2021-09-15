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
package org.aoju.lancia.worker;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.toolkit.CollKit;
import org.aoju.bus.logger.Logger;
import org.aoju.lancia.Builder;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 事件发布，事件监听，模仿nodejs的EventEmitter
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class EventEmitter implements Event {

    private final Map<String, Set<BrowserListener>> listenerMap = new ConcurrentHashMap<>();
    private final AtomicInteger listenerCount = new AtomicInteger(0);

    /**
     * 也是监听事件，不过这个方法只要在本项目内部使用，如果你想要自己监听事件，请使用{@link EventEmitter#on(String, EventHandler)}
     *
     * @param method    事件名称
     * @param blistener 监听器
     * @param isOnce    是否只监听一次
     * @return Event
     */
    @Override
    public Event addListener(String method, Listener<?> blistener, boolean isOnce) {
        BrowserListener listener = (BrowserListener) blistener;
        if (!method.equals(listener.getMethod())) {
            Logger.error("addListener fail:{} is not equals listener.getMethod()[{}]", method, listener.getMethod());
            return this;
        }
        listener.setIsOnce(isOnce);
        Set<BrowserListener> browserListeners = this.listenerMap.get(method);
        if (browserListeners == null) {
            Set<BrowserListener> listeners = Builder.getConcurrentSet();
            this.listenerMap.putIfAbsent(method, listeners);
            listeners.add(listener);
        } else {
            browserListeners.add(listener);
        }
        listenerCount.incrementAndGet();
        return this;
    }

    /**
     * 移除监听器
     *
     * @param method   监听器对应的方法
     * @param listener 要移除的监听器
     * @return Event
     */
    @Override
    public Event removeListener(String method, Listener<?> listener) {
        Set<BrowserListener> listeners = this.listenerMap.get(method);
        if (CollKit.isNotEmpty(listeners)) {
            listeners.remove(listener);
            listenerCount.decrementAndGet();
        }
        return this;
    }

    @Override
    public void emit(String method, Object params) {
        Assert.notNull(method, "method must not be null");
        Set<BrowserListener> listeners = this.listenerMap.get(method);
        if (CollKit.isEmpty(listeners))
            return;
        for (BrowserListener listener : listeners) {
            if (!listener.getIsAvaliable()) {
                listeners.remove(listener);
                listenerCount.decrementAndGet();
                continue;
            }
            if (listener.getIsOnce()) {
                listeners.remove(listener);
                listenerCount.decrementAndGet();
            }
            try {
                Object event;
                if (params != null) {
                    Class<?> resolveType = null;
                    Type genericSuperclass = listener.getClass().getGenericSuperclass();
                    if (genericSuperclass instanceof ParameterizedType) {
                        ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
                        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                        if (actualTypeArguments.length == 1) {
                            resolveType = (Class) actualTypeArguments[0];
                        }
                    } else {
                        resolveType = listener.getResolveType();
                    }

                    if (JSONObject.class.isAssignableFrom(params.getClass())) {
                        event = readJsonObject(resolveType, (JSONObject) params);
                    } else {
                        event = params;
                    }
                } else {
                    event = null;
                }
                invokeListener(listener, event);
            } catch (IOException e) {
                Logger.error("publish event error:", e);
                return;
            }
        }
    }

    /**
     * 执行监听器，如果是用户的监听，则用用户的处理器去处理，不然执行onBrowserEvent方法
     *
     * @param listener 监听器
     * @param event    事件
     */
    private void invokeListener(BrowserListener listener, Object event) {
        try {
            if (listener.getIsSync()) {
                Builder.commonExecutor().submit(() -> {
                    listener.onBrowserEvent(event);
                });
            } else {
                listener.onBrowserEvent(event);
            }
        } finally {
            if (listener.getIsOnce()) {
                listener.setIsAvaliable(false);
            }
        }
    }

    /**
     * 如果clazz属于JSONObject.class则不用转换类型，如果不是，则将JSONObject转化成clazz类型对象
     *
     * @param clazz  目标类型
     * @param params event的具体内容
     * @param <T>    具体类型
     * @return T
     * @throws IOException 转化失败抛出的异常
     */
    private <T> T readJsonObject(Class<T> clazz, JSONObject params) throws IOException {
        if (params == null) {
            throw new IllegalArgumentException(
                    "Failed converting null response to clazz " + clazz.getName());
        }
        if (JSONObject.class.isAssignableFrom(clazz)) {
            return (T) params;
        }
        return JSON.toJavaObject(params, clazz);
    }

    public int getListenerCount(String method) {
        Set<BrowserListener> listeners = this.listenerMap.get(method);
        int i = 0;
        if (CollKit.isEmpty(listeners)) {
            return 0;
        }
        for (BrowserListener listener : listeners) {
            if (!listener.getIsAvaliable()) {
                continue;
            }
            i++;
        }
        return i;
    }

    /**
     * 监听事件，可用于自定义事件监听,用户监听的事件都是在别的线程中异步执行的
     *
     * @param method  事件名称
     * @param handler 事件的处理器
     * @return Event
     */
    public Event on(String method, EventHandler<?> handler) {
        BrowserListener listener = new BrowserListener();
        listener.setIsSync(true);
        listener.setMethod(method);
        listener.setHandler(handler);
        return this.addListener(method, listener);
    }

    /**
     * 一次性事件监听，用于自定义事件监听，与{@link EventEmitter#on(String, EventHandler)}的区别就是on会一直监听
     *
     * @param method  事件名称
     * @param handler 事件处理器
     * @return Event
     */
    public Event once(String method, EventHandler<?> handler) {
        BrowserListener listener = new BrowserListener();
        listener.setIsSync(true);
        listener.setMethod(method);
        listener.setHandler(handler);
        return this.addListener(method, listener, true);
    }

    /**
     * 取消事件
     *
     * @param method   事件名称
     * @param listener 事件的监听器
     * @return Event
     */
    public Event off(String method, Listener<?> listener) {
        return this.removeListener(method, listener);
    }

}
