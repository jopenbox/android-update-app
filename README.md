# android-update-app

[![Download](https://api.bintray.com/packages/smartbetter/maven/update-app/images/download.svg)](https://bintray.com/smartbetter/maven/update-app/_latestVersion)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

This library is a update and install app for Android.

![](https://raw.githubusercontent.com/smartbetter/android-update-app/master/website/static/file_download.png)

The update progress is shown in the notification bar.

# Download

	compile 'net.smartbetter.android:update-app:1.0.5'

# Features

- Download Apk
- Install Apk

# Usage

## Activity

```java
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
protected void onDestroy() {
    super.onDestroy();
    unbindService(connection);
}
```

## Activity onCreate()

```java
Intent intent = new Intent(this, DownloadApkService.class);
startService(intent); //启动服务
bindService(intent, connection, BIND_AUTO_CREATE); //绑定服务
```

## Activity onClick()

```java
if (downloadBinder == null) {
    return;
}
/**
 * 需要动态授权:
 * Manifest.permission.READ_EXTERNAL_STORAGE //外置存储读权限
 * Manifest.permission.WRITE_EXTERNAL_STORAGE //外置存储写权限
 */
downloadBinder.startDownload(url); //Start download (开始下载)
downloadBinder.pauseDownload(); //Pause download (暂停下载)
downloadBinder.cancelDownload(); //Cancel the download (取消下载)
```

# Sample usage

A sample project which provides runnable code examples that demonstrate uses of the classes in this project is available in the sample-app/ folder.

# Preview

![](https://raw.githubusercontent.com/smartbetter/android-update-app/master/website/static/screenshot.jpg)
