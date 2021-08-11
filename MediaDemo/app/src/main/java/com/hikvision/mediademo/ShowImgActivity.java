package com.hikvision.mediademo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.hikvision.mediademo.permission.CheckPermissionActivity;

import java.io.File;


public class ShowImgActivity extends Activity {

    private static final String TAG = "ShowImgActivity";
    Button btn_showImg;
    Button btn_showSVImg;
    Button btn_showVImg;
    ImageView iv;
    SurfaceViewTemplate surfaceViewTemplate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.showimglayout);

        btn_showImg=(Button)findViewById(R.id.btn_showImg);
        btn_showSVImg=(Button)findViewById(R.id.btn_showSVImg);
        btn_showVImg=(Button)findViewById(R.id.btn_show_VImg);

        btn_showImg.setOnClickListener(v -> {
            Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath() +
                    File.separator + "11.jpg");
            //Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.bg);
            iv=(ImageView)findViewById(R.id.imageView);
            iv.setImageBitmap(bitmap);
        });

        btn_showSVImg.setOnClickListener(v -> {
        });

        btn_showVImg.setOnClickListener(v -> {
        });
    }


}
