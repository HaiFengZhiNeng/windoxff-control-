package com.ocean.speech.main;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ocean.mvp.library.view.PresenterActivity;
import com.ocean.speech.R;

public class MainActivity extends PresenterActivity<MainPresenter> implements IMainView, View.OnClickListener {

    private TextView mResultQ,mResultA, grammar_result;
    private Button mAsrGrammar, mStart, mStop;

    @Override
    protected int getContentViewResource() {
        return R.layout.activity_main;
    }

    @Override
    public MainPresenter createPresenter() {
        return new MainPresenter(this);
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    protected void onViewInit() {
        super.onViewInit();
        mResultQ = (TextView) findViewById(R.id.result_q);
        mResultA = (TextView) findViewById(R.id.result_a);
        grammar_result = (TextView) findViewById(R.id.grammar_result);
        mAsrGrammar = (Button) findViewById(R.id.asr_grammar);
        mStart = (Button) findViewById(R.id.start_asr);
        mStop = (Button) findViewById(R.id.stop_asr);
    }

    @Override
    protected void setOnListener() {
        super.setOnListener();
        mAsrGrammar.setOnClickListener(this);
        mStop.setOnClickListener(this);
        mStart.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.asr_grammar:
                mPresenter.buildGrammar();
                break;
            case R.id.start_asr:
                mPresenter.startAsr();
                break;
            case R.id.stop_asr:
                mPresenter.stopAsr();
                break;
        }
    }

    @Override
    public void setResult(String resultQ, String resultA) {
        mResultQ.setText(resultQ);
        mResultA.setText(resultA);
    }

    @Override
    public void setGrammarResult(String result) {
        grammar_result.setText(result);
    }
}
