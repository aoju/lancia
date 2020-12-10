package org.aoju.lancia.socket;

import java.util.regex.Pattern;

/**
 * Class which represents the protocol used as Sec-WebSocket-Protocol
 *
 * @since 1.3.7
 */
public class Protocol {

    private static final Pattern patternSpace = Pattern.compile(" ");
    private static final Pattern patternComma = Pattern.compile(",");

    /**
     * Attribute for the provided protocol
     */
    private final String providedProtocol;

    /**
     * Constructor for a Sec-Websocket-Protocol
     *
     * @param providedProtocol the protocol string
     */
    public Protocol(String providedProtocol) {
        if (providedProtocol == null) {
            throw new IllegalArgumentException();
        }
        this.providedProtocol = providedProtocol;
    }

    public boolean acceptProvidedProtocol(String inputProtocolHeader) {
        String protocolHeader = patternSpace.matcher(inputProtocolHeader).replaceAll("");
        String[] headers = patternComma.split(protocolHeader);
        for (String header : headers) {
            if (providedProtocol.equals(header)) {
                return true;
            }
        }
        return false;
    }

    public String getProvidedProtocol() {
        return this.providedProtocol;
    }

    public Protocol copyInstance() {
        return new Protocol(getProvidedProtocol());
    }

    @Override
    public String toString() {
        return getProvidedProtocol();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Protocol protocol = (Protocol) o;

        return providedProtocol.equals(protocol.providedProtocol);
    }

    @Override
    public int hashCode() {
        return providedProtocol.hashCode();
    }

}
