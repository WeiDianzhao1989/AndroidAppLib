package com.weidian.plugin.core.ctx;

import android.util.Log;
import com.weidian.plugin.app.PageHelper;
import com.weidian.plugin.core.install.Installer;
import com.weidian.plugin.util.ReflectUtil;

import java.lang.reflect.Field;
import java.util.Map;

/*package*/ final class HostParentClassLoader extends ClassLoader {

	private static HostParentClassLoader instance;
	private static ClassLoader appClassLoader;
	private static ClassLoader bootClassLoader;

	private HostParentClassLoader(Host host) {
		ReflectUtil.init();
		// class loader结构:
		// bootClassLoader <-- HostParentClassLoader <-- appClassLoader
		appClassLoader = host.getClassLoader();
		Class<?> loaderCls = ClassLoader.class;
		try {
			Field parentField = loaderCls.getDeclaredField("parent");
			parentField.setAccessible(true);
			bootClassLoader = appClassLoader.getParent();
			parentField.set(this, bootClassLoader);
			parentField.set(appClassLoader, this);
		} catch (Throwable e) {
			Log.e("plugin", "init app class loader", e);
		}
	}

	public synchronized static void init(Host host) {
		if (instance == null) {
			instance = new HostParentClassLoader(host);
		}
	}

	public static ClassLoader getBootClassLoader() {
		return bootClassLoader;
	}

	public static ClassLoader getAppClassLoader() {
		return appClassLoader;
	}

	@Override
	protected Class<?> findClass(String className) throws ClassNotFoundException {
		if (PageHelper.HOST_ACTIVITY.equals(className)) {
			Installer.waitForInit();
			return PageHelper.getTargetActivityClass();
		}

		Class<?> result = ReflectUtil.findClass(appClassLoader, className);
		if (result != null) {
			return result;
		}

		result = findClassFromModules(className);

		if (result == null) {
			throw new ClassNotFoundException(className);
		}

		return result;
	}

	// 从已加载的模块查找类型
	private Class<?> findClassFromModules(String className) {
		if (className.endsWith("Activity")) {
			Installer.waitForInit();
		}
		Class<?> result = null;
		Map<String, Module> moduleMap = Installer.getLoadedModules();
		if (moduleMap != null) {
			for (Module module : moduleMap.values()) {
				try {
					// 不要让ModuleClassLoader的findClass查找其他依赖, 最好不要覆盖它.
					result = ((ModuleClassLoader) module.getClassLoader()).loadClass(className, false);
					if (result != null) {
						break;
					}
				} catch (Throwable ignored) {
				}
			}
		}
		return result;
	}
}