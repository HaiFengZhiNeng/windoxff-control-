package com.ocean.mvp.library.progress;

import okhttp3.Request;

/**
 * Created by zhangyuanyuan on 2017/7/3.
 */

public abstract class DownLoadProgressListener extends UIProgressListener {
    public abstract void onSuccess(String savePath);

    public abstract void onFailure(Exception e, Request request);

}

