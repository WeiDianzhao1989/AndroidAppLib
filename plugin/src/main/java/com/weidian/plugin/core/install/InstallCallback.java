package com.weidian.plugin.core.install;

import com.weidian.plugin.core.ctx.Module;

public interface InstallCallback {

	void callback(Module module, boolean needRestart);

	void error(Throwable ex, boolean isCallbackError);
}
