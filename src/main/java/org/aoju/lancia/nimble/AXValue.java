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

import java.util.List;

/**
 * 一个单独的计算AX属性
 *
 * @author Kimi Liu
 * @version 1.2.1
 * @since JDK 1.8+
 */
public class AXValue {
    /**
     * 此值的类型
     * "boolean"|"tristate"|"booleanOrUndefined"|"idref"|"idrefList"|"integer"|"node"|"nodeList"|"number"|"string"|"computedString"|"token"|"tokenList"|"domRelation"|"role"|"internalRole"|"valueUndefined";
     */
    private String type;
    /**
     * 此属性的计算值.
     */
    private Object value;
    /**
     * 一个或多个相关节点
     */
    private List<AXRelatedNode> relatedNodes;
    /**
     * 用于计算此属性的源
     */
    private List<AXValueSource> sources;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public List<AXRelatedNode> getRelatedNodes() {
        return relatedNodes;
    }

    public void setRelatedNodes(List<AXRelatedNode> relatedNodes) {
        this.relatedNodes = relatedNodes;
    }

    public List<AXValueSource> getSources() {
        return sources;
    }

    public void setSources(List<AXValueSource> sources) {
        this.sources = sources;
    }

}
