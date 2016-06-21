package com.koudai.net.toolbox;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaoyu on 15/11/16.
 */
public final class RequestParams {

    private Map<String, String> params = new HashMap<String, String>();

    private Map<String, List<String>> multiParams = new HashMap<String, List<String>>();

    public void addParam(String key, String value) {
        if (params.containsKey(key)) {
            List<String> values = multiParams.get(key);
            if (values == null) {
                values = new ArrayList<String>();
                values.add(params.get(key));
            }
            values.add(value);
            JSONArray valuesJson = new JSONArray(values);
            params.put(key, valuesJson.toString());
        } else {
            params.put(key, value);
        }
    }

    public void putParam(String key, String value) {
        params.put(key, value);
        multiParams.put(key, new ArrayList<String>());
    }

    public void removeParam(String key) {
        params.remove(key);
        multiParams.remove(key);
    }

    public Map<String, String> toMap() {
        Map<String, String> params = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : this.params.entrySet()) {
            params.put(entry.getKey(), entry.getValue());
        }
        return params;
    }

    public void putAll(Map<String, String> params) {
        this.params.putAll(params);
    }

    public void putAll(RequestParams params) {
        this.params.putAll(params.toMap());
    }

    @Override
    public int hashCode() {
        return params.hashCode();
    }

    @Override
    public String toString() {
        return params.toString();
    }
}
