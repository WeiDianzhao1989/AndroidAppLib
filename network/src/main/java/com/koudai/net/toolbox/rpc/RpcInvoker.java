package com.koudai.net.toolbox.rpc;

import android.text.TextUtils;

import com.koudai.net.toolbox.HttpGetRequest;
import com.koudai.net.toolbox.HttpPostRequest;
import com.koudai.net.toolbox.HttpRequest;
import com.koudai.net.toolbox.NetworkFetcher;
import com.koudai.net.toolbox.RequestControl;

/**
 * Created by krystaljake on 16/6/20.
 */
final class RpcInvoker {

    public RequestControl invoke(String commonHost,
                                 String commonPath, RpcInvokeTarget target) {

        StringBuilder url = new StringBuilder(2048);

        if (!TextUtils.isEmpty(commonHost) && TextUtils.isEmpty(target.host)) {
            url.append(commonHost);
        } else if (TextUtils.isEmpty(commonHost) && !TextUtils.isEmpty(target.host)) {
            url.append(target.host);
        } else {

        }

        url.append(commonPath).append(target.path);
        HttpRequest<?> request = null;

        switch (target.httpMethod) {
            case HttpMethod.GET: {
                request = createGetRequest(url.toString(), target);
                break;
            }
            case HttpMethod.POST: {
                request = createPostRequest(url.toString(), target);
                break;
            }
        }

        if (request != null) {
            return NetworkFetcher.getInstance().execute(request);
        }

        return null;
    }

    private HttpRequest<?> createGetRequest(String url, RpcInvokeTarget target) {
        HttpGetRequest<?> getRequest =
                new HttpGetRequest.Builder<>()
                        .url(url)
                        .headers(target.headers)
                        .params(target.encryptQueryParams)
                        .nonEncryptParams(target.queryParams)
                        .parser(target.responseParser)
                        .callback(target.callback)
                        .build();
        return getRequest;
    }

    private HttpRequest<?> createPostRequest(String url, RpcInvokeTarget target) {
        HttpPostRequest<?> postRequest = new HttpPostRequest.Builder<>()
                .url(url)
                .headers(target.headers)
                .params(target.encryptBodyParams)
                .nonEncryptParams(target.bodyParams)
                .parser(target.responseParser)
                .callback(target.callback)
                .build();
        return postRequest;
    }


}
