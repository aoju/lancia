package org.aoju.lancia.socket;

/**
 * Enum which represents the states a handshake may be in
 */
public enum HandshakeState {
    /**
     * Handshake matched this Draft successfully
     */
    MATCHED,
    /**
     * Handshake is does not match this Draft
     */
    NOT_MATCHED;

    /**
     * Enum which contains the different valid opcodes
     */
    public enum Opcode {
        CONTINUOUS, TEXT, BINARY, PING, PONG, CLOSING
        // more to come
    }

    /**
     * Enum which represents the state a websocket may be in
     */
    public enum ReadyState {
        NOT_YET_CONNECTED, OPEN, CLOSING, CLOSED
    }

    /**
     * Enum which represents the states a websocket may be in
     */
    public enum Role {
        CLIENT, SERVER
    }

    /**
     * Enum which represents type of handshake is required for a close
     */
    public enum CloseHandshakeType {
        NONE, ONEWAY, TWOWAY
    }
}