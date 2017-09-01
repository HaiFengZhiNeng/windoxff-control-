package com.ocean.mvp.library.net;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ocean.mvp.library.download.IGetFileStreamListener;
import com.ocean.mvp.library.utils.L;
import com.ocean.mvp.library.utils.NetUtil;
import com.ocean.mvp.library.progress.ProgressHelper;
import com.ocean.mvp.library.utils.UrlFomatUtils;
import com.ocean.mvp.library.download.DownLoadManager;
import com.ocean.mvp.library.progress.DownLoadProgressListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.CookieHandler;
import java.net.DatagramPacket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

import static android.content.ContentValues.TAG;

/**
 * Created by zhangyuanyuan on 2017/7/3.
 */

public class NetClient {

    private final static String TAG = "NetClient";

    public static final int ERROR_CODE_UNKNOWN = -1;
    public static final int ERROR_CODE_SUCCESS = 0;
    public static final int ERROR_CODE_NO_NETWORK = -21;
    public static final int ERROR_CODE_UNKNOWN_CONNECT = -3;
    public static final int ERROR_CODE_TIME_OUT = -4;
    public static final int ERROR_CODE_RESPONSE_CODE_EXCEPTION = -5;
    public static final int ERROR_CONNECTEXCEPTION = -6;

    private Context mContext;

    private UdpRegisterRequestListener udpServerRequestListener;
    private TcpRegisterRequestListener tcpServerRequestListener;
    private TcpConnRequestListener tcpConnRequestListener;
    private SendRequestListener globalSendRequestListener;

    private BaseHandler messageHandler;
    private SocketManager socketClient;
    private OkHttpClient mOkHttpClient;

    //默认不支持Https
    private boolean mSupportHttps = false;
    private boolean mSupportCookie = false;
    private static NetClient client;
    private DownLoadManager mDownLoadInstance;
    private Call mCall;
    private CookieHandler mCookieHandler;
    /**
     * 是否支持断点续传
     */
    private boolean mAutoResume = false;//是否支持断点续传;

    private NetClient(Context mContext) {
        this.mContext = mContext;
        //初始化一个互联网访问端
        mOkHttpClient = new OkHttpClient();
        //初始化一个局域网访问端
        messageHandler = new BaseHandler(this);
        mDownLoadInstance = DownLoadManager.getInstance();
    }

    public SocketManager getSocketManager() {
        if (socketClient == null) socketClient = SocketManager.getInstance(mContext);
        return socketClient;
    }

    public static NetClient getInstance(Context mContext) {
        if (client == null) {
            synchronized (NetClient.class) {
                if (client == null)
                    client = new NetClient(mContext.getApplicationContext());
            }
        }
        return client;
    }

    /**
     * 设置是否支持HTTPS
     *
     * @param supportHttps true 是 false 否
     */
    public void setSupportHttps(boolean supportHttps) {
        this.mSupportHttps = supportHttps;

    }

    public void setSupportCookie(boolean isSupport) {
        this.mSupportCookie = isSupport;
    }

    public void setSupportCookie(CookieHandler cookieHandler) {
        this.mSupportCookie = true;
        this.mCookieHandler = cookieHandler;
    }

    /**
     * 通过互联网/局域网发送数据（文本数据或者文件数据）
     *
     * @param message   被发送的数据
     * @param mListener 发送的数据监听
     */
    public void sendNetMessage(NetMessage message, SendRequestListener mListener) {
        if (message != null) {
            if (message.getUrl() != null)
                this.mSupportHttps = message.getUrl().startsWith("https");
            requestMap.put(message.getId(), new NetRequest(message, mListener));
            int requestConnType = message.getSendMethod();

            if (requestConnType == TextMessage.SEND_METHOD_INTERNET) {
                sendNetMessageByInternet(message);
            } else if (requestConnType == TextMessage.SEND_METHOD_LAN) {
                sendNetMessageByLan(message);
            } else {
                notifyFailMessage(message.getId(), ERROR_CODE_UNKNOWN_CONNECT, " 未设置连接类型？( TextMessage.SEND_METHOD_INTERNET or TextMessage.SEND_METHOD_LAN)");
            }
        }
    }

    public void setSendRequestListener(SendRequestListener globalSendRequestListener) {
        this.globalSendRequestListener = globalSendRequestListener;
    }

    /**
     * 监听本地的端口（Udp方式监听）
     *
     * @param listener 被连接的监听
     */
    public void registerUdpServer(UdpRegisterRequestListener listener) {
        this.udpServerRequestListener = listener;
        if (socketClient == null) socketClient = SocketManager.getInstance(mContext);

        socketClient.registerUdpServer(new SocketManager.UdpServerListener() {
            @Override
            public void onReceive(DatagramPacket recvPacket) {
                String recvStr = new String(recvPacket.getData(), 0, recvPacket.getLength());
                if (udpServerRequestListener != null)
                    udpServerRequestListener.onReceive(recvPacket.getAddress().getHostAddress(), recvPacket.getPort(), recvStr);
            }

            @Override
            public void onFail(Exception e) {
                if (udpServerRequestListener != null)
                    udpServerRequestListener.onFail(e);
            }
        });
    }

    /**
     * 烦注册本地端口（Udp方式监听）
     */
    public void unRegisterUdpServer() {
        if (socketClient == null)
            socketClient = SocketManager.getInstance(mContext);
        socketClient.unRegisterUdpServer();
    }

    public void connectSocketByTcp(String mAddress, int mPort, SocketManager.TcpConnListener mListener) {
        if (socketClient == null)
            socketClient = SocketManager.getInstance(mContext);
        socketClient.connectByTcp(mAddress, mPort, mListener);
    }

    /**
     * 监听本地的端口（Tcp方式监听）
     *
     * @param listener 被连接的监听
     */
    public void registerTcpServer(TcpRegisterRequestListener listener) {
        this.tcpServerRequestListener = listener;
        if (socketClient == null) socketClient = SocketManager.getInstance(mContext);

        socketClient.registerTcpServer(new SocketManager.TcpClientListener() {
            @Override
            public void onSuccess(String accept) {
                if (tcpServerRequestListener != null)
                    tcpServerRequestListener.onReceive(accept);
            }

            @Override
            public void onFail(Exception e) {
                if (tcpServerRequestListener != null)
                    tcpServerRequestListener.onFail(e);
            }
        });

    }

    /**
     * 反注册本地端口（Tcp方式监听）
     */
    public void unRegisterTcpServer() {
        if (socketClient == null)
            socketClient = SocketManager.getInstance(mContext);
        socketClient.unRegisterTcpServer();
    }

    public void sendTextMessageByTcp(String host, int port, String text, final SocketRequestListener socketRequestListener){
        if (socketClient == null)
            socketClient = SocketManager.getInstance(mContext);
        socketClient.sendTextByTCPConnected(host, port, text, new SocketManager.TCPTextSendListener() {
            @Override
            public void onFail(Exception e) {
                if (socketRequestListener != null)
                    socketRequestListener.onFail(e);
            }

            @Override
            public void onSuccess(String result) {
                if (socketRequestListener != null)
                    socketRequestListener.onSuccess(result);
            }
        });
    }

    /**
     * 通过互联网发送数据（文本数据或者文件数据）
     *
     * @param msg 被发送的数据
     */
    public void sendNetMessageByInternet(final NetMessage msg) {
        if (!NetUtil.checkNet(mContext)) {
            notifyFailMessage(msg.getId(), ERROR_CODE_NO_NETWORK, "do you connection Internet?");
            return;
        }
        if (msg != null && msg.getUrl() != null)
            this.mSupportHttps = msg.getUrl().startsWith("https");
        Request.Builder builder = new Request.Builder();
        builder.url(msg.getUrl());

        if (msg instanceof FileMessage) {
            /*FileMessage fileMessage = (FileMessage) msg;
            File file = fileMessage.getFilePaths().get(0);

            MultipartBuilder requestBody = new MultipartBuilder() //建立请求的内容
                    .type(MultipartBuilder.FORM);
            JSONObject bodyJson = fileMessage.getBody();
            Iterator<String> keys = bodyJson.keys();
            while (keys.hasNext()) {
                String next = keys.next();
                try {
                    requestBody.addFormDataPart(next, bodyJson.getString(next));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            requestBody.addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse("multipart/form-data"), file));
            RequestBody formBody = requestBody.build();

            if (msg.getUiProgressListener() != null) {
                builder.post(ProgressHelper.addProgressRequestListener(formBody, msg.getUiProgressListener()))
                        .build();
            } else {
                builder.post(formBody)
                        .build();
            }*/

        } else {

            int requestMethod = msg.getRequestMethod();
            if (requestMethod == NetMessage.REQUEST_METHOD_POST) {
                builder.post(RequestBody.create(msg.getMediaType(), msg.getContent()));
            } else if (requestMethod == NetMessage.REQUEST_METHOD_DELETE) {
                if (msg.getContent() != null && msg.getContent().trim().length() != 0)
                    builder.delete(RequestBody.create(msg.getMediaType(), msg.getContent()));
                else builder.delete();
            } else if (requestMethod == NetMessage.REQUEST_METHOD_PUT) {
                builder.put(RequestBody.create(msg.getMediaType(), msg.getContent()));
            } else if (requestMethod == NetMessage.REQUEST_METHOD_GET) {
                builder.get();
            }
        }


        /*if (msg.getConnectTimeout() != 0) {
            mOkHttpClient.setConnectTimeout(msg.getConnectTimeout(), TimeUnit.SECONDS);//连接
        }*/

        if (mSupportHttps) {
            try {
                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, new TrustManager[]{
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(final X509Certificate[] chain, final String
                                    authType)
                                    throws CertificateException {
                            }

                            @Override
                            public void checkServerTrusted(final X509Certificate[] chain, final String
                                    authType)
                                    throws CertificateException {
                            }

                            @Override
                            public X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[0];
                            }
                        }
                }, new SecureRandom());
                /*mOkHttpClient.setSslSocketFactory(sslContext.getSocketFactory());
                mOkHttpClient.setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(final String hostname, final SSLSession session) {
                        return true;
                    }
                });*/
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e1) {

            }

        }
        /*if (mSupportCookie) {
            if (mCookieHandler == null) {
                mCookieHandler = new CookieManager(new PersistentCookieStore(mContext), CookiePolicy.ACCEPT_ALL);
            }
            mOkHttpClient.setCookieHandler(mCookieHandler);
        }
        mOkHttpClient.setReadTimeout(msg.getReadTimeOut(), TimeUnit.SECONDS);*/

        mOkHttpClient.newCall(builder.build()).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                dealWithException(e, msg);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() != 200) {
                    notifyFailMessage(msg.getId(), ERROR_CODE_RESPONSE_CODE_EXCEPTION, "response code : " + response.code());
                } else {
                    try {
                        String responseStr = response.body().string();
                        notifySuccessMessage(msg.getId(), responseStr);
                    } catch (Exception e) {
                        dealWithException(e, msg);
                    }
                }
            }
        });
    }

    private void dealWithException(Exception e, NetMessage msg) {
        if (e != null) {

            String message = e.getMessage();

            if (e instanceof SocketTimeoutException) {
                notifyFailMessage(msg.getId(), ERROR_CODE_TIME_OUT, message != null ? message : e.getClass().getName());
            } else {
                notifyFailMessage(msg.getId(), ERROR_CODE_UNKNOWN, message != null ? message : e.getClass().getName());
            }

        } else {
            notifyFailMessage(msg.getId(), ERROR_CODE_UNKNOWN, "unknown exception");
        }

    }


    private void notifyDoingMessage(long messageId, long fileLength, long current) {
        NetRequest netRequest = requestMap.get(messageId);
        if (netRequest == null) {
            L.w(TAG, "notifyFailMessage fail");
        } else {
            netRequest.setDoing(fileLength, current);
            Message message = Message.obtain();
            message.what = WHAT_REQUEST_UPDATE;
            message.obj = messageId;
            messageHandler.sendMessage(message);
        }
    }

    private void notifyFailMessage(long messageId, int errorCode, String errorMessage) {
        NetRequest netRequest = requestMap.get(messageId);
        if (netRequest == null) {
            L.w(TAG, "notifyFailMessage fail");
        } else {
            netRequest.setFail(errorCode, errorMessage);
            Message message = Message.obtain();
            message.what = WHAT_REQUEST_FAILED;
            message.obj = messageId;
            messageHandler.sendMessage(message);
        }
    }

    private void notifySuccessMessage(long messageId, String result) {
        NetRequest netRequest = requestMap.get(messageId);
        if (netRequest == null) {
            L.w(TAG, "notifySuccessMessage fail");
        } else {
            netRequest.setSuccess(result);
            Message message = Message.obtain();
            message.what = WHAT_REQUEST_SUCCESS;
            message.obj = messageId;
            messageHandler.sendMessage(message);
        }
    }


    /**
     * 通过局域网发送数据（文本数据或者文件数据）
     *
     * @param msg 被发送的数据
     */
    public void sendNetMessageByLan(final NetMessage msg) {
        if (socketClient == null) socketClient = SocketManager.getInstance(mContext);
        switch (msg.getTransportType()) {
            case TextMessage.TRANSPORT_TYPE_TCP:
                if (msg instanceof FileMessage) {
                    socketClient.sendFileByTCP(msg.hostIp, msg.tcpPort, msg.getContent(), ((FileMessage) msg).getFilePaths(), new SocketManager.TCPFileSendListener() {

                        @Override
                        public void onSendingFile(File file, long filesTotal, long fileListSendCount, long fileLength, long current) {
                            notifyDoingMessage(msg.getId(), filesTotal, fileListSendCount);
                        }

                        @Override
                        public void onSendSuccessFile(File file) {

                        }

                        @Override
                        public void onFail(Exception e) {
                            if (e instanceof ConnectException) {
                                notifyFailMessage(msg.getId(), ERROR_CONNECTEXCEPTION, e.getMessage());
                            } else {
                                notifyFailMessage(msg.getId(), ERROR_CODE_UNKNOWN, e.getMessage());
                            }
                        }

                        @Override
                        public void onSuccess(String result) {
                            notifySuccessMessage(msg.getId(), result);
                        }
                    });
                } else if (msg instanceof TextMessage) {
                    socketClient.sendTextByTCP(msg.hostIp, msg.tcpPort, msg.getContent(), new SocketManager.TCPTextSendListener() {

                        @Override
                        public void onFail(Exception e) {
                            notifyFailMessage(msg.getId(), ERROR_CODE_UNKNOWN, e.getMessage());
                        }

                        @Override
                        public void onSuccess(String result) {
                            notifySuccessMessage(msg.getId(), result);
                        }
                    });
                }

                break;
            case TextMessage.TRANSPORT_TYPE_UDP:
                if (msg.getClass().equals(TextMessage.class)) {
                    socketClient.sendTextByUDP(msg.hostIp, msg.udpPort, msg.getContent());
                } else if (msg.getClass().equals(FileMessage.class)) {
                    throw new RuntimeException("you can\'t send file by udp");
                }
                break;
            default:
                throw new RuntimeException("unknown NetMessage conntype");
        }
    }

    public void sendByteMessageByUdp(String ip,int port,byte[] bytes){
        if (socketClient == null) socketClient = SocketManager.getInstance(mContext);
        socketClient.sendByteByUdp(ip,port,bytes);
    }

    public void sendTextMessageByUdp(String ip,int port,String msg){
        if (socketClient == null) socketClient = SocketManager.getInstance(mContext);
        socketClient.sendTextByUDP(ip,port,msg);
    }

    private Map<Long, NetRequest> requestMap = new HashMap<Long, NetRequest>();

    protected static class BaseHandler extends Handler {
        private final WeakReference<NetClient> mObjects;

        public BaseHandler(NetClient objects) {
            mObjects = new WeakReference<NetClient>(objects);
        }

        @Override
        public void handleMessage(Message msg) {
            NetClient objects = mObjects.get();
            objects.handleMessage(msg);
        }
    }


    private static final int WHAT_REQUEST_SUCCESS = 0;
    private static final int WHAT_REQUEST_UPDATE = 1;
    private static final int WHAT_REQUEST_FAILED = 2;

    private void handleMessage(Message msg) {
        long requestIndex = (Long) msg.obj;
        switch (msg.what) {
            case WHAT_REQUEST_SUCCESS:
            case WHAT_REQUEST_FAILED:
                NetRequest netRequest = requestMap.get(requestIndex);
                if (netRequest != null) {
                    netRequest.notifyListener();
                    requestMap.remove(requestIndex);

                    if (netRequest.isFileMessage() && globalSendRequestListener != null) {
                        if (netRequest.getState() == NetRequest.STATE_SUCCESS) {

                            globalSendRequestListener.onSuccess(netRequest.getMessage(), netRequest.getResult());
                        } else if (netRequest.getState() == NetRequest.STATE_FAIL) {
                            globalSendRequestListener.onFail(netRequest.getMessage(), netRequest.getErrorCode(), netRequest.getErrorMessage());
                        }
                    }
                } else {
                    L.e(TAG, " handleMessage netRequest null  requestIndex :" + requestIndex);
                }

                break;
            case WHAT_REQUEST_UPDATE:
                NetRequest mRequest = requestMap.get(requestIndex);
                mRequest.notifyListener();

                if (mRequest.isFileMessage() && globalSendRequestListener != null) {
                    globalSendRequestListener.onSending(mRequest.getMessage(), mRequest.getFileLength(), mRequest.getCurrent());
                }
                break;
        }
    }

    /**
     * 下载的回调
     */
    public interface DownloadListener {
        void onSuccess(String path);

        void onFailure(Request request, Exception e);
    }

    /**
     * @param url      下载的路径
     * @param savePath 下载成功后保存的文件的路径
     * @param listener 回调
     */
    public void downloadFile(final String url, final String savePath, final DownloadListener listener) {
        NetUtil.checkNotNull(mOkHttpClient);
        //检查文件是否存在
        File appDir = new File(savePath);
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        Request request = new Request.Builder().url(url).build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.onFailure(call.request(), e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream inputStream = null;
                byte[] buff = new byte[1024 * 2];
                FileOutputStream fileOutputStream = null;
                int len = 0;
                try {
                    inputStream = response.body().byteStream();
                    String downloadname = NetUtil.getFileNameFromUrl(url);
                    File file = new File(savePath, downloadname);
                    fileOutputStream = new FileOutputStream(file);
                    while ((len = inputStream.read(buff)) != -1) {
                        fileOutputStream.write(buff, 0, len);
                    }
                    fileOutputStream.flush();
                    listener.onSuccess(savePath + "/" + downloadname);
                } catch (IOException e) {
                    listener.onFailure(response.request(), e);
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                    } catch (IOException e) {
                        listener.onFailure(response.request(), e);
                    }
                }
            }
        });
    }

    /**
     * @param url      要下载的路径
     * @param savePath 保存文件的文件夹路径
     * @param listener 下载的进度
     */
    public void downloadFileProgress(String url, final String savePath, final DownLoadProgressListener listener) {
        downloadFileProgress(url, savePath, "", listener, 0);
    }

    /**
     * @param downloadUrl 要下载的路径
     * @param savePath    保存文件的文件夹路径
     * @param filename    文件名称
     * @param listener    监听
     */
    public void downloadFileProgress(String downloadUrl, final String savePath, final String filename, final DownLoadProgressListener listener) {
        downloadFileProgress(downloadUrl, savePath, filename, listener, 0);
    }

    /**
     * @param downloadUrl 要下载的路径
     * @param savePath    保存文件的文件夹路径
     * @param filename    文件名称
     * @param listener    监听
     * @param startPoint  是否支持断点续传
     */
    public void downloadFileProgress(final String downloadUrl, final String savePath, final String filename, final DownLoadProgressListener listener, final long startPoint) {
        final Request request;
        if (!mAutoResume) {
            request = new Request.Builder()
                    .url(downloadUrl)
                    .build();
        } else {
            if (startPoint >= 0) {
                //使用断点续传
                request = new Request.Builder()
                        .url(downloadUrl)
                        .header("RANGE", "bytes=" + startPoint + "-")//断点续传要用到的，指示下载的区间
                        .build();
            } else {
                throw new IllegalStateException("起始位置不能小于0");
            }
        }
        mCall = ProgressHelper.addProgressResponseListener(mOkHttpClient, listener).newCall(request);
        //生成文件名
        String downloadFileName = NetUtil.getFileName(downloadUrl, savePath, filename);
        final String finalDownloadFileName = downloadFileName;
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.onFailure(e, request);

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    File downloadFile = new File(savePath, finalDownloadFileName);
                    if (!mAutoResume) {
                        Log.d(TAG, "不使用断点续传");
                        BufferedSink sink = Okio.buffer(Okio.sink(downloadFile));
                        sink.writeAll(response.body().source());
                        sink.close();
                    } else {
                        Log.d(TAG, "使用断点续传");
                        mDownLoadInstance.downloadPoint(downloadFile, response, startPoint);
                    }
                    listener.onSuccess(savePath + "/" + finalDownloadFileName);
                } catch (SocketTimeoutException | FileNotFoundException e) {
                    listener.onFailure(e, response.request());
                    e.printStackTrace();
                } catch (IOException e) {
                    listener.onFailure(e, response.request());
                    e.printStackTrace();
                } catch (Exception e) {
                    listener.onFailure(e, response.request());
                    e.printStackTrace();
                }

            }
        });
    }

    public CookieHandler getCookieHandler() {
        if (mCookieHandler == null) {
            throw new NullPointerException("please use setSupportCookie method to Support cookieHandler");
        } else {
            return mCookieHandler;
        }
    }

    /**
     * 是否支持断点续传
     *
     * @param autoResume
     */
    public void setAutoResume(boolean autoResume) {
        this.mAutoResume = autoResume;
    }

    public void pauseDownload() {
        if (mCall != null) {
            mCall.cancel();
        }
    }

    public void cancelDownload() {
        if (mCall != null) {
            mCall.cancel();
            mCall = null;
        }
    }

    /**
     * 直接get方式请求服务器
     *
     * @param url      :请求路径
     * @param params   : 请求参数
     * @param listener : 结果回调
     */
    public void sendGetMessage(String url, HashMap<String, String> params, final RequestListener listener) {
        String urlWithParams = UrlFomatUtils.attachHttpGetParams(url, params);
        Log.d(TAG, "get 请求的路径: " + urlWithParams);
        /*if (mSupportCookie) {
            if (mCookieHandler == null) {
                mCookieHandler = new CookieManager(new PersistentCookieStore(mContext), CookiePolicy.ACCEPT_ALL);
            }
            mOkHttpClient.setCookieHandler(mCookieHandler);
        }*/
        final Request request = new Request.Builder()
                .url(urlWithParams)
                .get()
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.onFailure(request, e);

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String result = new String(response.body().bytes(), "utf-8");
                    listener.onSuccess(result);
                } else {
                    listener.onSuccess("服务器错误:" + response.code());
                }
            }
        });
    }

    public void sendPostMessage(String url, FormBody body, final RequestListener listener) {
        /*if (mSupportCookie) {
            if (mCookieHandler == null) {
                mCookieHandler = new CookieManager(new PersistentCookieStore(mContext), CookiePolicy.ACCEPT_ALL);
            }
            mOkHttpClient.setCookieHandler(mCookieHandler);
        }*/
        final Request builder = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        mOkHttpClient.newCall(builder).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.onFailure(call.request(), e);

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String result = new String(response.body().bytes(), "utf-8");
                    listener.onSuccess(result);
                } else {
                    listener.onSuccess("服务器错误:" + response.code());
                }
            }
        });
    }

    /**
     * 获取文件流的方法。
     */
    public void getFileStream(String url, final IGetFileStreamListener listener) {
        Request request = new Request.Builder().url(url).build();
        /*if (mSupportCookie) {
            if (mCookieHandler == null) {
                mCookieHandler = new CookieManager(new PersistentCookieStore(mContext), CookiePolicy.ACCEPT_ALL);
            }
            mOkHttpClient.setCookieHandler(mCookieHandler);
        }*/
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.onFailure(call.request(), e);

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream inputStream = null;
                try {
                    inputStream = response.body().byteStream();
                    listener.onSuccess(inputStream);
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

}
