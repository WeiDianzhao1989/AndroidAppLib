package com.koudai.net.toolbox;

import org.json.JSONException;


/**
 * Created by zhaoyu on 15/11/3.
 */
public interface Parser<T> {

    <M>T parse(M object) throws JSONException;
}
