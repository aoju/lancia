package org.aoju.lancia.socket;

import org.aoju.bus.core.lang.exception.InstrumentException;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * websocket规范中不常见的所有东西的基类，比如读取握手或传输帧的方式
 */
public abstract class Draft {

    protected HandshakeState.Opcode continuousFrameType = null;

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
        // ensure that there wont be any bytes skipped
        buf.position(buf.position() - sbuf.position());
        return null;
    }

    public static String readStringLine(ByteBuffer buf) {
        ByteBuffer b = readLine(buf);
        return b == null ? null : Base64.stringAscii(b.array(), 0, b.limit());
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
        // translating/parsing the response from the SERVER
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

    public abstract HandshakeState acceptHandshakeAsClient(HandshakeBuilder request, HandshakeBuilder response) throws InstrumentException;

    protected boolean basicAccept(HandshakeBuilder HandshakeBuilder) {
        return HandshakeBuilder.getFieldValue("Upgrade").equalsIgnoreCase("websocket") && HandshakeBuilder.getFieldValue("Connection").toLowerCase(Locale.ENGLISH).contains("upgrade");
    }

    public abstract ByteBuffer createBinaryFrame(Framedata framedata);

    public abstract List<Framedata> createFrames(ByteBuffer binary, boolean mask);

    public abstract List<Framedata> createFrames(String text, boolean mask);


    /**
     * Handle the frame specific to the draft
     *
     * @param webSocketImpl the websocketimpl used for this draft
     * @param frame         the frame which is supposed to be handled
     * @throws InstrumentException will be thrown on invalid data
     */
    public abstract void processFrame(WebSocketImpl webSocketImpl, Framedata frame) throws InstrumentException, InvalidDataException;

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
        try {
            bui.isValid();
        } catch (InstrumentException | InvalidDataException e) {
            throw new IllegalArgumentException(e); // can only happen when one builds close frames(Opcode.Close)
        }
        if (fin) {
            continuousFrameType = null;
        } else {
            continuousFrameType = op;
        }
        return Collections.singletonList(bui);
    }

    public abstract void reset();

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

    public abstract HandshakeBuilder postProcessHandshakeRequestAsClient(HandshakeBuilder request) throws InstrumentException;

    public abstract List<Framedata> translateFrame(ByteBuffer buffer) throws InstrumentException, InvalidDataException;

    public abstract HandshakeState.CloseHandshakeType getCloseHandshakeType();

    /**
     * Drafts must only be by one websocket at all. To prevent drafts to be used more than once the Websocket implementation should call this method in order to create a new usable version of a given draft instance.<br>
     * The copy can be safely used in conjunction with a new websocket connection.
     *
     * @return a copy of the draft
     */
    public abstract Draft copyInstance();

    public HandshakeBuilder translateHandshake(ByteBuffer buf) throws InstrumentException {
        return translateHandshakeHttp(buf);
    }

    public int checkAlloc(int bytecount) throws InstrumentException {
        if (bytecount < 0)
            throw new InstrumentException("Negative count:" + Framedata.PROTOCOL_ERROR);
        return bytecount;
    }

    int readVersion(HandshakeBuilder HandshakeBuilder) {
        String vers = HandshakeBuilder.getFieldValue("Sec-WebSocket-Version");
        if (vers.length() > 0) {
            int v;
            try {
                v = new Integer(vers.trim());
                return v;
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    public String toString() {
        return getClass().getSimpleName();
    }

}
