package com.koudai.net.toolbox.rpc;

import com.koudai.net.callback.Callback;
import com.koudai.net.toolbox.Parser;
import com.koudai.net.toolbox.RequestHeaders;
import com.koudai.net.toolbox.RequestParams;
import com.koudai.net.toolbox.rpc.annotation.BodyParameter;
import com.koudai.net.toolbox.rpc.annotation.Get;
import com.koudai.net.toolbox.rpc.annotation.Header;
import com.koudai.net.toolbox.rpc.annotation.Host;
import com.koudai.net.toolbox.rpc.annotation.Path;
import com.koudai.net.toolbox.rpc.annotation.Post;
import com.koudai.net.toolbox.rpc.annotation.QueryParameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by krystaljake on 16/6/20.
 */
final class RpcInvokeTarget {

    public Method rpcServiceMethod;
    public Object args[];
    public String host;
    public String path;
    public int httpMethod;
    //加密
    public RequestParams encryptBodyParams = new RequestParams();
    public RequestParams encryptQueryParams = new RequestParams();
    //不加密
    public RequestParams bodyParams = new RequestParams();
    public RequestParams queryParams = new RequestParams();

    public RequestHeaders headers = new RequestHeaders();
    public Parser responseParser;
    public Callback callback;

    public RpcInvokeTarget(Method rpcServiceMethod, Object args[]) {
        this.rpcServiceMethod = rpcServiceMethod;
        this.args = args;

        if (rpcServiceMethod.isAnnotationPresent(Get.class)) {
            this.httpMethod = HttpMethod.GET;
        } else if (rpcServiceMethod.isAnnotationPresent(Post.class)) {
            this.httpMethod = HttpMethod.POST;
        } else {
            this.httpMethod = HttpMethod.GET;
        }

        if (rpcServiceMethod.isAnnotationPresent(Host.class)) {
            host = rpcServiceMethod.getAnnotation(Host.class).host();
        }

        if (rpcServiceMethod.isAnnotationPresent(Path.class)) {
            path = rpcServiceMethod.getAnnotation(Path.class).path();
        }

        Annotation[][] rpcAnnotations = rpcServiceMethod.getParameterAnnotations();

        for (int i = 0; i < rpcAnnotations.length && i < this.args.length; i++) {
            final Object param = args[i];
            Annotation[] parameterAnnotations = rpcAnnotations[i];
            for (int j = 0; j < parameterAnnotations.length; j++) {
                parse(param, parameterAnnotations[j]);
            }

        }

    }

    private void parse(Object value, Annotation annotation) {
        if (annotation.annotationType() == Host.class) {
            Host host = (Host) annotation;
            this.host = host.host();
        } else if (annotation.annotationType() == Path.class) {
            Path path = (Path) annotation;
            this.path = path.path();
        } else if (annotation.annotationType() == BodyParameter.class) {
            BodyParameter bp = (BodyParameter) annotation;
            if (bp.isNeedEncrypt()) {
                if (value instanceof Map) {
                    Map<String, String> params = (Map<String, String>) value;
                    encryptQueryParams.putAll(params);
                } else {
                    encryptBodyParams.addParam(bp.key(), value.toString());
                }
            } else {
                if (value instanceof Map) {
                    Map<String, String> params = (Map<String, String>) value;
                    bodyParams.putAll(params);
                } else {
                    bodyParams.addParam(bp.key(), value.toString());
                }
            }
        } else if (annotation.annotationType() == QueryParameter.class) {
            QueryParameter qp = (QueryParameter) annotation;
            if (qp.isNeedEncrypt()) {
                if (value instanceof Map) {
                    Map<String, String> params = (Map<String, String>) value;
                    encryptQueryParams.putAll(params);
                } else {
                    encryptQueryParams.addParam(qp.key(), value.toString());
                }
            } else {
                if (value instanceof Map) {
                    Map<String, String> params = (Map<String, String>) value;
                    queryParams.putAll(params);
                } else {
                    queryParams.addParam(qp.key(), value.toString());
                }
            }
        } else if (annotation.annotationType() == Header.class) {
            Header header = (Header) annotation;
            headers.addHeader(header.key(), value.toString());
        } else if (annotation.annotationType() == com.koudai.net.toolbox.rpc.annotation.Parser.class) {
            this.responseParser = (Parser<?>) value;
        } else if (annotation.annotationType() == com.koudai.net.toolbox.rpc.annotation.Callback.class) {
            this.callback = (Callback<?>) value;
        }
    }
}
