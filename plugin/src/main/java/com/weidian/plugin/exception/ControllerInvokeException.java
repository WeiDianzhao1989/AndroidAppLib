package com.weidian.plugin.exception;

public class ControllerInvokeException extends RuntimeException {

	public ControllerInvokeException(String message) {
		super(message);
	}

	public ControllerInvokeException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}
}
