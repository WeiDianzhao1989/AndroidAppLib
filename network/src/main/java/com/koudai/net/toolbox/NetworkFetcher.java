package com.koudai.net.toolbox;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by zhaoyu on 15/11/2.
 * 网络请求
 */
public final class NetworkFetcher {

    private AtomicBoolean isInit = new AtomicBoolean(false);


    // Singleton.
    private static class NetworkFetcherHolder {
        public static final NetworkFetcher instance = new NetworkFetcher();
    }

    private NetworkFetcher() {
    }

    public static NetworkFetcher getInstance() {
        return NetworkFetcherHolder.instance;
    }

    /**
     * 初始化网络库
     */
    public void initNetwork(NetworkFetcherConfiguration configuration) {

        if (configuration.getContext() == null)
            throw new IllegalArgumentException("please supply android application context!");

        if (ProcessUtils.isMainProcess(configuration.getContext())) {
            NetworkLog.getInstance().setLogger(configuration.isLogger());
            NetworkFetcherGlobalParams.getInstance().setContext(configuration.getContext().getApplicationContext());

            NetworkFetcherGlobalParams.getInstance().setRequestParamsInterceptor(configuration.getRequestParamsInterceptor());
            NetworkFetcherGlobalParams.getInstance().setRequestHeaderInterceptor(configuration.getRequestHeaderInterceptor());
            NetworkFetcherGlobalParams.getInstance().setResponseProcessor(configuration.getResponseProcessor());

            NetworkFetcherGlobalParams.getInstance().setDefaultReadTimeout(configuration.getReadTimeOut());
            NetworkFetcherGlobalParams.getInstance().setDefaultConnectionTimeout(configuration.getConnectTimeOut());
            NetworkFetcherGlobalParams.getInstance().setDefaultWriteTimeout(configuration.getWriteTimeOut());

            NetworkFetcherGlobalParams.getInstance().setMaxRetryTimesAfterFailed(
                    configuration.getMaxRetryTimesAfterFailed());

            AutoControlNetworkTime.getInstance().registerBandwidthListener();
            isInit.compareAndSet(false, true);
            RequestQueue.getInstance().start();
            CancelQueue.getInstance().start();
        }
    }

    public void stopNetwork() {
        if (ProcessUtils.isMainProcess(NetworkFetcherGlobalParams.getInstance().getContext())) {
            AutoControlNetworkTime.getInstance().unRegisterBandwidthListener();
            RequestQueue.getInstance().stop();
            CancelQueue.getInstance().stop();
        }
    }

    /**
     * 发起请求
     */
    public RequestControl execute(HttpRequest<?> httpRequest) {
        if (isInit.get()) {
            HttpRequestRunnable requestRunnable = RequestQueue.getInstance().enqueue(httpRequest);
            return new RequestControl(requestRunnable);
        }
        throw new RuntimeException("network fetcher does not be inited");
    }

    /**
     * 执行一系列网络请求，可以通过level区分优先级，低优先级的会等待高优先级的完成，好处是减少并发
     * 网络IO、带宽，线程等资源的抢占，同时高优先级请求将使用keep-alive，以加快网络请求
     *
     * @return
     */
    public MultiRequestControl executeAll(HttpRequest<?>... httpRequests) {

        if (isInit.get() && httpRequests != null) {

            List<RequestControl> controls = new ArrayList<RequestControl>();


            List<HttpRequestRunnable> requestRunnables = RequestQueue.getInstance().enqueueAll(httpRequests);

            if (requestRunnables != null) {
                for (HttpRequestRunnable requestRunnable : requestRunnables) {
                    controls.add(new RequestControl(requestRunnable));
                }
            }

            return new MultiRequestControl(controls);
        }

        if (!isInit.get())
            throw new RuntimeException("network fetcher does not be inited");
        else
            throw new RuntimeException("execute error");
    }
}
