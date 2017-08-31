package com.haifeng.robot.config;

/**
 * Created by dell on 2017/7/27.
 */

public class Constant {
    /**
     * 昵称
     */
    public static final String EXTRA_CALL_NAME = "com.haifeng.robot.VoIP_CALL_NAME";
    /**
     * 通话号码
     */
    public static final String EXTRA_CALL_NUMBER = "com.haifeng.robot.VoIP_CALL_NUMBER";
    /**
     * 呼入方或者呼出方
     */
    public static final String EXTRA_OUTGOING_CALL = "com.haifeng.robot.VoIP_OUTGOING_CALL";
    /**
     * VoIP呼叫
     */
    public static final String ACTION_VOICE_CALL = "com.haifeng.robot.intent.ACTION_VOICE_CALL";
    /**
     * Video呼叫
     */
    public static final String ACTION_VIDEO_CALL = "com.haifeng.robot.intent.ACTION_VIDEO_CALL";
    public static final String ACTION_CALLBACK_CALL = "com.haifeng.robot.intent.ACTION_VIDEO_CALLBACK";
    /**
     * 是否正在呼叫
     */
    public static final String ACTION_CALLBACKING = "com.haifeng.robot.intent.ACTION_VIDEO_CALLING";
    /**
     * 联系人账号
     */
    public final static String RECIPIENTS = "recipients";
    /**
     * 联系人名称
     */
    public final static String CONTACT_USER = "contact_user";
    public final static String CUSTOMER_SERVICE = "is_customer_service";


    public static final int USB_TIMEOUT_IN_MS = 100;
    public static final int BUFFER_SIZE_IN_BYTES = 256;

}
