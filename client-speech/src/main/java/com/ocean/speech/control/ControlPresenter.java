package com.ocean.speech.control;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;
import com.iflytek.aiui.AIUIListener;
import com.iflytek.cloud.thirdparty.V;
import com.ocean.mvp.library.net.NetClient;
import com.ocean.mvp.library.utils.L;
import com.ocean.speech.R;
import com.ocean.speech.SpeechApplication;
import com.ocean.speech.asr.AsrControl;
import com.ocean.speech.asr.CustomControl;
import com.ocean.speech.asr.NlpControl;
import com.ocean.speech.base.ControlBasePresenter;
import com.ocean.speech.config.Constant;
import com.ocean.speech.dao.DataBaseDao;
import com.ocean.speech.dialog.InterfaceDialog;
import com.ocean.speech.dialog.PersonSelectDialog;
import com.ocean.speech.dialog.SceneDialog;
import com.ocean.speech.udp.UdpControl;
import com.ocean.speech.udp.UdpReceiver;
import com.ocean.speech.util.CallFailReason;
import com.ocean.speech.util.MyAnimation;
import com.ocean.speech.youdao.TranslateLanguage;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECVoIPCallManager;
import com.yuntongxun.ecsdk.ECVoIPSetupManager;
import com.yuntongxun.ecsdk.VideoRatio;
import com.yuntongxun.ecsdk.VoipMediaChangedInfo;
import com.yuntongxun.ecsdk.voip.video.ECOpenGlView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static com.ocean.speech.select.SelectPresenter.RESULT_CODE_STARTAUDIO;

/**
 * Created by zhangyuanyuan on 2017/7/8.
 */

public class ControlPresenter extends ControlBasePresenter<IControlView> implements NavController.OnNavAndSpeedListener {


    public ControlPresenter(IControlView mView) {
        super(mView);
    }

    private InterfaceDialog interfaceDialog = null;//显示界面控制的Dialog
    private SceneDialog sceneDialog = null;//显示DIY音频的 Dialog
    private PersonSelectDialog selectDialog = null;// 选择发音人Dialog


    private boolean isAutoAction = false;
    private boolean isSpeech = false;

    private UdpControl udpControl;

    private SpeechApplication application;

    private NetClient client;
    byte[] controlBytes = new byte[7];

        private Handler mHandler;
    private long delayedTime = 200;


    private Thread mUDPReceiveRunnable;

    byte[] sendMsg = null;
    //设置重复监听所需的状态：true：可交互；false：不可交互
    private boolean isSendMsg = false;

    byte[] interfaceBytes = null;
    private boolean isInterface = false;

    byte[] getDataBytes = null;
    private boolean isGetData = false;

    private StringBuffer stringBuffer = new StringBuffer();

    private ArrayList<InterfaceBean> been = null;
    /**
     * 标记当前是否为语音输入
     */
    private boolean isSpeechInput = false;

    private AsrControl mAsr;
    private NlpControl nlpControl;
    private int mAIUIState = AIUIConstant.STATE_IDLE;

    private CustomControl customControl;
    /**
     *语义理解返回结果，如果需要翻译成英文：true,否则：false
     */
    private boolean needTransalte = false;

    private HashMap<String, Object> asrMap = null;
    private String asrAnswer = "";
    private int textSendCount = 0;

    private boolean isCanSend = false;

    //失败原因
    private int faild_reason = 0;
    private boolean isVisiable = true;

    private boolean isAreLevelShowing = true;

    private boolean isShowInput = true;

    //是否复读
    private boolean isRepeat = false;


    public static final int RESULT_CODE_STARTAUDIO = 100;

    // 解析得到语义结果
    String finalText = "";

    //默认发音人
    private String voicer = "xiaoyan";

    private boolean isSpeak = true;

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        udpControl = UdpControl.getInstance();
        application = SpeechApplication.from(getContext());
        client = application.getNetClient();

        initHandler();
        mHandler = getHandler();

        initData();

        mAsr = new AsrControl(SpeechApplication.from(getContext()));
        mAsr.init();
        mAsr.setOnRecognizerListener(recognizerListener);

        nlpControl = new NlpControl(SpeechApplication.from(getContext()));
        nlpControl.setmAIUIListener(mAIUIListener);
        nlpControl.init();

        customControl = new CustomControl(SpeechApplication.from(getContext()));
        customControl.setmAIUIListener(mAIUIListener);

        controlBytes[0] = (byte) 0xAA;
        controlBytes[1] = (byte) 0x01;
        controlBytes[3] |= (byte) (1);
//        controlBytes[3] &= (byte) ~(1);
//        controlBytes[3] &= (byte) 0xfe;
        controlBytes[6] = (byte) 0xBB;


        //获取UDP的ip
//        udpControl.sendUdpSocketToByIp(application, client);
        //// TODO: 2017/11/18 暂时修改IP，端口号不用改 
        UdpControl.getInstance().setUdpIp("192.168.0.239", 8891);

        mUDPReceiveRunnable = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isCanSend) {
                    try {
                        L.i("control", "send control bytes ");
                        byte[] bytes = null;
                        if (isSendMsg) {
                            textSendCount++;
                            isSendMsg = false;
                            bytes = sendMsg;
                            if (textSendCount <= 5)
                                mHandler.sendEmptyMessageDelayed(3, 100);
                        } else if (isInterface) {
                            isInterface = false;
                            bytes = interfaceBytes;
                        } else if (isGetData) {
                            isGetData = false;
                            bytes = getDataBytes;
                        } else {
                            controlBytes[5] = (byte) (controlBytes[1] ^ controlBytes[2] ^ controlBytes[3] ^ controlBytes[4]);
                            bytes = controlBytes;
                        }
                        if (bytes != null)
                            udpControl.sendUdpByteMessage(bytes, application, client);

                        controlBytes[3] &= (byte) 0x07;//清空语音DIY的数据
                        Thread.sleep(delayedTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        //增加
        isCanSend = true;
        mUDPReceiveRunnable.start();

        client.registerUdpServer(new UdpReceiver(new OnListenerUDPServer() {
            @Override
            public void receiver(String receiver) {
                L.e(TAG, "接收到UDP返回的数据--->" + receiver);
                if (receiver.contains("{")) {
                    mHandler.removeMessages(4);
                    stringBuffer.append(receiver);
                    mHandler.sendEmptyMessageDelayed(4, 300);
                }
            }

            @Override
            public void acquireIp(boolean isAcquire) {
                //已获取到ip，开始循环发送指令
                mHandler.sendEmptyMessage(0);
                L.e(TAG, "已获取到ip，开始循环发送指令--->" + isAcquire);
                if (isAcquire) {
                    isCanSend = true;
                    mUDPReceiveRunnable.start();
                }

            }
        }));

    }

    private void initData() {
//        L.e("key", ECDevice.getECVoIPSetupManager().getCameraInfos() + "");
//        CameraInfo[] cameraInfo = ECDevice.getECVoIPSetupManager().getCameraInfos();

        /**
         * name-->Camera 0, Facing back, Orientation 90      caps-->0      describeContents-->0      index-->0
         *
         * name-->Camera 1, Facing front, Orientation 270      caps-->0      describeContents-->0      index-->1
         * */
//        for (int i = 0; i < cameraInfo.length; i++) {
//            L.e("key", "name-->" + cameraInfo[i].name + "      caps-->" + cameraInfo[i].caps[1].cameraIndex + "      describeContents-->" + cameraInfo[i].describeContents() + "      index-->" + cameraInfo[i].index);
//        }
        /**
         * cameraIndex 手机摄像头标示
         * capabilituIndex 手机摄像头所支持的分辨率集合中的index
         * fps 摄像头码率
         * rotate 摄像头旋转的度数 默认为0 （0，90，180，270）
         * force 是否强制初始化摄像头
         * */
        ECDevice.getECVoIPSetupManager().selectCamera(1, 3, 15, ECVoIPSetupManager.Rotate.ROTATE_0, true);

        if (mIncomingCall) {
            // 来电
            //获取当前的callid
            mCallId = ((Activity) mView).getIntent().getStringExtra(ECDevice.CALLID);
            mCallNumber = ((Activity) mView).getIntent().getStringExtra(ECDevice.CALLER);
            mView.setBottomVisible(true);
            mView.setTopText(mCallNumber + "向您发起视频");
        } else {
            // 呼出
//            mCallId = ((Activity) mView).getIntent().getStringExtra(ECDevice.CALLID);
//            mCallName = ((Activity) mView).getIntent().getStringExtra(Constant.EXTRA_CALL_NAME);
//            mCallNumber = ((Activity) mView).getIntent().getStringExtra(Constant.EXTRA_CALL_NUMBER);
//            mView.setBottomVisible(false);
        }

    }

    @Override
    protected void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case 0:
                mView.setTextView("已连接" + UdpControl.getInstance().mUdpIP);
                mView.setLinkVisiable(true);
                showToast("获取到ip");
                break;
            case 3:
                isSendMsg = true;
                break;
            case 4:
                parseJsonConter(stringBuffer.toString());
                break;
            case 5:
                if (msg.obj != null) {
                    asrMap = (HashMap<String, Object>) msg.obj;
                    String question = "";
                    if (asrMap.containsKey("QUESTION")) {
                        //问题
                        question = ((String) asrMap.get("QUESTION")).trim().replace(" ", "");
                    }
                    if (asrMap.containsKey("ANSWER")) {
                        //识别到的答案
                        asrAnswer = (String) asrMap.get("ANSWER");
                    }

                    mView.setAsrLayoutVisible(true);
                    mView.setQuestion("Q:" + question);
                    mView.setAnswer("A:" + asrAnswer);
                } else {
                    mView.setAsrLayoutVisible(true);
                    mView.setQuestion("未识别出来");
                    mView.setAnswer("");
                    mView.setLinkVisiable(false);
                }
                break;
            case 1000:
                mView.setTopText("正在呼叫。。。");
                break;
            case 1001:
                mView.setTopText("等待对方接听");
                mView.setLayoutVisible(true);
                break;
            case 1002:
                if (mIncomingCall) {

                    mView.setTopText("正在和" + mCallNumber + "语音通话");
                } else {
                    mView.setTopText("正在和" + mNumber + "语音通话");
                }
                mView.startUpChronometer(true);
                mView.setChronometerVisible(true);
                mView.setChronometerTime();
                break;
            case 1003:
                mView.setTopText(CallFailReason.getCallFailReason(faild_reason) + "");
                mView.startUpChronometer(false);
                mView.setChronometerVisible(false);
                break;
            case 1004:
                ECDevice.setAudioMode(1);
                mView.setTopText("通话结束");
                mView.startUpChronometer(false);
                mView.setChronometerVisible(false);
                ECDevice.getECVoIPCallManager().releaseCall(mCallId);
                exit();
                break;
        }
    }

    private AsrControl.OnRecognizerListener recognizerListener = new AsrControl.OnRecognizerListener() {
        @Override
        public void onResult(HashMap<String, Object> result) {
            //将识别到的结果 展示出来，由用户确定是否发送
//            if (result != null) {
//                mHandler.sendMessage(mHandler.obtainMessage(5, result));
//                isSendMsg = true;
//            } else {
//                mHandler.sendMessage(mHandler.obtainMessage(5, null));
//            }
        }

        @Override
        public void onVolumeChange(int volume) {
            mView.getVoiceView().setSignalEMA(volume);
        }

        @Override
        public void onEndOfSpeech() {
            mView.setAnimationVisible(false);
        }
    };


    int counter = 0;

    /**
     * 判断str1中包含str2的个数
     *
     * @param str1
     * @param str2
     * @return counter
     */
    private int countStr(String str1, String str2) {
        if (str1.indexOf(str2) == -1) {
            return 0;
        } else if (str1.indexOf(str2) != -1) {
            counter++;
            countStr(str1.substring(str1.indexOf(str2) +
                    str2.length()), str2);
            return counter;
        }
        return 0;
    }

    /**
     * 解析界面控制数据
     *
     * @param result json
     */
    private void parseJsonConter(String result) {
        if (TextUtils.isEmpty(result))
            return;

        counter = 0;
        int count = countStr(result, "}");

        if (count > 0) {
            if (count == 1) {
                pasreJson(result);
            } else {
                String[] arr = result.split("\\u007B");// { 的转义
                int size = arr.length;
                for (int i = 1; i < size; i++) {
                    L.e("json", "result->{" + arr[i]);
                    pasreJson("{" + arr[i]);
                }
            }
        }
    }

    /**
     * 解析json
     *
     * @param result
     */
    private void pasreJson(String result) {
        try {
            JSONObject obj = new JSONObject(result);
            String content = obj.optString("key_word");
            int id = obj.optInt("id");
            int count = obj.optInt("count");
            if (been != null) {
                been.add(new InterfaceBean(content, id));
                L.e("json", "been--" + been.size() + "    count--" + count);
                if (count == been.size()) {

                    DataBaseDao dao = new DataBaseDao(getContext());
                    dao.clear();
                    dao.insert(been);
                    if (interfaceDialog != null && interfaceDialog.isShowing())
                        interfaceDialog.setData(been);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * 显示界面控制的dialog
     */

    void showInterfaceDialog() {
        if (interfaceDialog == null) {
            interfaceDialog = new InterfaceDialog(getContext(), "");
        }
        interfaceDialog.setClickListener(clickListenerInterface);
        interfaceDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                //获取data数据
                DataBaseDao dataBaseDao = new DataBaseDao(getContext());
                ArrayList<InterfaceBean> beans = dataBaseDao.queryAll();
                Log.e("本地数据库数据", beans.toString());
                if (beans != null && beans.size() > 0)
                    interfaceDialog.setData(beans);
            }
        }).start();
    }

    /**
     * 显示DIY音频的dialog
     */
    void showSceneDialog() {
        if (sceneDialog == null) {
            sceneDialog = new SceneDialog(getContext(), "");
        }
        sceneDialog.setClickListener(clickListenerInterface);
        sceneDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                //获取data数据
                ArrayList<InterfaceBean> beans = new ArrayList<>();
                beans.add(new InterfaceBean("你好", 1));
                beans.add(new InterfaceBean("你多大了", 2));
                beans.add(new InterfaceBean("你是谁", 3));
                beans.add(new InterfaceBean("", 4));
                beans.add(new InterfaceBean("", 5));
                beans.add(new InterfaceBean("", 6));
//                if (beans != null && beans.size() > 0)
                sceneDialog.setData(beans);
            }
        }).start();
    }

    private ClickListenerInterface clickListenerInterface = new ClickListenerInterface() {
        @Override
        public void sendInterface(int id) {
            //界面控制
            L.i(TAG, "界面控制-->" + id);
            interfaceBytes = new byte[7];
            interfaceBytes[0] = (byte) 0xAA;
            interfaceBytes[1] = (byte) 0x02;
            interfaceBytes[2] = (byte) ((id & 0xff00) >> 8);//高位
            interfaceBytes[3] = (byte) (id & 0xff);//低位
            interfaceBytes[5] = (byte) (interfaceBytes[1] ^ interfaceBytes[2] ^ interfaceBytes[3] ^ interfaceBytes[4]);
            interfaceBytes[6] = (byte) 0xBB;
            isInterface = true;
        }

        @Override
        public void sendScene(int id) {
            //情景 DIY
            L.i(TAG, "情景 DIY-->" + id);
            controlBytes[3] |= (byte) (id << 3) | (controlBytes[3] & 0x07);
        }

        @Override
        public void sendRefreshData() {
            //发送获取界面数据
            L.i(TAG, "获取界面数据");
            been = new ArrayList<>();
            stringBuffer = new StringBuffer();
            getDataBytes = new byte[3];
            getDataBytes[0] = (byte) 0xAA;
            getDataBytes[1] = (byte) 0x03;
            getDataBytes[2] = (byte) 0xBB;
            isGetData = true;
        }

        @Override
        public void sendPersonSelect(String text) {
            voicer = text;
            SharedPreferences sp = getContext().getSharedPreferences("com.iflytek.setting", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            if (text.equals("henry") || text.equals("aiscatherine") || text.equals("catherine") || text.equals("vimary") || text.equals("aistom")) {
                editor.putString("iat_language_preference","en_us");
            }else if(text.equals("dalong") ||text.equals("xiaomei")) {
                editor.putString("iat_language_preference","cantonese");
            }else {
                editor.putString("iat_language_preference", "mandarin");
            }
            editor.commit();
            Log.i("WCJ",getContext().getSharedPreferences("com.iflytek.setting", Activity.MODE_PRIVATE).getString("iat_language_preference","wu"));
        }
    };

    /**
     * 挂断
     */
    void onHangUp() {
        mCallId = ((Activity) mView).getIntent().getStringExtra(ECDevice.CALLID);
        if (!TextUtils.isEmpty(mCallId)) {
            ECDevice.getECVoIPCallManager().releaseCall(mCallId);
//            exit();
        }
        mView.setGlViewVisable(false);
        mView.setRefuseVisable(true);
        mView.setChronometerVisible(true);
    }

    private String mCurrentCallId = "";
    private String mNumber = "15505398047";

    /**
     * 音频呼叫
     */
    void makeCall(ECVoIPCallManager.CallType callType) {

        //说明：mCurrentCallId如果返回空则代表呼叫失败，可能是参数错误引起。否则返回是一串数字，是当前通话的标识。
        mCurrentCallId = ECDevice.getECVoIPCallManager().makeCall(callType, mNumber);//17600738557//15554955416
        if (!"".equals(mCurrentCallId)) {
            mView.setGlViewVisable(true);
            mView.setBottomVisible(false);
            Intent callAction = new Intent();
            //视频
//            callAction.putExtra(ACTION_CALLBACKING, true);
            //是否正在通话
//            callAction.putExtra(Constant.ACTION_CALLBACK_CALL, true);
            callAction.putExtra(Constant.EXTRA_CALL_NAME, mNumber);
            callAction.putExtra(Constant.EXTRA_CALL_NUMBER, mNumber);
            callAction.putExtra(ECDevice.CALLTYPE, callType);
            callAction.putExtra(ECDevice.CALLID, mCurrentCallId);
            callAction.putExtra(Constant.EXTRA_OUTGOING_CALL, true);
        } else {
            showToast("发起失败");
        }
    }

    /**
     * 接听
     */

    void onAccept() {
        if (!TextUtils.isEmpty(mCallId)) {
            ECDevice.getECVoIPCallManager().acceptCall(mCallId);
            mView.setBottomVisible(false);
        }
    }

    /**
     * 拒绝
     */

    void onRefuse() {
        //拒绝呼入
        if (!TextUtils.isEmpty(mCurrentCallId)) {
            ECDevice.getECVoIPCallManager().rejectCall(mCurrentCallId, 6666);
        }
        mView.setGlViewVisable(false);
        mView.setRefuseVisable(true);
//        exit();
    }

    /**
     * 自由运动
     */
    void sendAutoAction() {
        String data;
        if (isAutoAction) {
            //执行开
            mView.setSportVisiable(true);
            data = "自由运动(关)";
            controlBytes[3] &= (byte) ~(1 << 1);
        } else {
            //执行关
            controlBytes[3] |= (byte) (1 << 1);

            data = "自由运动(开)";
            mView.setSportVisiable(false);
        }
//        mView.setAutoText(data);
        L.i(TAG, "自由运动-->" + data);
        isAutoAction = !isAutoAction;
    }

    /**
     * 语音识别的开启和关闭
     */
    void sendSpeech() {
        String data = "";
        if (isSpeech) {//执行开
            data = "语音识别(开)";
            mView.setVoicetVisiable(true);
            controlBytes[3] |= (byte) (1);
        } else {//执行关
            data = "语音识别(关)";
            mView.setVoicetVisiable(false);
            controlBytes[3] &= (byte) 0xfe;
        }
//        mView.setSpeechText(data);
        isSpeech = !isSpeech;
        L.i(TAG, "语音识别-->" + data);
    }

    /**
     * 发送纯文本信息
     */
//    void sendText() {
//        String text = mView.getEditText();
//        if (TextUtils.isEmpty(text))
//            return;
//        L.i(TAG, "纯文本信息-->" + text);
//        sendTextToByte(text);
//        isSendMsg = true;
//        mView.setEditText("");//清空文本框内容
//    }

    void sendAIUIText() {
        String text = mView.getEditText();
        if (TextUtils.isEmpty(text))
            return;
        L.i(TAG, "AIUI纯文本信息-->" + text + "repeat " + isRepeat);
        if (isRepeat) {
            SharedPreferences sp = getContext().getSharedPreferences("com.iflytek.setting", Activity.MODE_PRIVATE);
            String language = sp.getString("iat_language_preference", "mandarin");
            if(language.equals("en_us")){
                customControl.setFromQuestion(true);
                customControl.query(text, TranslateLanguage.LanguageType.EN, TranslateLanguage.LanguageType.ZH);
                customControl.setOnQueryListener(new CustomControl.QueryListener() {
                    @Override
                    public void onQueryBack(String result) {

                    }

                    @Override
                    public void translateBack(String translates) {
                        needTransalte = true;
                        nlpControl.startTextNlp(translates);
                    }
                });
            }else {
                needTransalte = false;
                nlpControl.startTextNlp(text);
            }
        } else {
            sendTextToByte(text);
            isSendMsg = true;
        }

        mView.setEditText("");//清空文本框内容
    }

    /**
     * 文本编辑 隐藏显示
     */

    void showInput(RelativeLayout relativeLayout) {
        if (isShowInput) {
            TranslateAnimation translateAnimation = new TranslateAnimation(0.1f, 0.0f, 0.1f, 0.0f);
            translateAnimation.setDuration(500);
            relativeLayout.startAnimation(translateAnimation);
            mView.setInputVisiable(true);
        } else {
            TranslateAnimation translateAnimation = new TranslateAnimation(0.1f, 900.0f, 0.1f, 0.0f);
            translateAnimation.setDuration(500);
            relativeLayout.startAnimation(translateAnimation);
            mView.setInputVisiable(false);
        }
        isShowInput = !isShowInput;
    }

    /**
     * 控制盘的显示与隐藏
     */
    void showControl(RelativeLayout relate_level) {
        if (!isAreLevelShowing) {
            MyAnimation.startAnimationsIn(relate_level, 800, getContext());
        } else {
            if (isAreLevelShowing) {
                MyAnimation.startAnimationsOut(relate_level, 800, 500, getContext());
            } else {
                MyAnimation.startAnimationsOut(relate_level, 800, 0, getContext());
            }
        }
        isAreLevelShowing = !isAreLevelShowing;
    }

    private void sendTextToByte(String text) {
        // 进行字符串的拼接
        text = voicer + "\0" + text;
        //  音库参数   \0   发的内容
        Log.e("WCJ", text);
        byte[] textBytes = text.getBytes();
        int length = textBytes.length;

        sendMsg = new byte[length + 5];
        int size = sendMsg.length;
        sendMsg[0] = (byte) 0xAA;
        sendMsg[1] = (byte) 0x04;
        sendMsg[size - 1] = (byte) 0xBB;

        sendMsg[2] = (byte) (length & 0xff);//高位

        sendMsg[size - 2] = (byte) (sendMsg[1] ^ sendMsg[2]);

        for (int i = 0; i < length; i++) {
            sendMsg[i + 3] = textBytes[i];
            sendMsg[size - 2] ^= sendMsg[i + 3];
        }
        mView.setVoiceText("点击开始");
        mView.setVoiceBack(false);
        isSpeak = !isSpeak;
    }

    /**
     * 控制运动
     *
     * @param nav 运动值 8个方向：1、上；2下；3、左；4、右；5、左上；6、左下；7、右上；8、右下;9、停止
     */
    @Override
    public void onNavAndSpeed(int nav) {
        L.i(TAG, "控制运动-->" + nav);
        controlBytes[2] = (byte) 0x00;
        controlBytes[3] &= (byte) ~(1 << 2);

        switch (nav) {
            case 7:
                controlBytes[2] = (byte) (1 << 7);
                break;
            case 6:
                controlBytes[2] = (byte) (1 << 6);
                break;
            case 5:
                controlBytes[2] = (byte) (1 << 5);
                break;
            case 4:
                controlBytes[2] = (byte) (1 << 4);
                break;
            case 3:
                controlBytes[2] = (byte) (1 << 3);
                break;
            case 2:
                controlBytes[2] = (byte) (1 << 2);
                break;
            case 1:
                controlBytes[2] = (byte) (1 << 1);
                break;
            case 0:
                controlBytes[2] = (byte) (1);
                break;
            case 9:
                controlBytes[2] = (byte) 0;
                break;
            default:
                break;
        }
    }

    /**
     * 发送语音
     */
    void startAsr() {
        if (isSpeak) {
            mView.setAsrLayoutVisible(false);
            mView.setAnimationVisible(false);//音量动画 关闭
            mView.setVoiceText("结束说话");
            mView.setVoiceBack(true);

            SharedPreferences sp = getContext().getSharedPreferences("com.iflytek.setting", Activity.MODE_PRIVATE);
            String language = sp.getString("iat_language_preference", "mandarin");
            if ("en_us".equals(language)){//英语
                //英语流程
                Log.i("WCJ", "你选择了英语，语言：" + language);
                if (customControl != null) {
                    if (isRepeat){//英语语义理解
                        //需要翻译成英语，进行自定义语义理解流程
                        needTransalte = true;
                    }else {//英语复读
                        //进行自定义语义理解流程
                        customControl.setRepeatEnglish(true);
                        customControl.setOnQueryListener(new CustomControl.QueryListener() {
                            @Override
                            public void onQueryBack(String result) {

                            }

                            @Override
                            public void translateBack(String translates) {
                                sendTextToByte(translates);
                                isSendMsg = true;
                            }
                        });
                    }
                    customControl.init();
                }

            }else if("cantonese".equals(language)){//粤语:先听写成汉语在进行复读或者语义理解
                Log.i("WCJ","你选择了粤语，语言："+language);
                if (customControl != null) {
                    customControl.setCantonese(true);
                    customControl.setOnQueryListener(new CustomControl.QueryListener() {
                        @Override
                        public void onQueryBack(String result) {
                            if (isRepeat) {//语义理解
                                needTransalte = false;
                                nlpControl.startTextNlp(result);
                            } else {//复读
                                sendTextToByte(result);
                                isSendMsg = true;
                            }
                        }

                        @Override
                        public void translateBack(String translates) {
                        }
                    });
                    customControl.init();
                }
            } else {
                Log.i("WCJ","你选择了中文，语言："+language);
                if (nlpControl != null) {
                    needTransalte = false;
                    nlpControl.startVoiceNlp();
                }
            }
        } else {
            nlpControl.stopVoiceNlp();
            mView.setVoiceText("点击开始");
            mView.setVoiceBack(false);
        }
        isSpeak = !isSpeak;

//            if (mAsr != null)
//                mAsr.startAsr();
    }

    /**
     * 音频权限
     */
    public void audioPermission() {
        if (PackageManager.PERMISSION_GRANTED == ContextCompat.
                checkSelfPermission(getContext(), android.Manifest.permission.RECORD_AUDIO)) {
            startAsr();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //提示用户开户权限音频
                String[] perms = {"android.permission.RECORD_AUDIO"};
                ActivityCompat.requestPermissions((Activity) mView, perms, RESULT_CODE_STARTAUDIO);
            }
        }

    }

    //本地视频显示
    void localViewVisible() {
        if (isVisiable) {
            mView.setLocalViewVisiable(true);
        } else {
            mView.setLocalViewVisiable(false);
        }
        isVisiable = !isVisiable;

    }

    /**
     * 切换语音识别和文本输入
     */
    void changeSpeech() {
        if (isSpeechInput) {
            //隐藏
            mView.setLayoutVisible(true);
        } else {
            //显示
            mView.setLayoutVisible(false);
        }

        isSpeechInput = !isSpeechInput;

    }

    /**
     * 发送语义理解的answer
     */
    void sendAsrResult() {
        if (!TextUtils.isEmpty(asrAnswer)) {
            sendTextToByte(asrAnswer);
            isSendMsg = true;
            mView.setAsrLayoutVisible(false);
        }
    }

    /**
     * 复读
     */
    void doRepear() {
        finalText = "";
        if (isRepeat) {
            mView.setRepeatShow(false);
        } else {
            mView.setRepeatShow(true);
        }
        isRepeat = !isRepeat;
    }

    /**
     * 重新连接机器
     */
    void reLink() {
        if (UdpControl.getInstance().isGetTcpIp) {
            isCanSend = false;
            //重置数据
            resetData();
            udpControl.sendUdpSocketToByIp(application, client);
        } else {
            showToast("当前未连接机器人");
        }
    }

    private int selectedNum = 0;

    /**
     * 选择发音人
     */
    void showPersonSelectDialog() {
        if (selectDialog == null) {
            selectDialog = new PersonSelectDialog(getContext(), "");
        }
        selectDialog.setClickListener(clickListenerInterface);
        selectDialog.show();
    }

    private void resetData() {
        controlBytes = new byte[7];
        controlBytes[0] = (byte) 0xAA;
        controlBytes[1] = (byte) 0x01;
//        controlBytes[3] &= (byte) ~(1);
        controlBytes[3] &= (byte) 0xfe;
        controlBytes[6] = (byte) 0xBB;
    }

    public interface ClickListenerInterface {
        void sendInterface(int id);

        void sendScene(int id);

        void sendRefreshData();

        void sendPersonSelect(String text);
    }

    public interface OnListenerUDPServer {
        void receiver(String receiver);

        void acquireIp(boolean isAcquire);
    }

    /**
     * 初始化界面
     * 如果视频呼叫，则在接受呼叫之前，需要先设置视频通话显示的view
     * localView本地显示视频的view
     * view 显示远端视频的surfaceview
     */
    void attachGlView(ECOpenGlView localView, ECOpenGlView remoteView) {
        ECVoIPSetupManager setupManager = ECDevice.getECVoIPSetupManager();
        if (setupManager == null) {
            return;
        }
        setupManager.setGlDisplayWindow(localView, remoteView);
    }

    @Override
    public void onResume() {
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
                    L.e("key", "onVideoRatioChanged");
                    if (videoRatio == null) {
                        return;
                    }
                    int width = videoRatio.getWidth();
                    int height = videoRatio.getHeight();
                    if (width == 0 || height == 0) {
                        L.e("key", "invalid video width(" + width + ") or height(" + height + ")");
                        return;
                    }
                    if (width > height) {
                        DisplayMetrics dm = new DisplayMetrics();
                        ((Activity) mView).getWindowManager().getDefaultDisplay().getMetrics(dm);
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
                    L.e("key", "onSwitchCallMediaTypeRequest");
                }

                @Override
                public void onSwitchCallMediaTypeResponse(String s, ECVoIPCallManager.CallType callType) {
                    L.e("key", "onSwitchCallMediaTypeResponse");
                }

                @Override
                public void onDtmfReceived(String s, char c) {
                    L.e("key", "onDtmfReceived");
                }

                @Override
                public void onCallEvents(ECVoIPCallManager.VoIPCall voipCall) {
                    L.e("key", "onCallEvents");
                    // 处理呼叫事件回调
                    if (voipCall == null) {
                        L.e("key", "handle call event error , voipCall null");
                        return;
                    }
                    // 根据不同的事件通知类型来处理不同的业务
                    ECVoIPCallManager.ECCallState callState = voipCall.callState;
                    switch (callState) {
                        case ECCALL_PROCEEDING:
                            L.e("key", "正在连接服务器处理呼叫请求，callid：" + voipCall.callId);
                            ECDevice.getECVoIPSetupManager().enableLoudSpeaker(true);
                            mHandler.sendEmptyMessage(1000);
                            break;
                        case ECCALL_ALERTING:
                            L.e("key", "呼叫到达对方，正在振铃，callid：" + voipCall.callId);
                            mHandler.sendEmptyMessage(1001);
                            break;
                        case ECCALL_ANSWERED:
                            mHandler.sendEmptyMessage(1002);
                            L.e("key", "对方接听本次呼叫,callid：" + voipCall.callId);
                            break;
                        case ECCALL_FAILED:
                            // 本次呼叫失败，根据失败原因进行业务处理或跳转
                            L.e("key", "called:" + voipCall.callId + ",reason:" + voipCall.reason);
                            faild_reason = voipCall.reason;
                            mHandler.sendEmptyMessage(1003);
                            break;
                        case ECCALL_RELEASED:
                            mHandler.sendEmptyMessage(1004);
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

    /**
     * AIUI 回调
     */
    private AIUIListener mAIUIListener = new AIUIListener() {

        @Override
        public void onEvent(AIUIEvent event) {
            switch (event.eventType) {
                case AIUIConstant.EVENT_WAKEUP:
                    Log.i(TAG, "on event: " + event.eventType);
                    showTip("进入识别状态");
                    L.i("GG", "on event: " + event.eventType);
                    break;

                case AIUIConstant.EVENT_RESULT: {
//                    Log.i( TAG,  "on event: "+ event.eventType );
                    boolean isSend = false;
                    try {
                        JSONObject bizParamJson = new JSONObject(event.info);
                        JSONObject data = bizParamJson.getJSONArray("data").getJSONObject(0);
                        JSONObject params = data.getJSONObject("params");
                        JSONObject content = data.getJSONArray("content").getJSONObject(0);

                        if (content.has("cnt_id")) {
                            String cnt_id = content.getString("cnt_id");
                            JSONObject cntJson = new JSONObject(new String(event.data.getByteArray(cnt_id), "utf-8"));

                            String sub = params.optString("sub");
                            if ("nlp".equals(sub)) {

                                String resultStr = cntJson.optString("intent");
//                                Log.i( TAG+"语义理解：", "语义理解"+resultStr );
                                JSONObject jsonObject = new JSONObject(resultStr);
                                L.i(TAG + "answer==========>", resultStr);
                                L.e("GG", resultStr.length() + "'");
                                if (resultStr != null && resultStr.length() > 3) {
                                    //语义分析
                                    speechAnalysis(jsonObject);
                                }
                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }

                }
                break;

                case AIUIConstant.EVENT_ERROR: {
                    Log.i(TAG, "on event: " + event.eventType);
                }
                break;

                case AIUIConstant.EVENT_VAD: {
                    if (AIUIConstant.VAD_BOS == event.arg1) {
                        showTip("找到vad_bos");
                        L.i("GG", "on 找到vad_bos: ");
                    } else if (AIUIConstant.VAD_EOS == event.arg1) {
                        showTip("找到vad_eos");
                        L.i("GG", "on 找到vad_eos: ");
                    } else {
                        showTip("" + event.arg2);
                        L.i("GG", "event.arg2");
                    }
                }
                break;

                case AIUIConstant.EVENT_START_RECORD: {
                    Log.i(TAG, "on event: " + event.eventType);
//                    mView.setAnimationVisible(true);
//                    mView.getVoiceView().setSignalEMA((int) Math.random() * 10 + 1);
                    showTip("开始录音");
                    L.i("GG", "开始录音");
                }
                break;

                case AIUIConstant.EVENT_STOP_RECORD: {
                    Log.i(TAG, "on event: " + event.eventType);
//                    mView.setAnimationVisible(false);
                    showTip("停止录音");
                    L.i("GG", "停止录音");
                    mView.setVoiceText("点击开始");
                    mView.setVoiceBack(false);
                }
                break;

                case AIUIConstant.EVENT_STATE: {    // 状态事件
                    mAIUIState = event.arg1;

                    if (AIUIConstant.STATE_IDLE == mAIUIState) {
                        // 闲置状态，AIUI未开启
                        showTip("STATE_IDLE");
                    } else if (AIUIConstant.STATE_READY == mAIUIState) {
                        // AIUI已就绪，等待唤醒
                        showTip("STATE_READY");
                    } else if (AIUIConstant.STATE_WORKING == mAIUIState) {
                        // AIUI工作中，可进行交互
                        showTip("STATE_WORKING");
                        L.i("GG", "STATE_WORKING");
                    }
                }
                break;

                case AIUIConstant.EVENT_CMD_RETURN: {
                    if (AIUIConstant.CMD_UPLOAD_LEXICON == event.arg1) {
                        showTip("上传" + (0 == event.arg2 ? "成功" : "失败"));
                    }
                }
                break;

                default:
                    break;
            }
        }

    };

    /**
     * 将AIUI监听中返回的结果提出来
     * @param jsonObject
     * @throws JSONException
     */
    private void speechAnalysis(JSONObject jsonObject) throws JSONException {
        if (jsonObject.has("answer")) {
            //被语音语义识别，返回结果
            JSONObject answerObj = jsonObject.getJSONObject("answer");
            if(isRepeat){//有结果——不复读
                finalText = answerObj.optString("text");
                Log.e(TAG, "方法 发送给机器人");
                if (needTransalte) {//英文——语义理解
                    customControl.setOnlyOne(false);
                    customControl.setTranslateSource(true);
                    customControl.query(finalText, TranslateLanguage.LanguageType.ZH, TranslateLanguage.LanguageType.EN);
                    customControl.setOnQueryListener(new CustomControl.QueryListener() {
                        @Override
                        public void onQueryBack(String result) {
                            sendTextToByte(result);
                            isSendMsg = true;
                            Toast.makeText(getContext(), "已发送" + result, Toast.LENGTH_SHORT).show();
                            Log.i(TAG+"WCJ英文语音语义结果","已发送" + result);
                        }

                        @Override
                        public void translateBack(String translates) {

                        }
                    });
                }else {//中文——语义理解
                    sendTextToByte(finalText);
                    isSendMsg = true;
                    Toast.makeText(getContext(), "已发送" + finalText, Toast.LENGTH_SHORT).show();
                    Log.i(TAG+"WCJ中文语音语义结果","已发送" + finalText);
                }
            }else {//有结果——复读
                JSONObject question = answerObj.optJSONObject("question");
                String repeatText = question.optString("q");
                if (needTransalte) {
                    customControl.setFromQuestion(true);
                    customControl.query(repeatText, TranslateLanguage.LanguageType.ZH, TranslateLanguage.LanguageType.EN);
                    customControl.setOnQueryListener(new CustomControl.QueryListener() {
                        @Override
                        public void onQueryBack(String result) {

                        }

                        @Override
                        public void translateBack(String translates) {
                            sendTextToByte(translates);
                            isSendMsg = true;
                        }
                    });
                }else {
                    sendTextToByte(repeatText);
                    isSendMsg = true;
                }
            }
        } else if (jsonObject.has("rc") && "4".equals(jsonObject.getString("rc"))) {
            //不能返回结果
            if (isRepeat){//无结果——不复读
                //随机在结果集中找出一个结果输出
                String[] arrResult= getContext().getResources().getStringArray(R.array.no_result);
                finalText = arrResult[new Random().nextInt(arrResult.length)];
                if (needTransalte) {//英文语义理解——无结果
                    customControl.setOnlyOne(false);
                    customControl.setTranslateSource(true);
                    customControl.query(finalText, TranslateLanguage.LanguageType.ZH, TranslateLanguage.LanguageType.EN);
                    customControl.setOnQueryListener(new CustomControl.QueryListener() {
                        @Override
                        public void onQueryBack(String result) {
                            sendTextToByte(result);
                            isSendMsg = true;
                            Toast.makeText(getContext(), "已发送" + result, Toast.LENGTH_SHORT).show();
                            Log.i(TAG+"WCJ英文语音语义无结果","已发送" + result);
                        }
                        @Override
                        public void translateBack(String translates) {

                        }
                    });
                }else {//中文语义理解——无结果
                    sendTextToByte(finalText);
                    isSendMsg = true;
                    Toast.makeText(getContext(), "已发送" + finalText, Toast.LENGTH_SHORT).show();
                    Log.i(TAG+"WCJ中文语音语义无结果","已发送" + finalText);
                }
            }else {//无结果——复读
                String repeatStr = jsonObject.optString("text");
                sendTextToByte(repeatStr);
                isSendMsg = true;
            }
        }
    }

    private void showTip(String tip) {
//        Toast.makeText(application,tip,Toast.LENGTH_SHORT).show();
    }

    /**
     * 权限回调
     */
    @Override
    protected void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RESULT_CODE_STARTAUDIO:
                boolean albumAccepted_audio = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (!albumAccepted_audio) {
                    Toast.makeText(getContext(), "请开启应用音频权限", Toast.LENGTH_LONG).show();
                } else {
                    startAsr();
                }
                break;
        }
    }

    /**
     * 选择语言类型:中文，粤语，英文
     */
//    public void showLanguageType() {
//        final String[] language_entries = getContext().getResources().getStringArray(R.array.language_entries);
//        final String[] language_values = getContext().getResources().getStringArray(R.array.language_values);
//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
//                .setTitle(R.string.language_type)
//                .setItems(language_entries, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        SharedPreferences sp = getContext().getSharedPreferences("com.iflytek.setting", Activity.MODE_PRIVATE);
//                        SharedPreferences.Editor editor = sp.edit();
//                        switch (i){
//                            case 0:
//                                editor.putString("iat_language_preference",language_values[0]);
//                                break;
//                            case 1:
//                                editor.putString("iat_language_preference",language_values[1]);
//                                break;
//                            case 2:
//                                editor.putString("iat_language_preference",language_values[2]);
//                                break;
//                        }
//                        editor.commit();
//                        Log.i("WCJ",getContext().getSharedPreferences("com.iflytek.setting", Activity.MODE_PRIVATE).getString("iat_language_preference","wu"));
//                        fullScreen();
//                    }
//                });
//        builder.show();
//    }

}
