package com.ocean.mvp.library.download;

import java.io.File;

import okhttp3.Response;

/**
 * 下载的业务
 * Created by zhangyuanyuan on 2017/7/3.
 */

public interface DownLoaderInterface {
    public void downloadPoint(File file, Response response, long startPoint);//下载的起始位置
}
