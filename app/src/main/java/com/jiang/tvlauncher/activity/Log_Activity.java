package com.jiang.tvlauncher.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.jiang.tvlauncher.R;
import com.jiang.tvlauncher.utils.SystemPropertiesProxy;
import com.jiang.tvlauncher.utils.Tools;
import com.jmgo.android.projector.JmgoCommonManager;
import com.jmgo.android.projector.JmgoConfig;

/**
 * @author: jiangadmin
 * @date: 2018/9/6
 * @Email: www.fangmu@qq.com
 * @Phone: 186 6120 1018
 * TODO:
 */
public class Log_Activity extends Base_Activity {
    private static final String TAG = "Log_Activity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        TextView textView = findViewById(R.id.log);

        String s = JmgoCommonManager.getInstance("feekrs").getString(JmgoConfig.DLP, 0x50F19);

        s += "\nMAC" + Tools.getMacAddress();
        s += "\n机器型号:" + SystemPropertiesProxy.getString(this, "ro.product.name");
        s += "\n固件版本:" + SystemPropertiesProxy.getString(this, "ro.build.version.incremental");
        s += "\n固件版本:" + Build.VERSION.INCREMENTAL;
        s += "\n系统版本:" + SystemPropertiesProxy.getString(this, "ro.build.version.release");
        s += "\n系统版本:" + Build.VERSION.RELEASE;
        textView.setText(s);

    }
}
