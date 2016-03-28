package com.koudai.net.toolbox;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

/**
 * Created by zhaoyu on 15/11/3.
 */
public abstract class JsonObjectListParser<T> implements Parser<List<T>> {

    @Override
    public <M> List<T> parse(M object) throws JSONException {
        return parseResponse((JSONArray) object);
    }

    protected abstract List<T> parseResponse(JSONArray object) throws JSONException;

}
