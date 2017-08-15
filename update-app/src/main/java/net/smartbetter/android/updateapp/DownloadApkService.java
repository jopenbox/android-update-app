package net.smartbetter.android.updateapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.FileProvider;
import android.support.v7.app.NotificationCompat;

import java.io.File;

/**
 * 下载服务
 *
 * 需要动态授权
 * Manifest.permission.READ_EXTERNAL_STORAGE //外置存储读权限
 * Manifest.permission.WRITE_EXTERNAL_STORAGE //外置存储写权限
 */
public class DownloadApkService extends Service {

    private DownloadApkTask downloadApkTask;
    private String downloadUrl;

    private DownloadApkListener listener = new DownloadApkListener() {
        @Override
        public void onProgress(int progress) {
            getNotificationManager().notify(1, getNotification(
                    getResources().getString(R.string.download_apk_downloading), progress));
        }

        @Override
        public void onSuccess() {
            downloadApkTask = null;
            //下载成功时将前台服务通知关闭，并创建一个下载成功的通知
            stopForeground(true);
            getNotificationManager().notify(1, getNotification(
                    getResources().getString(R.string.download_apk_download_success), -1));
            installApk();

        }

        @Override
        public void onFailed() {
            downloadApkTask = null;
            //下载失败时将前台服务通知关闭，并创建一个下载失败的通知
            stopForeground(true);
            getNotificationManager().notify(1, getNotification(
                    getResources().getString(R.string.download_apk_download_failed), -1));
        }

        @Override
        public void onPaused() {
            downloadApkTask = null;
        }

        @Override
        public void onCanceled() {
            downloadApkTask = null;
            stopForeground(true);
        }
    };

    private DownloadBinder mBinder = new DownloadBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class DownloadBinder extends Binder {

        public void startDownload(String url) {
            if (downloadApkTask == null) {
                downloadUrl = url;
                downloadApkTask = new DownloadApkTask(listener);
                downloadApkTask.execute(downloadUrl);
                startForeground(1, getNotification(
                        getResources().getString(R.string.download_apk_downloading), 0));
            }
        }

        public void pauseDownload() {
            if (downloadApkTask != null) {
                downloadApkTask.pauseDownload();
            }
        }

        public void cancelDownload() {
            if (downloadApkTask != null) {
                downloadApkTask.cancelDownload();
            } else {
                if (downloadUrl != null) {
                    //取消下载时需将文件删除，并将通知关闭
                    String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                    String directory = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS).getPath();
                    File file = new File(directory + fileName);
                    if (file.exists()) {
                        file.delete();
                    }
                    getNotificationManager().cancel(1);
                    stopForeground(true);
                }
            }
        }

    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private Notification getNotification(String title, int progress) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_file_download_black_24dp);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_file_download_black_24dp));
        builder.setContentTitle(title);
        if (progress >= 0) {
            //当progress大于或等于0时才需显示下载进度
            builder.setContentText(progress + "%");
            builder.setProgress(100, progress, false);
        }
        return builder.build();
    }

    /**
     * 安装apk
     */
    private void installApk() {
        String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
        String directory = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS).getPath();
        //获取当前sdcard存储路径
        File apkfile = new File(directory + fileName);
        if (!apkfile.exists()) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        //判断是否是AndroidN以及更高的版本
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.N) {
            Uri contentUri = FileProvider.getUriForFile(getApplicationContext(),
                    "net.smartbetter.android.updateapp.fileProvider",apkfile);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(contentUri,"application/vnd.android.package-archive");
        }else{
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //安装，如果签名不一致，可能出现程序未安装提示
            intent.setDataAndType(Uri.fromFile(apkfile),
                    "application/vnd.android.package-archive");
        }
        getApplicationContext().startActivity(intent);
    }
}