package com.koudai.net.toolbox;

import java.util.Map;

/**
 * Created by zhaoyu on 15/12/18.
 * 网络库可以定制如何处理传入的参数，也可以定制一些公共参数，参数加密之类的需求
 */
public interface IRequestParamsInterceptor {

    Map<String, String> interceptParams(RequestParams customParams);

}
