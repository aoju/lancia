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
package org.aoju.bus.puppeteer.nimble;

import java.util.List;

/**
 * 可访问性树中的节点
 *
 * @author Kimi Liu
 * @version 6.1.3
 * @since JDK 1.8+
 */
public class AXNode {

    /**
     * 该节点的唯一标识符
     */
    private String nodeId;
    /**
     * 是否忽略此节点的可访问性
     */
    private boolean ignored;
    /**
     * 隐藏此节点的原因的集合
     */
    private List<AXProperty> ignoredReasons;
    /**
     * 这个节点的角色，无论是显式的还是隐式的
     */
    private AXValue role;
    /**
     * 此节点的可访问名称
     */
    private AXValue name;
    /**
     * 此节点的可访问性描述
     */
    private AXValue description;
    /**
     * 此节点的值
     */
    private AXValue value;
    /**
     * 所有其他属性
     */
    private List<AXProperty> properties;
    /**
     * 此节点的每个子节点的id
     */
    private List<String> childIds;
    /**
     * 关联DOM节点的后端ID
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
