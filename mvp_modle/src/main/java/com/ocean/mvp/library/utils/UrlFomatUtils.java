package com.ocean.mvp.library.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Created by zhangyuanyuan on 2017/7/3.
 */

public class UrlFomatUtils {
    public static String formatParams(HashMap<String, String> params) {
        StringBuilder mParamsString = new StringBuilder();
        int pos = 0;
        for (String key : params.keySet()) {
            try {
                if (pos > 0) {
                    mParamsString.append("&");
                }
                mParamsString.append(String.format("%s=%s", key, URLEncoder.encode(params.get(key), "utf-8")));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            pos++;
        }
        return mParamsString.toString();
    }

    public static String attachHttpGetParams(String url,HashMap<String, String> params) {
        if (params != null)
            return url + "?" + formatParams(params);
        return url;
    }
}
