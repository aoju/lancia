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
package org.aoju.lancia.socket;

import java.net.InetSocketAddress;

/**
 * 由WebSocketClient实现。其中的方法由WebSocket调用。几乎每个方法都接受第一个参数conn，它表示各自事件的源。
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public interface Listeners {

    /**
     * Called when an entire text frame has been received. Do whatever you want here...
     *
     * @param conn    The Sockets instance this event is occurring on.
     * @param message The UTF-8 decoded message that was received.
     */
    void onWebsocketMessage(Sockets conn, String message);

    /**
     * Called after <var>onHandshakeReceived</var> returns <var>true</var>. Indicates that a complete
     * Sockets connection has been established, and we are ready to send/receive data.
     *
     * @param conn The Sockets instance this event is occurring on.
     * @param d    The handshake of the websocket instance
     */
    void onWebsocketOpen(Sockets conn, HandshakeBuilder d);

    /**
     * Called after Sockets#close is explicity called, or when the other end of the
     * Sockets connection is closed.
     *
     * @param ws     The Sockets instance this event is occurring on.
     * @param code   The codes can be looked up here: {@link Framedata}
     * @param reason Additional information string
     * @param remote Returns whether or not the closing of the connection was initiated by the remote
     *               host.
     */
    void onWebsocketClose(Sockets ws, int code, String reason, boolean remote);

    /**
     * Called as soon as no further frames are accepted
     *
     * @param ws     The Sockets instance this event is occurring on.
     * @param code   The codes can be looked up here: {@link Framedata}
     * @param reason Additional information string
     * @param remote Returns whether or not the closing of the connection was initiated by the remote
     *               host.
     */
    void onWebsocketClosing(Sockets ws, int code, String reason, boolean remote);

    /**
     * send when this peer sends a close handshake
     *
     * @param ws     The Sockets instance this event is occurring on.
     * @param code   The codes can be looked up here: {@link Framedata}
     * @param reason Additional information string
     */
    void onWebsocketCloseInitiated(Sockets ws, int code, String reason);

    /**
     * Called if an exception worth noting occurred. If an error causes the connection to fail onClose
     * will be called additionally afterwards.
     *
     * @param conn The Sockets instance this event is occurring on.
     * @param ex   The exception that occurred. <br> Might be null if the exception is not related to
     *             any specific connection. For example if the server port could not be bound.
     */
    void onWebsocketError(Sockets conn, Exception ex);

    /**
     * This method is used to inform the selector thread that there is data queued to be written to
     * the org.aoju.lancia.socket.
     *
     * @param conn The Sockets instance this event is occurring on.
     */
    void onWriteDemand(Sockets conn);

    /**
     * @param conn The Sockets instance this event is occurring on.
     * @return Returns the address of the endpoint this org.aoju.lancia.socket is bound to.
     * @see Sockets#getLocalSocketAddress()
     */
    InetSocketAddress getLocalSocketAddress(Sockets conn);

    /**
     * @param conn The Sockets instance this event is occurring on.
     * @return Returns the address of the endpoint this org.aoju.lancia.socket is connected to, or{@code null} if it
     * is unconnected.
     * @see Sockets#getRemoteSocketAddress()
     */
    InetSocketAddress getRemoteSocketAddress(Sockets conn);

}
