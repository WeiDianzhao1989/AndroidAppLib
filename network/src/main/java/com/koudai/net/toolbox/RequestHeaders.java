package com.koudai.net.toolbox;

import android.support.v4.util.ArrayMap;

import java.util.Map;

/**
 * Created by zhaoyu on 15/11/16.
 */
public class RequestHeaders {

    private Map<String, String> headers = new ArrayMap<String, String>();


    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void removeHeader(String key) {
        headers.remove(key);
    }

    public void addHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
    }

    public void removeHeaders(Map<String, String> headers) {
        for (String key : headers.keySet()) {
            this.headers.remove(key);
        }
    }

    public Map<String, String> toMap() {
        Map<String, String> headers = new ArrayMap<String, String>();
        for (Map.Entry<String, String> entry : this.headers.entrySet()) {
            headers.put(entry.getKey(), entry.getValue());
        }
        return headers;
    }

    @Override
    public int hashCode() {
        return headers.hashCode();
    }

    @Override
    public String toString() {
        return headers.toString();
    }
}
