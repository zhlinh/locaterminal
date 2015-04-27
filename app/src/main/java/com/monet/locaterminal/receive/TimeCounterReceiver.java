package com.monet.locaterminal.receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.monet.locaterminal.service.DetectiveMotionService;

/**
 * Created by Monet on 2015/4/27.
 */
public class TimeCounterReceiver extends BroadcastReceiver{
    /**
     * 在onReceive方法中再次启动AutoUpdateService就可以实现后台定时更新功能了
     */

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, DetectiveMotionService.class);
        context.startService(i);
    }
}
