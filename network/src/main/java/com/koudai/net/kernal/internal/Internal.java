
package com.koudai.net.kernal.internal;

import com.koudai.net.kernal.Address;
import com.koudai.net.kernal.Call;
import com.koudai.net.kernal.ConnectionPool;
import com.koudai.net.kernal.ConnectionSpec;
import com.koudai.net.kernal.Headers;
import com.koudai.net.kernal.HttpUrl;
import com.koudai.net.kernal.OkHttpClient;
import com.koudai.net.kernal.internal.http.StreamAllocation;
import com.koudai.net.kernal.internal.io.RealConnection;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocket;

/**
 * Escalate internal APIs in {@code okhttp3} so they can be used from OkHttp's implementation
 * packages. The only implementation of this interface is in {@link OkHttpClient}.
 */
public abstract class Internal {
    public static final Logger logger = Logger.getLogger(OkHttpClient.class.getName());

    public static void initializeInstanceForTests() {
        // Needed in tests to ensure that the instance is actually pointing to something.
        new OkHttpClient();
    }

    public static Internal instance;

    public abstract void addLenient(Headers.Builder builder, String line);

    public abstract void addLenient(Headers.Builder builder, String name, String value);

    public abstract void setCache(OkHttpClient.Builder builder, InternalCache internalCache);

    public abstract InternalCache internalCache(OkHttpClient client);

    public abstract RealConnection get(
            ConnectionPool pool, Address address, StreamAllocation streamAllocation);

    public abstract void put(ConnectionPool pool, RealConnection connection);

    public abstract boolean connectionBecameIdle(ConnectionPool pool, RealConnection connection);

    public abstract RouteDatabase routeDatabase(ConnectionPool connectionPool);

    public abstract void apply(ConnectionSpec tlsConfiguration, SSLSocket sslSocket,
                               boolean isFallback);

    public abstract HttpUrl getHttpUrlChecked(String url)
            throws MalformedURLException, UnknownHostException;

    // TODO delete the following when web sockets move into the main package.
    //public abstract void callEnqueue(Call call, Callback responseCallback, boolean forWebSocket);

    public abstract StreamAllocation callEngineGetStreamAllocation(Call call);
}


