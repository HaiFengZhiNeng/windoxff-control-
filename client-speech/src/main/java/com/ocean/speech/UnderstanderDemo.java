package com.ocean.speech;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUnderstander;
import com.iflytek.cloud.SpeechUnderstanderListener;
import com.iflytek.cloud.UnderstanderResult;
import com.iflytek.cloud.util.ResourceUtil;
import com.ocean.mvp.library.utils.L;
import com.ocean.speech.util.FucUtil;

import org.json.JSONObject;

import static android.content.ContentValues.TAG;

/**
 * Created by zhangyuanyuan on 2017/7/7.
 */

public class UnderstanderDemo extends Activity implements View.OnClickListener {

    // 语义理解对象（语音到语义）。
    private SpeechUnderstander mSpeechUnderstander;





    private String[] mCloudVoicersEntries;
    private String[] mCloudVoicersValue ;

    private String mEngineType = SpeechConstant.TYPE_LOCAL;
    private static final String GRAMMAR_TYPE_BNF = "bnf";

    private TextView mResult;

    private String mLocalGrammar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_understander);
        mResult = (TextView) findViewById(R.id.understander_text);

        findViewById(R.id.understander).setOnClickListener(this);

        /**
         * 申请的appid时，我们为开发者开通了开放语义（语义理解）
         * 由于语义理解的场景繁多，需开发自己去开放语义平台：http://www.xfyun.cn/services/osp
         * 配置相应的语音场景，才能使用语义理解，否则文本理解将不能使用，语义理解将返回听写结果。
         */
        // 初始化对象
        mSpeechUnderstander = SpeechUnderstander.createUnderstander(UnderstanderDemo.this, mSpeechUdrInitListener);

        // 云端发音人名称列表
        mCloudVoicersEntries = getResources().getStringArray(R.array.voicer_cloud_entries);
        mCloudVoicersValue = getResources().getStringArray(R.array.voicer_cloud_values);

        mLocalGrammar = FucUtil.readFile(this, "call.bnf", "utf-8");

    }

    private InitListener mSpeechUdrInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            L.d(TAG, "speechUnderstanderListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                L.d(TAG, "初始化失败,错误码：" + code);
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.understander:
                // 设置参数
                setParam();

                if (mSpeechUnderstander.isUnderstanding()) {// 开始前检查状态
                    mSpeechUnderstander.stopUnderstanding();
                    L.d(TAG, "停止录音");
                } else {
                    int ret = mSpeechUnderstander.startUnderstanding(mSpeechUnderstanderListener);
                    if (ret != 0) {
                        L.d(TAG, "语义理解失败,错误码:" + ret);
                    }/*else {
                        L.d(TAG,getString(R.string.text_begin));
                    }*/
                }
                break;
        }
    }

    private void setParam() {
/*
        String mContent = new String(mLocalGrammar);
        mSpeechUnderstander.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");


        //设置识别引擎
        mSpeechUnderstander.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        //设置返回结果json格式
        mSpeechUnderstander.setParameter(SpeechConstant.RESULT_TYPE, "json");

        // 设置资源路径
        mSpeechUnderstander.setParameter(ResourceUtil.ASR_RES_PATH, getResourcePath());

        // 设置语法构建路径
        mSpeechUnderstander.setParameter(ResourceUtil.GRM_BUILD_PATH, Environment.getExternalStorageDirectory() + "/grmPath");

//        int ret = mSpeechUnderstander.buildGrammar(GRAMMAR_TYPE_BNF, mContent, mLocalGrammarListener);



        mSpeechUnderstander.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "false");
        //设置本地识别使用语法id
        mSpeechUnderstander.setParameter(SpeechConstant.LOCAL_GRAMMAR, "call");


        //设置识别引擎
        mSpeechUnderstander.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        //设置本地识别的门限值
        mSpeechUnderstander.setParameter(SpeechConstant.ASR_THRESHOLD, "30");*/


        /*if (lang.equals("en_us")) {
            // 设置语言
            mSpeechUnderstander.setParameter(SpeechConstant.LANGUAGE, "en_us");
            mSpeechUnderstander.setParameter(SpeechConstant.ACCENT, null);
        }else {*/
        // 设置语言
        mSpeechUnderstander.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        // 设置语言区域
        mSpeechUnderstander.setParameter(SpeechConstant.ACCENT,  "mandarin");
//        }
        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mSpeechUnderstander.setParameter(SpeechConstant.VAD_BOS, "4000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mSpeechUnderstander.setParameter(SpeechConstant.VAD_EOS, "1000");

        // 设置标点符号，默认：1（有标点）
        mSpeechUnderstander.setParameter(SpeechConstant.ASR_PTT, "1");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mSpeechUnderstander.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mSpeechUnderstander.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/sud.wav");

        // 设置语义情景
        //mSpeechUnderstander.setParameter(SpeechConstant.SCENE, "main");
    }


    // 获取识别资源路径
    private String getResourcePath() {

//        StringBuffer tempBuffer = new StringBuffer();
//        //识别通用资源
//        tempBuffer.append(ResourceUtil.generateResourcePath(mContext, ResourceUtil.RESOURCE_TYPE.assets, "asr/common.jet"));
        //识别8k资源-使用8k的时候请解开注释
//		tempBuffer.append(";");
//		tempBuffer.append(ResourceUtil.generateResourcePath(this, RESOURCE_TYPE.assets, "asr/common_8k.jet"));
//        return tempBuffer.toString();
        return ResourceUtil.generateResourcePath(this, ResourceUtil.RESOURCE_TYPE.assets, "asr/common.jet");
    }



    public static final String errorTip = "请确认是否有在 aiui.xfyun.cn 配置语义。（另外，已开通语义，但从1115（含1115）以前的SDK更新到1116以上版本SDK后，语义需要重新到 aiui.xfyun.cn 配置）";
    private SpeechUnderstanderListener mSpeechUnderstanderListener = new SpeechUnderstanderListener() {
        @Override
        public void onResult(final UnderstanderResult result) {
            if (null != result) {
                L.d(TAG, result.getResultString());

                // 显示
                String text = result.getResultString();
                if (!TextUtils.isEmpty(text)) {
                    mResult.setText(text);
                    if( 0 != getResultError(text) ){
                        L.d(TAG, errorTip );
                    }
                }
            } else {
                L.d(TAG,"识别结果不正确。");
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            L.d(TAG,"当前正在说话，音量大小：" + volume);
            L.d(TAG, data.length+"");
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            L.d(TAG,"结束说话");
        }

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            L.d(TAG,"开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            if( error.getErrorCode() == ErrorCode.MSP_ERROR_NO_DATA ){
                L.d(TAG,error.getPlainDescription(true) );
            }else{
                L.d(TAG,error.getPlainDescription(true)+", "+errorTip);
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };

    private int getResultError( final String resultText ){
        int error = 0;
        try{
            final String KEY_ERROR = "error";
            final String KEY_CODE = "code";
            final JSONObject joResult = new JSONObject( resultText );
            final JSONObject joError = joResult.optJSONObject( KEY_ERROR );
            if( null!=joError ){
                error = joError.optInt( KEY_CODE );
            }
        }catch( Throwable e ){
            e.printStackTrace();
        }//end of try-catch

        return error;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if( null != mSpeechUnderstander ){
            // 退出时释放连接
            mSpeechUnderstander.cancel();
            mSpeechUnderstander.destroy();
        }
    }
}
