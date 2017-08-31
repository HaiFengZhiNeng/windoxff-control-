package com.haifeng.robot.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.haifeng.robot.R;
import com.haifeng.robot.config.Constant;
import com.haifeng.robot.utils.CallFailReason;
import com.haifeng.robot.utils.Dlog;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECVoIPCallManager;
import com.yuntongxun.ecsdk.ECVoIPSetupManager;
import com.yuntongxun.ecsdk.VideoRatio;
import com.yuntongxun.ecsdk.VoipMediaChangedInfo;
import com.yuntongxun.ecsdk.voip.video.ECCaptureView;
import com.yuntongxun.ecsdk.voip.video.ECOpenGlView;
import com.yuntongxun.ecsdk.voip.video.OnCameraInitListener;

/**
 * 音频通话
 *
 * @author 管罗苍
 *         created at 2017/7/26 17:25
 */
public class VideoActivity extends BaseVoipActivity implements View.OnClickListener {
    /**
     * 当前呼叫类型对应的布局
     */
    RelativeLayout mCallRoot;

    private Button video_btn_cancel;
    private Button video_btn_accept;
    private Button video_btn_relese;
    private ECOpenGlView remote_video_view;
    private ECOpenGlView local_video_view;

    private LinearLayout ll_release;//结束布局
    private LinearLayout ll_begin;//接收拒绝布局
    private ECCaptureView mCaptureView;
    //计时器
    private Chronometer chronometer;
    private TextView tv_tip;

    private int faild_reason = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        initView();
        initData();
        attachGlView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        final ECVoIPCallManager callInterface = ECDevice.getECVoIPCallManager();
        if (callInterface != null) {
            callInterface.setOnVoIPCallListener(new ECVoIPCallManager.OnVoIPListener() {
                @Override
                public void onVideoRatioChanged(VideoRatio videoRatio) {
                    /**
                     * 远端视频分辨率到达，标识收到视频图像
                     *
                     * @param videoRatio 视频分辨率信息
                     */
                    Dlog.i("onVideoRatioChanged");
                    if (videoRatio == null) {
                        return;
                    }
                    int width = videoRatio.getWidth();
                    int height = videoRatio.getHeight();
                    if (width == 0 || height == 0) {
                        Dlog.e("invalid video width(" + width + ") or height(" + height + ")");
                        return;
                    }
                    if (width > height) {
                        DisplayMetrics dm = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(dm);
                        int mSurfaceViewWidth = dm.widthPixels;
                        int mSurfaceViewHeight = dm.heightPixels;
                        int w = mSurfaceViewWidth * height / width;
//                        int margin = (mSurfaceViewHeight - mVideoTipsLy.getHeight() - w) / 2;
//                        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
//                                RelativeLayout.LayoutParams.MATCH_PARENT,
//                                RelativeLayout.LayoutParams.MATCH_PARENT);
//                        lp.setMargins(0, margin, 0, margin);
//                        remote_video_view.setLayoutParams(lp);
                    }


                }

                @Override
                public void onSwitchCallMediaTypeRequest(String s, ECVoIPCallManager.CallType callType) {
                    Dlog.i("onSwitchCallMediaTypeRequest");
                }

                @Override
                public void onSwitchCallMediaTypeResponse(String s, ECVoIPCallManager.CallType callType) {
                    Dlog.i("onSwitchCallMediaTypeResponse");
                }

                @Override
                public void onDtmfReceived(String s, char c) {
                    Dlog.i("onDtmfReceived");
                }

                @Override
                public void onCallEvents(ECVoIPCallManager.VoIPCall voipCall) {
                    Dlog.i("onCallEvents");
                    // 处理呼叫事件回调
                    if (voipCall == null) {
                        Dlog.i("handle call event error , voipCall null");
                        return;
                    }
                    // 根据不同的事件通知类型来处理不同的业务
                    ECVoIPCallManager.ECCallState callState = voipCall.callState;
                    switch (callState) {
                        case ECCALL_PROCEEDING:
                            Dlog.i("正在连接服务器处理呼叫请求，callid：" + voipCall.callId);
                            ECDevice.getECVoIPSetupManager().enableLoudSpeaker(true);
                            mHandler.sendEmptyMessage(0);
                            break;
                        case ECCALL_ALERTING:
                            Dlog.i("呼叫到达对方，正在振铃，callid：" + voipCall.callId);
                            mHandler.sendEmptyMessage(1);
                            break;
                        case ECCALL_ANSWERED:
                            mHandler.sendEmptyMessage(2);
                            Dlog.i("对方接听本次呼叫,callid：" + voipCall.callId);
                            break;
                        case ECCALL_FAILED:
                            // 本次呼叫失败，根据失败原因进行业务处理或跳转
                            Dlog.i("called:" + voipCall.callId + ",reason:" + voipCall.reason);
                            faild_reason = voipCall.reason;
                            mHandler.sendEmptyMessage(3);
                            break;
                        case ECCALL_RELEASED:
                            mHandler.sendEmptyMessage(4);
                            // 通话释放[完成一次呼叫]
                            break;
                        default:
                            Dlog.i("handle call event error , callState " + callState);
                            break;
                    }
                }

                @Override
                public void onMediaDestinationChanged(VoipMediaChangedInfo voipMediaChangedInfo) {

                }
            });
        }
    }

    private void initData() {
        if (mIncomingCall) {
            // 来电
            //获取当前的callid
            mCallId = getIntent().getStringExtra(ECDevice.CALLID);
            mCallNumber = getIntent().getStringExtra(ECDevice.CALLER);
            ll_release.setVisibility(View.GONE);
            ll_begin.setVisibility(View.VISIBLE);
            tv_tip.setText(mCallNumber + "向您发起视频");
        } else {
            // 呼出
            mCallId = getIntent().getStringExtra(ECDevice.CALLID);
            mCallName = getIntent().getStringExtra(Constant.EXTRA_CALL_NAME);
            mCallNumber = getIntent().getStringExtra(Constant.EXTRA_CALL_NUMBER);
            ll_release.setVisibility(View.VISIBLE);
            ll_begin.setVisibility(View.GONE);
        }
    }

    private void initView() {
        (mCallRoot) = (RelativeLayout) findViewById(R.id.video_root);
        (video_btn_cancel) = (Button) findViewById(R.id.video_btn_cancel);
        (video_btn_accept) = (Button) findViewById(R.id.video_btn_accept);
        (video_btn_relese) = (Button) findViewById(R.id.video_btn_relese);
        (ll_release) = (LinearLayout) findViewById(R.id.ll_release);
        (chronometer) = (Chronometer) findViewById(R.id.chronometer);
        (ll_begin) = (LinearLayout) findViewById(R.id.ll_begin);
        (tv_tip) = (TextView) findViewById(R.id.tv_tip);

        chronometer.stop();
        chronometer.setVisibility(View.GONE);

        (remote_video_view) = (ECOpenGlView) findViewById(R.id.remote_video_view);
        remote_video_view.setGlType(ECOpenGlView.RenderType.RENDER_REMOTE);
        remote_video_view.setAspectMode(ECOpenGlView.AspectMode.CROP);

        (local_video_view) = (ECOpenGlView) findViewById(R.id.local_video_view);
        local_video_view.setGlType(ECOpenGlView.RenderType.RENDER_REMOTE);
        local_video_view.setAspectMode(ECOpenGlView.AspectMode.CROP);

        //本地视频view点击事件
        local_video_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        video_btn_accept.setOnClickListener(this);
        video_btn_cancel.setOnClickListener(this);
        video_btn_relese.setOnClickListener(this);

        mCaptureView = new ECCaptureView(this);
        mCaptureView.setOnCameraInitListener(new OnCameraInitListener() {
            @Override
            public void onCameraInit(boolean result) {
                if (!result) Dlog.e("摄像头被占用");
            }
        });

        setCaptureView(mCaptureView);

    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    tv_tip.setText("正在呼叫。。。");
                    break;
                case 1:
                    tv_tip.setText("等待对方接听");
                    ll_begin.setVisibility(View.GONE);
                    ll_release.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    tv_tip.setText("正在和" + mCallNumber + "视频通话");
                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.setVisibility(View.VISIBLE);
                    chronometer.start();
                    break;
                case 3:
                    tv_tip.setText(CallFailReason.getCallFailReason(faild_reason));
                    chronometer.stop();
                    chronometer.setVisibility(View.GONE);
//                    finish();
                    break;
                case 4:
                    ECDevice.setAudioMode(1);
                    tv_tip.setText("通话结束");
                    chronometer.stop();
                    ECDevice.getECVoIPCallManager().releaseCall(mCallId);
                    finish();
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.video_btn_cancel:
                if (!TextUtils.isEmpty(mCallId)) {
                    ECDevice.getECVoIPCallManager().releaseCall(mCallId);
                    finish();
                }
                break;
            case R.id.video_btn_accept:
                attachGlView();
                ECDevice.getECVoIPCallManager().acceptCall(mCallId);
                ll_release.setVisibility(View.VISIBLE);
                ll_begin.setVisibility(View.GONE);
                break;
            case R.id.video_btn_relese:
                //拒绝呼入
                if (!TextUtils.isEmpty(mCallId)) {
                    ECDevice.getECVoIPCallManager().rejectCall(mCallId, 6666);
                }
                break;
        }
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

    /**
     * 初始化界面
     * 如果视频呼叫，则在接受呼叫之前，需要先设置视频通话显示的view
     * localView本地显示视频的view
     * view 显示远端视频的surfaceview
     */
    private void attachGlView() {
        ECVoIPSetupManager setupManager = ECDevice.getECVoIPSetupManager();
        if (setupManager == null) {
            return;
        }
        setupManager.setGlDisplayWindow(local_video_view, remote_video_view);
    }

    public void setCaptureView(ECCaptureView captureView) {
        ECVoIPSetupManager setUpMgr = ECDevice.getECVoIPSetupManager();
        if (setUpMgr != null) {
            setUpMgr.setCaptureView(captureView);
        }
        addCaptureView(captureView);
    }


}
