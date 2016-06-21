package com.koudai.net.toolbox.rpc.annotation;

/**
 * Created by krystaljake on 16/6/20.
 */
public @interface BodyParameter {
    String key() default "";
    boolean isNeedEncrypt() default false;
}
