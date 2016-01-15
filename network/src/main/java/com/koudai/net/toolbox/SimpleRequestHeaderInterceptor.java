package com.koudai.net.toolbox;

import java.util.Map;

/**
 * Created by zhaoyu on 15/12/29.
 */
public class SimpleRequestHeaderInterceptor implements IRequestHeaderInterceptor {
    @Override
    public Map<String, String> interceptHeader(RequestHeaders customHeaders) {
        return customHeaders.toMap();
    }
}
