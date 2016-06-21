package com.koudai.net.toolbox.rpc;

import java.lang.reflect.Proxy;

/**
 * Created by krystaljake on 16/6/20.
 */
public final class RpcServiceFactory {


    public <T extends RpcService> T createRpcService(Class<? extends RpcService> rpcServiceClass) {
        if (RpcService.class.isAssignableFrom(rpcServiceClass)) {
            throw new ClassCastException("every rpc service must extends RpcService interface");
        }

        RpcServiceHandler rpcServiceHandler = new RpcServiceHandler(rpcServiceClass);


        return (T) Proxy.newProxyInstance(rpcServiceClass.getClassLoader(), new Class<?>[]{rpcServiceClass}, rpcServiceHandler);
    }

}
