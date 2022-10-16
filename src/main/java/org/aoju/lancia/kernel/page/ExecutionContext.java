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


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.toolkit.CollKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.lancia.Builder;
import org.aoju.lancia.nimble.PageEvaluateType;
import org.aoju.lancia.nimble.runtime.ExceptionDetails;
import org.aoju.lancia.nimble.runtime.ExecutionContextDescription;
import org.aoju.lancia.nimble.runtime.RemoteObject;
import org.aoju.lancia.worker.CDPSession;
import org.aoju.lancia.worker.exception.ProtocolException;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class ExecutionContext {

    public static final String EVALUATION_SCRIPT_URL = "__puppeteer_evaluation_script__";

    public static final Pattern SOURCE_URL_REGEX = Pattern.compile("^[\\040\\t]*//[@#] sourceURL=\\s*(\\S*?)\\s*$", Pattern.MULTILINE);
    private final int contextId;
    private CDPSession client;
    private DOMWorld world;

    public ExecutionContext(CDPSession client, ExecutionContextDescription contextPayload, DOMWorld world) {
        this.client = client;
        this.world = world;
        this.contextId = contextPayload.getId();
    }

    public Frame frame() {
        return this.world != null ? this.world.frame() : null;
    }

    public DOMWorld getWorld() {
        return world;
    }

    public void setWorld(DOMWorld world) {
        this.world = world;
    }

    public ElementHandle adoptElementHandle(ElementHandle elementHandle) {
        Assert.isTrue(elementHandle.executionContext() != this, "Cannot adopt handle that already belongs to this execution context");
        Assert.isTrue(this.world != null, "Cannot adopt handle without DOMWorld");
        Map<String, Object> params = new HashMap<>();
        params.put("objectId", elementHandle.getRemoteObject().getObjectId());
        JsonNode nodeInfo = this.client.send("DOM.describeNode", params, true);
        return this.adoptBackendNodeId(nodeInfo.get("node").get("backendNodeId").asInt());
    }

    public Object evaluateHandle(String pageFunction, List<Object> args) {
        return this.evaluateInternal(false, pageFunction, Builder.isFunction(pageFunction) ? PageEvaluateType.FUNCTION : PageEvaluateType.STRING, args);
    }

    public Object evaluate(String pageFunction, List<Object> args) {
        return this.evaluateInternal(true, pageFunction, Builder.isFunction(pageFunction) ? PageEvaluateType.FUNCTION : PageEvaluateType.STRING, args);
    }

    private Object evaluateInternal(boolean returnByValue, String pageFunction, PageEvaluateType type, List<Object> args) {
        String suffix = "//# sourceURL=" + ExecutionContext.EVALUATION_SCRIPT_URL;
        if (PageEvaluateType.STRING.equals(type)) {
            int contextId = this.contextId;
            String expression = pageFunction;
            String expressionWithSourceUrl = ExecutionContext.SOURCE_URL_REGEX.matcher(expression).find() ? expression : expression + "\n" + suffix;
            Map<String, Object> params = new HashMap<>();
            params.put("expression", expressionWithSourceUrl);
            params.put("contextId", contextId);
            params.put("returnByValue", returnByValue);
            params.put("awaitPromise", true);
            params.put("userGesture", true);
            JsonNode result = this.client.send("Runtime.evaluate", params, true);
            JsonNode exceptionDetails = result.get("exceptionDetails");
            try {
                if (exceptionDetails != null)
                    throw new RuntimeException("Evaluation failed: " + Builder.getExceptionMessage(org.aoju.lancia.Builder.OBJECTMAPPER.treeToValue(exceptionDetails, ExceptionDetails.class)));
                RemoteObject remoteObject = org.aoju.lancia.Builder.OBJECTMAPPER.treeToValue(result.get("result"), RemoteObject.class);
                return returnByValue ? Builder.valueFromRemoteObject(remoteObject) : createJSHandle(this, remoteObject);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        if (!PageEvaluateType.FUNCTION.equals(type))
            throw new IllegalArgumentException("Expected to get |string| or |function| as the first argument, but got " + type.name() + " instead.");
        String functionText = pageFunction;
        Map<String, Object> params = new HashMap<>();
        List<Object> argList = new ArrayList<>();
        if (CollKit.isNotEmpty(args)) {
            for (Object arg : args) {
                argList.add(convertArgument(this, arg));
            }
        }
        params.put("functionDeclaration", functionText + "\n" + suffix + "\n");
        params.put("executionContextId", this.contextId);
        params.put("arguments", argList);
        params.put("returnByValue", returnByValue);
        params.put("awaitPromise", true);
        params.put("userGesture", true);
        JsonNode callFunctionOnPromise;
        try {
            callFunctionOnPromise = this.client.send("Runtime.callFunctionOn", params, true);
        } catch (Exception e) {
            if (e.getMessage().startsWith("Converting circular structure to JSON"))
                throw new RuntimeException(e.getMessage() + " Are you passing a nested JSHandle?");
            else
                throw new RuntimeException(e);
        }
        if (callFunctionOnPromise == null) {
            return null;
        }
        JsonNode exceptionDetails = callFunctionOnPromise.get("exceptionDetails");
        RemoteObject remoteObject;
        try {
            if (exceptionDetails != null)
                throw new ProtocolException("Evaluation failed: " + Builder.getExceptionMessage(org.aoju.lancia.Builder.OBJECTMAPPER.treeToValue(exceptionDetails, ExceptionDetails.class)));
            remoteObject = org.aoju.lancia.Builder.OBJECTMAPPER.treeToValue(callFunctionOnPromise.get("result"), RemoteObject.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return returnByValue ? Builder.valueFromRemoteObject(remoteObject) : createJSHandle(this, remoteObject);
    }

    public JSHandle queryObjects(JSHandle prototypeHandle) {
        Assert.isTrue(!prototypeHandle.getDisposed(), "Prototype JSHandle is disposed!");
        Assert.isTrue(StringKit.isNotEmpty(prototypeHandle.getRemoteObject().getObjectId()), "Prototype JSHandle must not be referencing primitive value");
        Map<String, Object> params = new HashMap<>();
        params.put("prototypeObjectId", prototypeHandle.getRemoteObject().getObjectId());
        JsonNode response = this.client.send("Runtime.queryObjects", params, true);
        try {
            return createJSHandle(this, org.aoju.lancia.Builder.OBJECTMAPPER.treeToValue(response.get("objects"), RemoteObject.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Object convertArgument(ExecutionContext th, Object arg) {
        ObjectNode objectNode = org.aoju.lancia.Builder.OBJECTMAPPER.createObjectNode();
        if (arg == null) {
            return null;
        }
        if (arg instanceof BigInteger) // eslint-disable-line valid-typeof
            return objectNode.put("unserializableValue", arg + "n");
        if ("-0".equals(arg))
            return objectNode.put("unserializableValue", "-0");

        if ("Infinity".equals(arg))
            return objectNode.put("unserializableValue", "Infinity");

        if ("-Infinity".equals(arg))
            return objectNode.put("unserializableValue", "-Infinity");

        if ("NaN".equals(arg))
            return objectNode.put("unserializableValue", "NaN");
        JSHandle objectHandle = arg instanceof JSHandle ? (JSHandle) arg : null;
        if (objectHandle != null) {
            if (objectHandle.getContext() != this)
                throw new IllegalArgumentException("JSHandles can be evaluated only in the context they were created!");
            if (objectHandle.getDisposed())
                throw new IllegalArgumentException("JSHandle is disposed!");
            if (objectHandle.getRemoteObject().getUnserializableValue() != null)
                return objectNode.put("unserializableValue", objectHandle.getRemoteObject().getUnserializableValue());
            if (StringKit.isEmpty(objectHandle.getRemoteObject().getObjectId()))
                return objectNode.putPOJO("value", objectHandle.getRemoteObject().getValue());
            return objectNode.put("objectId", objectHandle.getRemoteObject().getObjectId());
        }
        return objectNode.putPOJO("value", arg);
    }

    private JSHandle createJSHandle(ExecutionContext executionContext, RemoteObject remoteObject) {
        return JSHandle.createJSHandle(executionContext, remoteObject);
    }

    public ElementHandle adoptBackendNodeId(int backendNodeId) {
        Map<String, Object> params = new HashMap<>();
        params.put("backendNodeId", backendNodeId);
        params.put("executionContextId", this.contextId);
        JsonNode object = this.client.send("DOM.resolveNode", params, true);
        try {
            return (ElementHandle) createJSHandle(this, org.aoju.lancia.Builder.OBJECTMAPPER.treeToValue(object.get("object"), RemoteObject.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public CDPSession getClient() {
        return client;
    }

    public void setClient(CDPSession client) {
        this.client = client;
    }

    public int getContextId() {
        return contextId;
    }

}
