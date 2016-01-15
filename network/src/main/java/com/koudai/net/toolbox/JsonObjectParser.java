package com.koudai.net.toolbox;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by zhaoyu on 15/11/3.
 * 网络解析，一般会把网络解析写在这块，如果写在request里也可以，但不建议
 * 最好抽出来，也方便查找
 * T是最后解析出来的实体类的类型
 */
public abstract class JsonObjectParser<T> implements Parser<T> {

    @Override
    public <M> T parse(M object) throws JSONException {
        return parseResponse((JSONObject)object);
    }

    protected abstract T parseResponse(JSONObject object) throws JSONException;
}
