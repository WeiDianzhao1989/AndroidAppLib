package com.weidian.plugin.core.install;

import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;

import java.util.HashMap;
import java.util.LinkedHashSet;

public final class Config {
	/*package*/ String label;
	/*package*/ String packageName;
	/*package*/ int version;
	/*package*/ String versionName;
	/*package*/ Drawable icon;
	/*package*/ LinkedHashSet<String> dependence;
	/*package*/ HashMap<String, String> actionMap;       // <action, className>
	/*package*/ HashMap<String, ActivityInfo> pageMap;   // <className, info>
	/*package*/ HashMap<String, String[]> receiverMap;   // <className, actionList>

	public String getLabel() {
		return label;
	}

	public String getPackageName() {
		return packageName;
	}

	public int getVersion() {
		return version;
	}

	public String getVersionName() {
		return versionName;
	}

	public Drawable getIcon() {
		return icon;
	}

	public LinkedHashSet<String> getDependence() {
		return dependence;
	}

	/**
	 * key: action
	 * value: className
	 */
	public HashMap<String, String> getActionMap() {
		return actionMap;
	}

	/**
	 * key: className
	 * value: info
	 */
	public HashMap<String, ActivityInfo> getPageMap() {
		return pageMap;
	}

	/**
	 * key: className
	 * value: actionList
	 */
	public HashMap<String, String[]> getReceiverMap() {
		return receiverMap;
	}

	@Override
	public String toString() {
		return packageName;
	}
}
