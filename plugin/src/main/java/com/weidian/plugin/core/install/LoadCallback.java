package com.weidian.plugin.core.install;

import com.weidian.plugin.core.ctx.Module;

public interface LoadCallback {

    void callback(Module module);

    void error(Throwable ex, boolean isCallbackError);
}
