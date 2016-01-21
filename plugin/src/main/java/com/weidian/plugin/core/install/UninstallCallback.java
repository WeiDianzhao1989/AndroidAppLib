package com.weidian.plugin.core.install;

/**
 * @author: wyouflf
 * @date: 2014/11/10
 */
public interface UninstallCallback {

	void callback(boolean needRestart);

	void error(Throwable ex, boolean isCallbackError);
}
