package com.haifeng.robot.activity;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.haifeng.robot.MainActivity;
import com.haifeng.robot.R;
import com.haifeng.robot.utils.Dlog;
import com.ocean.mvp.library.utils.L;
import com.yuntongxun.ecsdk.ECChatManager;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECInitParams;
import com.yuntongxun.ecsdk.ECMessage;
import com.yuntongxun.ecsdk.ECVoIPCallManager;
import com.yuntongxun.ecsdk.ECVoIPSetupManager;
import com.yuntongxun.ecsdk.OnChatReceiveListener;
import com.yuntongxun.ecsdk.OnMeetingListener;
import com.yuntongxun.ecsdk.PersonInfo;
import com.yuntongxun.ecsdk.SdkErrorCode;
import com.yuntongxun.ecsdk.VideoRatio;
import com.yuntongxun.ecsdk.VoipMediaChangedInfo;
import com.yuntongxun.ecsdk.im.ECMessageNotify;
import com.yuntongxun.ecsdk.im.group.ECGroupNoticeMessage;
import com.yuntongxun.ecsdk.meeting.intercom.ECInterPhoneMeetingMsg;
import com.yuntongxun.ecsdk.meeting.video.ECVideoMeetingMsg;
import com.yuntongxun.ecsdk.meeting.voice.ECVoiceMeetingMsg;

import java.util.List;

public class LoginActivity extends BaseVoipActivity implements View.OnClickListener {

    private TextView tv_Dlogin;
    private EditText et_username;

    private String username = "";
    private String appKey = "8a216da85d793b69015d7ca91a9800c3";
    private String token = "8164f9dc076e539220fd200188de9ddc";

    private boolean isInitSuccess = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        (et_username) = (EditText) findViewById(R.id.et_username);
        (tv_Dlogin) = (TextView) findViewById(R.id.tv_login);

        tv_Dlogin.setOnClickListener(this);

        init();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_login:
                username = et_username.getText().toString().trim();
                if (!TextUtils.isEmpty(username)) {
                    if (isInitSuccess) {
                        Dlogin();
                    } else {
                        Toast.makeText(this, "初始化失败", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "请输入账号", Toast.LENGTH_LONG).show();
                }

                break;
        }
    }

    /**
     * 初始化
     */
    public void init() {
        //判断SDK是否已经初始化
        if (!ECDevice.isInitialized()) {
            ECDevice.initial(this, initListener);
        }
    }

    /**
     * 登录
     */
    private void Dlogin() {
        if (!isInitSuccess)
            return;
        Dlog.e("初始化SDK及登陆代码完成");
        /**
         * 设置接收VoIP来电事件通知Intent
         * 呼入界面activity、开发者需修改该类
         * */
        Intent intent = new Intent(LoginActivity.this, VoIPCallActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(LoginActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ECDevice.setPendingIntent(pendingIntent);

        /**
         * 登录回调
         */
        ECDevice.setOnDeviceConnectListener(onECDeviceConnectListener);

        ECInitParams params = ECInitParams.createParams();
        params.setUserid(username);
        params.setAppKey(appKey);
        params.setToken(token);
        //设置登陆验证模式：自定义登录方式
        params.setAuthType(ECInitParams.LoginAuthType.NORMAL_AUTH);
        //DloginMode（强制上线：FORCE_DlogIN  默认登录：AUTO。使用方式详见注意事项）
        params.setMode(ECInitParams.LoginMode.FORCE_LOGIN);


        /**
         * 验证参数是否正确
         * */
        if (params.validate()) {
            // 登录函数
            ECDevice.login(params);
        }
        //设置VOIP 自定义铃声路径
        ECVoIPSetupManager setupManager = ECDevice.getECVoIPSetupManager();
        if (setupManager != null) {
            // 目前支持下面三种路径查找方式
            // 1、如果是assets目录则设置为前缀[assets://]
            setupManager.setInComingRingUrl(true, "raw://phonering.mp3");
            setupManager.setOutGoingRingUrl(true, "raw://phonering.mp3");
            setupManager.setBusyRingTone(true, "raw://playend.mp3");

            // 2、如果是raw目录则设置为前缀[raw://]
            // 3、如果是SDCard目录则设置为前缀[file://]
        }

        ECChatManager manager = ECDevice.getECChatManager();
        // 调用查询个人信息接口，设置结果回调
        manager.getPersonInfo(new ECChatManager.OnGetPersonInfoListener() {
            @Override
            public void onGetPersonInfoComplete(ECError e, PersonInfo p) {
                if (SdkErrorCode.REQUEST_SUCCESS == e.errorCode) {
                    // 个人信息获取成功
                    // 更新个人信息到本地数据库，通知UI刷新
                    L.e("key", p.toString());
                    return;
                }
                L.e("ECSDK_Demo", "get person info fail  " +
                        ", errorCode=" + e.errorCode);

//                userId='15554955416', version=1, nickName='啊啊啊', sex=MALE, birth='2017-07-29', sign='看看'
            }
        });

    }


    private ECDevice.InitListener initListener = new ECDevice.InitListener() {
        @Override
        public void onInitialized() {
            Dlog.e("初始化SDK成功");
            /**
             * 音视频回调
             * */
            if (ECDevice.getECMeetingManager() != null) {
                ECDevice.getECMeetingManager().setOnMeetingListener(new OnMeetingListener() {
                    @Override
                    public void onVideoRatioChanged(VideoRatio videoRatio) {

                    }

                    @Override
                    public void onReceiveInterPhoneMeetingMsg(ECInterPhoneMeetingMsg msg) {
                        // 处理实时对讲消息Push
                    }

                    @Override
                    public void onReceiveVoiceMeetingMsg(ECVoiceMeetingMsg msg) {
                        // 处理语音会议消息push
                    }

                    @Override
                    public void onReceiveVideoMeetingMsg(ECVideoMeetingMsg msg) {
                        // 处理视频会议消息Push（暂未提供）
                    }

                    @Override
                    public void onMeetingPermission(String s) {

                    }
                });
            }
            /***
             * 语音通话的回调监听
             * */
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
                        // 处理呼叫事件回调
                        if (voipCall == null) {
                            return;
                        }
                        // 根据不同的事件通知类型来处理不同的业务
                        ECVoIPCallManager.ECCallState callState = voipCall.callState;
                        switch (callState) {
                            case ECCALL_PROCEEDING:
                                Dlog.i("正在连接服务器处理呼叫请求，callid：" + voipCall.callId);
                                break;
                            case ECCALL_ALERTING:
                                Dlog.i("呼叫到达对方，正在振铃，callid：" + voipCall.callId);
                                break;
                            case ECCALL_ANSWERED:
                                Dlog.i("对方接听本次呼叫,callid：" + voipCall.callId);
                                break;
                            case ECCALL_FAILED:
                                // 本次呼叫失败，根据失败原因进行业务处理或跳转
                                Dlog.i("called:" + voipCall.callId + ",reason:" + voipCall.reason);
                                break;
                            case ECCALL_RELEASED:
                                // 通话释放[完成一次呼叫]
                                ECDevice.getECVoIPCallManager().releaseCall(mCallId);
//                                releaseCall;
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

            //IM接收消息监听，使用IM功能的开发者需要设置。
            ECDevice.setOnChatReceiveListener(new OnChatReceiveListener() {
                @Override
                public void OnReceivedMessage(ECMessage msg) {
                    L.e("key", "==收到新消息");
                }

                @Override
                public void onReceiveMessageNotify(ECMessageNotify ecMessageNotify) {

                }

                @Override
                public void OnReceiveGroupNoticeMessage(ECGroupNoticeMessage notice) {
                    //收到群组通知消息,可以根据ECGroupNoticeMessage.ECGroupMessageType类型区分不同消息类型
                    Log.i("", "==收到群组通知消息（有人加入、退出...）");
                }

                @Override
                public void onOfflineMessageCount(int count) {
                    // 登陆成功之后SDK回调该接口通知帐号离线消息数
                }

                @Override
                public int onGetOfflineMessage() {
                    return 0;
                }

                @Override
                public void onReceiveOfflineMessage(List msgs) {
                    // SDK根据应用设置的离线消息拉取规则通知应用离线消息
                }

                @Override
                public void onReceiveOfflineMessageCompletion() {
                    // SDK通知应用离线消息拉取完成
                }

                @Override
                public void onServicePersonVersion(int version) {
                    // SDK通知应用当前帐号的个人信息版本号
                }

                @Override
                public void onReceiveDeskMessage(ECMessage ecMessage) {

                }

                @Override
                public void onSoftVersion(String s, int i) {

                }
            });


            isInitSuccess = true;
        }

        @Override
        public void onError(Exception exception) {
            Dlog.e("初始化SDK失败" + exception.getMessage());
            isInitSuccess = false;
        }
    };

    /**
     * 设置通知回调监听包含登录状态监听，接收消息监听，呼叫事件回调监听和
     * 设置接收来电事件通知Intent等
     */
    private ECDevice.OnECDeviceConnectListener onECDeviceConnectListener = new ECDevice.OnECDeviceConnectListener() {
        public void onConnect() {
            //兼容旧版本的方法，不必处理
        }

        @Override
        public void onDisconnect(ECError error) {
            //兼容旧版本的方法，不必处理
        }

        @Override
        public void onConnectState(ECDevice.ECConnectState state, ECError error) {
            if (state == ECDevice.ECConnectState.CONNECT_FAILED) {
                if (error.errorCode == SdkErrorCode.SDK_KICKED_OFF) {
                    Dlog.e("==帐号异地登陆");
                } else {
                    Dlog.e("==其他登录失败,错误码：" + error.errorCode);
                    Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_LONG).show();
                }
                return;
            } else if (state == ECDevice.ECConnectState.CONNECT_SUCCESS) {
                Dlog.e("==登陆成功" + error.toString());

                Intent intent_gomain = new Intent(LoginActivity.this, MainActivity.class);
                LoginActivity.this.startActivity(intent_gomain);
                finish();
            }
        }
    };


    // 增加通过账号获取个人信息接口：

}
