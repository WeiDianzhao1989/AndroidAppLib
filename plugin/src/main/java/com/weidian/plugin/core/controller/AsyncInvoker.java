package com.weidian.plugin.core.controller;

import com.weidian.plugin.MsgCallback;
import com.weidian.plugin.PluginMsg;
import com.weidian.plugin.core.ctx.Plugin;
import com.weidian.plugin.task.Task;
import com.weidian.plugin.task.TaskManager;

import java.lang.reflect.Method;
import java.util.Map;

/*package*/ class AsyncInvoker extends Invoker {
    public AsyncInvoker(Plugin plugin, Object controller, Method method) {
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

        TaskManager.start(new Task<Map<String, Object>>() {
            @Override
            protected Map<String, Object> doBackground() throws Exception {
                return (Map<String, Object>) method.invoke(controller, msg);
            }

            @Override
            protected void onFinished(Map<String, Object> result) {
                if (msgCallback != null) {
                    try {
                        msgCallback.callback(result);
                    } catch (Throwable ex) {
                        msgCallback.error(ex, true);
                    }
                }
            }

            @Override
            protected void onError(Throwable ex, boolean isCallbackError) {
                if (msgCallback != null) {
                    msgCallback.error(ex, false);
                }
            }
        });
    }
}
