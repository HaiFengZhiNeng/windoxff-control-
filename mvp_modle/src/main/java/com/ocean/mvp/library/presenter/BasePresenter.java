package com.ocean.mvp.library.presenter;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.ocean.mvp.library.utils.PromptManager;
import com.ocean.mvp.library.view.UiView;

import java.lang.ref.WeakReference;

/**
 * 控制器的父类
 * Created by zhangyuanyuan on 2017/7/3.
 */

public abstract class BasePresenter<T extends UiView> {

    public String TAG = this.getClass().getSimpleName();
    private BaseHandler mBaseHandler;
    protected T mView;

    public BasePresenter(T mView){
        this.mView = mView;
    }

    /**
     * 创建ContentView之前调用
     */
    public void onViewCreateBefore() {}

    /**
     * ContentView创建之后调用
     */
    public void onViewCreated() {}

    /**
     * 同 Activity 哦你Create
     * @param saveInstanceState
     */
    public void onCreate(Bundle saveInstanceState) {}

    /**
     * 同 Activity onResume
     */
    public void onResume(){}

    /**
     * 同 Activity onPause
     */
    public void onPause(){}

    /**
     * 同 Activity onRestart
     */
    protected void onRestart() {}

    /**
     * 同 Activity onStart
     */
    protected void onStart() {}

    /**
     * 同 Activity onStop
     */
    public void onStop() {}

    /**
     * 同 Activity onDestroy
     */
    public void onDestroy(){}

    /**
     * 返回键处理
     * @return  true 程序将继续执行Activity里面的onBackPressed ，为false不执行
     */
    public boolean onBackPressed(){ return true;}



    /**
     * 初始化一个Handler，如果需要使用Handler，先调用此方法，
     * 然后可以使用postRunnable(Runnable runnable)，
     * sendMessage在handleMessage（Message msg）中接收msg
     */
    public void initHandler() {
        mBaseHandler = new BaseHandler(this);
    }

    /**
     * 返回Handler，在此之前确定已经调用initHandler（）
     * @return Handler
     */
    public Handler getHandler() {
        return mBaseHandler;
    }

    /**
     * 同Handler的postRunnable
     * 在此之前确定已经调用initHandler（）
     */
    protected void postRunnable(Runnable runnable) {
        postRunnableDelayed(runnable, 0);
    }

    /**
     * 同Handler的postRunnableDelayed
     * 在此之前确定已经调用initHandler（）
     */
    protected void postRunnableDelayed(Runnable runnable, long delayMillis) {
        if(mBaseHandler == null ) initHandler();
        mBaseHandler.postDelayed(runnable, delayMillis);
    }

    /**
     * 得到Context
     * @return
     */

    public Context getContext() {return mView.getContext(); }



    /**
     * startActivityForResult的返回结果
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {}

    /**
     *
     * @param mIntent
     * @param resultCode
     */
    public void setResult(Intent mIntent, int resultCode){
        ((Activity)getContext()).setResult(resultCode,mIntent);
    }

    /**
     * 开启一个服务
     * @param mService
     */
    public void startService(Service mService) {
        startService(mService.getClass());
    }
    /**
     * 开启一个服务
     * @param clazz 需要启动的Activity的class文件
     */
    public void startService(Class<? extends  Service> clazz) {
        getContext().startService(new Intent(getContext(), clazz));
    }


    /**
     * 启动一个Activity
     * @param clazz 需要启动的Activity的class文件
     */
    public void startActivity(Class<? extends  Activity> clazz) {
        Intent intent = new Intent(getContext(), clazz);
        getContext().startActivity(intent);
    }

    public void startActivity(Class<? extends  Activity> clazz, Bundle mBundle) {
        Intent intent = new Intent(getContext(), clazz);
        intent.putExtras(mBundle);
        getContext().startActivity(intent);
    }

    /**
     * 启动一个Activity
     * @param
     */
    public void startActivity(Intent mIntent) {
        getContext().startActivity(mIntent);
    }

    public void startActivityForResult(Intent mIntent, int requestCode) {
        ((Activity)getContext()).startActivityForResult(mIntent, requestCode);
    }

    /**
     * 显示Toast
     *
     * @param toastId
     */
    public void showToast(int toastId) {
        PromptManager.showToast(getContext(), toastId);
    }

    /**
     * 显示Toast
     *
     * @param text
     */
    public void showToast(String text) {
        PromptManager.showToast(getContext(), text);
    }

    /**
     * 显示Toast
     *
     * @param toastId
     */
    public void showToast(int toastId, int duration) {
        PromptManager.showToast(getContext(), toastId, duration);
    }

    /**
     * 显示Toast
     *
     * @param text
     */
    public void showToast(String text, int duration) {
        PromptManager.showToast(getContext(), text, duration);
    }


    /**
     *
     * 同Handler 的 handleMessage，
     * getHandler.sendMessage,发送的Message在此接收
     * 在此之前确定已经调用initHandler（）
     * @param msg
     */
    protected void handleMessage(Message msg) {}
    protected static class BaseHandler extends Handler {
        private final WeakReference<BasePresenter> mObjects;

        public BaseHandler(BasePresenter mPresenter) {
            mObjects = new WeakReference<BasePresenter>(mPresenter);
        }

        @Override
        public void handleMessage(Message msg) {
            BasePresenter mPresenter = mObjects.get();
            if (mPresenter != null)
                mPresenter.handleMessage(msg);
        }
    }

}
