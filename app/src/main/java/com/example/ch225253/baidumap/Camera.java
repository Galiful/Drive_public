package com.example.ch225253.baidumap;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Camera extends AppCompatActivity {

    private final static String TAG = "tsb";

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    //在关闭摄像机之前阻止应用程序退出的{链接信号量}。
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    ///为了使照片竖直显示
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    //预览textureView
    public TextureView textureView;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        textureView = (TextureView) findViewById(R.id.textureView);



        textureView.setSurfaceTextureListener(surfaceTextureListener);

    }
    public void opencamera2() {
        //查看是否拥有摄像头权限 没有就申请
        if (ActivityCompat.checkSelfPermission(Camera.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},100);
        }

        textureView.setSurfaceTextureListener(surfaceTextureListener);
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
            mImageReader = ImageReader.newInstance(1080, 1920, ImageFormat.JPEG, 3);
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

    /**
     * 关闭摄像头
     */
    private void closeCamera(){
        try{
            mCameraOpenCloseLock.acquire();
            if (mCameraCaptureSession !=null){
                mCameraCaptureSession.close();
                mCameraCaptureSession = null;
            }
            if (mCameraDevice!= null){
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (mImageReader !=null){
                mImageReader.close();
                mImageReader = null;
            }
            stopBackgroundThread();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * 停止线程
     */
    private void stopBackgroundThread(){
        if (mHandlerThread !=null){
            mHandlerThread.quitSafely();
            try {
                mHandlerThread.join();
                mHandlerThread = null;
                childHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * image转bitmap
     * @param image
     * @return
     */
    private Bitmap imageToBitmap(Image image){
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);//由缓冲区存入字节数组
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (permissions[0].equals(Manifest.permission.CAMERA) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            closeCamera();
            openCamera();
        }
    }
}