package com.example.ch225253.baidumap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;

import java.util.ArrayList;

/* 此Demo用来演示离线地图的下载和显示 */
public class OfflineMap extends Activity implements MKOfflineMapListener {

    private static final String TAG = "OfflineMap";

    private MKOfflineMap mOffline = null;
    private TextView cidView;
    private TextView stateView;
    private EditText cityNameView;

    /**
     * 已下载的离线地图信息列表
     */
    private ArrayList<MKOLUpdateElement> localMapList = null;
    private LocalMapAdapter lAdapter = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline);
        mOffline = new MKOfflineMap();
        mOffline.init(this);
        initView();
    }

    private void initView() {

        cidView = (TextView) findViewById(R.id.cityid);
        cityNameView = (EditText) findViewById(R.id.city);
        stateView = (TextView) findViewById(R.id.state);

        final ListView hotCityList = (ListView) findViewById(R.id.hotcitylist);
        final ArrayList<String> hotCities = new ArrayList<String>();
        final ArrayList<String> hotCityNames = new ArrayList<String>();
        final ArrayList<Integer> hotCityID = new ArrayList<Integer>();
        // 获取热闹城市列表
        final ArrayList<MKOLSearchRecord> records1 = mOffline.getHotCityList();
        if (records1 != null) {
            for (MKOLSearchRecord r : records1) {
                //V4.5.0起，保证数据不溢出，使用long型保存数据包大小结果
                hotCities.add(r.cityName + "   --"
                        + this.formatDataSize(r.dataSize));
                hotCityNames.add(r.cityName);
                hotCityID.add(r.cityID);
            }
            //添加热门城市“芜湖”
            hotCities.add("芜湖市");//用于列表显示
            hotCityNames.add("芜湖市");//搜索框
            hotCityID.add(129);//id
        }
        final ListAdapter hAdapter = (ListAdapter) new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, hotCities);
        hotCityList.setAdapter(hAdapter);
        hotCityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                cityNameView.setText(hotCityNames.get(i));
                cidView.setText(String.valueOf(hotCityID.get(i)));
                search(hotCityList.getSelectedView());
            }
        });

        final ListView allCityList = (ListView) findViewById(R.id.allcitylist);
        // 获取所有支持离线地图的城市
        final ArrayList<String> allCities = new ArrayList<String>();
        final ArrayList<String> allCityNames = new ArrayList<String>();
        final ArrayList<Integer> allCityID = new ArrayList<Integer>();
        ArrayList<MKOLSearchRecord> records2 = mOffline.getOfflineCityList();
        if (records2 != null) {
            for (MKOLSearchRecord r : records2) {
                //V4.5.0起，保证数据不溢出，使用long型保存数据包大小结果
                allCities.add(r.cityName + "   --"
                        + this.formatDataSize(r.dataSize));
                allCityNames.add(r.cityName);
                allCityID.add(r.cityID);
            }
        }
        ListAdapter aAdapter = (ListAdapter) new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, allCities);
        allCityList.setAdapter(aAdapter);
        allCityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                cityNameView.setText(allCityNames.get(i));
                cidView.setText(String.valueOf(allCityID.get(i)));
                search(allCityList.getSelectedView());
            }
        });
        LinearLayout cl = (LinearLayout) findViewById(R.id.citylist_layout);
        LinearLayout lm = (LinearLayout) findViewById(R.id.localmap_layout);
        lm.setVisibility(View.GONE);
        cl.setVisibility(View.VISIBLE);

        // 获取已下过的离线地图信息
        localMapList = mOffline.getAllUpdateInfo();
        if (localMapList == null) {
            localMapList = new ArrayList<MKOLUpdateElement>();
        }

        ListView localMapListView = (ListView) findViewById(R.id.localmaplist);
        lAdapter = new LocalMapAdapter();
        localMapListView.setAdapter(lAdapter);

    }

    /**
     * 返回上一级
     *
     * @param view
     */
    public void clickback(View view){
        finish();
    }

    /**
     * 切换至城市列表
     *
     * @param view
     */
    public void clickCityListButton(View view) {
        LinearLayout cl = (LinearLayout) findViewById(R.id.citylist_layout);
        LinearLayout lm = (LinearLayout) findViewById(R.id.localmap_layout);
        LinearLayout search_layout = (LinearLayout) findViewById(R.id.search_layout);
        LinearLayout state_layout = (LinearLayout) findViewById(R.id.state_layout);
        LinearLayout searchlist = (LinearLayout) findViewById(R.id.searchlist);
        LinearLayout citylist = (LinearLayout) findViewById(R.id.citylist);

        lm.setVisibility(View.GONE);
        cl.setVisibility(View.VISIBLE);
        search_layout.setVisibility(View.VISIBLE);
        state_layout.setVisibility(View.GONE);
        searchlist.setVisibility(View.GONE);
        citylist.setVisibility(View.VISIBLE);

        Button clButton = (Button)findViewById(R.id.clButton);
        clButton.setBackgroundResource(R.drawable.city_list_pressed);
        clButton.setTextColor(Color.parseColor("#7699fe"));
        Button localButton = (Button) findViewById(R.id.localButton);
        localButton.setBackgroundResource(R.drawable.down_manager);
        localButton.setTextColor(Color.parseColor("#ffffff"));
    }

    /**
     * 切换至下载管理列表
     *
     * @param view
     */
    public void clickLocalMapListButton(View view) {
        LinearLayout cl = (LinearLayout) findViewById(R.id.citylist_layout);
        LinearLayout lm = (LinearLayout) findViewById(R.id.localmap_layout);
        LinearLayout search_layout = (LinearLayout) findViewById(R.id.search_layout);
//        LinearLayout state_layout = (LinearLayout) findViewById(R.id.state_layout);
        lm.setVisibility(View.VISIBLE);
        cl.setVisibility(View.GONE);
        search_layout.setVisibility(View.GONE);
//        state_layout.setVisibility(View.VISIBLE);
        Button localButton = (Button) findViewById(R.id.localButton);
        localButton.setBackgroundResource(R.drawable.down_manager_pressed);
        localButton.setTextColor(Color.parseColor("#7699fe"));
        Button clButton = (Button)findViewById(R.id.clButton);
        clButton.setBackgroundResource(R.drawable.city_list);
        clButton.setTextColor(Color.parseColor("#ffffff"));
        //隐藏输入法
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (this.getCurrentFocus()!=null) {
            inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
    }

    /**
     * 搜索离线城市
     *
     * @param view
     */
    public void search(View view) {
        ArrayList<MKOLSearchRecord> records = mOffline.searchCity(cityNameView
                .getText().toString());
        final LinearLayout citylist = (LinearLayout) findViewById(R.id.citylist);
        TextView textView = (TextView) findViewById(R.id.search_result);
        cityNameView = (EditText) findViewById(R.id.city);
        cityNameView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (i==0){
                    citylist.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        if (records == null || records.size() != 1) {

            Toast.makeText(this, "不支持该城市离线地图", Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            cidView.setText(String.valueOf(records.get(0).cityID));
            LinearLayout searchlist = (LinearLayout) findViewById(R.id.searchlist);
            citylist.setVisibility(View.GONE);
            searchlist.setVisibility(View.VISIBLE);
            textView.setText(records.get(0).cityName+"("+records.get(0).cityID+")");
        }
    }

    /**
     * 开始下载
     *
     * @param view
     */
    public void start(View view) {
        int cityid = Integer.parseInt(cidView.getText().toString());
        mOffline.start(cityid);
        clickLocalMapListButton(null);

        Toast.makeText(this, "开始下载离线地图. cityid: " + cityid, Toast.LENGTH_SHORT).show();
        updateView();
    }

    /**
     * 暂停下载
     *
     * @param view
     */
    public void stop(View view) {
        int cityid = Integer.parseInt(cidView.getText().toString());
        mOffline.pause(cityid);
        Toast.makeText(this, "暂停下载离线地图. cityid: " + cityid, Toast.LENGTH_SHORT)
                .show();
        updateView();
    }

//    public void start_stop1(View view) {
//        ToggleButton toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
//        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
//        {
//            @Override
//            public void onCheckedChanged (CompoundButton compoundButton,boolean isChecked)
//            {
//                if (isChecked) {
//                    int cityid = Integer.parseInt(cidView.getText().toString());
//                    mOffline.start(cityid);
//                    Toast.makeText(getApplicationContext(), "开始下载离线地图. cityid: " + cityid, Toast.LENGTH_SHORT).show();
//                    updateView();
//                } else {
//                    int cityid = Integer.parseInt(cidView.getText().toString());
//                    mOffline.pause(cityid);
//                    Toast.makeText(OfflineMap.this, "暂停下载离线地图. cityid: " + cityid, Toast.LENGTH_SHORT).show();
//                    updateView();
//                }
//            }
//        });
//    }
    /**
     * 删除离线地图
     *
     * @param view
     */
    public void remove(View view) {
        int cityid = Integer.parseInt(cidView.getText().toString());
        mOffline.remove(cityid);
        Toast.makeText(this, "删除离线地图. cityid: " + cityid, Toast.LENGTH_SHORT).show();
        updateView();
    }

    /**
     * 更新状态显示
     */
    public void updateView() {
        localMapList = mOffline.getAllUpdateInfo();
        if (localMapList == null) {
            localMapList = new ArrayList<MKOLUpdateElement>();
        }
        lAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        int cityid = Integer.parseInt(cidView.getText().toString());
        MKOLUpdateElement temp = mOffline.getUpdateInfo(cityid);
        if (temp != null && temp.status == MKOLUpdateElement.DOWNLOADING) {
            mOffline.pause(cityid);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * V4.5.0起，保证数据不溢出，使用long型保存数据包大小结果
    */
    public String formatDataSize(long size) {
        String ret = "";
        if (size < (1024 * 1024)) {
            ret = String.format("%dK", size / 1024);
        } else {
            ret = String.format("%.1fM", size / (1024 * 1024.0));
        }
        return ret;
    }

    @Override
    protected void onDestroy() {
        /**
         * 退出时，销毁离线地图模块
         */
        mOffline.destroy();
        super.onDestroy();
    }


    @Override
    public void onGetOfflineMapState(int type, int state) {
        switch (type) {
            case MKOfflineMap.TYPE_DOWNLOAD_UPDATE: {
                MKOLUpdateElement update = mOffline.getUpdateInfo(state);
                // 处理下载进度更新提示
                if (update != null) {
                    stateView.setText(String.format("%s : %d%%", update.cityName, update.ratio));
                    updateView();
                }
            }
            break;

            case MKOfflineMap.TYPE_NEW_OFFLINE:
                // 有新离线地图安装
                Log.d("OfflineMap", String.format("add item_offlinemap num:%d", state));
                break;

            case MKOfflineMap.TYPE_VER_UPDATE:
                // 版本更新提示
                // MKOLUpdateElement e = mOffline.getUpdateInfo(state);
                break;

            default:
                break;
        }

    }

    /**
     * 离线地图管理列表适配器
     */
    public class LocalMapAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return localMapList.size();
        }

        @Override
        public Object getItem(int index) {
            return localMapList.get(index);
        }

        @Override
        public long getItemId(int index) {
            return index;
        }

        @Override
        public View getView(int index, View view, ViewGroup arg2) {
            MKOLUpdateElement e = (MKOLUpdateElement) getItem(index);
            view = View.inflate(OfflineMap.this,
                    R.layout.activity_offlinemap, null);
            initViewItem(view, e);
            return view;
        }

        void initViewItem(View view, final MKOLUpdateElement e) {
            ImageButton remove = (ImageButton) view.findViewById(R.id.remove);
            final ImageButton start_stop = (ImageButton) view.findViewById(R.id.start_stop);
            TextView title = (TextView) view.findViewById(R.id.title);
            TextView update = (TextView) view.findViewById(R.id.update);
            TextView ratio = (TextView) view.findViewById(R.id.ratio);
            ratio.setText(e.ratio + "%");
            title.setText(e.cityName);

            if(e.ratio==100){
                start_stop.setVisibility(View.GONE);
            }

            if (e.update) {
                update.setText("可更新");
            } else {
                update.setText("最新");
            }

            remove.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    mOffline.remove(e.cityID);
                    updateView();
                }
            });
            start_stop.setOnClickListener(new OnClickListener() {
                boolean flag = true;
                @Override
                public void onClick(View view) {
                    if (flag) {
                        mOffline.pause(e.cityID);
                        start_stop.setBackgroundResource(R.drawable.stop);
                        flag = false;
                        Toast.makeText(getApplicationContext(), "暂停下载", Toast.LENGTH_SHORT).show();
                    } else {
                        mOffline.start(e.cityID);
                        start_stop.setBackgroundResource(R.drawable.start);
                        flag = true;
                        Toast.makeText(getApplicationContext(), "开始下载", Toast.LENGTH_SHORT).show();
                    }
                }
            });


        }

    }
    //监听键盘，以及事件的操作
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            //隐藏软件盘
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager.isActive()) {
                inputMethodManager.hideSoftInputFromWindow(OfflineMap.this.getCurrentFocus().getWindowToken(), 0);
            }

            return true;
        }
        return super.dispatchKeyEvent(event);
    }

}