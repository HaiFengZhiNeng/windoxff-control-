package com.ocean.mvp.library.net;

import java.io.IOException;

import okhttp3.Request;

/**
 * 网络请求的基本回调
 * Created by zhangyuanyuan on 2017/7/3.
 */

public interface RequestListener {
    void onFailure(Request request, IOException e);
    void onSuccess(String result);
}
