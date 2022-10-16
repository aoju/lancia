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

import org.aoju.bus.core.codec.Base64;
import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.logger.Logger;
import org.aoju.lancia.Builder;
import org.aoju.lancia.worker.exception.SocketException;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

/**
 * Implementation for the RFC 6455 websocket socketProtocol This is the recommended class for your
 * websocket connection
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class Draft_6455 {

    /**
     * HandshakeBuilder specific field for the key
     */
    private static final String SEC_WEB_SOCKET_KEY = "Sec-WebSocket-Key";

    /**
     * HandshakeBuilder specific field for the socketProtocol
     */
    private static final String SEC_WEB_SOCKET_PROTOCOL = "Sec-WebSocket-SocketProtocol";

    /**
     * Handshake specific field for the extension
     */
    private static final String SEC_WEB_SOCKET_EXTENSIONS = "Sec-WebSocket-Extensions";

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
     * Attribute for the reusable random instance
     */
    private final SecureRandom reuseableRandom = new SecureRandom();
    /**
     * Attribute for all available extension in this draft
     */
    private final List<String> knownExtensions;
    /**
     * Attribute for all available protocols in this draft
     */
    private final List<SocketProtocol> knownSocketProtocols;
    /**
     * Attribute for the maximum allowed size of a frame
     */
    private final int maxFrameSize;
    protected Builder.Opcode continuousFrameType = null;
    /**
     * Attribute for the used extension in this draft
     */
    private String negotiatedExtension;
    /**
     * Attribute for the used socketProtocol in this draft
     */
    private SocketProtocol socketProtocol;
    /**
     * Attribute for the current incomplete frame
     */
    private ByteBuffer incompleteframe;

    /**
     * Constructor for the websocket socketProtocol specified by RFC 6455 with default extensions
     */
    public Draft_6455() {
        this(Collections.emptyList());
    }

    /**
     * Constructor for the websocket socketProtocol specified by RFC 6455 with custom extensions
     *
     * @param inputExtensions the extensions which should be used for this draft
     */
    public Draft_6455(List<String> inputExtensions) {
        this(inputExtensions, Collections.singletonList(new SocketProtocol("")));
    }

    /**
     * Constructor for the websocket socketProtocol specified by RFC 6455 with custom extensions and
     * protocols
     *
     * @param inputExtensions      the extensions which should be used for this draft
     * @param inputSocketProtocols the protocols which should be used for this draft
     */
    public Draft_6455(List<String> inputExtensions, List<SocketProtocol> inputSocketProtocols) {
        this(inputExtensions, inputSocketProtocols, Integer.MAX_VALUE);
    }

    /**
     * Constructor for the websocket socketProtocol specified by RFC 6455 with custom extensions and
     * protocols
     *
     * @param inputExtensions      the extensions which should be used for this draft
     * @param inputSocketProtocols the protocols which should be used for this draft
     * @param inputMaxFrameSize    the maximum allowed size of a frame (the real payload size, decoded
     *                             frames can be bigger)
     */
    public Draft_6455(List<String> inputExtensions, List<SocketProtocol> inputSocketProtocols,
                      int inputMaxFrameSize) {
        if (inputExtensions == null || inputSocketProtocols == null || inputMaxFrameSize < 1) {
            throw new IllegalArgumentException();
        }
        knownExtensions = new ArrayList<>(inputExtensions.size());
        knownSocketProtocols = new ArrayList<>(inputSocketProtocols.size());
        boolean hasDefault = false;
        for (String inputExtension : inputExtensions) {
            if (inputExtension.getClass().equals(String.class)) {
                hasDefault = true;
                break;
            }
        }
        knownExtensions.addAll(inputExtensions);
        //We always add the DefaultExtension to implement the normal RFC 6455 specification
        if (!hasDefault) {
            knownExtensions.add(this.knownExtensions.size(), negotiatedExtension);
        }
        knownSocketProtocols.addAll(inputSocketProtocols);
        maxFrameSize = inputMaxFrameSize;
    }

    public static ByteBuffer readLine(ByteBuffer buf) {
        ByteBuffer b = ByteBuffer.allocate(buf.remaining());
        byte prev;
        byte cur = '0';
        while (buf.hasRemaining()) {
            prev = cur;
            cur = buf.get();
            b.put(cur);
            if (prev == (byte) '\r' && cur == (byte) '\n') {
                b.limit(b.position() - 2);
                b.position(0);
                return b;
            }
        }
        // ensure that there wont be any bytes skipped
        buf.position(buf.position() - b.position());
        return null;
    }

    public static String readStringLine(ByteBuffer buf) {
        ByteBuffer b = readLine(buf);
        return b == null ? null : new String(b.array(), 0, b.limit(), Charset.US_ASCII);
    }

    public static HandshakeBuilder translateHandshakeHttp(ByteBuffer buf) throws SocketException {
        HandshakeBuilder handshake;

        String line = readStringLine(buf);
        if (line == null) {
            throw new SocketException(buf.capacity() + 128);
        }

        String[] firstLineTokens = line.split(" ", 3);// eg. HTTP/1.1 101 Switching the Protocols
        if (firstLineTokens.length != 3) {
            throw new SocketException(Framedata.PROTOCOL_ERROR);
        }

        handshake = translateHandshakeHttpClient(firstLineTokens, line);

        line = readStringLine(buf);
        while (line != null && line.length() > 0) {
            String[] pair = line.split(":", 2);
            if (pair.length != 2) {
                throw new SocketException(Framedata.PROTOCOL_ERROR, "not an http header");
            }
            // If the handshake contains already a specific key, append the new value
            if (handshake.hasFieldValue(pair[0])) {
                handshake.put(pair[0], handshake.getFieldValue(pair[0]) + "; " + pair[1].replaceFirst("^ +", ""));
            } else {
                handshake.put(pair[0], pair[1].replaceFirst("^ +", ""));
            }
            line = readStringLine(buf);
        }
        if (line == null) {
            throw new SocketException();
        }
        return handshake;
    }

    /**
     * Checking the handshake for the role as client
     *
     * @param firstLineTokens the token of the first line split as as an string array
     * @param line            the whole line
     * @return a handshake
     */
    private static HandshakeBuilder translateHandshakeHttpClient(String[] firstLineTokens, String line) throws SocketException {
        // translating/parsing the response from the SERVER
        if (!"101".equals(firstLineTokens[1])) {
            throw new SocketException(Framedata.PROTOCOL_ERROR, String.format("Invalid status code received: %s Status line: %s", firstLineTokens[1], line));
        }
        if (!"HTTP/1.1".equalsIgnoreCase(firstLineTokens[0])) {
            throw new SocketException(Framedata.PROTOCOL_ERROR, String.format("Invalid status line received: %s Status line: %s", firstLineTokens[0], line));
        }
        HandshakeBuilder handshake = new HandshakeBuilder();
        handshake.setHttpStatus(Short.parseShort(firstLineTokens[1]));
        handshake.setHttpStatusMessage(firstLineTokens[2]);
        return handshake;
    }

    public static String stringUtf8(ByteBuffer bytes) throws SocketException {
        CodingErrorAction codingErrorAction = CodingErrorAction.REPORT;
        CharsetDecoder decode = Charset.UTF_8.newDecoder();
        decode.onMalformedInput(codingErrorAction);
        decode.onUnmappableCharacter(codingErrorAction);
        String s;
        try {
            bytes.mark();
            s = decode.decode(bytes).toString();
            bytes.reset();
        } catch (CharacterCodingException e) {
            throw new SocketException(Framedata.NO_UTF8, e);
        }
        return s;
    }

    /**
     * Check if the requested socketProtocol is part of this draft
     *
     * @param requestedProtocol the requested socketProtocol
     * @return MATCHED if it is matched, otherwise NOT_MATCHED
     */
    private Builder.HandshakeState containsRequestedProtocol(String requestedProtocol) {
        for (SocketProtocol knownSocketProtocol : knownSocketProtocols) {
            if (knownSocketProtocol.accept(requestedProtocol)) {
                socketProtocol = knownSocketProtocol;
                Logger.trace("acceptHandshake - Matching socketProtocol found: {}", socketProtocol);
                return Builder.HandshakeState.MATCHED;
            }
        }
        return Builder.HandshakeState.NOT_MATCHED;
    }

    protected boolean basicAccept(HandshakeBuilder handshake) {
        return handshake.getFieldValue("Upgrade").equalsIgnoreCase("websocket") && handshake.getFieldValue("Connection").toLowerCase(Locale.ENGLISH).contains("upgrade");
    }

    public Builder.HandshakeState acceptHandshakeAsClient(HandshakeBuilder request, HandshakeBuilder response) {
        if (!basicAccept(response)) {
            Logger.trace("acceptHandshakeAsClient - Missing/wrong upgrade or connection in handshake.");
            return Builder.HandshakeState.NOT_MATCHED;
        }
        if (!request.hasFieldValue(SEC_WEB_SOCKET_KEY) || !response
                .hasFieldValue(SEC_WEB_SOCKET_ACCEPT)) {
            Logger.trace("acceptHandshakeAsClient - Missing Sec-WebSocket-Key or Sec-WebSocket-Accept");
            return Builder.HandshakeState.NOT_MATCHED;
        }

        String seckeyAnswer = response.getFieldValue(SEC_WEB_SOCKET_ACCEPT);
        String seckeyChallenge = request.getFieldValue(SEC_WEB_SOCKET_KEY);
        seckeyChallenge = generateFinalKey(seckeyChallenge);

        if (!seckeyChallenge.equals(seckeyAnswer)) {
            Logger.trace("acceptHandshakeAsClient - Wrong key for Sec-WebSocket-Key.");
            return Builder.HandshakeState.NOT_MATCHED;
        }
        Builder.HandshakeState extensionState = Builder.HandshakeState.NOT_MATCHED;
        for (String knownExtension : knownExtensions) {
            negotiatedExtension = knownExtension;
            extensionState = Builder.HandshakeState.MATCHED;
            Logger.trace("acceptHandshakeAsClient - Matching extension found: {}", negotiatedExtension);
            break;
        }
        Builder.HandshakeState protocolState = containsRequestedProtocol(
                response.getFieldValue(SEC_WEB_SOCKET_PROTOCOL));
        if (protocolState == Builder.HandshakeState.MATCHED && extensionState == Builder.HandshakeState.MATCHED) {
            return Builder.HandshakeState.MATCHED;
        }
        Logger.trace("acceptHandshakeAsClient - No matching extension or socketProtocol found.");
        return Builder.HandshakeState.NOT_MATCHED;
    }

    /**
     * Getter for the socketProtocol which is used by this draft
     *
     * @return the socketProtocol which is used or null, if handshake is not yet done or no valid protocols
     */
    public SocketProtocol getProtocol() {
        return socketProtocol;
    }

    /**
     * Getter for all available protocols for this draft
     *
     * @return the protocols which are enabled for this draft
     */
    public List<SocketProtocol> getKnownProtocols() {
        return knownSocketProtocols;
    }

    public HandshakeBuilder postProcessHandshakeRequestAsClient(
            HandshakeBuilder request) {
        request.put(UPGRADE, "websocket");
        request.put(CONNECTION, UPGRADE); // to respond to a Connection keep alives
        byte[] random = new byte[16];
        reuseableRandom.nextBytes(random);
        request.put(SEC_WEB_SOCKET_KEY, Base64.encode(random));
        request.put("Sec-WebSocket-Version", "13");// overwriting the previous
        StringBuilder requestedExtensions = new StringBuilder();
        if (requestedExtensions.length() != 0) {
            request.put(SEC_WEB_SOCKET_EXTENSIONS, requestedExtensions.toString());
        }
        StringBuilder requestedProtocols = new StringBuilder();
        for (SocketProtocol knownSocketProtocol : knownSocketProtocols) {
            if (knownSocketProtocol.getProvided().length() != 0) {
                if (requestedProtocols.length() > 0) {
                    requestedProtocols.append(", ");
                }
                requestedProtocols.append(knownSocketProtocol.getProvided());
            }
        }
        if (requestedProtocols.length() != 0) {
            request.put(SEC_WEB_SOCKET_PROTOCOL, requestedProtocols.toString());
        }
        return request;
    }

    public Draft_6455 copyInstance() {
        List<SocketProtocol> newSocketProtocols = new ArrayList<>();
        for (SocketProtocol knownSocketProtocol : getKnownProtocols()) {
            newSocketProtocols.add(knownSocketProtocol.copyInstance());
        }
        return new Draft_6455(new ArrayList<>(), newSocketProtocols, maxFrameSize);
    }

    public ByteBuffer createBinaryFrame(Framedata framedata) {
        if (Logger.isTrace()) {
            Logger.trace("afterEnconding({}): {}", framedata.getPayloadData().remaining(),
                    (framedata.getPayloadData().remaining() > 1000 ? "too big to display"
                            : new String(framedata.getPayloadData().array())));
        }
        return createByteBufferFromFramedata(framedata);
    }

    public HandshakeBuilder translateHandshake(ByteBuffer buf) throws SocketException {
        return translateHandshakeHttp(buf);
    }

    public List<ByteBuffer> createHandshake(HandshakeBuilder handshake) {
        return createHandshake(handshake, true);
    }

    public List<Framedata> continuousFrame(Builder.Opcode op, ByteBuffer buffer, boolean fin) {
        if (op != Builder.Opcode.BINARY && op != Builder.Opcode.TEXT) {
            throw new IllegalArgumentException("Only Opcode.BINARY or  Opcode.TEXT are allowed");
        }
        Framedata bui = null;
        continuousFrameType = op;
        if (op == Builder.Opcode.TEXT) {
            bui = new Framedata(Builder.Opcode.TEXT);

        }
        bui.setPayload(buffer);
        bui.setFin(fin);
        try {
            bui.isValid();
        } catch (SocketException e) {
            throw new IllegalArgumentException(e); // can only happen when one builds close frames(Opcode.Close)
        }
        if (fin) {
            continuousFrameType = null;
        } else {
            continuousFrameType = op;
        }
        return Collections.singletonList(bui);
    }

    public List<ByteBuffer> createHandshake(HandshakeBuilder handshake, boolean withcontent) {
        StringBuilder bui = new StringBuilder(100);
        bui.append("GET ").append(handshake.getResourceDescriptor()).append(" HTTP/1.1");
        bui.append("\r\n");
        Iterator<String> it = handshake.iterateHttpFields();
        while (it.hasNext()) {
            String fieldname = it.next();
            String fieldvalue = handshake.getFieldValue(fieldname);
            bui.append(fieldname);
            bui.append(": ");
            bui.append(fieldvalue);
            bui.append("\r\n");
        }
        bui.append("\r\n");
        byte[] httpheader = bui.toString().getBytes(Charset.US_ASCII);

        byte[] content = withcontent ? handshake.getContent() : null;
        ByteBuffer bytebuffer = ByteBuffer.allocate((content == null ? 0 : content.length) + httpheader.length);
        bytebuffer.put(httpheader);
        if (content != null) {
            bytebuffer.put(content);
        }
        bytebuffer.flip();
        return Collections.singletonList(bytebuffer);
    }

    private ByteBuffer createByteBufferFromFramedata(Framedata framedata) {
        ByteBuffer mes = framedata.getPayloadData();
        boolean mask = true;
        int sizebytes = getSizeBytes(mes);
        ByteBuffer buf = ByteBuffer.allocate(
                1 + (sizebytes > 1 ? sizebytes + 1 : sizebytes) + (mask ? 4 : 0) + mes.remaining());
        byte optcode = fromOpcode(framedata.getOpcode());
        byte one = (byte) (framedata.isFin() ? -128 : 0);
        one |= optcode;
        if (framedata.isRSV1()) {
            one |= getRSVByte(1);
        }
        if (framedata.isRSV2()) {
            one |= getRSVByte(2);
        }
        if (framedata.isRSV3()) {
            one |= getRSVByte(3);
        }
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
            maskkey.putInt(reuseableRandom.nextInt());
            buf.put(maskkey.array());
            for (int i = 0; mes.hasRemaining(); i++) {
                buf.put((byte) (mes.get() ^ maskkey.get(i % 4)));
            }
        } else {
            buf.put(mes);
            //Reset the position of the bytebuffer e.g. for additional use
            mes.flip();
        }
        assert (buf.remaining() == 0) : buf.remaining();
        buf.flip();
        return buf;
    }

    private Framedata translateSingleFrame(ByteBuffer buffer)
            throws SocketException {
        if (buffer == null) {
            throw new IllegalArgumentException();
        }
        int maxpacketsize = buffer.remaining();
        int realpacketsize = 2;
        translateSingleFrameCheckPacketSize(maxpacketsize, realpacketsize);
        byte b1 = buffer.get(/*0*/);
        boolean fin = b1 >> 8 != 0;
        boolean rsv1 = (b1 & 0x40) != 0;
        boolean rsv2 = (b1 & 0x20) != 0;
        boolean rsv3 = (b1 & 0x10) != 0;
        byte b2 = buffer.get(/*1*/);
        boolean mask = (b2 & -128) != 0;
        int payloadlength = (byte) (b2 & ~(byte) 128);
        Builder.Opcode optcode = toOpcode((byte) (b1 & 15));

        if (!(payloadlength >= 0 && payloadlength <= 125)) {
            TranslatedPayloadMetaData payloadData = translateSingleFramePayloadLength(buffer, optcode,
                    payloadlength, maxpacketsize, realpacketsize);
            payloadlength = payloadData.getPayloadLength();
            realpacketsize = payloadData.getRealPackageSize();
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
                payload.put((byte) (buffer.get(/*payloadstart + i*/) ^ maskskey[i % 4]));
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

        if (Logger.isTrace()) {
            Logger.trace("afterDecoding({}): {}", frame.getPayloadData().remaining(),
                    (frame.getPayloadData().remaining() > 1000 ? "too big to display"
                            : new String(frame.getPayloadData().array())));
        }
        frame.isValid();
        return frame;
    }

    public int checkAlloc(int bytecount) throws SocketException {
        if (bytecount < 0) {
            throw new SocketException(Framedata.PROTOCOL_ERROR, "Negative count");
        }
        return bytecount;
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
     * @throws SocketException if the maxpacketsize is smaller than the realpackagesize
     */
    private TranslatedPayloadMetaData translateSingleFramePayloadLength(ByteBuffer buffer,
                                                                        Builder.Opcode optcode, int oldPayloadlength, int maxpacketsize, int oldRealpacketsize)
            throws SocketException {
        int payloadlength = oldPayloadlength;
        int realpacketsize = oldRealpacketsize;
        if (optcode == Builder.Opcode.PING || optcode == Builder.Opcode.PONG || optcode == Builder.Opcode.CLOSING) {
            Logger.trace("Invalid frame: more than 125 octets");
            throw new SocketException(Framedata.PROTOCOL_ERROR, "more than 125 octets");
        }
        if (payloadlength == 126) {
            realpacketsize += 2; // additional length bytes
            translateSingleFrameCheckPacketSize(maxpacketsize, realpacketsize);
            byte[] sizebytes = new byte[3];
            sizebytes[1] = buffer.get(/*1 + 1*/);
            sizebytes[2] = buffer.get(/*1 + 2*/);
            payloadlength = new BigInteger(sizebytes).intValue();
        } else {
            realpacketsize += 8; // additional length bytes
            translateSingleFrameCheckPacketSize(maxpacketsize, realpacketsize);
            byte[] bytes = new byte[8];
            for (int i = 0; i < 8; i++) {
                bytes[i] = buffer.get(/*1 + i*/);
            }
            long length = new BigInteger(bytes).longValue();
            translateSingleFrameCheckLengthLimit(length);
            payloadlength = (int) length;
        }
        return new TranslatedPayloadMetaData(payloadlength, realpacketsize);
    }

    /**
     * Check if the frame size exceeds the allowed limit
     *
     * @param length the current payload length
     * @throws SocketException if the payload length is to big
     */
    private void translateSingleFrameCheckLengthLimit(long length) throws SocketException {
        if (length > Integer.MAX_VALUE) {
            Logger.trace("Limit exedeed: Payloadsize is to big...");
            throw new SocketException(Integer.MAX_VALUE, "Payloadsize is to big...");
        }
        if (length > maxFrameSize) {
            Logger.trace("Payload limit reached. Allowed: {} Current: {}", maxFrameSize, length);
            throw new SocketException("Payload limit reached.", maxFrameSize);
        }
        if (length < 0) {
            Logger.trace("Limit underflow: Payloadsize is to little...");
            throw new SocketException(Integer.MAX_VALUE, "Payloadsize is to little...");
        }
    }

    /**
     * Check if the max packet size is smaller than the real packet size
     *
     * @param maxpacketsize  the max packet size
     * @param realpacketsize the real packet size
     * @throws SocketException if the maxpacketsize is smaller than the realpackagesize
     */
    private void translateSingleFrameCheckPacketSize(int maxpacketsize, int realpacketsize)
            throws SocketException {
        if (maxpacketsize < realpacketsize) {
            Logger.trace("Incomplete frame: maxpacketsize < realpacketsize");
            throw new SocketException(realpacketsize);
        }
    }

    /**
     * Get a byte that can set RSV bits when OR(|)'d. 0 1 2 3 4 5 6 7 +-+-+-+-+-------+ |F|R|R|R|
     * opcode| |I|S|S|S|  (4)  | |N|V|V|V|       | | |1|2|3|       |
     *
     * @param rsv Can only be {0, 1, 2, 3}
     * @return byte that represents which RSV bit is set.
     */
    private byte getRSVByte(int rsv) {
        switch (rsv) {
            case 1: // 0100 0000
                return 0x40;
            case 2: // 0010 0000
                return 0x20;
            case 3: // 0001 0000
                return 0x10;
            default:
                return 0;
        }
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

    public List<Framedata> translateFrame(ByteBuffer buffer) throws SocketException {
        while (true) {
            List<Framedata> frames = new LinkedList<>();
            Framedata cur;
            if (incompleteframe != null) {
                // complete an incomplete frame
                try {
                    buffer.mark();
                    int availableNextByteCount = buffer.remaining();// The number of bytes received
                    int expectedNextByteCount = incompleteframe
                            .remaining();// The number of bytes to complete the incomplete frame

                    if (expectedNextByteCount > availableNextByteCount) {
                        // did not receive enough bytes to complete the frame
                        incompleteframe.put(buffer.array(), buffer.position(), availableNextByteCount);
                        buffer.position(buffer.position() + availableNextByteCount);
                        return Collections.emptyList();
                    }
                    incompleteframe.put(buffer.array(), buffer.position(), expectedNextByteCount);
                    buffer.position(buffer.position() + expectedNextByteCount);
                    cur = translateSingleFrame(incompleteframe.duplicate().position(0));
                    frames.add(cur);
                    incompleteframe = null;
                } catch (SocketException e) {
                    // extending as much as suggested
                    ByteBuffer extendedframe = ByteBuffer.allocate(checkAlloc(e.getValue()));
                    assert (extendedframe.limit() > incompleteframe.limit());
                    incompleteframe.rewind();
                    extendedframe.put(incompleteframe);
                    incompleteframe = extendedframe;
                    continue;
                }
            }

            // Read as much as possible full frames
            while (buffer.hasRemaining()) {
                buffer.mark();
                try {
                    cur = translateSingleFrame(buffer);
                    frames.add(cur);
                } catch (SocketException e) {
                    // remember the incomplete data
                    buffer.reset();
                    int pref = e.getValue();
                    incompleteframe = ByteBuffer.allocate(checkAlloc(pref));
                    incompleteframe.put(buffer);
                    break;
                }
            }
            return frames;
        }
    }

    public List<Framedata> createFrames(String text) {
        Framedata curframe = new Framedata(Builder.Opcode.TEXT);
        curframe.setPayload(ByteBuffer.wrap(text.getBytes(Charset.UTF_8)));
        try {
            curframe.isValid();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        return Collections.singletonList(curframe);
    }

    public void reset() {
        incompleteframe = null;
        negotiatedExtension = "";
        socketProtocol = null;
    }

    /**
     * Generate a final key from a input string
     *
     * @param in the input string
     * @return a final key
     */
    private String generateFinalKey(String in) {
        String seckey = in.trim();
        String acc = seckey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        MessageDigest sh1;
        try {
            sh1 = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
        return Base64.encode(sh1.digest(acc.getBytes()));
    }

    private byte[] toByteArray(long val, int bytecount) {
        byte[] buffer = new byte[bytecount];
        int highest = 8 * bytecount - 8;
        for (int i = 0; i < bytecount; i++) {
            buffer[i] = (byte) (val >>> (highest - 8 * i));
        }
        return buffer;
    }

    private byte fromOpcode(Builder.Opcode opcode) {
        if (opcode == Builder.Opcode.CONTINUOUS) {
            return 0;
        } else if (opcode == Builder.Opcode.TEXT) {
            return 1;
        } else if (opcode == Builder.Opcode.BINARY) {
            return 2;
        } else if (opcode == Builder.Opcode.CLOSING) {
            return 8;
        } else if (opcode == Builder.Opcode.PING) {
            return 9;
        } else if (opcode == Builder.Opcode.PONG) {
            return 10;
        }
        throw new IllegalArgumentException("Don't know how to handle " + opcode.toString());
    }

    private Builder.Opcode toOpcode(byte opcode) throws SocketException {
        switch (opcode) {
            case 0:
                return Builder.Opcode.CONTINUOUS;
            case 1:
                return Builder.Opcode.TEXT;
            case 2:
                return Builder.Opcode.BINARY;
            // 3-7 are not yet defined
            case 8:
                return Builder.Opcode.CLOSING;
            case 9:
                return Builder.Opcode.PING;
            case 10:
                return Builder.Opcode.PONG;
            // 11-15 are not yet defined
            default:
                throw new SocketException(Framedata.PROTOCOL_ERROR, "Unknown opcode " + (short) opcode);
        }
    }

    public void processFrame(SocketBuilder socketBuilder, Framedata frame)
            throws SocketException {
        Builder.Opcode curop = frame.getOpcode();
        if (curop == Builder.Opcode.TEXT) {
            processFrameText(socketBuilder, frame);
        } else {
            Logger.error("non control or continious frame expected");
            throw new SocketException(Framedata.PROTOCOL_ERROR,
                    "non control or continious frame expected");
        }
    }

    /**
     * Process the frame if it is a text frame
     *
     * @param socketBuilder the websocket impl
     * @param frame         the frame
     */
    private void processFrameText(SocketBuilder socketBuilder, Framedata frame)
            throws SocketException {
        try {
            socketBuilder.getWebSocketListener()
                    .onWebsocketMessage(socketBuilder, stringUtf8(frame.getPayloadData()));
        } catch (RuntimeException e) {
            Logger.error("Runtime exception during onWebsocketMessage {} ", e.getMessage());
            socketBuilder.getWebSocketListener().onWebsocketError(socketBuilder, e);
        }
    }

    public Builder.CloseHandshakeType getCloseHandshakeType() {
        return Builder.CloseHandshakeType.TWOWAY;
    }

    private class TranslatedPayloadMetaData {

        private final int payloadLength;
        private final int realPackageSize;

        TranslatedPayloadMetaData(int newPayloadLength, int newRealPackageSize) {
            this.payloadLength = newPayloadLength;
            this.realPackageSize = newRealPackageSize;
        }

        private int getPayloadLength() {
            return payloadLength;
        }

        private int getRealPackageSize() {
            return realPackageSize;
        }
    }

}
