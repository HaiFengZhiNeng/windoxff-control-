package com.ocean.speech.asr;

import android.content.Context;
import android.text.TextUtils;

import com.ocean.mvp.library.utils.L;
import com.ocean.speech.bean.AnswerBean;
import com.ocean.speech.util.NluParseJson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by zhangyuanyuan on 2017/7/27.
 */

public class AsrJsonParse {

    private static AsrJsonParse mAsrJsonParser;
    private final Context mContext;

    private AsrJsonParse(final Context mContext) {
        this.mContext = mContext;

    }


    public static AsrJsonParse getInstance(Context mContext) {

        if (mAsrJsonParser == null) {
            mAsrJsonParser = new AsrJsonParse(mContext);
        }
        return mAsrJsonParser;

    }

    public void parseUnderResult(final String json, final long tag, final OnResultCallback onResultCallback) {
        //解析数据
        String textValue = "";
        try {
            JSONObject object = new JSONObject(json);

            //记录问题文本
            if (object.has("text")) textValue = object.getString("text");

            final HashMap<String, Object> map = new HashMap<String, Object>();
            //设置答案
            map.put("QUESTION", textValue);
            map.put("type", 2);
            L.w("ASR_RESULT_QUESTION", textValue);


            //开始解析数据
            if (object.has("rc")) {
                /**
                 * 是否含有唤醒词
                 */
                int rc = object.getInt("rc");
                switch (rc) {
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                        //没有识别出来
                        //未知答案
                        if (onResultCallback != null)
                            onResultCallback.onCallBack(tag,map);
                        break;
                    case 0:
                        if (object.has("service")) {
                            String type = object.getString("service");
                            AnswerBean answerBean = null;
                            switch (type) {
                                case "openQA"://QA
                                    JSONObject obj = new JSONObject(object.optString("answer"));
                                    map.put("ANSWER",obj.optString("text"));
                                    break;
                                case "cookbook"://菜谱
                                    JSONObject obj1 = new JSONObject(object.optString("answer"));
                                    map.put("ANSWER",obj1.optString("text"));
                                    break;
                                case "weather"://天气
                                    JSONObject obj2 = new JSONObject(object.optString("answer"));
                                    map.put("ANSWER",obj2.optString("text"));
                                    break;
                                /*case "story"://故事
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
                                    break;*/
                                default:
                                    if (object.has("answer")) {
                                        JSONObject objd = new JSONObject(object.optString("answer"));
                                        map.put("ANSWER",objd.optString("text"));
                                    } else {
                                        map.put("ANSWER","");
                                    }
                                    break;
                            }

                            //返回
                            if (onResultCallback != null)
                                onResultCallback.onCallBack(tag,map);

                        }

                        break;
                    default:

                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            L.e("MUSIC_INFO", "抛出异常");
            if (onResultCallback != null)
                onResultCallback.onCallBack(tag,null);
        }
    }

    interface OnResultCallback{
        void onCallBack(long tag, HashMap<String, Object> value);
    }

}
