package com.koudai.net.toolbox;

import android.text.TextUtils;

import com.koudai.net.NetworkLibraryConstants;
import com.koudai.net.error.NetworkError;
import com.koudai.net.error.RetryException;
import com.koudai.net.kernal.Call;
import com.koudai.net.kernal.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;

/**
 * Created by zhaoyu on 15/11/16.
 * 内部默认的请求回调接口
 */
final class DefaultResponseCallback<T> implements com.koudai.net.kernal.Callback {

    private HttpRequest<T> httpRequest;
    private Parser<T> parser;
    private IResponseProcessor responseProcessor;
    private StringBuilder log = new StringBuilder();

    public DefaultResponseCallback(HttpRequest<T> httpRequest,
                                   IResponseProcessor responseProcessor,
                                   Parser<T> parser) {
        this.httpRequest = httpRequest;
        this.responseProcessor = responseProcessor;
        this.parser = parser;
    }

    @Override
    public void onFailure(Call call, IOException e) throws RetryException {
        //TODO 无网络情况下走到这里是什么异常
        if (httpRequest.retryTimes() < httpRequest.maxRetryTimesAfterFailed()) {
            if (e instanceof SocketTimeoutException) {//socket 连接，读写超时重试
                throw new RetryException(e.getMessage());
            }
        }

        NetworkError error = new NetworkError(NetworkLibraryConstants.INTERNAL_ERROR,
                NetworkLibraryConstants.INTERNAL_ERROR, e.getMessage());
        httpRequest.reportError(error);
        DefaultResponseDelivery.getInstance().postError(httpRequest, error);
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException, RetryException {
        try {
            if (response != null) {
                int responseCode = response.code();
                if (response.isSuccessful()) {
                    if (response.headers() != null) {
                        log.setLength(0);
                        log.append("response header is ").append(response.headers().toString());
                        NetworkLog.getInstance().d(log.toString());
                    }

                    IResponseProcessor responseProcessor =
                            this.responseProcessor != null ?
                                    this.responseProcessor :
                                    NetworkFetcherGlobalParams.getInstance().getResponseProcessor();
                    if (parser != null) {
                        String responseContentStr = responseProcessor.process(httpRequest, response);

                        if (!TextUtils.isEmpty(responseContentStr)) {
                            JSONObject jsonObject = null;
                            JSONArray jsonArray = null;
                            T result;

                            try {

                                NetworkLog.getInstance().d(responseContentStr);

                                Object json = new JSONTokener(responseContentStr).nextValue();

                                if (json instanceof JSONObject) { //you have an jsonObject
                                    jsonObject = new JSONObject(responseContentStr);
                                    NetworkLog.getInstance().v(jsonObject.toString());
                                } else if (json instanceof JSONArray) { //you have an jsonArray
                                    jsonArray = new JSONArray(responseContentStr);
                                    NetworkLog.getInstance().v(jsonArray.toString());
                                } else {
                                    jsonObject = new JSONObject();
                                    jsonObject.put(NetworkLibraryConstants.CUSTOM_KEY, responseContentStr);
                                }


                                if (jsonObject != null) {
                                    result = parser.parse(jsonObject);
                                } else if (jsonArray != null) {
                                    result = parser.parse(jsonArray);
                                } else {
                                    log.setLength(0);
                                    log = new StringBuilder("parse response body error [");
                                    log.append(responseContentStr).append("]");
                                    NetworkLog.getInstance().d(log.toString());
                                    NetworkError error = new NetworkError(HttpURLConnection.HTTP_OK,
                                            NetworkLibraryConstants.RESPONSE_PARSE_ERROR, "return json has problem");
                                    httpRequest.reportError(error);
                                    DefaultResponseDelivery.getInstance().postError(httpRequest, error);
                                    return;
                                }

                                if (!httpRequest.isCanceled()) {
                                    httpRequest.markSuccess();
                                    httpRequest.deliveryResponse(result);
                                    DefaultResponseDelivery.getInstance().postResponse(httpRequest, result);
                                } else {
                                    DefaultResponseDelivery.getInstance().postCancel(httpRequest);
                                }
                            } catch (JSONException e) {
                                log.setLength(0);
                                log = new StringBuilder("parse response body error [");

                                if (jsonObject != null) {
                                    log.append(jsonObject.toString());
                                } else if (jsonArray != null) {
                                    log.append(jsonArray.toString());
                                }

                                log.append("]");
                                NetworkLog.getInstance().d(log.toString());

                                NetworkError error = new NetworkError(HttpURLConnection.HTTP_OK,
                                        NetworkLibraryConstants.RESPONSE_PARSE_ERROR, "parse json cause throw a exception");
                                httpRequest.reportError(error);
                                DefaultResponseDelivery.getInstance().postError(httpRequest, error);
                            } catch (Exception e) {
                                NetworkLog.getInstance().e(
                                        "process response failed", e
                                );
                                NetworkError error = new NetworkError(HttpURLConnection.HTTP_OK,
                                        NetworkLibraryConstants.INTERNAL_ERROR, e.getMessage());
                                httpRequest.reportError(error);
                                DefaultResponseDelivery.getInstance().postError(httpRequest, error);
                            }
                        } else {

                            log.setLength(0);
                            log = new StringBuilder("parse response body error [");
                            log.append(responseContentStr).append("]");

                            NetworkError error = new NetworkError(HttpURLConnection.HTTP_OK,
                                    NetworkLibraryConstants.INTERNAL_ERROR, "response body parse error");
                            httpRequest.reportError(error);
                            DefaultResponseDelivery.getInstance().postError(httpRequest, error);
                        }
                    } else {
                        httpRequest.markSuccess();
                        httpRequest.deliveryResponse(null);
                        DefaultResponseDelivery.getInstance().postResponse(httpRequest, null);
                    }

                } else if (responseCode == HttpURLConnection.HTTP_UNAVAILABLE
                        || responseCode == HttpURLConnection.HTTP_BAD_GATEWAY
                        || responseCode == HttpURLConnection.HTTP_NOT_IMPLEMENTED) { //服务器内部错误
                    //由于服务端原因，重试
                    log.setLength(0);
                    log = new StringBuilder("business server error")
                            .append("response_code=").append(responseCode)
                            .append("response_message [").append(response.message())
                            .append("]");
                    NetworkLog.getInstance().v(log.toString());

                    if (httpRequest.retryTimes() < httpRequest.maxRetryTimesAfterFailed()) {
                        throw new RetryException(responseCode, response.message());
                    } else {
                        NetworkError error = new NetworkError(responseCode, responseCode
                                , response.message());
                        httpRequest.reportError(error);
                        DefaultResponseDelivery.getInstance().postError(httpRequest, error);
                    }

                } else {

                    log.setLength(0);
                    log = new StringBuilder("business server error")
                            .append("response_code=").append(responseCode)
                            .append("response_message [").append(response.message())
                            .append("]");
                    NetworkLog.getInstance().v(log.toString());

                    NetworkError error = new NetworkError(responseCode, responseCode
                            , response.message());
                    httpRequest.reportError(error);
                    DefaultResponseDelivery.getInstance().postError(httpRequest, error);
                }
            }
        } finally {
            if (response != null && response.body() != null) {
                response.body().close();
            }
        }
    }

    @Override
    public void onCancel(Call call) {
        log.setLength(0);
        log.append("request ").append(httpRequest.url()).append(" has been canceled");
        NetworkLog.getInstance().d(log.toString());
        DefaultResponseDelivery.getInstance().postCancel(httpRequest);
    }
}
