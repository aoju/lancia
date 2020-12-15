package org.aoju.lancia.socket;

import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.BufferKit;
import org.aoju.bus.core.toolkit.RandomKit;
import org.aoju.bus.logger.Logger;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Implementation for the RFC 6455 websocket protocol
 * This is the recommended class for your websocket connection
 */
public class Draft {

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
     * Attribute for the maximum allowed size of a frame
     */
    private final int maxBufferSize;
    /**
     * Attribute for the current incomplete frame
     */
    public ByteBuffer byteBuffer;

    private int realpacketsize;

    /**
     * Constructor for the websocket protocol specified by RFC 6455 with custom extensions and protocols
     */
    public Draft() {
        this(Integer.MAX_VALUE);
    }

    /**
     * Constructor for the websocket protocol specified by RFC 6455 with custom extensions and protocols
     *
     * @param inputMaxBufferSize the maximum allowed size of a frame (the real payload size, decoded frames can be bigger)
     */
    public Draft(int inputMaxBufferSize) {
        if (inputMaxBufferSize < 1) {
            throw new IllegalArgumentException();
        }
        this.maxBufferSize = inputMaxBufferSize;
    }

    public static HandshakeBuilder translateHandshakeHttp(ByteBuffer buf) throws InstrumentException {
        String line = BufferKit.readLine(buf, Charset.UTF_8);
        if (line == null)
            throw new InstrumentException("" + buf.capacity() + 128);

        String[] firstLineTokens = line.split(" ", 3);
        if (firstLineTokens.length != 3) {
            throw new InstrumentException();
        }

        if (!"101".equals(firstLineTokens[1])) {
            throw new InstrumentException(String.format("Invalid status code received: %s Status line: %s", firstLineTokens[1], line));
        }
        if (!"HTTP/1.1".equalsIgnoreCase(firstLineTokens[0])) {
            throw new InstrumentException(String.format("Invalid status line received: %s Status line: %s", firstLineTokens[0], line));
        }
        HandshakeBuilder handshake = new HandshakeBuilder();
        handshake.setHttpStatus(Short.parseShort(firstLineTokens[1]));
        handshake.setHttpStatusMessage(firstLineTokens[2]);

        line = BufferKit.readLine(buf, Charset.UTF_8);
        while (line != null && line.length() > 0) {
            String[] pair = line.split(":", 2);
            if (pair.length != 2)
                throw new InstrumentException("Not an http header");
            // If the handshake contains already a specific key, append the new value
            if (handshake.hasFieldValue(pair[0])) {
                handshake.put(pair[0], handshake.getFieldValue(pair[0]) + "; " + pair[1].replaceFirst("^ +", ""));
            } else {
                handshake.put(pair[0], pair[1].replaceFirst("^ +", ""));
            }
            line = BufferKit.readLine(buf, Charset.UTF_8);
        }
        if (line == null)
            throw new InstrumentException();
        return handshake;
    }

    public ByteBuffer createBinaryFrame(ByteBuffer framedata) {
        if (Logger.get().isTrace())
            Logger.trace("afterEnconding({}): {}", framedata.remaining(), (framedata.remaining() > 1000 ? "too big to display" : new String(framedata.array())));
        ByteBuffer mes = framedata;
        boolean mask = true;
        int sizebytes = getSizeBytes(mes);
        ByteBuffer buf = ByteBuffer.allocate(1 + (sizebytes > 1 ? sizebytes + 1 : sizebytes) + (mask ? 4 : 0) + mes.remaining());
        byte optcode = 1;
        byte one = -128;
        one |= optcode;
        buf.put(one);
        byte[] payloadlengthbytes = toByteArray(mes.remaining(), sizebytes);
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
            for (int i = 0; mes.hasRemaining(); i++) {
                buf.put((byte) (mes.get() ^ maskkey.get(i % 4)));
            }
        } else {
            buf.put(mes);
            mes.flip();
        }
        assert (buf.remaining() == 0) : buf.remaining();
        buf.flip();
        return buf;
    }

    private Framedatads translateSingleFrame(ByteBuffer buffer) {
        if (buffer == null)
            throw new IllegalArgumentException();
        int maxpacketsize = buffer.remaining();
        this.realpacketsize = 2;
        byte b1 = buffer.get( /*0*/);

        boolean rsv1 = (b1 & 0x40) != 0;
        boolean rsv2 = (b1 & 0x20) != 0;
        boolean rsv3 = (b1 & 0x10) != 0;
        byte b2 = buffer.get( /*1*/);
        boolean mask = (b2 & -128) != 0;
        int payloadlength = (byte) (b2 & ~(byte) 128);

        if (!(payloadlength >= 0 && payloadlength <= 125)) {
            int[] array = {payloadlength, realpacketsize};
            translateSingleFramePayloadLength(array, buffer, payloadlength, maxpacketsize, realpacketsize);
            payloadlength = array[0];
            realpacketsize = array[1];
        }
        realpacketsize += (mask ? 4 : 0);
        realpacketsize += payloadlength;

        if (maxpacketsize < realpacketsize) {
            Logger.trace("Incomplete frame: maxpacketsize < realpacketsize");
            throw new InstrumentException("" + realpacketsize);
        }
        ByteBuffer payload = ByteBuffer.allocate(payloadlength);
        if (mask) {
            byte[] maskskey = new byte[4];
            buffer.get(maskskey);
            for (int i = 0; i < payloadlength; i++) {
                payload.put((byte) (buffer.get( /*payloadstart + i*/) ^ maskskey[i % 4]));
            }
        } else {
            payload.put(buffer.array(), buffer.position(), payload.limit());
            buffer.position(buffer.position() + payload.limit());
        }

        Framedatads frame = new Framedatads();
        payload.flip();
        frame.setPayload(payload);
        if (Logger.get().isTrace())
            Logger.trace("afterDecoding({}): {}", frame.getPayload().remaining(), (frame.getPayload().remaining() > 1000 ? "too big to display" : new String(frame.getPayload().array())));
        return frame;
    }

    /**
     * Translate the buffer depending when it has an extended payload length (126 or 127)
     *
     * @param buffer            the buffer to read from
     * @param oldPayloadlength  the old payload length
     * @param maxpacketsize     the max packet size allowed
     * @param oldRealpacketsize the real packet size
     * @return the new payload data containing new payload length and new packet size
     * @throws InstrumentException thrown if a control frame has an invalid length
     */
    private void translateSingleFramePayloadLength(int[] array, ByteBuffer buffer, int oldPayloadlength, int maxpacketsize, int oldRealpacketsize) throws InstrumentException {
        int payloadlength = oldPayloadlength,
                realpacketsize = oldRealpacketsize;

        if (payloadlength == 126) {
            realpacketsize += 2; // additional length bytes
            byte[] sizebytes = new byte[3];
            sizebytes[1] = buffer.get( /*1 + 1*/);
            sizebytes[2] = buffer.get( /*1 + 2*/);
            payloadlength = new BigInteger(sizebytes).intValue();
        } else {
            realpacketsize += 8; // additional length bytes
            byte[] bytes = new byte[8];
            for (int i = 0; i < 8; i++) {
                bytes[i] = buffer.get( /*1 + i*/);
            }
            long length = new BigInteger(bytes).longValue();
            payloadlength = (int) length;
        }

        array[0] = payloadlength;
        array[1] = realpacketsize;
    }

    public HandshakeBuilder postProcessHandshakeRequestAsClient(HandshakeBuilder request) {
        request.put(UPGRADE, "websocket");
        request.put(CONNECTION, UPGRADE); // to respond to a Connection keep alives
        request.put(SEC_WEB_SOCKET_KEY, RandomKit.randomString(16));
        request.put("Sec-WebSocket-Version", "13");// overwriting the previouss
        return request;
    }

    /**
     * Get a byte that can set RSV bits when OR(|)'d.
     * 0 1 2 3 4 5 6 7
     * +-+-+-+-+-------+
     * |F|R|R|R| opcode|
     * |I|S|S|S|  (4)  |
     * |N|V|V|V|       |
     * | |1|2|3|       |
     *
     * @param rsv Can only be {0, 1, 2, 3}
     * @return byte that represents which RSV bit is set.
     */
    private byte getRSVByte(int rsv) {
        if (rsv == 1) // 0100 0000
            return 0x40;
        if (rsv == 2) // 0010 0000
            return 0x20;
        if (rsv == 3) // 0001 0000
            return 0x10;
        return 0;
    }

    /**
     * Get the mask byte if existing
     *
     * @param mask is mask active or not
     * @return -128 for true, 0 for false
     */
    private byte getMaskByte(boolean mask) {
        return mask ? (byte) -128 : 0;
    }

    /**
     * Get the size bytes for the byte buffer
     *
     * @param mes the current buffer
     * @return the size bytes
     */
    private int getSizeBytes(ByteBuffer mes) {
        if (mes.remaining() <= 125) {
            return 1;
        } else if (mes.remaining() <= 65535) {
            return 2;
        }
        return 8;
    }

    public List<Framedatads> translateFrame(ByteBuffer buffer) {
        while (true) {
            List<Framedatads> frames = new LinkedList<>();
            Framedatads cur;
            if (byteBuffer != null) {

                buffer.mark();
                int availableNextByteCount = buffer.remaining();// The number of bytes received
                int expectedNextByteCount = byteBuffer.remaining();// The number of bytes to complete the incomplete frame

                if (expectedNextByteCount > availableNextByteCount) {
                    // did not receive enough bytes to complete the frame
                    byteBuffer.put(buffer.array(), buffer.position(), availableNextByteCount);
                    buffer.position(buffer.position() + availableNextByteCount);
                    return Collections.emptyList();
                }
                byteBuffer.put(buffer.array(), buffer.position(), expectedNextByteCount);
                buffer.position(buffer.position() + expectedNextByteCount);
                cur = translateSingleFrame((ByteBuffer) byteBuffer.duplicate().position(0));
                frames.add(cur);
                byteBuffer = null;

            }

            while (buffer.hasRemaining()) {// Read as much as possible full frames
                buffer.mark();
                try {
                    cur = translateSingleFrame(buffer);
                    frames.add(cur);
                } catch (InstrumentException e) {
                    buffer.reset();
                    byteBuffer = ByteBuffer.allocate(this.realpacketsize);
                    byteBuffer.put(buffer);
                    break;
                }
            }
            return frames;
        }
    }

    private byte[] toByteArray(long val, int bytecount) {
        byte[] buffer = new byte[bytecount];
        int highest = 8 * bytecount - 8;
        for (int i = 0; i < bytecount; i++) {
            buffer[i] = (byte) (val >>> (highest - 8 * i));
        }
        return buffer;
    }

    /**
     * Process the frame if it is a text frame
     *
     * @param RFCWebSocket the websocket impl
     * @param frame        the frame
     */
    void processFrameText(RFCWebSocket RFCWebSocket, Framedatads frame) {
        try {
            RFCWebSocket.getWebSocketListener().onWebsocketMessage(BufferKit.readLine(frame.getPayload()));
        } catch (RuntimeException e) {
            RFCWebSocket.getWebSocketListener().onWebsocketError(e);
        }
    }

    public HandshakeState.CloseHandshakeType getCloseHandshakeType() {
        return HandshakeState.CloseHandshakeType.TWOWAY;
    }

    public Draft copyInstance() {
        return new Draft(maxBufferSize);
    }

    public ByteBuffer createHandshake(HandshakeBuilder HandshakeBuilder, boolean withcontent) {
        StringBuilder bui = new StringBuilder(100);
        if (HandshakeBuilder instanceof HandshakeBuilder) {
            bui.append("GET ").append(HandshakeBuilder.getResourceDescriptor()).append(" HTTP/1.1");
        } else if (HandshakeBuilder instanceof HandshakeBuilder) {
            bui.append("HTTP/1.1 101 ").append(HandshakeBuilder.getHttpStatusMessage());
        } else {
            throw new IllegalArgumentException("unknown role");
        }
        bui.append("\r\n");
        Iterator<String> it = HandshakeBuilder.iterateHttpFields();
        while (it.hasNext()) {
            String fieldname = it.next();
            String fieldvalue = HandshakeBuilder.getFieldValue(fieldname);
            bui.append(fieldname);
            bui.append(": ");
            bui.append(fieldvalue);
            bui.append("\r\n");
        }
        bui.append("\r\n");
        byte[] httpHeader = bui.toString().getBytes(Charset.US_ASCII);
        byte[] content = withcontent ? HandshakeBuilder.getContent() : null;
        ByteBuffer bytebuffer = ByteBuffer.allocate((content == null ? 0 : content.length) + httpHeader.length);
        bytebuffer.put(httpHeader);
        if (content != null) {
            bytebuffer.put(content);
        }
        bytebuffer.flip();
        return bytebuffer;
    }

}
