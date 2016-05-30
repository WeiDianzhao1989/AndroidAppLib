package com.weidian.plugin.core.install;

public interface UninstallCallback {

    void callback(boolean needRestart);

    void error(Throwable ex, boolean isCallbackError);
}
