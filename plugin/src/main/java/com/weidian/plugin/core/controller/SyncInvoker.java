package com.weidian.plugin.core.controller;

import com.weidian.plugin.MsgCallback;
import com.weidian.plugin.PluginMsg;
import com.weidian.plugin.core.ctx.Plugin;

import java.lang.reflect.Method;
import java.util.Map;

/*package*/ class SyncInvoker extends Invoker {

    public SyncInvoker(Plugin plugin, Object controller, Method method) {
        super(plugin, controller, method);
    }

    /**
     * 处理消息
     *
     * @param msg
     * @param msgCallback
     * @return 是否处理完成
     */
    public void invoke(final PluginMsg msg, final MsgCallback msgCallback) {

        Map<String, Object> result = null;
        try {
            result = (Map<String, Object>) method.invoke(controller, msg);

            if (msgCallback != null) {
                try {
                    msgCallback.callback(result);
                } catch (Throwable ex) {
                    msgCallback.error(ex, true);
                }
            }
        } catch (Throwable ex) {
            if (msgCallback != null) {
                msgCallback.error(ex, false);
            }
        }
    }
}
