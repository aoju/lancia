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
package org.aoju.lancia.worker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.logger.Logger;
import org.aoju.lancia.Builder;
import org.aoju.lancia.events.EventEmitter;
import org.aoju.lancia.events.Events;
import org.aoju.lancia.kernel.page.TargetInfo;
import org.aoju.lancia.option.ConnectionOptions;
import org.aoju.lancia.worker.exception.ProtocolException;

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
    /**
     * 并发
     */
    private final Map<Long, Messages> callbacks = new ConcurrentHashMap<>();

    private final Map<String, CDPSession> sessions = new ConcurrentHashMap<>();

    private boolean closed;

    private ConnectionOptions connectionOptions;

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
        this.connectionOptions = new ConnectionOptions();
    }

    public Connection(String url, Transport transport, int delay, ConnectionOptions connectionOptions) {
        this(url, transport, delay);
        this.connectionOptions = connectionOptions == null ? new ConnectionOptions() : connectionOptions;
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

    public JsonNode send(String method, Map<String, Object> params, boolean isWait) {
        Messages message = new Messages();
        message.setMethod(method);
        message.setParams(params);
        try {
            if (isWait) {
                message.setCountDownLatch(new CountDownLatch(1));
                long id = rawSend(message, true, this.callbacks);
                message.waitForResult(0, TimeUnit.MILLISECONDS);
                if (StringKit.isNotEmpty(message.getErrorText())) {
                    throw new ProtocolException(message.getErrorText());
                }
                return callbacks.remove(id).getResult();
            } else {
                rawSend(message, false, this.callbacks);
                return null;
            }
        } catch (InterruptedException e) {
            throw new ProtocolException(e);
        }
    }

    public JsonNode send(String method, Map<String, Object> params, boolean isWait, CountDownLatch outLatch) {
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
                    throw new ProtocolException(message.getErrorText());
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
            throw new ProtocolException(e);
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
        try {
            if (putCallback) {
                callbacks.put(id, message);
            }
            String sendMsg = Builder.OBJECTMAPPER.writeValueAsString(message);
            transport.send(sendMsg);
            Logger.trace("SEND -> " + sendMsg);
            return id;
        } catch (JsonProcessingException e) {
            Logger.error("parse message fail:", e);
        }
        return -1;
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
                JsonNode readTree = org.aoju.lancia.Builder.OBJECTMAPPER.readTree(message);
                JsonNode methodNode = readTree.get(org.aoju.lancia.Builder.RECV_MESSAGE_METHOD_PROPERTY);
                String method = null;
                if (methodNode != null) {
                    method = methodNode.asText();
                }
                if ("Target.attachedToTarget".equals(method)) {
                    // attached to target -> page attached to browser
                    JsonNode paramsNode = readTree.get(org.aoju.lancia.Builder.RECV_MESSAGE_PARAMS_PROPERTY);
                    JsonNode sessionId = paramsNode.get(org.aoju.lancia.Builder.RECV_MESSAGE_SESSION_ID_PROPERTY);
                    JsonNode typeNode = paramsNode.get(org.aoju.lancia.Builder.RECV_MESSAGE_TARGETINFO_PROPERTY).get(org.aoju.lancia.Builder.RECV_MESSAGE_TYPE_PROPERTY);
                    CDPSession cdpSession = new CDPSession(this, typeNode.asText(), sessionId.asText());
                    sessions.put(sessionId.asText(), cdpSession);
                } else if ("Target.detachedFromTarget".equals(method)) {//页面与浏览器脱离关系
                    JsonNode paramsNode = readTree.get(org.aoju.lancia.Builder.RECV_MESSAGE_PARAMS_PROPERTY);
                    JsonNode sessionId = paramsNode.get(org.aoju.lancia.Builder.RECV_MESSAGE_SESSION_ID_PROPERTY);
                    String sessionIdString = sessionId.asText();
                    CDPSession cdpSession = sessions.get(sessionIdString);
                    if (cdpSession != null) {
                        cdpSession.onClosed();
                        sessions.remove(sessionIdString);
                    }
                }
                JsonNode objectSessionId = readTree.get(org.aoju.lancia.Builder.RECV_MESSAGE_SESSION_ID_PROPERTY);
                JsonNode objectId = readTree.get(org.aoju.lancia.Builder.RECV_MESSAGE_ID_PROPERTY);
                if (objectSessionId != null) {
                    // cdpsession消息，当然cdpsession来处理
                    String objectSessionIdString = objectSessionId.asText();
                    CDPSession cdpSession = this.sessions.get(objectSessionIdString);
                    if (cdpSession != null) {
                        cdpSession.onMessage(readTree);
                    }
                } else if (objectId != null) {
                    // long类型的id,说明属于这次发送消息后接受的回应
                    long id = objectId.asLong();
                    Messages callback = this.callbacks.get(id);
                    if (callback != null) {
                        try {
                            JsonNode error = readTree.get(org.aoju.lancia.Builder.RECV_MESSAGE_ERROR_PROPERTY);
                            if (error != null) {
                                if (callback.getCountDownLatch() != null) {
                                    callback.setErrorText(Builder.createProtocolError(readTree));
                                }
                            } else {
                                JsonNode result = readTree.get(org.aoju.lancia.Builder.RECV_MESSAGE_RESULT_PROPERTY);
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
                    JsonNode paramsNode = readTree.get(org.aoju.lancia.Builder.RECV_MESSAGE_PARAMS_PROPERTY);
                    this.emit(method, paramsNode);
                }
            }
        } catch (Exception e) {
            ProtocolException protocolException = new ProtocolException(e);
            throw protocolException;
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
        JsonNode result = this.send("Target.attachToTarget", params, true);
        return this.sessions.get(result.get(org.aoju.lancia.Builder.RECV_MESSAGE_SESSION_ID_PROPERTY).asText());
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
        this.emit(Events.CONNECTION_DISCONNECTED.getName(), null);
    }

    public boolean getClosed() {
        return closed;
    }

    public ConnectionOptions getConnectionOptions() {
        return connectionOptions;
    }

    public void setConnectionOptions(ConnectionOptions connectionOptions) {
        this.connectionOptions = connectionOptions == null ? new ConnectionOptions() : connectionOptions;
    }

}

