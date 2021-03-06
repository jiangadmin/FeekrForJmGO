package com.jiang.tvlauncher.servlet;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.RemoteException;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.jiang.tvlauncher.MyAppliaction;
import com.jiang.tvlauncher.activity.Home_Activity;
import com.jiang.tvlauncher.dialog.Loading;
import com.jiang.tvlauncher.entity.Const;
import com.jiang.tvlauncher.entity.Point;
import com.jiang.tvlauncher.entity.Save_Key;
import com.jiang.tvlauncher.entity.TurnOnEntity;
import com.jiang.tvlauncher.server.TimingService;
import com.jiang.tvlauncher.utils.HttpUtil;
import com.jiang.tvlauncher.utils.LogUtil;
import com.jiang.tvlauncher.utils.SaveUtils;
import com.jiang.tvlauncher.utils.SystemPropertiesProxy;
import com.jiang.tvlauncher.utils.Tools;
import com.jmgo.android.projector.JmgoCommonManager;
import com.jmgo.android.projector.JmgoConfig;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: jiangadmin
 * @date: 2017/6/19.
 * @Email: www.fangmu@qq.com
 * @Phone: 186 6120 1018
 * TODO: 开机发送
 */

public class TurnOn_servlet extends AsyncTask<String, Integer, TurnOnEntity> {
    private static final String TAG = "TurnOn_servlet";
    Context context;

    TimeCount timeCount;

    public TurnOn_servlet(Context context) {
        this.context = context;
        timeCount = new TimeCount(3000, 1000);
    }

    @Override
    protected TurnOnEntity doInBackground(String... strings) {
        Map map = new HashMap();
        TurnOnEntity entity;

        map.put("serialNum", JmgoCommonManager.getInstance("feekrs").getString(JmgoConfig.DLP, 0x50F19));         //设备号
        map.put("turnType", MyAppliaction.turnType);    //开机方式
        map.put("modelNum",  SystemPropertiesProxy.getString(context, "ro.product.name"));    //设备型号

        map.put("systemVersion", Build.VERSION.INCREMENTAL);//固件版本
        map.put("androidVersion", Build.VERSION.RELEASE);   //Android版本

        String res = HttpUtil.doPost(Const.URL + "dev/devTurnOffController/turnOn.do", map);

        if (TextUtils.isEmpty(res)) {
            entity = new TurnOnEntity();
            entity.setErrorcode(-1);
            entity.setErrormsg("连接服务器失败");
        } else {
            try {
                entity = new Gson().fromJson(res, TurnOnEntity.class);
            } catch (Exception e) {
                entity = new TurnOnEntity();
                entity.setErrorcode(-2);
                entity.setErrormsg("数据解析失败");
                LogUtil.e(TAG, e.getMessage());
            }
        }

        LogUtil.e(TAG, "=======================================================================================");
        if (entity != null && entity.getErrormsg() != null)
            LogUtil.e(TAG, entity.getErrormsg());
//        Toast.makeText(context, "开机请求返回："+entity.getErrormsg(), Toast.LENGTH_SHORT).show();
        LogUtil.e(TAG, "=======================================================================================");

        if (entity.getErrorcode() == 1000) {
            MyAppliaction.TurnOnS = true;

            //归零
            num = 0;

            Const.ID = entity.getResult().getDevInfo().getId();

            //存储ID
            SaveUtils.setString(Save_Key.ID, String.valueOf(entity.getResult().getDevInfo().getId()));

            //存储密码
            if (entity.getResult().getShadowcnf() != null && entity.getResult().getShadowcnf().getShadowPwd() != null) {
                SaveUtils.setString(Save_Key.Password, entity.getResult().getShadowcnf().getShadowPwd());
            } else {
                SaveUtils.setString(Save_Key.Password, "");
            }
            //更改开机动画
            if (entity.getResult().getLaunch() != null)
                if (!TextUtils.isEmpty(entity.getResult().getLaunch().getMediaUrl())) {
                    LogUtil.e(TAG, entity.getResult().getLaunch().getMediaUrl());
                    SaveUtils.setString(Save_Key.BootAn, entity.getResult().getLaunch().getMediaUrl());
                }

            //方案类型（1=开机，2=屏保，3=互动）
            if (entity.getResult().getLaunch() != null)
                if (entity.getResult().getLaunch().getLaunchType() == 1) {
                    //非空判断
                    if (!TextUtils.isEmpty(entity.getResult().getLaunch().getMediaUrl())) {
                        //图片
                        if (entity.getResult().getLaunch().getMediaType() == 1) {
                            SaveUtils.setBoolean(Save_Key.NewImage, true);
                            SaveUtils.setBoolean(Save_Key.NewVideo, false);
                            SaveUtils.setString(Save_Key.NewImageUrl, entity.getResult().getLaunch().getMediaUrl());
                        }
                        //视频
                        if (entity.getResult().getLaunch().getMediaType() == 2) {
                            SaveUtils.setBoolean(Save_Key.NewVideo, true);
                            SaveUtils.setBoolean(Save_Key.NewImage, false);
                            SaveUtils.setString(Save_Key.NewVideoUrl, entity.getResult().getLaunch().getMediaUrl());
                        }
                    }
                }

            //存储间隔时间
            if (entity.getResult().getShadowcnf() != null)
                SaveUtils.setInt(Save_Key.Timming, entity.getResult().getShadowcnf().getMonitRate());

            //启动定时服务
            context.startService(new Intent(context, TimingService.class));

            //判断是否是有线连接 & 服务启用同步数据
            if (Tools.isLineConnected() && entity.getResult().getShadowcnf() != null
                    && entity.getResult().getShadowcnf().getHotPointFlag() == 1) {
                if (entity.getResult().getShadowcnf().getHotPoint() == 1
                        && entity.getResult().getShadowcnf().getWifi() != null
                        && entity.getResult().getShadowcnf().getWifiPassword() != null) {                //开启热点

                    //获取热点名称&热点密码
                    String SSID = entity.getResult().getShadowcnf().getWifi();
                    String APPWD = entity.getResult().getShadowcnf().getWifiPassword();

                    //存储热点名称&密码
                    SaveUtils.setString(Save_Key.WiFiName, SSID);
                    SaveUtils.setString(Save_Key.WiFiPwd, APPWD);

                    LogUtil.e(TAG, "SSID:" + SSID + "  PassWord:" + APPWD);

                    //打开并设置热点信息.注意热点密码8-32位，只限制了英文密码位数。
                    //使用极米开启/关闭热点接口

                }
            }


        } else if (entity.getErrorcode() == -2) {
            LogUtil.e(TAG, entity.getErrormsg());

        } else {
            timeCount.start();
            LogUtil.e(TAG, "失败了" + entity.getErrormsg());
        }

        return entity;
    }

    @Override
    protected void onPostExecute(TurnOnEntity entity) {
        super.onPostExecute(entity);
        Const.Nets = false;
        Loading.dismiss();

        switch (entity.getErrorcode()) {
            case 1000:

                EventBus.getDefault().post("update");

                break;
        }

    }

    public static int num = 0;

    /**
     * 计时器
     */
    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);//参数依次为总时长,和计时的时间间隔
        }

        //倒计时完成
        @Override
        public void onFinish() {
            num++;
            //再次启动
            new TurnOn_servlet(context).execute();

        }

        @Override
        public void onTick(long millisUntilFinished) {//计时过程显示

        }
    }
}
