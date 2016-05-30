package com.weidian.plugin.exception;

public class PluginAlreadyLoadedException extends RuntimeException {
    private String packageName;

    public PluginAlreadyLoadedException(String packageName) {
        super("already loaded:" + packageName);
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }
}
