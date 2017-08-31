package com.ocean.speech.voice;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.ocean.mvp.library.utils.L;
import com.ocean.speech.base.ControlBasePresenter;
import com.ocean.speech.config.Constant;
import com.ocean.speech.control.ControlView;
import com.ocean.speech.util.CallFailReason;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECVoIPCallManager;
import com.yuntongxun.ecsdk.VideoRatio;
import com.yuntongxun.ecsdk.VoipMediaChangedInfo;

/**
 * Created by dell on 2017/8/1.
 */

public class VoicePresenter extends ControlBasePresenter<IVoiceView> {
    public VoicePresenter(IVoiceView mView) {
        super(mView);
    }

    //失败原因
    private int faild_reason = 0;

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        initCall();
    }

    @Override
    public void onResume() {
        super.onResume();
        final ECVoIPCallManager callInterface = ECDevice.getECVoIPCallManager();
        if (callInterface != null) {
            callInterface.setOnVoIPCallListener(new ECVoIPCallManager.OnVoIPListener() {
                @Override
                public void onVideoRatioChanged(VideoRatio videoRatio) {
                    L.e("key", "onVideoRatioChanged，");
                }

                @Override
                public void onSwitchCallMediaTypeRequest(String s, ECVoIPCallManager.CallType callType) {
                    L.e("key", "onSwitchCallMediaTypeRequest，");
                }

                @Override
                public void onSwitchCallMediaTypeResponse(String s, ECVoIPCallManager.CallType callType) {
                    L.e("key", "onSwitchCallMediaTypeResponse，");
                }

                @Override
                public void onDtmfReceived(String s, char c) {
                    L.e("key", "onDtmfReceived，");
                }

                @Override
                public void onCallEvents(ECVoIPCallManager.VoIPCall voipCall) {
                    // 处理呼叫事件回调
                    if (voipCall == null) {
                        return;
                    }
                    // 根据不同的事件通知类型来处理不同的业务
                    ECVoIPCallManager.ECCallState callState = voipCall.callState;
                    switch (callState) {
                        case ECCALL_PROCEEDING:
                            mHandler.sendEmptyMessage(0);
                            break;
                        case ECCALL_ALERTING:
                            L.e("key", "呼叫到达对方，正在振铃，callid：" + voipCall.callId);
                            mHandler.sendEmptyMessage(1);
                            break;
                        case ECCALL_ANSWERED:
                            mHandler.sendEmptyMessage(2);
                            L.e("key", "对方接听本次呼叫,callid：" + voipCall.callId);
                            break;
                        case ECCALL_FAILED:
                            // 本次呼叫失败，根据失败原因进行业务处理或跳转
                            L.e("key", "called:" + voipCall.callId + ",reason:" + voipCall.reason);
                            faild_reason = voipCall.reason;
                            mHandler.sendEmptyMessage(3);
                            break;
                        case ECCALL_RELEASED:
                            mHandler.sendEmptyMessage(4);
                            // 通话释放[完成一次呼叫]
                            break;
                        default:
                            L.e("key", "handle call event error , callState " + callState);
                            break;
                    }
                }

                @Override
                public void onMediaDestinationChanged(VoipMediaChangedInfo voipMediaChangedInfo) {

                }
            });
        }
    }

    private void initCall() {

        //获取是否是呼入还是呼出
        if (mIncomingCall) {
            // 来电
            mCallId = ((Activity) mView).getIntent().getStringExtra(ECDevice.CALLID);
            mCallNumber = ((Activity) mView).getIntent().getStringExtra(ECDevice.CALLER);
            mView.setLayoutVisible(false);
            mView.setVoiceText(mCallNumber + "来电");
        } else {
            // 呼出
            mCallId = ((Activity) mView).getIntent().getStringExtra(ECDevice.CALLID);
            mCallName = ((Activity) mView).getIntent().getStringExtra(Constant.EXTRA_CALL_NAME);
            mCallNumber = ((Activity) mView).getIntent().getStringExtra(Constant.EXTRA_CALL_NUMBER);
            mView.setLayoutVisible(true);
        }
        //true  代表呼出  获取到呼入的类型是音频或者视频呼叫，然后来设置对应UI布局
        //获取是否是音频还是视频
        mCallType = (ECVoIPCallManager.CallType)
                ((Activity) mView).getIntent().getSerializableExtra(ECDevice.CALLTYPE);
        if (ECVoIPCallManager.CallType.VIDEO.equals(mCallType)) {
            //获取当前的callid
            mCallId = ((Activity) mView).getIntent().getStringExtra(ECDevice.CALLID);
            mCallNumber = ((Activity) mView).getIntent().getStringExtra(ECDevice.CALLER);
            Bundle bundle = new Bundle();
            bundle.putString(ECDevice.CALLID, mCallId);
            bundle.putString(ECDevice.CALLER, mCallNumber);
            startActivity(ControlView.class, bundle);
            exit();
        }
    }

    /**
     * 挂断
     */
    void onHangUp() {
        mCallId = ((Activity) mView).getIntent().getStringExtra(ECDevice.CALLID);
        if (!TextUtils.isEmpty(mCallId)) {
            ECDevice.getECVoIPCallManager().releaseCall(mCallId);
            exit();
        }
        mView.setChronometerVisible(true);
    }

    /**
     * 接听
     */
    void onAccept() {
        if (!TextUtils.isEmpty(mCallId)) {
            ECDevice.getECVoIPCallManager().acceptCall(mCallId);
            mView.setLayoutVisible(true);
        }
    }

    /**
     * 拒绝
     */
    void onRefuse() {
        //拒绝呼入
        if (!TextUtils.isEmpty(mCallId)) {
            ECDevice.getECVoIPCallManager().rejectCall(mCallId, 666);
        }
    }

    /**
     * 静音
     */
    void onMute() {

        //功能:获取当前通话静音状态
        //返回值：返回true则是静音状态，false则不是静音状态.
        ECDevice.getECVoIPSetupManager().getMuteStatus();
        if (ECDevice.getECVoIPSetupManager().getMuteStatus()) {
            //功能:设置通话静音状态
            //参数：on:传入true则对方听不到说话，false则对方可以听到说话。
            mView.setMuteVisiable(false);
            ECDevice.getECVoIPSetupManager().setMute(false);
        } else {
            //功能:设置通话静音状态
            //参数：on:传入true则对方听不到说话，false则对方可以听到说话。
            mView.setMuteVisiable(true);
            ECDevice.getECVoIPSetupManager().setMute(true);
        }


    }

    /**
     * 扬声器
     */
    void onSpeaker() {
        //获取当前扬声器的状态
        // true是开启，false则为关闭。
//        ECDevice.getECVoIPSetupManager().getLoudSpeakerStatus();
        if (ECDevice.getECVoIPSetupManager().getLoudSpeakerStatus()) {
            //设置扬声器的状态 true是开启，false则为关闭。
            mView.setSpeakerVisiable(false);
            ECDevice.getECVoIPSetupManager().enableLoudSpeaker(false);
        } else {
            //设置扬声器的状态 true是开启，false则为关闭。
            mView.setSpeakerVisiable(true);
            ECDevice.getECVoIPSetupManager().enableLoudSpeaker(true);
        }
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    mView.setVoiceText("正在呼叫。。。");
                    break;
                case 1:
                    mView.setVoiceText("等待对方接听");
                    mView.setLayoutVisible(true);
                    break;
                case 2:
                    mView.setVoiceText("正在和" + mCallNumber + "语音通话");
                    mView.setChronometerVisible(true);
                    mView.startUpChronometer(true);
                    mView.setChronometerTime();
                    break;
                case 3:
                    mView.setVoiceText(CallFailReason.getCallFailReason(faild_reason) + "");
                    mView.startUpChronometer(false);
                    mView.setChronometerVisible(false);
                    break;
                case 4:
                    ECDevice.setAudioMode(1);
                    mView.setVoiceText("通话结束");
                    mView.startUpChronometer(false);
                    mView.setChronometerVisible(false);
                    ECDevice.getECVoIPCallManager().releaseCall(mCallId);
                    exit();
                    break;
            }
        }
    };


}
