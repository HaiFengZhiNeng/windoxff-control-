package com.ocean.speech.udp;

import com.ocean.mvp.library.net.UdpRegisterRequestListener;
import com.ocean.mvp.library.utils.L;
import com.ocean.speech.control.ControlPresenter;

import static android.content.ContentValues.TAG;

/**
 * UDP监听server
 * Created by zhangyuanyuan on 2017/7/10.
 */

public class UdpReceiver extends UdpRegisterRequestListener {

    ControlPresenter.OnListenerUDPServer onListenerUDPServer;

    public UdpReceiver(ControlPresenter.OnListenerUDPServer onListenerUDPServer) {
        this.onListenerUDPServer = onListenerUDPServer;
    }

    @Override
    protected void onFail(Exception e) {
        super.onFail(e);
        e.printStackTrace();
    }

    @Override
    protected void onReceive(String ip, int port, String result) {
        super.onReceive(ip, port, result);
        //host:192.168.0.158,port:8891
        if (!UdpControl.getInstance().isGetTcpIp) {
            if (result.contains(",")) {
                String[] split = result.split(",");
                String mUdpIP = split[0].substring(5, split[0].length());
                int mUdpPort = Integer.parseInt(split[1].substring(5, split[1].length()));
                L.e(TAG, "通过UDP获取到的ip--->" + mUdpIP + "   port-->" + mUdpPort);
                UdpControl.getInstance().setUdpIp(mUdpIP,mUdpPort);
//                isConnectLastIp = false;
                if (onListenerUDPServer != null)
                    onListenerUDPServer.acquireIp(true);
            }
        } else {
            if (onListenerUDPServer != null)
                onListenerUDPServer.receiver(result);
        }
    }

}
