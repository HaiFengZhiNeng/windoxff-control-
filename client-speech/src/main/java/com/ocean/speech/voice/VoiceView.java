package com.ocean.speech.voice;

import android.content.Context;
import android.os.SystemClock;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ocean.speech.R;
import com.ocean.speech.base.ControlBaseActivity;

/**
 * Created by dell on 2017/8/1.
 */

public class VoiceView extends ControlBaseActivity<VoicePresenter> implements IVoiceView, View.OnClickListener {
    @Override
    public VoicePresenter createPresenter() {
        return new VoicePresenter(this);
    }

    @Override
    protected int getContentViewResource() {
        return R.layout.activity_voice;
    }

    @Override
    public Context getContext() {
        return this;
    }

    private TextView mCalling;
    private Chronometer mChronometer;
    //挂断
    private LinearLayout mCallingBottomReleaseLayout;
    private ImageView mCallingRelease;
    //来电
    private LinearLayout mCallBottomLayout;
    private ImageView mCallCancele;//取消
    private ImageView mCallAccept;//接听
    //静音 扬声器
    private ImageView mLayoutCallMute;
    private ImageView mLayoutCallSpeaker;

    @Override
    protected void onViewInit() {
        super.onViewInit();
        (mCalling) = (TextView) findViewById(R.id.tv_calling);
        (mChronometer) = (Chronometer) findViewById(R.id.chronometer);
        (mCallingBottomReleaseLayout) = (LinearLayout) findViewById(R.id.calling_bottom_release);
        (mCallingRelease) = (ImageView) findViewById(R.id.layout_call_release);
        (mCallBottomLayout) = (LinearLayout) findViewById(R.id.layout_call_bottom);
        (mCallCancele) = (ImageView) findViewById(R.id.layout_call_cancel);
        (mCallAccept) = (ImageView) findViewById(R.id.layout_call_accept);
        (mLayoutCallMute) = (ImageView) findViewById(R.id.layout_call_mute);
        (mLayoutCallSpeaker) = (ImageView) findViewById(R.id.layout_call_speaker);
        mChronometer.setVisibility(View.GONE);
        mChronometer.stop();
    }

    @Override
    protected void setOnListener() {
        super.setOnListener();
        mCallingRelease.setOnClickListener(this);
        mCallCancele.setOnClickListener(this);
        mCallAccept.setOnClickListener(this);
        mLayoutCallMute.setOnClickListener(this);
        mLayoutCallSpeaker.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layout_call_release:
                mPresenter.onHangUp();
                break;
            case R.id.layout_call_accept:
                mPresenter.onAccept();
                break;
            case R.id.layout_call_cancel:
                mPresenter.onRefuse();
                break;
            case R.id.layout_call_mute:
                mPresenter.onMute();
                break;
            case R.id.layout_call_speaker:
                mPresenter.onSpeaker();
                break;
        }
    }

    @Override
    public void setLayoutVisible(boolean visible) {
        mCallingBottomReleaseLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
        mCallBottomLayout.setVisibility(visible ? View.GONE : View.VISIBLE);
    }

    @Override
    public void setVoiceText(String text) {
        mCalling.setText(text);
    }

    @Override
    public void startUpChronometer(boolean falg) {
        if (falg) {
            mChronometer.start();
        } else {
            mChronometer.stop();
        }
    }

    @Override
    public void setChronometerVisible(boolean visible) {
        mChronometer.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setChronometerTime() {
        mChronometer.setBase(SystemClock.elapsedRealtime());
    }

    @Override
    public void setSpeakerVisiable(boolean visiable) {
        if (!visiable) {
            mLayoutCallSpeaker.setImageResource(R.mipmap.ic_speaker);
        } else {
            mLayoutCallSpeaker.setImageResource(R.mipmap.ic_speaker_pressed);
        }
    }

    @Override
    public void setMuteVisiable(boolean visiable) {
        if (!visiable) {
            mLayoutCallMute.setImageResource(R.mipmap.ic_mute);
        } else {
            mLayoutCallMute.setImageResource(R.mipmap.ic_mute_pressed);
        }
    }
}
