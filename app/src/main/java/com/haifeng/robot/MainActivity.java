package com.haifeng.robot;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.haifeng.robot.activity.BaseVoipActivity;
import com.haifeng.robot.activity.ChattingActivity;
import com.haifeng.robot.activity.VideoActivity;
import com.haifeng.robot.activity.VoIPCallActivity;
import com.haifeng.robot.config.Constant;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECVoIPCallManager;

import static com.haifeng.robot.config.Constant.ACTION_CALLBACKING;

public class MainActivity extends BaseVoipActivity implements View.OnClickListener {

    public static final int RESULT_CODE_STARTAUDIO = 100;
    public static final int RESULT_CODE_STARTVIDEO = 200;
    //语音
    private Button mEntrance_voice;
    //视频
    private Button mEntrance_video;
    //IM
    private Button entrance_chat;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;

        setContentView(R.layout.activity_main);

        (mEntrance_voice) = (Button) findViewById(R.id.entrance_voice);
        (mEntrance_video) = (Button) findViewById(R.id.entrance_video);
        (entrance_chat) = (Button) findViewById(R.id.entrance_chat);

        mEntrance_voice.setOnClickListener(this);
        mEntrance_video.setOnClickListener(this);
        entrance_chat.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.entrance_video:
                videoPermission();
                break;
            case R.id.entrance_voice:
                audioPermission();
                break;
            case R.id.entrance_chat:
                Intent intent_chat = new Intent(context, ChattingActivity.class);
                intent_chat.setClass(context, ChattingActivity.class);
//                intent_chat.putExtra(Constant.RECIPIENTS, contactid);
//                intent_chat.putExtra(Constant.CONTACT_USER, username);
                intent_chat.putExtra(Constant.CUSTOMER_SERVICE, false);
                context.startActivity(intent_chat);
                break;
        }
    }


    /**
     * 音频呼叫
     */
    private void makeCall(ECVoIPCallManager.CallType callType) {
        //说明：mCurrentCallId如果返回空则代表呼叫失败，可能是参数错误引起。否则返回是一串数字，是当前通话的标识。
        String mCurrentCallId = ECDevice.getECVoIPCallManager().makeCall(callType, "17600738557");//17600738557
        if (!"".equals(mCurrentCallId)) {
            Intent callAction = new Intent();
            //视频
            if (ECVoIPCallManager.CallType.VIDEO.equals(callType)) {
                callAction.setClass(context, VideoActivity.class);
                callAction.putExtra(ACTION_CALLBACKING, true);
            } else {
                //音频
                callAction.setClass(context, VoIPCallActivity.class);
                callAction.putExtra(ACTION_CALLBACKING, false);
            }
            //是否正在通话
//            callAction.putExtra(Constant.ACTION_CALLBACK_CALL, true);

            callAction.putExtra(Constant.EXTRA_CALL_NAME, "17600738557");
            callAction.putExtra(Constant.EXTRA_CALL_NUMBER, "17600738557");
            callAction.putExtra(ECDevice.CALLTYPE, callType);
            callAction.putExtra(ECDevice.CALLID, mCurrentCallId);
            callAction.putExtra(Constant.EXTRA_OUTGOING_CALL, true);
            context.startActivity(callAction);
        } else {
            Toast.makeText(this, "发起失败", Toast.LENGTH_LONG).show();
        }

    }

    /**
     * 音频权限
     */
    public void audioPermission() {
        if (PackageManager.PERMISSION_GRANTED == ContextCompat.
                checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO)) {
            makeCall(ECVoIPCallManager.CallType.VOICE);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //提示用户开户权限音频
                String[] perms = {"android.permission.RECORD_AUDIO"};
                ActivityCompat.requestPermissions(MainActivity.this, perms, RESULT_CODE_STARTAUDIO);
            }
        }

    }

    //视频权限
    public void videoPermission() {
        if (PackageManager.PERMISSION_GRANTED == ContextCompat.
                checkSelfPermission(context, android.Manifest.permission.CAMERA)) {
            makeCall(ECVoIPCallManager.CallType.VIDEO);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //提示用户开户权限
                String[] perms = {"android.permission.CAMERA"};
                ActivityCompat.requestPermissions(MainActivity.this, perms, RESULT_CODE_STARTVIDEO);
            }
        }
    }

    /**
     * 权限回调
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {
        switch (permsRequestCode) {
            case RESULT_CODE_STARTAUDIO:
                boolean albumAccepted_audio = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (!albumAccepted_audio) {
                    Toast.makeText(MainActivity.this, "请开启应用音频权限", Toast.LENGTH_LONG).show();
                } else {
                    makeCall(ECVoIPCallManager.CallType.VOICE);
                }
                break;
            case RESULT_CODE_STARTVIDEO:
                boolean albumAccepted_video = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (!albumAccepted_video) {
                    Toast.makeText(MainActivity.this, "请开启应用视频权限", Toast.LENGTH_LONG).show();
                } else {
                    makeCall(ECVoIPCallManager.CallType.VIDEO);
                }
                break;
        }
    }

}
