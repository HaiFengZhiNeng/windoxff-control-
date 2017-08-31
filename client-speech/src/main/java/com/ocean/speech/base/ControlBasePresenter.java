package com.ocean.speech.base;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.ocean.mvp.library.presenter.BasePresenter;
import com.ocean.mvp.library.view.UiView;
import com.ocean.speech.config.Constant;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECVoIPCallManager;

/**
 * Created by dell on 2017/8/1.
 */

public class ControlBasePresenter<T extends UiView> extends BasePresenter<T> {
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

    public ControlBasePresenter(T mView) {
        super(mView);
    }

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        mAudioManager = ((AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE));
        if (initview()) {
            return;
        }
        if (mCallType == null) {
            mCallType = ECVoIPCallManager.CallType.VOICE;
        }
    }

    protected void exit() {
        if (mView != null)
            ((Activity) mView).finish();
    }


    public boolean initview() {
        mIncomingCall = !((Activity) mView).getIntent().getBooleanExtra(Constant.ACTION_CALLBACKING, false);
        mCallType = (ECVoIPCallManager.CallType) ((Activity) mView).getIntent().getSerializableExtra(ECDevice.CALLTYPE);
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

    protected void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    }
}
