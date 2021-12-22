package com.hikvision.dspjourney.camera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.hikvision.dspjourney.R;
import com.hikvision.dspjourney.utils.BitmapUtils;
import com.hikvision.dspjourney.utils.CameraUtil;
import com.hikvision.dspjourney.utils.FileUtils;
import com.hikvision.dspjourney.utils.SystemUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class CameraActivity extends FragmentActivity {
    private static final String TAG = "CameraActivity";
    private Camera mCamera;
    private SurfaceHolder mSurfaceHolder;
    SurfaceView surfaceview;
    private int curCameraId = 0;//前后摄像头
    private boolean hadPrinted = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_layout);
        initSurfaceView();
    }

    private void initSurfaceView() {
        surfaceview=findViewById(R.id.camSurfaceview);
        mSurfaceHolder = surfaceview.getHolder();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                handleSurfaceCreated();
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                handleSurfaceDestroyed();
            }
        });
    }

    private void handleSurfaceCreated() {
        Log.d(TAG, "handleSurfaceCreated: start");
        if (mCamera == null) {
            initCamera(curCameraId);
        }
        try {
            //问题2：页面重新打开后SurfaceView的内容黑屏
            //Camera is being used after Camera.release() was called
            //在surfaceDestroyed时调用了Camera的release 但是没有设置为null,
            //--》如何解耦合，把生命周期相关的方法和Camera的生命周期绑定而不时在回调中处理，方便业务实现
            //onResume--》surfaceCreated
            //onPause--》surfaceDestroyed
            mCamera.setPreviewDisplay(mSurfaceHolder);
            startPreview();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "handleSurfaceCreated: " + e.getMessage());
        }
        Log.i(TAG, "handleSurfaceCreated: end");
    }

    private void handleSurfaceDestroyed() {
        releaseCamera();
        mSurfaceHolder = null;
        Log.i(TAG, "handleSurfaceDestroyed: ");
    }
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }
    private void initCamera(int cameraId) {
        curCameraId = cameraId;
        mCamera = Camera.open(curCameraId);
        Log.d(TAG, "initCamera: Camera Open ");
        setCamerDisplayOrientation(this, curCameraId, mCamera);
        if (!hadPrinted) {
            printCameraInfo();
            hadPrinted = true;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        Camera.Size closelyPreSize = CameraUtil.getCloselyPreSize(true, SystemUtils.getDisplayWidth(), SystemUtils.getDisplayHeight(), parameters.getSupportedPreviewSizes());
        Log.i(TAG, "initCamera: closelyPreSizeW="+closelyPreSize.width+" closelyPreSizeH="+closelyPreSize.height);
        parameters.setPreviewSize(closelyPreSize.width, closelyPreSize.height);
        mCamera.setParameters(parameters);
    }


    /**
     * 设置Camera展示的方向，如果不设置就会导致前摄像方向不对
     *
     * @param activity
     * @param cameraId
     * @param camera
     */
    public static void setCamerDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Log.i(TAG, "setCamerDisplayOrientation: rotation=" + rotation + " cameraId=" + cameraId);
        int degress = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degress = 0;
                break;
            case Surface.ROTATION_90:
                degress = 90;
                break;
            case Surface.ROTATION_180:
                degress = 180;
                break;
            case Surface.ROTATION_270:
                degress = 270;
                break;
        }
        int result = 0;
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (cameraInfo.orientation + degress) % 360;
            //镜像
            //问题4：前摄像头出现倒立的情况
            result = (360 - result) % 360;

        } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            //result = (cameraInfo.orientation - degress + 360) % 360;
        }
        Log.i(TAG, "setCamerDisplayOrientation: result=" + result + " cameraId=" + cameraId + " facing=" + cameraInfo.facing + " cameraInfo.orientation=" + cameraInfo.orientation);

        camera.setDisplayOrientation(result);
    }

    private void printCameraInfo() {
        //1. 调用getParameters获取Parameters
        Camera.Parameters parameters = mCamera.getParameters();
        //2. 获取Camera预览支持的图片格式(常见的是NV21和YUV420sp)
        int previewFormat = parameters.getPreviewFormat();
        Log.d(TAG, "initCamera: previewFormat=" + previewFormat); // NV21
        //3. 获取Camera预览支持的W和H的大小，
        // 手动设置Camera的W和H时，要检测camera是否支持，如果设置了Camera不支持的预览大小，会出现黑屏。
        // 那么这里有一个问，由于Camera不同厂商支持的预览大小不同，如果做到兼容呐？
        // 需要使用方采用一定策略进行选择（比如：选择和预设置的最接近的支持的WH）
        //通过输出信息，我们可以看到Camera是横向的即 W>H
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        for (Camera.Size item : supportedPreviewSizes
        ) {
            Log.d(TAG, "initCamera: supportedPreviewSizes w= " + item.width + " h=" + item.height);
        }

        //可以看到Camera的宽高是屏幕的宽高是不一致的，手机屏幕是竖屏的H>W，而Camera的宽高是横向的W>H
        Camera.Size previewSize = parameters.getPreviewSize();
        int[] physicalSS = SystemUtils.getPhysicalSS(this);
        Log.i(TAG, "initCamera: w=" + previewSize.width + " h=" + previewSize.height
                + " screenW=" + SystemUtils.getDisplayWidth() + " screenH=" + SystemUtils.getDisplayHeight()
                + " physicalW=" + physicalSS[0] + " physicalH=" + physicalSS[1]);

        //4. 获取Camera支持的帧率 一般是10～30
        List<Integer> supportedPreviewFrameRates = parameters.getSupportedPreviewFrameRates();
        for (Integer item : supportedPreviewFrameRates
        ) {
            Log.i(TAG, "initCamera: supportedPreviewFrameRates frameRate=" + item);
        }

        //5. 获取Camera的个数信息，以及每一个Camera的orientation，这个很关键，如果根据Camera的orientation正确的设置Camera的DisplayOrientation可能会导致预览倒止或者出现镜像的情况
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            Log.i(TAG, "initCamera: facing=" + cameraInfo.facing
                    + " orientation=" + cameraInfo.orientation);
        }
    }


    private void startPreview() {
//        mCamera.setPreviewCallback(new Camera.PreviewCallback() {
//            @Override
//            public void onPreviewFrame(byte[] data, Camera camera) {
//                Log.i(TAG, "onPreviewFrame: setPreviewCallback");
//            }
//        });
        //问题六：很多时候，不仅仅要预览，在预览视频的时候，希望能做一些检测，比如人脸检测等。这就需要获得预览帧视频，该如何做呐？
        mCamera.setOneShotPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                Log.i(TAG, "onPreviewFrame: setOneShotPreviewCallback");
                Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
                YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, previewSize.width, previewSize.height, null);
                ByteArrayOutputStream os = new ByteArrayOutputStream(data.length);
                if(!yuvImage.compressToJpeg(new Rect(0,0,previewSize.width,previewSize.height),100,os)){
                    Log.e(TAG, "onPreviewFrame: compressToJpeg error" );
                    return;
                }
                byte[] bytes = os.toByteArray();
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                //这里的处理方式是简单的把预览的一帧图保存下。如果需要做人脸设别或者其他操作，可以拿到这个bitmap进行分析处理
                //我们可以通过找出这张图片发现预览保存的图片的方向是不对的，还是Camera的原始方向，需要旋转一定角度才可以。
                //问题7：那么该如何处理呐？
                if(curCameraId == Camera.CameraInfo.CAMERA_FACING_BACK){
                    bitmap = BitmapUtils.rotate(bitmap,90);
                }else {
                    bitmap = BitmapUtils.mirror(BitmapUtils.rotate(bitmap,270));
                }
                FileUtils.saveBitmapToFile(bitmap,"oneShot.jpg");
            }
        });

//        mCamera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
//            @Override
//            public void onPreviewFrame(byte[] data, Camera camera) {
//                Log.i(TAG, "onPreviewFrame: setPreviewCallbackWithBuffer");
//            }
//        });
        mCamera.startPreview();

    }
}
