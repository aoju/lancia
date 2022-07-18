/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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

import org.aoju.bus.core.exception.InstrumentException;
import org.aoju.bus.core.io.ByteString;
import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.Header;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.BufferKit;
import org.aoju.bus.core.toolkit.RandomKit;
import org.aoju.bus.logger.Logger;

import javax.net.SocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

/**
 * 与chromuim通过Socket通信实现
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class SocketTransport implements Transport {

    private final Socket socket;
    private Consumer<String> consumer;

    private Connection connection = null;

    public SocketTransport(String browserWSEndpoint) {
        this.socket = new Socket(URI.create(browserWSEndpoint)) {
            @Override
            public void onWebsocketMessage(String message) {
                consumer.accept(message);
            }

            @Override
            public void onWebsocketError(Exception ex) {
                Logger.error(ex, ex.getMessage());
            }
        };
        try {
            socket.connectBlocking();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(String message) {
        if (this.connection == null) {
            Logger.warn("Transport connection is null, maybe closed?");
            return;
        }
        Logger.debug(message);
        this.socket.send(message);
    }

    @Override
    public void close() {
        if (this.socket.writeThread != null) {
            this.socket.close(1000);
        }
        if (this.connection != null) {
            this.connection.dispose();
        }
    }

    public void addConsumer(Consumer<String> consumer) {
        this.consumer = consumer;
    }

    public void addConnection(Connection connection) {
        this.connection = connection;
    }


    /**
     * RFC 6455 websocket协议的实现
     */
    static class Protocol {

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
        public Protocol() {
            this(Integer.MAX_VALUE);
        }

        /**
         * websocket协议的构造函数由RFC 6455指定，带有自定义扩展和协议
         *
         * @param inputMaxBufferSize 帧允许的最大大小(实际有效负载大小，解码帧可以更大)
         */
        public Protocol(int inputMaxBufferSize) {
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

        public Protocol newInstance() {
            return new Protocol(maxBufferSize);
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
            int maxPacketsize = buffer.remaining();
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

            if (maxPacketsize < realPacketSize) {
                Logger.trace("Incomplete frame: maxPacketsize < realPacketSize");
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

    /**
     * 表示单个连接的一端(客户端)。处理“握手”阶段，然后允许简单地发送文本帧，并通过基于事件的模型接收帧
     */
    public abstract static class Socket implements Runnable {

        public static final String NOT_YET_CONNECTED = "NOT_YET_CONNECTED";

        public static final String OPEN = "OPEN";

        public static final String CLOSING = "CLOSING";

        public static final String CLOSED = "CLOSED";
        /**
         * 指示正常关闭，意味着无论建立连接的目的是什么，它都已实现
         */
        public static final int NORMAL = 1000;
        /**
         * 1002表示端点由于协议错误正在终止连接
         */
        public static final int PROTOCOL_ERROR = 1002;
        /**
         * 1006是一个保留值，终端不能将其设置为关闭控制帧中的状态码
         */
        public static final int ABNORMAL_CLOSE = 1006;
        /**
         * 这种联系从未被证实过
         */
        public static final int NEVER_CONNECTED = -1;
        /**
         * 连接已刷新并关闭
         */
        public static final int FLASHPOLICY = -3;

        /**
         * Attribute来同步写操作
         */
        private final Object object = new Object();
        /**
         * 这个套接字的SocketFactory
         */
        private final SocketFactory socketFactory = null;
        /**
         * 使用的代理(如果有的话)
         */
        private final Proxy proxy = Proxy.NO_PROXY;
        /**
         * 这个websocket使用的草稿
         */
        private final Protocol protocol;
        /**
         * 以毫秒为单位的套接字超时值
         */
        private final int connectTimeout;
        /**
         * 需要发送到客户端的缓冲区队列
         */
        public BlockingQueue<ByteBuffer> outQueue;
        /**
         * 需要处理的缓冲区队列
         */
        public BlockingQueue<ByteBuffer> inQueue;
        /**
         * 套接字客户端
         */
        public java.net.Socket socket;
        /**
         * 自定义输出流
         */
        public OutputStream ostream;
        /**
         * 写传出消息的线程
         */
        public java.lang.Thread writeThread;
        /**
         * 此通道要连接到的URI
         */
        protected URI uri;
        /**
         * 连接的当前状态
         */
        private volatile String readyState = NOT_YET_CONNECTED;
        /**
         * 接收到的临时的握手字节
         */
        private ByteBuffer tmpHandshakeBytes = ByteBuffer.allocate(0);
        /**
         * 存储此websocket发送的握手
         */
        private String closeMessage = null;
        private Integer closeCode = null;
        private Boolean closeDremotely = null;
        private String descriptor = "*";
        private String host;
        /**
         * 如果为true，则不再有帧被提交发送
         */
        private boolean flushandCloseState = false;
        /**
         * 用于连接和读取消息的线程
         */
        private java.lang.Thread connectReadThread;
        /**
         * 要使用的附加头
         */
        private Map<String, String> headers;
        /**
         * 用于connectBlocking()的锁
         */
        private CountDownLatch connectLatch = new CountDownLatch(1);
        /**
         * The latch for closeBlocking()
         */
        private CountDownLatch closeLatch = new CountDownLatch(1);
        /**
         * 允许您停用Nagle算法的属性
         */
        private boolean tcpNoDelay;
        /**
         * 允许您启用/禁用SO_REUSEADDR套接字选项的属性
         */
        private boolean reuseAddr;

        /**
         * 构造一个WebSocket实例并将其设置为连接到指定的URI
         * 频道未尝试自动连接一旦调用<code>connect</code>将建立连接
         *
         * @param serverUri 要连接的服务器URI
         */
        public Socket(URI serverUri) {
            this(serverUri, new Protocol());
        }

        /**
         * 构造一个WebSocket实例并将其设置为连接到指定的URI
         * 频道未尝试自动连接一旦调用<code>connect</code>将建立连接
         *
         * @param serverUri 要连接的服务器URI
         * @param protocol  用于连接的协议消息
         */
        public Socket(URI serverUri, Protocol protocol) {
            this(serverUri, protocol, null, 0);
        }

        /**
         * 构造一个WebSocket实例并将其设置为连接到指定的URI
         * 频道未尝试自动连接一旦调用<code>connect</code>将建立连接
         *
         * @param serverUri      要连接的服务器URI
         * @param protocol       用于连接的协议消息
         * @param httpHeaders    额外的 Http Header
         * @param connectTimeout 超时时间
         */
        public Socket(URI serverUri, Protocol protocol, Map<String, String> httpHeaders, int connectTimeout) {
            if (serverUri == null) {
                throw new IllegalArgumentException();
            }
            this.protocol = protocol.newInstance();
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

        public boolean send(String text) {
            if (text == null)
                throw new IllegalArgumentException("Cannot send 'null' data to a WebSocketImpl.");
            ByteBuffer byteBuffer = ByteBuffer.wrap(text.getBytes(Charset.UTF_8));
            if (byteBuffer == null) {
                throw new IllegalArgumentException();
            }
            write(protocol.createBinary(byteBuffer));
            return true;
        }

        public boolean close(int code, String message) {
            close(code, message, false);
            return true;
        }

        /**
         * 解码提供的ByteBuffer的方法
         *
         * @param byteBuffer 要解码的ByteBuffer
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
         * 这将立即关闭连接，而没有适当的关闭握手
         *
         * @param code    结束代码
         * @param message 结束消息
         * @param remote  <code>true</code>表示此端点从另一个端点接收了<code>code</code>
         *                <code>false</code>表示此端点决定发送给定的代码，如果此端点开始了关闭握手，
         *                则<code>remote</code>也可能为true，因为另一个端点可能不会简单地回显<code>code</code>
         *                但在此端点执行相同操作的同时关闭连接，但使用另一个<code>code</code>进行连接
         */
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
            if (this.protocol != null)
                this.protocol.byteBuffer = null;
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

            if (this.protocol != null)
                this.protocol.byteBuffer = null;
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
            map.put(Header.HOST, this.host);
            map.put(Header.UPGRADE, "websocket");
            map.put(Header.CONNECTION, Header.UPGRADE);
            map.put(Header.SEC_WEBSOCKET_KEY, RandomKit.randomString(16));
            map.put(Header.SEC_WEBSOCKET_VERSION, "13");

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
            connectReadThread = new java.lang.Thread(this);
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
                    socket = new java.net.Socket(proxy);
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

            writeThread = new java.lang.Thread(new Thread(this));
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
         *
         * @param message 消息
         */
        protected abstract void onWebsocketMessage(String message);

        /**
         * 调用子类的实现onWebsocketError
         *
         * @param ex 异常信息
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
            java.lang.Thread current = java.lang.Thread.currentThread();
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
                this.protocol.byteBuffer = null;
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
            List<ByteString> frames = protocol.translate(socketBuffer);
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
            Logger.trace("open using draft: {}", protocol);
            readyState = OPEN;
            try {
                this.onWebsocketOpen();
            } catch (RuntimeException e) {
                this.onWebsocketError(e);
            }
        }

    }

    static class Thread implements Runnable {

        private final Socket rfc;

        Thread(Socket socket) {
            this.rfc = socket;
        }

        @Override
        public void run() {
            java.lang.Thread.currentThread().setName("WriteThread-" + java.lang.Thread.currentThread().getId());
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
                while (!java.lang.Thread.interrupted()) {
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
                java.lang.Thread.currentThread().interrupt();
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
