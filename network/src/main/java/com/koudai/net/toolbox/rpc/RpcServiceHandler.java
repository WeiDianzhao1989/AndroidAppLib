package com.koudai.net.toolbox.rpc;

import com.koudai.net.toolbox.rpc.annotation.Host;
import com.koudai.net.toolbox.rpc.annotation.Path;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by krystaljake on 16/6/20.
 */
public class RpcServiceHandler implements InvocationHandler {

    private Class<? extends RpcService> rpcService;
    private String host;
    private String path;

    public RpcServiceHandler(Class<? extends RpcService> rpcService) {
        this.rpcService = rpcService;
        if (this.rpcService.isAnnotationPresent(Host.class)) {
            host = rpcService.getAnnotation(Host.class).host();
        }

        if (this.rpcService.isAnnotationPresent(Path.class)) {
            path = rpcService.getAnnotation(Path.class).path();
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcInvoker rpcInvoker = new RpcInvoker();
        RpcInvokeTarget rpcInvokeTarget = new RpcInvokeTarget(method, args);
        return rpcInvoker.invoke(host, path, rpcInvokeTarget);
    }

}
