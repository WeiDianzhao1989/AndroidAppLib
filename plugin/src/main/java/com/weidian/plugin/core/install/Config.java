package com.weidian.plugin.core.install;

import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public final class Config {
    /*package*/ String label;
    /*package*/ String packageName;
    /*package*/ int version;
    /*package*/ String versionName;
    /*package*/ Drawable icon;
    /*package*/ LinkedHashSet<String> dependence;
    /*package*/ Map<String, String> actionMap;       // <action, className>
    /*package*/ Map<String, ActivityInfo> pageMap;   // <className, info>
    /*package*/ Map<String, String[]> receiverMap;   // <className, actionList>

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
    public Map<String, String> getActionMap() {
        return actionMap;
    }

    /**
     * key: className
     * value: info
     */
    public Map<String, ActivityInfo> getPageMap() {
        return pageMap;
    }

    /**
     * key: className
     * value: actionList
     */
    public Map<String, String[]> getReceiverMap() {
        return receiverMap;
    }

    @Override
    public String toString() {
        return packageName;
    }
}
