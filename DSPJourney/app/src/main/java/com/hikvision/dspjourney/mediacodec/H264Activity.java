package com.hikvision.dspjourney.mediacodec;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;

import com.hikvision.dspjourney.R;

import java.io.IOException;


public class H264Activity extends Activity  implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private static String TAG = "H264Activity";
    SurfaceView m_SurfaceView;
    SurfaceHolder m_SurfaceHolder;
    Camera camera;

    int width = 1280;
    int height = 720;
    int framerate = 30;
    H264Encoder encoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_h264);
        m_SurfaceView = (SurfaceView) findViewById(R.id.surface_view);
        m_SurfaceHolder = m_SurfaceView.getHolder();
        m_SurfaceHolder.addCallback(this);

        if (supportH264Codec()) {
            Log.e(TAG, "support H264 hard codec");
        } else {
            Log.e(TAG, "not support H264 hard codec");
        }
    }

    private boolean supportH264Codec() {
        // 遍历支持的编码格式信息
        if (Build.VERSION.SDK_INT >= 18) {
            for (int j = MediaCodecList.getCodecCount() - 1; j >= 0; j--) {
                MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(j);

                String[] types = codecInfo.getSupportedTypes();
                for (int i = 0; i < types.length; i++) {
                    if (types[i].equalsIgnoreCase("video/avc")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        Log.w("MainActivity", "enter surfaceCreated method");
        // 目前设定的是，当surface创建后，就打开摄像头开始预览
        camera = Camera.open(0);
        //camera.setDisplayOrientation(90);
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewFormat(ImageFormat.NV21);
        parameters.setPreviewSize(1280, 720);

        try {
            camera.setParameters(parameters);
            camera.setPreviewDisplay(m_SurfaceHolder);
            camera.setPreviewCallback(this);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

        encoder = new H264Encoder(width, height, framerate);
        encoder.startEncoder();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        Log.w(TAG, "enter surfaceChanged method");
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        Log.w(TAG, "enter surfaceDestroyed method");
        // 停止预览并释放资源
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera = null;
        }

        if (encoder != null) {
            encoder.stopEncoder();
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (encoder != null) {
            encoder.putData(data);
        }
    }
}
