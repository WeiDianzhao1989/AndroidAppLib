package com.weidian.plugin;

import android.app.Application;
import android.content.res.AssetManager;
import android.content.res.Resources;
import com.weidian.plugin.core.ctx.Module;

import java.io.File;
import java.util.Map;

/**
 * @author: wyouflf
 * @date: 2014/10/29
 */
public abstract class HostApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		PluginManager.init(this);
	}

	/**
	 * 是否使用调试模式
	 *
	 * @return
	 */
	public abstract boolean isDebug();

	/**
	 * 验证插件文件
	 *
	 * @param pluginFile
	 * @return
	 */
	public abstract boolean verifyPluginFile(File pluginFile);


	/**
	 * 宿主初始化完成回调
	 * <p/>
	 * 如果宿主初始化报错, 则在onPluginsLoadError方法之后回调.
	 */
	public abstract void onHostInitialised();

	/**
	 * 模块加载回调
	 *
	 * @param modules 新加载的模块
	 */
	public abstract void onModulesLoaded(Map<String, Module> modules);

	/**
	 * 模块加载错误回调
	 *
	 * @param ex
	 * @param isCallbackError
	 */
	public abstract void onPluginsLoadError(Throwable ex, boolean isCallbackError);

	// #region assets 修复中兴G717C等型号手机app的assets获取不正确的问题
	private AssetManager assets;

	@Override
	public AssetManager getAssets() {
		if (assets != null) {
			return assets;
		}
		Resources resources = super.getResources();
		if (resources != null) {
			return assets = resources.getAssets();
		}
		return super.getAssets();
	}
	// #endregion assets

}
