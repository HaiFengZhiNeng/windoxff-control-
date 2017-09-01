package com.ocean.mvp.library.progress;

/**
 * 进度回调接口，比如用于文件上传与下载
 * Created by zhangyuanyuan on 2017/7/3.
 */

public interface ProgressListener {
    void onProgress(long currentBytes, long contentLength, boolean done);
}
