package org.aoju.lancia.socket;

import org.aoju.bus.core.lang.exception.InstrumentException;

import java.nio.ByteBuffer;

/**
 * Abstract implementation of a frame
 */
public class Framedata {

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


    public Framedata() {
        unmaskedpayload = ByteBuffer.allocate(0);
        fin = true;
        rsv1 = false;
        rsv2 = false;
        rsv3 = false;
    }


    public void isValid() {
        if (!isFin()) {
            throw new InstrumentException("Control frame cant have fin==false set");
        }
        if (isRSV1()) {
            throw new InstrumentException("Control frame cant have rsv1==true set");
        }
        if (isRSV2()) {
            throw new InstrumentException("Control frame cant have rsv2==true set");
        }
        if (isRSV3()) {
            throw new InstrumentException("Control frame cant have rsv3==true set");
        }
    }

    public boolean isRSV1() {
        return rsv1;
    }

    /**
     * Set the rsv1 of this frame to the provided boolean
     *
     * @param rsv1 true if fin has to be set
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
     * @param rsv2 true if fin has to be set
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
     * @param rsv3 true if fin has to be set
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

    public ByteBuffer getPayloadData() {
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
