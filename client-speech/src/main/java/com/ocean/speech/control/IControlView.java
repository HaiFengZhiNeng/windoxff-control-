package com.ocean.speech.control;

import com.ocean.mvp.library.view.UiView;
import com.ocean.speech.custom.AnimationView;

/**
 * Created by zhangyuanyuan on 2017/7/8.
 */

public interface IControlView extends UiView {

    String getEditText();

    void setEditText(String text);

    void setTextView(String textView);

    void setLayoutVisible(boolean visible);

    void setAsrLayoutVisible(boolean visible);

    void setQuestion(String question);

    void setAnswer(String answer);

    AnimationView getVoiceView();

    void setAnimationVisible(boolean visible);

    void setLocalViewVisiable(boolean viewVisiable);

    void setBottomVisible(boolean visible);

    void setTopText(String text);

    void setChronometerVisible(boolean visible);

    void startUpChronometer(boolean falg);

    void setChronometerTime();

    void setGlViewVisable(boolean visable);

    void setSportVisiable(boolean visiable);

    void setVoicetVisiable(boolean visiable);

    void setLinkVisiable(boolean visiable);

    void setInputVisiable(boolean visiable);

}
