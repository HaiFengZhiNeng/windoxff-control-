package com.ocean.mvp.library.view;

import android.os.Bundle;

import com.ocean.mvp.library.presenter.BasePresenter;

/**
 * Created by zhangyuanyuan on 2017/7/3.
 */

public abstract class PresenterActivity<T extends BasePresenter> extends BaseActivity {
    protected T mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mPresenter = createPresenter();
        super.onCreate(savedInstanceState);
        mPresenter.onCreate(savedInstanceState);
    }

    public abstract T createPresenter();

    @Override
    protected void onViewCreateBefore() {
        mPresenter.onViewCreateBefore();
    }


    @Override
    protected void onViewCreated() {
        mPresenter.onViewCreated();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPresenter.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPresenter.onPause();

    }

    @Override
    protected void onDestroy() {
        mPresenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mPresenter.onBackPressed())
            super.onBackPressed();
    }

}
