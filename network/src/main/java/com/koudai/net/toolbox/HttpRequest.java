package com.koudai.net.toolbox;

import android.text.TextUtils;

import com.koudai.net.callback.Callback;
import com.koudai.net.callback.RetryCallback;
import com.koudai.net.error.NetworkError;
import com.koudai.net.kernal.HttpUrl;
import com.koudai.net.kernal.RequestBody;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zhaoyu on 15/11/26.
 * 网络请求的基类
 */
public abstract class HttpRequest<T> implements Comparable<HttpRequest<?>> {

    //通用部分
    protected String url;
    protected RequestHeaders headers;
    protected RequestParams params;
    protected Charset encodeCharset;

    //请求控制相关
    protected String tag;
    protected final AtomicBoolean isFinished = new AtomicBoolean(false);
    protected final AtomicBoolean isSuccess = new AtomicBoolean(false);
    protected final AtomicBoolean isCanceled = new AtomicBoolean(false);
    protected int requestLevel;
    protected int priority;

    //重试先关
    protected int maxRetryTimesAfterFailed = NetworkFetcherGlobalParams.getInstance().getMaxRetryTimesAfterFailed();
    protected AtomicInteger retryTimes = new AtomicInteger(0);

    //响应回调相关
    protected Callback<T> callback;
    protected RetryCallback retryCallback;

    private volatile T response;
    private volatile NetworkError error;

    private RequestQueue requestQueue;

    protected long requestStartTime;

    protected IRequestHeaderInterceptor requestHeaderInterceptor;
    protected IRequestParamsInterceptor requestParamsInterceptor;

    private Map<String, String> monitorParams = new HashMap<String, String>();


    public Map<String, String> monitorParams() {
        return monitorParams;
    }

    public void monitor(String key, String value) {
        monitorParams.put(key, value);
    }

    public String url() {
        return url;
    }

    long requestStartTime() {
        return requestStartTime;
    }

    int maxRetryTimesAfterFailed() {
        return maxRetryTimesAfterFailed;
    }

    int retryTimes() {
        return retryTimes.get();
    }

    Callback<T> callback() {
        return callback;
    }

    RetryCallback retryCallback() {
        return retryCallback;
    }


    int requestLevel() {
        return requestLevel;
    }

    int priority() {
        return priority;
    }

    T response() {
        return response;
    }

    NetworkError error() {
        return error;
    }

    void addRetryTimes() {
        retryTimes.incrementAndGet();
    }

    void clearCallback() {
        callback = null;
    }

    void markCancel() {
        isCanceled.set(true);
    }

    void markSuccess() {
        isSuccess.set(true);
    }

    void markFinish() {
        isFinished.set(true);
    }

    public final boolean isCanceled() {
        return isCanceled.get();
    }


    public final boolean isFinished() {
        return isFinished.get();
    }

    public final boolean isSuccess() {
        return isSuccess.get();
    }

    public void setRequestQueue(RequestQueue requestQueue) {
        this.requestQueue = requestQueue;
    }


    void deliveryResponse(T response) {
        this.response = response;
    }

    void reportError(NetworkError error) {
        this.error = error;
    }

    void finish() {
        if (!isFinished()) {
            markFinish();
            requestQueue.finish(this);
            requestQueue = null;
        }
    }

    protected abstract String tag();

    /**
     * 请求的方法
     *
     * @return
     */
    protected abstract String httpMethod();

    /**
     * 构建请求URL
     *
     * @param params
     * @return
     */
    protected abstract HttpUrl buildUrl(Map<String, String> params);

    /**
     * 构建请求体
     *
     * @param params
     * @return
     */
    protected abstract RequestBody buildBody(Map<String, String> params);


    /**
     * 内部响应回调
     *
     * @return
     */
    protected abstract com.koudai.net.kernal.Callback buildResponseCallback();


    /**
     * 处理请求header的定制
     *
     * @return
     */
    protected Map<String, String> assembleHeaders() {
        if (headers != null) {
            IRequestHeaderInterceptor headersInterceptor =
                    requestHeaderInterceptor != null ?
                            requestHeaderInterceptor :
                            NetworkFetcherGlobalParams.getInstance().getRequestHeaderInterceptor();
            return headersInterceptor.interceptHeader(this.headers);
        } else {
            return CollectionUtils.EMPTY_STRING_MAP;
        }
    }

    /**
     * 处理请求参数
     *
     * @return
     */
    protected Map<String, String> assembleParams() {
        if (params != null) {
            IRequestParamsInterceptor paramsInterceptor =
                    requestParamsInterceptor != null ?
                            requestParamsInterceptor :
                            NetworkFetcherGlobalParams.getInstance().getRequestParamsInterceptor();

            return paramsInterceptor.interceptParams(this.params);
        } else {
            return CollectionUtils.EMPTY_STRING_MAP;
        }
    }

    @Override
    public int compareTo(HttpRequest<?> other) {
        if (other == null) return 1;

        int left = this.priority();
        int right = other.priority();

        if (left == right) {
            if (requestLevel == other.requestLevel) {
                return this.requestStartTime > other.requestStartTime ? -1 : 1;
            } else {
                return this.requestLevel > other.requestLevel ?
                        -1 : 1;
            }
        } else {
            return left > right ? -1 : 1;
        }
    }

    @Override
    public int hashCode() {
        String tag = tag();
        return TextUtils.isEmpty(tag) ? super.hashCode() : tag.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HttpRequest)) {
            return false;
        }

        HttpRequest other = (HttpRequest) obj;

        return TextUtils.equals(tag(), other.tag());
    }
}
