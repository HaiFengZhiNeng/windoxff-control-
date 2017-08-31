package com.ocean.speech.udp;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.ocean.mvp.library.net.NetClient;
import com.ocean.mvp.library.utils.PreferencesUtils;
import com.ocean.speech.SpeechApplication;

/**
 * UDP控制类
 * Created by zhangyuanyuan on 2017/7/10.
 */

public class UdpControl {

    private static UdpControl mUdpControl;

    private NetClient client;

    public String mUdpIP = "";
    private int mUdpPort = 8889;

    private Context mContext;
    /**
     * 标记是否成功获取ip
     */
    public boolean isGetTcpIp = false;

    public static synchronized UdpControl getInstance() {
        if (mUdpControl == null) {
            synchronized (UdpControl.class) {
                if (mUdpControl == null) {
                    mUdpControl = new UdpControl();
                }
            }
        }
        return mUdpControl;
    }

    void setUdpIp(String mUdpIP,int mUdpPort) {
        this.mUdpIP = mUdpIP;
        this.mUdpPort = mUdpPort;
        isGetTcpIp = true;
        PreferencesUtils.putString(mContext, "ocean_ip", mUdpIP);
        mHandler.removeMessages(1);

    }

    /**
     * 发送Udp广播 为获取Udp server的Ip
     */
    public void sendUdpSocketToByIp(SpeechApplication application, NetClient client) {
        this.mContext = application;
        this.client = client;
        if (mHandler == null)
            initHandler();
        isGetTcpIp = false;
        mUdpIP = "";
        mHandler.sendEmptyMessageDelayed(1, 1000);
        client.sendTextMessageByUdp("255.255.255.255", mUdpPort,"Please get me your IP.");
    }

    public void sendUdpByteMessage(byte[] value, SpeechApplication application, NetClient client) {
        this.mContext = application;

        if (client == null)
            client = application.getNetClient();

        if (mHandler == null)
            initHandler();

        if (!isGetTcpIp) {
            mHandler.sendEmptyMessage(2);
            return;
        }

        client.sendByteMessageByUdp(mUdpIP, mUdpPort, value);
    }


    private Handler mHandler = null;

    private void initHandler() {
        mHandler = new Handler(SpeechApplication.from(mContext).getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:
                        if (TextUtils.isEmpty(mUdpIP)) {
                            if (client == null)
                                client = SpeechApplication.from(mContext).getNetClient();
                            client.sendTextMessageByUdp(TextUtils.isEmpty(mUdpIP) ? "255.255.255.255" : mUdpIP, mUdpPort, "Please get me your IP.");
                            mHandler.sendEmptyMessageDelayed(1, 1000);
                        }
                        break;
                    case 2:
                        Toast.makeText(mContext, "请等待连接机器人", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };
    }


}
