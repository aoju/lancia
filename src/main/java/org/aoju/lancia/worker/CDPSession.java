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

import com.alibaba.fastjson.JSONObject;
import org.aoju.bus.core.exception.InstrumentException;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.lancia.Builder;
import org.aoju.lancia.Variables;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * CDPSession实例被用来谈论原始的Chrome Devtools协议
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class CDPSession extends EventEmitter {

    private final Map<Long, Messages> callbacks = new ConcurrentHashMap<>();

    private final String targetType;

    private final String sessionId;

    private Connection connection;

    public CDPSession(Connection connection, String targetType, String sessionId) {
        super();
        this.targetType = targetType;
        this.sessionId = sessionId;
        this.connection = connection;
    }

    public void onClosed() {
        for (Messages callback : callbacks.values()) {
            callback.setErrorText("Protocol error " + callback.getMethod() + " Target closed.");
            if (callback.getCountDownLatch() != null) {
                callback.getCountDownLatch().countDown();
            }
        }
        connection = null;
        callbacks.clear();
        this.emit(Variables.Event.CDPSESSION_DISCONNECTED.getName(), null);
    }

    /**
     * 发送消息到浏览器
     *
     * @param method   消息签名中的方法
     * @param params   消息签名中的参数
     * @param isBlock  是否是阻塞，阻塞的话会等待结果返回
     * @param outLatch 是否自己提供Countdownlatch
     * @param timeout  超时时间
     * @return 结果
     */
    public JSONObject send(String method, Map<String, Object> params, boolean isBlock, CountDownLatch outLatch, int timeout) {
        if (connection == null) {
            throw new InstrumentException("Protocol error (" + method + "): Session closed. Most likely the" + this.targetType + "has been closed.");
        }
        Messages message = new Messages();
        message.setMethod(method);
        message.setParams(params);
        message.setSessionId(this.sessionId);
        try {
            if (isBlock) {
                if (outLatch != null) {
                    message.setCountDownLatch(outLatch);
                } else {
                    CountDownLatch latch = new CountDownLatch(1);
                    message.setCountDownLatch(latch);
                }
                long id = this.connection.rawSend(message, true, this.callbacks);
                boolean hasResult = message.waitForResult(timeout > 0 ? timeout : Variables.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
                if (!hasResult) {
                    throw new InstrumentException("Wait " + method + " for " + (timeout > 0 ? timeout : Variables.DEFAULT_TIMEOUT) + " MILLISECONDS with no response");
                }
                if (StringKit.isNotEmpty(message.getErrorText())) {
                    throw new InstrumentException(message.getErrorText());
                }
                return callbacks.remove(id).getResult();
            } else {
                if (outLatch != null) {
                    message.setNeedRemove(true);
                    message.setCountDownLatch(outLatch);
                    this.connection.rawSend(message, true, this.callbacks);
                } else {
                    this.connection.rawSend(message, false, this.callbacks);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * cdpsession send message
     *
     * @param method  方法
     * @param params  参数
     * @param isBlock 是否阻塞，阻塞会等待结果放回
     * @return result
     */
    public JSONObject send(String method, Map<String, Object> params, boolean isBlock) {
        if (connection == null) {
            throw new InstrumentException("Protocol error (" + method + "): Session closed. Most likely the" + this.targetType + "has been closed.");
        }
        Messages message = new Messages();
        message.setMethod(method);
        message.setParams(params);
        message.setSessionId(this.sessionId);
        try {
            if (isBlock) {
                CountDownLatch latch = new CountDownLatch(1);
                message.setCountDownLatch(latch);
                long id = this.connection.rawSend(message, true, this.callbacks);
                message.waitForResult(0, TimeUnit.MILLISECONDS);
                if (StringKit.isNotEmpty(message.getErrorText())) {
                    throw new RuntimeException(message.getErrorText());
                }
                return callbacks.remove(id).getResult();
            } else {
                this.connection.rawSend(message, false, this.callbacks);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * 页面分离浏览器
     */
    public void detach() {
        if (connection == null) {
            throw new RuntimeException("Session already detached. Most likely the" + this.targetType + "has been closed.");
        }
        Map<String, Object> params = new HashMap<>();
        params.put("sessionId", this.sessionId);
        this.connection.send("Target.detachFromTarget", params, false);
    }

    public void onMessage(JSONObject node) {
        Long idLong = node.getLong(Variables.RECV_MESSAGE_ID_PROPERTY);
        if (idLong != null) {
            Messages callback = this.callbacks.get(idLong);
            if (callback != null) {
                try {
                    JSONObject errNode = node.getJSONObject(Variables.RECV_MESSAGE_ERROR_PROPERTY);
                    if (errNode != null) {
                        if (callback.getCountDownLatch() != null) {
                            callback.setErrorText(Builder.createProtocolError(node));
                        }
                    } else {
                        JSONObject result = node.getJSONObject(Variables.RECV_MESSAGE_RESULT_PROPERTY);
                        callback.setResult(result);
                    }
                } finally {
                    // 最后把callback都移除掉，免得关闭页面后打印错误
                    if (callback.getNeedRemove()) {
                        this.callbacks.remove(idLong);
                    }
                    // 放行等待的线程
                    if (callback.getCountDownLatch() != null) {
                        callback.getCountDownLatch().countDown();
                        callback.setCountDownLatch(null);
                    }
                }
            }
        } else {
            JSONObject paramsNode = node.getJSONObject(Variables.RECV_MESSAGE_PARAMS_PROPERTY);
            String method = node.getString(Variables.RECV_MESSAGE_METHOD_PROPERTY);
            if (method != null) {
                this.emit(method, paramsNode);
            }
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public String getSessionId() {
        return sessionId;
    }

}
