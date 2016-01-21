package com.weidian.plugin.core.install;

/**
 * @author: wyouflf
 * @date: 2014/11/10
 */
public interface LoadConfigCallback {

	void callback(Config module);

	void error(Throwable ex, boolean isCallbackError);
}
