package com.koudai.net.error;

/**
 * Created by zhaoyu on 15/12/2.
 */
public class BusinessException extends Exception {

    private int errorCode;

    public BusinessException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
