package com.ocean.speech.asr;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.GrammarListener;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.util.ResourceUtil;
import com.ocean.mvp.library.utils.L;
import com.ocean.speech.SpeechApplication;
import com.ocean.speech.util.FucUtil;

import java.lang.ref.WeakReference;
import java.util.HashMap;

/**
 * Created by zhangyuanyuan on 2017/7/5.
 */

public class AsrControl {

    private String TAG = "AsrControl";

    private SpeechApplication mContext;

    // 语音识别对象
    private SpeechRecognizer mAsr;

    private OnRecognizerListener onRecognizerListener;

    private WeakRefHandler mHandler;

    private AsrJsonParse jsonParse;

    private HashMap<String, Object> resultMap = null;
    /**
     * 构造方法
     *
     * @param mContext 上下文
     */
    public AsrControl(SpeechApplication mContext) {
        this.mContext = mContext;
        mHandler = new WeakRefHandler(this);
        jsonParse = AsrJsonParse.getInstance(mContext);
    }

    // 本地语法文件
    private String mLocalGrammar = null;
    // 本地词典
    private String mLocalLexicon = null;
    // 云端语法文件
    private String mCloudGrammar = null;

    private String mEngineType = SpeechConstant.TYPE_MIX;
    //    private String mEngineType = SpeechConstant.TYPE_LOCAL;
    private SharedPreferences mSharedPreferences = null;

    private static final String GRAMMAR_TYPE_BNF = "bnf";

    public void setOnRecognizerListener(OnRecognizerListener onRecognizerListener) {
        this.onRecognizerListener = onRecognizerListener;
    }

    /**
     * 设置讯飞数据
     */
    public void init() {
        // 初始化识别对象
        mAsr = SpeechRecognizer.createRecognizer(mContext, mInitListener);
        mLocalGrammar = FucUtil.readFile(mContext, "call.bnf", "utf-8");
        buildGrammar();
    }

    /**
     * 构建语法
     */
    private void buildGrammar() {
        if (mAsr == null) return;

        mEngineType = SpeechConstant.TYPE_LOCAL;//设置引擎类型

        //mAsr.setParameter(SpeechConstant.PARAMS, null); //清空参数
        mAsr.setParameter(SpeechConstant.KEY_SPEECH_TIMEOUT, "20000");//会话超时时间
        mAsr.setParameter(SpeechConstant.RESULT_TYPE, "json");// 设置返回结果格式
        mAsr.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");   // 设置文本编码格式
        mAsr.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);// 设置引擎类型
        mAsr.setParameter(SpeechConstant.MFV_SCENES, "main2");// 设置语义情景
        // 设置本地资源路径
        mAsr.setParameter(ResourceUtil.ASR_RES_PATH, getResourcePath());

        // 设置语法构建路径
        mAsr.setParameter(ResourceUtil.GRM_BUILD_PATH, Environment.getExternalStorageDirectory() + "/grmPath");
        mAsr.setParameter(SpeechConstant.LOCAL_GRAMMAR, "call"); // 设置本地识别使用语法id
        mLocalGrammar = FucUtil.readFile(mContext, "call.bnf", "utf-8");
        String mContent = new String(mLocalGrammar);// 语法、词典临时变量

        int ret = mAsr.buildGrammar(GRAMMAR_TYPE_BNF, mContent, mLocalGrammarListener);

        if (ret != ErrorCode.SUCCESS) {
            L.e(TAG, "语法构建失败，错误码：" + ret);
        }
    }

    /**
     * 设置参数
     */
    private void setParam() {
        mAsr.setParameter(SpeechConstant.ENGINE_TYPE, "mix");
        mAsr.setParameter("asr_sch", "1");//是否进行语义识别
        mAsr.setParameter(SpeechConstant.NLP_VERSION, "3.0");//通过此参数，设置开放语义协议版本号。
        mAsr.setParameter(SpeechConstant.RESULT_TYPE, "json");//返回文本结果类型
        mAsr.setParameter("mixed_type", "realtime");//混合模式的类型
        mAsr.setParameter(SpeechConstant.MIXED_TIMEOUT, "2000");//在线结果超时控制. 0-30000, def:2000
        mAsr.setParameter(SpeechConstant.MIXED_THRESHOLD, "30");//离线结果混合门限. 0-100, def:60
        mAsr.setParameter(SpeechConstant.ASR_THRESHOLD, "10");//离线结果识别门限0-100, default:0
        mAsr.setParameter("local_prior", "1");


        // 设置语言
        mAsr.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        // 设置语言区域
        mAsr.setParameter(SpeechConstant.ACCENT, "mandarin");
        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mAsr.setParameter(SpeechConstant.VAD_BOS, "4000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mAsr.setParameter(SpeechConstant.VAD_EOS, "1000");

        // 设置标点符号，默认：1（有标点）
        mAsr.setParameter(SpeechConstant.ASR_PTT,  "1");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mAsr.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mAsr.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/sud.wav");

        // 设置语义情景
        mAsr.setParameter(SpeechConstant.MFV_SCENES, "main");
    }

    /**
     * 开始语音识别
     */
    public void startAsr() {
        setParam();
        resultMap = null;
        int ret = mAsr.startListening(mRecognizerListener);
        if (ret != ErrorCode.SUCCESS) {
            L.e(TAG, "识别失败，错误码：" + ret);
        }
    }

    /**
     * 停止语音识别
     */
    public void stopAsr() {
        if (mAsr != null)
        mAsr.stopListening();
    }

    // 获取识别资源路径
    private String getResourcePath() {
        return ResourceUtil.generateResourcePath(mContext, ResourceUtil.RESOURCE_TYPE.assets, "asr/common.jet");
    }

    private InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int i) {
            if (i != ErrorCode.SUCCESS) {
                L.e(TAG, "初始化失败,错误码：" + i);
            }
        }
    };

    private GrammarListener mLocalGrammarListener = new GrammarListener() {
        @Override
        public void onBuildFinish(String grammarId, SpeechError speechError) {
            if (speechError == null) {
                L.e(TAG, "语法构建成功：" + grammarId);
            } else {
                L.e(TAG, "语法构建失败,错误码：" + speechError.getErrorCode());
                
            }
        }
    };

    private RecognizerListener mRecognizerListener = new RecognizerListener() {
        @Override
        public void onVolumeChanged(int i, byte[] bytes) {
            L.e(TAG, "当前音量：" + i);
            if (onRecognizerListener != null)
                onRecognizerListener.onVolumeChange(i);
        }

        @Override
        public void onBeginOfSpeech() {
            L.e(TAG, "开始说话");
        }

        @Override
        public void onEndOfSpeech() {
            L.e(TAG, "结束说话");
            if (onRecognizerListener != null)
                onRecognizerListener.onEndOfSpeech();
        }

        @Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
            L.e(TAG, "识别到结果");
            if (b) {
                if (null != recognizerResult && !TextUtils.isEmpty(recognizerResult.getResultString())) {
                    L.i(TAG,recognizerResult.getResultString());
                    //返回结果
                    mHandler.sendMessage(mHandler.obtainMessage(1, recognizerResult.getResultString()));
                } else {
                    mHandler.sendMessage(mHandler.obtainMessage(1, null));
                }
            }
        }

        @Override
        public void onError(SpeechError speechError) {
            L.e(TAG, "onError Code:" + speechError.getErrorCode());
        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };

    private AsrJsonParse.OnResultCallback callback = new AsrJsonParse.OnResultCallback() {
        @Override
        public void onCallBack(long tag, HashMap<String, Object> value) {
            resultMap = value;
            mHandler.sendEmptyMessage(2);
        }
    };


    public class WeakRefHandler extends Handler {
        private WeakReference<AsrControl> ref = null;


        public WeakRefHandler(AsrControl activity) {
            ref = new WeakReference<AsrControl>(activity);
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }


        @Override
        public void handleMessage(Message msg) {
            if (ref.get() != null) {
                switch (msg.what) {
                    case 1:
                        //接收到结果
                        if (onRecognizerListener != null){
                            if (msg.obj != null){
                                long tag = System.currentTimeMillis();
                                jsonParse.parseUnderResult((String) msg.obj,tag,callback);
                            }
                        }else {
                            sendEmptyMessage(2);
                        }
                        break;
                    case 2:
                        //返回结果
                        if (onRecognizerListener != null)
                            onRecognizerListener.onResult(resultMap);
                        break;
                    case 3:
                        sendEmptyMessage(2);
                        break;
                }
            }
            super.handleMessage(msg);
        }
    }

    public interface OnRecognizerListener{
        void onResult(HashMap<String, Object> result);

        void onVolumeChange(int volume);

        void onEndOfSpeech();
    }

}
