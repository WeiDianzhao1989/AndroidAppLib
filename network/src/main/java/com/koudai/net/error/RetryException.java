package com.koudai.net.error;

/**
 * Created by zhaoyu on 15/12/9.
 */
public class RetryException extends Exception {

    private int responseCode;
    private String retryReason;


    public RetryException(String retryReason) {
        this.retryReason = retryReason;
    }

    public RetryException(int responseCode, String retryReason) {
        this.responseCode = responseCode;
        this.retryReason = retryReason;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getRetryReason() {
        return retryReason;
    }
}
