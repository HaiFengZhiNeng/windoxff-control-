package com.haifeng.robot.bean;

/**
 * Created by dell on 2017/8/7.
 */

public class ReceiverChatMsg {
    private String msgBody;
    private int id;

    public String getMsgBody() {
        return msgBody;
    }

    public void setMsgBody(String msgBody) {
        this.msgBody = msgBody;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "ChatMsg{" +
                "msgBody='" + msgBody + '\'' +
                ", id=" + id +
                '}';
    }
}
