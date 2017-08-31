package com.haifeng.robot;

import android.app.Application;
import android.util.Log;

/**
 * Created by dell on 2017/7/26.
 */

public class HaifengApplication extends Application {

    public static final String TAG = "ECApplication";
    private static HaifengApplication instance;
    /**
     * 单例，返回一个实例
     * @return
     */
    public static HaifengApplication getInstance() {
        if (instance == null) {
            Log.w("","[ECApplication] instance is null.");
        }
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
