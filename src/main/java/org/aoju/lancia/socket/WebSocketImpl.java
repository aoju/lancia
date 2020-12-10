package org.aoju.lancia.socket;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.logger.Logger;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Represents one end (client or server) of a single WebSocketImpl connection.
 * Takes care of the "handshake" phase, then allows for easy sending of
 * text frames, and receiving frames through an event-based model.
 */
public class WebSocketImpl implements WebSocket {

    /**
     * The default port of WebSockets, as defined in the spec. If the nullary
     * constructor is used, DEFAULT_PORT will be the port the WebSocketServer
     * is binded to. Note that ports under 1024 usually require root permissions.
     */
    public static final int DEFAULT_PORT = 80;

    /**
     * The default wss port of WebSockets, as defined in the spec. If the nullary
     * constructor is used, DEFAULT_WSS_PORT will be the port the WebSocketServer
     * is binded to. Note that ports under 1024 usually require root permissions.
     */
    public static final int DEFAULT_WSS_PORT = 443;

    /**
     * Initial buffer size
     */
    public static final int RCVBUF = 16384;
    /**
     * Queue of buffers that need to be sent to the client.
     */
    public final BlockingQueue<ByteBuffer> outQueue;
    /**
     * Queue of buffers that need to be processed
     */
    public final BlockingQueue<ByteBuffer> inQueue;
    /**
     * The listener to notify of WebSocket events.
     */
    private final WebSocketListener wsl;
    /**
     * Attribut to synchronize the write
     */
    private final Object synchronizeWriteObject = new Object();
    /**
     * When true no further frames may be submitted to be sent
     */
    private boolean flushandclosestate = false;
    /**
     * The current state of the connection
     */
    private volatile HandshakeState.ReadyState readyState = HandshakeState.ReadyState.NOT_YET_CONNECTED;
    /**
     * The draft which is used by this websocket
     */
    private Draft draft = null;
    /**
     * the bytes of an incomplete received handshake
     */
    private ByteBuffer tmpHandshakeBytes = ByteBuffer.allocate(0);
    /**
     * stores the handshake sent by this websocket ( Role.CLIENT only )
     */
    private HandshakeBuilder handshakerequest = null;
    private String closemessage = null;
    private Integer closecode = null;
    private Boolean closedremotely = null;
    private String resourceDescriptor = null;
    /**
     * Attribute, when the last pong was recieved
     */
    private long lastPong = System.nanoTime();
    /**
     * Attribute to store connection attachment
     *
     * @since 1.3.7
     */
    private Object attachment;

    /**
     * creates a websocket with client role
     *
     * @param listener The listener for this instance
     * @param draft    The draft which should be used
     */
    public WebSocketImpl(WebSocketListener listener, Draft draft) {
        if (listener == null || draft == null)// socket can be null because we want do be able to create the object without already having a bound channel
            throw new IllegalArgumentException("parameters must not be null");
        this.outQueue = new LinkedBlockingQueue<>();
        inQueue = new LinkedBlockingQueue<>();
        this.wsl = listener;
        if (draft != null)
            this.draft = draft.copyInstance();
    }

    /**
     * Method to decode the provided ByteBuffer
     *
     * @param socketBuffer the ByteBuffer to decode
     */
    public void decode(ByteBuffer socketBuffer) {
        assert (socketBuffer.hasRemaining());
        Logger.trace("process({}): ({})", socketBuffer.remaining(), (socketBuffer.remaining() > 1000 ? "too big to display" : new String(socketBuffer.array(), socketBuffer.position(), socketBuffer.remaining())));

        if (readyState != HandshakeState.ReadyState.NOT_YET_CONNECTED) {
            if (readyState == HandshakeState.ReadyState.OPEN) {
                decodeFrames(socketBuffer);
            }
        } else {
            if (decodeHandshake(socketBuffer) && (!isClosing() && !isClosed())) {
                assert (tmpHandshakeBytes.hasRemaining() != socketBuffer.hasRemaining() || !socketBuffer.hasRemaining()); // the buffers will never have remaining bytes at the same time
                if (socketBuffer.hasRemaining()) {
                    decodeFrames(socketBuffer);
                } else if (tmpHandshakeBytes.hasRemaining()) {
                    decodeFrames(tmpHandshakeBytes);
                }
            }
        }
    }

    /**
     * Returns whether the handshake phase has is completed.
     * In case of a broken handshake this will be never the case.
     **/
    private boolean decodeHandshake(ByteBuffer socketBufferNew) {
        ByteBuffer socketBuffer;
        if (tmpHandshakeBytes.capacity() == 0) {
            socketBuffer = socketBufferNew;
        } else {
            if (tmpHandshakeBytes.remaining() < socketBufferNew.remaining()) {
                ByteBuffer buf = ByteBuffer.allocate(tmpHandshakeBytes.capacity() + socketBufferNew.remaining());
                tmpHandshakeBytes.flip();
                buf.put(tmpHandshakeBytes);
                tmpHandshakeBytes = buf;
            }

            tmpHandshakeBytes.put(socketBufferNew);
            tmpHandshakeBytes.flip();
            socketBuffer = tmpHandshakeBytes;
        }
        socketBuffer.mark();

        HandshakeBuilder tmphandshake = draft.translateHandshakeHttp(socketBuffer);
        if (!(tmphandshake instanceof HandshakeBuilder)) {
            Logger.trace("Closing due to protocol error: wrong http function");
            flushAndClose(Framedata.PROTOCOL_ERROR, "wrong http function", false);
            return false;
        }
        open(tmphandshake);
        return true;
    }

    private void decodeFrames(ByteBuffer socketBuffer) {
        List<Framedata> frames;
        try {
            frames = draft.translateFrame(socketBuffer);
            for (Framedata f : frames) {
                Logger.trace("matched frame: {}", f);
                draft.processFrame(this, f);
            }
        } catch (InvalidDataException e) {
            Logger.error("Closing due to invalid data in frame", e);
            wsl.onWebsocketError(this, e);
            close(e);
        }
    }

    public synchronized void close(int code, String message, boolean remote) {
        if (readyState != HandshakeState.ReadyState.CLOSING && readyState != HandshakeState.ReadyState.CLOSED) {
            if (readyState == HandshakeState.ReadyState.OPEN) {
                if (code == Framedata.ABNORMAL_CLOSE) {
                    assert (!remote);
                    readyState = HandshakeState.ReadyState.CLOSING;
                    flushAndClose(code, message, false);
                    return;
                }
                if (draft.getCloseHandshakeType() != HandshakeState.CloseHandshakeType.NONE) {

                    if (isOpen()) {
                        Framedata framedata = new Framedata(HandshakeState.Opcode.CLOSING);
                        framedata.isValid();
                        sendFrame(framedata);
                    }
                }
                flushAndClose(code, message, remote);
            } else if (code == Framedata.FLASHPOLICY) {
                assert (remote);
                flushAndClose(Framedata.FLASHPOLICY, message, true);
            } else if (code == Framedata.PROTOCOL_ERROR) { // this endpoint found a PROTOCOL_ERROR
                flushAndClose(code, message, remote);
            } else {
                flushAndClose(Framedata.NEVER_CONNECTED, message, false);
            }
            readyState = HandshakeState.ReadyState.CLOSING;
            tmpHandshakeBytes = null;
            return;
        }
    }

    @Override
    public void close(int code, String message) {
        close(code, message, false);
    }

    /**
     * This will close the connection immediately without a proper close handshake.
     * The code and the message therefore won't be transfered over the wire also they will be forwarded to onClose/onWebsocketClose.
     *
     * @param code    the closing code
     * @param message the closing message
     * @param remote  Indicates who "generated" <code>code</code>.<br>
     *                <code>true</code> means that this endpoint received the <code>code</code> from the other endpoint.<br>
     *                false means this endpoint decided to send the given code,<br>
     *                <code>remote</code> may also be true if this endpoint started the closing handshake since the other endpoint may not simply echo the <code>code</code> but close the connection the same time this endpoint does do but with an other <code>code</code>. <br>
     **/
    public synchronized void closeConnection(int code, String message, boolean remote) {
        if (readyState == HandshakeState.ReadyState.CLOSED) {
            return;
        }
        //Methods like eot() call this method without calling onClose(). Due to that reason we have to adjust the ReadyState manually
        if (readyState == HandshakeState.ReadyState.OPEN) {
            if (code == Framedata.ABNORMAL_CLOSE) {
                readyState = HandshakeState.ReadyState.CLOSING;
            }
        }
        try {
            this.wsl.onWebsocketClose(this, code, message, remote);
        } catch (RuntimeException e) {

            wsl.onWebsocketError(this, e);
        }
        if (draft != null)
            draft.reset();
        handshakerequest = null;
        readyState = HandshakeState.ReadyState.CLOSED;
    }

    protected void closeConnection(int code, boolean remote) {
        closeConnection(code, "", remote);
    }

    public void closeConnection(int code, String message) {
        closeConnection(code, message, false);
    }

    public synchronized void flushAndClose(int code, String message, boolean remote) {
        if (flushandclosestate) {
            return;
        }
        closecode = code;
        closemessage = message;
        closedremotely = remote;

        flushandclosestate = true;

        wsl.onWriteDemand(this); // ensures that all outgoing frames are flushed before closing the connection
        if (draft != null)
            draft.reset();
        handshakerequest = null;
    }

    public void eot() {
        if (readyState == HandshakeState.ReadyState.NOT_YET_CONNECTED) {
            closeConnection(Framedata.NEVER_CONNECTED, true);
        } else if (flushandclosestate) {
            closeConnection(closecode, closemessage, closedremotely);
        } else if (draft.getCloseHandshakeType() == HandshakeState.CloseHandshakeType.NONE) {
            closeConnection(Framedata.NORMAL, true);
        } else if (draft.getCloseHandshakeType() == HandshakeState.CloseHandshakeType.ONEWAY) {
            closeConnection(Framedata.NORMAL, true);
        } else {
            closeConnection(Framedata.ABNORMAL_CLOSE, true);
        }
    }

    @Override
    public void close(int code) {
        close(code, "", false);
    }

    public void close(InvalidDataException e) {
        close(e.getCloseCode(), e.getMessage(), false);
    }

    /**
     * Send Text data to the other end.
     */
    @Override
    public void send(String text) {
        if (text == null)
            throw new IllegalArgumentException("Cannot send 'null' data to a WebSocketImpl.");
        send(draft.createFrames(text));
    }

    /**
     * Send Binary data (plain bytes) to the other end.
     *
     * @throws IllegalArgumentException the data is null
     */
    @Override
    public void send(ByteBuffer bytes) {
        if (bytes == null)
            throw new IllegalArgumentException("Cannot send 'null' data to a WebSocketImpl.");
        send(draft.createFrames(bytes));
    }

    @Override
    public void send(byte[] bytes) {
        send(ByteBuffer.wrap(bytes));
    }

    private void send(Collection<Framedata> frames) {
        if (!isOpen()) {
            throw new InstrumentException();
        }
        if (frames == null) {
            throw new IllegalArgumentException();
        }
        ArrayList<ByteBuffer> outgoingFrames = new ArrayList<>();
        for (Framedata f : frames) {
            Logger.trace("send frame: {}", f);
            outgoingFrames.add(draft.createBinaryFrame(f));
        }
        write(outgoingFrames);
    }

    @Override
    public void sendFragmentedFrame(HandshakeState.Opcode op, ByteBuffer buffer, boolean fin) {
        send(draft.continuousFrame(op, buffer, fin));
    }

    @Override
    public void sendFrame(Collection<Framedata> frames) {
        send(frames);
    }

    @Override
    public void sendFrame(Framedata framedata) {
        send(Collections.singletonList(framedata));
    }

    public void sendPing() throws NullPointerException {
        // Gets a PingFrame from WebSocketListener(wsl) and sends it.
        Framedata pingFrame = wsl.onPreparePing(this);
        if (pingFrame == null)
            throw new NullPointerException("onPreparePing(WebSocket) returned null. PingFrame to sent can't be null.");
        sendFrame(pingFrame);
    }

    @Override
    public boolean hasBufferedData() {
        return !this.outQueue.isEmpty();
    }

    public void startHandshake(HandshakeBuilder handshakedata) throws InstrumentException {
        // Store the Handshake Request we are about to send
        this.handshakerequest = draft.postProcessHandshakeRequestAsClient(handshakedata);

        resourceDescriptor = handshakedata.getResourceDescriptor();
        assert (resourceDescriptor != null);

        write(draft.createHandshake(this.handshakerequest, true));
    }

    private void write(ByteBuffer buf) {
        Logger.trace("write({}): {}", buf.remaining(), buf.remaining() > 1000 ? "too big to display" : new String(buf.array()));

        outQueue.add(buf);
        wsl.onWriteDemand(this);
    }

    /**
     * Write a list of bytebuffer (frames in binary form) into the outgoing queue
     *
     * @param bufs the list of bytebuffer
     */
    private void write(List<ByteBuffer> bufs) {
        synchronized (synchronizeWriteObject) {
            for (ByteBuffer b : bufs) {
                write(b);
            }
        }
    }

    private void open(HandshakeBuilder d) {
        Logger.trace("open using draft: {}", draft);
        readyState = HandshakeState.ReadyState.OPEN;
        try {
            wsl.onWebsocketOpen(this, d);
        } catch (RuntimeException e) {
            wsl.onWebsocketError(this, e);
        }
    }

    @Override
    public boolean isOpen() {
        return readyState == HandshakeState.ReadyState.OPEN;
    }

    @Override
    public boolean isClosing() {
        return readyState == HandshakeState.ReadyState.CLOSING;
    }

    @Override
    public boolean isFlushAndClose() {
        return flushandclosestate;
    }

    @Override
    public boolean isClosed() {
        return readyState == HandshakeState.ReadyState.CLOSED;
    }

    @Override
    public HandshakeState.ReadyState getReadyState() {
        return readyState;
    }

    @Override
    public InetSocketAddress getRemoteSocketAddress() {
        return wsl.getRemoteSocketAddress(this);
    }

    @Override
    public InetSocketAddress getLocalSocketAddress() {
        return wsl.getLocalSocketAddress(this);
    }

    @Override
    public void close() {
        close(Framedata.NORMAL);
    }

    /**
     * Getter for the last pong recieved
     *
     * @return the timestamp for the last recieved pong
     */
    long getLastPong() {
        return lastPong;
    }

    /**
     * Update the timestamp when the last pong was received
     */
    public void updateLastPong() {
        this.lastPong = System.nanoTime();
    }

    /**
     * Getter for the websocket listener
     *
     * @return the websocket listener associated with this instance
     */
    public WebSocketListener getWebSocketListener() {
        return wsl;
    }

    @Override
    public <T> T getAttachment() {
        return (T) attachment;
    }

    @Override
    public <T> void setAttachment(T attachment) {
        this.attachment = attachment;
    }

}
