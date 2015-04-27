package com.monet.locaterminal.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.monet.locaterminal.R;
import com.monet.locaterminal.service.DetectiveMotionService;
import com.monet.locaterminal.util.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Monet on 2015/4/17.
 */



public class MainActivity extends Activity{


    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<String>();
    private static int justOneTime = 0;
    /**
     * 站点列表
     */
    private List<String> stationList;
    /**
     * 显示当前到达的站点
     */
    private TextView currentStation;
    /**
     * 显示当前设备拥有的传感器
     */
    private TextView sensorTypes;
    /**
     * 显示站点列表，备用
     */
    private ListView listView;
    /**
     * 一个广播接收器，用来改变UI
     */
    private UiUpdateReceiver uiUpdateReceiver;
    /**
     * 显示传感器的数据
     */
    private TextView sensorData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        currentStation = (TextView) findViewById(R.id.layout_top);
        sensorTypes = (TextView) findViewById(R.id.layout_center);
        sensorTypes.setMovementMethod(ScrollingMovementMethod.getInstance());
        sensorData = (TextView) findViewById(R.id.sensor_data);
        listView = (ListView) findViewById(R.id.station_list);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        //先从本地assets加载站点列表
        if (justOneTime == 0) {
            String jsonData = Utility.getFileData(this, "Stations.txt");
            stationList = Utility.parseJSONWithJSONObject(this,jsonData);
            justOneTime++;
        }
        queryStation();   //加载站点数据
        //注册广播接收器，因依赖activity，故不能在Manifest里注册
        uiUpdateReceiver = new UiUpdateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("UI_UPDATE_ACTION");
        registerReceiver(uiUpdateReceiver, filter);

        //激活DetectiveMotionSevice服务
        Intent intent = new Intent(this, DetectiveMotionService.class);
        startService(intent);
    }

    /**
     * 查询数据库中的站点数据
     */
    private void queryStation() {
        if (stationList.size() > 0) {
            dataList.clear();
            for (String station : stationList) {
                dataList.add(station);
            }
            adapter.notifyDataSetChanged();
            //listView.setSelection(0);
            //currentStation.setText("moumou**");
        }
    }

    private void showInfo(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        currentStation.setText("站点"+prefs.getString("current_station","1"));
        //因justonetime前面oncreate使用过一次，此处是一样的用法
        if (justOneTime == 1) {
            sensorTypes.setText(prefs.getString("sensor_types", "Loading..."));
            justOneTime++;
        }
        sensorData.setText(prefs.getString("sensor_data", "Loading..."));
        //index从0开始
        int index = Integer.parseInt(prefs.getString("current_station", "1")) - 1;
        listView.setSelection(index);
        //adapter.setSelectedItem(index);
        //adapter.notifyDataSetChanged();
//        listView.setItemChecked(index,true);
    }

    public class UiUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
           showInfo();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(uiUpdateReceiver); Intent intent = new Intent(this, DetectiveMotionService.class); stopService(intent); } }


