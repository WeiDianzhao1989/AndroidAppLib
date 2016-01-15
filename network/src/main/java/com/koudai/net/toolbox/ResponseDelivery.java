package com.koudai.net.toolbox;

import com.koudai.net.callback.ProgressCallback;
import com.koudai.net.error.NetworkError;

/**
 * Created by zhaoyu on 15/11/5.
 */
public interface ResponseDelivery {
    /**
     * Parses a response from the network or cache and delivers it.
     */
    <T> void postResponse(HttpRequest<T> request, T response);

    /**
     * Parses a response from the network or cache and delivers it. The provided
     * Runnable will be executed after delivery.
     */
    <T> void postResponse(HttpRequest<T> request, T response, Runnable runnable);

    /**
     * Posts an error for the given request.
     */
    <T> void postError(HttpRequest<T> request, NetworkError error);

    /**
     * Posts an cancel for the given request.
     */
    <T> void postCancel(HttpRequest<T> request);

    void postProgress(long total, long currentProgress, ProgressCallback callback);
}
