package com.koudai.net.toolbox;

import android.content.Context;

import com.koudai.net.NetworkLibraryConstants;
import com.koudai.net.io.ByteArrayPool;

/**
 * Created by zhaoyu on 15/12/25.
 */
public class NetworkFetcherGlobalParams {

    private static final class NetworkFetcherGlobalParamsHolder {
        public static final NetworkFetcherGlobalParams instance = new NetworkFetcherGlobalParams();
    }

    private ByteArrayPool byteArrayPool = new ByteArrayPool(1024 * 512);

    private Context context;
    private IRequestParamsInterceptor requestParamsInterceptor;
    private IRequestHeaderInterceptor requestHeaderInterceptor;
    private IResponseProcessor responseProcessor;

    private long defaultConnectionTimeout = NetworkLibraryConstants.DEFAULT_CONNECT_TIMEOUT;
    private long defaultReadTimeout = NetworkLibraryConstants.DEFAULT_READ_TIMEOUT;
    private long defaultWriteTimeout = NetworkLibraryConstants.DEFAULT_WRITE_TIMEOUT;

    private long connectionTimeout;
    private long readTimeout;
    private long writeTimeout;

    private int maxRetryTimesAfterFailed;

    public static NetworkFetcherGlobalParams getInstance() {
        return NetworkFetcherGlobalParamsHolder.instance;
    }

    private NetworkFetcherGlobalParams() {

    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public IRequestParamsInterceptor getRequestParamsInterceptor() {
        return requestParamsInterceptor;
    }

    public void setRequestParamsInterceptor(IRequestParamsInterceptor requestParamsInterceptor) {
        this.requestParamsInterceptor = requestParamsInterceptor;
    }

    public IRequestHeaderInterceptor getRequestHeaderInterceptor() {
        return requestHeaderInterceptor;
    }

    public void setRequestHeaderInterceptor(IRequestHeaderInterceptor requestHeaderInterceptor) {
        this.requestHeaderInterceptor = requestHeaderInterceptor;
    }


    public IResponseProcessor getResponseProcessor() {
        return responseProcessor;
    }

    public void setResponseProcessor(IResponseProcessor responseProcessor) {
        this.responseProcessor = responseProcessor;
    }

    public long getConnectionTimeout() {
        return connectionTimeout == 0 ? defaultConnectionTimeout : connectionTimeout;
    }

    public void setConnectionTimeout(long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public long getReadTimeout() {
        return readTimeout == 0 ? defaultReadTimeout : readTimeout;
    }

    public void setReadTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
    }

    public long getWriteTimeout() {
        return writeTimeout == 0 ? defaultWriteTimeout : defaultWriteTimeout;
    }

    public void setWriteTimeout(long writeTimeout) {
        this.writeTimeout = writeTimeout;
    }


    public long getDefaultConnectionTimeout() {
        return defaultConnectionTimeout;
    }

    public void setDefaultConnectionTimeout(long defaultConnectionTimeout) {
        this.defaultConnectionTimeout = defaultConnectionTimeout;
    }

    public long getDefaultReadTimeout() {
        return defaultReadTimeout;
    }

    public void setDefaultReadTimeout(long defaultReadTimeout) {
        this.defaultReadTimeout = defaultReadTimeout;
    }

    public long getDefaultWriteTimeout() {
        return defaultWriteTimeout;
    }

    public void setDefaultWriteTimeout(long defaultWriteTimeout) {
        this.defaultWriteTimeout = defaultWriteTimeout;
    }

    public int getMaxRetryTimesAfterFailed() {
        return maxRetryTimesAfterFailed;
    }

    public void setMaxRetryTimesAfterFailed(int maxRetryTimesAfterFailed) {
        this.maxRetryTimesAfterFailed = maxRetryTimesAfterFailed;
    }

    public ByteArrayPool getByteArrayPool() {
        return byteArrayPool;
    }
}
