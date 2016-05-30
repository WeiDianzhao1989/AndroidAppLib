package com.weidian.plugin.core.controller;

import com.weidian.plugin.MsgCallback;
import com.weidian.plugin.PluginMsg;
import com.weidian.plugin.core.ctx.Plugin;

import java.lang.reflect.Method;

/*package*/ abstract class Invoker {

    protected final Plugin plugin;
    protected final Object controller;
    protected Method method;

    public Invoker(Plugin plugin, Object controller, Method method) {
        this.plugin = plugin;
        this.controller = controller;
        this.method = method;
    }

    /**
     * 处理消息
     *
     * @param msg
     * @param msgCallback
     * @return 是否处理完成
     */
    public abstract void invoke(final PluginMsg msg, final MsgCallback msgCallback);
}
