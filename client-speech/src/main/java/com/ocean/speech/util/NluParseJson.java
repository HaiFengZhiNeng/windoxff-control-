package com.ocean.speech.util;

import com.ocean.speech.bean.AnswerBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by zhangyuanyuan on 2017/7/18.
 */

public class NluParseJson {
    /**
     * 解析故事json
     *
     * @param object
     */
    public static AnswerBean parseStory(JSONObject object) {
        try {
            AnswerBean bean = new AnswerBean();
            JSONObject obj3 = new JSONObject(object.optString("answer"));
            String answer = obj3.optString("text");
            bean.setAnswer(answer);
            if (object.has("data")) {
                JSONObject data = new JSONObject(object.optString("data"));
                JSONArray jsonArray = data.optJSONArray("result");
                if (jsonArray != null) {
                    int size = jsonArray.length();
                    if (size > 0) {
                        JSONObject voice = jsonArray.optJSONObject(0);
                        String path = voice.optString("playUrl");
                        bean.setUrl(path);
                    }
                }
                return bean;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解析音乐X musicX
     *
     * @param object json
     * @return tts and url
     */
    public static AnswerBean parseMusicX(JSONObject object) {
        try {
            AnswerBean bean = new AnswerBean();
            JSONObject obj4 = new JSONObject(object.optString("answer"));
            String answer = obj4.optString("text");
            bean.setAnswer(answer);
            if (object.has("data")) {
                JSONObject music = new JSONObject(object.optString("data"));
                JSONArray musicArray = music.optJSONArray("result");
                if (musicArray != null) {
                    int size = musicArray.length();
                    if (size > 0) {
                        JSONObject voice = musicArray.optJSONObject(0);
                        String path = voice.optString("audiopath");
                        bean.setUrl(path);
                    }
                }
                return bean;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解析音乐 musicPlayer_smartHome
     *
     * @param object json
     * @return tts and url
     */
    public static AnswerBean parseMusicSmartHome(JSONObject object) {
        try {
            AnswerBean bean = new AnswerBean();
            JSONArray moreResults = object.optJSONArray("moreResults");
            if (moreResults != null) {
                if (moreResults.length() > 0) {
                    JSONObject moreObj = moreResults.optJSONObject(0);
                    if (object.has("data")) {
                        JSONObject moredata = new JSONObject(moreObj.optString("data"));
                        JSONArray moreArray = moredata.optJSONArray("result");
                        if (moreArray != null) {
                            int size = moreArray.length();
                            if (size > 0) {
                                JSONObject voice = moreArray.optJSONObject(0);
                                String path = voice.optString("audiopath");
                                bean.setUrl(path);

                                JSONArray singerArr = voice.optJSONArray("singernames");
                                if (singerArr != null) {
                                    if (singerArr.length() > 0) {
                                        String name = singerArr.optString(0);
                                        String answer = "播放一首" + name + "的" + voice.optString("albumname");
                                        bean.setAnswer(answer);
                                    } else {
                                        String answer = "播放一首" + voice.optString("albumname");
                                        bean.setAnswer(answer);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return bean;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 笑话
     */
    public static AnswerBean parseJoke(JSONObject object) {

        try {
            AnswerBean bean = new AnswerBean();
            JSONObject obj4 = new JSONObject(object.optString("answer"));
            String answer = obj4.optString("text");
            bean.setAnswer(answer);
            if (object.has("data")) {
                JSONObject music = new JSONObject(object.optString("data"));
                JSONArray musicArray = music.optJSONArray("result");
                if (musicArray != null) {
                    int size = musicArray.length();
                    if (size > 0) {
                        JSONObject voice = musicArray.optJSONObject(0);
                        String path = voice.optString("mp3Url");
                        bean.setUrl(path);
                    }
                }
                return bean;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 新闻
     *
     * @param object
     */
    public static void parseNews(JSONObject object) {

        try {
            if (object.has("data")) {
                JSONObject news = new JSONObject(object.optString("data"));
                JSONArray newsArray = news.optJSONArray("result");
                if (newsArray != null) {
                    int size = newsArray.length();
                    if (size > 0) {
                        JSONObject news_result = newsArray.optJSONObject(0);
                        String category = news_result.optString("category");
                        String content = news_result.optString("content");
                        String imgUrl = news_result.optString("imgUrl");
                        String url = news_result.optString("url");

                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();


        }
    }

    /**
     * 计算
     *
     * @param object
     */
    public static void parseCalculat(JSONObject object) {

        try {
            if (object.has("answer")) {
                JSONObject news = new JSONObject(object.optString("answer"));
                String text = news.optString("text");
            }

        } catch (JSONException e) {
            e.printStackTrace();


        }
    }

    /**
     * 古诗词
     *
     * @param object
     */
    public static void parsePoetry(JSONObject object) {
        try {
            JSONObject obj_answer = new JSONObject(object.optString("answer"));
            String answer = obj_answer.optString("text");
            if (object.has("data")) {
                JSONObject proety = new JSONObject(object.optString("data"));
                JSONArray proetyArray = proety.optJSONArray("result");
                if (proetyArray != null) {
                    int size = proetyArray.length();
                    if (size > 0) {
                        JSONObject news_result = proetyArray.optJSONObject(0);
                        String author = news_result.optString("author");//作者
                        String category = news_result.optString("category");//诗的类别
                        String content = news_result.optString("content");//内容
                        String title = news_result.optString("title");//题目
                        String dynasty = news_result.optString("dynasty");//朝代

                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();


        }
    }



}
