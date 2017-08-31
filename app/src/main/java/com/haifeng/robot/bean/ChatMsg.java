package com.haifeng.robot.bean;

/**
 * Created by dell on 2017/8/5.
 */

public class ChatMsg {
    private String msgBody;
    private int id;
    private int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

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
                ", type=" + type +
                '}';
    }
}
