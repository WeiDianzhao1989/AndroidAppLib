package com.koudai.net.toolbox;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by zhaoyu on 15/11/18.
 */
final class CancelQueue {

    // Singleton.
    private static class CancelQueueHolder {
        public static final CancelQueue instance = new CancelQueue();
    }

    static CancelQueue getInstance() {
        return CancelQueueHolder.instance;
    }

    private volatile boolean mQuit = false;
    private BlockingQueue<CancelItem> cancelQueue = new LinkedBlockingQueue<CancelItem>();
    private CancelNetworkDispatcher dispatcher;

    private CancelQueue() {
    }

    public void enqueue(CancelItem cancelItem) {
        try {
            cancelQueue.offer(cancelItem);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        dispatcher = new CancelNetworkDispatcher();
        dispatcher.start();
    }

    public void stop() {
        mQuit = true;
        if (dispatcher != null) {
            dispatcher.interrupt();
        }
    }

    static final class CancelItem {

        private boolean isCallCancelCallback;
        private HttpRequestRunnable call;

        public CancelItem(HttpRequestRunnable call, boolean isCallCancelCallback) {
            this.call = call;
            this.isCallCancelCallback = isCallCancelCallback;
        }

    }

    final class CancelNetworkDispatcher extends Thread {
        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            while (true) {
                try {
                    CancelItem cancelItem = cancelQueue.take();
                    if (!cancelItem.call.isCanceled() && !cancelItem.call.isFinished()) {
                        cancelItem.call.cancel(cancelItem.isCallCancelCallback);
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                    if (mQuit) {
                        return;
                    }
                }
            }
        }
    }
}
