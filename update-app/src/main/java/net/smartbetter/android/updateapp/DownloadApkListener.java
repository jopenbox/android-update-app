package net.smartbetter.android.updateapp;

/**
 * 定义下载接口
 */
public interface DownloadApkListener {
    //下载进度
    void onProgress(int progress);
    //下载成功
    void onSuccess();
    //下载失败
    void onFailed();
    //暂停下载
    void onPaused();
    //取消下载
    void onCanceled();
}