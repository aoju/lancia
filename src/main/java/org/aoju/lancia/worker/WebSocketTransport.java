/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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

import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.toolkit.ObjectKit;
import org.aoju.bus.http.socket.CoverWebSocket;
import org.aoju.bus.logger.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.function.Consumer;

/**
 * 网络套接字客户端
 *
 * @author Kimi Liu
 * @version 6.1.3
 * @since JDK 1.8+
 */
public class WebSocketTransport extends WebSocketClient implements Transport {
//public class WebSocketTransport implements Transport {

    private Consumer<String> messageConsumer = null;
    public CoverWebSocket socket;
    private Connection connection = null;

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Logger.info("Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: " + reason);
        this.onClose();
        if (ObjectKit.isNotEmpty(this.connection)) {
            this.connection.dispose();
        }
    }

    @Override
    public void onError(Exception e) {
        Logger.error(e);
    }

    public WebSocketTransport(String browserWSEndpoint) {
        super(URI.create(browserWSEndpoint));
        try {
            this.setConnectionLostTimeout(0);
            this.connectBlocking();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /* public WebSocketTransport(String browserWSEndpoint) {
         Httpv httpv = Httpv.builder().build();
         this.socket = httpv.webSocket(browserWSEndpoint).listen();
     }

     @Override
     public void close() {
         if (ObjectKit.isNotEmpty(this.connection)) {
             this.connection.dispose();
         }
     }

     */

    @Override
    public void send(String message) {
        Logger.debug(message);
        super.send(message);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        Logger.info("Websocket serverHandshake status: " + serverHandshake.getHttpStatus());
    }

    @Override
    public void onMessage(String message) {
        Assert.notNull(this.messageConsumer, "MessageConsumer must be initialized");
        this.messageConsumer.accept(message);
    }

    @Override
    public void onClose() {
        this.close();
    }

    public void addMessageConsumer(Consumer<String> consumer) {
        this.messageConsumer = consumer;
    }

    public void addConnection(Connection connection) {
        this.connection = connection;
    }

}
