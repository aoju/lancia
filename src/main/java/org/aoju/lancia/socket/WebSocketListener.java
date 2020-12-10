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
     * Called on the server side when the socket connection is first established, and the WebSocket
     * handshake has been received. This method allows to deny connections based on the received handshake.<br>
     * By default this method only requires protocol compliance.
     *
     * @param conn    The WebSocket related to this event
     * @param draft   The protocol draft the client uses to connect
     * @param request The opening http message send by the client. Can be used to access additional fields like cookies.
     * @return Returns an incomplete handshake containing all optional fields
     * @throws InvalidDataException Throwing this exception will cause this handshake to be rejected
     */
    HandshakeBuilder onWebsocketHandshakeReceivedAsServer(WebSocket conn, Draft draft, HandshakeBuilder request) throws InvalidDataException;

    /**
     * Called on the client side when the socket connection is first established, and the WebSocketImpl
     * handshake response has been received.
     *
     * @param conn     The WebSocket related to this event
     * @param request  The handshake initially send out to the server by this websocket.
     * @param response The handshake the server sent in response to the request.
     * @throws InvalidDataException Allows the client to reject the connection with the server in respect of its handshake response.
     */
    void onWebsocketHandshakeReceivedAsClient(WebSocket conn, HandshakeBuilder request, HandshakeBuilder response) throws InvalidDataException;

    /**
     * Called on the client side when the socket connection is first established, and the WebSocketImpl
     * handshake has just been sent.
     *
     * @param conn    The WebSocket related to this event
     * @param request The handshake sent to the server by this websocket
     * @throws InvalidDataException Allows the client to stop the connection from progressing
     */
    void onWebsocketHandshakeSentAsClient(WebSocket conn, HandshakeBuilder request) throws InvalidDataException;

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
     * Called as soon as no further frames are accepted
     *
     * @param ws     The <tt>WebSocket</tt> instance this event is occuring on.
     * @param code   The codes can be looked up here: {@link Framedata}
     * @param reason Additional information string
     * @param remote Returns whether or not the closing of the connection was initiated by the remote host.
     */
    void onWebsocketClosing(WebSocket ws, int code, String reason, boolean remote);

    /**
     * send when this peer sends a close handshake
     *
     * @param ws     The <tt>WebSocket</tt> instance this event is occuring on.
     * @param code   The codes can be looked up here: {@link Framedata}
     * @param reason Additional information string
     */
    void onWebsocketCloseInitiated(WebSocket ws, int code, String reason);

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
     * Called when a pong frame is received.
     *
     * @param conn The <tt>WebSocket</tt> instance this event is occuring on.
     * @param f    The pong frame. Control frames may contain payload.
     **/
    void onWebsocketPong(WebSocket conn, Framedata f);

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
