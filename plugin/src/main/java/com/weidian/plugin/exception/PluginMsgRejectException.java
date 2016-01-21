package com.weidian.plugin.exception;

import com.weidian.plugin.PluginMsg;

/**
 * @author: wyouflf
 * @date: 2014/11/14
 */
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
