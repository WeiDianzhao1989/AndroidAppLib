package com.koudai.net.toolbox;

import android.content.Context;

import com.koudai.net.NetworkLibraryConstants;

/**
 * Created by zhaoyu on 15/11/5.
 * 网络库配置
 */
public final class NetworkFetcherConfiguration {

    private static final int DEFAULT_MAX_RETRY_TIMES_AFTER_FAILED = 2;

    private Context context;

    private int connectTimeOut;
    private int readTimeOut;
    private int writeTimeOut;
    private int maxRetryTimesAfterFailed;

    private IRequestParamsInterceptor requestParamsInterceptor;
    private IRequestHeaderInterceptor requestHeaderInterceptor;
    private IResponseProcessor responseProcessor;

    private NetworkFetcherConfiguration() {

    }

    private NetworkFetcherConfiguration(Builder builder) {
        this.context = builder.context;
        this.connectTimeOut = builder.connectTimeOut;
        this.readTimeOut = builder.readTimeOut;
        this.writeTimeOut = builder.writeTimeOut;
        this.maxRetryTimesAfterFailed = builder.maxRetryTimesAfterFailed;
        this.requestParamsInterceptor = builder.requestParamsInterceptor;
        this.requestHeaderInterceptor = builder.requestHeaderInterceptor;
        this.responseProcessor = builder.responseProcessor;

        if (this.connectTimeOut == 0) {
            connectTimeOut = NetworkLibraryConstants.DEFAULT_CONNECT_TIMEOUT;
        }

        if (this.readTimeOut == 0) {
            readTimeOut = NetworkLibraryConstants.DEFAULT_READ_TIMEOUT;
        }

        if (this.writeTimeOut == 0) {
            writeTimeOut = NetworkLibraryConstants.DEFAULT_WRITE_TIMEOUT;
        }

        if (this.requestParamsInterceptor == null) {
            this.requestParamsInterceptor = new SimpleRequestParamsInterceptor();
        }

        if (this.requestHeaderInterceptor == null) {
            this.requestHeaderInterceptor = new SimpleRequestHeaderInterceptor();
        }

        if (this.responseProcessor == null) {
            this.responseProcessor = new SimpleResponseProcessor();
        }

    }


    public Context getContext() {
        return context;
    }

    public int getConnectTimeOut() {
        return connectTimeOut;
    }

    public int getReadTimeOut() {
        return readTimeOut;
    }

    public int getWriteTimeOut() {
        return writeTimeOut;
    }

    public int getMaxRetryTimesAfterFailed() {
        return maxRetryTimesAfterFailed;
    }

    public IRequestParamsInterceptor getRequestParamsInterceptor() {
        return requestParamsInterceptor;
    }

    public IRequestHeaderInterceptor getRequestHeaderInterceptor() {
        return requestHeaderInterceptor;
    }

    public IResponseProcessor getResponseProcessor() {
        return responseProcessor;
    }

    public static final class Builder {
        private Context context;
        private int connectTimeOut;
        private int readTimeOut;
        private int writeTimeOut;
        private int maxRetryTimesAfterFailed = DEFAULT_MAX_RETRY_TIMES_AFTER_FAILED;

        private IRequestParamsInterceptor requestParamsInterceptor;
        private IRequestHeaderInterceptor requestHeaderInterceptor;
        private IResponseProcessor responseProcessor;

        public Builder context(Context context) {
            this.context = context;
            return this;
        }

        public Builder connectTimeOut(int connectTimeOut) {
            this.connectTimeOut = connectTimeOut;
            return this;
        }

        public Builder readTimeOut(int readTimeOut) {
            this.readTimeOut = readTimeOut;
            return this;
        }

        public Builder writeTimeOut(int writeTimeOut) {
            this.writeTimeOut = writeTimeOut;
            return this;
        }


        public Builder maxRetryTimesAfterFailed(int maxRetryTimesAfterFailed) {
            this.maxRetryTimesAfterFailed = maxRetryTimesAfterFailed;
            return this;
        }

        public Builder requestParamsInterceptor(IRequestParamsInterceptor requestParamsInterceptor) {
            this.requestParamsInterceptor = requestParamsInterceptor;
            return this;
        }

        public Builder requestHeaderInterceptor(IRequestHeaderInterceptor requestHeaderInterceptor) {
            this.requestHeaderInterceptor = requestHeaderInterceptor;
            return this;
        }

        public Builder responseProcessor(IResponseProcessor responseProcessor) {
            this.responseProcessor = responseProcessor;
            return this;
        }


        public NetworkFetcherConfiguration build() {
            return new NetworkFetcherConfiguration(this);
        }

    }

}
