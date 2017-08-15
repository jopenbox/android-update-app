package com.example.updateapp;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.updateapp.utils.PermissionUtils;

import net.smartbetter.android.updateapp.DownloadApkService;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private DownloadApkService.DownloadBinder downloadBinder;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder = (DownloadApkService.DownloadBinder) service;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button startDownload = (Button) findViewById(R.id.start_download);
        Button pauseDownload = (Button) findViewById(R.id.pause_download);
        Button cancelDownload = (Button) findViewById(R.id.cancel_download);
        startDownload.setOnClickListener(this);
        pauseDownload.setOnClickListener(this);
        cancelDownload.setOnClickListener(this);

        Intent intent = new Intent(this, DownloadApkService.class);
        startService(intent); // 启动服务
        bindService(intent, connection, BIND_AUTO_CREATE); // 绑定服务
    }

    @Override
    public void onClick(final View v) {
        if (downloadBinder == null) {
            return;
        }
        PermissionUtils.getInstance().requestPermissions(this,101,
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE, //外置存储读权限
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, //外置存储写权限
                },
                new PermissionUtils.OnPermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        switch (v.getId()) {
                            // 开始下载
                            case R.id.start_download:
                                String url = "https://qd.myapp.com/myapp/qqteam/AndroidQQ/mobileqq_android.apk";
                                downloadBinder.startDownload(url);
                                break;
                            // 暂停下载
                            case R.id.pause_download:
                                downloadBinder.pauseDownload();
                                break;
                            // 取消下载
                            case R.id.cancel_download:
                                downloadBinder.cancelDownload();
                                break;
                            default:
                                break;
                        }
                    }
                    @Override
                    public void onPermissionDenied(String[] deniedPermissions) {
                    }
                });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PermissionUtils.getInstance().onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

}