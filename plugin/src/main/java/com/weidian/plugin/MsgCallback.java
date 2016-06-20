package com.weidian.plugin;

import java.util.Map;

public interface MsgCallback {

    void callback(Map<String, Object> result);

    void error(Throwable ex, boolean isCallbackError);
}

