package com.koudai.net.toolbox;

import java.util.Map;

/**
 * Created by zhaoyu on 15/12/25.
 */
public interface IRequestHeaderInterceptor {
    Map<String, String> interceptHeader(RequestHeaders customHeaders);
}
