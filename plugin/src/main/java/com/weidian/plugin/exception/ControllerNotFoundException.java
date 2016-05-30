package com.weidian.plugin.exception;

public class ControllerNotFoundException extends RuntimeException {

    public ControllerNotFoundException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
