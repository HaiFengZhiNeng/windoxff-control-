package com.ocean.speech.select;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import com.ocean.speech.R;
import com.ocean.speech.base.ControlBaseActivity;

/**
 * Created by dell on 2017/8/1.
 */

public class SelectView extends ControlBaseActivity<SelectPresenter> implements ISelectView, View.OnClickListener {
    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public SelectPresenter createPresenter() {
        return new SelectPresenter(this);
    }

    @Override
    protected int getContentViewResource() {
        return R.layout.activity_select;
    }

    private Button mEntrance_voice;
    private Button mEntrance_video;

    @Override
    protected void onViewInit() {
        super.onViewInit();
        (mEntrance_voice) = (Button) findViewById(R.id.entrance_voice);
        (mEntrance_video) = (Button) findViewById(R.id.entrance_video);
    }

    @Override
    protected void setOnListener() {
        super.setOnListener();
        mEntrance_voice.setOnClickListener(this);
        mEntrance_video.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.entrance_voice:
                mPresenter.audioPermission();
                break;
            case R.id.entrance_video:
                mPresenter.videoPermission();
                break;
        }
    }
}
