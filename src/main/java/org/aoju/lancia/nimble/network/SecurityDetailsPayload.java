/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
 * 请求的安全细节
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class SecurityDetailsPayload {

    /**
     * 协议名称 (e.g. "TLS 1.2" or "QUIC").
     */
    private String protocol;
    /**
     * 连接使用的密钥交换，如果不适用，则使用空字符串
     */
    private String keyExchange;
    /**
     * (EC)连接使用的DH组
     */
    private String keyExchangeGroup;
    /**
     * 密码的名字
     */
    private String cipher;
    /**
     * 注意，AEAD密码没有单独的Macs
     */
    private String mac;
    /**
     * 证书ID值
     */
    private int certificateId;
    /**
     * 证书标题名称
     */
    private String subjectName;
    /**
     * 标题备选名称(SAN) DNS名称和IP地址
     */
    private List<String> sanList;
    /**
     * 签发CA的名称
     */
    private String issuer;
    /**
     * 证书生效日期
     */
    private Double validFrom;
    /**
     * 证书到期日期
     */
    private Double validTo;
    /**
     * 已签名的证书时间戳(SCTs)列表
     */
    private List<SignedCertificate> signedCertificateList;
    /**
     * 该请求是否符合证书透隐私策略
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

    public List<SignedCertificate> getSignedCertificateTimestampList() {
        return signedCertificateList;
    }

    public void setSignedCertificateTimestampList(List<SignedCertificate> signedCertificateList) {
        this.signedCertificateList = signedCertificateList;
    }

    public String getCertificateTransparencyCompliance() {
        return certificateTransparencyCompliance;
    }

    public void setCertificateTransparencyCompliance(String certificateTransparencyCompliance) {
        this.certificateTransparencyCompliance = certificateTransparencyCompliance;
    }

}
