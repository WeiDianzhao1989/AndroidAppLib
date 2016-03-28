package com.koudai.net.toolbox;

import com.koudai.net.NetworkLibraryConstants;
import com.koudai.net.monitor.ConnectionClassManager;
import com.koudai.net.monitor.ConnectionQuality;

/**
 * Created by zhaoyu on 15/12/7.
 * 通过监听网络带宽，了解网络状况，变化时调整一些网络请求参数
 */
final class AutoControlNetworkTime implements ConnectionClassManager.ConnectionClassStateChangeListener {

    private static final class AutoControlNetworkTimeHolder {
        public static final AutoControlNetworkTime instance = new AutoControlNetworkTime();
    }

    private AutoControlNetworkTime() {
    }


    public static AutoControlNetworkTime getInstance() {
        return AutoControlNetworkTimeHolder.instance;
    }

    public void registerBandwidthListener() {
        ConnectionClassManager.getInstance().register(this);
    }

    public void unRegisterBandwidthListener() {
        ConnectionClassManager.getInstance().remove(this);
    }

    @Override
    public void onBandwidthStateChange(ConnectionQuality bandwidthState) {
        switch (bandwidthState) {
            case POOR:
                NetworkFetcherGlobalParams.getInstance().setReadTimeout(NetworkLibraryConstants.POOR_READ_TIMEOUT);
                NetworkFetcherGlobalParams.getInstance().setWriteTimeout(NetworkLibraryConstants.POOR_WRITE_TIMEOUT);
                NetworkFetcherGlobalParams.getInstance().setConnectionTimeout(NetworkLibraryConstants.POOR_CONNECT_TIMEOUT);
                RequestQueue.getInstance().setMaxRequests(NetworkLibraryConstants.POOR_MAX_REQUEST);
                RequestQueue.getInstance().setMaxRequestsPerHost(NetworkLibraryConstants.POOR_MAX_REQUEST_PER_HOST);

                break;
            case MODERATE:
                NetworkFetcherGlobalParams.getInstance().setReadTimeout(NetworkLibraryConstants.MODERATE_READ_TIMEOUT);
                NetworkFetcherGlobalParams.getInstance().setWriteTimeout(NetworkLibraryConstants.MODERATE_WRITE_TIMEOUT);
                NetworkFetcherGlobalParams.getInstance().setConnectionTimeout(NetworkLibraryConstants.MODERATE_CONNECT_TIMEOUT);
                RequestQueue.getInstance().setMaxRequests(NetworkLibraryConstants.MODERATE_MAX_REQUEST);
                RequestQueue.getInstance().setMaxRequestsPerHost(NetworkLibraryConstants.MODERATE_MAX_REQUEST_PER_HOST);

                break;
            case GOOD:
                NetworkFetcherGlobalParams.getInstance().setReadTimeout(NetworkLibraryConstants.GOOD_READ_TIMEOUT);
                NetworkFetcherGlobalParams.getInstance().setWriteTimeout(NetworkLibraryConstants.GOOD_WRITE_TIMEOUT);
                NetworkFetcherGlobalParams.getInstance().setConnectionTimeout(NetworkLibraryConstants.GOOD_CONNECT_TIMEOUT);
                RequestQueue.getInstance().setMaxRequests(NetworkLibraryConstants.GOOD_MAX_REQUEST);
                RequestQueue.getInstance().setMaxRequestsPerHost(NetworkLibraryConstants.GOOD_MAX_REQUEST_PER_HOST);

                break;
            case EXCELLENT:
                NetworkFetcherGlobalParams.getInstance().setReadTimeout(NetworkLibraryConstants.EXCELLENT_READ_TIMEOUT);
                NetworkFetcherGlobalParams.getInstance().setWriteTimeout(NetworkLibraryConstants.EXCELLENT_WRITE_TIMEOUT);
                NetworkFetcherGlobalParams.getInstance().setConnectionTimeout(NetworkLibraryConstants.EXCELLENT_CONNECT_TIMEOUT);
                RequestQueue.getInstance().setMaxRequests(NetworkLibraryConstants.EXCELLENT_MAX_REQUEST);
                RequestQueue.getInstance().setMaxRequestsPerHost(NetworkLibraryConstants.EXCELLENT_MAX_REQUEST_PER_HOST);

                break;
            case UNKNOWN:
                NetworkFetcherGlobalParams.getInstance().setReadTimeout(NetworkFetcherGlobalParams.getInstance().getDefaultReadTimeout());
                NetworkFetcherGlobalParams.getInstance().setWriteTimeout(NetworkFetcherGlobalParams.getInstance().getDefaultWriteTimeout());
                NetworkFetcherGlobalParams.getInstance().setConnectionTimeout(NetworkFetcherGlobalParams.getInstance().getDefaultConnectionTimeout());
                RequestQueue.getInstance().setMaxRequests(NetworkLibraryConstants.DEFAULT_MAX_REQUEST);
                RequestQueue.getInstance().setMaxRequestsPerHost(NetworkLibraryConstants.DEFAULT_MAX_REQUEST_PER_HOST);

                break;
        }
    }
}
