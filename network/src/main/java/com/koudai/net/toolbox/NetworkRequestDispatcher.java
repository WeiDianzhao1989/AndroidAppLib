package com.koudai.net.toolbox;


import android.os.Process;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Created by zhaoyu on 15/12/25.
 * 网络请求分发
 */
final class NetworkRequestDispatcher extends Thread {

    private RequestQueueLock queueLock;
    private final Queue<HttpRequestRunnable> networkQueue;
    private final Map<String, Queue<HttpRequest<?>>> waitingRequests;
    private final Set<HttpRequest<?>> currentRunningRequests;

    private volatile boolean mQuit = false;

    public NetworkRequestDispatcher(RequestQueueLock queueLock,
                                    Queue<HttpRequestRunnable> networkQueue,
                                    Map<String, Queue<HttpRequest<?>>> waitingRequests,
                                    Set<HttpRequest<?>> currentRunningRequests) {
        this.queueLock = queueLock;
        this.networkQueue = networkQueue;
        this.waitingRequests = waitingRequests;
        this.currentRunningRequests = currentRunningRequests;
    }

    public void quit() {
        mQuit = true;
        interrupt();
    }


    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        while (true) {
            HttpRequestRunnable requestRunnable;
            try {

                //从请求队列，取出请求
                try {
                    queueLock.lock();
                    while (true) {
                        requestRunnable = networkQueue.poll();
                        if (requestRunnable == null) {
                            queueLock.waitFor();
                        } else {
                            break;
                        }
                    }
                } finally {
                    queueLock.unlock();
                }


                HttpRequest<?> httpRequest = requestRunnable.getHttpRequest();

                HttpRequest<?> currentRequest = null;

                synchronized (currentRunningRequests) {
                    for (HttpRequest<?> request : currentRunningRequests) {
                        if (request.tag().equalsIgnoreCase(httpRequest.tag())) {
                            currentRequest = request;
                        }
                    }
                    //添加到表示正在进行请求的集合中
                    if (currentRunningRequests.contains(httpRequest)) {
                        currentRunningRequests.add(httpRequest);
                    }
                }

                //合并同一个请求
                synchronized (waitingRequests) {

                    String tag = requestRunnable.getHttpRequest().tag();
                    if (waitingRequests.containsKey(tag)
                            && (currentRequest != null && !currentRequest.isCanceled())) {
                        Queue<HttpRequest<?>> stagedRequests = waitingRequests.get(tag);
                        if (stagedRequests == null) {
                            stagedRequests = new LinkedList<HttpRequest<?>>();
                        }
                        stagedRequests.add(requestRunnable.getHttpRequest());
                        waitingRequests.put(tag, stagedRequests);
                    } else {
                        waitingRequests.put(tag, null);
                        WorkersCenter.getInstance()
                                .getRequestWorkers()
                                .execute(requestRunnable);
                    }
                }
            } catch (InterruptedException e) {
                NetworkLog.getInstance().e(e.getMessage());
                if (mQuit) {
                    return;
                }
            }
        }
    }
}
