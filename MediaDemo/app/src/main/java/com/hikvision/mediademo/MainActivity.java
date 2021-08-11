package com.hikvision.mediademo;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import com.hikvision.mediademo.permission.CheckPermissionActivity;



public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    Button btn_imgOperation;
    Button btn_audioOperation;
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
        btn_imgOperation=(Button)findViewById(R.id.btn_imgOpration);
        btn_audioOperation=(Button)findViewById(R.id.btn_audioOpration);
        btn_imgOperation.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this,ShowImgActivity.class);
            startActivity(intent);

        });
        btn_audioOperation.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this,AudioOperationActivity.class);
            startActivity(intent);

        });
    }

}