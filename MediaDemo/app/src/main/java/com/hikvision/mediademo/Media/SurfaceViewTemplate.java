package com.hikvision.mediademo.Media;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.hardware.Camera;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.hikvision.mediademo.R;

import java.io.File;
import java.io.IOException;

public class SurfaceViewTemplate extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    Camera camera;
    private SurfaceHolder mHolder;
//    private boolean mIsDraw;
//    private Canvas mCanvas;
//    private int x;
//    private int y;
//    private Path mPath = new Path();
//    private Paint mPaint = new Paint();
    public SurfaceHolder getmHolder() {
        return mHolder;
    }



    public SurfaceViewTemplate(Context context) {
        super(context);
        initView();
    }
    public SurfaceViewTemplate(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public SurfaceViewTemplate(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
//        mHolder=holder;
//        Paint paint = new Paint();
//        paint.setAntiAlias(true);
//        paint.setStyle(Paint.Style.STROKE);
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg);
//        Canvas canvas = holder.lockCanvas(); // 先锁定当前surfaceView的画布
//        canvas.drawBitmap(bitmap, 0, 0, paint); //执行绘制操作
//        holder.unlockCanvasAndPost(canvas); // 解除锁定并显示在界面上

        // 打开摄像头并将展示方向旋转90度
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        camera.release();
    }

    @Override
    public void run() {

    }

   private void initView() {
//        mHolder = getHolder();
//        mHolder.addCallback(this);
//        setFocusable(true);
//        setFocusableInTouchMode(true);
//        setKeepScreenOn(true);
//        mPaint.setAntiAlias(true);
//        mPaint.setColor(Color.RED);
//        mPaint.setStyle(Paint.Style.STROKE);
//        mPaint.setStrokeWidth(50);
//        mPaint.setPathEffect(new DashPathEffect(new float[]{20, 50}, 0));
       camera = Camera.open();
       camera.setDisplayOrientation(90);
   }
}