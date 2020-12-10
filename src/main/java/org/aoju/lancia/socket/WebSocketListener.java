package org.aoju.lancia.socket;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * Implemented by <tt>WebSocketClient</tt> and <tt>WebSocketServer</tt>.
 * The methods within are called by <tt>WebSocket</tt>.
 * Almost every method takes a first parameter conn which represents the source of the respective event.
 */
public interface WebSocketListener {

    /**
     * Called when an entire text frame has been received. Do whatever you want
     * here...
     *
     * @param conn    The <tt>WebSocket</tt> instance this event is occurring on.
     * @param message The UTF-8 decoded message that was received.
     */
    void onWebsocketMessage(WebSocket conn, String message);

    /**
     * Called when an entire binary frame has been received. Do whatever you want
     * here...
     *
     * @param conn The <tt>WebSocket</tt> instance this event is occurring on.
     * @param blob The binary message that was received.
     */
    void onWebsocketMessage(WebSocket conn, ByteBuffer blob);

    /**
     * Called after <var>onHandshakeReceived</var> returns <var>true</var>.
     * Indicates that a complete WebSocket connection has been established,
     * and we are ready to send/receive data.
     *
     * @param conn The <tt>WebSocket</tt> instance this event is occuring on.
     * @param d    The handshake of the websocket instance
     */
    void onWebsocketOpen(WebSocket conn, HandshakeBuilder d);

    /**
     * Called after <tt>WebSocket#close</tt> is explicity called, or when the
     * other end of the WebSocket connection is closed.
     *
     * @param ws     The <tt>WebSocket</tt> instance this event is occuring on.
     * @param code   The codes can be looked up here: {@link Framedata}
     * @param reason Additional information string
     * @param remote Returns whether or not the closing of the connection was initiated by the remote host.
     */
    void onWebsocketClose(WebSocket ws, int code, String reason, boolean remote);

    /**
     * Called if an exception worth noting occurred.
     * If an error causes the connection to fail onClose will be called additionally afterwards.
     *
     * @param conn The <tt>WebSocket</tt> instance this event is occuring on.
     * @param ex   The exception that occurred. <br>
     *             Might be null if the exception is not related to any specific connection. For example if the server port could not be bound.
     */
    void onWebsocketError(WebSocket conn, Exception ex);

    /**
     * Called a ping frame has been received.
     * This method must send a corresponding pong by itself.
     *
     * @param conn The <tt>WebSocket</tt> instance this event is occuring on.
     * @param f    The ping frame. Control frames may contain payload.
     */
    void onWebsocketPing(WebSocket conn, Framedata f);

    /**
     * Called just before a ping frame is sent, in order to allow users to customize their ping frame data.
     *
     * @param conn The <tt>WebSocket</tt> connection from which the ping frame will be sent.
     * @return PingFrame to be sent.
     */
    Framedata onPreparePing(WebSocket conn);

    /**
     * This method is used to inform the selector thread that there is data queued to be written to the socket.
     *
     * @param conn The <tt>WebSocket</tt> instance this event is occuring on.
     */
    void onWriteDemand(WebSocket conn);

    /**
     * @param conn The <tt>WebSocket</tt> instance this event is occuring on.
     * @return Returns the address of the endpoint this socket is bound to.
     * @see WebSocket#getLocalSocketAddress()
     */
    InetSocketAddress getLocalSocketAddress(WebSocket conn);

    /**
     * @param conn The <tt>WebSocket</tt> instance this event is occuring on.
     * @return Returns the address of the endpoint this socket is connected to, or{@code null} if it is unconnected.
     * @see WebSocket#getRemoteSocketAddress()
     */
    InetSocketAddress getRemoteSocketAddress(WebSocket conn);

}
