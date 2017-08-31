package com.ocean.speech.main;

import com.ocean.mvp.library.view.UiView;

/**
 * Created by zhangyuanyuan on 2017/7/5.
 */

public interface IMainView extends UiView {
    void setResult(String resultQ,String resultA);
    void setGrammarResult(String result);
}
