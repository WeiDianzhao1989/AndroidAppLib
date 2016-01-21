package com.weidian.plugin.core.ctx;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import com.weidian.plugin.core.controller.ControllerProxy;
import com.weidian.plugin.core.install.Config;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class Module extends Plugin {

	private List<BroadcastReceiver> registerReceiverList;

	public Module(ModuleContext context, Config config) {
		super(context, config);
		context.classLoader.setModule(this);
		this.controllerProxy = new ControllerProxy(this);
		this.registerReceivers();
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return this.getClassLoader().loadClass(name);
	}

	public String findLibrary(String name) {
		return ((ModuleClassLoader) this.getClassLoader()).findLibrary(name);
	}

	/**
	 * 注册广播接收者
	 */
	private void registerReceivers() {
		HashMap<String, String[]> map = this.getConfig().getReceiverMap();
		if (map != null && map.size() > 0) {
			registerReceiverList = new LinkedList<BroadcastReceiver>();
			for (Map.Entry<String, String[]> entry : map.entrySet()) {
				String clsName = entry.getKey();
				String[] actions = entry.getValue();
				if (clsName != null && actions != null && actions.length > 0) {
					try {
						Class<?> cls = this.loadClass(clsName);
						BroadcastReceiver receiver = (BroadcastReceiver) cls.newInstance();
						IntentFilter filter = new IntentFilter();
						for (String action : actions) {
							filter.addAction(action);
						}
						this.getContext().registerReceiver(receiver, filter);
						registerReceiverList.add(receiver);
					} catch (Throwable ex) {
						throw new RuntimeException("register receiver error", ex);
					}
				}
			}
		}
	}

	@Override
	protected void finalize() throws Throwable {
		if (registerReceiverList != null && registerReceiverList.size() > 0) {
			Context context = this.getContext();
			if (context != null) {
				for (BroadcastReceiver receiver : registerReceiverList) {
					context.unregisterReceiver(receiver);
				}
			}
		}
		super.finalize();
	}
}
