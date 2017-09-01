package com.haifeng.robot.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.haifeng.robot.R;
import com.haifeng.robot.adapter.ChatMsgAdapter;
import com.haifeng.robot.bean.ChatMsg;
import com.yuntongxun.ecsdk.ECChatManager;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECMessage;
import com.yuntongxun.ecsdk.OnChatReceiveListener;
import com.yuntongxun.ecsdk.im.ECMessageNotify;
import com.yuntongxun.ecsdk.im.ECTextMessageBody;
import com.yuntongxun.ecsdk.im.group.ECGroupNoticeMessage;

import java.util.ArrayList;
import java.util.List;

public class ChattingActivity extends BaseVoipActivity implements View.OnClickListener {

    private ImageView iv_top_back;
    private TextView tv_top_center;
    private EditText et_msg;
    private Button sendMsg_btn;
    private List<ChatMsg> mChatMsgs = new ArrayList<>();
    private ChatMsgAdapter chatMsgAdapter;
    private ListView lv_chat;
    private ChatMsg chatMsg;
    String textMsg = "";
    private ECMessage msg_send;
    private ECMessage msg_receive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        initViewJect();
        initData();

        chatMsgAdapter = new ChatMsgAdapter(ChattingActivity.this, false);
        lv_chat.setAdapter(chatMsgAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //IM接收消息监听，使用IM功能的开发者需要设置。
        ECDevice.setOnChatReceiveListener(new OnChatReceiveListener() {
            @Override
            public void OnReceivedMessage(ECMessage msg) {
                Log.e("key", "==收到新消息");
//                mHandler.sendEmptyMessage(0);
                getReceivedMessage(msg);
            }

            @Override
            public void onReceiveMessageNotify(ECMessageNotify ecMessageNotify) {

            }

            @Override
            public void OnReceiveGroupNoticeMessage(ECGroupNoticeMessage notice) {
                //收到群组通知消息,可以根据ECGroupNoticeMessage.ECGroupMessageType类型区分不同消息类型
                Log.i("", "==收到群组通知消息（有人加入、退出...）");
            }

            @Override
            public void onOfflineMessageCount(int count) {
                // 登陆成功之后SDK回调该接口通知帐号离线消息数
            }

            @Override
            public int onGetOfflineMessage() {
                return 0;
            }

            @Override
            public void onReceiveOfflineMessage(List msgs) {
                // SDK根据应用设置的离线消息拉取规则通知应用离线消息
            }

            @Override
            public void onReceiveOfflineMessageCompletion() {
                // SDK通知应用离线消息拉取完成
            }

            @Override
            public void onServicePersonVersion(int version) {
                // SDK通知应用当前帐号的个人信息版本号
            }

            @Override
            public void onReceiveDeskMessage(ECMessage ecMessage) {

            }

            @Override
            public void onSoftVersion(String s, int i) {

            }
        });

    }

    private void initData() {
        // 组建一个待发送的ECMessage
        msg_send = ECMessage.createECMessage(ECMessage.Type.TXT);
        // 设置消息接收者
        msg_send.setTo("17600738557");
        tv_top_center.setText("17600738557");
        msg_receive = ECMessage.createECMessage(ECMessage.Type.TXT);
        msg_receive.setTo("17600738557");
    }

    private void initViewJect() {
        (tv_top_center) = (TextView) findViewById(R.id.tv_top_center);
        (iv_top_back) = (ImageView) findViewById(R.id.iv_top_back);
        (et_msg) = (EditText) findViewById(R.id.et_msg);
        (sendMsg_btn) = (Button) findViewById(R.id.sendMsg_btn);
        (lv_chat) = (ListView) findViewById(R.id.lv_chat);

        iv_top_back.setOnClickListener(this);
        sendMsg_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_top_back:
                finish();
                break;
            case R.id.sendMsg_btn:
                doSendMsg();
                break;
        }
    }


    private void doSendMsg() {
        textMsg = et_msg.getText().toString();
        if (!TextUtils.isEmpty(textMsg)) {
            // 创建一个文本消息体，并添加到消息对象中
            ECTextMessageBody msgBody = new ECTextMessageBody(textMsg);
            msg_send.setBody(msgBody);
            // 调用SDK发送接口发送消息到服务器
            ECChatManager manager = ECDevice.getECChatManager();
            msg_send.setMsgTime(System.currentTimeMillis());
            if (manager == null) {
                msg_send.setMsgStatus(ECMessage.MessageStatus.FAILED);
                return;
            }
            manager.sendMessage(msg_send, onSendMessageListener);
        }
    }

    ECChatManager.OnSendMessageListener onSendMessageListener = new ECChatManager.OnSendMessageListener() {
        @Override
        public void onSendMessageComplete(ECError ecError, ECMessage ecMessage) {
            // 处理消息发送结果
            if (ecMessage == null) {
                return;
            }
            // 将发送的消息更新到本地数据库并刷新UI
            Log.e("key", ecError.toString());
            if (ecError.errorCode == 200) {

                et_msg.setText("");
                chatMsg = new ChatMsg();
                chatMsg.setMsgBody(textMsg);
                chatMsg.setType(0);
                mChatMsgs.add(chatMsg);
                chatMsgAdapter.notifyData(mChatMsgs);
            } else {
                Toast.makeText(ChattingActivity.this, "发送失败", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onProgress(String s, int i, int i1) {

            // 处理文件发送上传进度（尽上传文件、图片时候SDK回调该方法）
            Log.e("key", "[IMChattingHelper - onProgress] msgId：" + s
                    + " ,total：" + i + " ,progress:" + i1);
        }
    };

    public void getReceivedMessage(ECMessage msg) {
        if (msg == null) {
            return;
        }

        // 接收到的IM消息，根据IM消息类型做不同的处理(IM消息类型：ECMessage.Type)
        ECMessage.Type type = msg.getType();
        if (type == ECMessage.Type.TXT) {
            // 在这里处理文本消息
            ECTextMessageBody textMessageBody = (ECTextMessageBody) msg.getBody();
            Log.e("key", "textMessageBody" + textMessageBody);
            if (textMessageBody != null) {
                chatMsg = new ChatMsg();
                chatMsg.setMsgBody(textMessageBody.getMessage());
                chatMsg.setType(1);
                mChatMsgs.add(chatMsg);
                chatMsgAdapter.notifyData(mChatMsgs);
            } else {
//                Toast.makeText(ChattingActivity.this, "暂无消息", Toast.LENGTH_LONG).show();
            }
        }
        // 根据不同类型处理完消息之后，将消息序列化到本地存储（sqlite）
        // 通知UI有新消息到达


    }
}
