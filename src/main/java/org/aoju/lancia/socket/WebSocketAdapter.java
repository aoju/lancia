package org.aoju.lancia.socket;

/**
 * This class default implements all methods of the WebSocketListener that can be overridden optionally when advances functionalities is needed.<br>
 **/
public abstract class WebSocketAdapter implements WebSocketListener {

    private Framedata pingFrame;

    /**
     * This default implementation does not do anything. Go ahead and overwrite it.
     *
     * @see WebSocketListener#onWebsocketHandshakeReceivedAsServer(WebSocket, Draft, HandshakeBuilder)
     */
    @Override
    public HandshakeBuilder onWebsocketHandshakeReceivedAsServer(WebSocket conn, Draft draft, HandshakeBuilder request) throws InvalidDataException {
        return new HandshakeBuilder();
    }

    @Override
    public void onWebsocketHandshakeReceivedAsClient(WebSocket conn, HandshakeBuilder request, HandshakeBuilder response) throws InvalidDataException {
        //To overwrite
    }

    /**
     * This default implementation does not do anything which will cause the connections to always progress.
     *
     * @see WebSocketListener#onWebsocketHandshakeSentAsClient(WebSocket, HandshakeBuilder)
     */
    @Override
    public void onWebsocketHandshakeSentAsClient(WebSocket conn, HandshakeBuilder request) {
        //To overwrite
    }

    /**
     * This default implementation will send a pong in response to the received ping.
     * The pong frame will have the same payload as the ping frame.
     *
     * @see WebSocketListener#onWebsocketPing(WebSocket, Framedata)
     */
    @Override
    public void onWebsocketPing(WebSocket conn, Framedata f) {
        conn.sendFrame(f);
    }

    /**
     * This default implementation does not do anything. Go ahead and overwrite it.
     *
     * @see WebSocketListener#onWebsocketPong(WebSocket, Framedata)
     */
    @Override
    public void onWebsocketPong(WebSocket conn, Framedata f) {
        //To overwrite
    }

    /**
     * Default implementation for onPreparePing, returns a (cached) PingFrame that has no application data.
     *
     * @param conn The <tt>WebSocket</tt> connection from which the ping frame will be sent.
     * @return PingFrame to be sent.
     * @see WebSocketListener#onPreparePing(WebSocket)
     */
    @Override
    public Framedata onPreparePing(WebSocket conn) {
        if (pingFrame == null)
            pingFrame = new Framedata(HandshakeState.Opcode.PING);
        return pingFrame;
    }

}
