package com.ocean.mvp.library.net;

/**
 * Created by zhangyuanyuan on 2017/7/4.
 */

public interface SocketRequestListener {
    void onFail(Exception e);

    void onSuccess(String result);
}
