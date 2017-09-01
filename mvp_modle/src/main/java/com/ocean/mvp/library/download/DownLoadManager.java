package com.ocean.mvp.library.download;

import com.ocean.mvp.library.progress.ProgressResponseBody;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import okhttp3.Response;
import okio.BufferedSource;

/**
 * 负责下载业务的管理
 * http://blog.csdn.net/KevinsCSDN/article/details/51934274
 *
 * Created by zhangyuanyuan on 2017/7/3.
 */

public class DownLoadManager implements DownLoaderInterface {
    private static volatile DownLoadManager mDownManager;
    private DownLoadManager() {

    }

    public static synchronized DownLoadManager getInstance() {
        if (mDownManager == null) {
            synchronized (DownLoadManager.class) {
                if (mDownManager == null) {
                    mDownManager = new DownLoadManager();
                }
            }
        }
        return mDownManager;
    }



    /**
     * 断点下载
     *
     * @param response   响应的内容
     * @param startPoint 起始的位置
     * @param file       下载的文件
     */
    @Override
    public void downloadPoint(File file, Response response, long startPoint) {
        ProgressResponseBody responseBody = (ProgressResponseBody) response.body();//响应体
        BufferedSource in = null;
        try {
            in = responseBody.source();
        } catch (Exception e) {
            e.printStackTrace();
        }
        FileChannel channelOut = null;
        // 随机访问文件，可以指定断点续传的起始位置
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file, "rwd");
            //Chanel NIO中的用法，由于RandomAccessFile没有使用缓存策略，直接使用会使得下载速度变慢，亲测缓存下载3.3秒的文件，用普通的RandomAccessFile需要20多秒。
            channelOut = randomAccessFile.getChannel();
            // 内存映射，直接使用RandomAccessFile，是用其seek方法指定下载的起始位置，使用缓存下载，在这里指定下载位置。
            MappedByteBuffer mappedBuffer = channelOut.map(FileChannel.MapMode.READ_WRITE, startPoint, responseBody.contentLength());
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                mappedBuffer.put(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                if (channelOut != null) {
                    channelOut.close();
                }
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

