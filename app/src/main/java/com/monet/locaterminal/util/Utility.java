package com.monet.locaterminal.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
/**
 * Created by Monet on 2015/4/25.
 */
public class Utility {
    /**
     * 传感器管理
     */
    private SensorManager sensorManager;
    /**
     * Sensor的列表
     */
    private List<Sensor> allSensors;
    /**
     * 一个Sensor
     */
    private Sensor oneSensor;

    public static void getSensorTypes(Context context,SensorManager sensorManager) {
        List<Sensor> allSensors;
        Sensor oneSensor;

        //获取本机的传感器列表
        allSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        StringBuffer strBuf = new StringBuffer();
        strBuf.append("该手机有" + allSensors.size() + "个传感器,分别是:\n");
        float[] rR=null;
        float[] vValue=null;
        for (int i = 0; i < allSensors.size(); i++) {
            oneSensor = allSensors.get(i);
            switch (oneSensor.getType()) {
                //参考材料：官方的开发者网站 ————
                // http://developer.android.com/reference/android/hardware/Sensor.html
                //第一种
                case Sensor.TYPE_ACCELEROMETER:
                    strBuf.append(i + ".加速度传感器\n");
                    break;
                //第二种
                case Sensor.TYPE_AMBIENT_TEMPERATURE:
                    strBuf.append(i + ".环境温度传感器");
                    break;
                //第三种
                case Sensor.TYPE_GAME_ROTATION_VECTOR:
                    strBuf.append(i + ".无标定旋转矢量传感器");
                    break;
                //第四种
                case Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR:
                    strBuf.append(i + ".地球磁场旋转矢量");
                    break;
                //第五种
                case Sensor.TYPE_GRAVITY:
                    strBuf.append(i + ".重力传感器");
                    break;
                //第六种
                case Sensor.TYPE_GYROSCOPE:
                    strBuf.append(i + ".陀螺仪传感器\n");
                    break;
                //第七种
                case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
                    strBuf.append(i + ".未标定的陀螺仪传感器");
                    break;
                //第八种
                case Sensor.TYPE_HEART_RATE:
                    strBuf.append(i + ".心率传感器");    //顿时高大上了，有木有
                    break;
                //第九种
                case Sensor.TYPE_LIGHT:
                    strBuf.append(i + ".环境光线传感器\n");
                    break;
                //第十种
                case Sensor.TYPE_LINEAR_ACCELERATION:
                    strBuf.append(i + ".线性加速度传感器");
                    break;
                //第十一种
                case Sensor.TYPE_MAGNETIC_FIELD:
                    strBuf.append(i + ".电磁场传感器\n");
                    break;
                //第十二种
                case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                    strBuf.append(i + ".未标定的电磁场传感器");
                    break;
                //第十三种
                //API20中被取消了，用SensorManager.getOrientation()代替,
                // 但它返回的是float啊，而之前的是int，实在不解。
                case Sensor.TYPE_ORIENTATION:
                    strBuf.append(i + ".方向传感器");
                    break;
                //第十四种
                case Sensor.TYPE_PRESSURE:
                    strBuf.append(i + ".压力传感器\n");
                    break;
                //第十五种
                case Sensor.TYPE_PROXIMITY:
                    strBuf.append(i + ".距离传感器\n");
                    break;
                //第十六种
                case Sensor.TYPE_RELATIVE_HUMIDITY:
                    strBuf.append(i + ".相对湿度传感器");
                    break;
                //第十七种
                case Sensor.TYPE_ROTATION_VECTOR:
                    strBuf.append(i + ".旋转矢量传感器");
                    break;
                //第十八种
                case Sensor.TYPE_SIGNIFICANT_MOTION:
                    strBuf.append(i + ".显著运动传感器");
                    break;
                //第十九种
                case Sensor.TYPE_STEP_COUNTER:
                    strBuf.append(i + "计步器传感器");
                    break;
                //第二十种
                case Sensor.TYPE_STEP_DETECTOR:
                    strBuf.append(i + "步伐探测传感器");   //翻译无力了。。。
                    break;
                //TYPE_TEMPERATRUE在API14中被取消，
                // 使用Sensor.TYPE_AMBIENT_TEMPERATURE代替，已在前文列举
                case Sensor.TYPE_TEMPERATURE:
                    strBuf.append(i + ".温度传感器\n");
                    break;
                //第 N 种「因其未知」
                default:
                    strBuf.append(i + ".未知传感器\n");
                    break;

            }
            strBuf.append("  设备名称:" + oneSensor.getName() + "\n");
            strBuf.append("  设备版本:" + oneSensor.getVersion() + "\n");
            strBuf.append("  通用类型号:" + oneSensor.getType() + "\n");
            strBuf.append("  设备商名称:" + oneSensor.getVendor() + "\n");
            strBuf.append("  传感器功耗:" + oneSensor.getPower() + "\n");
            strBuf.append("  传感器分辨率:" + oneSensor.getResolution() + "\n");
            strBuf.append("  传感器最大量程:" + oneSensor.getMaximumRange() + "\n");
        }
        saveInfo(context, strBuf.toString());
        //Log.e("Sensor List:", strBuf.toString());
    }

    public static void saveInfo(Context context, String sensorTypes) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString("sensor_types", sensorTypes);
        editor.putString("current_time", sdf.format(new Date()));
        editor.commit();
    }

    /**
     * 使用JSONObject解析本地的json数据
     * [{"id":"201", "name":"北客站"},...]
     */
    public static List<String> parseJSONWithJSONObject(Context context, String jsonData) {
        List<String> stationList = null;
        try {
            stationList = new ArrayList<String>();
            JSONArray jsonArray = new JSONArray(jsonData);
            //String curStationCode = "";
            String curStationName = "";
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                //curStationCode = jsonObject.getString("code");
                curStationName = jsonObject.getString("name");
                Log.e("test", " - " + curStationName);
                stationList.add(curStationName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putInt("station_list_size", stationList.size());
        editor.commit();
        return stationList;
    }

    /**
     * 从asset路径下读取对应文件转String输出
     */
    public static String getFileData(Context mContext, String fileName) {
        // TODO Auto-generated method stub
        StringBuilder sb = new StringBuilder();
        AssetManager am = mContext.getAssets();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    am.open(fileName)));
            String next = "";
            while (null != (next = br.readLine())) {
                sb.append(next);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            sb.delete(0, sb.length());
        }
        return sb.toString().trim();
    }
}
