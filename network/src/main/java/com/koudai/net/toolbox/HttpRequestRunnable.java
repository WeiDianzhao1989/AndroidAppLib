package com.koudai.net.toolbox;

import android.os.Looper;
import android.os.Process;
import android.text.TextUtils;

import com.koudai.net.NetworkLibraryConstants;
import com.koudai.net.callback.ProgressCallback;
import com.koudai.net.error.NetworkError;
import com.koudai.net.kernal.Call;
import com.koudai.net.kernal.Callback;
import com.koudai.net.kernal.HttpUrl;
import com.koudai.net.kernal.OkHttpClient;
import com.koudai.net.kernal.Request;
import com.koudai.net.kernal.RequestBody;

import java.util.Map;

/**
 * Created by zhaoyu on 15/12/27.
 * 每次网络请求的通用执行模板类
 */
class HttpRequestRunnable implements Runnable, Comparable<HttpRequestRunnable> {

    private HttpRequest<?> httpRequest;
    private Call httpRequestCall = null;

    public HttpRequestRunnable(HttpRequest<?> httpRequest) {
        if (httpRequest == null) {
            throw new IllegalArgumentException("please supply request to execute");
        }
        this.httpRequest = httpRequest;
    }

    public HttpRequestRunnable() {
    }

    @Override
    public final void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        execute();
    }

    protected void execute() {

        //1、保证每个请求都是在工作者线程，非UI线程执行
        if (Looper.myLooper() == Looper.getMainLooper()) {
            httpRequest.finish();
            throw new IllegalStateException("shouldn't directly execute this method on UI");
        }

        boolean isNeedRetry = false;

        do {
            //2、检查手机网络是否开启
            if (!NetUtil.testNetworkConnection()) {
                NetworkError error = new NetworkError(
                        NetworkLibraryConstants.CONNECT_FAILED_ERROR,
                        NetworkLibraryConstants.CONNECT_FAILED_ERROR,
                        "network connection failed"
                );
                DefaultResponseDelivery.getInstance().postError(httpRequest, error);
                return;
            }

            if (isNeedRetry && httpRequest.retryCallback != null) { // 需要重试，给予一次回调修改request请求的机会
                HttpRequest<?> newHttpRequest = httpRequest.retryCallback().onRetry(httpRequest);
                if (newHttpRequest != null) {
                    this.httpRequest = newHttpRequest;
                }
            }


            if (TextUtils.isEmpty(httpRequest.url())) {
                throw new IllegalArgumentException("url is null");
            }

            StringBuilder logSb = new StringBuilder(1024);

            Request.Builder requestBuilder = new Request.Builder();

            logSb.append("request url is [").append(httpRequest.url()).append("]");
            NetworkLog.getInstance().d(logSb.toString());


            logSb.setLength(0);
            logSb.append("request params are [")
                    .append(httpRequest.params.toString()).append("]");
            NetworkLog.getInstance().d(logSb.toString());


            //3、调用request获取header头的最终设置
            final Map<String, String> finalHeaders = httpRequest.assembleHeaders();

            //4、调用request获取请求的参数
            final Map<String, String> finalParams = httpRequest.assembleParams();


            // 5、组装请求的header头
            if (!CollectionUtils.isMapEmpty(finalHeaders)) {
                for (Map.Entry<String, String> entry : finalHeaders.entrySet()) {
                    if (!TextUtils.isEmpty(entry.getKey()) && !TextUtils.isEmpty(entry.getValue())) {
                        requestBuilder.header(entry.getKey(), entry.getValue());
                    }
                }

                logSb.setLength(0);
                logSb.append("request header is [")
                        .append(finalHeaders.toString())
                        .append("]");

                NetworkLog.getInstance().d(logSb.toString());
            }

            // 6、构建请求的Url
            HttpUrl httpUrl = httpRequest.buildUrl(finalParams);

            if (httpUrl == null)
                throw new IllegalStateException("build url failed,please check url or params");

            logSb.setLength(0);
            logSb.append("get request final url is [").append(httpUrl.toString()).append("]");

            // 7、请求的方法类型
            String httpMethod = httpRequest.httpMethod();

            // 8、构建请求体
            RequestBody requestBody = httpRequest.buildBody(finalParams);

            // 9、请求的tag标识
            String tag = httpRequest.tag();


            if (httpRequest.isCanceled()) {
                logSb.setLength(0);
                logSb.append("request is canceled");
                NetworkLog.getInstance().v(logSb.toString());
                DefaultResponseDelivery.getInstance().postCancel(httpRequest);
                return;
            }

            final com.koudai.net.kernal.Request request = requestBuilder
                    .method(httpMethod, requestBody)
                    .url(httpUrl)
                    .tag(tag)
                    .build();

            // 10、创建内部响应回调
            Callback responseCallback = httpRequest.buildResponseCallback();

            // 11、创建内部网络调用过程
            httpRequestCall = createInternalCall(request);
            // 12、执行网络请求
            isNeedRetry = httpRequestCall.execute(responseCallback);
            if (!isNeedRetry) {
                break;
            }
            // 13、添加重试次数
            httpRequest.addRetryTimes();
        } while (httpRequest.retryTimes() < httpRequest.maxRetryTimesAfterFailed());
    }

    protected Call createInternalCall(Request request) {
        OkHttpClient httpClient = OkHttpClientFactory.createOkHttpClient();
        if (httpRequest instanceof FileDownloadRequest) {
            return httpClient.newCall(request, (ProgressCallback) httpRequest.callback());
        } else {
            return httpClient.newCall(request);
        }
    }

    public HttpRequest<?> getHttpRequest() {
        return httpRequest;
    }

    public boolean isFinished() {
        return httpRequest.isFinished();
    }

    public boolean isSuccess() {
        return httpRequest.isSuccess();
    }

    public boolean isCanceled() {
        return httpRequest.isCanceled();
    }

    public void cancel() {
        cancel(true);
    }

    /**
     * 取消请求
     *
     * @param isNotifyObserver 是否通知外界取消操作
     */
    public void cancel(boolean isNotifyObserver) {
        if (!httpRequest.isFinished()) {
            httpRequest.markCancel();

            if (!isNotifyObserver) {
                httpRequest.clearCallback();
            }

            if (httpRequestCall != null && !httpRequestCall.isCanceled()) {
                httpRequestCall.cancel();
            }
        }
    }

    @Override
    public int compareTo(HttpRequestRunnable other) {
        if (other == null) return 1;

        HttpRequest<?> left = getHttpRequest();
        HttpRequest<?> right = other.getHttpRequest();

        if (left == right) return 0;
        if (left == null) return -1;
        if (right == null) return 1;

        return left.compareTo(right);
    }
}
