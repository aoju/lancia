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
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.lancia.Builder;
import org.aoju.lancia.nimble.runtime.RemoteObject;
import org.aoju.lancia.worker.CDPSession;

import java.util.*;

/**
 * JS拦截器
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class JSHandle {

    private final CDPSession client;
    private final RemoteObject remoteObject;
    private ExecutionContext context;
    private boolean disposed = false;

    public JSHandle(ExecutionContext context, CDPSession client, RemoteObject remoteObject) {
        this.context = context;
        this.client = client;
        this.remoteObject = remoteObject;
    }

    public static JSHandle createJSHandle(ExecutionContext context, RemoteObject remoteObject) {
        Frame frame = context.frame();
        if ("node".equals(remoteObject.getSubtype()) && frame != null) {
            FrameManager frameManager = frame.getFrameManager();
            return new ElementHandle(context, context.getClient(), remoteObject, frameManager.getPage(), frameManager);
        }
        return new JSHandle(context, context.getClient(), remoteObject);
    }

    public ExecutionContext executionContext() {
        return this.context;
    }

    public Object evaluate(String pageFunction, List<Object> args) {
        List<Object> argsArray = new ArrayList<>();
        argsArray.add(this);
        argsArray.addAll(args);
        return this.executionContext().evaluate(pageFunction, argsArray);
    }

    public Object evaluateHandle(String pageFunction, List<Object> args) {
        List<Object> argsArray = new ArrayList<>();
        argsArray.add(this);
        argsArray.addAll(args);
        return this.executionContext().evaluateHandle(pageFunction, argsArray);
    }

    public JSHandle getProperty(String propertyName) {
        String pageFunction = "(object, propertyName) => {\n" +
                "            const result = { __proto__: null };\n" +
                "            result[propertyName] = object[propertyName];\n" +
                "            return result;\n" +
                "        }";
        JSHandle objectHandle = (JSHandle) this.evaluateHandle(pageFunction, Collections.singletonList(propertyName));
        Map<String, JSHandle> properties = objectHandle.getProperties();
        JSHandle result = properties.get(propertyName);
        objectHandle.dispose();
        return result;
    }

    public Map<String, JSHandle> getProperties() {
        Map<String, Object> params = new HashMap<>();
        params.put("objectId", this.remoteObject.getObjectId());
        params.put("ownProperties", true);
        JsonNode response = this.client.send("Runtime.getProperties", params, true);
        Map<String, JSHandle> result = new LinkedHashMap<>();
        Iterator<JsonNode> iterator = response.get("result").iterator();
        while (iterator.hasNext()) {
            JsonNode property = iterator.next();
            if (!property.get("enumerable").asBoolean())
                continue;
            try {
                result.put(property.get("name").asText(), createJSHandle(this.context, org.aoju.lancia.Builder.OBJECTMAPPER.treeToValue(property.get("value"), RemoteObject.class)));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    public Object jsonValue() {
        if (StringKit.isNotEmpty(this.remoteObject.getObjectId())) {
            Map<String, Object> params = new HashMap<>();
            params.put("functionDeclaration", "function() { return this; }");
            params.put("objectId", this.remoteObject.getObjectId());
            params.put("returnByValue", true);
            params.put("awaitPromise", true);
            JsonNode response = this.client.send("Runtime.callFunctionOn", params, true);
            try {
                return Builder.valueFromRemoteObject(org.aoju.lancia.Builder.OBJECTMAPPER.treeToValue(response.get("result"), RemoteObject.class));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return Builder.valueFromRemoteObject(this.remoteObject);
    }

    /* This always returns null but children can define this and return an ElementHandle */
    public ElementHandle asElement() {
        return null;
    }

    /**
     * 阻塞释放elementHandle
     */
    public void dispose() {
        this.dispose(true);
    }

    /**
     * 释放elementhandle
     * 当在websocket信息回调中处理时需要isBlock=false
     *
     * @param isBlock 是否是异步
     */
    public void dispose(boolean isBlock) {
        if (this.disposed)
            return;
        this.disposed = true;
        Builder.releaseObject(this.client, this.remoteObject, isBlock);
    }

    public String toString() {
        if (StringKit.isNotEmpty(this.remoteObject.getObjectId())) {
            String type = StringKit.isNotEmpty(this.remoteObject.getSubtype()) ? this.remoteObject.getSubtype() : this.remoteObject.getType();
            return "JSHandle@" + type;
        }
        return "JSHandle:" + Builder.valueFromRemoteObject(this.remoteObject);
    }

    protected ExecutionContext getContext() {
        return context;
    }

    protected void setContext(ExecutionContext context) {
        this.context = context;
    }

    protected boolean getDisposed() {
        return disposed;
    }

    protected void setDisposed(boolean disposed) {
        this.disposed = disposed;
    }

    public RemoteObject getRemoteObject() {
        return remoteObject;
    }

}
