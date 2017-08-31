package com.haifeng.robot.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

/**
 * 接听电话的页面
 *
 * @author 管罗苍
 *         created at 2017/7/27 11:43
 */
public class VoIPCallActivity extends BaseVoipActivity implements View.OnClickListener {


    private static final String TAG = "VoIPCallActivity";

    private TextView tv_calling;
    private Chronometer chronometer;
    //挂断
    private LinearLayout calling_bottom_release;
    private ImageButton layout_call_release;
    //来电
    private LinearLayout layout_call_bottom;
    private ImageButton layout_call_cancel;//取消
    private ImageButton layout_call_accept;//接听
    //失败原因
    private int faild_reason = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vo_ipcall);

        initData();

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            finish();
            return;
        }

        extras.getString(Constant.EXTRA_CALL_NUMBER);
        initCall();

        //true  代表呼出  获取到呼入的类型是音频或者视频呼叫，然后来设置对应UI布局
        //获取是否是音频还是视频
        mCallType = (ECVoIPCallManager.CallType)
                getIntent().getSerializableExtra(ECDevice.CALLTYPE);
        if (ECVoIPCallManager.CallType.VIDEO.equals(mCallType)) {
            //获取当前的callid
            mCallId = getIntent().getStringExtra(ECDevice.CALLID);
            mCallNumber = getIntent().getStringExtra(ECDevice.CALLER);
            Intent intent = new Intent(VoIPCallActivity.this, VideoActivity.class);
            intent.putExtra(ECDevice.CALLID, mCallId);
            intent.putExtra(ECDevice.CALLER, mCallNumber);
            VoIPCallActivity.this.startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        final ECVoIPCallManager callInterface = ECDevice.getECVoIPCallManager();
        if (callInterface != null) {
            callInterface.setOnVoIPCallListener(new ECVoIPCallManager.OnVoIPListener() {
                @Override
                public void onVideoRatioChanged(VideoRatio videoRatio) {
                    Dlog.i("onVideoRatioChanged");
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
        (tv_calling) = (TextView) findViewById(R.id.tv_calling);
        (layout_call_release) = (ImageButton) findViewById(R.id.layout_call_release);
        (calling_bottom_release) = (LinearLayout) findViewById(R.id.calling_bottom_release);

        //计时器
        (chronometer) = (Chronometer) findViewById(R.id.chronometer);
        chronometer.stop();
        chronometer.setVisibility(View.GONE);

        (layout_call_bottom) = (LinearLayout) findViewById(R.id.layout_call_bottom);
        (layout_call_cancel) = (ImageButton) findViewById(R.id.layout_call_cancel);
        (layout_call_accept) = (ImageButton) findViewById(R.id.layout_call_accept);
        layout_call_release.setOnClickListener(this);
        layout_call_cancel.setOnClickListener(this);
        layout_call_accept.setOnClickListener(this);
    }

    private void initCall() {

        ECDevice.getECVoIPSetupManager().selectCamera(1, 1000, 0, ECVoIPSetupManager.Rotate.ROTATE_0, true);
        //获取是否是呼入还是呼出
        if (mIncomingCall) {
            // 来电
            mCallId = getIntent().getStringExtra(ECDevice.CALLID);
            mCallNumber = getIntent().getStringExtra(ECDevice.CALLER);
            calling_bottom_release.setVisibility(View.GONE);
            layout_call_bottom.setVisibility(View.VISIBLE);
            tv_calling.setText(mCallNumber + "来电");
        } else {
            // 呼出
            mCallId = getIntent().getStringExtra(ECDevice.CALLID);
            mCallName = getIntent().getStringExtra(Constant.EXTRA_CALL_NAME);
            mCallNumber = getIntent().getStringExtra(Constant.EXTRA_CALL_NUMBER);
            calling_bottom_release.setVisibility(View.VISIBLE);
            layout_call_bottom.setVisibility(View.GONE);
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    tv_calling.setText("正在呼叫。。。");
                    break;
                case 1:
                    tv_calling.setText("等待对方接听");
                    calling_bottom_release.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    tv_calling.setText("正在和" + mCallNumber + "语音通话");
                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.setVisibility(View.VISIBLE);
                    chronometer.start();
                    break;
                case 3:
                    tv_calling.setText(CallFailReason.getCallFailReason(faild_reason));
                    chronometer.stop();
                    chronometer.setVisibility(View.GONE);
                    break;
                case 4:
                    ECDevice.setAudioMode(1);
                    tv_calling.setText("通话结束");
                    chronometer.stop();
                    chronometer.setVisibility(View.GONE);
                    ECDevice.getECVoIPCallManager().releaseCall(mCallId);
                    finish();
                    break;
            }
        }
    };


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_call_release:
                mCallId = getIntent().getStringExtra(ECDevice.CALLID);
                if (!TextUtils.isEmpty(mCallId)) {
                    ECDevice.getECVoIPCallManager().releaseCall(mCallId);
                    finish();
                }
                break;
            case R.id.layout_call_accept:
                if (!TextUtils.isEmpty(mCallId)) {
                    ECDevice.getECVoIPCallManager().acceptCall(mCallId);
                    calling_bottom_release.setVisibility(View.VISIBLE);
                    layout_call_bottom.setVisibility(View.GONE);
                }
                break;
            case R.id.layout_call_cancel:
                //拒绝呼入
                if (!TextUtils.isEmpty(mCallId)) {
                    ECDevice.getECVoIPCallManager().rejectCall(mCallId, 666);
                }
                break;
        }
    }
}
