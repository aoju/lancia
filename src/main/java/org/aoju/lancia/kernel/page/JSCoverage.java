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
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.toolkit.CollKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.lancia.Builder;
import org.aoju.lancia.Variables;
import org.aoju.lancia.nimble.*;
import org.aoju.lancia.worker.BrowserListener;
import org.aoju.lancia.worker.CDPSession;
import org.aoju.lancia.worker.ListenerWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JS覆盖范围
 *
 * @author Kimi Liu
 * @version 1.2.2
 * @since JDK 1.8+
 */
public class JSCoverage {

    private final CDPSession client;
    private final Map<String, String> scriptSources;
    private final Map<String, String> scriptURLs;
    private final List<ListenerWrapper> eventListeners;
    private boolean enabled;
    private boolean resetOnNavigation;

    private boolean reportAnonymousScripts;

    public JSCoverage(CDPSession client) {
        this.client = client;
        this.enabled = false;
        this.scriptURLs = new HashMap<>();
        this.scriptSources = new HashMap<>();
        this.eventListeners = new ArrayList<>();
        this.resetOnNavigation = false;
    }

    public void start(boolean resetOnNavigation, boolean reportAnonymousScripts) {
        Assert.isTrue(!this.enabled, "JSCoverage is already enabled");

        this.resetOnNavigation = resetOnNavigation;
        this.reportAnonymousScripts = reportAnonymousScripts;
        this.enabled = true;
        this.scriptURLs.clear();
        this.scriptSources.clear();
        BrowserListener<ScriptParsedPayload> scriptParsedLis = new BrowserListener<ScriptParsedPayload>() {
            @Override
            public void onBrowserEvent(ScriptParsedPayload event) {
                JSCoverage jsCoverage = (JSCoverage) this.getTarget();
                jsCoverage.onScriptParsed(event);
            }
        };
        scriptParsedLis.setTarget(this);
        scriptParsedLis.setMethod("Debugger.scriptParsed");
        this.eventListeners.add(Builder.addEventListener(this.client, scriptParsedLis.getMethod(), scriptParsedLis));

        BrowserListener<Object> clearedLis = new BrowserListener<Object>() {
            @Override
            public void onBrowserEvent(Object event) {
                JSCoverage jsCoverage = (JSCoverage) this.getTarget();
                jsCoverage.onExecutionContextsCleared();
            }
        };
        clearedLis.setTarget(this);
        clearedLis.setMethod("Runtime.executionContextsCleared");
        this.eventListeners.add(Builder.addEventListener(this.client, clearedLis.getMethod(), clearedLis));


        this.client.send("Profiler.enable", null, false);
        Map<String, Object> params = new HashMap<>();
        params.put("callCount", false);
        params.put("detailed", true);
        this.client.send("Profiler.startPreciseCoverage", params, false);
        this.client.send("Debugger.enable", null, false);
        params.clear();
        params.put("skip", true);
        this.client.send("Debugger.setSkipAllPauses", params, true);
    }

    private void onExecutionContextsCleared() {
        if (!this.resetOnNavigation)
            return;
        this.scriptURLs.clear();
        this.scriptSources.clear();
    }

    private void onScriptParsed(ScriptParsedPayload event) {
        if (ExecutionContext.EVALUATION_SCRIPT_URL.equals(event.getUrl()))
            return;

        if (StringKit.isEmpty(event.getUrl()) && !this.reportAnonymousScripts)
            return;
        Builder.commonExecutor().submit(() -> {
            Map<String, Object> params = new HashMap<>();
            params.put("scriptId", event.getScriptId());
            JsonNode response = client.send("Debugger.getScriptSource", params, true);
            scriptURLs.put(event.getScriptId(), event.getUrl());
            scriptSources.put(event.getScriptId(), response.get("scriptSource").asText());
        });
    }

    public List<CoverageEntry> stop() throws JsonProcessingException {
        Assert.isTrue(this.enabled, "JSCoverage is not enabled");
        this.enabled = false;

        JsonNode result = this.client.send("Profiler.takePreciseCoverage", null, true);
        this.client.send("Profiler.stopPreciseCoverage", null, false);
        this.client.send("Profiler.disable", null, false);
        this.client.send("Debugger.disable", null, false);


        Builder.removeEventListeners(this.eventListeners);

        List<CoverageEntry> coverage = new ArrayList<>();
        TakePreciseCoverage profileResponse = Variables.OBJECTMAPPER.treeToValue(result, TakePreciseCoverage.class);
        if (CollKit.isEmpty(profileResponse.getResult())) {
            return coverage;
        }
        for (ScriptCoverage entry : profileResponse.getResult()) {
            String url = this.scriptURLs.get(entry.getScriptId());
            if (StringKit.isEmpty(url) && this.reportAnonymousScripts)
                url = "debugger://VM" + entry.getScriptId();
            String text = this.scriptSources.get(entry.getScriptId());
            if (StringKit.isEmpty(url) || StringKit.isEmpty(text))
                continue;
            List<CoverageRange> flattenRanges = new ArrayList<>();
            for (FunctionCoverage func : entry.getFunctions())
                flattenRanges.addAll(func.getRanges());
            List<Range> ranges = Coverage.convertToDisjointRanges(flattenRanges);
            coverage.add(createCoverageEntry(url, ranges, text));
        }
        return coverage;
    }

    private CoverageEntry createCoverageEntry(String url, List<Range> ranges, String text) {
        CoverageEntry coverageEntity = new CoverageEntry();
        coverageEntity.setUrl(url);
        coverageEntity.setRanges(ranges);
        coverageEntity.setText(text);
        return coverageEntity;
    }

}
