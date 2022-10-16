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

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.lancia.Builder;
import org.aoju.lancia.events.DefaultBrowserListener;
import org.aoju.lancia.events.EventEmitter;
import org.aoju.lancia.events.Events;
import org.aoju.lancia.nimble.fetch.AuthRequiredPayload;
import org.aoju.lancia.nimble.fetch.RequestPausedPayload;
import org.aoju.lancia.nimble.network.*;
import org.aoju.lancia.nimble.webAuthn.Credentials;
import org.aoju.lancia.worker.CDPSession;

import java.util.*;

/**
 * 网络管理
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class NetworkManager extends EventEmitter {

    private final CDPSession client;

    private final boolean ignoreHTTPSErrors;

    private final FrameManager frameManager;
    private final Map<String, Request> requestIdToRequest;
    private final Map<String, RequestWillBeSentPayload> requestIdToRequestWillBeSentEvent;
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

        DefaultBrowserListener<RequestPausedPayload> requestPausedListener = new DefaultBrowserListener<RequestPausedPayload>() {
            @Override
            public void onBrowserEvent(RequestPausedPayload event) {
                NetworkManager manager = (NetworkManager) this.getTarget();
                manager.onRequestPaused(event);
            }
        };
        requestPausedListener.setMethod("Fetch.requestPaused");
        requestPausedListener.setTarget(this);
        this.client.addListener(requestPausedListener.getMethod(), requestPausedListener);

        DefaultBrowserListener<AuthRequiredPayload> authRequiredListener = new DefaultBrowserListener<AuthRequiredPayload>() {
            @Override
            public void onBrowserEvent(AuthRequiredPayload event) {
                NetworkManager manager = (NetworkManager) this.getTarget();
                manager.onAuthRequired(event);
            }
        };
        authRequiredListener.setMethod("Fetch.authRequired");
        authRequiredListener.setTarget(this);
        this.client.addListener(authRequiredListener.getMethod(), authRequiredListener);

        DefaultBrowserListener<RequestWillBeSentPayload> requestWillBeSentListener = new DefaultBrowserListener<RequestWillBeSentPayload>() {
            @Override
            public void onBrowserEvent(RequestWillBeSentPayload event) {
                NetworkManager manager = (NetworkManager) this.getTarget();
                manager.onRequestWillBeSent(event);
            }
        };
        requestWillBeSentListener.setMethod("Network.requestWillBeSent");
        requestWillBeSentListener.setTarget(this);
        this.client.addListener(requestWillBeSentListener.getMethod(), requestWillBeSentListener);

        DefaultBrowserListener<RequestServedFromCachePayload> requestServedFromCacheListener = new DefaultBrowserListener<RequestServedFromCachePayload>() {
            @Override
            public void onBrowserEvent(RequestServedFromCachePayload event) {
                NetworkManager manager = (NetworkManager) this.getTarget();
                manager.onRequestServedFromCache(event);
            }
        };
        requestServedFromCacheListener.setMethod("Network.requestServedFromCache");
        requestServedFromCacheListener.setTarget(this);
        this.client.addListener(requestServedFromCacheListener.getMethod(), requestServedFromCacheListener);

        DefaultBrowserListener<ResponseReceivedPayload> responseReceivedListener = new DefaultBrowserListener<ResponseReceivedPayload>() {
            @Override
            public void onBrowserEvent(ResponseReceivedPayload event) {
                NetworkManager manager = (NetworkManager) this.getTarget();
                manager.onResponseReceived(event);
            }
        };
        responseReceivedListener.setMethod("Network.responseReceived");
        responseReceivedListener.setTarget(this);
        this.client.addListener(responseReceivedListener.getMethod(), responseReceivedListener);

        DefaultBrowserListener<LoadingFinishedPayload> loadingFinishedListener = new DefaultBrowserListener<LoadingFinishedPayload>() {
            @Override
            public void onBrowserEvent(LoadingFinishedPayload event) {
                NetworkManager manager = (NetworkManager) this.getTarget();
                manager.onLoadingFinished(event);
            }
        };
        loadingFinishedListener.setMethod("Network.loadingFinished");
        loadingFinishedListener.setTarget(this);
        this.client.addListener(loadingFinishedListener.getMethod(), loadingFinishedListener);

        DefaultBrowserListener<LoadingFailedPayload> loadingFailedListener = new DefaultBrowserListener<LoadingFailedPayload>() {
            @Override
            public void onBrowserEvent(LoadingFailedPayload event) {
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
        boolean enabled = this.userRequestInterceptionEnabled || this.credentials != null;
        if (enabled == this.protocolRequestInterceptionEnabled)
            return;
        this.protocolRequestInterceptionEnabled = enabled;
        this.updateProtocolCacheDisabled();
        if (enabled) {
            Map<String, Object> params = new HashMap<>();
            params.put("handleAuthRequests", true);
            List<Object> patterns = new ArrayList<>();
            patterns.add(org.aoju.lancia.Builder.OBJECTMAPPER.createObjectNode().put("urlPattern", "*"));
            params.put("patterns", patterns);
            this.client.send("Fetch.enable", params, true);
        } else {
            this.client.send("Fetch.disable", null, true);
        }
    }

    public void onRequestWillBeSent(RequestWillBeSentPayload event) {
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

    public void onAuthRequired(AuthRequiredPayload event) {
        /* @type {"Default"|"CancelAuth"|"ProvideCredentials"} */
        String response = "Default";
        if (this.attemptedAuthentications.contains(event.getRequestId())) {
            response = "CancelAuth";
        } else if (this.credentials != null) {
            response = "ProvideCredentials";
            this.attemptedAuthentications.add(event.getRequestId());
        }
        String username, password;
        ObjectNode respParams = org.aoju.lancia.Builder.OBJECTMAPPER.createObjectNode();
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
            RequestWillBeSentPayload requestWillBeSentEvent = this.requestIdToRequestWillBeSentEvent.get(requestId);
            this.onRequest(requestWillBeSentEvent, interceptionId);
            this.requestIdToRequestWillBeSentEvent.remove(requestId);
        } else {
            this.requestIdToInterceptionId.put(requestId, interceptionId);
        }
    }

    public void onRequest(RequestWillBeSentPayload event, String interceptionId) {
        List<Request> redirectChain = new ArrayList<>();
        if (event.getRedirectResponse() != null) {
            Request request = this.requestIdToRequest.get(event.getRequestId());
            // If we connect late to the target, we could have missed the requestWillBeSent event.
            if (request != null) {
                this.handleRequestRedirect(request, event.getRedirectResponse());
                redirectChain = request.redirectChain();
            }
        }
        Frame frame = StringKit.isNotEmpty(event.getFrameId()) ? this.frameManager.getFrame(event.getFrameId()) : null;
        Request request = new Request(this.client, frame, interceptionId, this.userRequestInterceptionEnabled, event, redirectChain);
        this.requestIdToRequest.put(event.getRequestId(), request);
        this.emit(Events.NETWORK_MANAGER_REQUEST.getName(), request);
    }

    private void handleRequestRedirect(Request request, ResponsePayload responsePayload) {
        Response response = new Response(this.client, request, responsePayload);
        request.setResponse(response);
        request.redirectChain().add(request);
        response.resolveBody("Response body is unavailable for redirect responses");
        this.requestIdToRequest.remove(request.requestId());
        this.attemptedAuthentications.remove(request.interceptionId());
        this.emit(Events.NETWORK_MANAGER_RESPONSE.getName(), response);
        this.emit(Events.NETWORK_MANAGER_REQUEST_FINISHED.getName(), request);
    }

    public void onLoadingFinished(LoadingFinishedPayload event) {
        Request request = this.requestIdToRequest.get(event.getRequestId());
        // For certain requestIds we never receive requestWillBeSent event.
        // @see https://crbug.com/750469
        if (request == null)
            return;

        // Under certain conditions we never get the Network.responseReceived
        // event from protocol. @see https://crbug.com/883475
        if (request.response() != null)
            request.response().bodyLoadedPromiseFulfill(null);
        this.requestIdToRequest.remove(request.requestId());
        this.attemptedAuthentications.remove(request.interceptionId());
        this.emit(Events.NETWORK_MANAGER_REQUEST_FINISHED.getName(), request);
    }

    public void onResponseReceived(ResponseReceivedPayload event) {
        Request request = this.requestIdToRequest.get(event.getRequestId());
        // FileUpload sends a response without a matching request.
        if (request == null)
            return;
        Response response = new Response(this.client, request, event.getResponse());
        request.setResponse(response);
        this.emit(Events.NETWORK_MANAGER_RESPONSE.getName(), response);
    }

    public void onLoadingFailed(LoadingFailedPayload event) {
        Request request = this.requestIdToRequest.get(event.getRequestId());
        // For certain requestIds we never receive requestWillBeSent event.
        // @see https://crbug.com/750469
        if (request == null)
            return;
        request.setFailureText(event.getErrorText());
        Response response = request.response();
        if (response != null)
            response.bodyLoadedPromiseFulfill(null);
        this.requestIdToRequest.remove(request.requestId());
        this.attemptedAuthentications.remove(request.interceptionId());
        this.emit(Events.NETWORK_MANAGER_REQUEST_FAILED.getName(), request);
    }

    public void onRequestServedFromCache(RequestServedFromCachePayload event) {
        Request request = this.requestIdToRequest.get(event.getRequestId());
        if (request != null)
            request.setFromMemoryCache(true);
    }

}
