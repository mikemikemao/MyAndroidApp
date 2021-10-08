package com.hikvision.mediademo;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.hikvision.mediademo.Audio.AudioOperationActivity;
import com.hikvision.mediademo.Media.ShowImgActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    Button btn_imgOperation;
    Button btn_audioOperation;

    private static final String ADBPath="/sys/devices/platform/fe8a0000.usb2-phy/otg_mode";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        setContentView(R.layout.activity_main);

        btn_imgOperation=(Button)findViewById(R.id.btn_imgOpration);
        btn_audioOperation=(Button)findViewById(R.id.btn_audioOpration);
        btn_imgOperation.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ShowImgActivity.class);
            startActivity(intent);

        });
        btn_audioOperation.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intent);

        });
    }

    public void writeFileData(String filename, String content){
        try {
            FileOutputStream fos = this.openFileOutput(filename, MODE_PRIVATE);//获得FileOutputStream
            //将要写入的字符串转换为byte数组
            byte[]  bytes = content.getBytes();
            fos.write(bytes);//将byte数组写入文件
            fos.close();//关闭文件输出流

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 写文件
    private int fileWrite(String fileName, char[] buffer) {
        int i = -1;
        try {
            File file = new File(fileName);
            if(!file.exists()){
                if(!file.getParentFile().exists()){
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(fileName);
            fileWriter.write(buffer);
            fileWriter.close();
            i = 0;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return i;
    }

}