package com.weidian.plugin.exception;

/**
 * @author: wyouflf
 * @date: 2014/11/03
 */
public class ControllerInvokeException extends RuntimeException {

	public ControllerInvokeException(String message) {
		super(message);
	}

	public ControllerInvokeException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}
}
