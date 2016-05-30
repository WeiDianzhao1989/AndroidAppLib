package com.weidian.plugin.util;

import android.content.res.AssetManager;

import java.lang.reflect.Method;

public class ReflectUtil {

    private static Method addAssetPathMethod;
    private static Method findClassMethod;

    static {
        try {
            addAssetPathMethod = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
            addAssetPathMethod.setAccessible(true);
            findClassMethod = ClassLoader.class.getDeclaredMethod("findClass", String.class);
            findClassMethod.setAccessible(true);
        } catch (Throwable ex) {
            throw new RuntimeException("Plugin init failed", ex);
        }
    }

    private ReflectUtil() {
    }

    /**
     * 在App的classLoader被修改之前调用,
     * 防止ReflectUtil在load时陷入findClass的死循环.
     */
    public static void init() {
    }

    public static int addAssetPath(AssetManager assetManager, String path) {
        try {
            return (Integer) addAssetPathMethod.invoke(assetManager, path);
        } catch (Throwable ignored) {
        }
        return 0;
    }

    public static Class<?> findClass(ClassLoader classLoader, String className) {
        try {
            return (Class<?>) findClassMethod.invoke(classLoader, className);
        } catch (Throwable ignored) {
        }
        return null;
    }
}