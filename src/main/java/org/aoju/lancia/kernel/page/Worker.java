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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.logger.Logger;
import org.aoju.lancia.Variables;
import org.aoju.lancia.nimble.runtime.ConsoleCalledPayload;
import org.aoju.lancia.nimble.runtime.ExceptionDetails;
import org.aoju.lancia.nimble.runtime.ExecutionDescription;
import org.aoju.lancia.nimble.runtime.RemoteObject;
import org.aoju.lancia.worker.BrowserListener;
import org.aoju.lancia.worker.CDPSession;
import org.aoju.lancia.worker.EventEmitter;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 在页面对象上会发出“ workercreated”和“ workerdestroyed”事件，以表示工人的生命周期。
 *
 * @author Kimi Liu
 * @version 1.2.2
 * @since JDK 1.8+
 */
public class Worker extends EventEmitter {

    private final CDPSession client;

    private final String url;

    private ExecutionContext context;

    private CountDownLatch contextLatch;

    public Worker(CDPSession client, String url, ConsoleAPI consoleAPICalled, Consumer<ExceptionDetails> exceptionThrown) {
        super();
        this.client = client;
        this.url = url;
        BrowserListener<JsonNode> executionContextListener = new BrowserListener<JsonNode>() {
            @Override
            public void onBrowserEvent(JsonNode event) {
                try {
                    Worker worker = (Worker) this.getTarget();
                    ExecutionDescription contextDescription = Variables.OBJECTMAPPER.treeToValue(event.get("context"), ExecutionDescription.class);
                    ExecutionContext executionContext = new ExecutionContext(client, contextDescription, null);
                    worker.executionContextCallback(executionContext);
                } catch (JsonProcessingException e) {
                    Logger.error("executionContextCreated event json process error ", e);
                }
            }
        };
        executionContextListener.setMethod("Runtime.executionContextCreated");
        executionContextListener.setTarget(this);
        this.client.addListener(executionContextListener.getMethod(), executionContextListener, true);

        this.client.send("Runtime.enable", null, false);
        BrowserListener<ConsoleCalledPayload> consoleLis = new BrowserListener<ConsoleCalledPayload>() {
            @Override
            public void onBrowserEvent(ConsoleCalledPayload event) {
                consoleAPICalled.call(event.getType(), event.getArgs().stream().map(item -> jsHandleFactory(item)).collect(Collectors.toList()), event.getStackTrace());
            }
        };
        consoleLis.setMethod("Runtime.consoleAPICalled");
        this.client.addListener(consoleLis.getMethod(), consoleLis);

        BrowserListener<JsonNode> exceptionLis = new BrowserListener<JsonNode>() {
            @Override
            public void onBrowserEvent(JsonNode event) {
                try {
                    ExceptionDetails exceptionDetails = Variables.OBJECTMAPPER.treeToValue(event.get("exceptionDetails"), ExceptionDetails.class);
                    exceptionThrown.accept(exceptionDetails);
                } catch (JsonProcessingException e) {
                    Logger.error("exceptionThrown event json process error ", e);
                }
            }
        };
        exceptionLis.setMethod("Runtime.exceptionThrown");
        this.client.addListener(exceptionLis.getMethod(), exceptionLis);
    }

    public JSHandle jsHandleFactory(RemoteObject remoteObject) {
        return new JSHandle(this.context, client, remoteObject);
    }

    protected void executionContextCallback(ExecutionContext executionContext) {
        this.setContext(executionContext);
    }

    private ExecutionContext executionContextPromise() throws InterruptedException {
        if (context == null) {
            this.setContextLatch(new CountDownLatch(1));
            boolean await = this.getContextLatch().await(Variables.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
            if (!await) {
                throw new InstrumentException("Wait for ExecutionContext timeout");
            }
        }
        return context;
    }

    private CountDownLatch getContextLatch() {
        return contextLatch;
    }

    private void setContextLatch(CountDownLatch contextLatch) {
        this.contextLatch = contextLatch;
    }

    public void setContext(ExecutionContext context) {
        this.context = context;
    }

    public String url() {
        return this.url;
    }

    public ExecutionContext executionContext() throws InterruptedException {
        return this.executionContextPromise();
    }

    public Object evaluate(String pageFunction, List<Object> args) throws InterruptedException {
        return this.executionContextPromise().evaluate(pageFunction, args);
    }

    public Object evaluateHandle(String pageFunction, List<Object> args) throws InterruptedException {
        return this.executionContextPromise().evaluateHandle(pageFunction, args);
    }

}


