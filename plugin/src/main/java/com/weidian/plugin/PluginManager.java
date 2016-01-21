package com.weidian.plugin;

import android.app.Application;
import com.weidian.plugin.core.controller.ControllerProxy;
import com.weidian.plugin.core.ctx.Host;
import com.weidian.plugin.core.ctx.Module;
import com.weidian.plugin.core.ctx.Plugin;
import com.weidian.plugin.core.install.Installer;
import com.weidian.plugin.exception.PluginMsgRejectException;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author: wyouflf
 * @date: 2014/11/03
 */
public final class PluginManager {

	private static HostApplication app;
	private final static Object msgQueueLock = new Object();
	private final static int MSG_QUEUE_MAX_COUNT = 100;
	private static LinkedList<MsgQueueItem> msgQueue = new LinkedList<MsgQueueItem>();

	private PluginManager() {
	}

	/**
	 * 只能从HostApplication初始化
	 *
	 * @param app
	 */
	static void init(final HostApplication app) {
		PluginManager.app = app;
		Installer.initHost(new PluginManager());
	}

	public static Application getApplication() {
		return app;
	}

	/**
	 * 获取对象或类型所在插件的包名
	 *
	 * @param obj 对象实例或class
	 * @return
	 */
	public static String getPackageName(Object obj) {
		return Plugin.getPlugin(obj).getConfig().getPackageName();
	}

	/**
	 * 发送消息, 消息由controller去处理.
	 */
	public static void sendMsg(final PluginMsg msg, final MsgCallback msgCallback) {
		sendMsgInternal(msg, msgCallback, null, false);
	}

	/**
	 * 发送消息, 消息由controller去处理.
	 *
	 * @param msg
	 * @param msgCallback
	 * @param modules     如果没有targetPackage, 发给指定的modules集合, modules为null时发给已加载的所有插件.
	 * @param fromQueue   是否是队列中未处理的消息.
	 */
	private static void sendMsgInternal(final PluginMsg msg, final MsgCallback msgCallback, Map<String, Module> modules, boolean fromQueue) {
		boolean needResend = true;
		String pkg = msg.getTargetPackage();
		if (pkg != null && pkg.length() > 0) { // 发送给指定插件
			Plugin toPlugin = Installer.getLoadedPlugin(pkg);
			if (toPlugin != null) {
				ControllerProxy controller = toPlugin.getControllerProxy();
				if (controller != null) {
					needResend = false;
					controller.dispatchMsg(msg, msgCallback);
				}
			}
		} else { // 发送给自定义匹配插件
			if (modules == null) {
				Host host = Installer.getHost();
				if (msg.match(host)) {
					ControllerProxy controller = host.getControllerProxy();
					if (controller != null) {
						needResend = false;
						controller.dispatchMsg(msg, msgCallback);
					}
				}
			}

			Map<String, Module> moduleMap = modules == null ? Installer.getLoadedModules() : modules;
			if (moduleMap != null) {
				for (Module module : moduleMap.values()) {
					if (msg.match(module)) {
						ControllerProxy controller = module.getControllerProxy();
						if (controller != null) {
							needResend = false;
							controller.dispatchMsg(msg, msgCallback);
						}
					}
				}
			}
		}

		if (!fromQueue && needResend) {
			// 加入消息队列等待加载事件回调再执行
			synchronized (msgQueueLock) {
				msgQueue.addLast(new MsgQueueItem(msg, msgCallback));
				if (msgQueue.size() > MSG_QUEUE_MAX_COUNT) {
					msgQueue.removeFirst();
				}
			}
			if (pkg != null && pkg.length() > 0) {
				Installer.loadModule(pkg, null);
			}
		}
	}

	public boolean isDebug() {
		return app.isDebug();
	}

	/**
	 * 宿主初始化完成回调
	 * <p/>
	 * 如果宿主初始化报错, 则在onPluginsLoadError方法之后回调.
	 */
	public void onHostInitialised() {
		app.onHostInitialised();
	}

	/**
	 * 仅被异步安装或加载过程回调
	 */
	public void onModulesLoaded(Map<String, Module> modules) {
		app.onModulesLoaded(modules);
		Iterator<MsgQueueItem> iterator = msgQueue.iterator();
		while (iterator.hasNext()) {
			MsgQueueItem item = iterator.next();
			if (!item.sendAgain()) {
				synchronized (msgQueueLock) {
					iterator.remove();
				}
				// 未处理的消息移除后通知发送者
				MsgCallback msgCallback = item.msgCallbackRef.get();
				if (msgCallback != null && !item.msg.isHandled()) {
					msgCallback.error(new PluginMsgRejectException(item.msg), false);
				}
			} else {
				sendMsgInternal(item.msg, item.msgCallbackRef.get(), modules, true);
			}
		}
	}

	/**
	 * 仅被异步安装或加载过程回调
	 */
	public void onPluginsLoadError(Throwable ex, boolean isCallbackError) {
		app.onPluginsLoadError(ex, isCallbackError);
	}

	private static class MsgQueueItem {
		static final int SEND_MAX_COUNT = 5;

		final PluginMsg msg;
		final WeakReference<MsgCallback> msgCallbackRef;
		int sendCount = 0;

		public MsgQueueItem(PluginMsg msg, MsgCallback msgCallback) {
			this.msg = msg;
			this.msgCallbackRef = new WeakReference<MsgCallback>(msgCallback);
		}

		public boolean sendAgain() {
			sendCount++;
			return !msg.isHandled() && sendCount < SEND_MAX_COUNT;
		}
	}
}
