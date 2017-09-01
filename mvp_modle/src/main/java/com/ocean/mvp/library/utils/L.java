package com.ocean.mvp.library.utils;


import android.util.Log;

/**
 * Created by wym on 2015/9/5.邮箱：wuyamin@ren001.com
 */
public class L {
    public static boolean isDebug = true;

    public static void i(String tag, String msg) {
        if (isDebug)
            Log.i(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (isDebug)
            Log.e(tag, msg);
    }

    public static void w(String tag, String msg) {

        Log.w(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (isDebug)
            Log.e(tag, msg);
    }

    public static void e1(String tag, String msg) {
        Log.e(tag, msg);
    }
}
