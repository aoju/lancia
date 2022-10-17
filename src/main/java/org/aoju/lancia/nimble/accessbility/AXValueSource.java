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
package org.aoju.lancia.nimble.accessbility;

/**
 * A single source for a computed AX property.
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class AXValueSource {
    /**
     * What type of source this is.
     * "attribute"|"implicit"|"style"|"contents"|"placeholder"|"relatedElement";
     */
    private String type;
    /**
     * The value of this property source.
     */
    private AXNode value;
    /**
     * The name of the relevant attribute, if any.
     */
    private String attribute;
    /**
     * The value of the relevant attribute, if any.
     */
    private AXValue attributeValue;
    /**
     * Whether this source is superseded by a higher priority source.
     */
    private boolean superseded;
    /**
     * The native markup source for this value, e.g. a <label> element.
     * "figcaption"|"label"|"labelfor"|"labelwrapped"|"legend"|"tablecaption"|"title"|"other";
     */
    private String nativeSource;
    /**
     * The value, such as a node or node list, of the native source.
     */
    private AXValue nativeSourceValue;
    /**
     * Whether the value for this property is invalid.
     */
    private boolean invalid;
    /**
     * Reason for the value being invalid, if it is.
     */
    private String invalidReason;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public AXNode getValue() {
        return value;
    }

    public void setValue(AXNode value) {
        this.value = value;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public AXValue getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(AXValue attributeValue) {
        this.attributeValue = attributeValue;
    }

    public boolean getSuperseded() {
        return superseded;
    }

    public void setSuperseded(boolean superseded) {
        this.superseded = superseded;
    }

    public String getNativeSource() {
        return nativeSource;
    }

    public void setNativeSource(String nativeSource) {
        this.nativeSource = nativeSource;
    }

    public AXValue getNativeSourceValue() {
        return nativeSourceValue;
    }

    public void setNativeSourceValue(AXValue nativeSourceValue) {
        this.nativeSourceValue = nativeSourceValue;
    }

    public boolean getInvalid() {
        return invalid;
    }

    public void setInvalid(boolean invalid) {
        this.invalid = invalid;
    }

    public String getInvalidReason() {
        return invalidReason;
    }

    public void setInvalidReason(String invalidReason) {
        this.invalidReason = invalidReason;
    }
}
