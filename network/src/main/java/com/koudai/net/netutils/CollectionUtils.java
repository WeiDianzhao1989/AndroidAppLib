package com.koudai.net.netutils;

import android.support.v4.util.ArrayMap;

import java.util.List;
import java.util.Map;

/**
 * Created by zhaoyu on 15/11/6.
 */
public final class CollectionUtils {

    public static final Map<String,String> EMPTY_STRING_MAP = new ArrayMap<String, String>();

    public static boolean isMapEmpty(Map<?, ?> map) {

        return map == null || map.isEmpty();

    }

    public static boolean isListEmpty(List<?> list) {

        return list == null || list.isEmpty();

    }

}
