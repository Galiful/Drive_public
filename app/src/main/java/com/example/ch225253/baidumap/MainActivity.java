package com.example.ch225253.baidumap;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNRoutePlanManager;
import com.baidu.navisdk.adapter.IBNTTSManager;
import com.baidu.navisdk.adapter.IBaiduNaviManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static android.widget.Toast.LENGTH_LONG;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
//    public LocationService locationService;
//    public Vibrator mVibrator;
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private BMapManager mapManager;

    private MyLocationConfiguration.LocationMode locationMode;

    private LatLng latLng,latLng_re;
    private volatile LatLng latLng_addr;
    private boolean isFirstLoc = true; // 是否首次定位
    private boolean isFirstclick =true;//是否首次点击叫车按钮


    private double latitude;
    private double longitude;
    private String mInitLatitude;
    private String mInitLongtitude;
    private EditText editText_loc;
    private EditText editText_addr;
    private TextView textview_info;
    private Button button;
    private ImageButton imageButton;
    private BitmapDescriptor icon_geo;
    private BitmapDescriptor icon_addr;
    private UiSettings uiSettings;
    private String addr;
    private String locaAddrStr;
    private Toolbar toolbar;
    private String ip ="127.0.0.1";
    private int port = 8989;
    private  String response="";
    private String user_num = "01";
    private Socket socket=null;
    private DrawerLayout drawerLayout;
    //导航
    private static final String APP_FOLDER_NAME = "BNSDKSimpleDemo";
    private String mSDCardPath = null;
    private static final String[] authBaseArr = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
//    gradlew.bat clean



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SDKInitializer.initialize(getApplicationContext());//创建一个LocationClient的实例，
        // LocationClient的构建函数接收一个Context参数，这里调用getApplicationContext(),方法来获取一个全局的Context参数并传入。
        // 然后调用LocationClient的registerLocationListener（）方法来注册一个定位监听器，当获取到位置信息的时候，就会回调这个定位监听器。

        setContentView(R.layout.activity_main);
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
//        mBaiduMap.setMyLocationEnabled(true);

//        Window window = new AppCompatActivity().getWindow();
////        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
////        window.setStatusBarColor(new AppCompatActivity().getResources().getColor(colorResId));
//
//        //底部导航栏
//        window.setNavigationBarColor(new AppCompatActivity().getResources().getColor(R.color.colorPrimaryDark));

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //状态栏透明、沉浸
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);


        button = (Button) findViewById(R.id.button_loca);
        button.setOnClickListener(this);
        imageButton = (ImageButton) findViewById(R.id.imageButton);
        imageButton.setOnClickListener(this);
        editText_loc = (EditText) findViewById(R.id.editText_loc);
        editText_addr = (EditText) findViewById(R.id.editText_addr);
        textview_info = (TextView) findViewById(R.id.textview_info);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        navView();//左滑菜单

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }
        icon_geo = BitmapDescriptorFactory.fromResource(R.drawable.geo);
        icon_addr = BitmapDescriptorFactory.fromResource(R.drawable.addr);

        //程序创建时取上次活动销毁的定位信息
//        SharedPreferences settings = getSharedPreferences("GeoPoint_Info", MODE_PRIVATE);
//        mInitLatitude = settings.getString("latitude", mInitLatitude);
//        mInitLongtitude = settings.getString("longitude", mInitLongtitude);
//        Log.d("SharedPreferences", mInitLatitude + "," + mInitLongtitude);
//        if(!"".equals(mInitLatitude)){
//            latLng_re = new LatLng(Double.parseDouble(mInitLatitude), Double.parseDouble(mInitLongtitude));
//            MapStatus.Builder builder = new MapStatus.Builder();
//            builder.target(latLng_re).zoom(16.0f);
//            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
//        }


        //动态获取权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissionList = new ArrayList<>();
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(Manifest.permission.READ_PHONE_STATE);
            }
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (!permissionList.isEmpty()) {
                String[] permissions = permissionList.toArray(new String[permissionList.size()]);
                ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
            }
        }
        initLocation();
        Dialog();
        initPoiListener();//地图poi监听
    }

    private void Dialog(){
        LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = connectivityManager.getActiveNetworkInfo();
        // 定义提示内容
        String tipCont = "";
        if (!locManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) && mNetworkInfo == null) {
            tipCont = "请在设置中打开GPS和网络开关";
        } else if (!locManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) && mNetworkInfo != null) {
            tipCont = "请在设置中打开GPS开关";
        } else if (locManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) && mNetworkInfo == null) {
            tipCont = "请在设置中打开网络开关";
        }
        //提示dialog
        if (!tipCont.equals("")) {
            final CommonDialog dialog = new CommonDialog(this);
            dialog.setMessage(tipCont);
            dialog.setTitle("系统提示：");
            dialog.setSingle(true).setOnClickBottomListener(new CommonDialog.OnClickBottomListener() {
                @Override
                public void onPositiveClick() {
                    initLocation();
                    dialog.dismiss();
                }

                @Override
                public void onNegtiveClick() {
                    dialog.dismiss();
                }
            }).show();
        }
        initLocation();
    }
    //NavigationView左滑菜单布局
    private void navView(){
        NavigationView navView = (NavigationView)findViewById(R.id.nav_view);
        View headview=navView.inflateHeaderView(R.layout.nav_header);
        ImageView icon_image= (ImageView) headview.findViewById(R.id.icon_image);
        icon_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_item_ip:
                        Intent intent = new Intent(MainActivity.this, IpSetActivity.class);
                        intent.putExtra("item_ip", ip);
                        intent.putExtra("port", port);
                        startActivityForResult(intent, 1);
                        break;
                    case R.id.menu_item_return:
                        new Thread() {
                            public void run() {
                                socket("i need a car");
                            }
                        }.start();
                        break;
                    case R.id.menu_item_reset:
                        //隐藏显示框
                        editText_loc.setVisibility(View.INVISIBLE);
                        editText_addr.setVisibility(View.INVISIBLE);
                        textview_info.setVisibility(View.INVISIBLE);
                        mBaiduMap.clear();//清除地图上标记的点
                        latLng_addr = null;
                        response = "";
                        locaAddrStr = "";
                        addr = "";
                        editText_loc.setText(locaAddrStr);
                        editText_addr.setText(addr);
                        textview_info.setText(response);
                        button.setText("叫 车");
                        button.setEnabled(true);
                        isFirstclick = true;
                        //关闭socket
                        if (socket!=null) {
                            try {
                                socket.getOutputStream().close();
                                socket.getInputStream().close();
                                socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        Toast.makeText(MainActivity.this, "重置成功~", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.menu_item_user:
                        NavigationView navView = (NavigationView)findViewById(R.id.nav_view);
                        View view = navView.getHeaderView(0);
                        TextView username = view.findViewById(R.id.user);
                        if ("01".equals(user_num)) {
                            user_num = "02";
                            username.setText("：用户"+user_num);
                            Toast.makeText(MainActivity.this, "已切换，请重置召车操作~", Toast.LENGTH_SHORT).show();
                        } else {
                            user_num = "01";
                            username.setText("：用户"+user_num);
                            Toast.makeText(MainActivity.this, "已切换，请重置召车操作~", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.menu_item_carinfo:
                        textview_info.setVisibility(View.VISIBLE);
                        textview_info.setText(response);
                        Toast.makeText(MainActivity.this, "刷新成功", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.menu_item_verify:
//                        Intent intent1 = new Intent(MainActivity.this, FaceLogin.class);
//                        startActivity(intent1);
                        break;
                    case R.id.menu_item_camera:
                        Intent intent2 = new Intent(MainActivity.this, DemoMainActivity.class);
                        startActivity(intent2);
                        break;
                    case R.id.menu_item_offlinemap:
                        Intent intent3 = new Intent(MainActivity.this, OfflineMap.class);
                        startActivity(intent3);
                        break;
                    case R.id.menu_item_about:
                        Intent intent4 = new Intent(MainActivity.this, About.class);
                        startActivity(intent4);
                        break;
                    default:
                        break;
                }
                drawerLayout.closeDrawers();
                return true;
            }
        });
    }
    //左滑NavigationView
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                drawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    //数据回传
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    ip = data.getStringExtra("item_ip");
                    port = data.getIntExtra("port",8989);
                }
                break;
            default:
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", LENGTH_LONG).show();
                            finish();
                            return;
                        }
                    }
                    Dialog();
                } else {
                    Toast.makeText(this, "发生未知错误", LENGTH_LONG).show();
                    finish();
                }
                break;
            case 2:
                for (int ret : grantResults) {
                    if (ret == 0) {
                        continue;
                    } else {
                        Toast.makeText(MainActivity.this, "缺少导航基本的权限!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                initNavi();
                break;
            default:
        }//onRequestPermissionsResult()方法中，对权限申请结果进行逻辑判断。这里使用一个循环对每个权限进行判断，
        // 如果有任意一个权限被拒绝了，那么就会直接调用finish()方法关闭程序，只有当所有的权限被用户同意了，才会
        // 调用requestPermissions()方法开始地理位置定位。
    }

    private void initPoiListener() {
        mBaiduMap.setOnMapClickListener(new OnMapClickListener() {
            /**
             * 地图单击事件回调函数
             * @param point 点击的地理坐标
             */
            @Override
            public void onMapClick(LatLng point) {
//                mBaiduMap.clear();//这个方法清除地图上所有的mark点
//                getLocateinfo(latLng);//这个方法是根据坐标点（经纬度）获取位置信息
//                latLng=point;
//                Log.d("click", String.valueOf(latLng));
//                OverlayOptions overlayOptions = new MarkerOptions().position(latLng).icon(icon_geo).perspective(true);//这个方法是生成一个mark点信息，就是地图上的图标点
//                mBaiduMap.addOverlay(overlayOptions);//将上面生成的mark点添加到地图上
            }

            /**
             * 地图内 Poi 单击事件回调函数
             * @param poi 点击的 poi 信息
             */
            @Override
            public boolean onMapPoiClick(MapPoi poi) {
                mBaiduMap.clear();
                addr = poi.getName();
                latLng_addr = poi.getPosition();
                Log.d("latLng_addr", String.valueOf(latLng_addr));
//                OverlayOptions overlayOptions = new MarkerOptions().position(latLng).icon(icon_geo).perspective(true);//这个方法是生成一个mark点信息，就是地图上的图标点
//                mBaiduMap.addOverlay(overlayOptions);//将上面生成的mark点添加到地图上
                mBaiduMap.addOverlay(
                        new MarkerOptions()
                                .position(latLng_addr)
                                .icon(icon_addr)
                                .title(addr)
                );
                editText_addr.setText(addr);
                button.setText("叫 车");
                button.setEnabled(true);
                button.setTextColor(Color.parseColor("#ffffff"));
                textview_info.setVisibility(View.GONE);
                return true;
            }
        });
        //长按取点
        mBaiduMap.setOnMapLongClickListener(new BaiduMap.OnMapLongClickListener() {
            /**
             * 长按地图
             */
            public void onMapLongClick(LatLng point) {
                mBaiduMap.clear();//这个方法清除地图上所有的mark点
                latLng_addr = point;
                Log.d("latLng_addr", String.valueOf(latLng_addr));
                GeoCoder geoCoder = GeoCoder.newInstance();
                //设置反地理编码位置坐标

                //设置地址或经纬度反编译后的监听,这里有两个回调方法,
                OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
                    //经纬度转换成地址
                    @Override
                    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
                        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                            //没有检索到结果
                            Toast.makeText(MainActivity.this, "没有检测到结果，请检查网络",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            //获取地理编码结果
                            addr = result.getAddress();
                            editText_addr.setText(addr);
                            button.setText("叫 车");
                            button.setEnabled(true);
                            button.setTextColor(Color.parseColor("#ffffff"));
                            textview_info.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
                    }

                };
                geoCoder.setOnGetGeoCodeResultListener(listener);
                geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng_addr));


                OverlayOptions overlayOptions = new MarkerOptions().position(latLng_addr).icon(icon_addr).perspective(true);//这个方法是生成一个mark点信息，就是地图上的图标点
                mBaiduMap.addOverlay(overlayOptions);//将上面生成的mark点添加到地图上
            }
        });

    }


    private void initLocation() {

        LocationClient locationClient = new LocationClient(getApplicationContext());
        MyLocationListener myLocationListener  = new MyLocationListener();
        LocationClientOption locationOption  = new LocationClientOption();
        //注册监听函数
        locationClient.registerLocationListener(myLocationListener );

        //设置坐标类型
        locationOption .setCoorType("bd09ll");
        //设置是否需要地址信息，默认为无地址
        locationOption .setIsNeedAddress(true);
        locationOption .setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        // Hight_Accuracy表示高精确度模式，会在GPS信号正常的情况下优先使用GPS定位，在无法接收GPS信号的时候使用网络定位。
        // Battery_Saving表示节电模式，只会使用网络进行定位。
        // Device_Sensors表示传感器模式，只会使用GPS进行定位。
        //可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        locationOption .setLocationNotify(true);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        locationOption.setIgnoreKillProcess(true);
        //设置是否打开gps进行定位
        locationOption .setOpenGps(true);
        //设置扫描间隔，单位是毫秒 当<1000(1s)时，定时定位无效
        locationOption .setScanSpan(3000);
        //设置 LocationClientOption
        locationClient.setLocOption(locationOption );
        locationClient.start();



        //自定义定位icon
        MyLocationConfiguration configuration
                = new MyLocationConfiguration(locationMode, false, icon_geo);
        mBaiduMap.setMyLocationConfigeration(configuration);

//        mMapView.removeViewAt(1);//隐藏百度logo
        View child = mMapView.getChildAt(1);
        if (child != null && (child instanceof ImageView || child instanceof ZoomControls)){
            child.setVisibility(View.INVISIBLE);
        }
        //禁用旋转
        uiSettings = mBaiduMap.getUiSettings();
        uiSettings .setRotateGesturesEnabled(false);
        uiSettings .setCompassEnabled(false);

        //自定义比例尺位置
        mBaiduMap.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                // TODO Auto-generated method stub
//                WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//                int height = wm.getDefaultDisplay().getHeight();
                Rect rect = new Rect();
                getWindow().findViewById(Window.ID_ANDROID_CONTENT).getDrawingRect(rect);
                mMapView.setScaleControlPosition(new Point(20, (rect.height()-40)));
                Log.d("height1", String.valueOf(rect.height()));
            }
        });

        mBaiduMap.setMyLocationEnabled(true);//显示定位图层
    }

    //实现BDLocationListener接口,BDLocationListener为结果监听接口，异步获取定位结果
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {

            latitude = location.getLatitude();
            longitude = location.getLongitude();
            locaAddrStr = location.getAddrStr();
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
            if(location.getLongitude()==4.9E-324){
                latLng=new LatLng(39.91541675024999, 116.40385626888427);
//                Toast.makeText(MainActivity.this, "请打开GPS和网络获取您的准确位置", Toast.LENGTH_SHORT).show();
            }

            // 构造定位数据
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(0)
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(latLng.latitude)
                    .longitude(latLng.longitude).build();
            mBaiduMap.setMyLocationData(locData);
            Log.i("MyLocationListener", location.getLocType()+":"+location.getLocTypeDescription()+location.getLatitude()+" "+location.getLongitude()+" "+location);

            if (isFirstLoc) {
                if(location.getLongitude()==4.9E-324){
//                    latLng=new LatLng(39.91541675024999, 116.40385626888427);
                    Toast.makeText(MainActivity.this, "请打开网络和GPS以获取您的准确位置", Toast.LENGTH_SHORT).show();
                }
                isFirstLoc = false;
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(latLng).zoom(16.0f);
//                mBaiduMap.animateMapStatus(status);//动画的方式到中间
                mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));//直接到中间
            }

        }

    }



    //socket通信
    public void socket(String a) {
        Looper.prepare();
            try {
                socket = new Socket();
                //经过测试socket有个默认的超时时间，大概在2秒左右
                socket.connect(new InetSocketAddress(ip, port), 2000);
                // 建立连接后获得输出流
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(a.getBytes("UTF-8"));
                //通过shutdownOutput高速服务器已经发送完数据，后续只能接受数据
//          socket.shutdownOutput();
                Toast.makeText(this, "召车成功", Toast.LENGTH_SHORT).show();

                InputStream inputStream = socket.getInputStream();
                byte[] bytes = new byte[1024];
                int len;
                while ((len = inputStream.read(bytes)) != -1) {
                    //注意指定编码格式，发送方和接收方一定要统一，建议使用UTF-8("GB2312")
//            sb.append(new String(bytes, 0, len, "UTF-8"));
                    response = new String(bytes, 0, len, "GB2312");
                }

//        inputStream.close();
//        outputStream.close();
//        socket.close();
            } catch (IOException e) {
                Log.e("服务器异常", e.toString());
                response= "服务器异常";
                Toast.makeText(this, "服务器连接失败！", Toast.LENGTH_SHORT).show();
            }
        Looper.loop();


    }
    //声明一个long类型变量：用于存放上一点击“返回键”的时刻
    //再按一次退出程序
    private long mExitTime;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //判断用户是否点击了“返回键”
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //与上次点击返回键时刻作差
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                //大于2000ms则认为是误操作，使用Toast进行提示
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                //并记录下本次点击“返回键”的时刻，以便下次进行判断
                mExitTime = System.currentTimeMillis();
            } else {
                //小于2000ms则认为是用户确实希望退出程序-调用System.exit()方法进行退出
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //地理编码搜索（用地址检索坐标）
    public void geocoder(){
        GeoCoder mSearch = GeoCoder.newInstance();

        //设置地址或经纬度反编译后的监听,这里有两个回调方法
        OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {

            }
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
                if (geoCodeResult == null || geoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    //没有检索到结果
                    Toast.makeText(MainActivity.this, "没有检测到结果，请检查网络",
                            Toast.LENGTH_SHORT).show();
                } else {
                    String strInfo = String.format("纬度：%f 经度：%f",
                            geoCodeResult.getLocation().latitude, geoCodeResult.getLocation().longitude);
                    latLng_addr = new LatLng(geoCodeResult.getLocation().latitude, geoCodeResult.getLocation().longitude);
                    mBaiduMap.clear();
                    OverlayOptions overlayOptions = new MarkerOptions().position(geoCodeResult.getLocation()).icon(icon_addr).perspective(true);//这个方法是生成一个mark点信息，就是地图上的图标点
                    mBaiduMap.addOverlay(overlayOptions);
                    MapStatusUpdate mapStatusUpdate2 = MapStatusUpdateFactory.newLatLng(latLng_addr);
                    mBaiduMap.animateMapStatus(mapStatusUpdate2);//回调到目的地
                    Toast.makeText(MainActivity.this, strInfo, Toast.LENGTH_SHORT).show();
                }
            }

        };

        mSearch.setOnGetGeoCodeResultListener(listener);
        mSearch.geocode(new GeoCodeOption().city(
                "").address(editText_addr.getText().toString()));
        mSearch.destroy();

    }

    //监听键盘，目的地搜索
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            //隐藏软件盘
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager.isActive()) {
                inputMethodManager.hideSoftInputFromWindow(MainActivity.this.getCurrentFocus().getWindowToken(), 0);
            }

            if(!"".equals(editText_addr.getText())){
                geocoder();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    public void navi(){
        if (initDirs()) {
            initNavi();
        }

//        final DemoMainActivity demoMainActivity = new DemoMainActivity();
//        if(demoMainActivity.initDirs()){
//            demoMainActivity.initNavi();
//        }
        final CommonDialog dialog2 = new CommonDialog(this);
        dialog2.setMessage("是否开启智能导航？")
                .setImageResId(R.drawable.navi_dialog)
//                .setTitle("系统提示")
                .setSingle(false).setOnClickBottomListener(new CommonDialog.OnClickBottomListener() {
            @Override
            public void onPositiveClick() {

                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 100);
                    return;
                }

                if(BaiduNaviManagerFactory.getBaiduNaviManager().isInited()){
                    initListener();
                }
                dialog2.dismiss();
            }

            @Override
            public void onNegtiveClick() {
                dialog2.dismiss();
            }
        }).show();

    }
    public void initListener(){
        final BNRoutePlanNode sNode = new BNRoutePlanNode(latLng.longitude, latLng.latitude, "奇瑞汽车龙山试验中心", "奇瑞汽车龙山试验中心", BNRoutePlanNode.CoordinateType.BD09LL);
        BNRoutePlanNode eNode = new BNRoutePlanNode(latLng_addr.longitude, latLng_addr.latitude, "奇瑞汽车股份有限公司研发楼", "奇瑞汽车股份有限公司研发楼", BNRoutePlanNode.CoordinateType.BD09LL);
        List<BNRoutePlanNode> list = new ArrayList<BNRoutePlanNode>();
        list.add(sNode);
        list.add(eNode);
        BaiduNaviManagerFactory.getRoutePlanManager().routeplanToNavi(
                list,
                IBNRoutePlanManager.RoutePlanPreference.ROUTE_PLAN_PREFERENCE_DEFAULT,
                null,
                new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        switch (msg.what) {
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_START:
                                Toast.makeText(MainActivity.this, "算路开始", Toast.LENGTH_SHORT)
                                        .show();
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_SUCCESS:
//                                Toast.makeText(MainActivity.this, "算路成功", Toast.LENGTH_SHORT)
//                                        .show();
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_FAILED:
                                Toast.makeText(MainActivity.this, "算路失败", Toast.LENGTH_SHORT)
                                        .show();
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_TO_NAVI:
                                Toast.makeText(MainActivity.this, "算路成功准备进入导航", Toast.LENGTH_SHORT)
                                        .show();
                                Intent intent = new Intent(MainActivity.this,
                                        DemoGuideActivity.class);
                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable("routePlanNode", sNode);
                                        intent.putExtras(bundle);
                                startActivity(intent);
                                break;
                            default:
                                // nothing
                                break;
                        }
                    }
                });

    }
    public boolean initDirs() {
        mSDCardPath = getSdcardDir();
        if (mSDCardPath == null) {
            return false;
        }
        File f = new File(mSDCardPath, APP_FOLDER_NAME);
        if (!f.exists()) {
            try {
                f.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
    private String getSdcardDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }
    private boolean hasBasePhoneAuth() {
        PackageManager pm = this.getPackageManager();
        for (String auth : authBaseArr) {
            if (pm.checkPermission(auth, this.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    public void initNavi() {
        // 申请权限
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (!hasBasePhoneAuth()) {
                this.requestPermissions(authBaseArr, 2);
                return;
            }
        }

        BaiduNaviManagerFactory.getBaiduNaviManager().init(this,

                    mSDCardPath, APP_FOLDER_NAME, new IBaiduNaviManager.INaviInitListener() {
                    @Override
                    public void onAuthResult(int status, String msg) {
                        String result;
                        if (0 == status) {
                            result = "key校验成功!";
                        } else {
                            result = "key校验失败, " + msg;
                            Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
                        }

                    }

                    @Override
                    public void initStart() {
                        Toast.makeText(MainActivity.this, "百度导航引擎初始化开始", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void initSuccess() {
                        Toast.makeText(MainActivity.this, "百度导航引擎初始化成功", Toast.LENGTH_SHORT).show();
                        // 初始化tts
                        initTTS();
                    }

                    @Override
                    public void initFailed() {
                        Toast.makeText(MainActivity.this, "百度导航引擎初始化失败", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void initTTS() {
        // 使用内置TTS
        BaiduNaviManagerFactory.getTTSManager().initTTS(getApplicationContext(),
                getSdcardDir(), APP_FOLDER_NAME, NormalUtils.getTTSAppID());

        // 不使用内置TTS
//        BaiduNaviManagerFactory.getTTSManager().initTTS(mTTSCallback);

        // 注册同步内置tts状态回调
        BaiduNaviManagerFactory.getTTSManager().setOnTTSStateChangedListener(
                new IBNTTSManager.IOnTTSPlayStateChangedListener() {
                    @Override
                    public void onPlayStart() {
                        Log.e("BNSDKDemo", "ttsCallback.onPlayStart");
                    }

                    @Override
                    public void onPlayEnd(String speechId) {
                        Log.e("BNSDKDemo", "ttsCallback.onPlayEnd");
                    }

                    @Override
                    public void onPlayError(int code, String message) {
                        Log.e("BNSDKDemo", "ttsCallback.onPlayError");
                    }
                }
        );

        // 注册内置tts 异步状态消息
        BaiduNaviManagerFactory.getTTSManager().setOnTTSStateChangedHandler(
                new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        Log.e("BNSDKDemo", "ttsHandler.msg.what=" + msg.what);
                    }
                }
        );
    }



    @Override
    public void onClick(View v) {
        editText_addr.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d("TextChanged", charSequence + " " + i + " " + i1 + " " + i2);
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d("TextChanged", charSequence + " " + i + " " + i1 + " " + i2);
                if (charSequence.length() == i & i != 0) {
                    //表示删除动作
                    mBaiduMap.clear();
                    latLng_addr = null;
                    button.setText("叫 车");
                    button.setEnabled(true);
                    button.setTextColor(Color.parseColor("#ffffff"));
                    textview_info.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.d("TextChanged", " " + editable);
//                if(editable.length()==0){
//                    latLng_addr = null;
//                    button_fill.setText("叫 车");
//                    button_fill.setEnabled(true);
//                }
            }
        });

        switch(v.getId()){
            case R.id.button_loca:
                if (isFirstclick) {
                    editText_loc.setVisibility(View.VISIBLE);
                    editText_loc.setText(locaAddrStr);
                    editText_addr.setVisibility(View.VISIBLE);

                    //重定位,回调到当前位置
                    MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLngZoom(latLng, 16.0f);
                    mBaiduMap.animateMapStatus(mapStatusUpdate);
                    isFirstclick = false;
                } else if (!isFirstclick & latLng_addr == null & "".equals(editText_addr.getText().toString())) {
                    Toast.makeText(MainActivity.this, "请先点击或输入您的目的地~", Toast.LENGTH_SHORT).show();
                }

                if (!isFirstclick & latLng_addr != null) {
                    new Thread() {
                        public void run() {
                            try {
                                sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            socket("YH CM 0 :M" + user_num + "|0|" + latitude + ";" + longitude + ";" + latLng_addr.latitude + ";" + latLng_addr.longitude + "0\\r\\n");
                        }
                    }.start();
                    button.setText("召车中");
                    button.setEnabled(false);
                    button.setTextColor(Color.parseColor("#85ffffff"));
                    navi();
                    textview_info.setVisibility(View.VISIBLE);
                    textview_info.setText(response);
                } else if (latLng_addr == null & !"".equals(editText_addr.getText().toString())) {
                    geocoder();//根据文字搜索目的地
                    Log.d("latLng_addr", String.valueOf(latLng_addr));
                    if (latLng_addr != null) {
                        new Thread() {
                            public void run() {
                                try {
                                    sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                socket("YH CM 0 :M" + user_num + "|0|" + latitude + ";" + longitude + ";" + latLng_addr.latitude + ";" + latLng_addr.longitude + "0\\r\\n");
                            }
                        }.start();
                    }
                    button.setText("召车中");
                    button.setEnabled(false);
                    button.setTextColor(Color.parseColor("#85ffffff"));
//                    button_fill.setBackgroundColor(Color.parseColor("#FF6189FD"));
                    textview_info.setVisibility(View.VISIBLE);
                    textview_info.setText(response);
                }
                break;

            case R.id.imageButton:
                initLocation();
                MapStatusUpdate mapStatusUpdate2 = MapStatusUpdateFactory.newLatLngZoom(latLng, 16.0f);
                mBaiduMap.animateMapStatus(mapStatusUpdate2);//回调到当前位置
                break;

            default:
                break;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    //程序销毁后保存当前定位信息
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        if(mapManager != null )
            mapManager.destroy();
        /*退出时保存这次的定位信息*/
        SharedPreferences settings=getSharedPreferences("GeoPoint_Info",MODE_PRIVATE);
        settings.edit().clear();
        settings.edit().putString("latitude", String.valueOf(latitude)).apply();
        settings.edit().putString("longitude", String.valueOf(longitude)).apply();
        mapManager=null;
        Log.d("onDestroy","onDestroy:"+String.valueOf(latitude)+","+longitude);
    }

}