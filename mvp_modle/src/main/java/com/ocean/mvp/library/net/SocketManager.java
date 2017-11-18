package com.ocean.mvp.library.net;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.ocean.mvp.library.utils.L;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Socket发送文件文字类
 * Created by zhangyuanyuan on 2017/7/3.
 */

public class SocketManager {
    private final static String TAG = SocketManager.class.getSimpleName();

    /**
     * 默认UDP监听的接口
     */
    public final static int DEFAULT_UDPSERVER_PORT = 8890;

    /**
     * 默认TCP监听的接口
     */
    public final static int DEFAULT_TCPSERVER_PORT = 8891;

    /**
     * UDP监听的端口
     */
    private int udpServerPort = DEFAULT_UDPSERVER_PORT;

    /**
     * TCP监听的接口
     */
    private int tcpServerPort = DEFAULT_TCPSERVER_PORT;

    /**
     * 此类的对象，用与单例模式
     */
    private static SocketManager mInstance;

    /**
     * 线程池，用于后台通过局域网发送数据
     */
    private ThreadPoolExecutor executorService;

    /**
     * UDP端口监听类
     */
    private DatagramSocket mDatagramSocket;


    private TCPServerRunnable tcpServerRunnable;

    /**
     * TCP文件发送监听
     */
    private TCPFileSendListener mFileSendListener;

    /**
     * TCP文字发送监听
     */
    private TCPTextSendListener mTextSendListener;

    /**
     * UDP端口接收监听
     */
    private UdpServerListener mUdpServerListener;

    /**
     * TCP连接监听
     */
    private TcpConnListener mTcpConnListener;

    /**
     * TCP端口接收监听
     */
    private TcpClientListener mTcpClientListener;
    /**
     * TCP 写入数据
     */
    private PrintWriter tcpPrintWriter;
    /**
     * TCP 读取数据
     */
    private BufferedReader tcpBufferReader;
    /**
     * Tcp client 当前连接
     */
    private Socket socketClient;

    /**
     * 无参数的构造函数
     */
    private SocketManager() {
    }

    /**
     * 得到 SocketManager对象的方法
     *
     * @param context 上下文
     * @return SocketManager对象
     */
    public static SocketManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (SocketManager.class) {
                if (mInstance == null)
                    mInstance = new SocketManager();
            }
        }
        return mInstance;
    }


    /**
     * 返回监听的UDP的端口
     *
     * @return 监听的UDP的端口
     */
    public int getUDPServerPort() {
        return udpServerPort;
    }

    /**
     * 返回监听的TCP的端口
     *
     * @return 监听的TCP的端口
     */
    public int getTCPServerPort() {
        return tcpServerPort;
    }


    /**
     * 注册一个UDP端口监听服务
     *
     * @param mUdpServerListener UDP端口监听回调
     */
    public void registerUdpServer(UdpServerListener mUdpServerListener) {
        registerUdpServer(mUdpServerListener, udpServerPort);
    }

    /**
     * 注册一个UDP端口监听服务
     *
     * @param mUdpServerListener UDP端口监听回调
     */
    public void registerUdpServer(UdpServerListener mUdpServerListener, int udpServerPort) {
        this.udpServerPort = udpServerPort;
        this.mUdpServerListener = mUdpServerListener;

        if (mDatagramSocket == null) {
            L.w(TAG, "mDatagramSocket is null try create DatagramSocket object");
            try {
                mDatagramSocket = new DatagramSocket(udpServerPort);
            } catch (SocketException e) {
                e.printStackTrace();
                udpServerPort++;
                try {
                    mDatagramSocket = new DatagramSocket(udpServerPort);
                } catch (SocketException e1) {
                    e1.printStackTrace();
                    udpServerPort++;
                    try {
                        mDatagramSocket = new DatagramSocket(udpServerPort);
                    } catch (SocketException e2) {
                        e2.printStackTrace();
                        udpServerPort++;
                        try {
                            mDatagramSocket = new DatagramSocket(udpServerPort);
                        } catch (SocketException e3) {
                            e3.printStackTrace();
                            try {
                                mDatagramSocket = new DatagramSocket(udpServerPort);
                            } catch (SocketException e4) {
                                e4.printStackTrace();
                                udpServerPort++;
                                try {
                                    mDatagramSocket = new DatagramSocket(udpServerPort);
                                } catch (SocketException e5) {
                                    e5.printStackTrace();
                                    try {
                                        mDatagramSocket = new DatagramSocket(udpServerPort);
                                    } catch (SocketException e6) {
                                        e6.printStackTrace();
                                        udpServerPort++;
                                        try {
                                            mDatagramSocket = new DatagramSocket(udpServerPort);
                                        } catch (SocketException e7) {
                                            e7.printStackTrace();
                                            mUdpServerListener.onFail(e7);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (mDatagramSocket == null) throw new RuntimeException("DatagramSocket is null");
        getExecutorService().execute(new UDPReceiveRunnable(mDatagramSocket));
    }

    public void unRegisterUdpServer() {
        if (mDatagramSocket != null) {
            mDatagramSocket.close();
        }

        if (mUdpServerListener != null) mUdpServerListener = null;
    }

    /**
     * 注册一个TCP端口监听的服务
     *
     * @param mTcpServerListener TCP端口监听回调
     */
    public void registerTcpServer(TcpClientListener mTcpServerListener) {
        registerTcpServer(mTcpServerListener, tcpServerPort);
    }


    /**
     * 注册一个TCP端口监听的服务
     *
     * @param mTcpCLientListener TCP端口监听回调
     */
    public void registerTcpServer(TcpClientListener mTcpCLientListener, int tcpServerPort) {
        this.tcpServerPort = tcpServerPort;
        this.mTcpClientListener = mTcpCLientListener;
        tcpServerRunnable = new TCPServerRunnable();
        getExecutorService().execute(tcpServerRunnable);
    }

    /**
     * 注册一个TCP端口监听的服务
     */
    public void unRegisterTcpServer() {
        if (tcpServerRunnable != null) tcpServerRunnable.release();
    }

    /**
     * 通过UDP发送文本
     *
     * @param mAddress 接收端Ip地址
     * @param mPort    接收端的端口
     * @param msg      发送的字符串
     */
    public void sendTextByUDP2(String mAddress, int mPort, String msg) {
        try {
            InetAddress host = InetAddress.getByName(mAddress);
            sendMessageByUdp(mDatagramSocket, host, mPort, msg.getBytes());
//            getExecutorService().execute(new UDPSendRunnable(mDatagramSocket, host, mPort, msg));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过UDP发送文本
     *
     * @param mAddress 接收端Ip地址
     * @param mPort    接收端的端口
     * @param msg      发送的字符串
     */
    public void sendTextByUDP(String mAddress, int mPort, String msg) {
        try {
            InetAddress host = InetAddress.getByName(mAddress);
            getExecutorService().execute(new UDPSendRunnable(mDatagramSocket, host, mPort, msg));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }


    /**
     * 通过UDP发送byte[]
     *
     * @param mAddress 接收端Ip地址
     * @param mPort    接收端的端口
     * @param bytes      发送的字符串
     */
    public void sendByteByUdp(String mAddress,int mPort,byte[] bytes){
        try {
            InetAddress host = InetAddress.getByName(mAddress);
            sendMessageByUdp(mDatagramSocket, host, mPort, bytes);
//            getExecutorService().execute(new UDPSendRunnable(mDatagramSocket, host, port, bytes));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageByUdp(DatagramSocket client,InetAddress mAddress, int mPort, byte[] bytes){
        try {
            if (client == null)
                client = new DatagramSocket();
//            byte[] sendBuf = bytes;
            DatagramPacket sendPacket = new DatagramPacket(bytes, bytes.length, mAddress, mPort);
            client.send(sendPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 通过TCP发送文本
     *
     * @param host              接收端Ip地址
     * @param port              接收端的端口
     * @param msg               发送的字符串
     * @param mTextSendListener 发送的监听回调
     */
    public void sendTextByTCP(String host, int port, String msg, TCPTextSendListener mTextSendListener) {
        this.mTextSendListener = mTextSendListener;
        getExecutorService().execute(new TextSendRunnable(host, port, msg));
    }

    /**
     * 通过TCP发送文本(已连接的TCP)
     *
     * @param host              接收端Ip地址
     * @param port              接收端的端口
     * @param text              发送的字符串
     * @param mTextSendListener 发送的监听回调
     */
    public void sendTextByTCPConnected(String host, int port, final String text, final TCPTextSendListener mTextSendListener) {
        if (tcpPrintWriter != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    tcpPrintWriter.println(text);
                    tcpPrintWriter.flush();
                    if (mTextSendListener != null)
                        mTextSendListener.onSuccess("");
                }
            }).start();

        } else sendTextByTCP(host, port, text, mTextSendListener);
    }

    /**
     * 通过TCP发送文件
     *
     * @param host              接收端Ip地址
     * @param port              接收端的端口
     * @param msg               发送的字符串
     * @param filePaths         发送的文件列表
     * @param mFileSendListener 发送的监听回调
     */
    public void sendFileByTCP(String host, int port, String msg, List<File> filePaths, TCPFileSendListener mFileSendListener) {
        this.mFileSendListener = mFileSendListener;
        getExecutorService().execute(new FileSendRunnable(host, port, msg, filePaths));
    }

    /**
     * 建立TCP连接
     *
     * @param mAddress  接收端Ip地址
     * @param mPort     接收端的端口
     * @param mListener 连接的监听回调
     */
    public void connectByTcp(String mAddress, int mPort, TcpConnListener mListener) {
        mTcpConnListener = mListener;
        getExecutorService().execute(new TCPConnectRunnable(mAddress, mPort));
    }

    /**
     * 建立TCP连接
     *
     * @param mAddress  接收端Ip地址
     * @param mPort     接收端的端口
     * @param mListener 连接的监听回调
     * @param time      超时时间，毫秒
     */
    public void connectByTcp(String mAddress, int mPort, TcpConnListener mListener, int time) {
        mTcpConnListener = mListener;
        getExecutorService().execute(new TCPConnectRunnable(mAddress, mPort, time));
    }

    /**
     * 生成线程池
     *
     * @return 线程池
     */
    public synchronized ExecutorService getExecutorService() {
        if (executorService == null) {
            executorService = new ThreadPoolExecutor(3, Integer.MAX_VALUE, 10, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>());
        }
        return executorService;
    }

    /**
     * TCP的监听类
     */
    private class TCPServerRunnable implements Runnable {
        private boolean isListener;

        public TCPServerRunnable() {
            isListener = true;
            Log.d(TAG, "tcpServerPort: " + tcpServerPort);
        }

        @Override
        public void run() {
            try {
                while (isListener) {
                    Log.d(TAG, "read……");
                    if (socketClient != null && !socketClient.isInputShutdown()) {
                        try {
                            String formServerStr;
                            char[] buffer = new char[10];
                            if (tcpBufferReader != null) {
                                int count = tcpBufferReader.read(buffer);
                                if (count > 0) {
                                    Log.i(TAG, count + "");
                                    Log.i(TAG, buffer[0] + "");
//                                    formServerStr = getInfoBuff(buffer, count);//消息换行
                                    formServerStr = new String(buffer);
                                    if (mTcpClientListener != null)
                                        mTcpClientListener.onSuccess(formServerStr);
                                }
                            }
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            if (mTcpClientListener != null)
                                mTcpClientListener.onFail(e);
                        }
                    }


                }
            } catch (Exception e) {
                e.printStackTrace();
                if (mTcpClientListener != null) {
                    mTcpClientListener.onFail(e);
                }
            }
        }

        public void release() {
            try {
                isListener = false;

                socketClient.shutdownInput();
                socketClient.shutdownOutput();

                tcpBufferReader.close();
                tcpPrintWriter.close();
                socketClient.close();

                tcpBufferReader = null;
                tcpPrintWriter = null;
                socketClient = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 字符串发送的监听类
     */
    private class TextSendRunnable implements Runnable {
        private String host;
        private int port;
        private String msg;

        public TextSendRunnable(String host, int port, String msg) {
            this.host = host;
            this.port = port;
            this.msg = msg;
        }

        @Override
        public void run() {
            try {

                Socket mClient = new Socket(host, port);
                OutputStream outputStream = mClient.getOutputStream();
                InputStream inputStream = mClient.getInputStream();
                BufferedReader mInput = new BufferedReader(new InputStreamReader(inputStream));

                outputStream.write(msg.getBytes());
                outputStream.write("\r\n".getBytes());

                outputStream.flush();

                String line = mInput.readLine();
                if (mTextSendListener != null)
                    mTextSendListener.onSuccess(line);
                inputStream.close();
                outputStream.close();
                mClient.close();

            } catch (IOException e) {
                e.printStackTrace();
                if (mTextSendListener != null)
                    mTextSendListener.onFail(e);
            }
        }
    }

    /**
     * 文件发送的监听类
     */
    private class FileSendRunnable implements Runnable {
        private String host;
        private int port;
        private String msg;
        private List<File> filePaths;

        public FileSendRunnable(String host, int port, String msg, List<File> filePaths) {
            this.host = host;
            this.port = port;
            this.msg = msg;
            this.filePaths = filePaths;
        }

        @Override
        public void run() {
            try {
                Socket mClient = new Socket(host, port);
                mClient.setSoTimeout(10000);
                OutputStream outputStream = mClient.getOutputStream();
                InputStream inputStream = mClient.getInputStream();
                BufferedReader mInput = new BufferedReader(new InputStreamReader(inputStream));
                //发送文件传输请求
                outputStream.write(msg.getBytes());
                outputStream.write("\r\n".getBytes());
                outputStream.flush();

                //等到接收端返回是否接收（暂时不做解析处理。默认接受）
                String readLine = mInput.readLine();
                L.d(TAG, "readLine :" + readLine);


                //开始传输文件

                long filesTotal = new JSONObject(msg).getLong("total_length");
                long fileListSendCount = 0;


                FileInputStream fileInputStream;
                byte[] buffer = new byte[1024];
                for (int i = 0; i < filePaths.size(); i++) {

                    File file = filePaths.get(i);

                    long length = file.length();

                    if (mFileSendListener != null) {
                        mFileSendListener.onSendingFile(file, filesTotal, fileListSendCount, length, 0);
                    }

                    fileInputStream = new FileInputStream(file);
                    int readCount = 0;
                    int readLength;
                    while ((readLength = fileInputStream.read(buffer)) != -1) {
                        readCount += readLength;
                        fileListSendCount += readLength;
                        outputStream.write(buffer, 0, readLength);
                        if (mFileSendListener != null) {
                            mFileSendListener.onSendingFile(file, filesTotal, fileListSendCount, length, readCount);
                        }
                    }
                    fileInputStream.close();
                    readLine = mInput.readLine();
                    L.d(TAG, "for readLine :" + readLine);

                    JSONObject jsonObject = new JSONObject(readLine);
                    if (jsonObject.getString("code").equals("200")) {
                        if (mFileSendListener != null) {
                            mFileSendListener.onSendSuccessFile(file);
                        }
                    }
                }
                if (mFileSendListener != null) {
                    mFileSendListener.onSuccess("文件发送成功");
                }
                outputStream.close();
                inputStream.close();
                mClient.close();
            } catch (Exception e) {
                if (mFileSendListener != null) {
                    mFileSendListener.onFail(e);
                }
                e.printStackTrace();
            }
        }
    }

    /**
     * TCP连接的相关处理类
     */
    private class TCPConnectRunnable implements Runnable {
        private String dstName;
        private int dstPort;
        private int time;

        public TCPConnectRunnable(String dstName, int dstPort, int time) {
            this.dstName = dstName;
            this.dstPort = dstPort;
            this.time = time;
        }

        public TCPConnectRunnable(String dstName, int dstPort) {
            this.dstName = dstName;
            this.dstPort = dstPort;
        }

        @Override
        public void run() {
            try {
//                Socket mClient = new Socket(dstName, dstPort);
                socketClient = new Socket();
                SocketAddress socketAddress = new InetSocketAddress(dstName, dstPort);
                socketClient.connect(socketAddress, time);
                tcpPrintWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                        socketClient.getOutputStream(), "utf-8")), true);
                tcpBufferReader = new BufferedReader(new InputStreamReader(socketClient.getInputStream(), "utf-8"));

                socketClient.setTcpNoDelay(true);
                if (mTcpConnListener != null)
                    mTcpConnListener.onSuccess(dstName, dstPort, socketClient);
            } catch (IOException e) {
                if (mTcpConnListener != null) mTcpConnListener.onFail(e);
            }
        }
    }

    /**
     * UDP 监听的相关的处理类
     */
    private class UDPReceiveRunnable implements Runnable {
        DatagramSocket mServer;

        public UDPReceiveRunnable(DatagramSocket mDatagramSocket) {
            this.mServer = mDatagramSocket;
        }

        @Override
        public void run() {
            try {
                byte[] recvBuf = new byte[512];
                DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);

                while (!Thread.interrupted()) {
                    mServer.receive(recvPacket);
                    if (mUdpServerListener != null) {
                        mUdpServerListener.onReceive(recvPacket);
                    }
                }

            } catch (Exception e) {
                L.d(TAG, "RecveviceThread start fail");
                e.printStackTrace();
                if (mUdpServerListener != null) {
                    mUdpServerListener.onFail(e);
                }
            }
            L.d(TAG, "Thread.interrupted");
        }
    }

    /**
     * UDP 发送字符串的相关类
     */
    private class UDPSendRunnable implements Runnable {
        DatagramSocket client;
        private InetAddress mAddress;
        private int mPort;
        private String msg = "";
        private byte[] bytes;

        public UDPSendRunnable(DatagramSocket client, InetAddress mAddress, int mPort, String msg) {
            this.client = client;
            this.mAddress = mAddress;
            this.mPort = mPort;
            this.msg = msg;
        }

        public UDPSendRunnable(DatagramSocket client, InetAddress mAddress, int mPort, byte[] bytes) {
            this.client = client;
            this.mAddress = mAddress;
            this.mPort = mPort;
            this.bytes = bytes;
        }

        @Override
        public void run() {
            try {
                if (client == null)
                    client = new DatagramSocket();
                byte[] sendBuf;
                if (!TextUtils.isEmpty(msg)){
                    sendBuf = msg.getBytes();
                }else {
                    sendBuf = bytes;
                }
                DatagramPacket sendPacket = new DatagramPacket(sendBuf, sendBuf.length, mAddress, mPort);
                client.send(sendPacket);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * TCP发送字符串的监听回调接口
     */
    public interface TCPTextSendListener {
        void onFail(Exception e);

        void onSuccess(String result);
    }

    /**
     * TCP发送文件的监听回调接口
     */
    public interface TCPFileSendListener {

        /**
         * 发送文件监听
         *
         * @param file              发送的文件
         * @param filesTotal        需发送文件的总大小发送
         * @param fileListSendCount 已发送的总文件大小
         * @param fileLength        此文件的大小
         * @param current           此文件发送的大小
         */
        void onSendingFile(File file, long filesTotal, long fileListSendCount, long fileLength, long current);

        void onSendSuccessFile(File file);

        void onFail(Exception e);

        void onSuccess(String result);
    }

    /**
     * UDP端口监听的回调
     */
    public interface UdpServerListener {
        void onReceive(DatagramPacket receivePacket);

        void onFail(Exception e);
    }

    /**
     * TCP连接的回调
     */
    public interface TcpConnListener {
        void onSuccess(String dstName, int dstPort, Socket mClient);

        void onFail(Exception e);
    }

    /**
     * TCP端口监听的回调
     */
    public interface TcpClientListener {
        void onSuccess(String accept);

        void onFail(Exception e);
    }
}
