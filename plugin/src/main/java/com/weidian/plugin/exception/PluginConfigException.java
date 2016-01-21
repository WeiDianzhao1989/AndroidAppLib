package com.weidian.plugin.exception;

/**
 * @author: wyouflf
 * @date: 2014/10/30
 */
public class PluginConfigException extends RuntimeException {

	private String fileName;

	public PluginConfigException(String fileName, Throwable cause) {
		super("config read error:" + fileName, cause);
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}
}
