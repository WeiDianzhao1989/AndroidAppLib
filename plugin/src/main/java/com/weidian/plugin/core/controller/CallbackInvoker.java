package com.weidian.plugin.core.controller;

import com.weidian.plugin.MsgCallback;
import com.weidian.plugin.PluginMsg;
import com.weidian.plugin.core.ctx.Plugin;

import java.lang.reflect.Method;

/*package*/ class CallbackInvoker extends Invoker {
	public CallbackInvoker(Plugin plugin, Object controller, Method method) {
		super(plugin, controller, method);
	}

	/**
	 * 处理消息
	 *
	 * @param msg
	 * @param msgCallback
	 * @return 是否处理完成
	 */
	@Override
	public void invoke(final PluginMsg msg, final MsgCallback msgCallback) {
		try {
			method.invoke(controller, msg, msgCallback);
		} catch (Throwable ex) {
			if (msgCallback != null) {
				msgCallback.error(ex, false);
			}
		}
	}
}
