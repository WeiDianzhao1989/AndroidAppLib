package com.koudai.net.callback;

/**
 * Created by zhaoyu on 15/12/2.
 */
public interface ProgressCallback {

    void onProgress(long total, long writtenBytes);

}
