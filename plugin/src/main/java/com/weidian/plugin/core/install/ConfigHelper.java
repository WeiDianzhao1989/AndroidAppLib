package com.weidian.plugin.core.install;

import android.app.Application;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.weidian.plugin.HostApplication;
import com.weidian.plugin.PluginManager;
import com.weidian.plugin.exception.PluginConfigException;
import com.weidian.plugin.exception.PluginVerifyException;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;

/*package*/ class ConfigHelper {

    private final static String META_ACTION = "action";
    private final static String META_DEPENDENCE = "dependence";

    private ConfigHelper() {
    }

    public static void supplementHostConfig(final Config config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }
        Application app = PluginManager.getApplication();
        try {
            PackageManager pm = app.getPackageManager();
            PackageInfo pkgInfo = pm.getPackageInfo(app.getPackageName(),
                    PackageManager.GET_ACTIVITIES | PackageManager.GET_META_DATA);
            if (pkgInfo != null) {
                config.packageName = pkgInfo.packageName;
                config.version = pkgInfo.versionCode;
                config.versionName = pkgInfo.versionName;
                config.actionMap = new HashMap<String, String>();

                if (pkgInfo.activities != null) {
                    config.pageMap = new HashMap<String, ActivityInfo>(pkgInfo.activities.length);
                    for (ActivityInfo info : pkgInfo.activities) {
                        if (info.metaData != null) {
                            String actionStr = info.metaData.getString(META_ACTION);
                            if (!TextUtils.isEmpty(actionStr)) {
                                String className = info.name.startsWith(".") ? info.packageName + info.name : info.name;
                                config.pageMap.put(className, info);
                                String[] actions = actionStr.replaceAll("\\s*", "").split(",");
                                for (String action : actions) {
                                    config.actionMap.put(action, className);
                                }
                            }
                        }
                    }
                }

                ApplicationInfo appInfo = pkgInfo.applicationInfo;
                if (appInfo != null) {
                    if (appInfo.metaData != null) {
                        String depStr = appInfo.metaData.getString(META_DEPENDENCE);
                        if (!TextUtils.isEmpty(depStr)) {
                            String[] depArray = depStr.replaceAll("\\s*", "").split(",");
                            if (depArray != null && depArray.length > 0) {
                                config.dependence = new LinkedHashSet<String>(depArray.length);
                                Collections.addAll(config.dependence, depArray);
                            }
                        }
                    }
                }
            }
        } catch (Throwable ex) {
            throw new PluginConfigException(app.getPackageCodePath(), ex);
        }
    }

    public static Config getModuleConfig(final File pluginFile) {

        // 验证文件
        Application app = PluginManager.getApplication();
        if (!((HostApplication) app).verifyPluginFile(pluginFile)) {
            throw new PluginVerifyException(pluginFile.getName());
        }

        // 读取配置信息
        Config config = null;
        try {
            PackageManager pm = app.getPackageManager();
            PackageInfo pkgInfo = pm.getPackageArchiveInfo(pluginFile.getAbsolutePath(),
                    PackageManager.GET_META_DATA |
                            PackageManager.GET_ACTIVITIES |
                            PackageManager.GET_RECEIVERS);
            if (pkgInfo != null) {
                config = new Config();
                config.packageName = pkgInfo.packageName;
                config.version = pkgInfo.versionCode;
                config.versionName = pkgInfo.versionName;
                config.actionMap = new HashMap<String, String>();

                if (pkgInfo.activities != null) {
                    config.pageMap = new HashMap<String, ActivityInfo>(pkgInfo.activities.length);
                    for (ActivityInfo info : pkgInfo.activities) {
                        if (info.metaData != null) {
                            String actionStr = info.metaData.getString(META_ACTION);
                            if (!TextUtils.isEmpty(actionStr)) {
                                String className = info.name.startsWith(".") ? info.packageName + info.name : info.name;
                                config.pageMap.put(className, info);
                                String[] actions = actionStr.replaceAll("\\s*", "").split(",");
                                for (String action : actions) {
                                    config.actionMap.put(action, className);
                                }
                            }
                        }
                    }
                }

                if (pkgInfo.receivers != null) {
                    config.receiverMap = new HashMap<String, String[]>(pkgInfo.receivers.length);
                    for (ActivityInfo info : pkgInfo.receivers) {
                        if (info.metaData != null) {
                            String actionStr = info.metaData.getString(META_ACTION);
                            if (!TextUtils.isEmpty(actionStr)) {
                                String className = info.name.startsWith(".") ? info.packageName + info.name : info.name;
                                String[] actions = actionStr.replaceAll("\\s*", "").split(",");
                                config.receiverMap.put(className, actions);
                            }
                        }
                    }
                }

                ApplicationInfo appInfo = pkgInfo.applicationInfo;
                if (appInfo != null) {
                    if (appInfo.metaData != null) {
                        String depStr = appInfo.metaData.getString(META_DEPENDENCE);
                        if (!TextUtils.isEmpty(depStr)) {
                            String[] depArray = depStr.replaceAll("\\s*", "").split(",");
                            if (depArray != null && depArray.length > 0) {
                                config.dependence = new LinkedHashSet<String>(depArray.length);
                                Collections.addAll(config.dependence, depArray);
                            }
                        }
                    }

                    try {
                        appInfo.sourceDir = pluginFile.getAbsolutePath();
                        appInfo.publicSourceDir = appInfo.sourceDir;
                        CharSequence label = appInfo.loadLabel(pm);
                        config.label = label == null ? "" : label.toString();
                        config.icon = appInfo.loadIcon(pm);
                    } catch (Throwable ex) {
                        Log.e("plugin", "load icon error", ex);
                    }
                }
            }
        } catch (Throwable ex) {
            throw new PluginConfigException(pluginFile.getName(), ex);
        }

        return config;
    }
}
