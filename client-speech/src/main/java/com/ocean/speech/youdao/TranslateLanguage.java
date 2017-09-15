package com.ocean.speech.youdao;

import android.util.Log;

import com.youdao.sdk.app.Language;
import com.youdao.sdk.app.LanguageUtils;
import com.youdao.sdk.ydonlinetranslate.TranslateErrorCode;
import com.youdao.sdk.ydonlinetranslate.TranslateListener;
import com.youdao.sdk.ydonlinetranslate.TranslateParameters;
import com.youdao.sdk.ydonlinetranslate.Translator;
import com.youdao.sdk.ydtranslate.Translate;

/**
 * Created by Administrator on 2017/9/11/011.
 */

public class TranslateLanguage {
    public static final String TAG = "TranslateLanguage";
    private Translator translator;
    private String translateResult;

    public enum LanguageType{
        EN,
        ZH
    }

    /**
     * 通过有道词典进行翻译
     * @param source
     * @param fromType
     * @param toType
     * @return
     */
    public void query(String source, LanguageType fromType, LanguageType toType) {
        String from="",to="",input="";
        // 源语言或者目标语言其中之一必须为中文,目前只支持中文与其他几个语种的互译
        if (fromType == LanguageType.EN && toType == LanguageType.ZH){
            //英译中
             from = "英文";
             to = "中文";
        }else if(fromType == LanguageType.ZH && toType == LanguageType.EN){
            //中译英
             from = "中文";
             to = "英文";
        }else {
            Log.e(TAG,"暂时只支持中英互译");
            return;
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
                translateResult = td.translates();
                Log.i(TAG,"翻译返回结果"+td.translates());
            }

            @Override
            public void onError(TranslateErrorCode error) {
                Log.i(TAG,error.name());
            }
        });
    }
}
