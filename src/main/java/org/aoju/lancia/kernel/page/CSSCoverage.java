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

import com.alibaba.fastjson.JSONObject;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.lancia.Builder;
import org.aoju.lancia.events.BrowserListenerWrapper;
import org.aoju.lancia.events.DefaultBrowserListener;
import org.aoju.lancia.nimble.css.CSSStyleSheetHeader;
import org.aoju.lancia.nimble.css.Range;
import org.aoju.lancia.nimble.css.StyleSheetAddedPayload;
import org.aoju.lancia.nimble.profiler.CoverageEntry;
import org.aoju.lancia.nimble.profiler.CoverageRange;
import org.aoju.lancia.worker.CDPSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class CSSCoverage {

    private final CDPSession client;
    private final HashMap<String, String> stylesheetURLs;
    private final HashMap<String, String> stylesheetSources;
    private final List<BrowserListenerWrapper> eventListeners;
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

        DefaultBrowserListener<StyleSheetAddedPayload> addLis = new DefaultBrowserListener<StyleSheetAddedPayload>() {
            @Override
            public void onBrowserEvent(StyleSheetAddedPayload event) {
                CSSCoverage cssCoverage = (CSSCoverage) this.getTarget();
                cssCoverage.onStyleSheet(event);
            }
        };
        addLis.setMethod("CSS.styleSheetAdded");
        addLis.setTarget(this);

        DefaultBrowserListener<Object> clearLis = new DefaultBrowserListener<>() {
            @Override
            public void onBrowserEvent(Object event) {
                CSSCoverage cssCoverage = (CSSCoverage) this.getTarget();
                cssCoverage.onExecutionContextsCleared();
            }
        };
        clearLis.setMethod("Runtime.executionContextsCleared");
        clearLis.setTarget(this);

        this.eventListeners.add(Builder.addEventListener(this.client, addLis.getMethod(), addLis));
        this.eventListeners.add(Builder.addEventListener(this.client, clearLis.getMethod(), clearLis));

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
        CSSStyleSheetHeader header = event.getHeader();
        // Ignore anonymous scripts
        if (StringKit.isEmpty(header.getSourceURL())) return;

        Builder.commonExecutor().submit(() -> {
            Map<String, Object> params = new HashMap<>();
            params.put("styleSheetId", header.getStyleSheetId());
            JSONObject response = client.send("CSS.getStyleSheetText", params, true);
            stylesheetURLs.put(header.getStyleSheetId(), header.getSourceURL());
            stylesheetSources.put(header.getStyleSheetId(), response.getString("text"));
        });

    }

    public List<CoverageEntry> stop() {
        Assert.isTrue(this.enabled, "CSSCoverage is not enabled");
        this.enabled = false;

        JSONObject ruleTrackingResponse = this.client.send("CSS.stopRuleUsageTracking", null, true);

        this.client.send("CSS.disable", null, false);
        this.client.send("DOM.disable", null, false);

        Builder.removeEventListeners(this.eventListeners);

        // aggregate by styleSheetId
        Map<String, List<CoverageRange>> styleSheetIdToCoverage = new HashMap<>();
        JSONObject ruleUsageNode = ruleTrackingResponse.getJSONObject("ruleUsage");
        for (String key : ruleUsageNode.keySet()) {
            JSONObject entry = ruleUsageNode.getJSONObject(key);
            List<CoverageRange> ranges = styleSheetIdToCoverage.get(entry.getString("styleSheetId"));
            if (ranges == null) {
                ranges = new ArrayList<>();
                styleSheetIdToCoverage.put(entry.getString("styleSheetId"), ranges);
            }
            boolean used = entry.getBoolean("used");
            if (used)
                ranges.add(new CoverageRange(entry.getInteger("startOffset"), entry.getInteger("endOffset"), 1));
            else
                ranges.add(new CoverageRange(entry.getInteger("startOffset"), entry.getInteger("endOffset"), 0));
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
