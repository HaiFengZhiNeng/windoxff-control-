package com.ocean.speech.util;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by zhangyuanyuan on 2017/7/18.
 */

public class MediaPlayerUtil implements MediaPlayer.OnBufferingUpdateListener {

    private static MediaPlayerUtil mMusicPlayerManager;
    public MediaPlayer mediaPlayer; // 媒体播放器
    boolean isOnERROE;
    private OnMusicCompletionListener onCompletionListener;
    private int percent = 0;
    private int index = 0;


    private int mCurrentPosition;


    public static synchronized MediaPlayerUtil getInstance() {
        if (mMusicPlayerManager == null) {
            synchronized (MediaPlayerUtil.class) {
                if (mMusicPlayerManager == null) {
                    mMusicPlayerManager = new MediaPlayerUtil();
                }
            }
        }
        return mMusicPlayerManager;
    }

    private HandlerThread playHandlerThread;
    private Handler playHandler;
    /**
     * 播放
     */
    private static final int PLAY = 101;
    /**
     * 停止
     */
    private static final int STOP = 102;
    private static final int STOP_SUCCESS = 103;
    private static final int PREPARED = 104;
    private static final int RELEASE = 105;

    private Handler handler;

    // 初始化播放器
    private MediaPlayerUtil() {
        handler = new Handler(Looper.getMainLooper());
        createHandlerThreadIfNeed();
        createHandlerIfNeed();
    }

    private void createHandlerThreadIfNeed() {
        if (playHandlerThread == null) {
            playHandlerThread = new HandlerThread("playHandlerThread");
            playHandlerThread.start();
        }
    }

    private void createHandlerIfNeed() {
        if (playHandler == null) {
            playHandler = new Handler(playHandlerThread.getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case PLAY:
                            String value = msg.obj.toString();
                            if (!TextUtils.isEmpty(value))
                                playMusic01(value);
                            break;
                        case STOP:
                            stopMediaPlayer02();
                            break;
                        case STOP_SUCCESS:
                            stopMediaPlayer03();
                            break;
                        case PREPARED:
                            releaseMediaPlayer();
                            break;
                        case RELEASE:
                            release();
                            break;
                    }
                }
            };
        }
    }


    private void initMediaplayer() {
        try {
            mediaPlayer = new MediaPlayer();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initMediaData() {
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);// 设置媒体流类型
        mediaPlayer.setOnBufferingUpdateListener(this);
    }

    public void playMusic01(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }

        try {
            if (mediaPlayer == null) {
                initMediaplayer();
            } else {
                mediaPlayer.reset();
            }
            initMediaData();

            mediaPlayer.setDataSource(url); // 设置数据源
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(final MediaPlayer mp) {

                    try {
                        preparedMedia();
                        mp.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                        stopMediaPlayerError();
                    }
                }
            });

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(final MediaPlayer mp) {

                    stopMediaPlayerSuccess();
                }
            });

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(final MediaPlayer mp, final int what, final int extra) {

                    stopMediaPlayerError();
                    return false;
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
            stopMediaPlayerError();

        }

    }

    private void preparedMedia() {
        playHandler.sendEmptyMessage(PREPARED);
    }

    public void stopMediaPlayerError() {
        playHandler.sendEmptyMessage(STOP);
    }

    public void stopMediaPlayerSuccess() {
        playHandler.sendEmptyMessage(STOP_SUCCESS);
    }


    // 停止
    public void stop() {

        playHandler.sendEmptyMessage(RELEASE);


    }

    private void releaseMediaPlayer() {
        if (timer != null)
            timer.cancel();

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (onCompletionListener != null) {
                    onCompletionListener.onPrepare();
                }
            }
        });

    }

    private void release() {
        try {

            if (timer != null)
                timer.cancel();
            if (mediaPlayer != null) {
                mediaPlayer.setOnPreparedListener(null);
                mediaPlayer.setOnCompletionListener(null);
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
            onCompletionListener = null;
            handler.removeCallbacksAndMessages(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopMediaPlayer02() {
        if (timer != null)
            timer.cancel();
        handler.post(new Runnable() {
            @Override
            public void run() {
                isOnERROE = true;
                if (onCompletionListener != null)
                    onCompletionListener.onCompletion(false);
                Log.e("mediaPlayer", "onCompletion    " + false);
            }
        });
    }

    private void stopMediaPlayer03() {
        if (timer != null)
            timer.cancel();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (onCompletionListener != null && !isOnERROE) {
                    isOnERROE = false;
                    onCompletionListener.onCompletion(true);
                }
            }
        });
        Log.e("mediaPlayer", "onCompletion    " + true);
    }


    private CountDownTimer timer;

    /**
     * @param url url地址
     */
    public void playUrl(String url, final OnMusicCompletionListener mCompletionListener) {

        percent = 0;
        isOnERROE = false;
        this.onCompletionListener = mCompletionListener;

        timer = new CountDownTimer(15 * 1000, 5 * 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                if (onCompletionListener != null) {
                    onCompletionListener.onCompletion(false);
                }
            }
        };
        timer.start();
        playHandler.sendMessageDelayed(playHandler.obtainMessage(PLAY, url), 0L);
    }


    /**
     * 缓冲更新
     */
    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        if (percent != 100)
            if (percent == this.percent) {
                index++;
            } else {
                index = 0;
            }
        if (index == 15) {

            isOnERROE = true;
            if (onCompletionListener != null) {
                onCompletionListener.onCompletion(false);
            }
            index = 0;
        }
        this.percent = percent;
    }


    // 暂停
    public void mediaPlayPause() throws Exception {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            mCurrentPosition = mediaPlayer.getCurrentPosition();
        }
    }

    /***
     * 继续播放
     *
     * @throws Exception
     */
    public void mediaPlayContinue() throws Exception {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(mCurrentPosition);
            mediaPlayer.start();
        }
    }

    public boolean isPlayIng() {

        if (mediaPlayer != null) {
            return mediaPlayer.isPlaying();
        }
        return false;
    }

    public interface OnMusicCompletionListener {
        void onCompletion(boolean isPlaySuccess);

        void onPrepare();
    }

}