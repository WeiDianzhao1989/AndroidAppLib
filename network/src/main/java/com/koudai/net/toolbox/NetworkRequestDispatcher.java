package com.koudai.net.toolbox;


import android.os.Process;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

/**
 * Created by zhaoyu on 15/12/25.
 * 网络请求分发
 */
final class NetworkRequestDispatcher extends Thread {

    private final BlockingQueue<HttpRequestRunnable> networkQueue;
    private final Map<String, Queue<HttpRequest<?>>> waitingRequests;
    private final Set<HttpRequest<?>> currentRequest;

    private volatile boolean mQuit = false;


    public NetworkRequestDispatcher(BlockingQueue<HttpRequestRunnable> networkQueue,
                                    Set<HttpRequest<?>> currentRequest,
                                    Map<String, Queue<HttpRequest<?>>> waitingRequests) {
        this.networkQueue = networkQueue;
        this.currentRequest = currentRequest;
        this.waitingRequests = waitingRequests;
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
                requestRunnable = networkQueue.take();

                //添加到表示正在进行请求的集合中
                synchronized (currentRequest) {
                    currentRequest.add(requestRunnable.getHttpRequest());
                }

                //合并同一个请求
                synchronized (waitingRequests) {

                    String tag = requestRunnable.getHttpRequest().tag();
                    if (waitingRequests.containsKey(tag)) {
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
                if (mQuit) {
                    return;
                }
            }
        }
    }
}
