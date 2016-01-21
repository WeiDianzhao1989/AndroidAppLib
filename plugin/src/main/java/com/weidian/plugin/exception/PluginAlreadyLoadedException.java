package com.weidian.plugin.exception;

/**
 * @author: wyouflf
 * @date: 2014/10/30
 */
public class PluginAlreadyLoadedException extends RuntimeException {
	private String packageName;

	public PluginAlreadyLoadedException(String packageName) {
		super("already loaded:" + packageName);
		this.packageName = packageName;
	}

	public String getPackageName() {
		return packageName;
	}
}
