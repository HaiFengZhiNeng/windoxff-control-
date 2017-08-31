package com.ocean.speech.asr;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.iflytek.aiui.AIUIAgent;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;
import com.iflytek.aiui.AIUIListener;
import com.iflytek.aiui.AIUIMessage;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * 语音语义
 * Created by dell on 2017/8/25.
 */

public class NlpControl {
    private static final String TAG = "Npl++";

    private AIUIAgent mAIUIAgent = null;
    private int mAIUIState = AIUIConstant.STATE_IDLE;

    private Context context;
    private AIUIListener mAIUIListener = null;

    public void setmAIUIListener(AIUIListener listener){
        this.mAIUIListener = listener;
    }

    public NlpControl(Context context) {
        this.context = context;
//        init();
    }

    /**
     * 初始化
     */
    public void init(){
        if( null == mAIUIAgent ){
            mAIUIAgent = AIUIAgent.createAgent( context, getAIUIParams(), mAIUIListener );
            AIUIMessage startMsg = new AIUIMessage(AIUIConstant.CMD_START, 0, 0, null, null);
            mAIUIAgent.sendMessage( startMsg );
        }

        if( null == mAIUIAgent ){
            final String strErrorTip = "创建 AIUI Agent 失败！";
        }

    }

    /**
     * 设置参数
     * @return
     */
    private String getAIUIParams() {
        String params = "";

        AssetManager assetManager = context.getResources().getAssets();
        try {
            InputStream ins = assetManager.open( "cfg/aiui_phone.cfg" );
            byte[] buffer = new byte[ins.available()];

            ins.read(buffer);
            ins.close();

            params = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return params;
    }

    /**
     * 文本语义
     * @param text
     */
    public void startTextNlp(String text){
        Log.i( TAG, "start text nlp" );
        String params = "data_type=text";

        if( TextUtils.isEmpty(text) ){
            text = "合肥明天的天气怎么样？";
        }

        byte[] textData = text.getBytes();

        AIUIMessage msg = new AIUIMessage(AIUIConstant.CMD_WRITE, 0, 0, params, textData);

        mAIUIAgent.sendMessage(msg);





    }

    /**
     * 语音语义
     */
    public void startVoiceNlp(){
        Log.i( TAG, "start voice nlp" );

        // 先发送唤醒消息，改变AIUI内部状态，只有唤醒状态才能接收语音输入
        if( AIUIConstant.STATE_WORKING != 	this.mAIUIState ){
            AIUIMessage wakeupMsg = new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, "", null);
            mAIUIAgent.sendMessage(wakeupMsg);
        }

        // 打开AIUI内部录音机，开始录音
        String params = "sample_rate=16000,data_type=audio";
        AIUIMessage writeMsg = new AIUIMessage( AIUIConstant.CMD_START_RECORD, 0, 0, params, null );
        mAIUIAgent.sendMessage(writeMsg);
    }

    private void showTip(String str) {
//        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
    }

    public interface OnRecognizerListener{
        void onResult(HashMap<String, Object> result);

        void onVolumeChange(int volume);

        void onEndOfSpeech();
    }
}
