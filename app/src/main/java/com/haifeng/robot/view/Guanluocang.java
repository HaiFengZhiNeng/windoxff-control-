package com.haifeng.robot.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by dell on 2017/8/9.
 */

public class Guanluocang extends TextView {
    private LinearGradient gradient;
    private Paint paint;
    private Matrix matrix;
    private int mViewWidth = 0;
    private int mTranslate = 0;


    public Guanluocang(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mViewWidth == 0) {
            mViewWidth = getMeasuredWidth();
            if (mViewWidth > 0) {
                //获取当前TextView的Paint对象,并给这个Paint对象设置原生TextView没有的额LinearGradient属性
                paint = getPaint();
                gradient = new LinearGradient(
                        0,
                        0,
                        mViewWidth,
                        0,
                        new int[]{
                                Color.RED, 0xffffffff,
                                Color.BLUE, Color.BLACK, Color.GREEN, Color.BLUE},
                        null,
                        Shader.TileMode.CLAMP);
                paint.setShader(gradient);
                matrix = new Matrix();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (matrix != null) {
            mTranslate += mViewWidth / 5;
            if (mTranslate > 2 * mViewWidth) {
                mTranslate = -mViewWidth;
            }
            matrix.setTranslate(mTranslate, 0);
            gradient.setLocalMatrix(matrix);
            postInvalidateDelayed(100);
        }
    }
}
