package com.haifeng.robot.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by dell on 2017/8/7.
 */

public class ZigbeeService extends Service {
    private UsbManager myUsbManager;

    //这里定义一些必要的变量@Override
    public void onStart(Intent intent, int startId) {
        // TODO Auto-generated method stub
        super.onStart(intent, startId); // 每次startService（intent）时都回调该方法
        System.out.println("进入service的onStart函数");
        myUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE); // 获取UsbManager

        // 枚举设备
//        enumerateDevice(myUsbManager);
        // 查找设备接口
//        getDeviceInterface();
        // 获取设备endpoint
//        assignEndpoint(Interface2);
        // 打开conn连接通道
//        openDevice(Interface2);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
