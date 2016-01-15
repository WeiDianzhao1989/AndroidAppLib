package com.koudai.net.toolbox;

import android.os.Handler;
import android.os.Looper;

import com.koudai.net.callback.Callback;
import com.koudai.net.callback.ProgressCallback;
import com.koudai.net.error.NetworkError;

import java.util.concurrent.Executor;

/**
 * Created by zhaoyu on 15/11/5.
 * 负责分发响应
 */
public final class DefaultResponseDelivery implements ResponseDelivery {


    public static final class DefaultResponseDeliveryHolder {
        public static final ResponseDelivery instance = new DefaultResponseDelivery();
    }

    /**
     * Used for posting responses, typically to the main thread.
     */
    private final Executor mResponsePoster;

    /**
     * Used for posting responses to UI Thread
     */
    private final Handler handler = new Handler(Looper.getMainLooper());


    public static ResponseDelivery getInstance() {
        return DefaultResponseDeliveryHolder.instance;
    }

    /**
     * Creates a new response delivery interface.
     */
    private DefaultResponseDelivery() {
        // Make an Executor that just wraps the handler.
        mResponsePoster = new Executor() {
            @Override
            public void execute(Runnable command) {
                handler.post(command);
            }
        };
    }

    @Override
    public <T> void postResponse(HttpRequest<T> request, T response) {
        postResponse(request, response, null);
    }

    @Override
    public <T> void postResponse(HttpRequest<T> request, T response, Runnable runnable) {
        mResponsePoster.execute(new ResponseDeliveryRunnable<T>(request, response, runnable));
    }

    @Override
    public <T> void postError(HttpRequest<T> request, NetworkError error) {
        mResponsePoster.execute(new ErrorDeliveryRunnable<T>(request, error));
    }

    @Override
    public <T> void postCancel(HttpRequest<T> request) {
        mResponsePoster.execute(new CancelDeliveryRunnable<T>(request));
    }

    @Override
    public void postProgress(long total, long current, ProgressCallback callback) {
        mResponsePoster.execute(new ProgressDeliveryRunnable(total, current, callback));
    }

    /**
     * A Runnable used for delivering network responses to a listener on the
     * main thread.
     */
    @SuppressWarnings("rawtypes")
    private class ResponseDeliveryRunnable<T> implements Runnable {
        private final T response;
        private final Runnable runnable;
        private final HttpRequest<T> request;

        public ResponseDeliveryRunnable(HttpRequest<T> request, T response, Runnable runnable) {
            this.response = response;
            this.runnable = runnable;
            this.request = request;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            if (!request.isCanceled()) {
                Callback<T> callback = request.callback();
                if (callback != null) {
                    callback.onResponse(response);
                }

                // If we have been provided a post-delivery runnable, run it.
                if (runnable != null) {
                    runnable.run();
                }

                request.finish();
            } else {
                postCancel(request);
            }
        }
    }

    private class ErrorDeliveryRunnable<T> implements Runnable {

        private final NetworkError error;
        private final HttpRequest<T> request;

        public ErrorDeliveryRunnable(
                HttpRequest<T> request, NetworkError error) {
            this.error = error;
            this.request = request;
        }

        @Override
        public void run() {
            Callback<T> callback = request.callback();
            if (callback != null) {
                callback.onError(error);
            }
            request.finish();
        }
    }

    private class CancelDeliveryRunnable<T> implements Runnable {

        private final HttpRequest<T> request;

        public CancelDeliveryRunnable(HttpRequest<T> request) {
            this.request = request;
        }

        @Override
        public void run() {
            Callback<T> callback = request.callback();
            if (callback != null) {
                callback.onCancel();
            }
            request.finish();
        }
    }

    private class ProgressDeliveryRunnable implements Runnable {

        private final long total;
        private final long current;
        private final ProgressCallback callback;

        public ProgressDeliveryRunnable(long total, long current, ProgressCallback callback) {
            this.total = total;
            this.current = current;
            this.callback = callback;
        }

        @Override
        public void run() {
            if (callback != null) {
                callback.onProgress(total, current);
            }
        }
    }

}
