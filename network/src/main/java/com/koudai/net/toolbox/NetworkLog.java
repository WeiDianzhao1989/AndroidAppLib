package com.koudai.net.toolbox;

import android.util.Log;

/**
 * Created by zhaoyu on 15/12/7.
 */
final class NetworkLog {

    private static final String TAG = "network";
    private boolean debug = false;

    private static NetworkLog logger = new NetworkLog();

    public static NetworkLog getInstance() {
        return logger;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void d(String log) {
        Log.d(TAG, log);
    }

    public void d(String log, Throwable t) {
        Log.d(TAG, log, t);
    }

    public void v(String log) {
        Log.v(TAG, log);
    }

    public void v(String log, Throwable t) {
        Log.v(TAG, log, t);
    }

    public void e(String log) {
        Log.e(TAG, log);
    }

    public void e(String log, Throwable t) {
        Log.e(TAG, log, t);
    }

    public void i(String log) {
        Log.i(TAG, log);
    }

    public void i(String log, Throwable t) {
        Log.i(TAG, log, t);
    }

    public void w(String log) {
        Log.w(TAG, log);
    }

    public void w(String log, Throwable t) {
        Log.w(TAG, log, t);
    }

}
