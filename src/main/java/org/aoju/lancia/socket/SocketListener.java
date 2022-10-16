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

import org.aoju.lancia.worker.exception.SocketException;

import java.net.InetSocketAddress;

/**
 * 由WebSocketClient实现。其中的方法由WebSocket调用。几乎每个方法都接受第一个参数conn，它表示各自事件的源。
 */
public interface SocketListener {

    /**
     * Called on the client side when the org.aoju.lancia.socket connection is first established, and the
     * SocketBuilder handshake response has been received.
     *
     * @param conn     The WebSocket related to this event
     * @param request  The handshake initially send out to the server by this websocket.
     * @param response The handshake the server sent in response to the request.
     * @throws SocketException Allows the client to reject the connection with the server in
     *                         respect of its handshake response.
     */
    void onWebsocketHandshakeReceivedAsClient(WebSocket conn, HandshakeBuilder request,
                                              HandshakeBuilder response) throws SocketException;

    /**
     * Called on the client side when the org.aoju.lancia.socket connection is first established, and the
     * SocketBuilder handshake has just been sent.
     *
     * @param conn    The WebSocket related to this event
     * @param request The handshake sent to the server by this websocket
     * @throws SocketException Allows the client to stop the connection from progressing
     */
    void onWebsocketHandshakeSentAsClient(WebSocket conn, HandshakeBuilder request)
            throws SocketException;

    /**
     * Called when an entire text frame has been received. Do whatever you want here...
     *
     * @param conn    The WebSocket instance this event is occurring on.
     * @param message The UTF-8 decoded message that was received.
     */
    void onWebsocketMessage(WebSocket conn, String message);

    /**
     * Called after <var>onHandshakeReceived</var> returns <var>true</var>. Indicates that a complete
     * WebSocket connection has been established, and we are ready to send/receive data.
     *
     * @param conn The WebSocket instance this event is occurring on.
     * @param d    The handshake of the websocket instance
     */
    void onWebsocketOpen(WebSocket conn, HandshakeBuilder d);

    /**
     * Called after WebSocket#close is explicity called, or when the other end of the
     * WebSocket connection is closed.
     *
     * @param ws     The WebSocket instance this event is occurring on.
     * @param code   The codes can be looked up here: {@link Framedata}
     * @param reason Additional information string
     * @param remote Returns whether or not the closing of the connection was initiated by the remote
     *               host.
     */
    void onWebsocketClose(WebSocket ws, int code, String reason, boolean remote);

    /**
     * Called as soon as no further frames are accepted
     *
     * @param ws     The WebSocket instance this event is occurring on.
     * @param code   The codes can be looked up here: {@link Framedata}
     * @param reason Additional information string
     * @param remote Returns whether or not the closing of the connection was initiated by the remote
     *               host.
     */
    void onWebsocketClosing(WebSocket ws, int code, String reason, boolean remote);

    /**
     * send when this peer sends a close handshake
     *
     * @param ws     The WebSocket instance this event is occurring on.
     * @param code   The codes can be looked up here: {@link Framedata}
     * @param reason Additional information string
     */
    void onWebsocketCloseInitiated(WebSocket ws, int code, String reason);

    /**
     * Called if an exception worth noting occurred. If an error causes the connection to fail onClose
     * will be called additionally afterwards.
     *
     * @param conn The WebSocket instance this event is occurring on.
     * @param ex   The exception that occurred. <br> Might be null if the exception is not related to
     *             any specific connection. For example if the server port could not be bound.
     */
    void onWebsocketError(WebSocket conn, Exception ex);

    /**
     * This method is used to inform the selector thread that there is data queued to be written to
     * the org.aoju.lancia.socket.
     *
     * @param conn The WebSocket instance this event is occurring on.
     */
    void onWriteDemand(WebSocket conn);

    /**
     * @param conn The WebSocket instance this event is occurring on.
     * @return Returns the address of the endpoint this org.aoju.lancia.socket is bound to.
     * @see WebSocket#getLocalSocketAddress()
     */
    InetSocketAddress getLocalSocketAddress(WebSocket conn);

    /**
     * @param conn The WebSocket instance this event is occurring on.
     * @return Returns the address of the endpoint this org.aoju.lancia.socket is connected to, or{@code null} if it
     * is unconnected.
     * @see WebSocket#getRemoteSocketAddress()
     */
    InetSocketAddress getRemoteSocketAddress(WebSocket conn);

}
