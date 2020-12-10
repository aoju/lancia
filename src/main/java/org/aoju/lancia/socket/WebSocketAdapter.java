package org.aoju.lancia.socket;

import org.aoju.bus.core.thread.NamedThreadFactory;
import org.aoju.bus.logger.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * This class default implements all methods of the WebSocketListener that can be overridden optionally when advances functionalities is needed.<br>
 **/
public abstract class WebSocketAdapter implements WebSocketListener {

    private Framedata pingFrame;
    /**
     * Attribute to sync on
     */
    private final Object syncConnectionLost = new Object();
    /**
     * Attribute which allows you to deactivate the Nagle's algorithm
     */
    private boolean tcpNoDelay;
    /**
     * Attribute which allows you to enable/disable the SO_REUSEADDR socket option.
     */
    private boolean reuseAddr;
    /**
     * Attribute for a service that triggers lost connection checking
     */
    private ScheduledExecutorService connectionLostCheckerService;
    /**
     * Attribute for a task that checks for lost connections
     */
    private ScheduledFuture connectionLostCheckerFuture;
    /**
     * Attribute for the lost connection check interval in nanoseconds
     */
    private long connectionLostTimeout = TimeUnit.SECONDS.toNanos(60);
    /**
     * Attribute to keep track if the WebSocket Server/Client is running/connected
     */
    private boolean websocketRunning = false;

    /**
     * Setter for the interval checking for lost connections
     * A value lower or equal 0 results in the check to be deactivated
     *
     * @param connectionLostTimeout the interval in seconds
     */
    public void setConnectionLostTimeout(int connectionLostTimeout) {
        synchronized (syncConnectionLost) {
            this.connectionLostTimeout = TimeUnit.SECONDS.toNanos(connectionLostTimeout);
            if (this.connectionLostTimeout <= 0) {
                Logger.trace("Connection lost timer stopped");
                cancelConnectionLostTimer();
                return;
            }
            if (this.websocketRunning) {
                Logger.trace("Connection lost timer restarted");
                //Reset all the pings
                try {
                    ArrayList<WebSocket> connections = new ArrayList<>(getConnections());
                    WebSocketImpl webSocketImpl;
                    for (WebSocket conn : connections) {
                        if (conn instanceof WebSocketImpl) {
                            webSocketImpl = (WebSocketImpl) conn;
                            webSocketImpl.updateLastPong();
                        }
                    }
                } catch (Exception e) {
                    Logger.error("Exception during connection lost restart", e);
                }
                restartConnectionLostTimer();
            }
        }
    }

    /**
     * Stop the connection lost timer
     */
    protected void stopConnectionLostTimer() {
        synchronized (syncConnectionLost) {
            if (connectionLostCheckerService != null || connectionLostCheckerFuture != null) {
                this.websocketRunning = false;
                Logger.trace("Connection lost timer stopped");
                cancelConnectionLostTimer();
            }
        }
    }

    /**
     * Start the connection lost timer
     */
    protected void startConnectionLostTimer() {
        synchronized (syncConnectionLost) {
            if (this.connectionLostTimeout <= 0) {
                Logger.trace("Connection lost timer deactivated");
                return;
            }
            Logger.trace("Connection lost timer started");
            this.websocketRunning = true;
            restartConnectionLostTimer();
        }
    }

    /**
     * This methods allows the reset of the connection lost timer in case of a changed parameter
     */
    private void restartConnectionLostTimer() {
        cancelConnectionLostTimer();
        connectionLostCheckerService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("connectionLostChecker"));
        Runnable connectionLostChecker = new Runnable() {
            private final ArrayList<WebSocket> connections = new ArrayList<>();

            @Override
            public void run() {
                connections.clear();
                try {
                    connections.addAll(getConnections());
                    long minimumPongTime = (long) (System.nanoTime() - (connectionLostTimeout * 1.5));
                    for (WebSocket conn : connections) {
                        executeConnectionLostDetection(conn, minimumPongTime);
                    }
                } catch (Exception e) {
                    //Ignore this exception
                }
                connections.clear();
            }
        };

        connectionLostCheckerFuture = connectionLostCheckerService.scheduleAtFixedRate(connectionLostChecker, connectionLostTimeout, connectionLostTimeout, TimeUnit.NANOSECONDS);
    }

    /**
     * Send a ping to the endpoint or close the connection since the other endpoint did not respond with a ping
     *
     * @param webSocket       the websocket instance
     * @param minimumPongTime the lowest/oldest allowable last pong time (in nanoTime) before we consider the connection to be lost
     */
    private void executeConnectionLostDetection(WebSocket webSocket, long minimumPongTime) {
        if (!(webSocket instanceof WebSocketImpl)) {
            return;
        }
        WebSocketImpl webSocketImpl = (WebSocketImpl) webSocket;
        if (webSocketImpl.getLastPong() < minimumPongTime) {
            Logger.trace("Closing connection due to no pong received: {}", webSocketImpl);
            webSocketImpl.closeConnection(Framedata.ABNORMAL_CLOSE, "The connection was closed because the other endpoint did not respond with a pong in time. For more information check: https://github.com/TooTallNate/Java-WebSocket/wiki/Lost-connection-detection");
        } else {
            if (webSocketImpl.isOpen()) {
                webSocketImpl.sendPing();
            } else {
                Logger.trace("Trying to ping a non open connection: {}", webSocketImpl);
            }
        }
    }

    /**
     * Getter to get all the currently available connections
     *
     * @return the currently available connections
     */
    protected abstract Collection<WebSocket> getConnections();

    /**
     * Cancel any running timer for the connection lost detection
     */
    private void cancelConnectionLostTimer() {
        if (connectionLostCheckerService != null) {
            connectionLostCheckerService.shutdownNow();
            connectionLostCheckerService = null;
        }
        if (connectionLostCheckerFuture != null) {
            connectionLostCheckerFuture.cancel(false);
            connectionLostCheckerFuture = null;
        }
    }

    /**
     * Tests if TCP_NODELAY is enabled.
     *
     * @return a boolean indicating whether or not TCP_NODELAY is enabled for new connections.
     */
    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    /**
     * Setter for tcpNoDelay
     * Enable/disable TCP_NODELAY (disable/enable Nagle's algorithm) for new connections
     *
     * @param tcpNoDelay true to enable TCP_NODELAY, false to disable.
     */
    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    /**
     * Tests Tests if SO_REUSEADDR is enabled.
     *
     * @return a boolean indicating whether or not SO_REUSEADDR is enabled.
     */
    public boolean isReuseAddr() {
        return reuseAddr;
    }

    /**
     * Setter for soReuseAddr
     * Enable/disable SO_REUSEADDR for the socket
     *
     * @param reuseAddr whether to enable or disable SO_REUSEADDR
     */
    public void setReuseAddr(boolean reuseAddr) {
        this.reuseAddr = reuseAddr;
    }

    /**
     * This default implementation will send a pong in response to the received ping.
     * The pong frame will have the same payload as the ping frame.
     *
     * @see WebSocketListener#onWebsocketPing(WebSocket, Framedata)
     */
    @Override
    public void onWebsocketPing(WebSocket conn, Framedata f) {
        conn.sendFrame(f);
    }

    /**
     * Default implementation for onPreparePing, returns a (cached) PingFrame that has no application data.
     *
     * @param conn The <tt>WebSocket</tt> connection from which the ping frame will be sent.
     * @return PingFrame to be sent.
     * @see WebSocketListener#onPreparePing(WebSocket)
     */
    @Override
    public Framedata onPreparePing(WebSocket conn) {
        if (pingFrame == null)
            pingFrame = new Framedata(HandshakeState.Opcode.PING);
        return pingFrame;
    }

}
