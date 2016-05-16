package com.weidian.dycontainer;

import android.app.Application;
import android.content.Context;

/**
 * Created by krystaljake on 5/16/16.
 * 容器的Application，在这里完成Classloader双亲委派链的hack
 */
public class DyContainerApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}
