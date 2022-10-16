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

import org.aoju.lancia.Builder;
import org.aoju.lancia.worker.exception.SocketException;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.nio.ByteBuffer;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;

/**
 * A subclass must implement at least <var>onOpen</var>, <var>onClose</var>, and
 * <var>onMessage</var> to be useful. At runtime the user is expected to establish a connection via
 * {@link #connect()}, then receive events like {@link #onMessage(String)} via the overloaded
 * methods and to {@link #send(String)} data to the server.
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public abstract class SocketFactory extends ListenerBuilder implements Runnable, Sockets {

    /**
     * The underlying engine
     */
    private final SocketBuilder engine;
    /**
     * The SocketFactory for this SocketFactory
     */
    private final javax.net.SocketFactory socketFactory = null;
    /**
     * The used proxy, if any
     */
    private final Proxy proxy = Proxy.NO_PROXY;
    /**
     * The latch for connectBlocking()
     */
    private final CountDownLatch connectLatch = new CountDownLatch(1);
    /**
     * The latch for closeBlocking()
     */
    private final CountDownLatch closeLatch = new CountDownLatch(1);
    /**
     * The URI this channel is supposed to connect to.
     */
    protected URI uri;
    /**
     * The org.aoju.lancia.socket for this SocketFactory
     */
    private Socket socket = null;
    /**
     * The used OutputStream
     */
    private OutputStream ostream;
    /**
     * The thread to write outgoing message
     */
    private Thread writeThread;
    /**
     * The thread to connect and read message
     */
    private Thread connectReadThread;
    /**
     * The additional headers to use
     */
    private Map<String, String> headers;
    /**
     * The org.aoju.lancia.socket timeout value to be used in milliseconds.
     */
    private int connectTimeout = 0;

    /**
     * Constructs a SocketFactory instance and sets it to the connect to the specified URI. The
     * channel does not attampt to connect automatically. The connection will be established once you
     * call <var>connect</var>.
     *
     * @param serverUri the server URI to connect to
     */
    public SocketFactory(URI serverUri) {
        this(serverUri, new Draft_6455());
    }

    /**
     * Constructs a SocketFactory instance and sets it to the connect to the specified URI. The
     * channel does not attampt to connect automatically. The connection will be established once you
     * call <var>connect</var>.
     *
     * @param serverUri     the server URI to connect to
     * @param protocolDraft The draft which should be used for this connection
     */
    public SocketFactory(URI serverUri, Draft_6455 protocolDraft) {
        this(serverUri, protocolDraft, null, 0);
    }

    /**
     * Constructs a SocketFactory instance and sets it to the connect to the specified URI. The
     * channel does not attampt to connect automatically. The connection will be established once you
     * call <var>connect</var>.
     *
     * @param serverUri   the server URI to connect to
     * @param httpHeaders Additional HTTP-Headers
     */
    public SocketFactory(URI serverUri, Map<String, String> httpHeaders) {
        this(serverUri, new Draft_6455(), httpHeaders);
    }

    /**
     * Constructs a SocketFactory instance and sets it to the connect to the specified URI. The
     * channel does not attampt to connect automatically. The connection will be established once you
     * call <var>connect</var>.
     *
     * @param serverUri     the server URI to connect to
     * @param protocolDraft The draft which should be used for this connection
     * @param httpHeaders   Additional HTTP-Headers
     */
    public SocketFactory(URI serverUri, Draft_6455 protocolDraft, Map<String, String> httpHeaders) {
        this(serverUri, protocolDraft, httpHeaders, 0);
    }

    /**
     * Constructs a SocketFactory instance and sets it to the connect to the specified URI. The
     * channel does not attampt to connect automatically. The connection will be established once you
     * call <var>connect</var>.
     *
     * @param serverUri      the server URI to connect to
     * @param draft          The draft which should be used for this connection
     * @param httpHeaders    Additional HTTP-Headers
     * @param connectTimeout The Timeout for the connection
     */
    public SocketFactory(URI serverUri, Draft_6455 draft, Map<String, String> httpHeaders,
                         int connectTimeout) {
        if (serverUri == null) {
            throw new IllegalArgumentException();
        } else if (draft == null) {
            throw new IllegalArgumentException("null as draft is permitted for `WebSocketServer` only!");
        }
        this.uri = serverUri;
        if (httpHeaders != null) {
            headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            headers.putAll(httpHeaders);
        }
        this.connectTimeout = connectTimeout;
        setTcpNoDelay(false);
        setReuseAddr(false);
        this.engine = new SocketBuilder(this, draft);
    }

    /**
     * Initiates the websocket connection. This method does not block.
     */
    public void connect() {
        if (connectReadThread != null) {
            throw new IllegalStateException("SocketFactory objects are not reuseable");
        }
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
        return engine.isOpen();
    }

    /**
     * Initiates the websocket close handshake. This method does not block<br> In oder to make sure
     * the connection is closed use <code>closeBlocking</code>
     */
    public void close() {
        if (writeThread != null) {
            engine.close(Framedata.NORMAL);
        }
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

    /**
     * Sends <var>text</var> to the connected websocket server.
     *
     * @param text The string which will be transmitted.
     */
    public void send(String text) {
        engine.send(text);
    }

    @Override
    public <T> T getAttachment() {
        return engine.getAttachment();
    }

    @Override
    public <T> void setAttachment(T attachment) {
        engine.setAttachment(attachment);
    }

    @Override
    protected Collection<Sockets> getConnections() {
        return Collections.singletonList(engine);
    }

    @Override
    public void sendPing() {
        engine.sendPing();
    }

    public void run() {
        InputStream istream;
        try {
            boolean upgradeSocketToSSLSocket = prepareSocket();

            socket.setTcpNoDelay(isTcpNoDelay());
            socket.setReuseAddress(isReuseAddr());

            if (!socket.isConnected()) {
                InetSocketAddress addr = new InetSocketAddress(InetAddress.getByName(uri.getHost()), this.getPort());
                socket.connect(addr, connectTimeout);
            }

            // if the org.aoju.lancia.socket is set by others we don't apply any TLS wrapper
            if (upgradeSocketToSSLSocket && "wss".equals(uri.getScheme())) {
                upgradeSocketToSSL();
            }

            if (socket instanceof SSLSocket sslSocket) {
                SSLParameters sslParameters = sslSocket.getSSLParameters();
                onSetSSLParameters(sslParameters);
                sslSocket.setSSLParameters(sslParameters);
            }

            istream = socket.getInputStream();
            ostream = socket.getOutputStream();

            sendHandshake();
        } catch (/*IOException | SecurityException | UnresolvedAddressException | ClosedByInterruptException | SocketTimeoutException */
                Exception e) {
            onWebsocketError(engine, e);
            engine.closeConnection(Framedata.NEVER_CONNECTED, e.getMessage());
            return;
        } catch (InternalError e) {
            // https://bugs.openjdk.java.net/browse/JDK-8173620
            if (e.getCause() instanceof InvocationTargetException && e.getCause()
                    .getCause() instanceof IOException cause) {
                onWebsocketError(engine, cause);
                engine.closeConnection(Framedata.NEVER_CONNECTED, cause.getMessage());
                return;
            }
            throw e;
        }

        writeThread = new Thread(new WebsocketWriteThread(this));
        writeThread.start();

        byte[] rawbuffer = new byte[SocketBuilder.RCVBUF];
        int readBytes;

        try {
            while (!isClosing() && !isClosed() && (readBytes = istream.read(rawbuffer)) != -1) {
                engine.decode(ByteBuffer.wrap(rawbuffer, 0, readBytes));
            }
            engine.eot();
        } catch (IOException e) {
            handleIOException(e);
        } catch (RuntimeException e) {
            // this catch case covers internal errors only and indicates a bug in this websocket implementation
            onError(e);
            engine.closeConnection(Framedata.ABNORMAL_CLOSE, e.getMessage());
        }
        connectReadThread = null;
    }

    private void upgradeSocketToSSL()
            throws NoSuchAlgorithmException, KeyManagementException, IOException {
        SSLSocketFactory factory;
        // Prioritise the provided socketfactory
        // Helps when using web debuggers like Fiddler Classic
        if (socketFactory instanceof SSLSocketFactory) {
            factory = (SSLSocketFactory) socketFactory;
        } else {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, null);
            factory = sslContext.getSocketFactory();
        }
        socket = factory.createSocket(socket, uri.getHost(), getPort(), true);
    }

    private boolean prepareSocket() throws IOException {
        boolean upgradeSocketToSSLSocket = false;
        // Prioritise a proxy over a org.aoju.lancia.socket factory and apply the socketfactory later
        if (proxy != Proxy.NO_PROXY) {
            socket = new Socket(proxy);
            upgradeSocketToSSLSocket = true;
        } else if (socketFactory != null) {
            socket = socketFactory.createSocket();
        } else if (socket == null) {
            socket = new Socket(proxy);
            upgradeSocketToSSLSocket = true;
        } else if (socket.isClosed()) {
            throw new IOException();
        }
        return upgradeSocketToSSLSocket;
    }

    /**
     * Apply specific SSLParameters If you override this method make sure to always call
     * super.onSetSSLParameters() to ensure the hostname validation is active
     *
     * @param sslParameters the SSLParameters which will be used for the SSLSocket
     */
    protected void onSetSSLParameters(SSLParameters sslParameters) {
        // If you run into problem on Android (NoSuchMethodException), check out the wiki https://github.com/TooTallNate/Java-WebSocket/wiki/No-such-method-error-setEndpointIdentificationAlgorithm
        // Perform hostname validation
        sslParameters.setEndpointIdentificationAlgorithm("HTTPS");
    }

    /**
     * Extract the specified port
     *
     * @return the specified port or the default port for the specific scheme
     */
    private int getPort() {
        int port = uri.getPort();
        String scheme = uri.getScheme();
        if ("wss".equals(scheme)) {
            return port == -1 ? SocketBuilder.DEFAULT_WSS_PORT : port;
        } else if ("ws".equals(scheme)) {
            return port == -1 ? SocketBuilder.DEFAULT_PORT : port;
        } else {
            throw new IllegalArgumentException("unknown scheme: " + scheme);
        }
    }

    /**
     * Create and send the handshake to the other endpoint
     */
    private void sendHandshake() throws SocketException {
        String path;
        String part1 = uri.getRawPath();
        String part2 = uri.getRawQuery();
        if (part1 == null || part1.length() == 0) {
            path = "/";
        } else {
            path = part1;
        }
        if (part2 != null) {
            path += '?' + part2;
        }
        int port = getPort();
        String host = uri.getHost() + (
                (port != SocketBuilder.DEFAULT_PORT && port != SocketBuilder.DEFAULT_WSS_PORT)
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
        engine.startHandshake(handshake);
    }

    /**
     * This represents the state of the connection.
     */
    public Builder.ReadyState getReadyState() {
        return engine.getReadyState();
    }

    /**
     * Calls subclass' implementation of <var>onMessage</var>.
     */
    @Override
    public final void onWebsocketMessage(Sockets conn, String message) {
        onMessage(message);
    }

    /**
     * Calls subclass' implementation of <var>onOpen</var>.
     */
    @Override
    public final void onWebsocketOpen(Sockets conn, HandshakeBuilder handshake) {
        startConnectionLostTimer();
        onOpen(handshake);
        connectLatch.countDown();
    }

    /**
     * Calls subclass' implementation of <var>onClose</var>.
     */
    @Override
    public final void onWebsocketClose(Sockets conn, int code, String reason, boolean remote) {
        stopConnectionLostTimer();
        if (writeThread != null) {
            writeThread.interrupt();
        }
        onClose(code, reason, remote);
        connectLatch.countDown();
        closeLatch.countDown();
    }

    /**
     * Calls subclass' implementation of <var>onIOError</var>.
     */
    @Override
    public final void onWebsocketError(Sockets conn, Exception ex) {
        onError(ex);
    }

    @Override
    public final void onWriteDemand(Sockets conn) {
        // nothing to do
    }

    @Override
    public void onWebsocketCloseInitiated(Sockets conn, int code, String reason) {
        onCloseInitiated(code, reason);
    }

    @Override
    public void onWebsocketClosing(Sockets conn, int code, String reason, boolean remote) {
        onClosing(code, reason, remote);
    }

    /**
     * Send when this peer sends a close handshake
     *
     * @param code   The codes can be looked up here: {@link Framedata}
     * @param reason Additional information string
     */
    public void onCloseInitiated(int code, String reason) {
        //To overwrite
    }

    /**
     * Called as soon as no further frames are accepted
     *
     * @param code   The codes can be looked up here: {@link Framedata}
     * @param reason Additional information string
     * @param remote Returns whether or not the closing of the connection was initiated by the remote
     *               host.
     */
    public void onClosing(int code, String reason, boolean remote) {

    }

    /**
     * Getter for the engine
     *
     * @return the engine
     */
    public Sockets getConnection() {
        return engine;
    }

    @Override
    public InetSocketAddress getLocalSocketAddress(Sockets conn) {
        if (socket != null) {
            return (InetSocketAddress) socket.getLocalSocketAddress();
        }
        return null;
    }

    @Override
    public InetSocketAddress getRemoteSocketAddress(Sockets conn) {
        if (socket != null) {
            return (InetSocketAddress) socket.getRemoteSocketAddress();
        }
        return null;
    }

    /**
     * Called after an opening handshake has been performed and the given websocket is ready to be
     * written on.
     *
     * @param handshake The handshake of the websocket instance
     */
    public abstract void onOpen(HandshakeBuilder handshake);

    /**
     * Callback for string messages received from the remote host
     *
     * @param message The UTF-8 decoded message that was received.
     * @see #onMessage(ByteBuffer)
     **/
    public abstract void onMessage(String message);

    /**
     * Called after the websocket connection has been closed.
     *
     * @param code   The codes can be looked up here: {@link Framedata}
     * @param reason Additional information string
     * @param remote Returns whether or not the closing of the connection was initiated by the remote
     *               host.
     **/
    public abstract void onClose(int code, String reason, boolean remote);

    /**
     * Called when errors occurs. If an error causes the websocket connection to fail {@link
     * #onClose(int, String, boolean)} will be called additionally.<br> This method will be called
     * primarily because of IO or protocol errors.<br> If the given exception is an RuntimeException
     * that probably means that you encountered a bug.<br>
     *
     * @param ex The exception causing this error
     **/
    public abstract void onError(Exception ex);

    /**
     * Callback for binary messages received from the remote host
     *
     * @param bytes The binary message that was received.
     * @see #onMessage(String)
     **/
    public void onMessage(ByteBuffer bytes) {
        //To overwrite
    }

    @Override
    public void sendFragmentedFrame(Builder.Opcode op, ByteBuffer buffer, boolean fin) {
        engine.sendFragmentedFrame(op, buffer, fin);
    }

    @Override
    public boolean isOpen() {
        return engine.isOpen();
    }

    @Override
    public boolean isFlushAndClose() {
        return engine.isFlushAndClose();
    }

    @Override
    public boolean isClosed() {
        return engine.isClosed();
    }

    @Override
    public boolean isClosing() {
        return engine.isClosing();
    }

    @Override
    public boolean hasBufferedData() {
        return engine.hasBufferedData();
    }

    @Override
    public void close(int code) {
        engine.close(code);
    }

    @Override
    public void close(int code, String message) {
        engine.close(code, message);
    }

    @Override
    public void closeConnection(int code, String message) {
        engine.closeConnection(code, message);
    }

    @Override
    public void sendFrame(Framedata framedata) {
        engine.sendFrame(framedata);
    }

    @Override
    public void sendFrame(Collection<Framedata> frames) {
        engine.sendFrame(frames);
    }

    @Override
    public InetSocketAddress getLocalSocketAddress() {
        return engine.getLocalSocketAddress();
    }

    @Override
    public InetSocketAddress getRemoteSocketAddress() {
        return engine.getRemoteSocketAddress();
    }

    @Override
    public SocketProtocol getProtocol() {
        return engine.getProtocol();
    }

    /**
     * Method to give some additional info for specific IOExceptions
     *
     * @param e the IOException causing a eot.
     */
    private void handleIOException(IOException e) {
        if (e instanceof SSLException) {
            onError(e);
        }
        engine.eot();
    }

    private class WebsocketWriteThread implements Runnable {

        private final SocketFactory webSocketClient;

        WebsocketWriteThread(SocketFactory webSocketClient) {
            this.webSocketClient = webSocketClient;
        }

        @Override
        public void run() {
            Thread.currentThread().setName("WebSocketWriteThread-" + Thread.currentThread().getId());
            try {
                runWriteData();
            } catch (IOException e) {
                handleIOException(e);
            } finally {
                closeSocket();
                writeThread = null;
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
                    ByteBuffer buffer = engine.outQueue.take();
                    ostream.write(buffer.array(), 0, buffer.limit());
                    ostream.flush();
                }
            } catch (InterruptedException e) {
                for (ByteBuffer buffer : engine.outQueue) {
                    ostream.write(buffer.array(), 0, buffer.limit());
                    ostream.flush();
                }
                Thread.currentThread().interrupt();
            }
        }

        /**
         * Closing the org.aoju.lancia.socket
         */
        private void closeSocket() {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException ex) {
                onWebsocketError(webSocketClient, ex);
            }
        }
    }

}
