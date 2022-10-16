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

import org.aoju.bus.core.exception.InternalException;
import org.aoju.bus.logger.Logger;

import java.net.URI;

/**
 * 传输工厂
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class TransportFactory {

    /**
     * 创建套接字传输协议
     *
     * @param browserWSEndpoint 连接websocket的地址
     * @return WebSocketTransport/PipeTransport 客户端
     */
    public static Transport create(String browserWSEndpoint) {
        try {
            return socket(browserWSEndpoint);
        } catch (InternalException | InterruptedException e) {
            Logger.warn(e.getMessage());
            return pipe();
        }
    }

    /**
     * create websocket client
     *
     * @param browserWSEndpoint 连接websocket的地址
     * @return TransportBuilder websocket客户端
     * @throws InterruptedException 被打断异常
     */
    public static Transport socket(String browserWSEndpoint) throws InterruptedException {
        TransportBuilder client = new TransportBuilder(URI.create(browserWSEndpoint));
        // 保持websokcet连接
        client.setConnectionLostTimeout(0);
        client.connectBlocking();
        return client;
    }

    /**
     * 创建套接字传输协议
     */
    public static Transport pipe() {
        return new PipeTransport();
    }

}
