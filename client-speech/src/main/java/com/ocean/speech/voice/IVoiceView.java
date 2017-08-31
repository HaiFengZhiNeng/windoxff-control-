package com.ocean.speech.voice;

import com.ocean.mvp.library.view.UiView;

/**
 * Created by dell on 2017/8/1.
 */

public interface IVoiceView extends UiView {

    void setLayoutVisible(boolean visible);

    void setChronometerVisible(boolean visible);

    void setVoiceText(String text);

    void startUpChronometer(boolean falg);

    void setChronometerTime();

    void setSpeakerVisiable(boolean visiable);

    void setMuteVisiable(boolean visiable);
}
