package com.weidian.dycontainer.tools;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by krystaljake on 5/16/16.
 */
public final class ReflectCallTool {

    public static <T> T callStaticMethod(Class<?> reflectClass, String methodName, Object... params)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (reflectClass != null) {
            Method m = null;

            if (params != null && params.length > 0) {
                Class<?> parameterTypes[] = new Class<?>[params.length];
                for (int i = 0; i < params.length && i < parameterTypes.length; i++) {
                    parameterTypes[i] = params[i].getClass();
                }
                m = reflectClass.getDeclaredMethod(methodName, parameterTypes);
            } else {
                m = reflectClass.getMethod(methodName);
            }

            if (m == null) {
                return callStaticMethod(reflectClass.getSuperclass(), methodName, params);
            } else {
                m.setAccessible(true);
                return (T) m.invoke(null, params);
            }
        } else {
            throw new IllegalAccessException("no method name " + methodName + " is found");
        }
    }

    public static <T> T getStaticField(Class<?> reflectClass, String fieldName) throws NoSuchFieldException
            , IllegalAccessException {
        if (reflectClass != null) {
            Field f = reflectClass.getDeclaredField(fieldName);
            if (f == null) {
                return getStaticField(reflectClass.getSuperclass(), fieldName);
            } else {
                f.setAccessible(true);
                return (T) f.get(null);
            }
        } else {
            throw new IllegalAccessException("no field name " + fieldName + " is found");
        }
    }

    public void setStaticField(Class<?> reflectClass, String fieldName, Object value) throws NoSuchFieldException,
            IllegalAccessException {
        if (reflectClass != null) {
            Field f = reflectClass.getDeclaredField(fieldName);
            if (f == null) {
                setStaticField(reflectClass.getSuperclass(), fieldName, value);
            } else {
                f.setAccessible(true);
                f.set(null, value);
            }
        } else {
            throw new IllegalAccessException("no field name " + fieldName + " is found");
        }
    }
}
