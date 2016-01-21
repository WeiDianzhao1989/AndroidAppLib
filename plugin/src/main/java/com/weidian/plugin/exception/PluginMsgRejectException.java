package com.weidian.plugin.exception;

import com.weidian.plugin.PluginMsg;

public class PluginMsgRejectException extends Exception {

	private PluginMsg msg;

	public PluginMsgRejectException(PluginMsg msg) {
		super("msg has been rejected");
		this.msg = msg;
	}

	public PluginMsg getMsg() {
		return msg;
	}
}
