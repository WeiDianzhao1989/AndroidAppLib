package com.weidian.plugin.core.ctx;

import android.app.Application;

import com.weidian.plugin.core.controller.ControllerProxy;
import com.weidian.plugin.core.install.Config;

public final class Host extends Plugin {

    public Host(Application app, Config config) {
        super(app, config);
        HostParentClassLoader.init(this);
        this.controllerProxy = new ControllerProxy(this);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return this.getClassLoader().loadClass(name);
    }
}
