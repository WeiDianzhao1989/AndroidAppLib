package com.koudai.net.toolbox;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhaoyu on 15/12/25.
 * 网络库的后台线程中心，未来支持外部可配置，以便可以由
 * app掌控app内线程使用量和优化
 */
final class WorkersCenter {

    public static final class WorkersCenterHolder {
        private static final WorkersCenter instance = new WorkersCenter();
    }


    private static final int NETWORK_COOL_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final int NETWORK_MAX_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 3;
    private static final int ALIVE_TIME = 30;
    private static final int BIG_DATA_REQUEST_THREAD_POOL_SIZE = 3;

    private ExecutorService fastRequestWorkers = new ThreadPoolExecutor(
            NETWORK_COOL_POOL_SIZE,
            NETWORK_MAX_POOL_SIZE,
            ALIVE_TIME, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(),
            new WorkerFactory()); //后台执行网络请求的线程池

    private ExecutorService longTaskWorkers =
            Executors.newFixedThreadPool(2); //网络库里非网络请求等的长时间任务执行线程池

    private ExecutorService bigDataRequestWorkers = new ThreadPoolExecutor(
            BIG_DATA_REQUEST_THREAD_POOL_SIZE,
            BIG_DATA_REQUEST_THREAD_POOL_SIZE,
            0, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(),
            new WorkerFactory()); //后台执行网络请求的线程池

    public static WorkersCenter getInstance() {
        return WorkersCenterHolder.instance;
    }


    public ExecutorService getFastRequestWorkers() {
        return fastRequestWorkers;
    }

    public ExecutorService getLongTaskWorkers() {
        return longTaskWorkers;
    }

    public ExecutorService getSlowRequestWorkers() {
        return bigDataRequestWorkers;
    }

    private static final class WorkerFactory implements ThreadFactory {

        @Override
        public Thread newThread(Runnable r) {
            Worker thread = new Worker(r);
            thread.setName("dispatch_thread");
            return thread;
        }
    }

    private static final class Worker extends Thread {

        public Worker(Runnable target) {
            super(target);
        }

        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            super.run();
        }
    }
}
