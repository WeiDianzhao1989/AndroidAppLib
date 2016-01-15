package com.koudai.net.toolbox;

import android.text.TextUtils;

import com.koudai.net.NetworkLibraryConstants;
import com.koudai.net.callback.Callback;
import com.koudai.net.kernal.FormBody;
import com.koudai.net.kernal.HttpUrl;
import com.koudai.net.kernal.RequestBody;
import com.koudai.net.kernal.internal.Util;
import com.koudai.net.netutils.CollectionUtils;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * Created by zhaoyu on 15/11/2.
 * post请求的封装类，发post请求，通过Builder构建，然后放入Dispatcher
 */
public final class HttpPostRequest<T> extends HttpRequest<T> {

    private final RequestParams nonEncryptParams;//不需要加密的请求
    private final IResponseProcessor responseProcessor;
    private final Parser<T> parser;


    private HttpPostRequest(Builder<T> builder) {
        this.url = builder.url;
        this.headers = builder.headers != null ? builder.headers : new RequestHeaders();
        this.params = builder.params != null ? builder.params : new RequestParams();
        this.nonEncryptParams = builder.nonEncryptParams != null ? builder.nonEncryptParams : new RequestParams();
        this.encodeCharset = builder.encodeCharset;
        this.requestLevel = builder.requestLevel;
        this.callback = builder.callback;
        this.parser = builder.parser;
        this.priority = builder.priority;

        this.requestHeaderInterceptor = builder.requestHeaderInterceptor;
        this.requestParamsInterceptor = builder.requestParamsInterceptor;
        this.responseProcessor = builder.responseProcessor;

        if (builder.retryTimesAfterFailed >= 0) {
            this.maxRetryTimesAfterFailed =
                    builder.retryTimesAfterFailed;
        }

        this.timestamp = System.currentTimeMillis();
    }

    @Override
    protected String httpMethod() {
        return NetworkLibraryConstants.POST;
    }

    @Override
    protected HttpUrl buildUrl(Map<String, String> params) {
        return HttpUrl.parse(this.url);
    }

    @Override
    protected RequestBody buildBody(Map<String, String> params) {
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        //try {
        if (!CollectionUtils.isMapEmpty(params)) { //目前请求体不进行gzip压缩
            // 压入其他不需要加密的参数
            if (nonEncryptParams.toMap().size() > 0) {
                params.putAll(nonEncryptParams.toMap());
            }

            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (!TextUtils.isEmpty(entry.getKey()) && !TextUtils.isEmpty(entry.getValue())) {
                    formBodyBuilder.add(entry.getKey(), entry.getValue());
                }
            }
        }

        //}
//        catch (UnsupportedEncodingException e) {
//            if (this.params != null) {
//                AppMonitorAgaent.reportError(new StringBuilder("encode request params error [").append(
//                        this.params.toString())
//                        .append("]").toString());
//            } else {
//                AppMonitorAgaent.reportError("encode request params error");
//            }
//        }
        return formBodyBuilder.build();
    }

    @Override
    protected com.koudai.net.kernal.Callback buildResponseCallback() {
        return new DefaultResponseCallback<T>(this, responseProcessor,parser);
    }

    @Override
    public String tag() {
        if (TextUtils.isEmpty(tag)) {
            int result = 0;

            if (!TextUtils.isEmpty(url)) {
                result = this.url.hashCode();
            }

            if (this.params != null) {
                result += this.params.hashCode();
            }

            if (this.nonEncryptParams != null) {
                result += this.nonEncryptParams.hashCode();
            }

            if (this.headers != null) {
                result += this.headers.hashCode();
            }

            tag = String.valueOf(result);
        }

        return tag;
    }

    /**
     * 构建请求
     *
     * @param <T> 响应的实体类型
     */
    public static final class Builder<T> {

        private String url;
        private RequestHeaders headers;
        private RequestParams params;
        private RequestParams nonEncryptParams;

        private Charset encodeCharset = Util.UTF_8;

        private Callback<T> callback;
        private Parser<T> parser;
        private int retryTimesAfterFailed = -1;

        private int requestLevel = Level.MAIN_TASK;
        private int priority = 0;

        private IRequestHeaderInterceptor requestHeaderInterceptor;
        private IRequestParamsInterceptor requestParamsInterceptor;
        private IResponseProcessor responseProcessor;

        public Builder<T> url(String url) {
            this.url = url;
            return this;
        }

        public Builder<T> headers(RequestHeaders headers) {
            this.headers = headers;
            return this;
        }

        public Builder<T> params(RequestParams parmas) {
            this.params = parmas;
            return this;
        }

        public Builder<T> nonEncryptParams(RequestParams nonEncryptParams) {
            this.nonEncryptParams = nonEncryptParams;
            return this;
        }

        public Builder<T> charset(Charset encodeCharset) {
            this.encodeCharset = encodeCharset;
            return this;
        }

        public Builder<T> callback(Callback<T> callback) {
            this.callback = callback;
            return this;
        }

        public Builder<T> parser(Parser<T> parser) {
            this.parser = parser;
            return this;
        }

        public Builder<T> requestLevel(int requestLevel) {
            if (requestLevel < 0 || requestLevel > Level.MAIN_TASK)
                throw new IllegalArgumentException("level is a invalid value");
            this.requestLevel = requestLevel;
            return this;
        }

        public Builder<T> retryTimesAfterFailed(int retryTimesAfterFailed) {
            this.retryTimesAfterFailed = retryTimesAfterFailed;
            return this;
        }

        public Builder<T> priority(int priority) {
            this.priority = priority;
            return this;
        }

        public Builder requestHeaderInterceptor(IRequestHeaderInterceptor requestHeaderInterceptor) {
            this.requestHeaderInterceptor = requestHeaderInterceptor;
            return this;
        }

        public Builder requestParamsInterceptor(IRequestParamsInterceptor requestParamsInterceptor) {
            this.requestParamsInterceptor = requestParamsInterceptor;
            return this;
        }

        public Builder responseProcessor(IResponseProcessor responseProcessor) {
            this.responseProcessor = responseProcessor;
            return this;
        }

        public HttpPostRequest<T> build() {
            return new HttpPostRequest<T>(this);
        }
    }

}
