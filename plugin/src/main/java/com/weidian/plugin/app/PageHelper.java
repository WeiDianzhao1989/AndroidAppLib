package com.weidian.plugin.app;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.text.TextUtils;
import com.weidian.plugin.MsgCallback;
import com.weidian.plugin.PluginManager;
import com.weidian.plugin.PluginMsg;
import com.weidian.plugin.core.ctx.ContextProxy;
import com.weidian.plugin.exception.StartActivityException;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: wyouflf
 * @date: 2014/11/24
 */
public class PageHelper {
	private PageHelper() {
	}

	// 页面信息
	public final static String PAGE_INFO_CMD = "getPageInfo";
	public final static String PAGE_INFO_KEY = "pageInfo";
	public final static String PAGE_CLASS_KEY = "pageClass";

	// action和约定
	public final static String ACTION_PREFIX = "plugin."; // 插件的action前缀
	public final static String ACTION_HOST_ACTIVITY = "action.plugin.Activity";
	public final static String HOST_ACTIVITY = "com.autonavi.plugin.app.HostActivity";

	// 记录目标Activity
	private final static String TARGET_ACTIVITY_PREF = "TARGET_ACTIVITY_PREF";
	private final static String TARGET_ACTIVITY_CLASS_KEY = "TARGET_ACTIVITY_CLASS_KEY";

	private static WeakReference<Activity> lastActivityRef = null;
	private static final HashMap<String, IntentRedirector> INTENT_REDIRECTOR_MAP = new HashMap(1);

	/**
	 * 返回最近一次打开的插件activity
	 *
	 * @return
	 */
	public static Activity getLastActivity() {
		return lastActivityRef == null ? null : lastActivityRef.get();
	}

	public static void setLastActivity(Activity lastActivity) {
		lastActivityRef = new WeakReference<Activity>(lastActivity);
	}

	public static void putIntentRedirector(String actionOrActivity, IntentRedirector redirector) {
		synchronized (INTENT_REDIRECTOR_MAP) {
			INTENT_REDIRECTOR_MAP.put(actionOrActivity, redirector);
		}
	}

	public static int getThemeResId(Activity activity, ContextProxy contextProxy) {
		HashMap<String, ActivityInfo> pageMap = contextProxy.getPlugin().getConfig().getPageMap();
		if (pageMap != null) {
			ActivityInfo info = pageMap.get(activity.getClass().getName());
			if (info != null) {
				return info.theme;
			}
		}
		return 0;
	}

	public static void initTitleBar(Activity activity, ContextProxy contextProxy) {
		HashMap<String, ActivityInfo> pageMap = contextProxy.getPlugin().getConfig().getPageMap();
		if (pageMap != null) {
			ActivityInfo info = pageMap.get(activity.getClass().getName());
			if (info != null) {
				boolean hasActionBar = false;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
					ActionBar actionBar = activity.getActionBar();
					if (actionBar != null) {
						hasActionBar = true;
						if (info.labelRes > 0) {
							actionBar.setTitle(info.labelRes);
						} else if (info.nonLocalizedLabel != null && info.nonLocalizedLabel.length() > 0) {
							activity.setTitle(info.nonLocalizedLabel);
						}
						if (info.icon > 0) {
							actionBar.setIcon(info.icon);
						}
					}
				}

				if (!hasActionBar) {
					if (info.labelRes > 0) {
						activity.setTitle(info.labelRes);
					} else if (info.nonLocalizedLabel != null && info.nonLocalizedLabel.length() > 0) {
						activity.setTitle(info.nonLocalizedLabel);
					}
				}
			}
		}
	}

	private static Class<?> targetActivityClass;

	public static Class<?> getTargetActivityClass() throws ClassNotFoundException {
		if (targetActivityClass == null) {
			SharedPreferences pref = PluginManager.getApplication().getSharedPreferences(TARGET_ACTIVITY_PREF, 0);
			String targetActivityClassName = pref.getString(TARGET_ACTIVITY_CLASS_KEY, null);
			if (targetActivityClassName != null) {
				targetActivityClass = Class.forName(targetActivityClassName);
			}
		}
		return targetActivityClass;
	}

	@SuppressLint("NewApi")
	private static void setTargetActivityClass(Class<?> targetActivityClass) {
		PageHelper.targetActivityClass = targetActivityClass;
		SharedPreferences pref = PluginManager.getApplication().getSharedPreferences(TARGET_ACTIVITY_PREF, 0);
		SharedPreferences.Editor editor = pref.edit().putString(TARGET_ACTIVITY_CLASS_KEY, targetActivityClass.getName());
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
			editor.apply();
		} else {
			editor.commit();
		}
	}

	public static void startActivity(final Intent intent, final Runnable callSuper) {

		synchronized (INTENT_REDIRECTOR_MAP) {
			if (INTENT_REDIRECTOR_MAP.size() > 0) {
				ComponentName comp = intent.getComponent();
				if (comp != null) {
					String activityClsName = comp.getClassName();
					if (!TextUtils.isEmpty(activityClsName) && INTENT_REDIRECTOR_MAP.containsKey(activityClsName)) {
						IntentRedirector redirector = INTENT_REDIRECTOR_MAP.get(activityClsName);
						redirector.redirect(intent);
					} else {
						String action = intent.getAction();
						if (!TextUtils.isEmpty(action) && INTENT_REDIRECTOR_MAP.containsKey(action)) {
							IntentRedirector redirector = INTENT_REDIRECTOR_MAP.get(action);
							redirector.redirect(intent);
						}
					}
				}
			}
		}

		String targetAction = intent.getAction();
		if (targetAction != null && targetAction.startsWith(PageHelper.ACTION_PREFIX)) {
			intent.setAction(PageHelper.ACTION_HOST_ACTIVITY);
			PluginMsg msg = new PluginMsg(intent.getPackage(), PageHelper.PAGE_INFO_CMD);
			msg.put("action", targetAction);
			intent.setPackage(PluginManager.getApplication().getPackageName());
			PluginManager.sendMsg(msg, new MsgCallback() {
				@Override
				public void callback(Map<String, Object> result) {
					Class<?> activityClass = (Class<?>) result.get(PageHelper.PAGE_CLASS_KEY);
					if (activityClass != null) {
						PageHelper.setTargetActivityClass(activityClass);
						callSuper.run();
					}
				}

				@Override
				public void error(Throwable ex, boolean isCallbackError) {
					throw new StartActivityException(intent.toString(), ex);
				}
			});
		} else {
			callSuper.run();
		}
	}

	public interface IntentRedirector {
		void redirect(Intent intent);
	}
}
