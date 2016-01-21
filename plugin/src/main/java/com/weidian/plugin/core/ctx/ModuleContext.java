package com.weidian.plugin.core.ctx;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import com.weidian.plugin.PluginManager;
import com.weidian.plugin.core.install.Config;
import com.weidian.plugin.util.ReflectUtil;

import java.io.File;

public final class ModuleContext extends ContextThemeWrapper {

	/*package*/ final ModuleClassLoader classLoader;
	private final File pluginFile;
	private LayoutInflater layoutInflater;
	private Resources.Theme theme;
	private AssetManager assetManager;
	private Resources resources;
	private Configuration overrideConfiguration;

	public ModuleContext(File pluginFile, Config config) {
		super(PluginManager.getApplication(), 0);
		this.pluginFile = pluginFile;

		// init classLoader
		classLoader = new ModuleClassLoader(pluginFile, config);
	}

	@Override
	public Object getSystemService(String name) {
		if (Context.LAYOUT_INFLATER_SERVICE.equals(name)) {
			if (layoutInflater == null) {
				LayoutInflater service = (LayoutInflater) super.getSystemService(name);
				this.layoutInflater = service.cloneInContext(this);
			}
			return layoutInflater;
		}
		return super.getSystemService(name);
	}

	@Override
	public Resources.Theme getTheme() {
		if (this.theme == null) {
			Resources.Theme oldTheme = super.getTheme();
			this.theme = this.getResources().newTheme();
			this.theme.setTo(oldTheme);
		}
		return this.theme;
	}

	@Override
	public AssetManager getAssets() {
		if (assetManager == null) {
			synchronized (this) {
				if (assetManager == null) {
					String apkPath = this.pluginFile.getAbsolutePath();
					try {
						this.assetManager = AssetManager.class.newInstance();
					} catch (Throwable ex) {
						throw new RuntimeException("Plugin init failed:", ex);
					}
					if (assetManager != null) {
						int cookie = ReflectUtil.addAssetPath(assetManager, apkPath);
						if (cookie == 0) {
							throw new RuntimeException(
								"Plugin init failed: addAssets Failed:" + apkPath + "#" + cookie);
						}
					}
				}
			}
		}
		return assetManager;
	}

	@Override
	public Resources getResources() {
		if (resources == null) {
			synchronized (this) {
				if (resources == null) {
					// Resources parent = super.getResources(); 不能这样用, 中兴部分机型(P188T51)会陷入死循环.
					Resources parent = PluginManager.getApplication().getResources();
					resources =
						new ResourcesProxy(
							getAssets(),
							parent.getDisplayMetrics(),
							overrideConfiguration == null ?
								parent.getConfiguration() : overrideConfiguration);
				}
			}
		}
		return resources;
	}

	@Override
	public void applyOverrideConfiguration(Configuration overrideConfiguration) {
		this.overrideConfiguration = new Configuration(overrideConfiguration);
	}

	@Override
	public ClassLoader getClassLoader() {
		return classLoader;
	}
}
