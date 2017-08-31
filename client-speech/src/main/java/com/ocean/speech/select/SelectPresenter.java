package com.ocean.speech.select;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.ocean.speech.base.ControlBasePresenter;
import com.ocean.speech.config.Constant;
import com.ocean.speech.control.ControlView;
import com.ocean.speech.voice.VoiceView;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECVoIPCallManager;

import static com.ocean.speech.config.Constant.ACTION_CALLBACKING;

/**
 * Created by dell on 2017/8/1.
 */

public class SelectPresenter extends ControlBasePresenter<SelectView> {

    public static final int RESULT_CODE_STARTAUDIO = 100;
    public static final int RESULT_CODE_STARTVIDEO = 200;

    public SelectPresenter(SelectView mView) {
        super(mView);
    }

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
    }

/*    private void initCall(){
        //true  代表呼出  获取到呼入的类型是音频或者视频呼叫，然后来设置对应UI布局
        //获取是否是音频还是视频
        mCallType = (ECVoIPCallManager.CallType)
                ((Activity) mView).getIntent().getSerializableExtra(ECDevice.CALLTYPE);
        if (ECVoIPCallManager.CallType.VIDEO.equals(mCallType)) {
            //获取当前的callid
            mCallId = ((Activity) mView).getIntent().getStringExtra(ECDevice.CALLID);
            mCallNumber = ((Activity) mView).getIntent().getStringExtra(ECDevice.CALLER);
            Bundle bundle = new Bundle();
            bundle.putString(ECDevice.CALLID, mCallId);
            bundle.putString(ECDevice.CALLER, mCallNumber);
            startActivity(ControlView.class, bundle);
    }
    }*/

    /**
     * 音频呼叫
     */
    private void makeCall(ECVoIPCallManager.CallType callType) {
        //说明：mCurrentCallId如果返回空则代表呼叫失败，可能是参数错误引起。否则返回是一串数字，是当前通话的标识。

        Intent callAction = new Intent();
        //视频
        if (ECVoIPCallManager.CallType.VIDEO.equals(callType)) {
            callAction.setClass(getContext(), ControlView.class);
            callAction.putExtra(ACTION_CALLBACKING, true);
//            callAction.putExtra(Constant.EXTRA_OUTGOING_CALL, false);
            getContext().startActivity(callAction);
        } else {
            String mCurrentCallId = ECDevice.getECVoIPCallManager().makeCall(callType, "15554955416");//17600738557
            if (!"".equals(mCurrentCallId)) {
                //音频
                callAction.setClass(getContext(), VoiceView.class);
                callAction.putExtra(ACTION_CALLBACKING, true);
                callAction.putExtra(Constant.EXTRA_CALL_NAME, "15554955416");
                callAction.putExtra(Constant.EXTRA_CALL_NUMBER, "15554955416");
                callAction.putExtra(ECDevice.CALLTYPE, callType);
                callAction.putExtra(ECDevice.CALLID, mCurrentCallId);
                callAction.putExtra(Constant.EXTRA_OUTGOING_CALL, true);
                getContext().startActivity(callAction);
            }
            //是否正在通话
//            callAction.putExtra(Constant.ACTION_CALLBACK_CALL, true);
            else {
                showToast("发起失败");
            }
        }
    }

    /**
     * 音频权限
     */
    public void audioPermission() {
        if (PackageManager.PERMISSION_GRANTED == ContextCompat.
                checkSelfPermission(getContext(), android.Manifest.permission.RECORD_AUDIO)) {
            makeCall(ECVoIPCallManager.CallType.VOICE);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //提示用户开户权限音频
                String[] perms = {"android.permission.RECORD_AUDIO"};
                ActivityCompat.requestPermissions((Activity) mView, perms, RESULT_CODE_STARTAUDIO);
            }
        }

    }

    //视频权限
    public void videoPermission() {
        if (PackageManager.PERMISSION_GRANTED == ContextCompat.
                checkSelfPermission(getContext(), android.Manifest.permission.CAMERA)) {
            makeCall(ECVoIPCallManager.CallType.VIDEO);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //提示用户开户权限
                String[] perms = {"android.permission.CAMERA"};
                ActivityCompat.requestPermissions((Activity) mView, perms, RESULT_CODE_STARTVIDEO);
            }
        }
    }

    /**
     * 权限回调
     */
    @Override
    protected void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RESULT_CODE_STARTAUDIO:
                boolean albumAccepted_audio = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (!albumAccepted_audio) {
                    Toast.makeText(getContext(), "请开启应用音频权限", Toast.LENGTH_LONG).show();
                } else {
                    makeCall(ECVoIPCallManager.CallType.VOICE);
                }
                break;
            case RESULT_CODE_STARTVIDEO:
                boolean albumAccepted_video = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (!albumAccepted_video) {
                    Toast.makeText(getContext(), "请开启应用视频权限", Toast.LENGTH_LONG).show();
                } else {
                    makeCall(ECVoIPCallManager.CallType.VIDEO);
                }
                break;
        }
    }


}
