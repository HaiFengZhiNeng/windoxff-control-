package com.ocean.speech.control;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.ocean.mvp.library.utils.L;
import com.ocean.speech.R;

/**
 * Created by zhangyuanyuan on 2017/7/10.
 */

public class NavController extends View {
    private int innerColor;
    private int outerColor;
    private final static int INNER_COLOR_DEFAULT = Color.parseColor("#66838383");//d32f2f
    private final static int OUTER_COLOR_DEFAULT = Color.parseColor("#55cccccc");//f44336
    private int realWidth;//绘图使用的宽
    private int realHeight;//绘图使用的高
    private float innerCenterX;
    private float innerCenterY;
    private float outRadius;
    private float innerRedius;
    private Paint outerPaint;
    private Paint innerPaint;
    private OnNavAndSpeedListener mCallBack = null;

    private float centerX;//大圆圆心x
    private float centerY;//大圆圆心y
    private float originalX;//原始大圆圆心y
    private float originalY;//原始大圆圆心y

    private int screenWidth;//画布宽
    private int screenHeight;//画布高


    public interface OnNavAndSpeedListener {
        public void onNavAndSpeed(int nav);
    }

    public NavController(Context context) {
        this(context, null);

    }

    public NavController(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = getResources().obtainAttributes(attrs, R.styleable.NavController);
        innerColor = ta.getColor(R.styleable.NavController_InnerColor, INNER_COLOR_DEFAULT);
        outerColor = ta.getColor(R.styleable.NavController_OuterColor, OUTER_COLOR_DEFAULT);
        innerColor = INNER_COLOR_DEFAULT;
        outerColor = OUTER_COLOR_DEFAULT;
        ta.recycle();

        realWidth = dip2px(context, 145.0f);
        realHeight = dip2px(context, 145.0f);

        originalX = (float) (realWidth * 0.8);
        originalY = (float) (realHeight * 1.5);

        centerX = originalX;
        centerY = originalY;

        innerCenterX = centerX;
        innerCenterY = centerY;

        outerPaint = new Paint();
        innerPaint = new Paint();
        outerPaint.setColor(outerColor);
        outerPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        innerPaint.setColor(innerColor);
        innerPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
     /*   realWidth = w;
        realHeight = h;
        innerCenterX = realWidth / 2;
        innerCenterY = realHeight / 2;*/
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        screenWidth = canvas.getWidth();
        screenHeight = canvas.getHeight();

        outRadius = Math.min(Math.min(realWidth / 2 - getPaddingLeft(), realWidth / 2 - getPaddingRight()), Math.min(realHeight / 2 - getPaddingTop(), realHeight / 2 - getPaddingBottom()));
        //画外部圆
        canvas.drawCircle(centerX, centerY, outRadius, outerPaint);
        //内部圆
        innerRedius = outRadius * 0.25f;
        canvas.drawCircle(innerCenterX, innerCenterY, innerRedius, innerPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            changeOutCirclePosition(event);
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            changeInnerCirclePosition(event);
            Log.i("TAG", "MOVED");
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            centerX = originalX;
            centerY = originalY;
            innerCenterX = originalX;
            innerCenterY = originalY;
            invalidate();
            if (mCallBack != null) {
                mCallBack.onNavAndSpeed(9);
            }
        }
        return true;
    }


    private void changeOutCirclePosition(MotionEvent e) {
        //第一步，确定有效的触摸点集
        float X = e.getX();
        float Y = e.getY();

        if (X < outRadius && Y < outRadius) {//左上
            centerX = outRadius;
            centerY = outRadius;
        } else if (X < outRadius && (screenHeight - Y) < outRadius) {//左下
            centerX = outRadius;
            centerY = screenHeight - outRadius;
        } else if ((screenWidth - X) < outRadius && Y < outRadius) {//右上
            centerX = screenWidth - outRadius;
            centerY = outRadius;
        } else if ((screenWidth - X) < outRadius && (screenHeight - Y) < outRadius) {//右下
            centerX = screenWidth - outRadius;
            centerY = screenHeight - outRadius;
        } else if ((screenWidth - X) < outRadius) {//右
            centerX = screenWidth - outRadius;
            centerY = Y;
        } else if ((screenHeight - Y) < outRadius) {//下
            centerX = X;
            centerY = screenHeight - outRadius;
        } else if (X < outRadius) {//左
            centerX = outRadius;
            centerY = Y;
        } else if (Y < outRadius) {//上
            centerX = X;
            centerY = outRadius;
        } else {
            centerX = X;
            centerY = Y;
        }
        innerCenterX = centerX;
        innerCenterY = centerY;
        invalidate();
    }

    private void changeInnerCirclePosition(MotionEvent e) {
        //圆的方程：（x-realWidth/2）^2 +（y - realHeight/2）^2 <= outRadius^2
        //第一步，确定有效的触摸点集
        float X = e.getX();
        float Y = e.getY();

//        boolean isPointInOutCircle = Math.pow(X - realWidth / 2, 2) + Math.pow(Y - realHeight / 2, 2) <= Math.pow(outRadius, 2);
//        if (isPointInOutCircle) {
        Log.i("TAG", "inCircle");

        float aX = (X - centerX);
        float aY = (Y - centerY);
        float angle = (float) Math.atan2(Math.abs(aY), Math.abs((aX)));

        int direction = 0;//1、上；2下；3、左；4、右；5、左上；6、左下；7、右上；8、右下

        if (aX > 0 && aY >= 0) {
//            360-0  第四象限
            if (angle <= 0.3827) {
                //右
                direction = 5;
            } else if (angle > 0.3827 && angle <= 0.9239) {
                //右下
                direction = 4;
            } else if (angle > 0.9239) {
                //下
                direction = 3;
            }
        } else if (aX >= 0 && aY < 0) {
            //0-90 第一象限
            if (angle <= 0.3827) {
                //右
                direction = 5;
            } else if (angle > 0.3827 && angle <= 0.9239) {
                //右上
                direction = 6;
            } else if (angle > 0.9239) {
                //上
                direction = 7;
            }
        } else if (aX < 0 && aY >= 0) {
            //180-270   第三象限
            if (angle <= 0.3827) {
                //左
                direction = 1;
            } else if (angle > 0.3827 && angle <= 0.9239) {
                //左下
                direction = 2;
            } else if (angle > 0.9239) {
                //下
                direction = 3;
            }
        } else if (aX <= 0 && aY < 0) {
            //90-180   第二象限
            if (angle <= 0.3827) {
                //左
                direction = 1;
            } else if (angle > 0.3827 && angle <= 0.9239) {
                //左上
                direction = 0;
            } else if (angle > 0.9239) {
                //上
                direction = 7;
            }
        }

        if (mCallBack != null) {
            mCallBack.onNavAndSpeed(direction);
        }

        L.e("XY", "   X:" + aX + "    Y:" + aY + "   angle:" + angle + "   direction:" + direction);

        //两种情况：小圆半径
        boolean isPointInFree = Math.pow(X - centerX, 2) + Math.pow(Y - centerY, 2) <= Math.pow(outRadius - innerRedius, 2);
        if (isPointInFree) {
            innerCenterX = X;
            innerCenterY = Y;
        } else {
            //处理限制区域，这部分使用触摸点与中心点与外圆方程交点作为内圆的中心点
            //使用近似三角形来确定这个点
            //求出触摸点，触摸点垂足和中心点构成的直角三角形（pointTri）的直角边长
            float pointTriX = Math.abs(centerX - X);//横边
            float pointTriY = Math.abs(centerY - Y);//竖边
            float pointTriZ = (float) Math.sqrt((Math.pow(pointTriX, 2) + Math.pow(pointTriY, 2)));
            float TriSin = pointTriY / pointTriZ;
            float TriCos = pointTriX / pointTriZ;
            //求出在圆环上的三角形的两个直角边的长度
            float limitCircleTriY = (outRadius - innerRedius) * TriSin;
            float limitCircleTriX = (outRadius - innerRedius) * TriCos;
            //确定内圆中心点的位置，分四种情况
            if (X >= centerX && Y >= centerY) {//下
                innerCenterX = centerX + limitCircleTriX;
                innerCenterY = centerY + limitCircleTriY;
                L.e("angle", "1");
            } else if (X < centerX && Y >= centerY) {//左
                innerCenterX = centerX - limitCircleTriX;
                innerCenterY = centerY + limitCircleTriY;
                L.e("angle", "2");
            } else if (X >= centerX && Y < centerY) {//右
                innerCenterX = centerX + limitCircleTriX;
                innerCenterY = centerY - limitCircleTriY;
                L.e("angle", "3");
            } else {//上
                innerCenterX = centerX - limitCircleTriX;
                innerCenterY = centerY - limitCircleTriY;
                L.e("angle", "4");
            }

            Log.i("TAG", "inLimit");
        }
        invalidate();
        /*} else {
            Log.i("TAG", "notInCircle");
        }*/
    }

    public void setOnNavAndSpeedListener(OnNavAndSpeedListener listener) {
        mCallBack = listener;
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}