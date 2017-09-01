package com.ocean.mvp.library.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Json工具类
 * Created by zhangyuanyuan on 2017/7/3.
 */

public class JsonBuilder {

    private JSONObject mJSONObject;

    public JsonBuilder() {
        mJSONObject = new JSONObject();
    }



    public JsonBuilder append(String key, int value) throws JSONException {
        mJSONObject.put(key, value);
        return this;
    }

    public JsonBuilder append(String key, JSONArray value) throws JSONException {
        mJSONObject.put(key, value);
        return this;
    }

    public JsonBuilder append(String key, JsonBuilder value) throws JSONException {
        mJSONObject.put(key, value.builder());
        return this;
    }

    public JsonBuilder append(String key, Object value) throws JSONException {
        mJSONObject.put(key, value);
        return this;
    }

    public JsonBuilder append(String key, boolean value) throws JSONException {
        mJSONObject.put(key, value);
        return this;
    }

    public JsonBuilder append(String key, double value) throws JSONException {
        mJSONObject.put(key, value);
        return this;
    }

    public JsonBuilder append(String key, long value) throws JSONException {
        mJSONObject.put(key, value);
        return this;
    }

    public JSONObject builder() {
        return mJSONObject;
    }
}
