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
import org.aoju.bus.core.exception.InternalException;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.logger.Logger;
import org.aoju.lancia.Builder;
import org.aoju.lancia.kernel.page.TargetInfo;
import org.aoju.lancia.option.ConnectionOption;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * 浏览器级别的连接
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class Connection extends EventEmitter implements Consumer<String> {

    private static final AtomicLong lastId = new AtomicLong(0);
    /**
     * URL
     */
    private final String url;
    private final Transport transport;
    /**
     * 单位是毫秒
     */
    private final int delay;
    private final Map<Long, Messages> callbacks = new ConcurrentHashMap<>();// 并发

    private final Map<String, CDPSession> sessions = new ConcurrentHashMap<>();

    private boolean closed;

    private ConnectionOption connectionOption;

    public Connection(String url, Transport transport, int delay) {
        super();
        this.url = url;
        this.transport = transport;
        this.delay = delay;
        if (this.transport instanceof SocketTransport) {
            ((SocketTransport) this.transport).addConsumer(this);
            ((SocketTransport) this.transport).addConnection(this);
        }

        // 赋予默认值，调用方使用该构造方法后，需要set connection options
        this.connectionOption = new ConnectionOption();
    }

    public Connection(String url, Transport transport, int delay, ConnectionOption connectionOption) {
        this(url, transport, delay);
        this.connectionOption = connectionOption == null ? new ConnectionOption() : connectionOption;
    }

    /**
     * 从{@link CDPSession}中拿到对应的{@link Connection}
     *
     * @param client cdpsession
     * @return Connection
     */
    public static Connection fromSession(CDPSession client) {
        return client.getConnection();
    }

    public JSONObject send(String method, Map<String, Object> params, boolean isWait) {
        Messages message = new Messages();
        message.setMethod(method);
        message.setParams(params);
        try {
            if (isWait) {
                message.setCountDownLatch(new CountDownLatch(1));
                long id = rawSend(message, true, this.callbacks);
                message.waitForResult(0, TimeUnit.MILLISECONDS);
                if (StringKit.isNotEmpty(message.getErrorText())) {
                    throw new InternalException(message.getErrorText());
                }
                return callbacks.remove(id).getResult();
            } else {
                rawSend(message, false, this.callbacks);
                return null;
            }
        } catch (InterruptedException e) {
            throw new InternalException(e);
        }
    }

    public JSONObject send(String method, Map<String, Object> params, boolean isWait, CountDownLatch outLatch) {
        Messages message = new Messages();
        message.setMethod(method);
        message.setParams(params);
        try {
            if (isWait) {
                if (outLatch != null) {
                    message.setCountDownLatch(outLatch);
                } else {
                    message.setCountDownLatch(new CountDownLatch(1));
                }
                long id = this.rawSend(message, true, this.callbacks);
                message.waitForResult(0, TimeUnit.MILLISECONDS);
                if (StringKit.isNotEmpty(message.getErrorText())) {
                    throw new InternalException(message.getErrorText());
                }
                return callbacks.remove(id).getResult();
            } else {
                if (outLatch != null) {
                    message.setNeedRemove(true);
                    message.setCountDownLatch(outLatch);
                    this.rawSend(message, true, this.callbacks);
                } else {
                    this.rawSend(message, false, this.callbacks);
                }
            }


        } catch (InterruptedException e) {
            throw new InternalException(e);
        }
        return null;
    }

    /**
     * @param message     发送的消息内容
     * @param putCallback 是否应该放进callbacks里面
     * @param callbacks   对应的callbacks
     * @return 发送消息的id
     */
    public long rawSend(Messages message, boolean putCallback, Map<Long, Messages> callbacks) {
        long id = lastId.incrementAndGet();
        message.setId(id);
        if (putCallback) {
            callbacks.put(id, message);
        }
        String sendMsg = JSON.toJSONString(message);
        transport.send(sendMsg);
        Logger.trace("SEND -> " + sendMsg);
        return id;
    }

    /**
     * recevie message from browser by websocket
     *
     * @param message 从浏览器接受到的消息
     */
    public void onMessage(String message) {

        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Logger.error("slowMo browser Fail:", e);
            }
        }
        Logger.trace("<- RECV " + message);
        try {
            if (StringKit.isNotEmpty(message)) {
                JSONObject readTree = JSON.parseObject(message);
                String methodNode = readTree.getString(Builder.RECV_MESSAGE_METHOD_PROPERTY);
                String method = null;
                if (methodNode != null) {
                    method = methodNode;
                }
                if ("Target.attachedToTarget".equals(method)) {// attached to target -> page attached to browser
                    JSONObject paramsNode = readTree.getJSONObject(Builder.RECV_MESSAGE_PARAMS_PROPERTY);
                    String sessionId = paramsNode.getString(Builder.RECV_MESSAGE_SESSION_ID_PROPERTY);
                    String typeNode = paramsNode.getJSONObject(Builder.RECV_MESSAGE_TARGETINFO_PROPERTY).getString(Builder.RECV_MESSAGE_TYPE_PROPERTY);
                    CDPSession cdpSession = new CDPSession(this, typeNode, sessionId);
                    sessions.put(sessionId, cdpSession);
                } else if ("Target.detachedFromTarget".equals(method)) {// 页面与浏览器脱离关系
                    JSONObject paramsNode = readTree.getJSONObject(Builder.RECV_MESSAGE_PARAMS_PROPERTY);
                    String sessionId = paramsNode.getString(Builder.RECV_MESSAGE_SESSION_ID_PROPERTY);
                    CDPSession cdpSession = sessions.get(sessionId);
                    if (cdpSession != null) {
                        cdpSession.onClosed();
                        sessions.remove(sessionId);
                    }
                }
                String objectSessionId = readTree.getString(Builder.RECV_MESSAGE_SESSION_ID_PROPERTY);
                Long objectId = readTree.getLong(Builder.RECV_MESSAGE_ID_PROPERTY);
                if (objectSessionId != null) {//cdpsession消息，当然cdpsession来处理
                    CDPSession cdpSession = this.sessions.get(objectSessionId);
                    if (cdpSession != null) {
                        cdpSession.onMessage(readTree);
                    }
                } else if (objectId != null) {// long类型的id,说明属于这次发送消息后接受的回应
                    long id = objectId;
                    Messages callback = this.callbacks.get(id);
                    if (callback != null) {
                        try {
                            JSONObject error = readTree.getJSONObject(Builder.RECV_MESSAGE_ERROR_PROPERTY);
                            if (error != null) {
                                if (callback.getCountDownLatch() != null) {
                                    callback.setErrorText(Builder.createProtocolError(readTree));
                                }
                            } else {
                                JSONObject result = readTree.getJSONObject(Builder.RECV_MESSAGE_RESULT_PROPERTY);
                                callback.setResult(result);
                            }
                        } finally {

                            // 最后把callback都移除掉，免得关闭页面后打印错误
                            if (callback.getNeedRemove()) {
                                this.callbacks.remove(id);
                            }

                            // 放行等待的线程
                            if (callback.getCountDownLatch() != null) {
                                callback.getCountDownLatch().countDown();
                                callback.setCountDownLatch(null);
                            }
                        }
                    }
                } else {// 是我们监听的事件，把它事件
                    JSONObject paramsNode = readTree.getJSONObject(Builder.RECV_MESSAGE_PARAMS_PROPERTY);
                    this.emit(method, paramsNode);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建一个{@link CDPSession}
     *
     * @param targetInfo target info
     * @return CDPSession client
     */
    public CDPSession createSession(TargetInfo targetInfo) {
        Map<String, Object> params = new HashMap<>();
        params.put("targetId", targetInfo.getTargetId());
        params.put("flatten", true);
        JSONObject result = this.send("Target.attachToTarget", params, true);
        return this.sessions.get(result.getString(Builder.RECV_MESSAGE_SESSION_ID_PROPERTY));
    }

    public String url() {
        return this.url;
    }

    public String getUrl() {
        return url;
    }

    public CDPSession session(String sessionId) {
        return sessions.get(sessionId);
    }

    @Override
    public void accept(String t) {
        onMessage(t);
    }

    public void dispose() {
        this.onClose();
        this.transport.close();
    }

    public void onClose() {
        if (this.closed)
            return;
        this.closed = true;
        for (Messages callback : this.callbacks.values()) {
            callback.setErrorText("Protocol error " + callback.getMethod() + " Target closed.");
            if (callback.getCountDownLatch() != null) {
                callback.getCountDownLatch().countDown();
            }
        }
        this.callbacks.clear();
        for (CDPSession session : this.sessions.values())
            session.onClosed();
        this.sessions.clear();
        this.emit(Builder.Event.CONNECTION_DISCONNECTED.getName(), null);
    }

    public boolean getClosed() {
        return closed;
    }

    public ConnectionOption getConnectionOption() {
        return connectionOption;
    }

    public void setConnectionOption(ConnectionOption connectionOption) {
        this.connectionOption = connectionOption;
    }

}

