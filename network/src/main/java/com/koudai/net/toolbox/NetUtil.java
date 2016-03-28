package com.koudai.net.toolbox;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by zhaoyu on 16/3/28.
 */
public class NetUtil {
    public static boolean testNetworkConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) NetworkFetcherGlobalParams.getInstance().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
