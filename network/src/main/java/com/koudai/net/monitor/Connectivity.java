package com.koudai.net.monitor;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

/**
 * Created by zhaoyu on 15/10/23.
 * 判断网络类型
 */
public final class Connectivity {

    private Context context;

    public Connectivity(Context context) {
        this.context = context;
    }

    /**
     * Get the network info
     *
     * @return
     */
    public NetworkInfo getNetworkInfo() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    /**
     * Check if there is any connectivity
     *
     * @return
     */
    public boolean isConnected() {
        NetworkInfo info = getNetworkInfo();
        return (info != null && info.isConnected());
    }

    /**
     * Check if there is any connectivity to a Wifi network
     *
     * @return
     */
    public boolean isConnectedWifi() {
        NetworkInfo info = getNetworkInfo();
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
    }

    /**
     * Check if there is any connectivity to a mobile network
     *
     * @return
     */
    public boolean isConnectedMobile() {
        NetworkInfo info = getNetworkInfo();
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }


    public boolean isConnect2G() {
        NetworkInfo info = getNetworkInfo();
        if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
            int subType = info.getSubtype();
            if (subType == TelephonyManager.NETWORK_TYPE_EDGE
                    || subType == TelephonyManager.NETWORK_TYPE_CDMA
                    || subType == TelephonyManager.NETWORK_TYPE_GPRS) {
                return true;
            }
        }
        return false;
    }

    public boolean isConnect3G() {
        NetworkInfo info = getNetworkInfo();
        if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
            int subType = info.getSubtype();
            if (subType == TelephonyManager.NETWORK_TYPE_UMTS
                    || subType == TelephonyManager.NETWORK_TYPE_EVDO_0
                    || subType == TelephonyManager.NETWORK_TYPE_EVDO_A
                    || subType == TelephonyManager.NETWORK_TYPE_HSDPA
                    || subType == TelephonyManager.NETWORK_TYPE_HSUPA
                    || subType == TelephonyManager.NETWORK_TYPE_HSPA
                    || subType == TelephonyManager.NETWORK_TYPE_EVDO_B
                    || subType == TelephonyManager.NETWORK_TYPE_EHRPD
                    || subType == TelephonyManager.NETWORK_TYPE_HSPAP) {
                return true;
            }
        }
        return false;
    }

    public boolean isConnect4G() {
        NetworkInfo info = getNetworkInfo();
        if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
            int subType = info.getSubtype();
            if (subType == TelephonyManager.NETWORK_TYPE_LTE) {
                return true;
            }
        }
        return false;
    }

}
