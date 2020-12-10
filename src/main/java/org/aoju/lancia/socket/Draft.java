package org.aoju.lancia.socket;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.RandomKit;
import org.aoju.bus.logger.Logger;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Implementation for the RFC 6455 websocket protocol
 * This is the recommended class for your websocket connection
 */
public class Draft {

    protected HandshakeState.Opcode continuousFrameType = null;
    /**
     * Handshake specific field for the key
     */
    private static final String SEC_WEB_SOCKET_KEY = "Sec-WebSocket-Key";

    /**
     * Handshake specific field for the accept
     */
    private static final String SEC_WEB_SOCKET_ACCEPT = "Sec-WebSocket-Accept";

    /**
     * Handshake specific field for the upgrade
     */
    private static final String UPGRADE = "Upgrade";

    /**
     * Handshake specific field for the connection
     */
    private static final String CONNECTION = "Connection";

    /**
     * Attribute for the payload of the current continuous frame
     */
    private final List<ByteBuffer> byteBufferList;

    /**
     * Attribute for the maximum allowed size of a frame
     *
     * @since 1.4.0
     */
    private final int maxFrameSize;

    /**
     * Attribute for the current incomplete frame
     */
    private ByteBuffer incompleteframe;

    /**
     * Constructor for the websocket protocol specified by RFC 6455 with custom extensions and protocols
     */
    public Draft() {
        this(Integer.MAX_VALUE);
    }

    /**
     * Constructor for the websocket protocol specified by RFC 6455 with custom extensions and protocols
     *
     * @param inputMaxFrameSize the maximum allowed size of a frame (the real payload size, decoded frames can be bigger)
     */
    public Draft(int inputMaxFrameSize) {
        if (inputMaxFrameSize < 1) {
            throw new IllegalArgumentException();
        }

        byteBufferList = new ArrayList<>();
        maxFrameSize = inputMaxFrameSize;
    }

    public static ByteBuffer readLine(ByteBuffer buf) {
        ByteBuffer sbuf = ByteBuffer.allocate(buf.remaining());
        byte prev;
        byte cur = '0';
        while (buf.hasRemaining()) {
            prev = cur;
            cur = buf.get();
            sbuf.put(cur);
            if (prev == (byte) '\r' && cur == (byte) '\n') {
                sbuf.limit(sbuf.position() - 2);
                sbuf.position(0);
                return sbuf;

            }
        }
        buf.position(buf.position() - sbuf.position());
        return null;
    }

    public HandshakeBuilder postProcessHandshakeRequestAsClient(HandshakeBuilder request) {
        request.put(UPGRADE, "websocket");
        request.put(CONNECTION, UPGRADE);
        request.put(SEC_WEB_SOCKET_KEY, RandomKit.randomString(16));
        request.put("Sec-WebSocket-Version", "13");

        return request;
    }

    public static String readStringLine(ByteBuffer buf) {
        ByteBuffer b = readLine(buf);
        return b == null ? null : Base64.stringAscii(b.array(), 0, b.limit());
    }


    public ByteBuffer createBinaryFrame(Framedata framedata) {
        if (Logger.get().isTrace())
            Logger.trace("afterEnconding({}): {}", framedata.getPayloadData().remaining(), (framedata.getPayloadData().remaining() > 1000 ? "too big to display" : new String(framedata.getPayloadData().array())));
        return createByteBufferFromFramedata(framedata);
    }

    private ByteBuffer createByteBufferFromFramedata(Framedata framedata) {
        ByteBuffer mes = framedata.getPayloadData();
        boolean mask = true;
        int sizebytes = getSizeBytes(mes);
        ByteBuffer buf = ByteBuffer.allocate(1 + (sizebytes > 1 ? sizebytes + 1 : sizebytes) + (mask ? 4 : 0) + mes.remaining());
        byte optcode = fromOpcode(framedata.getOpcode());
        byte one = (byte) (framedata.isFin() ? -128 : 0);
        one |= optcode;
        if (framedata.isRSV1())
            one |= getRSVByte(1);
        if (framedata.isRSV2())
            one |= getRSVByte(2);
        if (framedata.isRSV3())
            one |= getRSVByte(3);
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

    private Framedata translateSingleFrame(ByteBuffer buffer) throws IncompleteException {
        if (buffer == null)
            throw new IllegalArgumentException();
        int maxpacketsize = buffer.remaining();
        int realpacketsize = 2;
        translateSingleFrameCheckPacketSize(maxpacketsize, realpacketsize);
        byte b1 = buffer.get( /*0*/);
        boolean fin = b1 >> 8 != 0;
        boolean rsv1 = (b1 & 0x40) != 0;
        boolean rsv2 = (b1 & 0x20) != 0;
        boolean rsv3 = (b1 & 0x10) != 0;
        byte b2 = buffer.get( /*1*/);
        boolean mask = (b2 & -128) != 0;
        int payloadlength = (byte) (b2 & ~(byte) 128);
        HandshakeState.Opcode optcode = toOpcode((byte) (b1 & 15));

        if (!(payloadlength >= 0 && payloadlength <= 125)) {
            int[] array = {payloadlength, realpacketsize};
            translateSingleFramePayloadLength(array, buffer, optcode, payloadlength, maxpacketsize, realpacketsize);
            payloadlength = array[0];
            realpacketsize = array[1];
        }
        translateSingleFrameCheckLengthLimit(payloadlength);
        realpacketsize += (mask ? 4 : 0);
        realpacketsize += payloadlength;
        translateSingleFrameCheckPacketSize(maxpacketsize, realpacketsize);

        ByteBuffer payload = ByteBuffer.allocate(checkAlloc(payloadlength));
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

        Framedata frame = Framedata.get(optcode);
        frame.setFin(fin);
        frame.setRSV1(rsv1);
        frame.setRSV2(rsv2);
        frame.setRSV3(rsv3);
        payload.flip();
        frame.setPayload(payload);
        if (Logger.get().isTrace())
            Logger.trace("afterDecoding({}): {}", frame.getPayloadData().remaining(), (frame.getPayloadData().remaining() > 1000 ? "too big to display" : new String(frame.getPayloadData().array())));
        frame.isValid();
        return frame;
    }

    /**
     * Translate the buffer depending when it has an extended payload length (126 or 127)
     *
     * @param buffer            the buffer to read from
     * @param optcode           the decoded optcode
     * @param oldPayloadlength  the old payload length
     * @param maxpacketsize     the max packet size allowed
     * @param oldRealpacketsize the real packet size
     * @return the new payload data containing new payload length and new packet size
     * @throws InstrumentException thrown if a control frame has an invalid length
     * @throws IncompleteException if the maxpacketsize is smaller than the realpackagesize
     */
    private void translateSingleFramePayloadLength(int[] array, ByteBuffer buffer, HandshakeState.Opcode optcode, int oldPayloadlength, int maxpacketsize, int oldRealpacketsize) throws InstrumentException, IncompleteException {
        int payloadlength = oldPayloadlength,
                realpacketsize = oldRealpacketsize;
        if (optcode == HandshakeState.Opcode.PING || optcode == HandshakeState.Opcode.PONG || optcode == HandshakeState.Opcode.CLOSING) {
            Logger.trace("Invalid frame: more than 125 octets");
            throw new InstrumentException("more than 125 octets");
        }
        if (payloadlength == 126) {
            realpacketsize += 2; // additional length bytes
            translateSingleFrameCheckPacketSize(maxpacketsize, realpacketsize);
            byte[] sizebytes = new byte[3];
            sizebytes[1] = buffer.get( /*1 + 1*/);
            sizebytes[2] = buffer.get( /*1 + 2*/);
            payloadlength = new BigInteger(sizebytes).intValue();
        } else {
            realpacketsize += 8; // additional length bytes
            translateSingleFrameCheckPacketSize(maxpacketsize, realpacketsize);
            byte[] bytes = new byte[8];
            for (int i = 0; i < 8; i++) {
                bytes[i] = buffer.get( /*1 + i*/);
            }
            long length = new BigInteger(bytes).longValue();
            translateSingleFrameCheckLengthLimit(length);
            payloadlength = (int) length;
        }

        array[0] = payloadlength;
        array[1] = realpacketsize;
    }

    /**
     * Check if the frame size exceeds the allowed limit
     *
     * @param length the current payload length
     */
    private void translateSingleFrameCheckLengthLimit(long length) {
        if (length > Integer.MAX_VALUE) {
            Logger.trace("Limit exedeed: Payloadsize is to big...");
            throw new InstrumentException("Payloadsize is to big...");
        }
        if (length > maxFrameSize) {
            Logger.trace("Payload limit reached. Allowed: {} Current: {}", maxFrameSize, length);
            throw new InstrumentException("Payload limit reached.", maxFrameSize);
        }
        if (length < 0) {
            Logger.trace("Limit underflow: Payloadsize is to little...");
            throw new InstrumentException("Payloadsize is to little...");
        }
    }

    /**
     * Check if the max packet size is smaller than the real packet size
     *
     * @param maxpacketsize  the max packet size
     * @param realpacketsize the real packet size
     * @throws IncompleteException if the maxpacketsize is smaller than the realpackagesize
     */
    private void translateSingleFrameCheckPacketSize(int maxpacketsize, int realpacketsize) throws IncompleteException {
        if (maxpacketsize < realpacketsize) {
            Logger.trace("Incomplete frame: maxpacketsize < realpacketsize");
            throw new IncompleteException(realpacketsize);
        }
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


    public List<Framedata> translateFrame(ByteBuffer buffer) {
        while (true) {
            List<Framedata> frames = new LinkedList<>();
            Framedata cur;
            if (incompleteframe != null) {
                // complete an incomplete frame
                try {
                    buffer.mark();
                    int availableNextByteCount = buffer.remaining();// The number of bytes received
                    int expectedNextByteCount = incompleteframe.remaining();// The number of bytes to complete the incomplete frame

                    if (expectedNextByteCount > availableNextByteCount) {
                        // did not receive enough bytes to complete the frame
                        incompleteframe.put(buffer.array(), buffer.position(), availableNextByteCount);
                        buffer.position(buffer.position() + availableNextByteCount);
                        return Collections.emptyList();
                    }
                    incompleteframe.put(buffer.array(), buffer.position(), expectedNextByteCount);
                    buffer.position(buffer.position() + expectedNextByteCount);
                    cur = translateSingleFrame((ByteBuffer) incompleteframe.duplicate().position(0));
                    frames.add(cur);
                    incompleteframe = null;
                } catch (IncompleteException e) {
                    // extending as much as suggested
                    ByteBuffer extendedframe = ByteBuffer.allocate(checkAlloc(e.getPreferredSize()));
                    assert (extendedframe.limit() > incompleteframe.limit());
                    incompleteframe.rewind();
                    extendedframe.put(incompleteframe);
                    incompleteframe = extendedframe;
                    continue;
                }
            }

            while (buffer.hasRemaining()) {// Read as much as possible full frames
                buffer.mark();
                try {
                    cur = translateSingleFrame(buffer);
                    frames.add(cur);
                } catch (IncompleteException e) {
                    buffer.reset();
                    int pref = e.getPreferredSize();
                    incompleteframe = ByteBuffer.allocate(checkAlloc(pref));
                    incompleteframe.put(buffer);
                    break;
                }
            }
            return frames;
        }
    }

    public List<Framedata> createFrames(ByteBuffer binary, boolean mask) {
        Framedata curframe = new Framedata(HandshakeState.Opcode.BINARY);
        curframe.setPayload(binary);
        curframe.setTransferemasked(mask);
        curframe.isValid();
        return Collections.singletonList(curframe);
    }

    public List<Framedata> createFrames(String text, boolean mask) {
        Framedata curframe = new Framedata(HandshakeState.Opcode.TEXT);
        curframe.setPayload(ByteBuffer.wrap(Base64.utf8Bytes(text)));
        curframe.setTransferemasked(mask);
        curframe.isValid();
        return Collections.singletonList(curframe);
    }

    public void reset() {
        incompleteframe = null;
    }

    private byte[] toByteArray(long val, int bytecount) {
        byte[] buffer = new byte[bytecount];
        int highest = 8 * bytecount - 8;
        for (int i = 0; i < bytecount; i++) {
            buffer[i] = (byte) (val >>> (highest - 8 * i));
        }
        return buffer;
    }

    private byte fromOpcode(HandshakeState.Opcode opcode) {
        if (opcode == HandshakeState.Opcode.CONTINUOUS)
            return 0;
        else if (opcode == HandshakeState.Opcode.TEXT)
            return 1;
        else if (opcode == HandshakeState.Opcode.BINARY)
            return 2;
        else if (opcode == HandshakeState.Opcode.CLOSING)
            return 8;
        else if (opcode == HandshakeState.Opcode.PING)
            return 9;
        else if (opcode == HandshakeState.Opcode.PONG)
            return 10;
        throw new IllegalArgumentException("Don't know how to handle " + opcode.toString());
    }

    private HandshakeState.Opcode toOpcode(byte opcode) throws InstrumentException {
        switch (opcode) {
            case 0:
                return HandshakeState.Opcode.CONTINUOUS;
            case 1:
                return HandshakeState.Opcode.TEXT;
            case 2:
                return HandshakeState.Opcode.BINARY;
            case 8:
                return HandshakeState.Opcode.CLOSING;
            case 9:
                return HandshakeState.Opcode.PING;
            case 10:
                return HandshakeState.Opcode.PONG;
            default:
                throw new InstrumentException("Unknown opcode " + (short) opcode);
        }
    }

    public void processFrame(WebSocketImpl webSocketImpl, Framedata frame) throws InvalidDataException {
        HandshakeState.Opcode curop = frame.getOpcode();
        if (curop == HandshakeState.Opcode.CLOSING) {
            processFrameClosing(webSocketImpl, frame);
        } else if (curop == HandshakeState.Opcode.PING) {
            webSocketImpl.getWebSocketListener().onWebsocketPing(webSocketImpl, frame);
        } else if (curop == HandshakeState.Opcode.PONG) {
            webSocketImpl.updateLastPong();
            // webSocketImpl.getWebSocketListener().onWebsocketPong(webSocketImpl, frame);
        } else if (!frame.isFin() || curop == HandshakeState.Opcode.CONTINUOUS) {
            processFrameContinuousAndNonFin(frame, curop);
        } else if (curop == HandshakeState.Opcode.TEXT) {
            processFrameText(webSocketImpl, frame);
        } else if (curop == HandshakeState.Opcode.BINARY) {
            processFrameBinary(webSocketImpl, frame);
        } else {
            Logger.error("non control or continious frame expected");
            throw new InvalidDataException(Framedata.PROTOCOL_ERROR, "non control or continious frame expected");
        }
    }

    /**
     * Process the frame if it is a continuous frame or the fin bit is not set
     *
     * @param frame the current frame
     * @param curop the current Opcode
     * @throws InvalidDataException if there is a protocol error
     */
    private void processFrameContinuousAndNonFin(Framedata frame, HandshakeState.Opcode curop) throws InvalidDataException {
        if (curop != HandshakeState.Opcode.CONTINUOUS) {
            checkBufferLimit();
        } else if (frame.isFin()) {
            clearBufferList();
        }
        //Check if the whole payload is valid utf8, when the opcode indicates a text
        if (curop == HandshakeState.Opcode.TEXT && !Base64.isValidUTF8(frame.getPayloadData())) {
            Logger.error("Protocol error: Payload is not UTF8");
            throw new InvalidDataException(Framedata.NO_UTF8);
        }
        //Checking if the current continuous frame contains a correct payload with the other frames combined
        if (curop == HandshakeState.Opcode.CONTINUOUS) {
            addToBufferList(frame.getPayloadData());
        }
    }

    /**
     * Process the frame if it is a binary frame
     *
     * @param webSocketImpl the websocket impl
     * @param frame         the frame
     */
    private void processFrameBinary(WebSocketImpl webSocketImpl, Framedata frame) {
        try {
            webSocketImpl.getWebSocketListener().onWebsocketMessage(webSocketImpl, frame.getPayloadData());
        } catch (RuntimeException e) {
            logRuntimeException(webSocketImpl, e);
        }
    }

    /**
     * Log the runtime exception to the specific WebSocketImpl
     *
     * @param webSocketImpl the implementation of the websocket
     * @param e             the runtime exception
     */
    private void logRuntimeException(WebSocketImpl webSocketImpl, RuntimeException e) {
        Logger.error("Runtime exception during onWebsocketMessage", e);
        webSocketImpl.getWebSocketListener().onWebsocketError(webSocketImpl, e);
    }

    /**
     * Process the frame if it is a text frame
     *
     * @param webSocketImpl the websocket impl
     * @param frame         the frame
     */
    private void processFrameText(WebSocketImpl webSocketImpl, Framedata frame) throws InvalidDataException {
        try {
            webSocketImpl.getWebSocketListener().onWebsocketMessage(webSocketImpl, Base64.stringUtf8(frame.getPayloadData()));
        } catch (RuntimeException e) {
            logRuntimeException(webSocketImpl, e);
        }
    }

    /**
     * Process the frame if it is a closing frame
     *
     * @param webSocketImpl the websocket impl
     * @param frame         the frame
     */
    private void processFrameClosing(WebSocketImpl webSocketImpl, Framedata frame) {
        int code = Framedata.NOCODE;
        String reason = "";
        if (frame instanceof Framedata) {
           /* CloseFrame cf = (CloseFrame) frame;
            code = cf.getCloseCode();
            reason = cf.getMessage();*/
        }
        if (webSocketImpl.getReadyState() == HandshakeState.ReadyState.CLOSING) {
            // complete the close handshake by disconnecting
            webSocketImpl.closeConnection(code, reason, true);
        } else {
            // echo close handshake
            if (getCloseHandshakeType() == HandshakeState.CloseHandshakeType.TWOWAY)
                webSocketImpl.close(code, reason, true);
            else
                webSocketImpl.flushAndClose(code, reason, false);
        }
    }

    /**
     * Clear the current bytebuffer list
     */
    private void clearBufferList() {
        synchronized (byteBufferList) {
            byteBufferList.clear();
        }
    }

    /**
     * Add a payload to the current bytebuffer list
     *
     * @param payloadData the new payload
     */
    private void addToBufferList(ByteBuffer payloadData) {
        synchronized (byteBufferList) {
            byteBufferList.add(payloadData);
        }
    }

    /**
     * Check the current size of the buffer and throw an exception if the size is bigger than the max allowed frame size
     */
    private void checkBufferLimit() {
        long totalSize = getByteBufferListSize();
        if (totalSize > maxFrameSize) {
            clearBufferList();
            Logger.trace("Payload limit reached. Allowed: {} Current: {}", maxFrameSize, totalSize);
            throw new InstrumentException("" + maxFrameSize);
        }
    }

    public HandshakeState.CloseHandshakeType getCloseHandshakeType() {
        return HandshakeState.CloseHandshakeType.TWOWAY;
    }

    /**
     * Method to generate a full bytebuffer out of all the fragmented frame payload
     *
     * @return a bytebuffer containing all the data
     */
    private ByteBuffer getPayloadFromByteBufferList() {
        long totalSize = 0;
        ByteBuffer resultingByteBuffer;
        synchronized (byteBufferList) {
            for (ByteBuffer buffer : byteBufferList) {
                totalSize += buffer.limit();
            }
            checkBufferLimit();
            resultingByteBuffer = ByteBuffer.allocate((int) totalSize);
            for (ByteBuffer buffer : byteBufferList) {
                resultingByteBuffer.put(buffer);
            }
        }
        resultingByteBuffer.flip();
        return resultingByteBuffer;
    }

    /**
     * Get the current size of the resulting bytebuffer in the bytebuffer list
     *
     * @return the size as long (to not get an integer overflow)
     */
    private long getByteBufferListSize() {
        long totalSize = 0;
        synchronized (byteBufferList) {
            for (ByteBuffer buffer : byteBufferList) {
                totalSize += buffer.limit();
            }
        }
        return totalSize;
    }

    public static HandshakeBuilder translateHandshakeHttp(ByteBuffer buf) throws InstrumentException {
        HandshakeBuilder handshake;

        String line = readStringLine(buf);
        if (line == null)
            throw new InstrumentException("" + buf.capacity() + 128);

        String[] firstLineTokens = line.split(" ", 3);// eg. HTTP/1.1 101 Switching the Protocols
        if (firstLineTokens.length != 3) {
            throw new InstrumentException();
        }

        handshake = translateHandshakeHttpClient(firstLineTokens, line);

        line = readStringLine(buf);
        while (line != null && line.length() > 0) {
            String[] pair = line.split(":", 2);
            if (pair.length != 2)
                throw new InstrumentException("not an http header");
            // If the handshake contains already a specific key, append the new value
            if (handshake.hasFieldValue(pair[0])) {
                handshake.put(pair[0], handshake.getFieldValue(pair[0]) + "; " + pair[1].replaceFirst("^ +", ""));
            } else {
                handshake.put(pair[0], pair[1].replaceFirst("^ +", ""));
            }
            line = readStringLine(buf);
        }
        if (line == null)
            throw new InstrumentException();
        return handshake;
    }

    /**
     * Checking the handshake for the role as client
     *
     * @param firstLineTokens the token of the first line split as as an string array
     * @param line            the whole line
     * @return a handshake
     */
    private static HandshakeBuilder translateHandshakeHttpClient(String[] firstLineTokens, String line) throws InstrumentException {
        if (!"101".equals(firstLineTokens[1])) {
            throw new InstrumentException(String.format("Invalid status code received: %s Status line: %s", firstLineTokens[1], line));
        }
        if (!"HTTP/1.1".equalsIgnoreCase(firstLineTokens[0])) {
            throw new InstrumentException(String.format("Invalid status line received: %s Status line: %s", firstLineTokens[0], line));
        }
        HandshakeBuilder serverhandshake = new HandshakeBuilder();
        serverhandshake.setHttpStatus(Short.parseShort(firstLineTokens[1]));
        serverhandshake.setHttpStatusMessage(firstLineTokens[2]);
        return serverhandshake;
    }

    public HandshakeState acceptHandshakeAsClient(HandshakeBuilder request, HandshakeBuilder response) throws InstrumentException {
        if (!basicAccept(response)) {
            Logger.trace("acceptHandshakeAsClient - Missing/wrong upgrade or connection in handshake.");
            return HandshakeState.NOT_MATCHED;
        }
        if (!request.hasFieldValue(SEC_WEB_SOCKET_KEY) || !response.hasFieldValue(SEC_WEB_SOCKET_ACCEPT)) {
            Logger.trace("acceptHandshakeAsClient - Missing Sec-WebSocket-Key or Sec-WebSocket-Accept");
            return HandshakeState.NOT_MATCHED;
        }


        return HandshakeState.MATCHED;
    }

    public Draft copyInstance() {
        return new Draft(maxFrameSize);
    }

    protected boolean basicAccept(HandshakeBuilder HandshakeBuilder) {
        return HandshakeBuilder.getFieldValue("Upgrade").equalsIgnoreCase("websocket") && HandshakeBuilder.getFieldValue("Connection").toLowerCase(Locale.ENGLISH).contains("upgrade");
    }

    public List<Framedata> continuousFrame(HandshakeState.Opcode op, ByteBuffer buffer, boolean fin) {
        if (op != HandshakeState.Opcode.BINARY && op != HandshakeState.Opcode.TEXT) {
            throw new IllegalArgumentException("Only Opcode.BINARY or  Opcode.TEXT are allowed");
        }
        Framedata bui = null;
        if (continuousFrameType != null) {
            bui = new Framedata(HandshakeState.Opcode.CONTINUOUS);
        } else {
            continuousFrameType = op;
            if (op == HandshakeState.Opcode.BINARY) {
                bui = new Framedata(HandshakeState.Opcode.BINARY);
            } else if (op == HandshakeState.Opcode.TEXT) {
                bui = new Framedata(HandshakeState.Opcode.TEXT);
            }
        }
        bui.setPayload(buffer);
        bui.setFin(fin);
        bui.isValid();
        if (fin) {
            continuousFrameType = null;
        } else {
            continuousFrameType = op;
        }
        return Collections.singletonList(bui);
    }

    public List<ByteBuffer> createHandshake(HandshakeBuilder HandshakeBuilder) {
        return createHandshake(HandshakeBuilder, true);
    }

    public List<ByteBuffer> createHandshake(HandshakeBuilder HandshakeBuilder, boolean withcontent) {
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
        byte[] httpheader = Base64.asciiBytes(bui.toString());

        byte[] content = withcontent ? HandshakeBuilder.getContent() : null;
        ByteBuffer bytebuffer = ByteBuffer.allocate((content == null ? 0 : content.length) + httpheader.length);
        bytebuffer.put(httpheader);
        if (content != null) {
            bytebuffer.put(content);
        }
        bytebuffer.flip();
        return Collections.singletonList(bytebuffer);
    }

    public HandshakeBuilder translateHandshake(ByteBuffer buf) throws InstrumentException {
        return translateHandshakeHttp(buf);
    }

    public int checkAlloc(int bytecount) throws InstrumentException {
        if (bytecount < 0)
            throw new InstrumentException("Negative count:" + Framedata.PROTOCOL_ERROR);
        return bytecount;
    }

}
