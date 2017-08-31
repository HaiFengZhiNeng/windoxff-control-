package com.ocean.speech.login;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.ocean.speech.R;
import com.ocean.speech.base.ControlBaseActivity;

/**
 * Created by dell on 2017/8/1.
 */

public class LoginView extends ControlBaseActivity<LoginPresenter> implements ILoginView, View.OnClickListener {

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public LoginPresenter createPresenter() {
        return new LoginPresenter(this);
    }

    @Override
    protected int getContentViewResource() {
        return R.layout.activity_login;
    }

    private TextView mLogin;
    private EditText mUsername;



    @Override
    protected void onViewInit() {
        super.onViewInit();

        (mLogin) = (TextView) findViewById(R.id.tv_login);
        (mUsername) = (EditText) findViewById(R.id.et_username);
    }

    @Override
    protected void setOnListener() {
        super.setOnListener();
        mLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_login:
                mPresenter.DoLogin();
                break;
        }
    }

    @Override
    public String getEditText() {
        return mUsername.getText().toString();
    }

    @Override
    public void SetEditText(String text) {
        mLogin.setText(text);
    }



}
