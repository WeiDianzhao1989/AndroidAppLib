package com.koudai.net.callback;

import com.koudai.net.toolbox.HttpRequest;

/**
 * @param <T>
 */
public interface RetryCallback<T extends HttpRequest<?>> {
    T onRetry(T originalRequest);
}
