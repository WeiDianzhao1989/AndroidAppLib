package com.koudai.net.error;

/**
 * Created by zhaoyu on 15/12/2.
 */
public class NetworkError {

    private int httpCode;
    private int businessCode;
    private String errorMessage;

    public NetworkError(int httpCode, int businessCode, String errorMessage) {
        this.httpCode = httpCode;
        this.businessCode = businessCode;
        this.errorMessage = errorMessage;
    }

    public int getHttpCode() {
        return httpCode;
    }

    public int getBusinessCode() {
        return businessCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return "NetworkError{" +
                "httpCode=" + httpCode +
                ", businessCode=" + businessCode +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
