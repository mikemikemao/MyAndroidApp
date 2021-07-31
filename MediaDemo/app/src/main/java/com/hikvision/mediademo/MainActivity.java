package com.hikvision.mediademo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;


import com.hikvision.mediademo.permission.CheckPermissionActivity;

import java.io.File;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "pirate";
    ImageView iv;

    Button btn_showImg;
    Button btn_showSVImg;
    Button btn_showVImg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: "+i.getAction());
        if (CheckPermissionActivity.jump2PermissionActivity(this, i != null ? i.getAction() : null)) {
            finish();
            return;
        }

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
            Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath() +
                    File.separator + "11.jpg");
            //Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.bg);
            iv=(ImageView)findViewById(R.id.imageView);
            iv.setImageBitmap(bitmap);
        });

    }



}