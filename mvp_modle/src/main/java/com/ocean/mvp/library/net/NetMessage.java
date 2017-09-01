package com.ocean.mvp.library.net;

import com.ocean.mvp.library.utils.JsonBuilder;
import com.ocean.mvp.library.progress.UIProgressListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;

/**
 * 通过网络发送的消息（Json消息，文件上传）
 * Created by zhangyuanyuan on 2017/7/3.
 */

public class NetMessage {
    /**
     * 连接方式，只通过互联网发送数据
     */
    public static final int SEND_METHOD_INTERNET = 1;
    /**
     * 连接方式，只通过局域网有线
     */
    public static final int SEND_METHOD_LAN = 2;

    /**
     * 传输协议，局域网传输数据情况下此参数有效，通过UDP协议发送数据
     */
    public static final int TRANSPORT_TYPE_UDP = 0;

    /**
     * 传输协议，局域网传输数据情况下此参数有效，通过TCP协议发送数据
     */
    public static final int TRANSPORT_TYPE_TCP = 1;


    public static final int REQUEST_METHOD_GET = 0;
    public static final int REQUEST_METHOD_POST = 1;
    public static final int REQUEST_METHOD_DELETE = 2;
    public static final int REQUEST_METHOD_PUT = 3;

    public static int CONNECT_TIMEOUT = 0;
    public static int READ_TIMEOUT = 30;


    private String mediaTypeMarkdown = "application/json; charset=utf-8";


    private UIProgressListener uiProgressListener;

    /**
     * 设置网络访问超时限制
     *
     * @param timeout 限制时间
     */
    public void setConnectTimeout(int timeout) {
        CONNECT_TIMEOUT = timeout;
    }

    /**
     * 获取网络访问超时限制
     *
     * @return 限制时间
     */
    public int getConnectTimeout() {
        return CONNECT_TIMEOUT;
    }

    public int getReadTimeOut() {
        return READ_TIMEOUT;
    }

    public void setReadTimeOut(int readTimeout) {
        this.READ_TIMEOUT = readTimeout;
    }

    /**
     * 设置进度监听
     *
     * @param uiProgressListener 进度监听
     */
    public void setUiProgressListener(UIProgressListener uiProgressListener) {
        this.uiProgressListener = uiProgressListener;
    }

    /**
     * 获取进度监听
     *
     * @return 进度监听
     */
    public UIProgressListener getUiProgressListener() {
        return uiProgressListener;
    }


    /**
     * 此消息的Id
     */
    protected long id;

    private String url;

    private int requestMethod = REQUEST_METHOD_POST;


    /**
     * 需要传输的数据（Json格式）
     */
    protected JSONObject mJSONObject;

    /**
     * 无参数的构造函数
     */
    public NetMessage() {
        id = System.currentTimeMillis();
        mJSONObject = new JSONObject();
    }

    /**
     * 需要上传给服务器时使用此构造函数
     *
     * @param url 访问地址
     */
    public NetMessage(String url) {
        id = System.currentTimeMillis();
        this.url = url;
        mJSONObject = new JSONObject();
        sendMethod = SEND_METHOD_INTERNET;
    }

    /**
     * 发送目标的ip地址
     */
    protected String hostIp;


    protected int udpPort = SocketManager.DEFAULT_UDPSERVER_PORT;
    protected int tcpPort = SocketManager.DEFAULT_TCPSERVER_PORT;

    /**
     * 发送数据的方法，SEND_METHOD_AUTO，SEND_METHOD_INTERNET，SEND__METHOD_LAN
     */
    protected int sendMethod = SEND_METHOD_LAN;

    /**
     * 发送数据的协议
     * TRANSPORT_TYPE_UDP, TRANSPORT_TYPE_TCP
     */
    protected int transportType = TRANSPORT_TYPE_TCP;


    /**
     * 通过这个构造函数的消息，只能通过局域网，Udp协议发送此消息
     *
     * @param hostIp  接收方IP
     * @param udpPort 接收方端口
     */
    public NetMessage(String hostIp, int udpPort) {
        id = System.currentTimeMillis();
        this.hostIp = hostIp;
        this.udpPort = udpPort;
    }

    /**
     * 通过这个构造函数的消息，只能通过局域网，Udp协议发送此消息
     *
     * @param hostIp  接收方IP
     * @param udpPort 接收方端口
     * @param content 发给接收方的内容
     */
    public NetMessage(String hostIp, int udpPort, String content) {
        try {
            id = System.currentTimeMillis();
            this.hostIp = hostIp;
            this.udpPort = udpPort;
            mJSONObject = new JSONObject(content);
            sendMethod = SEND_METHOD_LAN;
            transportType = TRANSPORT_TYPE_UDP;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setIp(String ip) {
        this.hostIp = ip;
    }

    public void setTcpPort(int tcpPort) {
        this.tcpPort = tcpPort;
    }

    public void setUdpPort(int udpPort) {
        this.udpPort = udpPort;
    }


    public void setSendMethod(int sendMethod) {
        this.sendMethod = sendMethod;
    }

    public int getSendMethod() {
        return sendMethod;
    }

    public int getTransportType() {
        return transportType;
    }

    public void setMediaTypeMarkdown(String mediaTypeMarkdown) {
        this.mediaTypeMarkdown = mediaTypeMarkdown;
    }

    public MediaType getMediaType()  {
        return MediaType.parse(mediaTypeMarkdown);
    }


    /**
     * 返回消息自动生成的id
     *
     * @return 消息的ID
     */
    public long getId() {
        return id;
    }

    public void setUrl(String url) {
        this.url = url;
        sendMethod = SEND_METHOD_INTERNET;
    }

    public void setRequestMethod(int requestMethod) {
        this.requestMethod = requestMethod;
    }

    public int getRequestMethod() {
        return requestMethod;
    }

    public String getUrl() {
        return url;
    }

    /**
     * @return 返回需要传递给服务器的String内容
     */
    public String getContent() {
        return mJSONObject.toString();
    }

    /**
     * 设置需要传递给服务器的内容
     *
     * @param content 需要传递给服务器的内容
     */
    public void setContent(String content) throws JSONException {
        mJSONObject = new JSONObject(content);
    }

    public void clean() {
        mJSONObject = new JSONObject();
    }

    /**
     * 设置需要传递给服务器的内容
     *
     * @param mJSONObject 需要传递给服务器的内容
     */
    public void setContent(JSONObject mJSONObject) {
        this.mJSONObject = mJSONObject;
    }

    /**
     * 添加需要传递给服务器的Json内容
     *
     * @param key   Json的key
     * @param value int 类型的value
     * @return 返回NetMessage对象，方便多次调用append（）方法
     * @throws JSONException Json异常
     */
    public NetMessage append(String key, int value) throws JSONException {
        mJSONObject.put(key, value);
        return this;
    }

    /**
     * 添加需要传递给服务器的Json内容
     *
     * @param key   Json的key
     * @param value JSONArray 类型的value
     * @return 返回NetMessage对象，方便多次调用append（）方法
     * @throws JSONException Json异常
     */
    public NetMessage append(String key, JSONArray value) throws JSONException {
        mJSONObject.put(key, value);
        return this;
    }

    /**
     * 添加需要传递给服务器的Json内容
     *
     * @param key   Json的key
     * @param value JsonBuilder 类型的value
     * @return 返回NetMessage对象，方便多次调用append（）方法
     * @throws JSONException Json异常
     */
    public NetMessage append(String key, JsonBuilder value) throws JSONException {
        mJSONObject.put(key, value.builder());
        return this;
    }

    /**
     * 添加需要传递给服务器的Json内容
     *
     * @param key   Json的key
     * @param value Object 类型的value
     * @return 返回NetMessage对象，方便多次调用append（）方法
     * @throws JSONException Json异常
     */
    public NetMessage append(String key, Object value) throws JSONException {
        mJSONObject.put(key, value);
        return this;
    }

    /**
     * 添加需要传递给服务器的Json内容
     *
     * @param key   Json的key
     * @param value boolean 类型的value
     * @return 返回NetMessage对象，方便多次调用append（）方法
     * @throws JSONException Json异常
     */
    public NetMessage append(String key, boolean value) throws JSONException {
        mJSONObject.put(key, value);
        return this;
    }

    /**
     * 添加需要传递给服务器的Json内容
     *
     * @param key   Json的key
     * @param value double 类型的value
     * @return 返回NetMessage对象，方便多次调用append（）方法
     * @throws JSONException Json异常
     */
    public NetMessage append(String key, double value) throws JSONException {
        mJSONObject.put(key, value);
        return this;
    }

    /**
     * 添加需要传递给服务器的Json内容
     *
     * @param key   Json的key
     * @param value long 类型的value
     * @return 返回NetMessage对象，方便多次调用append（）方法
     * @throws JSONException Json异常
     */
    public NetMessage append(String key, long value) throws JSONException {
        mJSONObject.put(key, value);
        return this;
    }
}
