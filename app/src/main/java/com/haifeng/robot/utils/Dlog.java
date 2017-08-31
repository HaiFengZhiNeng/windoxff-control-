/*
 * Created by 岱青海蓝信息系统(北京)有限公司 on 17-2-10 上午9:41
 * Copyright (c) 2017. All rights reserved.
 */

package com.haifeng.robot.utils;

import android.util.Log;

/**
 * Created by mac on 16/11/23.
 * 打印 log 工具类
 */
public class Dlog {

    private static final String TAG = "test";
    /**
     * log所在文件名即log信息打印时的TAG，如***.java
     */
    private static String className;
    /**
     * log信息所在的 方法名
     */
    private static String methodName;
    /**
     * log打印信息的行号
     */
    private static int lineNumber;

    public static final int VERBOSE = 1;
    public static final int DEBUG = 2;
    public static final int INFO = 3;
    public static final int WARN = 4;
    public static final int ERROR = 5;
    public static final int NOTHING = 6;

    // 只有当LEVEL常量值小于等于对应日志级别才会打印日志,
    // 当项目正式上线的时候将 LEVEL 指定成 NOTHING
    public static final int LEVEL = VERBOSE;

    /**
     * 获取Log信息所在的文件名、方法名、以及行号，以便迅速定位
     *
     * @param sElements
     */
    private static void getMethodNames(StackTraceElement[] sElements) {
        className = sElements[1].getFileName();
        methodName = sElements[1].getMethodName();
        lineNumber = sElements[1].getLineNumber();
    }

    private static String createLog(String log) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[");
        buffer.append(methodName);
        buffer.append(":");
        buffer.append(lineNumber);
        buffer.append("]");
        buffer.append(log);
        return buffer.toString();
    }

    public static void v(String msg) {
        if (LEVEL <= VERBOSE) {
            Log.v(TAG, msg);
        }
    }

    public static void d(String msg) {
        if (LEVEL <= VERBOSE) {
            Log.d(TAG, msg);
        }
    }

    public static void i(String msg) {
        if (LEVEL <= INFO) {
            Log.e(TAG, msg);
        }
    }

    public static void w(String message) {
        if (LEVEL <= WARN) {
            getMethodNames(new Throwable().getStackTrace());
            Log.w(className, createLog(message));
        }
    }

    public static void e(String msg) {
        if (LEVEL <= ERROR) {
            getMethodNames(new Throwable().getStackTrace());
            Log.e(className, createLog(msg));
        }
    }
}
