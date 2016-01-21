package com.weidian.plugin.core.ctx;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;

/**
 * @author: wyouflf
 * @date: 2014/11/14
 */
public final class ContextProxy extends ContextThemeWrapper {

	private final Plugin plugin;
	private Resources hostRes;
	private LayoutInflater layoutInflater;

	/**
	 * Context 代理
	 *
	 * @param baseContext Activity或Fragment中原有的context
	 * @param proxy       当前Activity或Fragment示例
	 */
	public ContextProxy(Context baseContext, Object proxy) {
		super(baseContext, 0);
		plugin = Plugin.getPlugin(proxy);
		//修复在ZTE G717C上发现的调用ContextProxy#getResources方法陷入死循环的问题
		if (plugin instanceof Host) {
			hostRes = plugin.getContext().getResources();
		}
	}

	public Plugin getPlugin() {
		return plugin;
	}

	@Override
	public Object getSystemService(String name) {
		if (Context.LAYOUT_INFLATER_SERVICE.equals(name)) {
			return getLayoutInflater();
		} else {
			return super.getSystemService(name);
		}
	}

	@Override
	public Resources.Theme getTheme() {
		return plugin.getContext().getTheme();
	}

	// wrap for Activity
	public LayoutInflater getLayoutInflater() {
		if (layoutInflater == null) {
			LayoutInflater service = (LayoutInflater) super.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.layoutInflater = service.cloneInContext(this);
		}
		return layoutInflater;
	}

	@Override
	public AssetManager getAssets() {
		return plugin.getContext().getAssets();
	}

	@Override
	public Resources getResources() {
		if (hostRes != null) {
			return hostRes;
		}
		return plugin.getContext().getResources();
	}

	@Override
	public void applyOverrideConfiguration(Configuration overrideConfiguration) {
		Context context = plugin.getContext();
		if (context instanceof ModuleContext) {
			((ModuleContext) context).applyOverrideConfiguration(overrideConfiguration);
		}
	}

	@Override
	public ClassLoader getClassLoader() {
		return plugin.getContext().getClassLoader();
	}
}
