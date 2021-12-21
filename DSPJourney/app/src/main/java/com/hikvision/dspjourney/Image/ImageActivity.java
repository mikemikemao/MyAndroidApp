package com.hikvision.dspjourney.Image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.hikvision.dspjourney.R;

import java.io.File;

public class ImageActivity extends AppCompatActivity {
    private static final String TAG = "ImageActivity";
    private ImageView imageView;
    private SurfaceView surfaceView;
    private CustomView customView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_layout);

        imageView = findViewById(R.id.imageview);
        surfaceView = findViewById(R.id.surfaceview);
        customView = findViewById(R.id.customview);
        //bitmap
        String path = Environment.getExternalStorageDirectory().getPath() + File.separator + "picture"/* + File.separator + "kugou" */+ File.separator + "test.jpg";
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        if (bitmap!=null){
            imageView.setImageBitmap(bitmap);
        }
        //surfaceView
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                if(holder == null){
                    return;
                }
                Paint paint = new Paint();//The Paint class holds the style and color information about how to draw geometries, text and bitmaps.
                paint.setAntiAlias(true);//使得边缘平滑
                paint.setStyle(Paint.Style.STROKE);
                Canvas canvas = null;
                //To draw something, you need 4 basic components: A Bitmap to hold the pixels, a Canvas to host
                //the draw calls (writing into the bitmap), a drawing primitive (e.g. Rect,
                //Path, text, Bitmap), and a paint (to describe the colors and styles for the drawing).
                if (bitmap!=null){
                    try {
                        canvas = holder.lockCanvas();
                        canvas.drawBitmap(bitmap,0,0,paint);
                    }catch (Exception e){
                        e.printStackTrace();
                    } finally {
                        if(canvas!=null){
                            holder.unlockCanvasAndPost(canvas);
                        }

                    }
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

            }
        });

    }
}
