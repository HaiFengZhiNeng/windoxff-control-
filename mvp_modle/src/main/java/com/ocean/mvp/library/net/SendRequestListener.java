package com.ocean.mvp.library.net;

import com.ocean.mvp.library.net.NetMessage;

/**
 * Tcp发送文本文件监听
 * Created by zhangyuanyuan on 2017/7/3.
 */

public interface SendRequestListener<T extends NetMessage>  {
    void onSending(T message, long total, long current);
    void onSuccess(T message, String result);
    void onFail(T message, int errorCode, String errorMessage);
}
