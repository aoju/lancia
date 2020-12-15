package org.aoju.lancia.socket;

import java.nio.ByteBuffer;

/**
 * Abstract implementation of a frame
 */
public class Framedata {

    /**
     * The unmasked "Payload data" which was sent in this frame
     */
    private ByteBuffer byteBuffer;

    public Framedata() {
        byteBuffer = ByteBuffer.allocate(0);
    }

    public ByteBuffer getPayload() {
        return byteBuffer;
    }

    /**
     * Set the payload of this frame to the provided payload
     *
     * @param payload the payload which is to set
     */
    public void setPayload(ByteBuffer payload) {
        this.byteBuffer = payload;
    }

}
