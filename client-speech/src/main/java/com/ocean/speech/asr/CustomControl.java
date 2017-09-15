package com.ocean.speech.asr;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.iflytek.aiui.AIUIAgent;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIListener;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.thirdparty.Q;
import com.ocean.speech.util.JsonParser;
import com.ocean.speech.youdao.TranslateData;
import com.ocean.speech.youdao.TranslateLanguage;
import com.youdao.sdk.app.Language;
import com.youdao.sdk.app.LanguageUtils;
import com.youdao.sdk.ydonlinetranslate.TranslateErrorCode;
import com.youdao.sdk.ydonlinetranslate.TranslateListener;
import com.youdao.sdk.ydonlinetranslate.TranslateParameters;
import com.youdao.sdk.ydonlinetranslate.Translator;
import com.youdao.sdk.ydtranslate.Translate;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * 英语语义理解
 */
public class CustomControl {
    private static String TAG = "WCJ_Custom";

    // 语音听写对象
    private SpeechRecognizer mIat;

    //英语语音文本
    private String englishText;
    //英语复读标识
    private boolean repeatEnglish = false;
    public boolean isRepeatEnglish() {
        return repeatEnglish;
    }
    public void setRepeatEnglish(boolean repeatEnglish) {
        this.repeatEnglish = repeatEnglish;
    }

    //粤语听写成文本
    private boolean cantonese =false;
    public boolean isCantonese() {
        return cantonese;
    }
    public void setCantonese(boolean cantonese) {
        this.cantonese = cantonese;
    }

    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();

    private SharedPreferences mSharedPreferences;
    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;

    //有道翻译只需要该对象
    private Translator translator;

    //语义理解
    private AIUIAgent mAIUIAgent = null;
    private NlpControl nlpControler;
    private int mAIUIState = AIUIConstant.STATE_IDLE;
    String finalText = "";//语义理返回的结果

    //点一次按钮，翻译完成后走语义理解，第二次回来不进语义理解，不然陷入死循环
    private boolean onlyOne = false;
    public boolean isOnlyOne() {
        return onlyOne;
    }
    public void setOnlyOne(boolean onlyOne) {
        this.onlyOne = onlyOne;
    }

    //判断是否是经过语义理解而来的
    private boolean translateSource = false;
    public boolean isTranslateSource() {
        return translateSource;
    }
    public void setTranslateSource(boolean translateSource) {
        this.translateSource = translateSource;
    }

    /*
	从这儿开始定义合成
	 */
    // 语音合成对象
    private SpeechSynthesizer mTts;
    // 默认发音人
    private String voicer = "catherine";

    // 缓冲进度
    private int mPercentForBuffering = 0;
    // 播放进度
    private int mPercentForPlaying = 0;

    private long time = 0;

    private Context context;

    private AIUIListener mAIUIListener = null;
    public void setmAIUIListener(AIUIListener listener){
        this.mAIUIListener = listener;
    }

    public boolean isFromQuestion() {
        return fromQuestion;
    }
    public void setFromQuestion(boolean fromQuestion) {
        this.fromQuestion = fromQuestion;
    }

    private boolean fromQuestion;
    

    public interface QueryListener {
        void onQueryBack(String result);

        void translateBack(String translates);
    }
    private QueryListener queryListener;
    public void setOnQueryListener(QueryListener queryListener){
        this.queryListener = queryListener;
    }

    public CustomControl(Context context) {
        this.context = context;
        // 初始化识别无UI识别对象
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(context, mInitListener);

        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(context, mTtsInitListener);

        mSharedPreferences = context.getSharedPreferences("com.iflytek.setting", Activity.MODE_PRIVATE);
        nlpControler = new NlpControl(context);
        nlpControler.setmAIUIListener(mAIUIListener);
        nlpControler.init();
        if (null == mIat) {
            // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
            Toast.makeText(context,"创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化",Toast.LENGTH_SHORT).show();
            return;
        }
    }



    public void init() {
        mEngineType = SpeechConstant.TYPE_CLOUD;
        setOnlyOne(true);
        // 设置参数
        setParam();
        // 不显示听写对话框
        ret = mIat.startListening(mRecognizerListener);
        if (ret != ErrorCode.SUCCESS) {
            Toast.makeText(context,"听写失败,错误码：" + ret,Toast.LENGTH_SHORT).show();
        } else {
            Log.i(TAG,"请开始说话...");
        }

    }


    int ret = 0; // 函数调用返回值


    /**
     * 语音听写——初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                Toast.makeText(context,"初始化失败，错误码：" + code,Toast.LENGTH_SHORT).show();
            }
        }
    };



    /**
     * 语音听写——监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            Log.i(TAG,"开始听写");
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            // 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
//            ToastUtils.show(CustomControl.this,error.getPlainDescription(true));
            Log.i(TAG,"听写错误");
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
//            ToastUtils.show(CustomControl.this,"结束说话");
            Log.i(TAG,"结束听写");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            Log.i(TAG, "语音听写结果"+results.getResultString());


            if (!isLast) {
                // TODO 最后的结果
                printResult(results);
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
//            ToastUtils.show(CustomControl.this,"当前正在说话，音量大小：" + volume);
//            Log.e(TAG, "返回音频数据：" + data.length);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };

    /**
     * 语音听写——解析最后的字符串
     * @param results
     */
    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        /**
         * resultBuffer对应的结果
         */
        Log.i(TAG, "进入英翻汉");
        if (isRepeatEnglish()) {//英语重复
            englishText = resultBuffer.toString();
            queryListener.translateBack(englishText);
            setRepeatEnglish(false);
        }else if(isCantonese()){//粤语听写
            queryListener.onQueryBack(resultBuffer.toString());
            setCantonese(false);
        }else {//英语语义理解
            query(resultBuffer.toString(), TranslateLanguage.LanguageType.EN, TranslateLanguage.LanguageType.ZH);
        }

    }



    /**
     * 语音听写——参数设置
     */
    public void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

        String lag = mSharedPreferences.getString("iat_language_preference",
                "mandarin");
        if (lag.equals("en_us")) {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
            mIat.setParameter(SpeechConstant.ACCENT, null);
        }else if (lag.equals("cantonese")) {
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            mIat.setParameter(SpeechConstant.ACCENT, "cantonese");
        } else {
                // 设置语言
                mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
                // 设置语言区域
                mIat.setParameter(SpeechConstant.ACCENT, lag);
        }

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "999999"));

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "0"));

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");
    }

    protected void onDestroy() {
        if (null != mIat) {
            // 退出时释放连接
            mIat.cancel();
            mIat.destroy();
        }
    }

    /**
     * 文本——语义理解
     * @param text
     */
    private void sendAIUIText(String text) {
        if (TextUtils.isEmpty(text))
            return;
        nlpControler.startTextNlp(text);
    }

    /**
     * 有道API——进行翻译
     * @param source    源文件
     * @param fromType   源语言类型
     * @param toType     目标语言类型
     * @return
     */
    public void query(final String source, TranslateLanguage.LanguageType fromType, TranslateLanguage.LanguageType toType) {
        Log.i(TAG, "语言： " + source);
        String from="",to="",input="";
        // 源语言或者目标语言其中之一必须为中文,目前只支持中文与其他几个语种的互译
        if (fromType == TranslateLanguage.LanguageType.EN && toType == TranslateLanguage.LanguageType.ZH){
            //英译中
            from = "英文";
            to = "中文";
        }else if(fromType == TranslateLanguage.LanguageType.ZH && toType == TranslateLanguage.LanguageType.EN){
            //中译英
            from = "中文";
            to = "英文";
        }else {
            Log.e(TAG,"暂时只支持中英互译");
            return ;
        }

        input = source;
        Language langFrom = LanguageUtils.getLangByName(from);
        Language langTo = LanguageUtils.getLangByName(to);

        TranslateParameters tps = new TranslateParameters.Builder()
                .source("youdao").from(langFrom).to(langTo).timeout(3000).build();// appkey可以省略

        translator = Translator.getInstance(tps);

        translator.lookup(input, new TranslateListener() {

            @Override
            public void onResult(Translate result, String input) {
                TranslateData td = new TranslateData(
                        System.currentTimeMillis(), result);
                Log.i(TAG,"输入" + input + " 翻译结果: "+td.translates());
                if (isOnlyOne()){
                    sendAIUIText(td.translates());
                    setOnlyOne(false);
                }else if (isTranslateSource()){
//                    gotoSpeak(td.translates());

                    Log.i("WCJ英语语义结果翻译结果","已经成功了" + td.translates()+"\n"+"============");
                    queryListener.onQueryBack(td.translates());
                    setTranslateSource(false); 
                }else  if(isFromQuestion()){
                    //英语问题翻译中文
                    queryListener.translateBack(td.translates());
                    setFromQuestion(false);
                }

            }

            @Override
            public void onError(TranslateErrorCode error) {
                Log.i(TAG,error.name());
            }
        });
    }


    /**
     * 语音合成操作
     */
    private void gotoSpeak(String showResult) {
        //				String text = ((EditText) findViewById(R.id.tts_text)).getText().toString();
        // 设置参数
        setSpeakParam();
        int code = mTts.startSpeaking(showResult, mTtsListener);
//			/**
//			 * 只保存音频不进行播放接口,调用此接口请注释startSpeaking接口
//			 * text:要合成的文本，uri:需要保存的音频全路径，listener:回调接口
//			*/
//			String path = Environment.getExternalStorageDirectory()+"/tts.pcm";
//			int code = mTts.synthesizeToUri(text, path, mTtsListener);

    }

    /**
     * 语音合成——参数设置
     * @return
     */
    private void setSpeakParam(){
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        // 根据合成引擎设置相应参数
        if(mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
            // 设置在线合成发音人
            mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
            //设置合成语速
            mTts.setParameter(SpeechConstant.SPEED, mSharedPreferences.getString("speed_preference", "45"));
            //设置合成音调
            mTts.setParameter(SpeechConstant.PITCH, mSharedPreferences.getString("pitch_preference", "50"));
            //设置合成音量
            mTts.setParameter(SpeechConstant.VOLUME, mSharedPreferences.getString("volume_preference", "50"));
        }else {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
            // 设置本地合成发音人 voicer为空，默认通过语记界面指定发音人。
            mTts.setParameter(SpeechConstant.VOICE_NAME, "");
            /**
             * TODO 本地合成不设置语速、音调、音量，默认使用语记设置
             * 开发者如需自定义参数，请参考在线合成参数设置
             */
        }
        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, mSharedPreferences.getString("stream_preference", "3"));
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/tts.wav");
    }


    /**
     * 语音合成——回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
            time = System.currentTimeMillis() - time;
            Log.i(TAG,"用时："+ time);
//            showTip("开始播放");
        }

        @Override
        public void onSpeakPaused() {
//            showTip("暂停播放");
        }

        @Override
        public void onSpeakResumed() {
//            showTip("继续播放");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
            // 合成进度
            mPercentForBuffering = percent;
//            showTip(String.format(context.getString(R.string.tts_toast_format),
//                    mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
            mPercentForPlaying = percent;
//            showTip(String.format(getString(R.string.tts_toast_format),
//                    mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
//                showTip("播放完成");
            } else if (error != null) {
//                showTip(error.getPlainDescription(true));
            }

        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };

    /**
     * 语音合成——初始化监听。
     */
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
//                showTip("初始化失败,错误码："+code);
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
            }
        }
    };


}
