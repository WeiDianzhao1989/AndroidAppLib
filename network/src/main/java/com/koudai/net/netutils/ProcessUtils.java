package com.koudai.net.netutils;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Process;
import android.text.TextUtils;

import java.util.List;

/**
 * Created by zhaoyu on 15/12/30.
 */
public class ProcessUtils {
    /**
     * 是否主进程
     *
     * @param context
     * @return
     */
    public static boolean isMainProcess(Context context) {

        ActivityManager am = ((ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = context.getPackageName();
        int myPid = Process.myPid();
        if (!CollectionUtils.isListEmpty(processInfos)) {
            for (ActivityManager.RunningAppProcessInfo info : processInfos) {
                if (info.pid == myPid && TextUtils.equals(mainProcessName, info.processName)) {
                    return true;
                }
            }
        }

        return false;
    }

}
