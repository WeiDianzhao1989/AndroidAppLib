package com.koudai.net.kernal;

import com.koudai.net.toolbox.OkHttpClientFactory;

/**
 * Created by zhaoyu on 15/10/29.
 */
public class InternalOkUrlFactory {

    private static class OkUrlFactoryHolder {
        public static final OkUrlFactory instance = new OkUrlFactory(OkHttpClientFactory.createOkHttpClient());
        static {
        }
    }

    public static OkUrlFactory getInstance() {
        return OkUrlFactoryHolder.instance;
    }
}
