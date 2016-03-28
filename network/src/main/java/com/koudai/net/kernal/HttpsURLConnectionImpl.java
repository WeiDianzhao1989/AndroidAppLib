
package com.koudai.net.kernal;

import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

public final class HttpsURLConnectionImpl extends DelegatingHttpsURLConnection {
    private final HttpURLConnectionImpl delegate;

    public HttpsURLConnectionImpl(URL url, OkHttpClient client) {
        this(new HttpURLConnectionImpl(url, client));
    }

    public HttpsURLConnectionImpl(URL url, OkHttpClient client, URLFilter filter) {
        this(new HttpURLConnectionImpl(url, client, filter));
    }

    public HttpsURLConnectionImpl(HttpURLConnectionImpl delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    protected Handshake handshake() {
        if (delegate.httpEngine == null) {
            throw new IllegalStateException("Connection has not yet been established");
        }

        // If there's a response, get the handshake from there so that caching
        // works. Otherwise get the handshake from the connection because we might
        // have not connected yet.
        return delegate.httpEngine.hasResponse()
                ? delegate.httpEngine.getResponse().handshake()
                : delegate.handshake;
    }

    @Override
    public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        delegate.client = delegate.client.newBuilder()
                .hostnameVerifier(hostnameVerifier)
                .build();
    }

    @Override
    public HostnameVerifier getHostnameVerifier() {
        return delegate.client.hostnameVerifier();
    }

    @Override
    public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
        delegate.client = delegate.client.newBuilder()
                .sslSocketFactory(sslSocketFactory)
                .build();
    }

    @Override
    public SSLSocketFactory getSSLSocketFactory() {
        return delegate.client.sslSocketFactory();
    }

//    @Override public long getContentLengthLong() {
//        return delegate.getContentLengthLong();
//    }

//    @Override public void setFixedLengthStreamingMode(long contentLength) {
//        delegate.setFixedLengthStreamingMode(contentLength);
//    }
//
//    @Override public long getHeaderFieldLong(String field, long defaultValue) {
//        return delegate.getHeaderFieldLong(field, defaultValue);
//    }
}