package org.aoju.lancia.worker;

import org.aoju.bus.core.io.ByteString;
import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.BufferKit;
import org.aoju.bus.core.toolkit.RandomKit;
import org.aoju.bus.http.Request;
import org.aoju.bus.http.socket.WebSocket;
import org.aoju.bus.logger.Logger;

import javax.net.SocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Represents one end (client or server) of a single WebSocketImpl connection.
 * Takes care of the "handshake" phase, then allows for easy sending of
 * text frames, and receiving frames through an event-based model.
 */
public abstract class RFCWebSocket implements WebSocket, Runnable {

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
     * Handshake specific field for the key
     */
    private static final String SEC_WEB_SOCKET_KEY = "Sec-WebSocket-Key";
    /**
     * Handshake specific field for the upgrade
     */
    private static final String UPGRADE = "Upgrade";
    /**
     * Handshake specific field for the connection
     */
    private static final String CONNECTION = "Connection";
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
     * The draft which is used by this websocket
     */
    private final Message message;
    /**
     * The socket timeout value to be used in milliseconds.
     */
    private final int connectTimeout;
    /**
     * Queue of buffers that need to be sent to the client.
     */
    public BlockingQueue<ByteBuffer> outQueue;
    /**
     * Queue of buffers that need to be processed
     */
    public BlockingQueue<ByteBuffer> inQueue;
    /**
     * The socket for this WebSocketClient
     */
    public Socket socket;
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
     * The current state of the connection
     */
    private volatile String readyState = NOT_YET_CONNECTED;
    /**
     * the bytes of an incomplete received handshake
     */
    private ByteBuffer tmpHandshakeBytes = ByteBuffer.allocate(0);
    /**
     * stores the handshake sent by this websocket ( Role.CLIENT only )
     */
    private String closeMessage = null;
    private Integer closeCode = null;
    private Boolean closeDremotely = null;
    private String descriptor = "*";
    private String host;
    /**
     * When true no further frames may be submitted to be sent
     */
    private boolean flushandCloseState = false;
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
     * Constructs a WebSocketClient instance and sets it to the connect to the
     * specified URI. The channel does not attampt to connect automatically. The connection
     * will be established once you call <var>connect</var>.
     *
     * @param serverUri the server URI to connect to
     */
    public RFCWebSocket(URI serverUri) {
        this(serverUri, new Message());
    }

    /**
     * Constructs a WebSocketClient instance and sets it to the connect to the
     * specified URI. The channel does not attampt to connect automatically. The connection
     * will be established once you call <var>connect</var>.
     *
     * @param serverUri the server URI to connect to
     * @param message   The draft which should be used for this connection
     */
    public RFCWebSocket(URI serverUri, Message message) {
        this(serverUri, message, null, 0);
    }

    /**
     * Constructs a WebSocketClient instance and sets it to the connect to the
     * specified URI. The channel does not attampt to connect automatically. The connection
     * will be established once you call <var>connect</var>.
     *
     * @param serverUri      the server URI to connect to
     * @param message        The draft which should be used for this connection
     * @param httpHeaders    Additional HTTP-Headers
     * @param connectTimeout The Timeout for the connection
     */
    public RFCWebSocket(URI serverUri, Message message, Map<String, String> httpHeaders, int connectTimeout) {
        if (serverUri == null) {
            throw new IllegalArgumentException();
        } else if (message == null) {
            throw new IllegalArgumentException("null as draft is permitted for `WebSocketServer` only!");
        }
        this.message = message.newInstance();
        this.uri = serverUri;
        this.outQueue = new LinkedBlockingQueue<>();
        this.inQueue = new LinkedBlockingQueue<>();
        if (httpHeaders != null) {
            headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            headers.putAll(httpHeaders);
        }
        this.connectTimeout = connectTimeout;
        setTcpNoDelay(false);
        setReuseAddr(false);
    }

    public static void translate(ByteBuffer buf) throws InstrumentException {
        String line = BufferKit.readLine(buf, Charset.UTF_8);
        if (line == null)
            throw new InstrumentException("" + buf.capacity() + 128);

        String[] firstLineTokens = line.split(Symbol.SPACE, 3);
        if (firstLineTokens.length != 3) {
            throw new InstrumentException();
        }

        if (!"101".equals(firstLineTokens[1])) {
            throw new InstrumentException(String.format("Invalid status code received: %s Status line: %s", firstLineTokens[1], line));
        }
        if (!"HTTP/1.1".equalsIgnoreCase(firstLineTokens[0])) {
            throw new InstrumentException(String.format("Invalid status line received: %s Status line: %s", firstLineTokens[0], line));
        }

        line = BufferKit.readLine(buf, Charset.UTF_8);
        while (line != null && line.length() > 0) {
            String[] pair = line.split(":", 2);
            if (pair.length != 2)
                throw new InstrumentException("Not an http header");
            line = BufferKit.readLine(buf, Charset.UTF_8);
        }
        if (line == null)
            throw new InstrumentException();
    }

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
        write(message.createBinary(byteBuffer));
        return true;
    }

    @Override
    public boolean send(ByteString bytes) {
        return false;
    }

    @Override
    public void cancel() {

    }

    @Override
    public boolean close(int code, String message) {
        close(code, message, false);
        return true;
    }

    /**
     * Method to decode the provided ByteBuffer
     *
     * @param byteBuffer the ByteBuffer to decode
     */
    public void decode(ByteBuffer byteBuffer) {
        assert (byteBuffer.hasRemaining());
        Logger.trace("process({}): ({})", byteBuffer.remaining(), (byteBuffer.remaining() > 1000 ? "too big to display" : new String(byteBuffer.array(), byteBuffer.position(), byteBuffer.remaining())));

        if (readyState != NOT_YET_CONNECTED) {
            if (readyState == OPEN) {
                decodeFrames(byteBuffer);
            }
        } else {
            if (decodeHandshake(byteBuffer) && (!isClosing() && !isClosed())) {
                assert (tmpHandshakeBytes.hasRemaining() != byteBuffer.hasRemaining() || !byteBuffer.hasRemaining()); // the buffers will never have remaining bytes at the same time
                if (byteBuffer.hasRemaining()) {
                    decodeFrames(byteBuffer);
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
    private boolean decodeHandshake(ByteBuffer byteBuffer) {
        ByteBuffer socketBuffer;
        if (tmpHandshakeBytes.capacity() == 0) {
            socketBuffer = byteBuffer;
        } else {
            if (tmpHandshakeBytes.remaining() < byteBuffer.remaining()) {
                ByteBuffer buf = ByteBuffer.allocate(tmpHandshakeBytes.capacity() + byteBuffer.remaining());
                tmpHandshakeBytes.flip();
                buf.put(tmpHandshakeBytes);
                tmpHandshakeBytes = buf;
            }

            tmpHandshakeBytes.put(byteBuffer);
            tmpHandshakeBytes.flip();
            socketBuffer = tmpHandshakeBytes;
        }
        socketBuffer.mark();
        translate(socketBuffer);
        open();
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
        if (this.message != null)
            this.message.byteBuffer = null;
        readyState = CLOSED;
    }

    public synchronized void flushAndClose(int code, String message, boolean remote) {
        if (flushandCloseState) {
            return;
        }
        closeCode = code;
        closeMessage = message;
        closeDremotely = remote;

        flushandCloseState = true;

        if (this.message != null)
            this.message.byteBuffer = null;
    }

    public void eot() {
        if (readyState == NOT_YET_CONNECTED) {
            closeConnection(NEVER_CONNECTED, Normal.EMPTY, true);
        } else if (flushandCloseState) {
            closeConnection(closeCode, closeMessage, closeDremotely);
        } else {
            closeConnection(ABNORMAL_CLOSE, Normal.EMPTY, true);
        }
    }

    public void close(int code) {
        close(code, Normal.EMPTY, false);
    }

    public void close(InstrumentException e) {
        close(0, e.getMessage(), false);
    }

    public void startHandshake() throws InstrumentException {
        TreeMap<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        map.put("Host", this.host);
        map.put(UPGRADE, "websocket");
        map.put(CONNECTION, UPGRADE);
        map.put(SEC_WEB_SOCKET_KEY, RandomKit.randomString(16));
        map.put("Sec-WebSocket-Version", "13");

        StringBuilder bui = new StringBuilder(100);
        bui.append("GET ")
                .append(this.descriptor)
                .append(" HTTP/1.1")
                .append("\r\n");
        Iterator<String> it = Collections.unmodifiableSet(map.keySet()).iterator();
        while (it.hasNext()) {
            String fieldname = it.next();
            String fieldvalue = map.get(fieldname);
            bui.append(fieldname);
            bui.append(": ");
            bui.append(fieldvalue);
            bui.append("\r\n");
        }
        bui.append("\r\n");
        byte[] httpHeader = bui.toString().getBytes(Charset.US_ASCII);
        ByteBuffer bytebuffer = ByteBuffer.allocate((0) + httpHeader.length);
        bytebuffer.put(httpHeader);

        bytebuffer.flip();

        write(bytebuffer);
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

    /**
     * 重新启动websocket连接。这个方法不会阻塞
     */
    public void reconnect() {
        reset();
        connect();
    }

    /**
     * 初始化websocket连接。这个方法不会阻塞
     */
    public void connect() {
        if (connectReadThread != null)
            throw new IllegalStateException("WebSocket objects are not reuseable");
        connectReadThread = new Thread(this);
        connectReadThread.setName("ReadThread-" + connectReadThread.getId());
        connectReadThread.start();
    }

    /**
     * 类似于connect阻塞时直到websocket连接或失败
     *
     * @return 无论成功与否，都会返回
     * @throws InterruptedException 当线程被中断时抛出
     */
    public boolean connectBlocking() throws InterruptedException {
        connect();
        connectLatch.await();
        return this.isOpen();
    }

    public void run() {
        InputStream istream;
        try {
            if (socketFactory != null) {
                socket = socketFactory.createSocket();
            } else if (socket == null) {
                socket = new Socket(proxy);
            } else if (socket.isClosed()) {
                throw new IOException();
            }

            socket.setTcpNoDelay(isTcpNoDelay());
            socket.setReuseAddress(isReuseAddr());

            if (!socket.isConnected()) {
                InetSocketAddress addr = new InetSocketAddress(InetAddress.getByName(uri.getHost()), uri.getPort());
                socket.connect(addr, connectTimeout);
            }

            istream = socket.getInputStream();
            ostream = socket.getOutputStream();

            sendHandshake();
        } catch (Exception e) {
            onWebsocketError(e);
            this.closeConnection(NEVER_CONNECTED, e.getMessage(), false);
            return;
        } catch (InternalError e) {
            if (e.getCause() instanceof InvocationTargetException && e.getCause().getCause() instanceof IOException) {
                IOException cause = (IOException) e.getCause().getCause();
                onWebsocketError(cause);
                this.closeConnection(NEVER_CONNECTED, cause.getMessage(), false);
                return;
            }
            throw e;
        }

        writeThread = new Thread(new Threads(this));
        writeThread.start();

        byte[] rawbuffer = new byte[16384];
        int readBytes;

        try {
            while (!isClosing() && !isClosed() && (readBytes = istream.read(rawbuffer)) != -1) {
                this.decode(ByteBuffer.wrap(rawbuffer, 0, readBytes));
            }
            this.eot();
        } catch (IOException e) {
            this.eot();
        } catch (RuntimeException e) {
            this.closeConnection(ABNORMAL_CLOSE, e.getMessage(), false);
        }
        connectReadThread = null;
    }

    /**
     * 调用子类的实现onWebsocketMessage
     */
    protected abstract void onWebsocketMessage(String message);

    /**
     * 调用子类的实现onWebsocketError
     */
    protected abstract void onWebsocketError(Exception ex);

    public final void onWebsocketOpen() {
        connectLatch.countDown();
    }

    public final void onWebsocketClose(int code, String reason, boolean remote) {
        if (writeThread != null)
            writeThread.interrupt();
        close(code, reason, remote);
        connectLatch.countDown();
        closeLatch.countDown();
    }

    /**
     * 测试TCP_NODELAY是否启用
     *
     * @return 否为新连接启用TCP_NODELAY
     */
    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    /**
     * 是否启用新连接 TCP_NODELAY
     *
     * @param tcpNoDelay true表示启用TCP_NODELAY, false表示禁用
     */
    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    /**
     * 否启用SO_REUSEADDR
     *
     * @return 是否启用SO_REUSEADDR
     */
    public boolean isReuseAddr() {
        return reuseAddr;
    }

    /**
     * 为套接字Enable/disable SO_REUSEADDR
     *
     * @param reuseAddr 是否启用或禁用SO_REUSEADDR
     */
    public void setReuseAddr(boolean reuseAddr) {
        this.reuseAddr = reuseAddr;
    }

    /**
     * 重置一切相关的允许重新连接
     */
    private void reset() {
        Thread current = Thread.currentThread();
        if (current == writeThread || current == connectReadThread) {
            throw new IllegalStateException("You cannot initialize a reconnect out of the websocket thread. Use reconnect in another thread to insure a successful cleanup.");
        }
        try {
            close(NORMAL);
            closeLatch.await();
            if (writeThread != null) {
                this.writeThread.interrupt();
                this.writeThread = null;
            }
            if (connectReadThread != null) {
                this.connectReadThread.interrupt();
                this.connectReadThread = null;
            }
            this.message.byteBuffer = null;
            if (this.socket != null) {
                this.socket.close();
                this.socket = null;
            }
        } catch (Exception e) {
            this.closeConnection(ABNORMAL_CLOSE, e.getMessage(), false);
            return;
        }
        connectLatch = new CountDownLatch(1);
        closeLatch = new CountDownLatch(1);
    }

    /**
     * 创建握手并将握手发送到另一个端点
     */
    private void sendHandshake() {
        String part1 = uri.getRawPath();
        String part2 = uri.getRawQuery();
        if (part1 == null || part1.length() == 0) {
            this.descriptor = Symbol.SLASH;
        } else {
            this.descriptor = part1;
        }

        if (part2 != null) {
            this.descriptor += Symbol.C_QUESTION_MARK + part2;
        }

        int port = uri.getPort();
        this.host = uri.getHost() + (
                (port != 80 && port != 443)
                        ? Symbol.COLON + port
                        : Normal.EMPTY);

        this.startHandshake();
    }

    private void decodeFrames(ByteBuffer socketBuffer) {
        List<ByteString> frames = message.translate(socketBuffer);
        for (ByteString f : frames) {
            Logger.trace("Matched frame: {}", f);
            try {
                this.onWebsocketMessage(BufferKit.readLine(ByteBuffer.wrap(f.internalArray())));
            } catch (RuntimeException e) {
                this.onWebsocketError(e);
            }
        }
    }

    /**
     * 写一个缓冲列表(以二进制形式的帧)到输出队列
     *
     * @param byteBuffer 缓冲列表
     */
    private void write(ByteBuffer byteBuffer) {
        synchronized (object) {
            outQueue.add(byteBuffer);
        }
    }

    private void open() {
        Logger.trace("open using draft: {}", message);
        readyState = OPEN;
        try {
            this.onWebsocketOpen();
        } catch (RuntimeException e) {
            this.onWebsocketError(e);
        }
    }

    /**
     * RFC 6455 websocket协议的实现
     */
    public static class Message {

        /**
         * 属性获取帧允许的最大大小
         */
        private final int maxBufferSize;
        /**
         * 属性设置为当前不完整框架
         */
        public ByteBuffer byteBuffer;

        private int realPacketSize;

        /**
         * websocket协议的构造函数由RFC 6455指定，带有自定义扩展和协议
         */
        public Message() {
            this(Integer.MAX_VALUE);
        }

        /**
         * websocket协议的构造函数由RFC 6455指定，带有自定义扩展和协议
         *
         * @param inputMaxBufferSize 帧允许的最大大小(实际有效负载大小，解码帧可以更大)
         */
        public Message(int inputMaxBufferSize) {
            if (inputMaxBufferSize < 1) {
                throw new IllegalArgumentException();
            }
            this.maxBufferSize = inputMaxBufferSize;
        }

        /**
         * 获取掩码字节
         *
         * @param mask 掩码是否有效
         * @return -128表示true, 0表示false
         */
        private static byte getMaskByte(boolean mask) {
            return mask ? (byte) -128 : 0;
        }

        /**
         * 获取字节缓冲区的大小字节
         *
         * @param mes 当前的缓冲区
         * @return 字节大小
         */
        private static int getSizeByte(ByteBuffer mes) {
            if (mes.remaining() <= 125) {
                return 1;
            } else if (mes.remaining() <= 65535) {
                return 2;
            }
            return 8;
        }

        private static byte[] toByteArray(long val, int bytecount) {
            byte[] buffer = new byte[bytecount];
            int highest = 8 * bytecount - 8;
            for (int i = 0; i < bytecount; i++) {
                buffer[i] = (byte) (val >>> (highest - 8 * i));
            }
            return buffer;
        }

        public Message newInstance() {
            return new Message(maxBufferSize);
        }

        public ByteBuffer createBinary(ByteBuffer byteBuffer) {
            if (Logger.get().isTrace())
                Logger.trace("afterEnconding({}): {}", byteBuffer.remaining(), (byteBuffer.remaining() > 1000 ? "too big to display" : new String(byteBuffer.array())));
            boolean mask = true;
            int sizebytes = getSizeByte(byteBuffer);
            ByteBuffer buf = ByteBuffer.allocate(1 + (sizebytes > 1 ? sizebytes + 1 : sizebytes) + (mask ? 4 : 0) + byteBuffer.remaining());
            byte optcode = 1;
            byte one = -128;
            one |= optcode;
            buf.put(one);
            byte[] payloadlengthbytes = toByteArray(byteBuffer.remaining(), sizebytes);
            assert (payloadlengthbytes.length == sizebytes);

            if (sizebytes == 1) {
                buf.put((byte) (payloadlengthbytes[0] | getMaskByte(mask)));
            } else if (sizebytes == 2) {
                buf.put((byte) ((byte) 126 | getMaskByte(mask)));
                buf.put(payloadlengthbytes);
            } else if (sizebytes == 8) {
                buf.put((byte) ((byte) 127 | getMaskByte(mask)));
                buf.put(payloadlengthbytes);
            } else {
                throw new IllegalStateException("Size representation not supported/specified");
            }
            if (mask) {
                ByteBuffer maskkey = ByteBuffer.allocate(4);
                buf.put(maskkey.array());
                for (int i = 0; byteBuffer.hasRemaining(); i++) {
                    buf.put((byte) (byteBuffer.get() ^ maskkey.get(i % 4)));
                }
            } else {
                buf.put(byteBuffer);
                byteBuffer.flip();
            }
            assert (buf.remaining() == 0) : buf.remaining();
            buf.flip();
            return buf;
        }

        public List<ByteString> translate(ByteBuffer buffer) {
            while (true) {
                List<ByteString> frames = new LinkedList<>();
                ByteString cur;
                if (byteBuffer != null) {

                    buffer.mark();
                    // 接收的字节数
                    int availableNextByteCount = buffer.remaining();
                    // 完成不完整帧的字节数
                    int expectedNextByteCount = byteBuffer.remaining();

                    if (expectedNextByteCount > availableNextByteCount) {
                        // 没有接收到足够的字节来完成帧
                        byteBuffer.put(buffer.array(), buffer.position(), availableNextByteCount);
                        buffer.position(buffer.position() + availableNextByteCount);
                        return Collections.emptyList();
                    }
                    byteBuffer.put(buffer.array(), buffer.position(), expectedNextByteCount);
                    buffer.position(buffer.position() + expectedNextByteCount);
                    cur = translateSingle((ByteBuffer) byteBuffer.duplicate().position(0));
                    frames.add(cur);
                    byteBuffer = null;
                }

                while (buffer.hasRemaining()) {
                    // 尽可能多读整帧
                    buffer.mark();
                    try {
                        cur = translateSingle(buffer);
                        frames.add(cur);
                    } catch (InstrumentException e) {
                        buffer.reset();
                        byteBuffer = ByteBuffer.allocate(this.realPacketSize);
                        byteBuffer.put(buffer);
                        break;
                    }
                }
                return frames;
            }
        }

        /**
         * 根据扩展的有效载荷长度(126或127)转换缓冲区
         *
         * @param buffer         要读取的缓冲区
         * @param payloadLength  旧有效载荷长度
         * @param realLacketSize 实际数据包大小
         * @return 包含新的有效负载长度和新的包大小的新有效负载数据
         * @throws InstrumentException 如果控制帧的长度无效，则抛出
         */
        private void translatePayload(int[] array, ByteBuffer buffer, int payloadLength, int realLacketSize) throws InstrumentException {
            if (payloadLength == 126) {
                realLacketSize += 2; // 额外的长度字节
                byte[] sizebytes = new byte[3];
                sizebytes[1] = buffer.get( /*1 + 1*/);
                sizebytes[2] = buffer.get( /*1 + 2*/);
                payloadLength = new BigInteger(sizebytes).intValue();
            } else {
                realLacketSize += 8; // 额外的长度字节
                byte[] bytes = new byte[8];
                for (int i = 0; i < 8; i++) {
                    bytes[i] = buffer.get( /*1 + i*/);
                }
                long length = new BigInteger(bytes).longValue();
                payloadLength = (int) length;
            }

            array[0] = payloadLength;
            array[1] = realLacketSize;
        }

        private ByteString translateSingle(ByteBuffer buffer) {
            if (buffer == null)
                throw new IllegalArgumentException();
            int maxpacketsize = buffer.remaining();
            this.realPacketSize = 2;

            buffer.get( /*0*/);

            byte b2 = buffer.get( /*1*/);
            boolean mask = (b2 & -128) != 0;
            int payloadLength = (byte) (b2 & ~(byte) 128);

            if (!(payloadLength >= 0 && payloadLength <= 125)) {
                int[] array = {payloadLength, realPacketSize};
                translatePayload(array, buffer, payloadLength, realPacketSize);
                payloadLength = array[0];
                realPacketSize = array[1];
            }
            realPacketSize += (mask ? 4 : 0);
            realPacketSize += payloadLength;

            if (maxpacketsize < realPacketSize) {
                Logger.trace("Incomplete frame: maxpacketsize < realpacketsize");
                throw new InstrumentException("" + realPacketSize);
            }
            ByteBuffer payload = ByteBuffer.allocate(payloadLength);
            if (mask) {
                byte[] maskskey = new byte[4];
                buffer.get(maskskey);
                for (int i = 0; i < payloadLength; i++) {
                    payload.put((byte) (buffer.get( /*payloadstart + i*/) ^ maskskey[i % 4]));
                }
            } else {
                payload.put(buffer.array(), buffer.position(), payload.limit());
                buffer.position(buffer.position() + payload.limit());
            }
            payload.flip();
            return ByteString.of(payload);
        }

    }

    class Threads implements Runnable {

        private final RFCWebSocket rfc;

        Threads(RFCWebSocket socket) {
            this.rfc = socket;
        }

        @Override
        public void run() {
            Thread.currentThread().setName("WriteThread-" + Thread.currentThread().getId());
            try {
                runWrite();
            } finally {
                close();
                rfc.writeThread = null;
            }
        }

        /**
         * 把数据写入输出流
         */
        private void runWrite() {
            try {
                while (!Thread.interrupted()) {
                    ByteBuffer buffer = rfc.outQueue.take();
                    rfc.ostream.write(buffer.array(), 0, buffer.limit());
                    rfc.ostream.flush();
                }
            } catch (InterruptedException | IOException e) {
                for (ByteBuffer buffer : rfc.outQueue) {
                    try {
                        rfc.ostream.write(buffer.array(), 0, buffer.limit());
                        rfc.ostream.flush();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
                Thread.currentThread().interrupt();
            }
        }

        private void close() {
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
