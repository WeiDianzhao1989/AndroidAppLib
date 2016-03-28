package com.koudai.net.monitor;

import android.net.TrafficStats;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * Class used to read from TrafficStats periodically, in order to determine a ConnectionClass.
 */
public class DeviceBandwidthSampler {

    /**
     * The DownloadBandwidthManager that keeps track of the moving average and ConnectionClass.
     */
    private final ConnectionClassManager mConnectionClassManager;

    private Connectivity connectivity;

    private AtomicInteger mSamplingCounter;

    private SamplingHandler mHandler;
    private HandlerThread mThread;

    private long mLastTimeReading;
    private static long sPreviousBytes = -1;

    // Singleton.
    private static class DeviceBandwidthSamplerHolder {
        public static final DeviceBandwidthSampler instance =
                new DeviceBandwidthSampler(ConnectionClassManager.getInstance());
    }

    /**
     * Retrieval method for the DeviceBandwidthSampler singleton.
     *
     * @return The singleton instance of DeviceBandwidthSampler.
     */
    public static DeviceBandwidthSampler getInstance() {
        return DeviceBandwidthSamplerHolder.instance;
    }

    private DeviceBandwidthSampler(
            ConnectionClassManager connectionClassManager) {
        mConnectionClassManager = connectionClassManager;
        mSamplingCounter = new AtomicInteger();
        mThread = new HandlerThread("ParseThread");
        mThread.start();
        mHandler = new SamplingHandler(mThread.getLooper());
    }

    public void setConnectivity(Connectivity connectivity) {
        this.connectivity = connectivity;
    }

    /**
     * Method call to start sampling for download bandwidth.
     */
    public void startSampling() {
        if (mSamplingCounter.getAndIncrement() == 0) {
            mHandler.startSamplingThread();
            mLastTimeReading = SystemClock.elapsedRealtime();
        }
    }

    /**
     * Finish sampling and prevent further changes to the
     * ConnectionClass until another timer is started.
     */
    public void stopSampling() {
        if (mSamplingCounter.decrementAndGet() == 0) {
            mHandler.stopSamplingThread();
            addFinalSample();
        }
    }

    /**
     * Method for polling for the change in total bytes since last update and
     * adding it to the BandwidthManager.
     */
    protected void addSample() {
        long newBytes = -1;
        newBytes = TrafficStats.getUidRxBytes(Process.myUid());
        if (newBytes == TrafficStats.UNSUPPORTED) {
            newBytes = TrafficStats.getTotalRxBytes();
        }
        long byteDiff = newBytes - sPreviousBytes;
        if (sPreviousBytes >= 0) {
            synchronized (this) {
                long curTimeReading = SystemClock.elapsedRealtime();
                mConnectionClassManager.addBandwidth(byteDiff, curTimeReading - mLastTimeReading);

                mLastTimeReading = curTimeReading;
            }
        }
        sPreviousBytes = newBytes;
    }

    /**
     * Resets previously read byte count after recording a sample, so that
     * we don't count bytes downloaded in between sampling sessions.
     */
    protected void addFinalSample() {
        addSample();
        sPreviousBytes = -1;
    }

    /**
     * @return True if there are still threads which are sampling, false otherwise.
     */
    public boolean isSampling() {
        return (mSamplingCounter.get() != 0);
    }

    private class SamplingHandler extends Handler {
        /**
         * Time between polls in ms.
         */
        static final long SAMPLE_TIME = 1000 * 30;

        static private final int MSG_START = 1;

        private long sample_time = SAMPLE_TIME;

        public SamplingHandler(Looper looper) {
            super(looper);
            if (connectivity != null) {
                if (connectivity.isConnectedMobile()) {
                    if (connectivity.isConnect2G()) {
                        sample_time = 1000 * 10;
                    } else if (connectivity.isConnect3G()) {
                        sample_time = 1000 * 20;
                    } else if (connectivity.isConnect4G()) {
                        sample_time = 1000 * 25;
                    } else {
                        sample_time = 1000 * 15;
                    }
                } else if (connectivity.isConnectedWifi()) {
                    sample_time = 1000 * 60 * 5;
                }
            }
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START:
                    addSample();
                    sendEmptyMessageDelayed(MSG_START, sample_time);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown what=" + msg.what);
            }
        }


        public void startSamplingThread() {
            sendEmptyMessage(SamplingHandler.MSG_START);
        }

        public void stopSamplingThread() {
            removeMessages(SamplingHandler.MSG_START);
        }
    }
}