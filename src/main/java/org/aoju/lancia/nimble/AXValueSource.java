/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
package org.aoju.lancia.nimble;

/**
 * 计算AX属性的单一源
 *
 * @author Kimi Liu
 * @version 1.2.1
 * @since JDK 1.8+
 */
public class AXValueSource {
    /**
     * 这是什么类型的来源
     * "attribute"|"implicit"|"style"|"contents"|"placeholder"|"relatedElement";
     */
    private String type;
    /**
     * 此属性的值为source
     */
    private AXNode value;
    /**
     * 相关属性的名称
     */
    private String attribute;
    /**
     * 相关属性的值
     */
    private AXValue attributeValue;
    /**
     * 此源是否被更高优先级的源取代.
     */
    private boolean superseded;
    /**
     * 此值的本机标记源
     * "figcaption"|"label"|"labelfor"|"labelwrapped"|"legend"|"tablecaption"|"title"|"other";
     */
    private String nativeSource;
    /**
     * 本机源的值，例如节点或节点列表
     */
    private AXValue nativeSourceValue;
    /**
     * 此属性的值是否无效
     */
    private boolean invalid;
    /**
     * 如果值是无效的，那么原因是什么
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
