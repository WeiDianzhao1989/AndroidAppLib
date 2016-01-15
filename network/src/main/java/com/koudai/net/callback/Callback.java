package com.koudai.net.callback;

import com.koudai.net.error.NetworkError;

/**
 * created by zhaoyu
 *
 * @param <T> 响应对象的泛型
 */
public interface Callback<T> {


    void onResponse(T result);

    void onError(NetworkError error);

    void onCancel();

}
