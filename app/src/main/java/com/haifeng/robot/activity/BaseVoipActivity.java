package com.haifeng.robot.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.haifeng.robot.config.Constant;
import com.haifeng.robot.view.ECCallControlUILayout;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECVoIPCallManager;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by dell on 2017/7/27.
 */

public class BaseVoipActivity extends Activity implements ECCallControlUILayout.OnCallControlDelegate {
    private boolean quit = false; //设置退出标识
    /**
     * 是否来电
     */
    protected boolean mIncomingCall = false;
    /**
     * 呼叫唯一标识号
     */
    protected String mCallId;
    /**
     * VoIP呼叫类型（音视频）
     */
    protected ECVoIPCallManager.CallType mCallType;
    /**
     * 通话昵称
     */
    protected String mCallName;
    /**
     * 通话号码
     */
    protected String mCallNumber;
    protected String mPhoneNumber;
    public AudioManager mAudioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAudioManager = ((AudioManager) getSystemService(Context.AUDIO_SERVICE));
        if (initview()) {
            return;
        }
        if (mCallType == null) {
            mCallType = ECVoIPCallManager.CallType.VOICE;
        }
    }


    public boolean initview() {
        mIncomingCall = !(getIntent().getBooleanExtra(Constant.EXTRA_OUTGOING_CALL, false));
        mCallType = (ECVoIPCallManager.CallType) getIntent().getSerializableExtra(ECDevice.CALLTYPE);
        return false;
    }

    /**
     * 收到的VoIP通话事件通知是否与当前通话界面相符
     *
     * @return 是否正在进行的VoIP通话
     */
    protected boolean isEqualsCall(String callId) {
        return (!TextUtils.isEmpty(callId) && callId.equals(mCallId));
    }


    @Override
    public void onViewAccept(ECCallControlUILayout controlPanelView, ImageButton view) {
        if (controlPanelView != null) {///
            controlPanelView.setControlEnable(false);
        }

    }

    @Override
    public void onViewRelease(ECCallControlUILayout controlPanelView, ImageButton view) {
        if (controlPanelView != null) {
            controlPanelView.setControlEnable(false);
        }
    }

    @Override
    public void setDialerpadUI() {

    }

    @Override
    public void onViewReject(ECCallControlUILayout controlPanelView, ImageButton view) {
        if (controlPanelView != null) {
            controlPanelView.setControlEnable(false);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
//        ECHandlerHelper.removeCallbacksRunnOnUI(OnCallFinish);
        setIntent(intent);
//        setIntent(sIntent);
        if (initview()) {
            return;
        }

        if (mCallType == null) {
            mCallType = ECVoIPCallManager.CallType.VOICE;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 获取音频类型
        int streamType = ECDevice.getECVoIPSetupManager().getStreamType();
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            // 调小音量
            adjustStreamVolumeDown(streamType);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            // 调大音量
            adjustStreamVolumeUo(streamType);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (!quit) { //询问退出程序
            new Timer(true).schedule(new TimerTask() { //启动定时任务
                @Override
                public void run() {
                    quit = false; //重置退出标识
                }
            }, 2000);
            quit = true;
        } else { //确认退出程序
            super.onBackPressed();
            finish();
        }
    }

    /**
     * 向下 调整音量
     *
     * @param streamType 类型
     */

    public final void adjustStreamVolumeDown(int streamType) {
        if (this.mAudioManager != null)
            this.mAudioManager.adjustStreamVolume(streamType, AudioManager.ADJUST_LOWER,
                    AudioManager.FX_FOCUS_NAVIGATION_UP);
    }


    /**
     * 向上 调整音量
     *
     * @param streamType 类型
     */
    public final void adjustStreamVolumeUo(int streamType) {
        if (this.mAudioManager != null)
            this.mAudioManager.adjustStreamVolume(streamType, AudioManager.ADJUST_RAISE,
                    AudioManager.FX_FOCUS_NAVIGATION_UP);
    }

    /**
     * 点击其他地方时，将软键盘隐藏
     *
     * @param ev
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

}
