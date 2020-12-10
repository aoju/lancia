package org.aoju.lancia.socket;

import java.util.Collections;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * Implementation of a handshake builder
 */
public class HandshakeBuilder {

    /**
     * Attribute for the http fields and values
     */
    private final TreeMap<String, String> map;
    /**
     * Attribute for the content of the handshake
     */
    private byte[] content;

    /**
     * Attribute for the http status
     */
    private short httpstatus;

    /**
     * Attribute for the http status message
     */
    private String httpstatusmessage;

    /**
     * Attribute for the resource descriptor
     */
    private String resourceDescriptor = "*";

    /**
     * Constructor for handshake implementation
     */
    public HandshakeBuilder() {
        map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    }

    public Iterator<String> iterateHttpFields() {
        return Collections.unmodifiableSet(map.keySet()).iterator();// Safety first
    }

    public String getFieldValue(String name) {
        String s = map.get(name);
        if (s == null) {
            return "";
        }
        return s;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void put(String name, String value) {
        map.put(name, value);
    }

    public String getResourceDescriptor() {
        return resourceDescriptor;
    }

    public void setResourceDescriptor(String resourceDescriptor) {
        if (resourceDescriptor == null)
            throw new IllegalArgumentException("http resource descriptor must not be null");
        this.resourceDescriptor = resourceDescriptor;
    }

    public String getHttpStatusMessage() {
        return httpstatusmessage;
    }

    public void setHttpStatusMessage(String message) {
        this.httpstatusmessage = message;
    }

    public short getHttpStatus() {
        return httpstatus;
    }

    public void setHttpStatus(short status) {
        httpstatus = status;
    }

    public boolean hasFieldValue(String name) {
        return map.containsKey(name);
    }

}
