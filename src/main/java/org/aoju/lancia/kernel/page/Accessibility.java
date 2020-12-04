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
package org.aoju.lancia.kernel.page;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.toolkit.CollKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.lancia.Variables;
import org.aoju.lancia.nimble.SerializedAXNode;
import org.aoju.lancia.worker.CDPSession;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @author Kimi Liu
 * @version 6.1.3
 * @since JDK 1.8+
 */
public class Accessibility {

    private final CDPSession client;

    public Accessibility(CDPSession client) {
        this.client = client;
    }

    public SerializedAXNode snapshot(boolean interestingOnly, ElementHandle root) throws JsonProcessingException, IllegalAccessException, IntrospectionException, InvocationTargetException {

        JsonNode nodes = this.client.send("Accessibility.getFullAXTree", null, false);
        String backendNodeId = null;
        if (root != null) {
            Map<String, Object> params = new HashMap<>();
            params.put("objectId", root.getRemoteObject().getObjectId());
            JsonNode node = this.client.send("DOM.describeNode", params, true);
            backendNodeId = node.get("backendNodeId").asText();
        }
        Iterator<JsonNode> elements = nodes.elements();
        List<org.aoju.lancia.nimble.AXNode> payloads = new ArrayList<>();
        while (elements.hasNext()) {
            payloads.add(Variables.OBJECTMAPPER.treeToValue(elements.next(), org.aoju.lancia.nimble.AXNode.class));
        }
        AXNode defaultRoot = AXNode.createTree(payloads);
        AXNode needle = defaultRoot;
        if (StringKit.isNotEmpty(backendNodeId)) {
            String finalBackendNodeId = backendNodeId;
            needle = defaultRoot.find(node -> finalBackendNodeId.equals(node.getPayload().getBackendDOMNodeId() + Normal.EMPTY));
            if (needle == null)
                return null;
        }
        if (!interestingOnly)
            return serializeTree(needle, null).get(0);

        Set<AXNode> interestingNodes = new HashSet<>();
        collectInterestingNodes(interestingNodes, defaultRoot, false);
        if (!interestingNodes.contains(needle))
            return null;
        return serializeTree(needle, interestingNodes).get(0);
    }

    private void collectInterestingNodes(Set<AXNode> collection, AXNode node, boolean insideControl) {
        if (node.isInteresting(insideControl))
            collection.add(node);
        if (node.isLeafNode())
            return;
        insideControl = insideControl || node.isControl();
        for (AXNode child :
                node.getChildren())
            collectInterestingNodes(collection, child, insideControl);
    }

    public List<SerializedAXNode> serializeTree(AXNode node, Set<AXNode> whitelistedNodes) throws IllegalAccessException, IntrospectionException, InvocationTargetException {
        List<SerializedAXNode> children = new ArrayList<>();
        for (AXNode child : node.getChildren())
            children.addAll(serializeTree(child, whitelistedNodes));

        if (CollKit.isNotEmpty(whitelistedNodes) && !whitelistedNodes.contains(node))
            return children;

        SerializedAXNode serializedNode = node.serialize();
        if (CollKit.isNotEmpty(children))
            serializedNode.setChildren(children);
        List<SerializedAXNode> result = new ArrayList<>();
        result.add(serializedNode);
        return result;
    }

}
