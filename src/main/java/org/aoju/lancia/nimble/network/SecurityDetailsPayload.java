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
package org.aoju.lancia.nimble.network;

import java.util.List;

/**
 * Security details about a request.
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class SecurityDetailsPayload {
    /**
     * Protocol name (e.g. "TLS 1.2" or "QUIC").
     */
    private String protocol;
    /**
     * Key Exchange used by the connection, or the empty string if not applicable.
     */
    private String keyExchange;
    /**
     * (EC)DH group used by the connection, if applicable.
     */
    private String keyExchangeGroup;
    /**
     * Cipher name.
     */
    private String cipher;
    /**
     * TLS MAC. Note that AEAD ciphers do not have separate MACs.
     */
    private String mac;
    /**
     * Certificate ID value.
     */
    private int certificateId;
    /**
     * Certificate subject name.
     */
    private String subjectName;
    /**
     * Subject Alternative Name (SAN) DNS names and IP addresses.
     */
    private List<String> sanList;
    /**
     * Name of the issuing CA.
     */
    private String issuer;
    /**
     * Certificate valid from date.
     */
    private Double validFrom;
    /**
     * Certificate valid to (expiration) date
     */
    private Double validTo;
    /**
     * List of signed certificate timestamps (SCTs).
     */
    private List<SignedCertificateTimestamp> signedCertificateTimestampList;
    /**
     * Whether the request complied with Certificate Transparency policy
     */
    private String certificateTransparencyCompliance;

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getKeyExchange() {
        return keyExchange;
    }

    public void setKeyExchange(String keyExchange) {
        this.keyExchange = keyExchange;
    }

    public String getKeyExchangeGroup() {
        return keyExchangeGroup;
    }

    public void setKeyExchangeGroup(String keyExchangeGroup) {
        this.keyExchangeGroup = keyExchangeGroup;
    }

    public String getCipher() {
        return cipher;
    }

    public void setCipher(String cipher) {
        this.cipher = cipher;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public int getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(int certificateId) {
        this.certificateId = certificateId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public List<String> getSanList() {
        return sanList;
    }

    public void setSanList(List<String> sanList) {
        this.sanList = sanList;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public Double getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Double validFrom) {
        this.validFrom = validFrom;
    }

    public Double getValidTo() {
        return validTo;
    }

    public void setValidTo(Double validTo) {
        this.validTo = validTo;
    }

    public List<SignedCertificateTimestamp> getSignedCertificateTimestampList() {
        return signedCertificateTimestampList;
    }

    public void setSignedCertificateTimestampList(List<SignedCertificateTimestamp> signedCertificateTimestampList) {
        this.signedCertificateTimestampList = signedCertificateTimestampList;
    }

    public String getCertificateTransparencyCompliance() {
        return certificateTransparencyCompliance;
    }

    public void setCertificateTransparencyCompliance(String certificateTransparencyCompliance) {
        this.certificateTransparencyCompliance = certificateTransparencyCompliance;
    }
}
