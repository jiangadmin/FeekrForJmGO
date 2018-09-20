package com.jiang.tvlauncher;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;

import com.jiang.tvlauncher.entity.Point;
import com.jiang.tvlauncher.entity.Save_Key;
import com.jiang.tvlauncher.servlet.TurnOn_servlet;
import com.jiang.tvlauncher.utils.LogUtil;
import com.jiang.tvlauncher.utils.SaveUtils;
import com.jiang.tvlauncher.utils.Tools;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.List;

/**
 * Created by  jiang
 * on 2017/7/3.
 * Email: www.fangmu@qq.com
 * Phone：186 6120 1018
 * Purpose:TODO
 * update：
 */

public class MyAppliaction extends Application {
    private static final String TAG = "MyAppliaction";
    public static boolean LogShow = true;
    public static Context context;

    public static boolean IsLineNet = true;//是否是有线网络
    public static String modelNum = "";
    public static String ID = "";
    public static String Temp = "FFFFFF";
    public static String WindSpeed = "FFFFFF";
    public static String turnType = "2";//开机类型 1 通电开机 2 手动开机
    Point point;
    public static boolean TurnOnS = false;

    public static Activity activity;


    @Override
    public void onCreate() {
        super.onCreate();
//        startService(new Intent(this, TimingService.class));
        context = this;

        //崩溃检测
        CrashReport.initCrashReport(getApplicationContext(), "5edcfe4eff", false);

        LogUtil.e(TAG, "有线连接：" + Tools.isLineConnected());
        Tools.setScreenOffTime(24 * 60 * 60 * 1000);
        LogUtil.e(TAG, "休眠时间：" + Tools.getScreenOffTime());

        SaveUtils.setBoolean(Save_Key.FristTurnOn, true);

        new TurnOn_servlet(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }


    /**
     * 当前应用是否处于前台
     *
     * @return
     */
    public static boolean isForeground() {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processes = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : processes) {
            if (processInfo.processName.equals(context.getPackageName())) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    return true;
                }
            }
        }
        return false;
    }
}
