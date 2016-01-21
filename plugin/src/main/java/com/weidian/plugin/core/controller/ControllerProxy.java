package com.weidian.plugin.core.controller;

import com.weidian.plugin.MsgCallback;
import com.weidian.plugin.PluginMsg;
import com.weidian.plugin.app.PageHelper;
import com.weidian.plugin.core.ctx.Plugin;
import com.weidian.plugin.core.install.Config;
import com.weidian.plugin.exception.ControllerInvokeException;
import com.weidian.plugin.exception.ControllerNotFoundException;
import com.weidian.plugin.task.TaskManager;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;

final public class ControllerProxy {

	private static final String CONTROLLER_CLS_NAME = "Controller";
	private static final String SYNC_CMD_SUFFIX = "Sync";
	private final Object controller;
	private final ConcurrentHashMap<String, Invoker> invokerMap = new ConcurrentHashMap<String, Invoker>();

	private final Plugin plugin;

	public ControllerProxy(final Plugin plugin) {
		this.plugin = plugin;
		Config config = plugin.getConfig();
		String pkg = config.getPackageName();
		String ctrlClsName = pkg + "." + CONTROLLER_CLS_NAME;
		try {
			Class<?> ctrlCls = plugin.loadClass(ctrlClsName);
			controller = ctrlCls.newInstance();
		} catch (Throwable ex) {
			throw new ControllerNotFoundException(ctrlClsName, ex);
		}

		resolveInvoker();
	}

	/**
	 * @param msg
	 * @param msgCallback
	 */
	public void dispatchMsg(final PluginMsg msg, final MsgCallback msgCallback) {
		final Invoker invoker = invokerMap.get(msg.getCmd());
		if (invoker != null) {
			msg.setHandled(true); // 被成功dispatch的消息, handled默认值改为true.
			if (invoker instanceof AsyncInvoker) {
				invoker.invoke(msg, msgCallback);
			} else {
				TaskManager.post(new Runnable() {
					@Override
					public void run() {
						invoker.invoke(msg, msgCallback);
					}
				});
			}
		} else {
			ControllerInvokeException ex = new ControllerInvokeException(
				"not found " + controller.getClass().getName() + "#" + msg.getCmd());
			if (msgCallback != null) {
				msgCallback.error(ex, false);
			}
		}
	}

	private void resolveInvoker() {
		Method[] declaredMethods = controller.getClass().getDeclaredMethods();
		invokerMap.put(PageHelper.PAGE_INFO_CMD,
			new GetPageInfoInvoker(plugin, controller, null));
		if (declaredMethods != null) {
			for (Method method : declaredMethods) {
				if (!Modifier.isPublic(method.getModifiers())) {
					continue;
				}
				Class<?>[] paramTypes = method.getParameterTypes();
				if (paramTypes != null && paramTypes.length > 0) {
					if (PluginMsg.class.equals(paramTypes[0])) {
						String cmd = method.getName();
						Invoker invoker = null;
						switch (paramTypes.length) {
							case 1: {
								if (cmd.endsWith(SYNC_CMD_SUFFIX)) {
									cmd = cmd.substring(0, cmd.length() - 4);
									invoker = new SyncInvoker(plugin, controller, method);
								} else {
									invoker = new AsyncInvoker(plugin, controller, method);
								}
								break;
							}
							case 2: {
								if (MsgCallback.class.equals(paramTypes[1])) {
									invoker = new CallbackInvoker(plugin, controller, method);
								}
								break;
							}
							default:
								break;
						}
						if (invoker != null) {
							invokerMap.put(cmd, invoker);
						}
					}
				}
			}
		}
	}

	@Override
	public String toString() {
		return plugin.toString();
	}
}
