package org.aoju.lancia.socket;

import org.aoju.bus.core.io.ByteString;
import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.thread.NamedThreadFactory;
import org.aoju.bus.http.Request;
import org.aoju.bus.http.socket.WebSocket;
import org.aoju.bus.logger.Logger;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;

/**
 * Represents one end (client or server) of a single WebSocketImpl connection.
 * Takes care of the "handshake" phase, then allows for easy sending of
 * text frames, and receiving frames through an event-based model.
 */
public abstract class RFCWebSocket implements WebSocket, Runnable {

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

    public static final String NOT_YET_CONNECTED = "NOT_YET_CONNECTED";

    public static final String OPEN = "OPEN";

    public static final String CLOSING = "CLOSING";

    public static final String CLOSED = "CLOSED";


    /**
     * indicates a normal closure, meaning whatever purpose the
     * connection was established for has been fulfilled.
     */
    public static final int NORMAL = 1000;
    /**
     * 1002 indicates that an endpoint is terminating the connection due
     * to a protocol error.
     */
    public static final int PROTOCOL_ERROR = 1002;
    /**
     * 1006 is a reserved value and MUST NOT be set as a status code in a
     * Close control frame by an endpoint. It is designated for use in
     * applications expecting a status code to indicate that the
     * connection was closed abnormally, e.g. without sending or
     * receiving a Close control frame.
     */
    public static final int ABNORMAL_CLOSE = 1006;

    /**
     * The connection had never been established
     */
    public static final int NEVER_CONNECTED = -1;

    /**
     * The connection was flushed and closed
     */
    public static final int FLASHPOLICY = -3;

    /**
     * Attribut to synchronize the write
     */
    private final Object object = new Object();
    /**
     * The SocketFactory for this WebSocketClient
     */
    private final SocketFactory socketFactory = null;
    /**
     * The used proxy, if any
     */
    private final Proxy proxy = Proxy.NO_PROXY;
    /**
     * Attribute to sync on
     */
    private final Object syncConnectionLost = new Object();
    /**
     * The draft which is used by this websocket
     */
    private final Draft draft;
    /**
     * Attribute, when the last pong was recieved
     */
    private final long lastPong = System.nanoTime();
    /**
     * The socket timeout value to be used in milliseconds.
     */
    private final int connectTimeout;
    /**
     * DNS resolver that translates a URI to an InetAddress
     */
    private final RFCWebSocket.DnsResolver dnsResolver;
    /**
     * Attribute for the lost connection check interval in nanoseconds
     */
    private final long connectionLostTimeout = TimeUnit.SECONDS.toNanos(60);
    /**
     * Queue of buffers that need to be sent to the client.
     */
    public BlockingQueue<ByteBuffer> outQueue;
    /**
     * Queue of buffers that need to be processed
     */
    public BlockingQueue<ByteBuffer> inQueue;
    /**
     * The current state of the connection
     */
    private volatile String readyState = NOT_YET_CONNECTED;
    /**
     * The socket for this WebSocketClient
     */
    public Socket socket = null;
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
     * The used OutputStream
     */
    public OutputStream ostream;
    /**
     * The thread to write outgoing message
     */
    public Thread writeThread;
    /**
     * The URI this channel is supposed to connect to.
     */
    protected URI uri;
    /**
     * When true no further frames may be submitted to be sent
     */
    private boolean flushandclosestate = false;
    /**
     * The thread to connect and read message
     */
    private Thread connectReadThread;
    /**
     * The additional headers to use
     */
    private Map<String, String> headers;
    /**
     * The latch for connectBlocking()
     */
    private CountDownLatch connectLatch = new CountDownLatch(1);
    /**
     * The latch for closeBlocking()
     */
    private CountDownLatch closeLatch = new CountDownLatch(1);
    /**
     * Attribute which allows you to deactivate the Nagle's algorithm
     */
    private boolean tcpNoDelay;
    /**
     * Attribute which allows you to enable/disable the SO_REUSEADDR socket option.
     */
    private boolean reuseAddr;
    /**
     * Attribute for a service that triggers lost connection checking
     */
    private ScheduledExecutorService connectionLostCheckerService;
    /**
     * Attribute for a task that checks for lost connections
     */
    private ScheduledFuture connectionLostCheckerFuture;

    /**
     * Constructs a WebSocketClient instance and sets it to the connect to the
     * specified URI. The channel does not attampt to connect automatically. The connection
     * will be established once you call <var>connect</var>.
     *
     * @param serverUri the server URI to connect to
     */
    public RFCWebSocket(URI serverUri) {
        this(serverUri, new Draft());
    }

    /**
     * Constructs a WebSocketClient instance and sets it to the connect to the
     * specified URI. The channel does not attampt to connect automatically. The connection
     * will be established once you call <var>connect</var>.
     *
     * @param serverUri the server URI to connect to
     * @param draft     The draft which should be used for this connection
     */
    public RFCWebSocket(URI serverUri, Draft draft) {
        this(serverUri, draft, null, 0);
    }

    /**
     * Constructs a WebSocketClient instance and sets it to the connect to the
     * specified URI. The channel does not attampt to connect automatically. The connection
     * will be established once you call <var>connect</var>.
     *
     * @param serverUri      the server URI to connect to
     * @param draft          The draft which should be used for this connection
     * @param httpHeaders    Additional HTTP-Headers
     * @param connectTimeout The Timeout for the connection
     */
    public RFCWebSocket(URI serverUri, Draft draft, Map<String, String> httpHeaders, int connectTimeout) {
        if (serverUri == null) {
            throw new IllegalArgumentException();
        } else if (draft == null) {
            throw new IllegalArgumentException("null as draft is permitted for `WebSocketServer` only!");
        }
        this.draft = draft.copyInstance();
        this.uri = serverUri;
        this.outQueue = new LinkedBlockingQueue<>();
        this.inQueue = new LinkedBlockingQueue<>();
        this.dnsResolver = uri -> InetAddress.getByName(uri.getHost());
        if (httpHeaders != null) {
            headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            headers.putAll(httpHeaders);
        }
        this.connectTimeout = connectTimeout;
        setTcpNoDelay(false);
        setReuseAddr(false);
    }

    /**
     * Called after an opening handshake has been performed and the given websocket is ready to be written on.
     *
     * @param handshakedata The handshake of the websocket instance
     */
    public abstract void onOpen(HandshakeBuilder handshakedata);

    /**
     * Callback for string messages received from the remote host
     *
     * @param message The UTF-8 decoded message that was received.
     **/
    public abstract void onMessage(String message);

    /**
     * Called after the websocket connection has been closed.
     *
     * @param code   The codes can be looked up here: {@link Framedata}
     * @param reason Additional information string
     * @param remote Returns whether or not the closing of the connection was initiated by the remote host.
     **/
    public abstract void onClose(int code, String reason, boolean remote);

    /**
     * Called when errors occurs. If an error causes the websocket connection to fail {@link #onClose(int, String, boolean)} will be called additionally.<br>
     * This method will be called primarily because of IO or protocol errors.<br>
     * If the given exception is an RuntimeException that probably means that you encountered a bug.<br>
     *
     * @param ex The exception causing this error
     **/
    public abstract void onError(Exception ex);

    @Override
    public Request request() {
        return null;
    }

    @Override
    public long queueSize() {
        return 0;
    }

    /**
     * Send Text data to the other end.
     */
    @Override
    public boolean send(String text) {
        if (text == null)
            throw new IllegalArgumentException("Cannot send 'null' data to a WebSocketImpl.");
        ByteBuffer byteBuffer = ByteBuffer.wrap(text.getBytes(Charset.UTF_8));
        if (!isOpen()) {
            throw new InstrumentException();
        }
        if (byteBuffer == null) {
            throw new IllegalArgumentException();
        }
        write(draft.createBinaryFrame(byteBuffer));
        return true;
    }

    @Override
    public boolean send(ByteString bytes) {
        return false;
    }

    @Override
    public boolean close(int code, String message) {
        close(code, message, false);
        return true;
    }

    @Override
    public void cancel() {

    }

    /**
     * Method to decode the provided ByteBuffer
     *
     * @param socketBuffer the ByteBuffer to decode
     */
    public void decode(ByteBuffer socketBuffer) {
        assert (socketBuffer.hasRemaining());
        Logger.trace("process({}): ({})", socketBuffer.remaining(), (socketBuffer.remaining() > 1000 ? "too big to display" : new String(socketBuffer.array(), socketBuffer.position(), socketBuffer.remaining())));

        if (readyState != NOT_YET_CONNECTED) {
            if (readyState == OPEN) {
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

        HandshakeBuilder tmphandshake = Draft.translateHandshakeHttp(socketBuffer);
        if (!(tmphandshake instanceof HandshakeBuilder)) {
            Logger.trace("Closing due to protocol error: wrong http function");
            flushAndClose(PROTOCOL_ERROR, "wrong http function", false);
            return false;
        }
        open(tmphandshake);
        return true;
    }

    public synchronized void close(int code, String message, boolean remote) {
        if (readyState != CLOSING && readyState != CLOSED) {
            if (readyState == OPEN) {
                if (code == ABNORMAL_CLOSE) {
                    assert (!remote);
                    readyState = CLOSING;
                    flushAndClose(code, message, false);
                    return;
                }
                flushAndClose(code, message, remote);
            } else if (code == FLASHPOLICY) {
                assert (remote);
                flushAndClose(FLASHPOLICY, message, true);
            } else if (code == PROTOCOL_ERROR) { // this endpoint found a PROTOCOL_ERROR
                flushAndClose(code, message, remote);
            } else {
                flushAndClose(NEVER_CONNECTED, message, false);
            }
            readyState = CLOSING;
            tmpHandshakeBytes = null;
            return;
        }
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
        if (readyState == CLOSED) {
            return;
        }
        if (readyState == OPEN) {
            if (code == ABNORMAL_CLOSE) {
                readyState = CLOSING;
            }
        }
        try {
            this.onWebsocketClose(code, message, remote);
        } catch (RuntimeException e) {
            this.onWebsocketError(e);
        }
        if (draft != null)
            draft.byteBuffer = null;
        handshakerequest = null;
        readyState = CLOSED;
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

        if (draft != null)
            draft.byteBuffer = null;
        handshakerequest = null;
    }

    public void eot() {
        if (readyState == NOT_YET_CONNECTED) {
            closeConnection(NEVER_CONNECTED, true);
        } else if (flushandclosestate) {
            closeConnection(closecode, closemessage, closedremotely);
        } else {
            closeConnection(ABNORMAL_CLOSE, true);
        }
    }

    public void close(int code) {
        close(code, "", false);
    }

    public void close(InstrumentException e) {
        close(0, e.getMessage(), false);
    }

    public void sendPing() throws NullPointerException {
        Framedata pingFrame = new Framedata();
        if (pingFrame == null)
            throw new NullPointerException("onPreparePing(WebSocket) returned null. PingFrame to sent can't be null.");
        send((ByteString) Collections.singletonList(pingFrame));
    }

    public void startHandshake(HandshakeBuilder handshakedata) throws InstrumentException {
        this.handshakerequest = draft.postProcessHandshakeRequestAsClient(handshakedata);
        resourceDescriptor = handshakedata.getResourceDescriptor();
        assert (resourceDescriptor != null);
        write(draft.createHandshake(this.handshakerequest, true));
    }

    public boolean isOpen() {
        return readyState == OPEN;
    }

    public boolean isClosing() {
        return readyState == CLOSING;
    }

    public boolean isClosed() {
        return readyState == CLOSED;
    }

    public void close() {
        close(NORMAL);
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
     * Getter for the websocket listener
     *
     * @return the websocket listener associated with this instance
     */
    public RFCWebSocket getWebSocketListener() {
        return this;
    }

    /**
     * Returns the socket to allow Hostname Verification
     *
     * @return the socket used for this connection
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * Reinitiates the websocket connection. This method does not block.
     */
    public void reconnect() {
        reset();
        connect();
    }

    /**
     * Initiates the websocket connection. This method does not block.
     */
    public void connect() {
        if (connectReadThread != null)
            throw new IllegalStateException("WebSocketClient objects are not reuseable");
        connectReadThread = new Thread(this);
        connectReadThread.setName("WebSocketConnectReadThread-" + connectReadThread.getId());
        connectReadThread.start();
    }

    /**
     * Same as <code>connect</code> but blocks until the websocket connected or failed to do so.<br>
     *
     * @return Returns whether it succeeded or not.
     * @throws InterruptedException Thrown when the threads get interrupted
     */
    public boolean connectBlocking() throws InterruptedException {
        connect();
        connectLatch.await();
        return this.isOpen();
    }

    /**
     * Same as <code>close</code> but blocks until the websocket closed or failed to do so.<br>
     *
     * @throws InterruptedException Thrown when the threads get interrupted
     */
    public void closeBlocking() throws InterruptedException {
        close();
        closeLatch.await();
    }

    protected List<WebSocket> getConnections() {
        return Collections.singletonList(this);
    }

    public void run() {
        InputStream istream;
        try {
            boolean isNewSocket = false;
            if (socketFactory != null) {
                socket = socketFactory.createSocket();
            } else if (socket == null) {
                socket = new Socket(proxy);
                isNewSocket = true;
            } else if (socket.isClosed()) {
                throw new IOException();
            }

            socket.setTcpNoDelay(isTcpNoDelay());
            socket.setReuseAddress(isReuseAddr());

            if (!socket.isConnected()) {
                InetSocketAddress addr = new InetSocketAddress(dnsResolver.resolve(uri), this.getPort());
                socket.connect(addr, connectTimeout);
            }

            // if the socket is set by others we don't apply any TLS wrapper
            if (isNewSocket && "wss".equals(uri.getScheme())) {
                SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
                sslContext.init(null, null, null);
                SSLSocketFactory factory = sslContext.getSocketFactory();
                socket = factory.createSocket(socket, uri.getHost(), getPort(), true);
            }

            istream = socket.getInputStream();
            ostream = socket.getOutputStream();

            sendHandshake();
        } catch ( /*IOException | SecurityException | UnresolvedAddressException | InstrumentException | ClosedByInterruptException | SocketTimeoutException */Exception e) {
            onWebsocketError(e);
            this.closeConnection(NEVER_CONNECTED, e.getMessage());
            return;
        } catch (InternalError e) {
            // https://bugs.openjdk.java.net/browse/JDK-8173620
            if (e.getCause() instanceof InvocationTargetException && e.getCause().getCause() instanceof IOException) {
                IOException cause = (IOException) e.getCause().getCause();
                onWebsocketError(cause);
                this.closeConnection(NEVER_CONNECTED, cause.getMessage());
                return;
            }
            throw e;
        }

        writeThread = new Thread(new RFCWebSocket.WriteThread(this));
        writeThread.start();

        byte[] rawbuffer = new byte[RFCWebSocket.RCVBUF];
        int readBytes;

        try {
            while (!isClosing() && !isClosed() && (readBytes = istream.read(rawbuffer)) != -1) {
                this.decode(ByteBuffer.wrap(rawbuffer, 0, readBytes));
            }
            this.eot();
        } catch (IOException e) {
            handleIOException(e);
        } catch (RuntimeException e) {
            // this catch case covers internal errors only and indicates a bug in this websocket implementation
            onError(e);
            this.closeConnection(ABNORMAL_CLOSE, e.getMessage());
        }
        connectReadThread = null;
    }

    /**
     * Calls subclass' implementation of <var>onMessage</var>.
     */
    public final void onWebsocketMessage(String message) {
        onMessage(message);
    }

    /**
     * Calls subclass' implementation of <var>onOpen</var>.
     */
    public final void onWebsocketOpen(HandshakeBuilder handshake) {
        startConnectionLostTimer();
        onOpen(handshake);
        connectLatch.countDown();
    }

    /**
     * Calls subclass' implementation of <var>onClose</var>.
     */
    public final void onWebsocketClose(int code, String reason, boolean remote) {
        stopConnectionLostTimer();
        if (writeThread != null)
            writeThread.interrupt();
        onClose(code, reason, remote);
        connectLatch.countDown();
        closeLatch.countDown();
    }

    /**
     * Calls subclass' implementation of <var>onIOError</var>.
     */
    public final void onWebsocketError(Exception ex) {
        onError(ex);
    }

    /**
     * Getter for the engine
     *
     * @return the engine
     */
    public WebSocket getConnection() {
        return this;
    }

    /**
     * Method to give some additional info for specific IOExceptions
     *
     * @param e the IOException causing a eot.
     */
    public void handleIOException(IOException e) {
        if (e instanceof SSLException) {
            onError(e);
        }
        this.eot();
    }

    /**
     * Stop the connection lost timer
     */
    protected void stopConnectionLostTimer() {
        synchronized (syncConnectionLost) {
            if (connectionLostCheckerService != null || connectionLostCheckerFuture != null) {
                Logger.trace("Connection lost timer stopped");
                cancelConnectionLostTimer();
            }
        }
    }

    /**
     * Start the connection lost timer
     */
    protected void startConnectionLostTimer() {
        synchronized (syncConnectionLost) {
            if (this.connectionLostTimeout <= 0) {
                Logger.trace("Connection lost timer deactivated");
                return;
            }
            Logger.trace("Connection lost timer started");
            restartConnectionLostTimer();
        }
    }

    /**
     * Tests if TCP_NODELAY is enabled.
     *
     * @return a boolean indicating whether or not TCP_NODELAY is enabled for new connections.
     */
    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    /**
     * Setter for tcpNoDelay
     * Enable/disable TCP_NODELAY (disable/enable Nagle's algorithm) for new connections
     *
     * @param tcpNoDelay true to enable TCP_NODELAY, false to disable.
     */
    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    /**
     * Tests Tests if SO_REUSEADDR is enabled.
     *
     * @return a boolean indicating whether or not SO_REUSEADDR is enabled.
     */
    public boolean isReuseAddr() {
        return reuseAddr;
    }

    /**
     * Setter for soReuseAddr
     * Enable/disable SO_REUSEADDR for the socket
     *
     * @param reuseAddr whether to enable or disable SO_REUSEADDR
     */
    public void setReuseAddr(boolean reuseAddr) {
        this.reuseAddr = reuseAddr;
    }

    /**
     * Extract the specified port
     *
     * @return the specified port or the default port for the specific scheme
     */
    private int getPort() {
        int port = uri.getPort();
        if (port == -1) {
            String scheme = uri.getScheme();
            if ("wss".equals(scheme)) {
                return RFCWebSocket.DEFAULT_WSS_PORT;
            } else if ("ws".equals(scheme)) {
                return RFCWebSocket.DEFAULT_PORT;
            } else {
                throw new IllegalArgumentException("unknown scheme: " + scheme);
            }
        }
        return port;
    }

    /**
     * Reset everything relevant to allow a reconnect
     */
    private void reset() {
        Thread current = Thread.currentThread();
        if (current == writeThread || current == connectReadThread) {
            throw new IllegalStateException("You cannot initialize a reconnect out of the websocket thread. Use reconnect in another thread to insure a successful cleanup.");
        }
        try {
            closeBlocking();
            if (writeThread != null) {
                this.writeThread.interrupt();
                this.writeThread = null;
            }
            if (connectReadThread != null) {
                this.connectReadThread.interrupt();
                this.connectReadThread = null;
            }
            this.draft.byteBuffer = null;
            if (this.socket != null) {
                this.socket.close();
                this.socket = null;
            }
        } catch (Exception e) {
            onError(e);
            this.closeConnection(ABNORMAL_CLOSE, e.getMessage());
            return;
        }
        connectLatch = new CountDownLatch(1);
        closeLatch = new CountDownLatch(1);
    }

    /**
     * Create and send the handshake to the other endpoint
     *
     * @throws InstrumentException a invalid handshake was created
     */
    private void sendHandshake() throws InstrumentException {
        String path;
        String part1 = uri.getRawPath();
        String part2 = uri.getRawQuery();
        if (part1 == null || part1.length() == 0)
            path = "/";
        else
            path = part1;
        if (part2 != null)
            path += '?' + part2;
        int port = getPort();
        String host = uri.getHost() + (
                (port != RFCWebSocket.DEFAULT_PORT && port != RFCWebSocket.DEFAULT_WSS_PORT)
                        ? ":" + port
                        : "");

        HandshakeBuilder handshake = new HandshakeBuilder();
        handshake.setResourceDescriptor(path);
        handshake.put("Host", host);
        if (headers != null) {
            for (Map.Entry<String, String> kv : headers.entrySet()) {
                handshake.put(kv.getKey(), kv.getValue());
            }
        }
        this.startHandshake(handshake);
    }

    private void decodeFrames(ByteBuffer socketBuffer) {
        List<Framedata> frames = draft.translateFrame(socketBuffer);
        for (Framedata f : frames) {
            Logger.trace("matched frame: {}", f);
            draft.processFrameText(this, f);
        }
    }

    /**
     * Write a list of bytebuffer (frames in binary form) into the outgoing queue
     *
     * @param byteBuffer the list of bytebuffer
     */
    private void write(ByteBuffer byteBuffer) {
        synchronized (object) {
            outQueue.add(byteBuffer);
        }
    }

    private void open(HandshakeBuilder d) {
        Logger.trace("open using draft: {}", draft);
        readyState = OPEN;
        try {
            this.onWebsocketOpen(d);
        } catch (RuntimeException e) {
            this.onWebsocketError(e);
        }
    }

    /**
     * This methods allows the reset of the connection lost timer in case of a changed parameter
     */
    private void restartConnectionLostTimer() {
        cancelConnectionLostTimer();
        connectionLostCheckerService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("connectionLostChecker"));
        Runnable connectionLostChecker = new Runnable() {
            private final ArrayList<WebSocket> connections = new ArrayList<>();

            @Override
            public void run() {
                connections.clear();
                try {
                    connections.addAll(getConnections());
                    long minimumPongTime = (long) (System.nanoTime() - (connectionLostTimeout * 1.5));
                    for (WebSocket conn : connections) {
                        executeConnectionLostDetection(conn, minimumPongTime);
                    }
                } catch (Exception e) {
                    //Ignore this exception
                }
                connections.clear();
            }
        };
        connectionLostCheckerFuture = connectionLostCheckerService.scheduleAtFixedRate(connectionLostChecker, connectionLostTimeout, connectionLostTimeout, TimeUnit.NANOSECONDS);
    }

    /**
     * Cancel any running timer for the connection lost detection
     */
    private void cancelConnectionLostTimer() {
        if (connectionLostCheckerService != null) {
            connectionLostCheckerService.shutdownNow();
            connectionLostCheckerService = null;
        }
        if (connectionLostCheckerFuture != null) {
            connectionLostCheckerFuture.cancel(false);
            connectionLostCheckerFuture = null;
        }
    }

    /**
     * Send a ping to the endpoint or close the connection since the other endpoint did not respond with a ping
     *
     * @param webSocket       the websocket instance
     * @param minimumPongTime the lowest/oldest allowable last pong time (in nanoTime) before we consider the connection to be lost
     */
    private void executeConnectionLostDetection(WebSocket webSocket, long minimumPongTime) {
        if (!(webSocket instanceof RFCWebSocket)) {
            return;
        }
        RFCWebSocket RFCWebSocket = (RFCWebSocket) webSocket;
        if (RFCWebSocket.getLastPong() < minimumPongTime) {
            Logger.trace("Closing connection due to no pong received: {}", RFCWebSocket);
            RFCWebSocket.closeConnection(ABNORMAL_CLOSE, "The connection was closed because the other endpoint did not respond with a pong in time. For more information check: https://github.com/TooTallNate/Java-WebSocket/wiki/Lost-connection-detection");
        } else {
            if (RFCWebSocket.isOpen()) {
                RFCWebSocket.sendPing();
            } else {
                Logger.trace("Trying to ping a non open connection: {}", RFCWebSocket);
            }
        }
    }

    /**
     * Users may implement this interface to override the default DNS lookup offered
     * by the OS.
     */
    interface DnsResolver {

        /**
         * Resolves the IP address for the given URI.
         * This method should never return null. If it's not able to resolve the IP
         * address then it should throw an UnknownHostException
         *
         * @param uri The URI to be resolved
         * @return The resolved IP address
         * @throws UnknownHostException if no IP address for the <code>uri</code> could be found.
         */
        InetAddress resolve(URI uri) throws UnknownHostException;

    }

    class WriteThread implements Runnable {

        private final RFCWebSocket rfc;

        WriteThread(RFCWebSocket webSocketClient) {
            this.rfc = webSocketClient;
        }

        @Override
        public void run() {
            Thread.currentThread().setName("WebSocketWriteThread-" + Thread.currentThread().getId());
            try {
                runWriteData();
            } catch (IOException e) {
                rfc.handleIOException(e);
            } finally {
                closeSocket();
                rfc.writeThread = null;
            }
        }

        /**
         * Write the data into the outstream
         *
         * @throws IOException if write or flush did not work
         */
        private void runWriteData() throws IOException {
            try {
                while (!Thread.interrupted()) {
                    ByteBuffer buffer = rfc.outQueue.take();
                    rfc.ostream.write(buffer.array(), 0, buffer.limit());
                    rfc.ostream.flush();
                }
            } catch (InterruptedException e) {
                for (ByteBuffer buffer : rfc.outQueue) {
                    rfc.ostream.write(buffer.array(), 0, buffer.limit());
                    rfc.ostream.flush();
                }
                Thread.currentThread().interrupt();
            }
        }

        /**
         * Closing the socket
         */
        private void closeSocket() {
            try {
                if (rfc.socket != null) {
                    rfc.socket.close();
                }
            } catch (IOException ex) {
                rfc.onWebsocketError(ex);
            }
        }
    }

}
