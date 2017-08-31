package com.ocean.speech.custom;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;


import com.ocean.speech.R;

import java.lang.ref.WeakReference;
import java.util.Random;

/**
 * 音量动画view
 * Created by Administrator on 2015/7/29.
 */
public class AnimationView extends View {
    private static float RATIO;
    private static int OFFSET_LEFT = 0;
    private static int OFFSET_TOP = 0;
    /**
     * 当前音量
     */
    private int signalEMA;
    /**
     * 画笔
     */
    private Paint paint = new Paint();
    /**
     * 画长线的位置
     */
    int startCount = 0;
    /**
     * 开始录音的时间
     */
    private long startTime;
    /**
     * handler 标志 音量
     */
    private static final int SEND_MESSAGE = 0;
    private Random random;
    private OnTimeChangeListen mOnTimeChangeLister;

    /**
     * 设置显示时间 语音时长
     */
    public void setTime(long time) {
        startTime = time;
    }

    public void stop() {
        mHandler.removeMessages(SEND_MESSAGE);
    }

    public void setonTimeChangeLister(final OnTimeChangeListen onTimeChangeLister) {
        mOnTimeChangeLister = onTimeChangeLister;
    }

    static class WeakHandler extends Handler {
        // 弱引用 可以调用此view里的所有变量，系统内存不足时，会先销毁弱引用里的数据
        private WeakReference<AnimationView> animHandler;

        public WeakHandler(AnimationView mAnimationView) {
            animHandler = new WeakReference<AnimationView>(mAnimationView);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SEND_MESSAGE:
                    animHandler.get().invalidate();
                    animHandler.get().startCount++;
                    if (animHandler.get().startCount > 14) {
                        animHandler.get().startCount = 0;
                    }
                    sendEmptyMessageDelayed(SEND_MESSAGE, 100);

                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 标记一次循环
     */
    boolean isEnd = true;

    public void setSignalEMA(int signalEMA) {
        this.signalEMA = signalEMA;
        if (signalEMA == 0) {
            mHandler.removeMessages(SEND_MESSAGE);
            invalidate();
            isEnd = true;
            startCount = 0;
        } else {
            if (isEnd) {
                isEnd = false;
                mHandler.sendEmptyMessage(SEND_MESSAGE);
            }
        }
    }

    public AnimationView(Context context) {
        super(context);
        initHandler(context);
    }

    public AnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initHandler(context);
    }

    public AnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initHandler(context);
    }

    private WeakHandler mHandler;
    float padding = 10;
    private float width;

    /**
     * 初始化
     */
    private void initHandler(Context context) {
        mHandler = new WeakHandler(this);
        // size =
        // DisplayParams.spToPixel(context,context.getResources().getDimension(R.dimen.textsize_10));
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay()
                .getMetrics(displayMetrics);

        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        float ratioWidth = (float) screenWidth / 480;
        float ratioHeight = (float) screenHeight / 800;
        RATIO = Math.min(ratioWidth, ratioHeight);
        if (ratioWidth != ratioHeight) {
            if (RATIO == ratioWidth) {
                OFFSET_LEFT = 0;
                OFFSET_TOP = Math.round((screenHeight - 800 * RATIO) / 2);
            } else {
                OFFSET_LEFT = Math.round((screenWidth - 480 * RATIO) / 2);
                OFFSET_TOP = 0;
            }
        }
        size = Math.round(25 * RATIO);
        startTime = System.currentTimeMillis();
        padding = Math.round(4 * RATIO) + 50;
        width = Math.round(10 * RATIO);

        random = new Random();

    }

    int size;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    int bottom;
    int bootGap;
    int totalCount = 20;

    int widthgap;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int availableWidth = getWidth();
        int availableHeight = getHeight();

        if (mOnTimeChangeLister != null) {
            mOnTimeChangeLister.onTimeChang(changeTime());
        }

        bottom = (int) (availableHeight * 0.05);
        bootGap = bottom * 2;
        widthgap = (int) (availableWidth * 0.05);
        padding = (float) ((availableWidth - widthgap * 15) * 0.75);
        paint.reset();
        paint.setColor(getResources().getColor(R.color.color_0Ec7dd));
        paint.setStyle(Paint.Style.FILL);
//        paint.setStrokeWidth(1);
        paint.setStrokeWidth(widthgap);

        int count;
        for (int i = 0; i < 15; i++) {
            count = 1 + (signalEMA > 0 ? random.nextInt(15) : 0);
            for (int j = 0; j < count; j++) {
                canvas.drawLine(
                        (float) (widthgap * 0.5 + i * (widthgap + 10)),
                        availableHeight - (bottom * j + bootGap),
                        (float) (widthgap * 0.5 + i * (widthgap + 10)),
                        availableHeight - (bottom * (j + 1) - 5 + bootGap),
                        paint);
            }

        }


    }


    /**
     * 转换时间格式
     *
     * @return 时间
     */
    public String changeTime() {
        int finalTime = (int) ((System.currentTimeMillis() - startTime) / 1000);
        if (finalTime < 10) {
            return "00:0" + finalTime;
        } else {
            return "00:" + finalTime;
        }
    }


    public interface OnTimeChangeListen {
        void onTimeChang(String time);
    }

}




/*
package com.ren001.control.alarm;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import R;
import com.ren001.control.utils.DisplayParams;

import java.lang.ref.WeakReference;

*/
/**
 * Created by Administrator on 2015/7/29.
 * <p/>
 * 当前音量
 * <p/>
 * 画笔
 * <p/>
 * 画长线的位置
 * <p/>
 * 开始录音的时间
 * <p/>
 * handler 标志 音量
 * <p/>
 * 设置显示时间  语音时长
 * <p/>
 * 标记一次循环
 * <p/>
 * 初始化
 * <p/>
 * 转换时间格式
 *
 * @return 时间
 * <p/>
 * 当前音量
 * <p/>
 * 画笔
 * <p/>
 * 画长线的位置
 * <p/>
 * 开始录音的时间
 * <p/>
 * handler 标志 音量
 * <p/>
 * 设置显示时间  语音时长
 * <p/>
 * 标记一次循环
 * <p/>
 * 初始化
 * <p/>
 * 转换时间格式
 * @return 时间
 * <p/>
 * 当前音量
 * <p/>
 * 画笔
 * <p/>
 * 画长线的位置
 * <p/>
 * 开始录音的时间
 * <p/>
 * handler 标志 音量
 * <p/>
 * 设置显示时间  语音时长
 * <p/>
 * 标记一次循环
 * <p/>
 * 初始化
 * <p/>
 * 转换时间格式
 * @return 时间
 * <p/>
 * 当前音量
 * <p/>
 * 画笔
 * <p/>
 * 画长线的位置
 * <p/>
 * 开始录音的时间
 * <p/>
 * handler 标志 音量
 * <p/>
 * 设置显示时间  语音时长
 * <p/>
 * 标记一次循环
 * <p/>
 * 初始化
 * <p/>
 * 转换时间格式
 * @return 时间
 * <p/>
 * 当前音量
 * <p/>
 * 画笔
 * <p/>
 * 画长线的位置
 * <p/>
 * 开始录音的时间
 * <p/>
 * handler 标志 音量
 * <p/>
 * 设置显示时间  语音时长
 * <p/>
 * 标记一次循环
 * <p/>
 * 初始化
 * <p/>
 * 转换时间格式
 * @return 时间
 * <p/>
 * 当前音量
 * <p/>
 * 画笔
 * <p/>
 * 画长线的位置
 * <p/>
 * 开始录音的时间
 * <p/>
 * handler 标志 音量
 * <p/>
 * 设置显示时间  语音时长
 * <p/>
 * 标记一次循环
 * <p/>
 * 初始化
 * <p/>
 * 转换时间格式
 * @return 时间
 * <p/>
 * 当前音量
 * <p/>
 * 画笔
 * <p/>
 * 画长线的位置
 * <p/>
 * 开始录音的时间
 * <p/>
 * handler 标志 音量
 * <p/>
 * 设置显示时间  语音时长
 * <p/>
 * 标记一次循环
 * <p/>
 * 初始化
 * <p/>
 * 转换时间格式
 * @return 时间
 * <p/>
 * 当前音量
 * <p/>
 * 画笔
 * <p/>
 * 画长线的位置
 * <p/>
 * 开始录音的时间
 * <p/>
 * handler 标志 音量
 * <p/>
 * 设置显示时间  语音时长
 * <p/>
 * 标记一次循环
 * <p/>
 * 初始化
 * <p/>
 * 转换时间格式
 * @return 时间
 * <p/>
 * 当前音量
 * <p/>
 * 画笔
 * <p/>
 * 画长线的位置
 * <p/>
 * 开始录音的时间
 * <p/>
 * handler 标志 音量
 * <p/>
 * 设置显示时间  语音时长
 * <p/>
 * 标记一次循环
 * <p/>
 * 初始化
 * <p/>
 * 转换时间格式
 * @return 时间
 * <p/>
 * 当前音量
 * <p/>
 * 画笔
 * <p/>
 * 画长线的位置
 * <p/>
 * 开始录音的时间
 * <p/>
 * handler 标志 音量
 * <p/>
 * 设置显示时间  语音时长
 * <p/>
 * 标记一次循环
 * <p/>
 * 初始化
 * <p/>
 * 转换时间格式
 * @return 时间
 * <p/>
 * 当前音量
 * <p/>
 * 画笔
 * <p/>
 * 画长线的位置
 * <p/>
 * 开始录音的时间
 * <p/>
 * handler 标志 音量
 * <p/>
 * 设置显示时间  语音时长
 * <p/>
 * 标记一次循环
 * <p/>
 * 初始化
 * <p/>
 * 转换时间格式
 * @return 时间
 * <p/>
 * 当前音量
 * <p/>
 * 画笔
 * <p/>
 * 画长线的位置
 * <p/>
 * 开始录音的时间
 * <p/>
 * handler 标志 音量
 * <p/>
 * 设置显示时间  语音时长
 * <p/>
 * 标记一次循环
 * <p/>
 * 初始化
 * <p/>
 * 转换时间格式
 * @return 时间
 * <p/>
 * 当前音量
 * <p/>
 * 画笔
 * <p/>
 * 画长线的位置
 * <p/>
 * 开始录音的时间
 * <p/>
 * handler 标志 音量
 * <p/>
 * 设置显示时间  语音时长
 * <p/>
 * 标记一次循环
 * <p/>
 * 初始化
 * <p/>
 * 转换时间格式
 * @return 时间
 * <p/>
 * 当前音量
 * <p/>
 * 画笔
 * <p/>
 * 画长线的位置
 * <p/>
 * 开始录音的时间
 * <p/>
 * handler 标志 音量
 * <p/>
 * 设置显示时间  语音时长
 * <p/>
 * 标记一次循环
 * <p/>
 * 初始化
 * <p/>
 * 转换时间格式
 * @return 时间
 * <p/>
 * 当前音量
 * <p/>
 * 画笔
 * <p/>
 * 画长线的位置
 * <p/>
 * 开始录音的时间
 * <p/>
 * handler 标志 音量
 * <p/>
 * 设置显示时间  语音时长
 * <p/>
 * 标记一次循环
 * <p/>
 * 初始化
 * <p/>
 * 转换时间格式
 * @return 时间
 * <p/>
 * 当前音量
 * <p/>
 * 画笔
 * <p/>
 * 画长线的位置
 * <p/>
 * 开始录音的时间
 * <p/>
 * handler 标志 音量
 * <p/>
 * 设置显示时间  语音时长
 * <p/>
 * 标记一次循环
 * <p/>
 * 初始化
 * <p/>
 * 转换时间格式
 * @return 时间
 * <p/>
 * 当前音量
 * <p/>
 * 画笔
 * <p/>
 * 画长线的位置
 * <p/>
 * 开始录音的时间
 * <p/>
 * handler 标志 音量
 * <p/>
 * 设置显示时间  语音时长
 * <p/>
 * 标记一次循环
 * <p/>
 * 初始化
 * <p/>
 * 转换时间格式
 * @return 时间
 * <p/>
 * 当前音量
 * <p/>
 * 画笔
 * <p/>
 * 画长线的位置
 * <p/>
 * 开始录音的时间
 * <p/>
 * handler 标志 音量
 * <p/>
 * 设置显示时间  语音时长
 * <p/>
 * 标记一次循环
 * <p/>
 * 初始化
 * <p/>
 * 转换时间格式
 * @return 时间
 * <p/>
 * 当前音量
 * <p/>
 * 画笔
 * <p/>
 * 画长线的位置
 * <p/>
 * 开始录音的时间
 * <p/>
 * handler 标志 音量
 * <p/>
 * 设置显示时间  语音时长
 * <p/>
 * 标记一次循环
 * <p/>
 * 初始化
 * <p/>
 * 转换时间格式
 * @return 时间
 * <p/>
 * 当前音量
 * <p/>
 * 画笔
 * <p/>
 * 画长线的位置
 * <p/>
 * 开始录音的时间
 * <p/>
 * handler 标志 音量
 * <p/>
 * 设置显示时间  语音时长
 * <p/>
 * 标记一次循环
 * <p/>
 * 初始化
 * <p/>
 * 转换时间格式
 * @return 时间
 * <p/>
 * 当前音量
 * <p/>
 * 画笔
 * <p/>
 * 画长线的位置
 * <p/>
 * 开始录音的时间
 * <p/>
 * handler 标志 音量
 * <p/>
 * 设置显示时间  语音时长
 * <p/>
 * 标记一次循环
 * <p/>
 * 初始化
 * <p/>
 * 转换时间格式
 * @return 时间
 * <p/>
 * 当前音量
 * <p/>
 * 画笔
 * <p/>
 * 画长线的位置
 * <p/>
 * 开始录音的时间
 * <p/>
 * handler 标志 音量
 * <p/>
 * 设置显示时间  语音时长
 * <p/>
 * 标记一次循环
 * <p/>
 * 初始化
 * <p/>
 * 转换时间格式
 * @return 时间
 * <p/>
 * 当前音量
 * <p/>
 * 画笔
 * <p/>
 * 画长线的位置
 * <p/>
 * 开始录音的时间
 * <p/>
 * handler 标志 音量
 * <p/>
 * 设置显示时间  语音时长
 * <p/>
 * 标记一次循环
 * <p/>
 * 初始化
 * <p/>
 * 转换时间格式
 * @return 时间
 * <p/>
 * 当前音量
 * <p/>
 * 画笔
 * <p/>
 * 画长线的位置
 * <p/>
 * 开始录音的时间
 * <p/>
 * handler 标志 音量
 * <p/>
 * 设置显示时间  语音时长
 * <p/>
 * 标记一次循环
 * <p/>
 * 初始化
 * <p/>
 * 转换时间格式
 * @return 时间
 *//*

public class AnimationView extends View {
    private static float RATIO;
    private static int OFFSET_LEFT = 0;
    private static int OFFSET_TOP = 0;
    */
/**
 * 当前音量
 *//*

    private int signalEMA;
    */
/**
 * 画笔
 *//*

    private Paint paint = new Paint();
    */
/**
 * 画长线的位置
 *//*

    int startCount = 0;
    */
/**
 * 开始录音的时间
 *//*

    private long startTime;
    */
/**
 * handler 标志 音量
 *//*

    private static final  int SEND_MESSAGE=0;

    */
/**
 * 设置显示时间  语音时长
 *//*

    public void setTime(long time) {
        startTime=time;
    }

    public void stop() {
        mHandler.removeMessages(SEND_MESSAGE);
    }


    static class  WeakHandler extends Handler{
        //弱引用 可以调用此view里的所有变量，系统内存不足时，会先销毁弱引用里的数据
        private WeakReference<AnimationView> animHandler;

        public  WeakHandler(AnimationView mAnimationView){
            animHandler=new WeakReference<>(mAnimationView);
        }


        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SEND_MESSAGE:
                    animHandler.get().invalidate();
                    animHandler.get().startCount++;
                    if (animHandler.get().startCount >14){
                        animHandler.get().startCount=0;
                    }
                    sendEmptyMessageDelayed(SEND_MESSAGE,100);

                    break;
                default:
                    break;
            }
        }
    }

    */
/**
 * 标记一次循环
 *//*

    boolean isEnd=true;
    public void setSignalEMA(int signalEMA) {
        this.signalEMA = signalEMA;
        if(signalEMA==0){
            mHandler.removeMessages(SEND_MESSAGE);
            invalidate();
            isEnd=true;
            startCount=0;
        }else{
            if(isEnd){
                isEnd=false;
                mHandler.sendEmptyMessage(SEND_MESSAGE);
            }
        }
    }

    public AnimationView(Context context) {
        super(context);
        initHandler(context);
    }



    public AnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initHandler(context);
    }

    public AnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initHandler(context);
    }

    private WeakHandler mHandler;

    */
/**
 * 初始化
 *//*

    private void initHandler(Context context) {
        mHandler=new WeakHandler(this);
        //计算字体
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        float ratioWidth = (float)screenWidth / 480;
        float ratioHeight = (float)screenHeight / 800;
        RATIO = Math.min(ratioWidth, ratioHeight);
        if (ratioWidth != ratioHeight) {
            if (RATIO == ratioWidth) {
                OFFSET_LEFT = 0;
                OFFSET_TOP = Math.round((screenHeight - 800 * RATIO) / 2);
            }else {
                OFFSET_LEFT = Math.round((screenWidth - 480 * RATIO) / 2);
                OFFSET_TOP = 0;
            }
        }
        size = Math.round(25 * RATIO);
    }
    int size;
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int availableWidth = getWidth();
        int availableHeight = getHeight();
        int x = availableWidth / 2;
        int y = availableHeight / 2;
        paint.reset();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(10);



        if (signalEMA > 0) {

             for (int i = 0; i < 15; i++) {
                 if (startCount == i) {
                     //左侧
                     canvas.drawLine(i * 20.0f +  150.0f, y - 15.0f, i * 20.0f + 150.0f,
                     y + 25.0f, paint);
                     //右侧
                     canvas.drawLine(i * 20.0f + (x + 100.0f), y - 15.0f, i * 20.0f + (x + 100.0f),
                     y + 25.0f, paint);
                 }else {
                     //左侧
                     canvas.drawLine(i * 20.0f + 150.0f, y-5.0f, i * 20.0f + 150.0f,
                     y+15.0f, paint);
                     //右侧
                     canvas.drawLine(i * 20.0f + (x + 100.0f), y - 5.0f, i * 20.0f + (x + 100.0f),
                     y + 15.0f, paint);
                 }
             }

         } else {

             for (int i = 0; i < 15; i++) {
                 //左侧
                 canvas.drawLine(i * 20.0f + 150.0f, y-5.0f, i * 20.0f + 150.0f,
                 y+15.0f, paint);
                 //右侧
                 canvas.drawLine(i * 20.0f + (x+100.0f), y-5.0f, i * 20.0f + (x+100.0f),
                 y+15.0f, paint);

             }
         }
        //显示录音的时间
        paint.reset();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(2);
        paint.setTextSize(size);
        canvas.drawText(changeTime(), x - 50, y + 20, paint);

    }

    */
/**
 * 转换时间格式
 * @return 时间
 *//*

    public String changeTime(){
        int finalTime = (int)((System.currentTimeMillis()-startTime)/1000);
        if (finalTime<10){
            return "00:0"+finalTime;
        }else {
            return "00:"+finalTime;
        }
    }


}
*/
