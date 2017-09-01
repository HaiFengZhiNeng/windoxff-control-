package com.ocean.mvp.library.download;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Request;

/**
 * Created by zhangyuanyuan on 2017/7/3.
 */

public interface IGetFileStreamListener {
    void onFailure(Request request, IOException e);
    void onSuccess(InputStream inputStream);
}
