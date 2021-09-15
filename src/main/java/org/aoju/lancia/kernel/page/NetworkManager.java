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

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.lancia.Builder;
import org.aoju.lancia.Variables;
import org.aoju.lancia.nimble.AuthorizePayload;
import org.aoju.lancia.nimble.Credentials;
import org.aoju.lancia.nimble.RequestPausedPayload;
import org.aoju.lancia.nimble.network.*;
import org.aoju.lancia.worker.BrowserListener;
import org.aoju.lancia.worker.CDPSession;
import org.aoju.lancia.worker.EventEmitter;

import java.util.*;

/**
 * 网络管理
 *
 * @author Kimi Liu
 * @version 1.2.2
 * @since JDK 1.8+
 */
public class NetworkManager extends EventEmitter {

    private final CDPSession client;

    private final boolean ignoreHTTPSErrors;

    private final FrameManager frameManager;
    private final Map<String, Request> requestIdToRequest;
    private final Map<String, RequestWillPayload> requestIdToRequestWillBeSentEvent;
    private final Set<String> attemptedAuthentications;
    private final Map<String, String> requestIdToInterceptionId;
    private Map<String, String> extraHTTPHeaders;
    private boolean offline;
    private Credentials credentials;
    private boolean userRequestInterceptionEnabled;
    private boolean protocolRequestInterceptionEnabled;
    private boolean userCacheDisabled;

    public NetworkManager(CDPSession client, boolean ignoreHTTPSErrors, FrameManager frameManager) {
        this.client = client;
        this.ignoreHTTPSErrors = ignoreHTTPSErrors;
        this.frameManager = frameManager;
        this.requestIdToRequest = new HashMap<>();
        this.requestIdToRequestWillBeSentEvent = new HashMap<>();
        this.extraHTTPHeaders = new HashMap<>();
        this.offline = false;
        this.credentials = null;
        this.attemptedAuthentications = new HashSet<>();
        this.userRequestInterceptionEnabled = false;
        this.protocolRequestInterceptionEnabled = false;
        this.userCacheDisabled = false;
        this.requestIdToInterceptionId = new HashMap<>();

        BrowserListener<RequestPausedPayload> requestPausedListener = new BrowserListener<RequestPausedPayload>() {
            @Override
            public void onBrowserEvent(RequestPausedPayload event) {
                NetworkManager manager = (NetworkManager) this.getTarget();
                manager.onRequestPaused(event);
            }
        };
        requestPausedListener.setMethod("Fetch.requestPaused");
        requestPausedListener.setTarget(this);
        this.client.addListener(requestPausedListener.getMethod(), requestPausedListener);

        BrowserListener<AuthorizePayload> authRequiredListener = new BrowserListener<AuthorizePayload>() {
            @Override
            public void onBrowserEvent(AuthorizePayload event) {
                NetworkManager manager = (NetworkManager) this.getTarget();
                manager.onAuthRequired(event);
            }
        };
        authRequiredListener.setMethod("Fetch.authRequired");
        authRequiredListener.setTarget(this);
        this.client.addListener(authRequiredListener.getMethod(), authRequiredListener);

        BrowserListener<RequestWillPayload> requestWillBeSentListener = new BrowserListener<RequestWillPayload>() {
            @Override
            public void onBrowserEvent(RequestWillPayload event) {
                NetworkManager manager = (NetworkManager) this.getTarget();
                manager.onRequestWillBeSent(event);
            }
        };
        requestWillBeSentListener.setMethod("Network.requestWillBeSent");
        requestWillBeSentListener.setTarget(this);
        this.client.addListener(requestWillBeSentListener.getMethod(), requestWillBeSentListener);

        BrowserListener<RequestCachePayload> requestServedFromCacheListener = new BrowserListener<RequestCachePayload>() {
            @Override
            public void onBrowserEvent(RequestCachePayload event) {
                NetworkManager manager = (NetworkManager) this.getTarget();
                manager.onRequestServedFromCache(event);
            }
        };
        requestServedFromCacheListener.setMethod("Network.requestServedFromCache");
        requestServedFromCacheListener.setTarget(this);
        this.client.addListener(requestServedFromCacheListener.getMethod(), requestServedFromCacheListener);

        BrowserListener<ReceivedPayload> responseReceivedListener = new BrowserListener<ReceivedPayload>() {
            @Override
            public void onBrowserEvent(ReceivedPayload event) {
                NetworkManager manager = (NetworkManager) this.getTarget();
                manager.onResponseReceived(event);
            }
        };
        responseReceivedListener.setMethod("Network.responseReceived");
        responseReceivedListener.setTarget(this);
        this.client.addListener(responseReceivedListener.getMethod(), responseReceivedListener);

        BrowserListener<FinishedPayload> loadingFinishedListener = new BrowserListener<FinishedPayload>() {
            @Override
            public void onBrowserEvent(FinishedPayload event) {
                NetworkManager manager = (NetworkManager) this.getTarget();
                manager.onLoadingFinished(event);
            }
        };
        loadingFinishedListener.setMethod("Network.loadingFinished");
        loadingFinishedListener.setTarget(this);
        this.client.addListener(loadingFinishedListener.getMethod(), loadingFinishedListener);

        BrowserListener<FailedPayload> loadingFailedListener = new BrowserListener<FailedPayload>() {
            @Override
            public void onBrowserEvent(FailedPayload event) {
                NetworkManager manager = (NetworkManager) this.getTarget();
                manager.onLoadingFailed(event);
            }
        };
        loadingFailedListener.setMethod("Network.loadingFailed");
        loadingFailedListener.setTarget(this);
        this.client.addListener(loadingFailedListener.getMethod(), loadingFailedListener);

    }

    public void setExtraHTTPHeaders(Map<String, String> extraHTTPHeaders) {
        this.extraHTTPHeaders = new HashMap<>();
        for (Map.Entry<String, String> entry : extraHTTPHeaders.entrySet()) {

            String value = entry.getValue();
            Assert.isTrue(Builder.isString(value), "Expected value of header " + entry.getKey() + " to be String, but " + value.getClass().getCanonicalName() + " is found.");
            this.extraHTTPHeaders.put(entry.getKey(), value);
        }
        Map<String, Object> params = new HashMap<>();
        params.put("headers", this.extraHTTPHeaders);
        this.client.send("Network.setExtraHTTPHeaders", params, true);
    }

    public void initialize() {
        this.client.send("Network.enable", null, true);
        if (this.ignoreHTTPSErrors) {
            Map<String, Object> params = new HashMap<>();
            params.put("ignore", true);
            this.client.send("Security.setIgnoreCertificateErrors", params, true);
        }

    }

    public void authenticate(Credentials credentials) {
        this.credentials = credentials;
        this.updateProtocolRequestInterception();
    }

    public Map<String, String> extraHTTPHeaders() {
        return new HashMap<>(this.extraHTTPHeaders);
    }

    public void setOfflineMode(boolean value) {
        if (this.offline == value)
            return;
        this.offline = value;
        Map<String, Object> params = new HashMap<>();
        params.put("offline", this.offline);
        // values of 0 remove any active throttling. crbug.com/456324#c9
        params.put("latency", 0);
        params.put("downloadThroughput", -1);
        params.put("uploadThroughput", -1);
        this.client.send("Network.emulateNetworkConditions", params, true);
    }

    public void setUserAgent(String userAgent) {
        Map<String, Object> params = new HashMap<>();
        params.put("userAgent", userAgent);
        this.client.send("Network.setUserAgentOverride", params, true);
    }

    public void setCacheEnabled(boolean enabled) {
        this.userCacheDisabled = !enabled;
        this.updateProtocolCacheDisabled();
    }

    public void setRequestInterception(boolean value) {
        this.userRequestInterceptionEnabled = value;
        this.updateProtocolRequestInterception();
    }

    private void updateProtocolCacheDisabled() {
        Map<String, Object> params = new HashMap<>();
        boolean cacheDisabled = this.userCacheDisabled || this.protocolRequestInterceptionEnabled;
        params.put("cacheDisabled", cacheDisabled);
        this.client.send("Network.setCacheDisabled", params, true);
    }

    public void updateProtocolRequestInterception() {
        boolean enabled = false;
        if (this.userRequestInterceptionEnabled || this.credentials != null) {
            enabled = true;
        }
        if (enabled == this.protocolRequestInterceptionEnabled)
            return;
        this.protocolRequestInterceptionEnabled = enabled;
        this.updateProtocolCacheDisabled();
        if (enabled) {
            Map<String, Object> params = new HashMap<>();
            params.put("handleAuthRequests", true);
            List<Object> patterns = new ArrayList<>();
            patterns.add(Variables.OBJECTMAPPER.createObjectNode().put("urlPattern", "*"));
            params.put("patterns", patterns);
            this.client.send("Fetch.enable", params, true);
        } else {
            this.client.send("Fetch.disable", null, true);
        }
    }

    public void onRequestWillBeSent(RequestWillPayload event) {
        // Request interception doesn't happen for data URLs with Network Service.
        if (this.protocolRequestInterceptionEnabled && !event.getRequest().url().startsWith("data:")) {
            String requestId = event.getRequestId();
            String interceptionId = this.requestIdToInterceptionId.get(requestId);
            if (StringKit.isNotEmpty(interceptionId)) {
                this.onRequest(event, interceptionId);
                this.requestIdToInterceptionId.remove(requestId);
            } else {
                this.requestIdToRequestWillBeSentEvent.put(event.getRequestId(), event);
            }
            return;
        }
        this.onRequest(event, null);
    }

    public void onAuthRequired(AuthorizePayload event) {
        /* @type {"Default"|"CancelAuth"|"ProvideCredentials"} */
        String response = "Default";
        if (this.attemptedAuthentications.contains(event.getRequestId())) {
            response = "CancelAuth";
        } else if (this.credentials != null) {
            response = "ProvideCredentials";
            this.attemptedAuthentications.add(event.getRequestId());
        }
        String username, password;
        ObjectNode respParams = Variables.OBJECTMAPPER.createObjectNode();
        respParams.put("response", response);
        if (this.credentials != null) {
            if (StringKit.isNotEmpty(username = credentials.getUsername())) {
                respParams.put("username", username);
            }
            if (StringKit.isNotEmpty(password = credentials.getPassword())) {
                respParams.put("password", password);
            }
        }
        Map<String, Object> params = new HashMap<>();
        params.put("response", "Default");
        params.put("requestId", event.getRequestId());
        params.put("authChallengeResponse", respParams);
        this.client.send("Fetch.continueWithAuth", params, false);
    }

    public void onRequestPaused(RequestPausedPayload event) {
        if (!this.userRequestInterceptionEnabled && this.protocolRequestInterceptionEnabled) {
            Map<String, Object> params = new HashMap<>();
            params.put("requestId", event.getRequestId());
            this.client.send("Fetch.continueRequest", params, false);
        }

        String requestId = event.getNetworkId();
        String interceptionId = event.getRequestId();
        if (StringKit.isNotEmpty(requestId) && this.requestIdToRequestWillBeSentEvent.containsKey(requestId)) {
            RequestWillPayload requestWillBeSentEvent = this.requestIdToRequestWillBeSentEvent.get(requestId);
            this.onRequest(requestWillBeSentEvent, interceptionId);
            this.requestIdToRequestWillBeSentEvent.remove(requestId);
        } else {
            this.requestIdToInterceptionId.put(requestId, interceptionId);
        }
    }

    public void onRequest(RequestWillPayload event, String interceptionId) {
        List<Request> redirectChain = new ArrayList<>();
        if (event.getRedirectResponse() != null) {
            Request request = this.requestIdToRequest.get(event.getRequestId());
            if (request != null) {
                this.handleRequestRedirect(request, event.getRedirectResponse());
                redirectChain = request.redirectChain();
            }
        }
        Frame frame = StringKit.isNotEmpty(event.getFrameId()) ? this.frameManager.getFrame(event.getFrameId()) : null;
        Request request = new Request(this.client, frame, interceptionId, this.userRequestInterceptionEnabled, event, redirectChain);
        this.requestIdToRequest.put(event.getRequestId(), request);
        this.emit(Variables.Event.NETWORK_MANAGER_REQUEST.getName(), request);
    }

    private void handleRequestRedirect(Request request, ResponsePayload responsePayload) {
        Response response = new Response(this.client, request, responsePayload);
        request.setResponse(response);
        request.redirectChain().add(request);
        response.resolveBody("Response body is unavailable for redirect responses");
        this.requestIdToRequest.remove(request.requestId());
        this.attemptedAuthentications.remove(request.interceptionId());
        this.emit(Variables.Event.NETWORK_MANAGER_RESPONSE.getName(), response);
        this.emit(Variables.Event.NETWORK_MANAGER_REQUEST_FINISHED.getName(), request);
    }

    public void onLoadingFinished(FinishedPayload event) {
        Request request = this.requestIdToRequest.get(event.getRequestId());
        if (request == null)
            return;

        if (request.response() != null)
            request.response().bodyLoadedPromiseFulfill(null);
        this.requestIdToRequest.remove(request.requestId());
        this.attemptedAuthentications.remove(request.interceptionId());
        this.emit(Variables.Event.NETWORK_MANAGER_REQUEST_FINISHED.getName(), request);
    }

    public void onResponseReceived(ReceivedPayload event) {
        Request request = this.requestIdToRequest.get(event.getRequestId());
        if (request == null)
            return;
        Response response = new Response(this.client, request, event.getResponse());
        request.setResponse(response);
        this.emit(Variables.Event.NETWORK_MANAGER_RESPONSE.getName(), response);
    }

    public void onLoadingFailed(FailedPayload event) {
        Request request = this.requestIdToRequest.get(event.getRequestId());
        if (request == null)
            return;
        request.setFailureText(event.getErrorText());
        Response response = request.response();
        if (response != null)
            response.bodyLoadedPromiseFulfill(null);
        this.requestIdToRequest.remove(request.requestId());
        this.attemptedAuthentications.remove(request.interceptionId());
        this.emit(Variables.Event.NETWORK_MANAGER_REQUEST_FAILED.getName(), request);
    }

    public void onRequestServedFromCache(RequestCachePayload event) {
        Request request = this.requestIdToRequest.get(event.getRequestId());
        if (request != null)
            request.setFromMemoryCache(true);
    }

}
