package com.koudai.net.kernal;

import java.util.concurrent.TimeUnit;

/**
 * A shared connection pool that uses system properties for tuning parameters:
 *
 * <ul>
 *     <li>{@code http.keepAlive} true if HTTP and SPDY connections should be pooled at all. Default
 *         is true.
 *     <li>{@code http.maxConnections} maximum number of idle connections to each to keep in the
 *         pool. Default is 5.
 *     <li>{@code http.keepAliveDuration} Time in milliseconds to keep the connection alive in the
 *         pool before closing it. Default is 5 minutes. This property isn't used by {@code
 *         HttpURLConnection}.
 * </ul>
 *
 * <p>The default instance <i>doesn't</i> adjust its configuration as system properties are changed.
 * This assumes that the applications that set these parameters do so before making HTTP
 * connections, and that this class is initialized lazily.
 */
public final class SystemPropertiesConnectionPool {
    private static final long DEFAULT_KEEP_ALIVE_DURATION_MS = 5 * 60 * 1000; // 5 min

    public static final ConnectionPool INSTANCE;
    static {
        String keepAlive = System.getProperty("http.keepAlive");
        int maxIdleConnections;
        if (keepAlive != null && !Boolean.parseBoolean(keepAlive)) {
            maxIdleConnections = 0;
        } else {
            String maxIdleConnectionsString = System.getProperty("http.maxConnections");
            if (maxIdleConnectionsString != null) {
                maxIdleConnections = Integer.parseInt(maxIdleConnectionsString);
            } else {
                maxIdleConnections = 5;
            }
        }

        String keepAliveDurationString = System.getProperty("http.keepAliveDuration");
        long keepAliveDurationMs = keepAliveDurationString != null
                ? Long.parseLong(keepAliveDurationString)
                : DEFAULT_KEEP_ALIVE_DURATION_MS;

        INSTANCE = new ConnectionPool(maxIdleConnections, keepAliveDurationMs, TimeUnit.MILLISECONDS);
    }

    private SystemPropertiesConnectionPool() {
    }
}

