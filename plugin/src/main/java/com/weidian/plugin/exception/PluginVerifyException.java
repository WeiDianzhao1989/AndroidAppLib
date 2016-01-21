package com.weidian.plugin.exception;

/**
 * @author: wyouflf
 * @date: 2014/10/30
 */
public class PluginVerifyException extends RuntimeException {

	private String fileName;

	public PluginVerifyException(String fileName) {
		super("verify error:" + fileName);
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}
}
