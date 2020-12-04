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
package org.aoju.bus.puppeteer.kernel.page;

import com.fasterxml.jackson.databind.JsonNode;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.puppeteer.Builder;
import org.aoju.bus.puppeteer.nimble.*;
import org.aoju.bus.puppeteer.worker.BrowserListener;
import org.aoju.bus.puppeteer.worker.CDPSession;
import org.aoju.bus.puppeteer.worker.ListenerWrapper;

import java.util.*;

/**
 * @author Kimi Liu
 * @version 6.1.3
 * @since JDK 1.8+
 */
public class CSSCoverage {

    private final CDPSession client;
    private final HashMap<String, String> stylesheetURLs;
    private final HashMap<String, String> stylesheetSources;
    private final List<ListenerWrapper> eventListeners;
    private boolean enabled;
    private boolean resetOnNavigation;

    public CSSCoverage(CDPSession client) {
        this.client = client;
        this.enabled = false;
        this.stylesheetURLs = new HashMap<>();
        this.stylesheetSources = new HashMap();
        this.eventListeners = new ArrayList<>();
        this.resetOnNavigation = false;
    }

    public void start(boolean resetOnNavigation) {
        Assert.isTrue(!this.enabled, "CSSCoverage is already enabled");

        this.resetOnNavigation = resetOnNavigation;
        this.enabled = true;
        this.stylesheetURLs.clear();
        this.stylesheetSources.clear();

        BrowserListener<StyleSheetAddedPayload> addLis = new BrowserListener<StyleSheetAddedPayload>() {
            @Override
            public void onBrowserEvent(StyleSheetAddedPayload event) {
                CSSCoverage cssCoverage = (CSSCoverage) this.getTarget();
                cssCoverage.onStyleSheet(event);
            }
        };
        addLis.setMothod("CSS.styleSheetAdded");
        addLis.setTarget(this);

        BrowserListener<Object> clearLis = new BrowserListener<Object>() {
            @Override
            public void onBrowserEvent(Object event) {
                CSSCoverage cssCoverage = (CSSCoverage) this.getTarget();
                cssCoverage.onExecutionContextsCleared();
            }
        };
        clearLis.setMothod("Runtime.executionContextsCleared");
        clearLis.setTarget(this);

        this.eventListeners.add(Builder.addEventListener(this.client, addLis.getMothod(), addLis));
        this.eventListeners.add(Builder.addEventListener(this.client, clearLis.getMothod(), clearLis));

        this.client.send("DOM.enable", null, false);
        this.client.send("CSS.enable", null, false);
        this.client.send("CSS.startRuleUsageTracking", null, true);

    }

    private void onExecutionContextsCleared() {
        if (!this.resetOnNavigation) return;
        this.stylesheetURLs.clear();
        this.stylesheetSources.clear();
    }

    private void onStyleSheet(StyleSheetAddedPayload event) {
        StyleSheetHeader header = event.getHeader();
        if (StringKit.isEmpty(header.getSourceURL())) {
            return;
        }

        Builder.commonExecutor().submit(() -> {
            Map<String, Object> params = new HashMap<>();
            params.put("styleSheetId", header.getStyleSheetId());
            JsonNode response = client.send("CSS.getStyleSheetText", params, true);
            stylesheetURLs.put(header.getStyleSheetId(), header.getSourceURL());
            stylesheetSources.put(header.getStyleSheetId(), response.get("text").asText());
        });

    }

    public List<CoverageEntry> stop() {
        Assert.isTrue(this.enabled, "CSSCoverage is not enabled");
        this.enabled = false;

        JsonNode ruleTrackingResponse = this.client.send("CSS.stopRuleUsageTracking", null, true);

        this.client.send("CSS.disable", null, false);
        this.client.send("DOM.disable", null, false);

        Builder.removeEventListeners(this.eventListeners);

        Map<String, List<CoverageRange>> styleSheetIdToCoverage = new HashMap<>();
        JsonNode ruleUsageNode = ruleTrackingResponse.get("ruleUsage");
        Iterator<JsonNode> elements = ruleUsageNode.elements();
        while (elements.hasNext()) {
            JsonNode entry = elements.next();
            List<CoverageRange> ranges = styleSheetIdToCoverage.get(entry.get("styleSheetId").asText());
            if (ranges == null) {
                ranges = new ArrayList<>();
                styleSheetIdToCoverage.put(entry.get("styleSheetId").asText(), ranges);
            }
            boolean used = entry.get("used").asBoolean();
            if (used)
                ranges.add(new CoverageRange(entry.get("startOffset").asInt(), entry.get("endOffset").asInt(), 1));
            else
                ranges.add(new CoverageRange(entry.get("startOffset").asInt(), entry.get("endOffset").asInt(), 0));
        }


        List<CoverageEntry> coverage = new ArrayList<>();
        for (String styleSheetId : this.stylesheetURLs.keySet()) {
            String url = this.stylesheetURLs.get(styleSheetId);
            String text = this.stylesheetSources.get(styleSheetId);
            List<Range> ranges = Coverage.convertToDisjointRanges(styleSheetIdToCoverage.get(styleSheetId));
            coverage.add(new CoverageEntry(url, ranges, text));
        }
        return coverage;
    }

}
