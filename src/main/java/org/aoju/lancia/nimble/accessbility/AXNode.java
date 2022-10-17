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
 * A node in the accessibility tree.
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class AXNode {

    /**
     * Unique identifier for this node.
     */
    private String nodeId;
    /**
     * Whether this node is ignored for accessibility
     */
    private boolean ignored;
    /**
     * Collection of reasons why this node is hidden.
     */
    private List<AXProperty> ignoredReasons;
    /**
     * This `Node`'s role, whether explicit or implicit.
     */
    private AXValue role;
    /**
     * The accessible name for this `Node`.
     */
    private AXValue name;
    /**
     * The accessible description for this `Node`.
     */
    private AXValue description;
    /**
     * The value for this `Node`.
     */
    private AXValue value;
    /**
     * All other properties
     */
    private List<AXProperty> properties;
    /**
     * IDs for each of this node's child nodes.
     */
    private List<String> childIds;
    /**
     * The backend ID for the associated DOM node, if any.
     */
    private int backendDOMNodeId;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public boolean getIgnored() {
        return ignored;
    }

    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
    }

    public List<AXProperty> getIgnoredReasons() {
        return ignoredReasons;
    }

    public void setIgnoredReasons(List<AXProperty> ignoredReasons) {
        this.ignoredReasons = ignoredReasons;
    }

    public AXValue getRole() {
        return role;
    }

    public void setRole(AXValue role) {
        this.role = role;
    }

    public AXValue getName() {
        return name;
    }

    public void setName(AXValue name) {
        this.name = name;
    }

    public AXValue getDescription() {
        return description;
    }

    public void setDescription(AXValue description) {
        this.description = description;
    }

    public AXValue getValue() {
        return value;
    }

    public void setValue(AXValue value) {
        this.value = value;
    }

    public List<AXProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<AXProperty> properties) {
        this.properties = properties;
    }

    public List<String> getChildIds() {
        return childIds;
    }

    public void setChildIds(List<String> childIds) {
        this.childIds = childIds;
    }

    public int getBackendDOMNodeId() {
        return backendDOMNodeId;
    }

    public void setBackendDOMNodeId(int backendDOMNodeId) {
        this.backendDOMNodeId = backendDOMNodeId;
    }
}
