package com.haifeng.robot.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.haifeng.robot.R;
import com.haifeng.robot.bean.ChatMsg;
import com.haifeng.robot.utils.ViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dell on 2017/8/5.
 */

public class ChatMsgAdapter extends BaseAdapter {

    private List<ChatMsg> mChatMsgList = new ArrayList<>();
    private Context mContext;
    private LayoutInflater mInflater;
    private boolean mIsWho = true;

    public ChatMsgAdapter(Context context, boolean isWho) {
        this.mContext = context;
        this.mIsWho = isWho;
        mInflater = LayoutInflater.from(mContext);
    }

    public void notifyData(List<ChatMsg> chatMsgList) {
        this.mChatMsgList = chatMsgList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mChatMsgList.size();
    }

    @Override
    public Object getItem(int position) {
        return mChatMsgList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.layout_chat_item, parent, false);
        }
        ChatMsg chatMsg = mChatMsgList.get(position);
        Log.e("key", mChatMsgList.size() + mChatMsgList.get(position).getMsgBody());
        RelativeLayout rl_mySend = ViewHolder.get(convertView, R.id.rl_mySend);
        LinearLayout ll_otherSend = ViewHolder.get(convertView, R.id.ll_otherSend);
        TextView tv_chatContent = ViewHolder.get(convertView, R.id.tv_chatContentMy);
        TextView tv_chatContentOther = ViewHolder.get(convertView, R.id.tv_chatContentOther);
        // 0 我发送消息  1  接收消息
        if (chatMsg.getType() == 0) {
            rl_mySend.setVisibility(View.VISIBLE);
            tv_chatContent.setText(chatMsg.getMsgBody());
        } else {
            ll_otherSend.setVisibility(View.VISIBLE);
            tv_chatContentOther.setText(chatMsg.getMsgBody());
        }
        return convertView;
    }


}
