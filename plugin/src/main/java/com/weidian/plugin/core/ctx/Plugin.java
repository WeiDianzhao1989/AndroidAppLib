package com.weidian.plugin.core.ctx;

import android.content.Context;
import android.view.LayoutInflater;
import com.weidian.plugin.core.controller.ControllerProxy;
import com.weidian.plugin.core.install.Config;
import com.weidian.plugin.core.install.Installer;

/**
 * @author: wyouflf
 * @date: 2014/10/29
 */
public abstract class Plugin {
	private final Config config;
	private final Context context;
	private LayoutInflater layoutInflater;
	protected ControllerProxy controllerProxy;

	/*package*/ Plugin(Context context, Config config) {
		this.context = context;
		this.config = config;
	}

	/**
	 * 获取对象或类型所在的插件
	 *
	 * @param obj 对象实例或class
	 * @return 产生这个实例的Plugin
	 */
	public static Plugin getPlugin(Object obj) {
		ClassLoader classLoader = null;
		if (obj instanceof Class) {
			classLoader = ((Class<?>) obj).getClassLoader();
		} else {
			classLoader = obj.getClass().getClassLoader();
		}
		if (classLoader instanceof ModuleClassLoader) {
			return ((ModuleClassLoader) classLoader).getModule();
		} else {
			return Installer.getHost();
		}
	}

	public abstract Class<?> loadClass(String name) throws ClassNotFoundException;

	/*package*/
	ClassLoader getClassLoader() {
		return this.context.getClassLoader();
	}

	public Config getConfig() {
		return this.config;
	}

	public Context getContext() {
		return context;
	}

	public LayoutInflater getLayoutInflater() {
		if (layoutInflater == null) {
			LayoutInflater service = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.layoutInflater = service.cloneInContext(context);
		}
		return layoutInflater;
	}

	public ControllerProxy getControllerProxy() {
		return controllerProxy;
	}

	@Override
	public String toString() {
		return config.getPackageName();
	}
}
