package com.ocean.mvp.library.net;

import java.net.Socket;

/**
 * Created by zhangyuanyuan on 2017/7/3.
 */

public class TcpRegisterRequestListener {
    protected void onFail(Exception e){};
    protected void onReceive(String result){};
}
