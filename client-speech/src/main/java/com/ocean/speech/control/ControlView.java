package com.ocean.speech.control;

import android.content.Context;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ocean.mvp.library.utils.L;
import com.ocean.speech.R;
import com.ocean.speech.base.ControlBaseActivity;
import com.ocean.speech.custom.AnimationView;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECVoIPCallManager;
import com.yuntongxun.ecsdk.ECVoIPSetupManager;
import com.yuntongxun.ecsdk.voip.video.ECCaptureView;
import com.yuntongxun.ecsdk.voip.video.ECOpenGlView;
import com.yuntongxun.ecsdk.voip.video.OnCameraInitListener;

import static com.ocean.speech.R.id.local_video_view;
import static com.ocean.speech.R.id.video_btn_cancel;

/**
 * Created by zhangyuanyuan on 2017/7/8.
 */

public class ControlView extends ControlBaseActivity<ControlPresenter> implements IControlView, View.OnClickListener {
    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public ControlPresenter createPresenter() {
        return new ControlPresenter(this);
    }

    @Override
    protected int getContentViewResource() {
        return R.layout.activity_control;
    }

    private Button mCancelVideo, mAcceptVideo, mReleaseVideo;
    private EditText mEditText;

    private TextView mConnetState, mSendEdit;
    private ImageView mChangeTxtOrVoice, mControlSetting, mSport, mVoice, mSetting, mLike, mLink, mRrelink, mOpenSurface, mLocalView,
            mHideInput, mRepeat, mVoicePeople;
    private Button mAsrBtn, mAsrSendBtn;
    private LinearLayout mEditLayout, mAsrResultLayout, mReleaseLayout, mAcceptLayout;
    private RelativeLayout mControlRelative, mMessage, mEdit;

    private TextView mQuestion, mAnswer, mTip;

    private NavController navController;

    private LinearLayout mAnimationLayout;

    private AnimationView mVoiceView;

    private FrameLayout mGlView_frameLayout;

    /**
     * 当前呼叫类型对应的布局
     */
    FrameLayout mCallRoot;

    private ECOpenGlView mRemoteVideoView, mLocalVideoView;

    private ECCaptureView mCaptureView;
    //计时器
    private Chronometer mChronometer;


    @Override
    protected void onViewInit() {
        super.onViewInit();
        mSendEdit = (TextView) findViewById(R.id.send_text);
        mEditText = (EditText) findViewById(R.id.edit_text);
        navController = (NavController) findViewById(R.id.nav_control);
        mConnetState = (TextView) findViewById(R.id.connect_state);
        mChangeTxtOrVoice = (ImageView) findViewById(R.id.change_text_voice);
        mAsrBtn = (Button) findViewById(R.id.voice_btn);
        mEditLayout = (LinearLayout) findViewById(R.id.edit_text_layout);

        mAsrResultLayout = (LinearLayout) findViewById(R.id.asr_result_layout);
        mAsrSendBtn = (Button) findViewById(R.id.asr_send);
        mQuestion = (TextView) findViewById(R.id.asr_question);
        mAnswer = (TextView) findViewById(R.id.asr_answer);

        mRrelink = (ImageView) findViewById(R.id.relink_imageView);
        mAnimationLayout = (LinearLayout) findViewById(R.id.voice_animation_bg);
        mVoiceView = (AnimationView) findViewById(R.id.voice_animation);
        (mLocalView) = (ImageView) findViewById(R.id.localView_imageView);
        (mLocalVideoView) = (ECOpenGlView) findViewById(local_video_view);

        (mCallRoot) = (FrameLayout) findViewById(R.id.callRoot_fm);
        (mCancelVideo) = (Button) findViewById(video_btn_cancel);
        (mAcceptVideo) = (Button) findViewById(R.id.video_btn_accept);
        (mReleaseVideo) = (Button) findViewById(R.id.video_btn_relese);

        (mGlView_frameLayout) = (FrameLayout) findViewById(R.id.glView_frameLayout);
        (mReleaseLayout) = (LinearLayout) findViewById(R.id.ll_release);
        (mAcceptLayout) = (LinearLayout) findViewById(R.id.ll_begin);
        (mTip) = (TextView) findViewById(R.id.tv_tip);
        (mChronometer) = (Chronometer) findViewById(R.id.chronometer);
        mChronometer.stop();
        mChronometer.setVisibility(View.GONE);

        (mRemoteVideoView) = (ECOpenGlView) findViewById(R.id.remote_video_view);
        mRemoteVideoView.setGlType(ECOpenGlView.RenderType.RENDER_REMOTE);
        mRemoteVideoView.setAspectMode(ECOpenGlView.AspectMode.CROP);
        (mOpenSurface) = (ImageView) findViewById(R.id.openSurface_imageView);

        (mLocalVideoView) = (ECOpenGlView) findViewById(local_video_view);
        mLocalVideoView.setGlType(ECOpenGlView.RenderType.RENDER_PREVIEW);
        mLocalVideoView.setAspectMode(ECOpenGlView.AspectMode.CROP);

        (mLike) = (ImageView) findViewById(R.id.like_imageView);
        (mControlSetting) = (ImageView) findViewById(R.id.controlSetting_imageView);
        (mSport) = (ImageView) findViewById(R.id.sport_imageView);
        (mVoice) = (ImageView) findViewById(R.id.voice_imageView);
        (mSetting) = (ImageView) findViewById(R.id.setting_imageView);
        mControlRelative = (RelativeLayout) findViewById(R.id.control_relative);

        (mLink) = (ImageView) findViewById(R.id.link_imageView);

        (mMessage) = (RelativeLayout) findViewById(R.id.message_relative);
        (mEdit) = (RelativeLayout) findViewById(R.id.edit_layout);
        (mHideInput) = (ImageView) findViewById(R.id.hideInput_iv);
        (mRepeat) = (ImageView) findViewById(R.id.repeat_imageView);

        (mVoicePeople) = (ImageView) findViewById(R.id.voicePeople_imageview);

        mPresenter.attachGlView(mLocalVideoView, mRemoteVideoView);

        hideSoftInput();

        mCaptureView = new ECCaptureView(this);

        setCaptureView(mCaptureView);

    }


    @Override
    protected void setOnListener() {
        super.setOnListener();
        mRrelink.setOnClickListener(this);
        mSendEdit.setOnClickListener(this);
        mChangeTxtOrVoice.setOnClickListener(this);
        mAsrSendBtn.setOnClickListener(this);
        mAsrBtn.setOnClickListener(this);
        navController.setOnNavAndSpeedListener(mPresenter);
        mLocalVideoView.setOnClickListener(this);
        mCancelVideo.setOnClickListener(this);
        mAcceptVideo.setOnClickListener(this);
        mReleaseVideo.setOnClickListener(this);
        mOpenSurface.setOnClickListener(this);
        mLike.setOnClickListener(this);
        mControlSetting.setOnClickListener(this);
        mSport.setOnClickListener(this);
        mVoice.setOnClickListener(this);
        mSetting.setOnClickListener(this);
        mMessage.setOnClickListener(this);
        mHideInput.setOnClickListener(this);
        mRepeat.setOnClickListener(this);
        mVoicePeople.setOnClickListener(this);
        mCaptureView.setOnCameraInitListener(new OnCameraInitListener() {
            @Override
            public void onCameraInit(boolean result) {
                if (!result) L.e("key", "摄像头被占用");
            }
        });
        //本地视频view点击事件
        mLocalVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
//        mEditText.setOnKeyListener(onKeyListener);
    }

    private View.OnKeyListener onKeyListener = new View.OnKeyListener() {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            L.e("key", "event--->" + event.getAction());
            if (keyCode == KeyEvent.KEYCODE_ENTER || event.getAction() == KeyEvent.ACTION_DOWN || event.getAction() == KeyEvent.ACTION_UP) {
                /*隐藏软键盘*/
                hideSoftInput();
                return true;
            }
            return false;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        hideSoftInput();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //自由运动
            case R.id.sport_imageView:
                mPresenter.sendAutoAction();
                break;
            //
            case R.id.setting_imageView:
                mPresenter.showInterfaceDialog();
                break;
            //语音开关
            case R.id.voice_imageView:
                mPresenter.sendSpeech();
                break;
            //
            case R.id.like_imageView:
                mPresenter.showSceneDialog();
                break;
            case R.id.send_text:
//                mPresenter.sendText();//暂时先关掉
                mPresenter.sendAIUIText();
                break;
            case R.id.change_text_voice:
                mPresenter.changeSpeech();
                break;
            //复读
            case R.id.repeat_imageView:
                mPresenter.doRepear();
                break;
            case R.id.asr_send:
                mPresenter.sendAsrResult();
                break;
            case R.id.relink_imageView:
                mPresenter.reLink();
                break;
            case R.id.voice_btn:
                mPresenter.audioPermission();
                break;
            case R.id.localView_imageView:
                mPresenter.localViewVisible();
                break;
            case R.id.video_btn_cancel:
                mPresenter.onRefuse();
                break;
            case R.id.video_btn_accept:
                mPresenter.attachGlView(mLocalVideoView, mRemoteVideoView);
                mPresenter.onAccept();
                break;
            case R.id.video_btn_relese:
                mPresenter.onHangUp();
                break;
            //视频通话
            case R.id.openSurface_imageView:
                mPresenter.makeCall(ECVoIPCallManager.CallType.VIDEO);
                break;
            case R.id.controlSetting_imageView:
                mPresenter.showControl(mControlRelative);
                break;
            case R.id.message_relative:
                mPresenter.showInput(mEdit);
                break;
            case R.id.hideInput_iv:
                mPresenter.showInput(mEdit);
                break;
            // 区别语言
            case R.id.voicePeople_imageview:
                mPresenter.showPersonSelectDialog();
                break;
            default:
                break;
        }
    }

    @Override
    public String getEditText() {
        return mEditText.getText().toString();
    }

    @Override
    public void setEditText(String text) {
        mEditText.setText(text);
    }


    @Override
    public void setTextView(String textView) {
        mConnetState.setText(textView);
    }

    @Override
    public void setLayoutVisible(boolean visible) {
        if (visible) {
            mEditLayout.setVisibility(View.VISIBLE);
            mAsrBtn.setVisibility(View.INVISIBLE);
            mAsrResultLayout.setVisibility(View.GONE);
        } else {
            mEditLayout.setVisibility(View.INVISIBLE);
            mAsrBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setAsrLayoutVisible(boolean visible) {
        mAsrResultLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setQuestion(String question) {
        mQuestion.setText(question);
    }

    @Override
    public void setAnswer(String answer) {
        mAnswer.setText(answer);
    }

    @Override
    public AnimationView getVoiceView() {
        return mVoiceView;
    }

    @Override
    public void setAnimationVisible(boolean visible) {
        mAnimationLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setLocalViewVisiable(boolean viewVisiable) {
        mLocalVideoView.setVisibility(viewVisiable ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setBottomVisible(boolean visible) {
        mReleaseLayout.setVisibility(visible ? View.GONE : View.VISIBLE);//结束(呼出)
        mAcceptLayout.setVisibility(visible ? View.VISIBLE : View.GONE);//接收(呼入)
    }

    @Override
    public void setTopText(String text) {
        mTip.setText(text);
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
    public void setGlViewVisable(boolean visable) {
        mGlView_frameLayout.setVisibility(visable ? View.VISIBLE : View.GONE);
    }


    @Override
    public void setSportVisiable(boolean visiable) {
        if (visiable) {
            mSport.setImageResource(R.mipmap.ic_control_sport);
        } else {
            mSport.setImageResource(R.mipmap.ic_control_sport_pressed);
        }
    }

    @Override
    public void setVoicetVisiable(boolean visiable) {
        if (visiable) {
            mVoice.setImageResource(R.mipmap.ic_control_voice);
        } else {
            mVoice.setImageResource(R.mipmap.ic_control_voice_pressed);
        }
    }

    @Override
    public void setLinkVisiable(boolean visiable) {
        if (visiable) {
            mLink.setImageResource(R.mipmap.ic_control_link);
        } else {
            mLink.setImageResource(R.mipmap.ic_control_link_no);
        }
    }

    @Override
    public void setInputVisiable(boolean visiable) {

        mMessage.setVisibility(visiable ? View.GONE : View.VISIBLE);
        mEdit.setVisibility(visiable ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setRefuseVisable(boolean visable) {

        mReleaseLayout.setVisibility(visable ? View.GONE : View.VISIBLE);//结束(呼出)
    }

    @Override
    public void setRepeatShow(boolean isShow) {
        if (isShow) {
            mRepeat.setImageResource(R.mipmap.ic_repeat_pressed);
        } else {
            mRepeat.setImageResource(R.mipmap.ic_repeat);
        }
    }

    /**
     * 隐藏键盘
     */
    private void hideSoftInput() {
//        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);//显示
//        imm.hideSoftInputFromWindow(mEditText.getApplicationWindowToken(), 0);//强制隐藏键盘
//        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);//自动隐藏/显示

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);

      /*  InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if(inputMethodManager.isActive()){
            inputMethodManager.hideSoftInputFromWindow(mEditText.getApplicationWindowToken(), 0);
        }*/
        fullScreen();

    }

    /**
     * 添加预览到视频通话界面上
     *
     * @param captureView 预览界面
     */
    private void addCaptureView(ECCaptureView captureView) {
        if (mCallRoot != null && captureView != null) {
            mCallRoot.removeView(mCaptureView);
            mCaptureView = null;
            mCaptureView = captureView;
            mCallRoot.addView(captureView, new RelativeLayout.LayoutParams(1, 1));
            mCaptureView.setVisibility(View.VISIBLE);
        }
    }


    public void setCaptureView(ECCaptureView captureView) {
        ECVoIPSetupManager setUpMgr = ECDevice.getECVoIPSetupManager();
        if (setUpMgr != null) {
            setUpMgr.setCaptureView(captureView);
        }
        addCaptureView(captureView);
    }

}
