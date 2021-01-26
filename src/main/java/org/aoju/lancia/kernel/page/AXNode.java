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
package org.aoju.lancia.kernel.page;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.toolkit.CollKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.lancia.nimble.AXProperty;
import org.aoju.lancia.nimble.SerializedAXNode;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @author Kimi Liu
 * @version 1.2.2
 * @since JDK 1.8+
 */
public class AXNode {

    public static final String[] TRISTATE_PROPERTIES = new String[]{
            "checked",
            "pressed"
    };
    public static final String[] TOKEN_PROPERTIES = new String[]{
            "autocomplete",
            "haspopup",
            "invalid",
            "orientation"
    };
    public static final String[] NUMERICAL_PROPERTIES = new String[]{
            "level",
            "valuemax",
            "valuemin"
    };
    private static final String[] USERSTRING_PROPERTIES = new String[]{
            "name",
            "value",
            "description",
            "keyshortcuts",
            "roledescription",
            "valuetext"
    };
    private static final String[] BOOLEAN_PROPERTIES = new String[]{"disabled",
            "expanded",
            "focused",
            "modal",
            "multiline",
            "multiselectable",
            "readonly",
            "required",
            "selected"
    };
    private org.aoju.lancia.nimble.AXNode payload;
    private List<AXNode> children = new ArrayList<>();
    private boolean richlyEditable = false;
    private boolean editable = false;
    private boolean focusable = false;
    private boolean expanded = false;
    private boolean hidden = false;
    private String name;
    private String role;
    private Boolean cachedHasFocusableChild;

    public AXNode() {
    }

    public AXNode(org.aoju.lancia.nimble.AXNode payload) {
        this.payload = payload;
        this.name = this.payload.getName() != null ? String.valueOf(this.payload.getName().getValue()) : Normal.EMPTY;
        this.role = this.payload.getRole() != null ? String.valueOf(this.payload.getRole().getValue()) : Normal.UNKNOWN;
        List<AXProperty> properties = this.payload.getProperties();
        if (CollKit.isNotEmpty(properties)) {
            for (AXProperty property : properties) {
                if ("editable".equals(property.getName())) {
                    this.richlyEditable = "richtext".equals(property.getValue().getValue());
                    this.editable = true;
                }
                if ("focusable".equals(property.getName()))
                    this.focusable = (Boolean) property.getValue().getValue();
                if ("expanded".equals(property.getName()))
                    this.expanded = (Boolean) property.getValue().getValue();
                if ("hidden".equals(property.getName()))
                    this.hidden = (Boolean) property.getValue().getValue();
            }
        }

    }

    public static AXNode createTree(List<org.aoju.lancia.nimble.AXNode> payloads) {
        Map<String, AXNode> nodeById = new HashMap<>();
        for (org.aoju.lancia.nimble.AXNode payload : payloads)
            nodeById.put(payload.getNodeId(), new AXNode(payload));
        for (AXNode node : nodeById.values()) {
            List<String> childIds = node.getPayload().getChildIds();
            if (CollKit.isNotEmpty(childIds)) {
                for (String childId : childIds) {
                    node.getChildren().add(nodeById.get(childId));
                }
            }

        }
        return nodeById.values().iterator().next();
    }

    private boolean isPlainTextField() {
        if (this.richlyEditable)
            return false;
        if (this.editable)
            return true;
        return "textbox".equals(this.role) || "ComboBox".equals(this.role) || "searchbox".equals(this.role);
    }

    private boolean isTextOnlyObject() {
        return ("LineBreak".equals(this.role) || "text".equals(this.role) || "InlineTextBox".equals(this.role));
    }

    private boolean hasFocusableChild() {
        if (this.cachedHasFocusableChild == null) {
            this.cachedHasFocusableChild = false;
            for (AXNode child : this.children) {
                if (child.getFocusable() || child.hasFocusableChild()) {
                    this.cachedHasFocusableChild = true;
                    break;
                }
            }
        }
        return this.cachedHasFocusableChild;
    }

    public AXNode find(Predicate<AXNode> predicate) {
        if (predicate.test(this))
            return this;
        for (AXNode child : this.children) {
            AXNode result = child.find(predicate);
            if (result != null)
                return result;
        }
        return null;
    }

    public boolean isLeafNode() {
        if (CollKit.isNotEmpty(this.children)) {
            return true;
        }

        if (this.isPlainTextField() || this.isTextOnlyObject()) {
            return true;
        }

        switch (this.role) {
            case "doc-cover":
            case "graphics-symbol":
            case "img":
            case "Meter":
            case "scrollbar":
            case "slider":
            case "separator":
            case "progressbar":
                return true;
            default:
                break;
        }

        if (this.hasFocusableChild())
            return false;
        if (this.focusable && StringKit.isNotEmpty(this.name))
            return true;
        return "heading".equals(this.role) && StringKit.isNotEmpty(this.name);
    }

    public boolean isControl() {
        switch (this.role) {
            case "button":
            case "checkbox":
            case "ColorWell":
            case "combobox":
            case "DisclosureTriangle":
            case "listbox":
            case "menu":
            case "menubar":
            case "menuitem":
            case "menuitemcheckbox":
            case "menuitemradio":
            case "radio":
            case "scrollbar":
            case "searchbox":
            case "slider":
            case "spinbutton":
            case "switch":
            case "tab":
            case "textbox":
            case "tree":
                return true;
            default:
                return false;
        }
    }

    public boolean isInteresting(boolean insideControl) {

        if ("Ignored".equals(this.role) || this.hidden) {
            return false;
        }

        if (this.focusable || this.richlyEditable) {
            return true;
        }

        if (this.isControl()) {
            return true;
        }

        if (insideControl) {
            return false;
        }

        return this.isLeafNode() && StringKit.isNotEmpty(this.name);
    }

    public SerializedAXNode serialize() throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        Map<String, Object> properties = new HashMap<>();
        List<AXProperty> properties1 = this.payload.getProperties();
        if (CollKit.isNotEmpty(properties1)) {
            for (AXProperty property : properties1)
                properties.put(property.getName().toLowerCase(), property.getValue().getValue());
        }

        if (this.payload.getName() != null) {

            properties.put("name", this.payload.getName().getValue());
        }
        if (this.payload.getValue() != null) {
            properties.put("value", this.payload.getValue().getValue());
        }

        if (this.payload.getDescription() != null) {
            properties.put("description", this.payload.getDescription().getValue());
        }

        SerializedAXNode node = new SerializedAXNode();
        node.setRole(this.role);

        for (String userStringProperty : USERSTRING_PROPERTIES) {
            if (!properties.containsKey(userStringProperty)) {
                continue;
            }
            PropertyDescriptor propDesc = new PropertyDescriptor(userStringProperty, SerializedAXNode.class);
            propDesc.getWriteMethod().invoke(node, properties.get(userStringProperty));
        }

        for (String booleanProperty : BOOLEAN_PROPERTIES) {
            if ("focused".equals(booleanProperty) && "WebArea".equals(this.role)) {
                continue;
            }
            boolean value = (Boolean) properties.get(booleanProperty);
            if (!value) {
                continue;
            }
            PropertyDescriptor propDesc = new PropertyDescriptor(booleanProperty, SerializedAXNode.class);
            propDesc.getWriteMethod().invoke(node, value);
        }

        for (String tristateProperty : TRISTATE_PROPERTIES) {
            if (!properties.containsKey(tristateProperty)) {
                continue;
            }
            PropertyDescriptor propDesc = new PropertyDescriptor(tristateProperty, SerializedAXNode.class);
            propDesc.getWriteMethod().invoke(node, properties.get(tristateProperty));
        }


        for (String numericalProperty : NUMERICAL_PROPERTIES) {
            if (!properties.containsKey(numericalProperty)) {
                continue;
            }
            PropertyDescriptor propDesc = new PropertyDescriptor(numericalProperty, SerializedAXNode.class);
            propDesc.getWriteMethod().invoke(node, properties.get(numericalProperty));
        }


        for (String tokenProperty : TOKEN_PROPERTIES) {
            Object value = properties.get(tokenProperty);

            if (value == null || "false".equals(value)) {
                continue;
            }
            PropertyDescriptor propDesc = new PropertyDescriptor(tokenProperty, SerializedAXNode.class);
            propDesc.getWriteMethod().invoke(node, value);
        }
        return node;
    }

    public org.aoju.lancia.nimble.AXNode getPayload() {
        return payload;
    }

    public void setPayload(org.aoju.lancia.nimble.AXNode payload) {
        this.payload = payload;
    }

    public List<AXNode> getChildren() {
        return children;
    }

    public void setChildren(List<AXNode> children) {
        this.children = children;
    }

    public boolean getRichlyEditable() {
        return richlyEditable;
    }

    public void setRichlyEditable(boolean richlyEditable) {
        this.richlyEditable = richlyEditable;
    }

    public boolean getEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean getFocusable() {
        return focusable;
    }

    public void setFocusable(boolean focusable) {
        this.focusable = focusable;
    }

    public boolean getExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public boolean getHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean getCachedHasFocusableChild() {
        return cachedHasFocusableChild;
    }

    public void setCachedHasFocusableChild(boolean cachedHasFocusableChild) {
        this.cachedHasFocusableChild = cachedHasFocusableChild;
    }

}
