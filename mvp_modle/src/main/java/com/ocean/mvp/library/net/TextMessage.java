package com.ocean.mvp.library.net;

import com.ocean.mvp.library.net.NetMessage;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 通过网络发送的文本消息
 * eg： TextMessage message = new TextMessage(getSelectRobot(), text)  (ps getSelectRobot() 在BasePresenter, text为想要发送的内容)
 * 如果想通过互联网发送此TextMessage，就可以message.setSendMethod(FileMessage.SEND_METHOD_INTERNET);
 *
 * 如果想通过局域网发送此TextMessage，就可以message.setSendMethod(FileMessage.SEND_METHOD_LAN);
 *      通过局域网发送数据可以有两个传输协议，如果通过UDP  就可以message.setTransportType(NetMessage.TRANSPORT_TYPE_UDP);
 *      如果通过TCP  就可以message.setTransportType(NetMessage.TRANSPORT_TYPE_TCP);
 *
 * Created by zhangyuanyuan on 2017/7/3.
 */

public class TextMessage extends NetMessage {


    public TextMessage() {
    }

    public TextMessage(String url) {
        super(url);
    }

    public TextMessage(String hostIp, int udpPort) {
        super(hostIp, udpPort);
    }

    public TextMessage(String hostIp, int udpPort, String content) {
        super(hostIp, udpPort, content);
    }

    /**
     * 分发消息消息需要
     * @param moduleId  接受模块的id
     * @param moduleName 接受模块的名称
     * @param actionName    接受广播的action
     * @param extraName 广播中数据的Key
     * @throws JSONException
     */
    public TextMessage(long moduleId, String moduleName,String actionName, String extraName){
        checkModuleId(moduleId);
        checkString(moduleName);
        checkString(actionName);
        checkString(extraName);
        try {
            jsonBox = new JSONObject();
            jsonBox.put("module_id",moduleId);
            jsonBox.put("module_name",moduleName);
            jsonBox.put("action_name",actionName);
            jsonBox.put("data_extra_name",extraName);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 分发消息消息需要
     * @param moduleId  接受模块的id
     * @param moduleName 接受模块的名称
     * @param actionName    接受广播的action
     * @param extraName 广播中数据的Key
     * @throws JSONException
     */
    public void setRequestAction(long moduleId, String moduleName, String actionName, String extraName){
        checkModuleId(moduleId);
        checkString(moduleName);
        checkString(actionName);
        checkString(extraName);
        try {
            jsonBox = new JSONObject();
            jsonBox.put("module_id",moduleId);
            jsonBox.put("module_name",moduleName);
            jsonBox.put("action_name",actionName);
            jsonBox.put("data_extra_name",extraName);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void checkModuleId(long moduleId) {
        if(moduleId < 10000000L) throw new RuntimeException("moduleId 为" + moduleId +",小于1000 0000");
    }

    public void checkString(String str) {
        if(str == null || str.trim().length() == 0 || "null".equals(str)) throw new RuntimeException("参数不能为空！");
    }

    @Override
    public String toString() {
        return getContent();
    }

    public JSONObject getBody() {
        return mJSONObject;
    }

    public JSONObject getBox() {
        return jsonBox;
    }

    public void setBody(String body) throws JSONException {
        mJSONObject = new JSONObject(body);
    }

    public void setBox(String box) throws JSONException {
        jsonBox = new JSONObject(box);
    }
    /**
     * 框架的Json，用来包含调用者的要发送的数据
     */
    protected JSONObject jsonBox;


    public String getContent() {
        if(jsonBox == null) return mJSONObject.toString();
        else {
            try {
                jsonBox.put("data", mJSONObject);
                return jsonBox.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


    public void setTransportType(int transportType) {
        this.transportType = transportType;
    }

}