package com.weidian.plugin.core.install;

import com.weidian.plugin.core.ctx.Module;

/**
 * @author: wyouflf
 * @date: 2014/11/10
 */
public interface InstallCallback {

	void callback(Module module, boolean needRestart);

	void error(Throwable ex, boolean isCallbackError);
}
