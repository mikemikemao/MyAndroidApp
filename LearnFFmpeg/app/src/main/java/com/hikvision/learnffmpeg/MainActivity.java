package com.hikvision.learnffmpeg;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.byteflow.learnffmpeg.Adapter.MyRecyclerViewAdapter;
import com.byteflow.learnffmpeg.media.FFMediaPlayer;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    //运行时权限
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String[] REQUEST_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    private int mSampleSelectedIndex = -1;
    private static final int FF_ANATIVE_WINDOWS_EXAMPLE = 0;
    private static  final String [] EXAMPLE_LIST = {
            "FFmpeg + ANativeWindow player",
    };

    @Override
    protected void onResume() {
        super.onResume();
        //申请得到运行时权限
        if (!hasPermissionsGranted(REQUEST_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, REQUEST_PERMISSIONS, PERMISSION_REQUEST_CODE);
        }
    }
    protected boolean hasPermissionsGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (!hasPermissionsGranted(REQUEST_PERMISSIONS)) {
                Toast.makeText(this, "We need the permission: WRITE_EXTERNAL_STORAGE", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取FFmpeg的版本信息
        ((TextView)findViewById(R.id.text_view)).setText("FFmpeg 版本和编译配置信息\n\n" + FFMediaPlayer.GetFFmpegVersion());
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        /**
         * 此方法用于初始化菜单，其中menu参数就是即将要显示的Menu实例。 返回true则显示该menu,false 则不显示;
         * (只会在第一次初始化菜单时调用) Inflate the menu; this adds items to the action bar
         * if it is present.
         */
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        /**
         * 菜单项被点击时调用，也就是菜单项的监听方法。
         * 通过这几个方法，可以得知，对于Activity，同一时间只能显示和监听一个Menu 对象。 TODO Auto-generated
         * method stub
         */
         int id = item.getItemId();
        if (id == R.id.action_change_sample) {
            showSelectExampleDialog();
        }
        return true;
    }

    private void showSelectExampleDialog() {
        //创建一个对话框
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();
        //创建一个sample_selected_layout 的inflater
        LayoutInflater inflater = LayoutInflater.from(this);
        final View rootView = inflater.inflate(R.layout.sample_selected_layout, null);
        //配置recycleview 和对应adapter 绑定
        final RecyclerView resolutionsListView = rootView.findViewById(R.id.resolution_list_view);
        final MyRecyclerViewAdapter myPreviewSizeViewAdapter = new MyRecyclerViewAdapter(this, Arrays.asList(EXAMPLE_LIST));
        resolutionsListView.setAdapter(myPreviewSizeViewAdapter);
        //标签在最上面
        myPreviewSizeViewAdapter.setSelectIndex(mSampleSelectedIndex);
        resolutionsListView.scrollToPosition(mSampleSelectedIndex);
        //设置为竖排列
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        resolutionsListView.setLayoutManager(manager);
        //recycleview 与dialog 绑定
        dialog.show();
        dialog.getWindow().setContentView(rootView);

        myPreviewSizeViewAdapter.addOnItemClickListener(new MyRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                int selectIndex = myPreviewSizeViewAdapter.getSelectIndex();
                myPreviewSizeViewAdapter.setSelectIndex(position);
                myPreviewSizeViewAdapter.safeNotifyItemChanged(selectIndex);
                myPreviewSizeViewAdapter.safeNotifyItemChanged(position);
                mSampleSelectedIndex = position;
                switch (position) {
                    case FF_ANATIVE_WINDOWS_EXAMPLE:
                        startActivity(new Intent(MainActivity.this, NativeMediaPlayerActivity.class));
                        break;
                    default:
                        break;
                }
                dialog.cancel();
            }
        });

        Button confirmBtn = rootView.findViewById(R.id.confirm_btn);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });








    }
}