package com.weidian.plugin.core.install;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import com.weidian.plugin.PluginManager;
import com.weidian.plugin.core.ctx.Host;
import com.weidian.plugin.core.ctx.Module;
import com.weidian.plugin.core.ctx.ModuleContext;
import com.weidian.plugin.core.ctx.Plugin;
import com.weidian.plugin.exception.PluginAlreadyLoadedException;
import com.weidian.plugin.exception.PluginInstallException;
import com.weidian.plugin.task.Task;
import com.weidian.plugin.task.TaskManager;
import com.weidian.plugin.task.pool.Priority;
import com.weidian.plugin.task.pool.PriorityExecutor;
import com.weidian.plugin.util.IOUtil;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class Installer {

	private Installer() {
	}

	private final static String ASSETS_MODULE_DIR_NAME = "module";
	private final static String ASSETS_MODULE_VERSION_NAME = "version";
	private final static String ASSETS_MODULE_NAME_SUFFIX = ".png";
	private final static String MODULE_INSTALL_DIR_NAME = "module";
	private final static String MODULE_INSTALL_TEMP_DIR_NAME = "temp";
	private final static String MODULE_NAME_SUFFIX = "_p1.apk";
	private final static String MODULE_UNINSTALL_NAME = "uninstall";

	private static Host host;
	private static PluginManager pluginManager;
	private static volatile boolean initFinished = false;
	private final static Object initLock = new Object();
	private final static Object installLock = new Object();
	private final static ConcurrentHashMap<String, Module> loadedModules = new ConcurrentHashMap<String, Module>(5);
	private final static Executor EXECUTOR = new PriorityExecutor(1); // 单线程执行安装相关操作

	/**
	 * 加载Host及其依赖的模块.
	 * PluginManager初始化时调用这个方法.
	 */
	public synchronized static void initHost(final PluginManager pluginManager) {
		if (host == null) {
			Installer.pluginManager = pluginManager;
			// init host
			Application app = PluginManager.getApplication();
			try {
				Config config = new Config();
				config.packageName = app.getPackageName();
				host = new Host(app, config);// config的其他属性由InitTask延迟补充
			} catch (Throwable ex) {
				pluginManager.onPluginsLoadError(
					new PluginInstallException("init host error", ex, app.getPackageName()),
					false);
			}

			// 1. decompress modules from assets
			// 2. install all modules from temp folder
			// 3. load host dependence
			TaskManager.start(new InitTask());
		}
	}

	/**
	 * 等待初始化完成, 仅提给ClassLoader使用.
	 */
	public static void waitForInit() {
		if (!initFinished) {
			synchronized (initLock) {
				while (!initFinished) {
					try {
						initLock.wait();
					} catch (Throwable ignored) {
					}
				}
			}
		}
	}

	/**
	 * 获取Host实例
	 *
	 * @return
	 */
	public static Host getHost() {
		return host;
	}

	/**
	 * 获取已加载的插件
	 *
	 * @param packageName
	 * @return
	 */
	public static Plugin getLoadedPlugin(final String packageName) {
		Plugin result = loadedModules.get(packageName);
		if (result == null && host.getConfig().getPackageName().equals(packageName)) {
			result = host;
		}
		return result;
	}

	/**
	 * 获取已加载的模块
	 *
	 * @return
	 */
	public static Map<String, Module> getLoadedModules() {
		Map<String, Module> result = new HashMap<String, Module>();
		result.putAll(loadedModules);
		return result;
	}

	/**
	 * 获取已安装的模块packageName
	 *
	 * @return
	 */
	public static Set<String> getInstalledModules() {
		final HashSet<String> result = new HashSet<String>();
		File moduleDir = PluginManager.getApplication().getDir(MODULE_INSTALL_DIR_NAME, 0);
		File[] files = moduleDir.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory() &&
					file.list().length > 0 &&
					!file.getName().equals(MODULE_INSTALL_TEMP_DIR_NAME) &&
					!new File(file, MODULE_UNINSTALL_NAME).exists() &&
					new File(file, file.getName() + MODULE_NAME_SUFFIX).exists()) {
					result.add(file.getName());
				}
			}
		}
		return result;
	}

	/**
	 * 获取已安装的插件的配置信息
	 *
	 * @param packageName
	 * @param callback
	 */
	public static void getInstalledModuleConfig(String packageName, LoadConfigCallback callback) {
		TaskManager.start(new LoadConfigTask(packageName, callback));
	}

	/**
	 * 安装外部插件
	 *
	 * @param externalFile
	 * @param callback
	 */
	public static void installModule(final File externalFile, final InstallCallback callback) {
		TaskManager.start(new InstallTask(externalFile, callback));
	}

	/**
	 * 卸载已安装的模块
	 *
	 * @param packageName
	 * @param callback
	 */
	public static void uninstallModule(final String packageName, final UninstallCallback callback) {
		TaskManager.start(new UninstallTask(packageName, callback));
	}

	/**
	 * 加载已安装插件
	 *
	 * @param packageName
	 * @param callback
	 */
	public static void loadModule(final String packageName, final LoadCallback callback) {
		if (!loadedModules.containsKey(packageName)) {
			TaskManager.start(new LoadTask(packageName, callback));
		}
	}

	/**
	 * 获取插件的so文件
	 *
	 * @param packageName
	 * @param libName
	 * @return
	 */
	public static File findLibrary(String packageName, String libName) {
		File libDir = getModuleInstallDir(packageName);
		return new File(libDir,
			(libName.startsWith("lib") ? libName : "lib" + libName) +
				(libName.endsWith(".so") ? "" : ".so"));
	}

	/**
	 * 获取插件本文件目录
	 */
	public static File getModuleInstallDir(String packageName) {
		File modulesDir = PluginManager.getApplication().getDir(MODULE_INSTALL_DIR_NAME, 0);
		return new File(modulesDir, packageName);
	}

	// #########################################  私有方法和类型  ###################################################

	/**
	 * 第一步:
	 * 解出assets中的插件文件到临时目录
	 *
	 * @throws IOException
	 */
	private synchronized static void decompressAssetsModules() throws IOException {

		if (!isAssetsModulesDecompressed()) { // 如果没有解压过当前版本
			AssetManager assets = PluginManager.getApplication().getAssets();
			String[] list = assets.list(ASSETS_MODULE_DIR_NAME);
			if (list != null) {
				for (String fileName : list) {
					if (fileName.endsWith(ASSETS_MODULE_NAME_SUFFIX)) {
						File tempFile = getTempModuleFile(fileName);
						InputStream in = null;
						FileOutputStream out = null;
						try {
							in = assets.open(ASSETS_MODULE_DIR_NAME + "/" + fileName);
							out = new FileOutputStream(tempFile);
							IOUtil.copy(in, out);
						} finally {
							IOUtil.closeQuietly(in);
							IOUtil.closeQuietly(out);
						}
					}
				}
			}

			setAssetsModulesDecompressed();
		}
	}

	/**
	 * 第二步:
	 * 安装临时文件到最终加载位置
	 *
	 * @param tempModuleFile
	 * @throws IOException
	 */
	private static void installTempModuleFile(File tempModuleFile) throws IOException {
		synchronized (installLock) {
			Config config = null;
			File moduleFile = null;
			ZipInputStream zin = null;
			try {
				// prepare config & pluginFile
				{
					config = ConfigHelper.getModuleConfig(tempModuleFile);
					File installDir = getModuleInstallDir(config.getPackageName());
					if (installDir.exists()) {
						IOUtil.deleteFileOrDir(installDir);
					}
					moduleFile = getModuleFile(config.getPackageName());
					File parentDir = moduleFile.getParentFile();
					if (!parentDir.exists()) {
						parentDir.mkdirs();
					}
				}

				// decompress so files
				ABI sysABI = ABI.getSystemArch();
				zin = new ZipInputStream(new FileInputStream(tempModuleFile));
				ZipEntry entry = null;
				while ((entry = zin.getNextEntry()) != null) {
					if (!entry.isDirectory()) {
						String entryName = entry.getName();
						if (entryName.startsWith("lib/")) {
							int index = entryName.lastIndexOf("/");
							if (index > 0) {
								String soDir = entryName.substring(0, index);
								ABI abi = ABI.fastValueOfSoDir(soDir);
								if (abi == ABI.ARM || abi == sysABI) {
									File outFile = new File(moduleFile.getParent(),
										entryName.substring(index + 1, entryName.length()));
									FileOutputStream out = null;
									try {
										out = new FileOutputStream(outFile);
										IOUtil.copy(zin, out);
									} finally {
										IOUtil.closeQuietly(out);
									}
								}
							}
						}
					}
				}
				if (moduleFile.exists()) {
					IOUtil.deleteFileOrDir(moduleFile);
				}
				if (!tempModuleFile.renameTo(moduleFile)) {
					throw new IOException("mv plugin file error: " + moduleFile.getName());
				}
			} finally {
				IOUtil.closeQuietly(zin);
			}
		}
	}

	/**
	 * 第三步:
	 * 加载已安装的模块
	 *
	 * @param packageName
	 * @param newLoadedModules
	 * @return
	 * @throws PluginInstallException
	 */
	private static Module loadInstalledModule(String packageName, Map<String, Module> newLoadedModules) throws PluginInstallException {
		boolean loadNew = false;
		Module result = null;
		synchronized (installLock) {
			if (loadedModules.containsKey(packageName)) {
				result = loadedModules.get(packageName);
			} else {
				// 创建module
				try {
					File moduleFile = getModuleFile(packageName);
					Config config = ConfigHelper.getModuleConfig(moduleFile);
					if (config != null) {
						result = new Module(new ModuleContext(moduleFile, config), config);
						loadedModules.put(packageName, result);
						newLoadedModules.put(packageName, result);
						loadNew = true;
					} else {
						throw new IOException("read config error!");
					}
				} catch (Throwable ex) {
					throw new PluginInstallException("load module error", ex, packageName);
				}
			}
		}

		if (loadNew) {
			// 加载依赖
			loadDependence(result, newLoadedModules);
		}

		return result;
	}

	/**
	 * @param plugin
	 * @param newLoadedModules
	 * @throws PluginInstallException
	 */
	private static void loadDependence(Plugin plugin, Map<String, Module> newLoadedModules) throws PluginInstallException {
		LinkedHashSet<String> dependence = plugin.getConfig().getDependence();
		if (dependence != null) {
			PluginInstallException exception = new PluginInstallException("load dependence error", null);
			for (String packageName : dependence) {
				try {
					loadInstalledModule(packageName, newLoadedModules);
				} catch (Throwable ex) {
					exception.addPackageName(packageName);
					exception.addEx(ex);
				}
			}
			if (exception.packageNameListCount() > 0) {
				throw exception;
			}
		}
	}

	/**
	 * 最终的插件文件
	 *
	 * @param packageName
	 * @return
	 */
	private static File getModuleFile(String packageName) {
		File moduleDir = getModuleInstallDir(packageName);
		return new File(moduleDir, packageName + MODULE_NAME_SUFFIX);
	}

	/**
	 * 临时的插件文件目录
	 *
	 * @return
	 */
	private static File getTempModuleDir() {
		File tempDir = PluginManager.getApplication().getDir(MODULE_INSTALL_DIR_NAME, 0);
		tempDir = new File(tempDir, MODULE_INSTALL_TEMP_DIR_NAME);
		if (tempDir.exists() || tempDir.mkdirs()) {
			return tempDir;
		} else {
			return null;
		}
	}

	/**
	 * 临时的插件文件
	 *
	 * @param fileName
	 * @return
	 */
	private static File getTempModuleFile(String fileName) {
		File tempDir = getTempModuleDir();
		if (tempDir != null) {
			return new File(tempDir, fileName);
		}
		return null;
	}

	private static void appendUninstallPlugin(String packageName) throws IOException {
		File modulesDir = getModuleInstallDir(packageName);
		if (modulesDir.exists()) {
			File uninstallFile = new File(modulesDir, MODULE_UNINSTALL_NAME);
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(uninstallFile, false);
				IOUtil.writeStr(out, packageName);
			} finally {
				IOUtil.closeQuietly(out);
			}
		}
	}

	private static List<String> getUninstallPlugins() throws IOException {
		List<String> result = new ArrayList<String>(1);
		File moduleDir = PluginManager.getApplication().getDir(MODULE_INSTALL_DIR_NAME, 0);
		File[] files = moduleDir.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory() &&
					!file.getName().equals(MODULE_INSTALL_TEMP_DIR_NAME) &&
					(new File(file, MODULE_UNINSTALL_NAME).exists() ||
						!new File(file, file.getName() + MODULE_NAME_SUFFIX).exists())) {
					result.add(file.getName());
				}
			}
		}
		return result;
	}

	/**
	 * 临时的插件文件目录
	 *
	 * @return
	 */
	private static File getAssetsVersionFile() {
		File result = PluginManager.getApplication().getDir(MODULE_INSTALL_DIR_NAME, 0);
		if (result.exists() || result.mkdirs()) {
			return new File(result, ASSETS_MODULE_VERSION_NAME);
		}
		return null;
	}

	/**
	 * 设置assets中插件解压后的版本
	 */
	private synchronized static void setAssetsModulesDecompressed() {
		FileOutputStream out = null;
		try {
			Config hostConfig = host.getConfig();
			String versionName = hostConfig.versionName;
			int versionCode = hostConfig.version;
			String versionMark = versionName + versionCode;

			File versionFile = getAssetsVersionFile();
			out = new FileOutputStream(versionFile);
			IOUtil.writeStr(out, versionMark);
		} catch (Throwable ex) {
			Log.d("plugin", ex.getMessage(), ex);
		} finally {
			IOUtil.closeQuietly(out);
		}
	}

	/**
	 * 判断assets中插件是否已解压
	 */
	private synchronized static boolean isAssetsModulesDecompressed() {
		if (pluginManager.isDebug()) {
			return false;
		}
		FileInputStream in = null;
		try {
			File versionFile = getAssetsVersionFile();
			if (versionFile.exists()) {
				in = new FileInputStream(versionFile);
				String versionMark = IOUtil.readStr(in);

				Config hostConfig = host.getConfig();
				String versionName = hostConfig.versionName;
				int versionCode = hostConfig.version;
				String versionStr = versionName + versionCode;

				return versionMark.equals(versionStr);
			}
		} catch (Throwable ex) {
			Log.d("plugin", ex.getMessage(), ex);
		} finally {
			IOUtil.closeQuietly(in);
		}
		return false;
	}

	// ###################################### init task ######################################
	private static class InitTask extends Task<Void> {

		private Map<String, Module> newLoadedModules;

		public InitTask() {
			this.newLoadedModules = new HashMap<String, Module>();
		}

		@Override
		public Priority getPriority() {
			return Priority.UI_TOP;
		}

		@Override
		public Executor getExecutor() {
			return EXECUTOR;
		}

		private boolean isMainProcess() {
			int pid = android.os.Process.myPid();
			ActivityManager am =
				(ActivityManager) PluginManager.getApplication().getSystemService(Context.ACTIVITY_SERVICE);
			for (ActivityManager.RunningAppProcessInfo processInfo : am.getRunningAppProcesses()) {
				if (processInfo.pid == pid) {
					return host.getConfig().getPackageName().equals(processInfo.processName);
				}
			}
			return false;
		}

		@Override
		protected Void doBackground() throws Exception {

			// supplement host config
			ConfigHelper.supplementHostConfig(host.getConfig());

			if (!isMainProcess()) {
				return null;
			}

			PluginInstallException exception = new PluginInstallException("init task error", null);
			synchronized (initLock) {
				// try delete modules form uninstall list
				try {
					List<String> uninstallPkgList = getUninstallPlugins();
					if (uninstallPkgList != null) {
						for (String pkg : uninstallPkgList) {
							File path = getModuleInstallDir(pkg);
							IOUtil.deleteFileOrDir(path);
						}
					}
				} catch (Throwable ex) {
					Log.d("plugin", ex.getMessage(), ex);
				}

				// 1. decompress modules from assets
				try {
					decompressAssetsModules();
				} catch (Throwable ex) {
					exception.addEx(ex);
				}

				// 2. install all modules from temp folder
				File[] tempPluginFiles = getTempModuleDir().listFiles();
				for (File tempPluginFile : tempPluginFiles) {
					try {
						installTempModuleFile(tempPluginFile);
					} catch (Throwable ex) {
						exception.addEx(ex);
					}
				}

				// 3. load host dependence
				try {
					loadDependence(host, this.newLoadedModules);
				} catch (Throwable ex) {
					exception.addEx(ex);
				}
				initFinished = true;
				initLock.notifyAll();
			}

			if (exception.exListCount() > 0) {
				throw exception;
			}
			return null;
		}

		@Override
		protected void onFinished(Void result) {
			initFinished = true;
			pluginManager.onHostInitialised();
			if (this.newLoadedModules.size() > 0) {
				pluginManager.onModulesLoaded(this.newLoadedModules);
			}
		}

		@Override
		protected void onError(Throwable ex, boolean isCallbackError) {
			initFinished = true;
			pluginManager.onPluginsLoadError(ex, isCallbackError);
			pluginManager.onHostInitialised();
			if (this.newLoadedModules.size() > 0) {
				pluginManager.onModulesLoaded(this.newLoadedModules);
			}
		}
	}

	// ###################################### install task ######################################
	private static class InstallTask extends Task<Module> {

		private final File externalFile;
		private InstallCallback callback;
		private Map<String, Module> newLoadedModules;

		public InstallTask(File externalFile, InstallCallback callback) {
			this.externalFile = externalFile;
			this.callback = callback;
			this.newLoadedModules = new HashMap<String, Module>();
		}

		@Override
		public Executor getExecutor() {
			return EXECUTOR;
		}

		@Override
		protected Module doBackground() throws Exception {
			Module result = null;

			File tempFile = null;
			Config config = null;
			boolean needRestart = false;
			synchronized (installLock) {
				// 1. 解析config.
				// 如果存在已安装的相同版本提示用户, 停止安装.
				config = ConfigHelper.getModuleConfig(this.externalFile);
				if (loadedModules.containsKey(config.getPackageName())) {
					Plugin plugin = getLoadedPlugin(config.getPackageName());
					if (config.getVersion() == plugin.getConfig().getVersion()) {
						// 存在已安装的相同版本提示用户, 停止安装.
						throw new PluginAlreadyLoadedException(config.getPackageName());
					} else {
						needRestart = true;
					}
				}

				// 2. 拷贝到安装临时文件.
				tempFile = getTempModuleFile(config.getPackageName());
				FileOutputStream out = null;
				InputStream in = null;
				try {
					in = new FileInputStream(this.externalFile);
					out = new FileOutputStream(tempFile);
					IOUtil.copy(in, out);
				} finally {
					IOUtil.closeQuietly(in);
					IOUtil.closeQuietly(out);
				}
			}

			// 3. 安装
			installTempModuleFile(tempFile);

			// 4. 加载
			if (!needRestart) {
				result = loadInstalledModule(config.getPackageName(), this.newLoadedModules);
			}

			return result;
		}

		@Override
		protected void onFinished(Module result) {
			if (this.newLoadedModules.size() > 0) {
				pluginManager.onModulesLoaded(this.newLoadedModules);
			}
			if (this.callback != null) {
				this.callback.callback(result, result == null);
			}
		}

		@Override
		protected void onError(Throwable ex, boolean isCallbackError) {
			if (this.callback != null) {
				this.callback.error(ex, isCallbackError);
			} else {
				pluginManager.onPluginsLoadError(ex, isCallbackError);
			}
			if (this.newLoadedModules.size() > 0) {
				pluginManager.onModulesLoaded(this.newLoadedModules);
			}
		}
	}

	// ###################################### load task ######################################
	private static class LoadTask extends Task<Module> {

		private final String packageName;
		private LoadCallback callback;
		private Map<String, Module> newLoadedModules;

		public LoadTask(String packageName, LoadCallback callback) {
			this.packageName = packageName;
			this.callback = callback;
			this.newLoadedModules = new HashMap<String, Module>();
		}

		@Override
		public Executor getExecutor() {
			return EXECUTOR;
		}

		@Override
		protected Module doBackground() throws Exception {
			return loadInstalledModule(this.packageName, this.newLoadedModules);
		}

		@Override
		protected void onFinished(Module result) {
			if (this.newLoadedModules.size() > 0) {
				pluginManager.onModulesLoaded(this.newLoadedModules);
			}
			if (this.callback != null) {
				this.callback.callback(result);
			}
		}

		@Override
		protected void onError(Throwable ex, boolean isCallbackError) {
			if (this.callback != null) {
				this.callback.error(ex, isCallbackError);
			} else {
				pluginManager.onPluginsLoadError(ex, isCallbackError);
			}
			if (this.newLoadedModules.size() > 0) {
				pluginManager.onModulesLoaded(this.newLoadedModules);
			}
		}
	}

	// ################################# uninstall task ############################
	private static class UninstallTask extends Task<Boolean> {

		private final String packageName;
		private UninstallCallback callback;

		public UninstallTask(String packageName, UninstallCallback callback) {
			this.packageName = packageName;
			this.callback = callback;
		}

		@Override
		public Executor getExecutor() {
			return EXECUTOR;
		}

		@Override
		protected Boolean doBackground() throws Exception {
			boolean needRestart = false;
			synchronized (installLock) {
				needRestart = loadedModules.containsKey(packageName);

				File path = getModuleInstallDir(packageName);
				if (!IOUtil.deleteFileOrDir(path)) {
					needRestart = true;
					appendUninstallPlugin(packageName);
				}

				loadedModules.remove(packageName);
			}

			return needRestart;
		}

		@Override
		protected void onFinished(Boolean result) {
			if (callback != null) {
				callback.callback(result);
			}
		}

		@Override
		protected void onError(Throwable ex, boolean isCallbackError) {
			if (callback != null) {
				callback.error(ex, isCallbackError);
			}
		}
	}

	// ###################################### load config task ######################################
	private static class LoadConfigTask extends Task<Config> {

		private final String packageName;
		private LoadConfigCallback callback;

		public LoadConfigTask(String packageName, LoadConfigCallback callback) {
			this.packageName = packageName;
			this.callback = callback;
		}

		@Override
		public Executor getExecutor() {
			return EXECUTOR;
		}

		@Override
		protected Config doBackground() throws Exception {
			Plugin plugin = getLoadedPlugin(packageName);
			if (plugin != null) {
				return plugin.getConfig();
			} else {
				return ConfigHelper.getModuleConfig(getModuleFile(packageName));
			}
		}

		@Override
		protected void onFinished(Config result) {
			if (this.callback != null) {
				this.callback.callback(result);
			}
		}

		@Override
		protected void onError(Throwable ex, boolean isCallbackError) {
			if (this.callback != null) {
				this.callback.error(ex, isCallbackError);
			}
		}
	}
}
