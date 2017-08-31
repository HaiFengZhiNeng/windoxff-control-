package com.ocean.speech.main;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.GrammarListener;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUnderstanderListener;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.UnderstanderResult;
import com.iflytek.cloud.util.ContactManager;
import com.iflytek.cloud.util.ResourceUtil;
import com.ocean.mvp.library.presenter.BasePresenter;
import com.ocean.mvp.library.utils.L;
import com.ocean.speech.SpeechApplication;
import com.ocean.speech.asr.AsrControl;
import com.ocean.speech.bean.AnswerBean;
import com.ocean.speech.util.FucUtil;
import com.ocean.speech.util.MediaPlayerUtil;
import com.ocean.speech.util.NluParseJson;
import com.ocean.speech.util.Player;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by zhangyuanyuan on 2017/7/5.
 */

public class MainPresenter extends BasePresenter<IMainView> {

    private String engineMode = SpeechConstant.TYPE_LOCAL;
    private String grmPath;
    private String asrResPath;


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

    // 默认发音人
    private String voicer = "xiaoyan";


    public MainPresenter(IMainView mView) {
        super(mView);
    }

    // 语音识别对象
    private SpeechRecognizer mAsr;
//    private SpeechUnderstander mAsr;

    // 语音合成对象
    private SpeechSynthesizer mTts;

    AnswerBean answerBean = null;

    MediaPlayerUtil mediaPlayer = null;

    private boolean isHaveVoice = false;


    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
//        AsrControl asrControl = new AsrControl(SpeechApplication.from(getContext()));
//        asrControl.init();


        // 初始化识别对象
        mAsr = SpeechRecognizer.createRecognizer(getContext(), mInitListener);
//        mAsr = SpeechUnderstander.createUnderstander(getContext(), mInitListener);

        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(getContext(), mTtsInitListener);

        mLocalGrammar = FucUtil.readFile(getContext(), "call.bnf", "utf-8");

        answerBean = new AnswerBean();

        mediaPlayer = MediaPlayerUtil.getInstance();

//        buildGrammar();
    }

    void buildGrammar() {
        mEngineType = SpeechConstant.TYPE_LOCAL;//设置引擎类型
        //mAsr.setParameter(SpeechConstant.PARAMS, null); //清空参数
        mAsr.setParameter(SpeechConstant.KEY_SPEECH_TIMEOUT, "20000");//会话超时时间
        mAsr.setParameter(SpeechConstant.RESULT_TYPE, "json");// 设置返回结果格式
        mAsr.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");   // 设置文本编码格式
        mAsr.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);// 设置引擎类型
        mAsr.setParameter(SpeechConstant.MFV_SCENES, "main");// 设置引擎类型
        // 设置本地资源路径
        mAsr.setParameter(ResourceUtil.ASR_RES_PATH, getResourcePath());

        // 设置语法构建路径
        mAsr.setParameter(ResourceUtil.GRM_BUILD_PATH, Environment.getExternalStorageDirectory() + "/grmPath");
        mAsr.setParameter(SpeechConstant.LOCAL_GRAMMAR, "call"); // 设置本地识别使用语法id
        mLocalGrammar = FucUtil.readFile(getContext(), "call.bnf", "utf-8");
        String mContent = new String(mLocalGrammar);// 语法、词典临时变量

        int ret = mAsr.buildGrammar(GRAMMAR_TYPE_BNF, mContent, mLocalGrammarListener);

        if (ret != ErrorCode.SUCCESS) {
            L.e(TAG, "语法构建失败，错误码：" + ret);
        }

        mAsr.setParameter(SpeechConstant.LOCAL_GRAMMAR, "call"); // 设置本地识别使用语法id
    }

    /**
     * 开始识别
     */
    void startAsr() {
        answerBean = null;
        setParam();

        int ret = mAsr.startListening(mRecognizerListener);
//        int ret = mAsr.startUnderstanding(mUnderstanderListener);
        if (ret != ErrorCode.SUCCESS) {
            L.e(TAG, "识别失败，错误码：" + ret);
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
        mAsr.setParameter(SpeechConstant.ASR_PTT, "1");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mAsr.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mAsr.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/sud.wav");

        // 设置语义情景
//        mAsr.setParameter(SpeechConstant.SCENE, "main2");
    }

    /**
     * 参数设置
     */
    private void setTTSParam() {
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        // 根据合成引擎设置相应参数
//        if(mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        // 设置在线合成发音人
        mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
        //设置合成语速
        mTts.setParameter(SpeechConstant.SPEED, "50");
        //设置合成音调
        mTts.setParameter(SpeechConstant.PITCH, "50");
        //设置合成音量
        mTts.setParameter(SpeechConstant.VOLUME, "50");
        /*}else {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
            // 设置本地合成发音人 voicer为空，默认通过语记界面指定发音人。
            mTts.setParameter(SpeechConstant.VOICE_NAME, "");
            *//**
         * TODO 本地合成不设置语速、音调、音量，默认使用语记设置
         * 开发者如需自定义参数，请参考在线合成参数设置
         *//*
        }*/
        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/tts.wav");
    }

    /**
     * 停止识别
     */
    void stopAsr() {
        mAsr.stopListening();
//        mAsr.stopUnderstanding();
    }


    // 获取识别资源路径
    private String getResourcePath() {
        return ResourceUtil.generateResourcePath(getContext(), ResourceUtil.RESOURCE_TYPE.assets, "asr/common.jet");
    }

    private ContactManager.ContactListener mContactListener = new ContactManager.ContactListener() {
        @Override
        public void onContactQueryFinish(String contactInfos, boolean b) {
            //获取联系人
            mLocalLexicon = contactInfos;
        }
    };

    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            L.d(TAG, "mTtsInitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                L.d(TAG, "tts 初始化失败,错误码：" + code);
            }
        }
    };

    private SynthesizerListener mTtsListener = new SynthesizerListener() {
        @Override
        public void onSpeakBegin() {
            L.i(TAG, "开始播放");
        }

        @Override
        public void onBufferProgress(int i, int i1, int i2, String s) {
            L.i(TAG, "开始播放");
        }

        @Override
        public void onSpeakPaused() {
            L.i(TAG, "暂停播放");
        }

        @Override
        public void onSpeakResumed() {
            L.i(TAG, "继续播放");
        }

        @Override
        public void onSpeakProgress(int i, int i1, int i2) {

        }

        @Override
        public void onCompleted(SpeechError speechError) {
            if (speechError == null) {
                L.d(TAG, "播放完成");
                if (isHaveVoice) {

                    playVoice(answerBean.getUrl());
                }
                startAsr();
            } else {
                L.d(TAG, speechError.getPlainDescription(true));
                startAsr();
            }
        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };

    private void playVoice(String url) {
        isHaveVoice = false;

        if (TextUtils.isEmpty(url))
            return;


        Player player = new Player();
        player.playUrl(url);


        if (mediaPlayer != null) {
            mediaPlayer.playMusic01(url);
           /* mediaPlayer.playUrl(url, new MediaPlayerUtil.OnMusicCompletionListener() {
                @Override
                public void onCompletion(boolean isPlaySuccess) {
                    L.e("music", "音乐播放完毕："+isPlaySuccess);
                }

                @Override
                public void onPrepare() {
                    L.e("music", "音乐播放准备");
                }
            });*/
        }
    }

    private SpeechUnderstanderListener mUnderstanderListener = new SpeechUnderstanderListener() {
        @Override
        public void onVolumeChanged(int i, byte[] bytes) {
            L.e("volume", "当前音量：" + i);
        }

        @Override
        public void onBeginOfSpeech() {
            L.e("volume", "开始说话");
        }

        @Override
        public void onEndOfSpeech() {
            L.e("volume", "结束说话");
        }

        @Override
        public void onResult(UnderstanderResult understanderResult) {
            L.e("volume", "识别到结果");
            if (understanderResult != null) {
                String text = understanderResult.getResultString();
                L.e("volume", "识别到结果--->" + text);
                parseJson(text);
            }
        }

        @Override
        public void onError(SpeechError speechError) {
            L.e("volume", "onError Code:" + speechError.getErrorCode());
            startAsr();
        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };


    private RecognizerListener mRecognizerListener = new RecognizerListener() {
        @Override
        public void onVolumeChanged(int i, byte[] bytes) {
            L.e("volume", "当前音量：" + i);
        }

        @Override
        public void onBeginOfSpeech() {
            L.e("volume", "开始说话");
        }

        @Override
        public void onEndOfSpeech() {
            L.e("volume", "结束说话");
//            startAsr();
        }

        @Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
            L.e("volume", "识别到结果");
            if (recognizerResult != null) {
                String text = recognizerResult.getResultString();
                L.e("volume", "识别到结果--->" + text);
                parseJson(text);
            }
        }

        @Override
        public void onError(SpeechError speechError) {
            L.e("volume", "onError Code:" + speechError.getErrorCode());
            startAsr();
        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };

    private void parseJson(String result) {
        try {
            JSONObject object = new JSONObject(result);
            String question = object.optString("text");
            answerBean = new AnswerBean();
            if (object.has("service")) {

                String service = object.optString("service");
                switch (service) {
                    case "openQA"://QA
                        JSONObject obj = new JSONObject(object.optString("answer"));
                        answerBean.setAnswer(obj.optString("text"));
                        break;
                    case "cookbook"://菜谱
                        JSONObject obj1 = new JSONObject(object.optString("answer"));
                        answerBean.setAnswer(obj1.optString("text"));
                        break;
                    case "weather"://天气
                        JSONObject obj2 = new JSONObject(object.optString("answer"));
                        answerBean.setAnswer(obj2.optString("text"));
                        break;
                    case "story"://故事
                        answerBean = NluParseJson.parseStory(object);
                        if (answerBean == null)
                            answerBean = new AnswerBean("对不起，没有找到故事", "");
                        break;
                    case "musicPlayer_smartHome"://音乐musicPlayer_smartHome
                        answerBean = NluParseJson.parseMusicSmartHome(object);
                        if (answerBean == null)
                            answerBean = new AnswerBean("对不起，没有找到音乐", "");
                        break;
                    case "musicX"://音乐 musicX
                        answerBean = NluParseJson.parseMusicX(object);
                        if (answerBean == null)
                            answerBean = new AnswerBean("对不起，没有找到音乐", "");
                        break;
                    case "joke"://笑话
                        answerBean = NluParseJson.parseJoke(object);
                        if (answerBean == null)
                            answerBean = new AnswerBean("对不起，没有找到故事", "");
                        break;
                    case "news"://新闻
                        NluParseJson.parseNews(object);
                        break;
                    case "calc"://计算
                        NluParseJson.parseCalculat(object);
                        break;
                    case "poetry"://古诗词
                        NluParseJson.parsePoetry(object);
                        break;
                    default:
                        if (object.has("answer")) {
                            JSONObject objd = new JSONObject(object.optString("answer"));
                            answerBean.setAnswer(objd.optString("text"));
                        } else {
                            answerBean.setAnswer("您说的我没有听懂");
                        }
                        break;
                }
            } else {
                answerBean.setAnswer("您说的我没有听懂");
            }

            mView.setResult("Q:" + question, "A:" + answerBean.getAnswer());

            setTTSParam();
            if (!TextUtils.isEmpty(answerBean.getUrl())) {
                isHaveVoice = true;

            }
            mTts.startSpeaking(answerBean.getAnswer(), mTtsListener);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private GrammarListener mLocalGrammarListener = new GrammarListener() {
        @Override
        public void onBuildFinish(String grammarId, SpeechError speechError) {
            if (speechError == null) {
                L.e(TAG, "语法构建成功：" + grammarId);
                mView.setGrammarResult("语法构建成功：" + grammarId);
            } else {
                L.e(TAG, "语法构建失败,错误码：" + speechError.getErrorCode());
                mView.setGrammarResult("语法构建失败,错误码：" + speechError.getErrorCode());
            }
        }
    };

    InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int i) {
            if (i != ErrorCode.SUCCESS) {
                L.e(TAG, "初始化失败,错误码：" + i);
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mTts) {
            mTts.stopSpeaking();
            // 退出时释放连接
            mTts.destroy();
        }
        if (mAsr != null) {
            mAsr.cancel();
            mAsr.stopListening();
//            mAsr.stopUnderstanding();
            mAsr.destroy();
        }
    }
}
