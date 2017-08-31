package com.ocean.speech.tts;

import android.os.Bundle;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.ocean.mvp.library.utils.L;
import com.ocean.speech.SpeechApplication;

/**
 * Created by zhangyuanyuan on 2017/7/27.
 */

public class TtsControl {

    private String TAG = "AsrControl";

    private SpeechApplication mContext;

    // 默认发音人
    private String voicer = "xiaoyan";

    // 语音合成对象
    private SpeechSynthesizer mTts;

    private OnTtsFinish onTtsFinish;

    public void setOnTtsFinish(OnTtsFinish onTtsFinish) {
        this.onTtsFinish = onTtsFinish;
    }

    /**
     * 构造方法
     *
     * @param mContext 上下文
     */
    public TtsControl(SpeechApplication mContext) {
        this.mContext = mContext;

    }

    /**
     * 初始化
     */
    public void init() {
        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(mContext, mTtsInitListener);
    }

    /**
     * 开始TTS
     * @param answer TTS的内容
     */
    public void startTts(String answer) {
        if (mTts != null)
            mTts.startSpeaking(answer, mTtsListener);
    }

    /**
     * 停止TTS
     */
    public void stopTts(){
        if (mTts != null)
            mTts.stopSpeaking();
    }

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
            } else {
                L.d(TAG, speechError.getPlainDescription(true));
            }

            if (onTtsFinish != null)
                onTtsFinish.onFinish();
        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };

    interface OnTtsFinish{
        void onFinish();
    }

}
