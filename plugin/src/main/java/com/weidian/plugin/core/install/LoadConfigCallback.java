package com.weidian.plugin.core.install;

public interface LoadConfigCallback {

    void callback(Config module);

    void error(Throwable ex, boolean isCallbackError);
}
