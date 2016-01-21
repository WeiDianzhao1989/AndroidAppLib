package com.weidian.plugin;

import java.util.Map;

/**
 * 插件消息回调接口
 */
public interface MsgCallback {

	public void callback(Map<String, Object> result);

	public void error(Throwable ex, boolean isCallbackError);
}

