package com.monet.locaterminal.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.monet.locaterminal.receive.TimeCounterReceiver;
import com.monet.locaterminal.util.Utility;

/**
 * Created by Monet on 2015/4/24.
 */
public class DetectiveMotionService extends Service {
    /**
     * 传感器管理
     */
    private SensorManager sensorManager;
    /**
     * 用来计数
     */
    private static int countStation = 1;
    // realStation=(countStation+1)/2 ,考虑到列车的进站出站
    private static int realStation = 1;
    /**
     * Station的个数
     */
    private static int stationNum;
    /**
     * 列车的前进方向
     */
    private static int direction = 1;
    /**
     * 用来记录传感器数值的持续时间
     */
    private static long duration;
    //5*1000/2 （5000ms（持续时间）除以采样率2ms）   经验值是10
    private static final long SAMPLE_TIMES = 10;
    private static long pretime;
    private static long curtime;
    private static long temptime;
    private static int nowOrNext = 0;    //取值0或1

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        stationNum = prefs.getInt("station_list_size", 0);
        sensorManager = (SensorManager) getSystemService (Context.SENSOR_SERVICE);
        //存储本设备拥有的所有传感器信息
        Utility.getSensorTypes(this, sensorManager);
        Intent uiIntent = new Intent();
        uiIntent.setAction("UI_UPDATE_ACTION");
        sendBroadcast(uiIntent);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (sensor != null) {
            sensorManager.registerListener(listener, sensor, SensorManager. SENSOR_DELAY_NORMAL);
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        duration = 0;
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 5 * 1000; //5秒的毫秒数
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, TimeCounterReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    //监听传感器数值是否发生改变
    private SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            synchronized (this) {
                /**
                 * 记录采样频率 单位ms
                 */
//                if (nowOrNext == 0) {
//                    curtime = System.currentTimeMillis();
//                }
//                if (nowOrNext == 1) {
//                    pretime = System.currentTimeMillis();
//                }
//                nowOrNext = (nowOrNext + 1) % 2;
//                temptime = Math.abs(curtime - pretime);
//                Log.e("test", String.valueOf(temptime));
                // 加速度可能会是负值，所以要取它们的绝对值
                float xValue = Math.abs(event.values[0]);
                float yValue = Math.abs(event.values[1]);
                float zValue = Math.abs(event.values[2]);
                //显示带正负号的原数值
                String axisData = "x=" + event.values[0] + " ,y=" + event.values[1] + " ,z=" + event.values[2];
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(DetectiveMotionService.this).edit();
                editor.putString("sensor_data", axisData);
                if (xValue > 12 || yValue > 12 || zValue > 12) {
                    //认为手机突然减速或突然加速
                    duration++;
                    Log.e("test", "duration" + duration);
                    if (duration >= SAMPLE_TIMES) {
                        if (realStation == 1) {
                            direction = 1;
                        }
                        if (realStation == stationNum) {
                            direction = -1;
                        }
                        countStation = countStation + direction;
                        realStation=(countStation + 1) / 2;
                        editor.putString("current_station", String.valueOf(realStation));
                        //Log.d("test", String.valueOf(countStation));
                        duration = 0;
                    }
                }
                editor.commit();
                Intent uiIntent = new Intent();
                uiIntent.setAction("UI_UPDATE_ACTION");
                sendBroadcast(uiIntent);
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(listener);
        }
    }
}
