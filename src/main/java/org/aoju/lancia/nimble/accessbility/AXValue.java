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

import java.util.List;

/**
 * A single computed AX property.
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class AXValue {
    /**
     * The type of this value.
     * Enum of possible property types.
     * "boolean"|"tristate"|"booleanOrUndefined"|"idref"|"idrefList"|"integer"|"node"|"nodeList"|"number"|"string"|"computedString"|"token"|"tokenList"|"domRelation"|"role"|"internalRole"|"valueUndefined";
     */
    private String type;
    /**
     * The computed value of this property.
     */
    private Object value;
    /**
     * One or more related nodes, if applicable.
     */
    private List<AXRelatedNode> relatedNodes;
    /**
     * The sources which contributed to the computation of this property.
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
