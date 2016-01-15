package com.koudai.net.netutils;

import android.text.TextUtils;

import com.koudai.net.NetworkLibraryConstants;
import com.koudai.net.kernal.Headers;

/**
 * Created by zhaoyu on 15/11/14.
 */
public class HeaderUtil {

    public static boolean isWdGzip(Headers headers) {
        String wdGzip = headers.get(NetworkLibraryConstants.GZIP_TYPE);
        return (!TextUtils.isEmpty(wdGzip) && NetworkLibraryConstants.ENCRYPT_TYPE_YES.equalsIgnoreCase(wdGzip));
    }
}
