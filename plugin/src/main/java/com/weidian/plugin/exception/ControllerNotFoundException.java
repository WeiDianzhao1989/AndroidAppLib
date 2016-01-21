package com.weidian.plugin.exception;

/**
 * @author: wyouflf
 * @date: 2014/11/02
 */
public class ControllerNotFoundException extends RuntimeException {

	public ControllerNotFoundException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}
}
