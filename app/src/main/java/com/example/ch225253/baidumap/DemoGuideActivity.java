/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.example.ch225253.baidumap;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNRoutePlanNode.CoordinateType;
import com.baidu.navisdk.adapter.BNaviCommonParams;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNRouteGuideManager;
import com.baidu.navisdk.adapter.map.BNItemizedOverlay;
import com.baidu.navisdk.adapter.map.BNOverlayItem;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


/**
 * 诱导界面
 */
public class DemoGuideActivity extends Activity {

    private static final String TAG = DemoGuideActivity.class.getName();

    private BNRoutePlanNode mBNRoutePlanNode = null;

    private IBNRouteGuideManager mRouteGuideManager;


    private Context mContext;
    private LinearLayout mRouteGuideLl;
    private TextView mRemainTimeTx;//剩余时间
    private TextView mRemainDistanceTx; // 剩余总距离
    private TextView mCurrentSpeedTx;//当前速度
    private ImageView mTurnImage;//转向图标
    private TextView mGoDistanceTx;//距下一路段距离
    private TextView mNextRoadTx;//下一路段
    private TextView mAlongMeters;
    private TextView mCurrentRoadTx;//当前路段
    private ImageView mEnlargeImg;
    private TextView mLocateTx;//是否gps定位


//    private EventView mEventDialog = null;
//    private Camera camera;

    //预览textureView
    private TextureView textureView;
    //摄像头管理器
    private CameraManager mCameraManager;
    //摄像头id 通常0代表后置摄像头，1代表前置摄像头
    private String mCameraID;
    //处理静态图像捕获
    private ImageReader mImageReader;
    private CameraCaptureSession mCameraCaptureSession;
    private CameraDevice mCameraDevice;

    private CaptureRequest.Builder previewBuilder;
    private CaptureRequest.Builder pictureBuilder;

    private HandlerThread mHandlerThread;
    private Handler childHandler;

    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createHandler();
        mRouteGuideManager = BaiduNaviManagerFactory.getRouteGuideManager();
        BaiduNaviManagerFactory.getProfessionalNaviSettingManager().enableBottomBarOpen(true);
        BaiduNaviManagerFactory.getProfessionalNaviSettingManager().setFullViewMode(0);
        BaiduNaviManagerFactory.getProfessionalNaviSettingManager().setDayNightMode(1);
        Context context = getApplicationContext();
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowMgr = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        windowMgr.getDefaultDisplay().getRealMetrics(dm);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, (int) (dm.heightPixels/dm.density*0.43+35),0,0);//外边距
        Log.d("height2", String.valueOf((int) (dm.heightPixels/dm.density*0.43+35)));

        BaiduNaviManagerFactory.getMapManager().getMapView().setTranslationY(-130);

        View view = mRouteGuideManager.onCreate(this, mOnNavigationListener);
        if (view != null) {
            setContentView(view,lp);
        }
        AddCamera();
        AddView();


        //查看是否拥有摄像头权限 没有就申请
        if (ActivityCompat.checkSelfPermission(DemoGuideActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},100);
        }
        textureView.setSurfaceTextureListener(surfaceTextureListener);

        mBNRoutePlanNode = new BNRoutePlanNode(12947471, 4846474, "百度大厦", "百度大厦", CoordinateType.BD09_MC);

//        Intent intent = getIntent();
//        if (intent != null) {
//            Bundle bundle = intent.getExtras();
//            if (bundle != null) {
//                mBNRoutePlanNode = (BNRoutePlanNode)
//                        bundle.getSerializable("routePlanNode");
//            }
//        }

        routeGuideEvent();
    }
    //TextureView 监听事件
    TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };
    /**
     * 打开摄像头
     */
    public void openCamera() {

        if (mHandlerThread == null){
            mHandlerThread = new HandlerThread("Camera2");
            mHandlerThread.start();
            childHandler = new Handler(mHandlerThread.getLooper());
        }

        if (mImageReader == null){
            Context context = getApplicationContext();
            DisplayMetrics dm = new DisplayMetrics();
            WindowManager windowMgr = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
            windowMgr.getDefaultDisplay().getRealMetrics(dm);
            mImageReader = ImageReader.newInstance(dm.widthPixels, (int) (dm.heightPixels), ImageFormat.JPEG, 3);
            mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                //可以在这里处理拍照得到的临时照片 例如，写入本地
                @Override
                public void onImageAvailable(ImageReader reader) {
                    // 拿到拍照照片数据
                    Image image = null;
                    try{
                        image = reader.acquireLatestImage();
                        final Bitmap bitmap = imageToBitmap(image);
                        if (bitmap != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    textureView.setVisibility(View.GONE);
                                }
                            });
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }finally {
                        if (image!=null){
                            image.close();
                            image = null;
                        }
                    }

                }
            }, childHandler);
        }

        if (mCameraManager == null)
            mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        String cameraIds[] = {};
        try {
            //获取所有摄像头ID
            cameraIds = mCameraManager.getCameraIdList();
        } catch (CameraAccessException e) {
            Log.e(TAG, "Cam access exception getting IDs", e);
        }
        if (cameraIds.length < 1) {
            Log.e(TAG, "No cameras found");
            return;
        }
        //通常0代表后置摄像头，1代表前置摄像头
        mCameraID = cameraIds[0];
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG,"没有照相机权限");
                return;
            }else{
                Log.e(TAG,"有照相机权限");
            }
            //开启摄像头
            mCameraManager.openCamera(mCameraID, stateCallback, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

    //摄像头监听事件
    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraOpenCloseLock.release();
            //打开摄像头
            mCameraDevice = camera;

            takePreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            mCameraOpenCloseLock.release();
            //关闭摄像头
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mImageReader.close();
                mImageReader = null;
            }
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            mCameraOpenCloseLock.release();
            mCameraDevice = null;
            //有错误
            Log.e(TAG,"摄像头开启失败");
        }
    };
    /**
     * 预览
     */
    private void takePreview() {
        SurfaceTexture texture = textureView.getSurfaceTexture();
        texture.setDefaultBufferSize(textureView.getWidth(), textureView.getHeight());
        Surface surface = new Surface(texture);
        try {
            if (previewBuilder == null){
                previewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                previewBuilder.addTarget(surface);
                // 自动对焦
                previewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                //   打开闪光灯
                previewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

            }
            mCameraDevice.createCaptureSession(Arrays.asList(surface,mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (mCameraDevice == null)
                        return;
                    mCameraCaptureSession = session;
                    try {
                        mCameraCaptureSession.setRepeatingRequest(previewBuilder.build(), null, childHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.e(TAG,"配置错误");
                }
            }, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private Bitmap imageToBitmap(Image image){
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);//由缓冲区存入字节数组
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    // 导航过程事件监听
    private void routeGuideEvent() {
//        EventHandler.getInstance().getDialog(this);
//        EventHandler.getInstance().showDialog();
//        getDialog(this);
//        mEventDialog.initview();
        BaiduNaviManagerFactory.getRouteGuideManager().setRouteGuideEventListener(
                new IBNRouteGuideManager.IRouteGuideEventListener() {
                    @Override
                    public void onCommonEventCall(int what, int arg1, int arg2, Bundle bundle) {
//                        EventHandler.getInstance().handleNaviEvent(what, arg1, arg2, bundle);
                        handleNaviEvent(what, arg1, arg2, bundle);
                    }
                }
        );

    }


    public void handleNaviEvent(int what, int arg1, int arg2, Bundle bundle) {
        Log.i("onCommonEventCall", String.format("%d,%d,%d,%s", what, arg1, arg2,
                (bundle == null ? "" : bundle.toString())));
//        if ( mEventDialog == null ) {
//            return ;
//        }
        switch (what) {
            case BNaviCommonParams.MessageType.EVENT_NAVIGATING_STATE_BEGIN:
                break;
            case BNaviCommonParams.MessageType.EVENT_NAVIGATING_STATE_END:
                break;
            case BNaviCommonParams.MessageType.EVENT_GPS_LOCATED:
                updateLocateState(true);
                break;
            case BNaviCommonParams.MessageType.EVENT_GPS_DISMISS:
                updateLocateState(false);
                break;
            case BNaviCommonParams.MessageType.EVENT_ON_YAW_SUCCESS:
                break;
            case BNaviCommonParams.MessageType.EVENT_ROAD_TURN_ICON_UPDATE:
                byte[] byteArray = bundle.getByteArray(BNaviCommonParams.BNGuideKey.ROAD_TURN_ICON);
                Bitmap map = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                updateTurnIcon(map);
                break;
            case BNaviCommonParams.MessageType.EVENT_ROAD_TURN_DISTANCE_UPDATE:
                String turndis = bundle.getString(BNaviCommonParams.BNGuideKey.TROAD_TURN_DISTANCE);
                updateGoDistanceTx(turndis);
                updateAlongMeters(turndis);
                break;
            case BNaviCommonParams.MessageType.EVENT_ROAD_NEXT_ROAD_NAME:
                String nextRoad = bundle.getString(BNaviCommonParams.BNGuideKey.NEXT_ROAD_NAME);
                if (!TextUtils.isEmpty(nextRoad)) {
                    updateNextRoad(nextRoad);
                }
                break;
            case BNaviCommonParams.MessageType.EVENT_ROAD_CURRENT_ROAD_NAME:
                String currentRoad = bundle.getString(BNaviCommonParams.BNGuideKey.CURRENT_ROAD_NAME);
                if (!TextUtils.isEmpty(currentRoad)) {
                    updateCurrentRoad(currentRoad);
                }
                break;
            case BNaviCommonParams.MessageType.EVENT_REMAIN_DISTANCE_UPDATE:
                String remainDisctance = bundle.getString(BNaviCommonParams.BNGuideKey.TOTAL_REMAIN_DISTANCE);
                updateRemainDistance(remainDisctance);
                break;
            case BNaviCommonParams.MessageType.EVENT_REMAIN_TIME_UPDATE:
                String remainTime = bundle.getString(BNaviCommonParams.BNGuideKey.TOTAL_REMAIN_TIME);
                updateRemainTime(remainTime);
                break;
            case BNaviCommonParams.MessageType.EVENT_RASTER_MAP_SHOW:
                int type = bundle.getInt(BNaviCommonParams.BNEnlargeRoadKey.ENLARGE_TYPE);
                byte[] arrowByte = bundle.getByteArray(BNaviCommonParams.BNEnlargeRoadKey.ARROW_IMAGE);
                byte[] bgByte = bundle.getByteArray(BNaviCommonParams.BNEnlargeRoadKey.BACKGROUND_IMAGE);
                Bitmap arrowMap = BitmapFactory.decodeByteArray(arrowByte, 0, arrowByte.length);
                Bitmap bgMap = BitmapFactory.decodeByteArray(bgByte, 0, bgByte.length);
                onEnlageShow(type, arrowMap, bgMap);
                break;
            case BNaviCommonParams.MessageType.EVENT_RASTER_MAP_UPDATE:
                String remainDistance = bundle.getString(BNaviCommonParams.BNEnlargeRoadKey.REMAIN_DISTANCE);
                String roadName = bundle.getString(BNaviCommonParams.BNEnlargeRoadKey.ROAD_NAME);
                int progress = bundle.getInt(BNaviCommonParams.BNEnlargeRoadKey.DRIVE_PROGRESS);
                break;
            case BNaviCommonParams.MessageType.EVENT_RASTER_MAP_HIDE:
                onEnlargeHide();
                break;
            case BNaviCommonParams.MessageType.EVENT_ROUTE_PLAN_SUCCESS:
                int distance = bundle.getInt(BNaviCommonParams.BNRouteInfoKey.TOTAL_DISTANCE);
                int time = bundle.getInt(BNaviCommonParams.BNRouteInfoKey.TOTAL_TIME);
                int tollFees = bundle.getInt(BNaviCommonParams.BNRouteInfoKey.TOLL_FESS);
                int lightCounts = bundle.getInt(BNaviCommonParams.BNRouteInfoKey.TRAFFIC_LIGHT);
                int gasMoney = bundle.getInt(BNaviCommonParams.BNRouteInfoKey.GAS_MONEY);
                break;
            case BNaviCommonParams.MessageType.EVENT_SERVICE_AREA_UPDATE:
                String firstName = bundle.getString(BNaviCommonParams.BNGuideKey.FIRST_SERVICE_NAME);
                int firstDistance = bundle.getInt(BNaviCommonParams.BNGuideKey.FIRST_SERVICE_TIME);
                String secondeName = bundle.getString(BNaviCommonParams.BNGuideKey.SECOND_SERVICE_NAME);
                int secondeDistance = bundle.getInt(BNaviCommonParams.BNGuideKey.SECOND_SERVICE_TIME);
                break;
            case BNaviCommonParams.MessageType.EVENT_CURRENT_SPEED:
                updateCurrentSpeed(String.valueOf(arg1));
                break;
            case BNaviCommonParams.MessageType.EVENT_ALONG_UPDATE:
                boolean isAlong = bundle.getBoolean(BNaviCommonParams.BNGuideKey.IS_ALONG);
                break;
            case BNaviCommonParams.MessageType.EVENT_CURRENT_MILES:
                int miles = arg1;
                break;
            default :
                // nothing
                break;
        }
    }
    public void AddView () {
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp1.setMargins(20,10,20,0);//外边距
        LayoutInflater inflater = LayoutInflater.from(this);
        View layout1 = inflater.inflate(R.layout.normal_demo_navi_event_textview, null);
        this.addContentView(layout1,lp1);

        mRouteGuideLl = layout1.findViewById(R.id.route_guide_ll);
        mRemainTimeTx = layout1.findViewById(R.id.remain_time_tx);
        mRemainDistanceTx = layout1.findViewById(R.id.remain_distance_tx);
        mCurrentSpeedTx = layout1.findViewById(R.id.current_speed_tx);
        mGoDistanceTx = layout1.findViewById(R.id.remain_distance);
        mNextRoadTx = layout1.findViewById(R.id.next_road_tx);
        mTurnImage = layout1.findViewById(R.id.turn_img);
        mAlongMeters = layout1.findViewById(R.id.along_meters_tx);
        mCurrentRoadTx = layout1.findViewById(R.id.current_road_tx);
        mEnlargeImg = layout1.findViewById(R.id.enlarge_view_img);
        mLocateTx = layout1.findViewById(R.id.loacte_tx);
    }

    public void AddCamera(){
        Context context = getApplicationContext();
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowMgr = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        windowMgr.getDefaultDisplay().getRealMetrics(dm);

        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,(int) (dm.heightPixels*(0.35)));
//        lp2.setMargins(0,0,0, 0);//外边距
        Log.d("dm.heightPixels", String.valueOf(dm.heightPixels));
        LayoutInflater inflater = LayoutInflater.from(this);
        View layout2 = inflater.inflate(R.layout.activity_camera, null);
        this.addContentView(layout2,lp2);
        textureView = (TextureView) findViewById(R.id.textureView);

    }


    @Override
    protected void onStart() {
        super.onStart();
        mRouteGuideManager.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRouteGuideManager.onResume();
        // 自定义图层
//        showOverlay();
    }

    private void showOverlay() {
        BNOverlayItem item =
                new BNOverlayItem(4825947, 12958160, BNOverlayItem.CoordinateType.BD09_MC);
        BNItemizedOverlay overlay = new BNItemizedOverlay(
                DemoGuideActivity.this.getResources().getDrawable(R.drawable
                        .navi_guide_turn));
        overlay.addItem(item);
        overlay.show();
    }

    protected void onPause() {
        super.onPause();
        mRouteGuideManager.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
        mRouteGuideManager.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRouteGuideManager.onDestroy(false);
        EventHandler.getInstance().disposeDialog();
    }

    @Override
    public void onBackPressed() {
        mRouteGuideManager.onBackPressed(false, true);
    }

    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mRouteGuideManager.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if ( !mRouteGuideManager.onKeyDown(keyCode, event) ) {
            return super.onKeyDown(keyCode, event);
        }
        return true;

    }

    private static final int MSG_RESET_NODE = 3;

    private Handler hd = null;

    private void createHandler() {
        if (hd == null) {
            hd = new Handler(getMainLooper()) {
                public void handleMessage(android.os.Message msg) {
                    if (msg.what == MSG_RESET_NODE) {
                        mRouteGuideManager.resetEndNodeInNavi(
                                new BNRoutePlanNode(116.21142, 40.85087, "百度大厦11",
                                        null, CoordinateType.GCJ02));
                    }
                }
            };
        }
    }

    private IBNRouteGuideManager.OnNavigationListener mOnNavigationListener =
            new IBNRouteGuideManager.OnNavigationListener() {

                @Override
                public void onNaviGuideEnd() {
                    // 退出导航
                    finish();
                }

                @Override
                public void notifyOtherAction(int actionType, int arg1, int arg2, Object obj) {
                    if (actionType == 0) {
                        // 导航到达目的地 自动退出
                        Log.i(TAG, "notifyOtherAction actionType = " + actionType + ",导航到达目的地！");
                        mRouteGuideManager.forceQuitNaviWithoutDialog();
                    }
                }
            };

    public void updateLocateState(boolean hasLocate) {
        if (mLocateTx != null) {
            mLocateTx.setText(hasLocate ? "定位成功" : "定位中");
        }
    }

    public void onEnlageShow(int type, Bitmap arrowBmp, Bitmap bgBmp) {
        if (mEnlargeImg != null) {
            mEnlargeImg.setImageBitmap(arrowBmp);
            mEnlargeImg.setBackgroundDrawable(new BitmapDrawable(bgBmp));
            mEnlargeImg.setVisibility(View.VISIBLE);
        }
        if (mRouteGuideLl != null) {
            mRouteGuideLl.setVisibility(View.GONE);
        }
    }

    public void onEnlargeHide() {
        if (mEnlargeImg != null) {
            mEnlargeImg.setVisibility(View.GONE);
        }
        if (mRouteGuideLl != null) {
            mRouteGuideLl.setVisibility(View.VISIBLE);
        }
    }

    public void updateTurnIcon(Bitmap map) {
        if (mTurnImage != null) {
            mTurnImage.setImageBitmap(map);
//            mTurnImage.setAlpha(100);
        }
    }

    public void updateGoDistanceTx(String tx) {
        if (mGoDistanceTx != null) {
            mGoDistanceTx.setText(tx);
        }
    }

    public void updateNextRoad(String nextRoad) {
        if (mNextRoadTx != null) {
            mNextRoadTx.setText(nextRoad);
        }
    }

    public void updateAlongMeters(String alongMeters) {
        if (mAlongMeters != null) {
            mAlongMeters.setText(alongMeters);
        }
    }

    public void updateCurrentRoad(String currentRoad) {
        if (mCurrentRoadTx != null) {
            mCurrentRoadTx.setText(currentRoad);
        }
    }

    public void updateCurrentSpeed(String speed) {
        if (mCurrentSpeedTx != null) {
            mCurrentSpeedTx.setText(speed);
        }
    }

    public void updateRemainDistance(String distance) {
        if (mRemainDistanceTx != null) {
            mRemainDistanceTx.setText(distance);
        }
    }

    public void updateRemainTime(String time) {
        if (mRemainTimeTx != null) {
            mRemainTimeTx.setText(time);
        }
    }
}
