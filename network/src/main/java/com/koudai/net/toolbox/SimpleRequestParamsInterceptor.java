package com.koudai.net.toolbox;

import java.util.Map;

/**
 * Created by zhaoyu on 15/12/29.
 * 默认的参数处理器
 */
public class SimpleRequestParamsInterceptor implements IRequestParamsInterceptor {

    @Override
    public Map<String, String> interceptParams(RequestParams customParams) {
        return customParams.toMap();
    }
}
