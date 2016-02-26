package com.koudai.net.toolbox;

import android.net.Uri;
import android.os.Process;
import android.text.TextUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhaoyu on 15/11/16.
 * 请求队列，管理着当前所有请求
 */
final class RequestQueue {

    //仿volley的四个分发请求线程，唯一不同的是，之后请求的实际执行另起线程
    private NetworkRequestDispatcher[] dispatchers = new NetworkRequestDispatcher[4];

    // Singleton.
    private static class RequestQueueHolder {
        public static final RequestQueue instance = new RequestQueue();
    }

    static RequestQueue getInstance() {
        return RequestQueueHolder.instance;
    }

    private final Map<String, Queue<HttpRequest<?>>> waitingRequests =
            new HashMap<String, Queue<HttpRequest<?>>>();
    private final Set<HttpRequest<?>> currentRequests = new HashSet<HttpRequest<?>>();

    //减少并发请求，以便能更快
    private final int maxRequests = 64;//总共允许64个并发请求
    private final int maxRequestsPerHost = 10;//每个host允许10个并发请求

    /**
     * Ready async calls in the order they'll be run.
     */
    private final Deque<HttpRequestRunnable> readyAsyncCalls = new ArrayDeque<HttpRequestRunnable>();

    /**
     * Running asynchronous calls. Includes canceled calls that haven't finished yet.
     */
    private final BlockingQueue<HttpRequestRunnable> runningAsyncCalls = new PriorityBlockingQueue<HttpRequestRunnable>();

    public void start() {
        for (int i = 0; i < dispatchers.length; i++) {
            dispatchers[i] = new NetworkRequestDispatcher(runningAsyncCalls, currentRequests,
                    waitingRequests);
            dispatchers[i].start();
        }
    }

    /**
     * 将请求入队
     *
     * @param request
     * @return
     */
    public HttpRequestRunnable enqueue(HttpRequest<?> request) {
        request.setRequestQueue(this);

        HttpRequestRunnable requestRunnable = new HttpRequestRunnable(request);

        internalEnqueue(requestRunnable);

        return requestRunnable;
    }

    private void internalEnqueue(HttpRequestRunnable requestRunnable) {
        synchronized (RequestQueue.this) {
            if (runningAsyncCalls.size() < maxRequests
                    && runningCallsForHost(requestRunnable) < maxRequestsPerHost) {
                runningAsyncCalls.offer(requestRunnable);
            } else {
                readyAsyncCalls.add(requestRunnable);
            }
        }
    }

    public List<HttpRequestRunnable> enqueueAll(HttpRequest<?>... httpRequests) {
        List<HttpRequest<?>> requests = Arrays.asList(httpRequests);
        List<HttpRequestRunnable> requestRunnables = new ArrayList<HttpRequestRunnable>();

        HttpRequestRunnable requestRunnable = null;

        for (HttpRequest request : requests) {
            request.setRequestQueue(this);
            requestRunnable = new HttpRequestRunnable(request);
            requestRunnables.add(requestRunnable);
        }

        MultiHttpRequestCall call = new MultiHttpRequestCall(requestRunnables);
        WorkersCenter.getInstance().getLongTaskWorkers().execute(call);

        return requestRunnables;
    }

    public void stop() {
        for (int i = 0; i < dispatchers.length; i++) {
            NetworkRequestDispatcher dispatcher = dispatchers[i];
            dispatcher.quit();
            dispatchers[i] = null;
        }
    }

    public void finish(HttpRequest request) {

        synchronized (currentRequests) {
            currentRequests.remove(request);
        }

        synchronized (waitingRequests) {
            String tag = request.tag();
            Queue<HttpRequest<?>> waitingRequests = this.waitingRequests.remove(tag);
            if (waitingRequests != null) {
                Iterator<HttpRequest<?>> it =
                        waitingRequests.iterator();
                while (it.hasNext()) {
                    HttpRequest waitingRequest = it.next();
                    if (!waitingRequest.isCanceled()) {
                        if (waitingRequest.isSuccess()) {
                            DefaultResponseDelivery.getInstance().postResponse(waitingRequest, request.response());
                        } else {
                            DefaultResponseDelivery.getInstance().postError(waitingRequest, request.error());
                        }
                    } else {
                        DefaultResponseDelivery.getInstance().postCancel(waitingRequest);
                    }
                    runningAsyncCalls.remove(waitingRequest);
                }
            }
        }

        promoteCalls();

    }

    /**
     * 返回某个域名并发请求数
     */
    private int runningCallsForHost(HttpRequestRunnable call) {
        int result = 0;
        String host = Uri.parse(call.getHttpRequest().url()).getHost();
        for (HttpRequestRunnable c : runningAsyncCalls) {
            Uri uri = Uri.parse(c.getHttpRequest().url());
            if (TextUtils.equals(host, uri.getHost())) {
                result++;
            }
        }
        return result;
    }

    /**
     * 释放一个等待请求到运行队列里
     */
    private synchronized void promoteCalls() {
        if (runningAsyncCalls.size() >= maxRequests) return; // Already running max capacity.
        if (readyAsyncCalls.isEmpty()) return; // No ready calls to promote.

        for (Iterator<HttpRequestRunnable> i = readyAsyncCalls.iterator(); i.hasNext(); ) {
            HttpRequestRunnable call = i.next();

            if (runningCallsForHost(call) < maxRequestsPerHost) {
                i.remove();
                if (!call.isCanceled()) {
                    runningAsyncCalls.offer(call);
                }
            }

            if (runningAsyncCalls.size() >= maxRequests) return; // Reached max capacity.
        }
    }

    private final class MultiHttpRequestCall implements Runnable {

        private List<HttpRequestRunnable> requestRunnables;

        public MultiHttpRequestCall(List<HttpRequestRunnable> requestRunnables) {
            this.requestRunnables = requestRunnables;
        }

        @Override
        public void run() {

            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            List<HttpRequestRunnable> mainTasks = new ArrayList<HttpRequestRunnable>();
            List<HttpRequestRunnable> minorTasks = new ArrayList<HttpRequestRunnable>();

            for (HttpRequestRunnable runnable : requestRunnables) {
                switch (runnable.getHttpRequest().requestLevel()) {
                    case Level.MAIN_TASK: {
                        mainTasks.add(runnable);
                        break;
                    }
                    case Level.MINOR_TASK: {
                        minorTasks.add(runnable);
                        break;
                    }

                }
            }

            if (minorTasks.size() != 0 || mainTasks.size() != 0) {
                try {

                    MultiRequestWatchDog watchDog = new MultiRequestWatchDog(mainTasks.size());

                    for (HttpRequestRunnable main : mainTasks) {
                        CallDelegate delegate = new CallDelegate(main, watchDog);
                        internalEnqueue(delegate);
                    }


                    boolean isMainTaskSuccess = watchDog.await((int) NetworkFetcherGlobalParams.getInstance().getWriteTimeout(), TimeUnit.SECONDS);

                    if (isMainTaskSuccess) {
                        for (HttpRequestRunnable minor : minorTasks) {
                            internalEnqueue(minor);
                        }
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                    if (minorTasks.size() > 0) {
                        for (HttpRequestRunnable minor : minorTasks) {
                            minor.cancel(false);
                        }
                    }
                }

            }

        }
    }

    private final class CallDelegate extends HttpRequestRunnable implements Runnable {

        private HttpRequestRunnable delegate;
        private MultiRequestWatchDog watchDog;

        public CallDelegate(HttpRequestRunnable delegate, MultiRequestWatchDog watchDog) {
            this.delegate = delegate;
            this.watchDog = watchDog;
        }

        @Override
        public void execute() {
            boolean isSuccess = false;
            try {
                if (this.delegate != null) {
                    this.delegate.execute();
                    isSuccess = this.delegate.isSuccess();
                }
            } finally {
                if (this.watchDog != null) {
                    this.watchDog.countDown(isSuccess);
                }
            }
        }

        @Override
        public HttpRequest<?> getHttpRequest() {
            return this.delegate.getHttpRequest();
        }

        @Override
        public void cancel() {
            this.delegate.cancel();
        }

        @Override
        public void cancel(boolean isNotifyObserver) {
            this.delegate.cancel(isNotifyObserver);
        }

        @Override
        public boolean isCanceled() {
            return this.delegate.isCanceled();
        }

        @Override
        public boolean isFinished() {
            return this.delegate.isFinished();
        }

        @Override
        public boolean isSuccess() {
            return this.delegate.isSuccess();
        }
    }
}
