package com.ocean.speech;

import android.app.Application;
import android.content.Context;

import com.iflytek.cloud.Setting;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.ocean.mvp.library.net.NetClient;
import com.ocean.mvp.library.net.NetMessage;
import com.ocean.mvp.library.net.SendRequestListener;
import com.ocean.speech.dbhelper.DBHelper;
import com.youdao.sdk.app.YouDaoApplication;

/**
 * Created by zhangyuanyuan on 2017/7/5.
 */

public class SpeechApplication extends Application implements SendRequestListener {

    private NetClient client;
    private DBHelper dbHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        client = NetClient.getInstance(this);
        client.setSendRequestListener(this);

        /**
         * 讯飞初始化
         * 原始app_id = 595c594c
         */
        SpeechUtility.createUtility(getApplicationContext(), SpeechConstant.APPID +"=599d6109"+ "," + SpeechConstant.ENGINE_MODE + "=" + SpeechConstant.MODE_MSC);
        Setting.setShowLog(false);


        // 以下语句用于设置日志开关（默认开启），设置成false时关闭语音云SDK日志打印
        // Setting.setShowLog(false);
        YouDaoApplication.init(this, getResources().getString(R.string.app_youdao_id));//创建应用，每个应用都会有一个Appid，绑定对应的翻译服务实例，即可使用

    }


    /**
     * 获取的app的Application对象
     *
     * @param context 上下文
     * @return Application对象
     */
    public static SpeechApplication from(Context context) {
        return (SpeechApplication) context.getApplicationContext();
    }
    /**
     * 获取数据库操作类
     *
     * @return 数据库操作类
     */

    public synchronized DBHelper getDataBase() {
        if (dbHelper == null)
            dbHelper = new DBHelper(getApplicationContext());
        return dbHelper;
    }

    /**
     * 获取访问网络对象
     *
     * @return 访问网络对象
     */
    public NetClient getNetClient() {
        return client;
    }

    public void setNetClient() {
        client = NetClient.getInstance(getApplicationContext());
        client.setSendRequestListener(this);
    }

    @Override
    public void onSending(NetMessage message, long total, long current) {

    }

    @Override
    public void onSuccess(NetMessage message, String result) {

    }

    @Override
    public void onFail(NetMessage message, int errorCode, String errorMessage) {

    }
}
