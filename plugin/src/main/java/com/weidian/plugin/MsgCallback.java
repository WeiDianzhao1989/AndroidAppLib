package com.weidian.plugin;

import java.util.Map;

public interface MsgCallback {

    public void callback(Map<String, Object> result);

    public void error(Throwable ex, boolean isCallbackError);
}

