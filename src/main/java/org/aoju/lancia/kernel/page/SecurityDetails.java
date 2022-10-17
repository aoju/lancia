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
package org.aoju.lancia.kernel.page;

import org.aoju.lancia.nimble.network.SecurityDetailsPayload;

/**
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class SecurityDetails {

    private String subjectName;

    private String issuer;

    private Double validFrom;

    private Double validTo;

    private String protocol;

    public SecurityDetails() {
    }

    public SecurityDetails(SecurityDetailsPayload securityDetails) {
        this.subjectName = securityDetails.getSubjectName();
        this.issuer = securityDetails.getIssuer();
        this.validFrom = securityDetails.getValidFrom();
        this.validTo = securityDetails.getValidTo();
        this.protocol = securityDetails.getProtocol();
    }

    public String subjectName() {
        return this.subjectName;
    }

    public String issuer() {
        return this.issuer;
    }

    public double validFrom() {
        return this.validFrom;
    }

    public double validTo() {
        return this.validTo;
    }

    public String protocol() {
        return this.protocol;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
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

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}
