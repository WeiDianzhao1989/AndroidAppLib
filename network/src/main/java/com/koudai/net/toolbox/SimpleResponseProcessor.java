package com.koudai.net.toolbox;

import com.koudai.net.kernal.Response;
import com.koudai.net.kernal.ResponseBody;

import java.io.IOException;

/**
 * Created by zhaoyu on 16/1/6.
 */
public class SimpleResponseProcessor implements IResponseProcessor {
    @Override
    public String process(Response response) throws RuntimeException, IOException {
        ResponseBody responseBody = response.body();
        String responseString = "";
        if (responseBody != null) {
            responseString = responseBody.string();
        }
        return responseString;
    }
}
