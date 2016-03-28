package com.koudai.net.toolbox;

import android.util.Log;

/**
 * Created by zhaoyu on 15/12/7.
 */
public final class NetworkLog {

    private static final String TAG = "network";
    private boolean isLogger = false;

    private static NetworkLog logger = new NetworkLog();

    public static NetworkLog getInstance() {
        return logger;
    }

    public void setLogger(boolean logger) {
        this.isLogger = logger;
    }

    public void d(String log) {
        if (isLogger) {
            Log.d(TAG, log);
        }
    }

    public void d(String log, Throwable t) {
        if (isLogger) {
            Log.d(TAG, log, t);
        }
    }

    public void v(String log) {
        if (isLogger) {
            Log.v(TAG, log);
        }
    }

    public void v(String log, Throwable t) {
        if (isLogger) {
            Log.v(TAG, log, t);
        }
    }

    public void e(String log) {
        if (isLogger) {
            Log.e(TAG, log);
        }
    }

    public void e(String log, Throwable t) {
        if (isLogger) {
            Log.e(TAG, log, t);
        }
    }

    public void i(String log) {
        if (isLogger) {
            Log.i(TAG, log);
        }
    }

    public void i(String log, Throwable t) {
        if (isLogger) {
            Log.i(TAG, log, t);
        }
    }

    public void w(String log) {
        if (isLogger) {
            Log.w(TAG, log);
        }
    }

    public void w(String log, Throwable t) {
        if (isLogger) {
            Log.w(TAG, log, t);
        }
    }

}
