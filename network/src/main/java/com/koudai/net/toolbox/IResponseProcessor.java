package com.koudai.net.toolbox;

import com.koudai.net.kernal.Response;

import java.io.IOException;

/**
 * Created by zhaoyu on 16/1/6.
 */
public interface IResponseProcessor {

    String process(HttpRequest<?> request, Response response) throws RuntimeException
            , IOException;
}
