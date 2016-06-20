package com.weidian.plugin.util;

/**
 * Created by krystaljake on 16/5/30.
 */
public final class ByteArrayPoolUtil {

    private static final int POOL_SIZE = 1024 * 1024 * 500;

    private static ByteArrayPool pool;

    static {
        pool = new ByteArrayPool(POOL_SIZE);
    }

    private ByteArrayPoolUtil() {
    }

    public static ByteArrayPool getPool() {
        return pool;
    }

    public static byte[] getBuf(int len) {
        return pool.getBuf(len);
    }

    public static void returnBuf(byte[] buf) {
        pool.returnBuf(buf);
    }
}
