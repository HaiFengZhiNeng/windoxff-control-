package com.ocean.mvp.library.net;

/**
 * Created by zhangyuanyuan on 2017/7/3.
 */

public class NetRequest {


    public static final int STATE_UNKNOWN = -1;
    public static final int STATE_DOING = 0;
    public static final int STATE_SUCCESS = 1;
    public static final int STATE_FAIL = 2;

    private NetMessage mMessage;
    private SendRequestListener mListener;
    private int state = STATE_UNKNOWN;

    private String result;

    private int errorCode;
    private String errorMessage;

    private long fileLength;
    private long current;

    public NetRequest(NetMessage mMessage, SendRequestListener mListener) {
        this.mMessage = mMessage;
        this.mListener = mListener;
    }

    public void notifyListener() {
        if(mListener == null) return;

        if(state == STATE_UNKNOWN) {
//            Log.e("NetRequest", "unknown state");
        } else if(state == STATE_DOING) {
            mListener.onSending(mMessage, fileLength, current);
        }else if(state == STATE_SUCCESS) {
            mListener.onSuccess(mMessage, result);
        }else if(state == STATE_FAIL) {
            mListener.onFail(mMessage, errorCode, errorMessage);
        }
    }

    public void setSuccess(String result) {
        state = STATE_SUCCESS;
        this.result = result;
    }

    public void setFail(int errorCode, String errorMessage) {
        state = STATE_FAIL;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public void setDoing(long fileLength, long current) {
        state = STATE_DOING;
        this.fileLength = fileLength;
        this.current = current;
    }

    public NetMessage getMessage() {
        return mMessage;
    }

    public void setMessage(NetMessage mMessage) {
        this.mMessage = mMessage;
    }

    public SendRequestListener getListener() {
        return mListener;
    }

    public void setListener(SendRequestListener mListener) {
        this.mListener = mListener;
    }

    public boolean isFileMessage() {
        return mMessage instanceof FileMessage;
    }

    public int getState() {
        return state;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = current;
    }
}
