package com.hikvision.dspjourney;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.hikvision.dspjourney.Image.ImageActivity;
import com.hikvision.dspjourney.camera.CameraActivity;
import com.hikvision.dspjourney.opengl.GlSurfaceActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.tv_image).setOnClickListener(this);
        findViewById(R.id.tv_camera).setOnClickListener(this);
        findViewById(R.id.tv_glsurfaceview).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Class<?> targetClass = null;
        switch (v.getId()) {
            case R.id.tv_image:
                targetClass = ImageActivity.class;
                break;
            case R.id.tv_camera:
                targetClass = CameraActivity.class;
            case R.id.tv_glsurfaceview:
                targetClass = GlSurfaceActivity.class;
            default:
                break;
        }
        if (targetClass != null) {
            Intent intent = new Intent(MainActivity.this, targetClass);
            startActivity(intent);
        }
    }


}