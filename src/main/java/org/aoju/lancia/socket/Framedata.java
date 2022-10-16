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

import org.aoju.bus.core.lang.Charset;
import org.aoju.lancia.Builder;
import org.aoju.lancia.worker.exception.SocketException;

import java.nio.ByteBuffer;

/**
 * The interface for the frame
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class Framedata {

    /**
     * indicates a normal closure, meaning whatever purpose the connection was established for has
     * been fulfilled.
     */
    public static final int NORMAL = 1000;
    /**
     * 1001 indicates that an endpoint is "going away", such as a server going down, or a browser
     * having navigated away from a page.
     */
    public static final int GOING_AWAY = 1001;
    /**
     * 1002 indicates that an endpoint is terminating the connection due to a protocol error.
     */
    public static final int PROTOCOL_ERROR = 1002;
    /**
     * 1003 indicates that an endpoint is terminating the connection because it has received a type of
     * data it cannot accept (e.g. an endpoint that understands only text data MAY send this if it
     * receives a binary message).
     */
    public static final int REFUSE = 1003;
    /*1004: Reserved. The specific meaning might be defined in the future.*/
    /**
     * 1005 is a reserved value and MUST NOT be set as a status code in a Close control frame by an
     * endpoint. It is designated for use in applications expecting a status code to indicate that no
     * status code was actually present.
     */
    public static final int NOCODE = 1005;
    /**
     * 1006 is a reserved value and MUST NOT be set as a status code in a Close control frame by an
     * endpoint. It is designated for use in applications expecting a status code to indicate that the
     * connection was closed abnormally, e.g. without sending or receiving a Close control frame.
     */
    public static final int ABNORMAL_CLOSE = 1006;
    /**
     * 1007 indicates that an endpoint is terminating the connection because it has received data
     * within a message that was not consistent with the type of the message (e.g., non-UTF-8
     * [RFC3629] data within a text message).
     */
    public static final int NO_UTF8 = 1007;
    /**
     * 1008 indicates that an endpoint is terminating the connection because it has received a message
     * that violates its policy. This is a generic status code that can be returned when there is no
     * other more suitable status code (e.g. 1003 or 1009), or if there is a need to hide specific
     * details about the policy.
     */
    public static final int POLICY_VALIDATION = 1008;
    /**
     * 1009 indicates that an endpoint is terminating the connection because it has received a message
     * which is too big for it to process.
     */
    public static final int TOOBIG = 1009;
    /**
     * 1010 indicates that an endpoint (client) is terminating the connection because it has expected
     * the server to negotiate one or more extension, but the server didn't return them in the
     * response message of the WebSocket handshake. The list of extensions which are needed SHOULD
     * appear in the /reason/ part of the Close frame. Note that this status code is not used by the
     * server, because it can fail the WebSocket handshake instead.
     */
    public static final int EXTENSION = 1010;
    /**
     * 1011 indicates that a server is terminating the connection because it encountered an unexpected
     * condition that prevented it from fulfilling the request.
     **/
    public static final int UNEXPECTED_CONDITION = 1011;
    /**
     * 1012 indicates that the service is restarted. A client may reconnect, and if it choses to do,
     * should reconnect using a randomized delay of 5 - 30s. See https://www.ietf.org/mail-archive/web/hybi/current/msg09670.html
     * for more information.
     **/
    public static final int SERVICE_RESTART = 1012;
    /**
     * 1013 indicates that the service is experiencing overload. A client should only connect to a
     * different IP (when there are multiple for the target) or reconnect to the same IP upon user
     * action. See https://www.ietf.org/mail-archive/web/hybi/current/msg09670.html for more
     * information.
     **/
    public static final int TRY_AGAIN_LATER = 1013;
    /**
     * 1014 indicates that the server was acting as a gateway or proxy and received an invalid
     * response from the upstream server. This is similar to 502 HTTP Status Code See
     * https://www.ietf.org/mail-archive/web/hybi/current/msg10748.html fore more information.
     **/
    public static final int BAD_GATEWAY = 1014;
    /**
     * 1015 is a reserved value and MUST NOT be set as a status code in a Close control frame by an
     * endpoint. It is designated for use in applications expecting a status code to indicate that the
     * connection was closed due to a failure to perform a TLS handshake (e.g., the server certificate
     * can't be verified).
     **/
    public static final int TLS_ERROR = 1015;

    /**
     * The connection had never been established
     */
    public static final int NEVER_CONNECTED = -1;

    /**
     * The connection had a buggy close (this should not happen)
     */
    public static final int BUGGYCLOSE = -2;

    /**
     * The connection was flushed and closed
     */
    public static final int FLASHPOLICY = -3;
    /**
     * Defines the interpretation of the "Payload data".
     */
    private final Builder.Opcode optcode;
    /**
     * Indicates that this is the final fragment in a message.
     */
    private boolean fin;
    /**
     * The unmasked "Payload data" which was sent in this frame
     */
    private ByteBuffer unmaskedpayload;

    /**
     * Indicates that the rsv1 bit is set or not
     */
    private boolean rsv1;

    /**
     * Indicates that the rsv2 bit is set or not
     */
    private boolean rsv2;

    /**
     * Indicates that the rsv3 bit is set or not
     */
    private boolean rsv3;

    /**
     * The close code used in this close frame
     */
    private int code;

    /**
     * The close message used in this close frame
     */
    private String reason;


    /**
     * Constructor for a FramedataImpl without any attributes set apart from the opcode
     *
     * @param op the opcode to use
     */
    public Framedata(Builder.Opcode op) {
        optcode = op;
        unmaskedpayload = ByteBuffer.allocate(0);
        fin = true;
        rsv1 = false;
        rsv2 = false;
        rsv3 = false;
    }

    /**
     * Get a frame with a specific opcode
     *
     * @param opcode the opcode representing the frame
     * @return the frame with a specific opcode
     */
    public static Framedata get(Builder.Opcode opcode) {
        if (opcode == null) {
            throw new IllegalArgumentException("Supplied opcode cannot be null");
        }
        if (opcode == Builder.Opcode.TEXT) {
            return new Framedata(Builder.Opcode.TEXT);
        }
        throw new IllegalArgumentException("Supplied opcode is invalid");
    }

    /**
     * Set the close code for this close frame
     *
     * @param code the close code
     */
    public void setCode(int code) {
        this.code = code;
        // Framedata.TLS_ERROR is not allowed to be transferred over the wire
        if (code == Framedata.TLS_ERROR) {
            this.code = Framedata.NOCODE;
            this.reason = "";
        }
        updatePayload();
    }

    /**
     * Set the close reason for this close frame
     *
     * @param reason the reason code
     */
    public void setReason(String reason) {
        if (reason == null) {
            reason = "";
        }
        this.reason = reason;
        updatePayload();
    }

    /**
     * Update the payload to represent the close code and the reason
     */
    private void updatePayload() {
        byte[] by = reason.getBytes(Charset.UTF_8);
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.putInt(code);
        buf.position(2);
        ByteBuffer pay = ByteBuffer.allocate(2 + by.length);
        pay.put(buf);
        pay.put(by);
        pay.rewind();
        this.setPayload(pay);
    }

    /**
     * Check if the frame is valid due to specification
     *
     * @throws SocketException thrown if the frame is not a valid frame
     */
    public void isValid() throws SocketException {
        if (!isFin()) {
            throw new SocketException(Framedata.PROTOCOL_ERROR, "Control frame can't have fin==false set");
        }
        if (isRSV1()) {
            throw new SocketException(Framedata.PROTOCOL_ERROR, "Control frame can't have rsv1==true set");
        }
        if (isRSV2()) {
            throw new SocketException(Framedata.PROTOCOL_ERROR, "Control frame can't have rsv2==true set");
        }
        if (isRSV3()) {
            throw new SocketException(Framedata.PROTOCOL_ERROR, "Control frame can't have rsv3==true set");
        }
    }


    public boolean isRSV1() {
        return rsv1;
    }

    /**
     * Set the rsv1 of this frame to the provided boolean
     *
     * @param rsv1 true if rsv1 has to be set
     */
    public void setRSV1(boolean rsv1) {
        this.rsv1 = rsv1;
    }

    public boolean isRSV2() {
        return rsv2;
    }

    /**
     * Set the rsv2 of this frame to the provided boolean
     *
     * @param rsv2 true if rsv2 has to be set
     */
    public void setRSV2(boolean rsv2) {
        this.rsv2 = rsv2;
    }

    public boolean isRSV3() {
        return rsv3;
    }

    /**
     * Set the rsv3 of this frame to the provided boolean
     *
     * @param rsv3 true if rsv3 has to be set
     */
    public void setRSV3(boolean rsv3) {
        this.rsv3 = rsv3;
    }

    public boolean isFin() {
        return fin;
    }

    /**
     * Set the fin of this frame to the provided boolean
     *
     * @param fin true if fin has to be set
     */
    public void setFin(boolean fin) {
        this.fin = fin;
    }

    public Builder.Opcode getOpcode() {
        return optcode;
    }

    public ByteBuffer getPayloadData() {
        if (code == NOCODE) {
            return ByteBuffer.allocate(0);
        }
        return unmaskedpayload;
    }

    /**
     * Set the payload of this frame to the provided payload
     *
     * @param payload the payload which is to set
     */
    public void setPayload(ByteBuffer payload) {
        this.unmaskedpayload = payload;
    }

}
