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

import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.logger.Logger;
import org.aoju.lancia.socket.Draft_6455;
import org.aoju.lancia.socket.HandshakeBuilder;
import org.aoju.lancia.socket.SocketFactory;

import java.net.URI;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 与chromuim通过Socket通信实现
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */

public class TransportBuilder extends SocketFactory implements Transport {

    private Consumer<String> messageConsumer = null;

    private Connection connection = null;

    public TransportBuilder(URI serverURI) {
        super(serverURI);
    }

    public TransportBuilder(URI serverUri, Draft_6455 draft) {
        super(serverUri, draft);
    }

    public TransportBuilder(URI serverUri, Map<String, String> httpHeaders) {
        super(serverUri, httpHeaders);
    }

    @Override
    public void send(String text) {
        if (this.connection == null) {
            Logger.warn("Transport connection is null, maybe closed?");
            return;
        }
        Logger.debug(text);
        super.send(text);
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

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Logger.info("Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code);
        this.onClose();
        if (this.connection != null) {
            this.connection.dispose();
        }
    }

    @Override
    public void onError(Exception e) {
        Logger.error("Websocket error:", e.getMessage());
    }


    @Override
    public void onOpen(HandshakeBuilder serverHandshake) {
        Logger.info("Websocket serverHandshake status: " + serverHandshake.getHttpStatus());
    }

    public void addConsumer(Consumer<String> consumer) {
        this.messageConsumer = consumer;
    }


    public void addConnection(Connection connection) {
        this.connection = connection;
    }

}
