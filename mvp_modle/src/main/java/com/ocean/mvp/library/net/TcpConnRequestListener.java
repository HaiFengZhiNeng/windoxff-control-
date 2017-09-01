package com.ocean.mvp.library.net;

import java.net.Socket;

/**
 * Tcp连接的请求监听
 * Created by zhangyuanyuan on 2017/7/3.
 */

public interface TcpConnRequestListener {
    void onSuccess(Socket mClient);
    void onFail(Exception e);
}
