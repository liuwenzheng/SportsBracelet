package com.blestep.sportsbracelet.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.view.View;

import com.blestep.sportsbracelet.R;

/**
 * @Date 2017/1/8
 * @Author wenzheng.liu
 * @Description 虚线
 * @ClassPath com.blestep.sportsbracelet.view.DashedLine
 */
public class DashedLine extends View {


    // 控件参数
    private int mWidth;
    private int mHeight;
    private int mCenterX;
    private int mCenterY;
    /**
     * 画笔
     *
     * @param context
     */
    private Paint mAimDashedPaint;


    public DashedLine(Context context) {
        this(context, null);
    }

    public DashedLine(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DashedLine(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initParam(context, attrs, defStyleAttr);
    }

    private void initParam(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DashedLineAttrs);
        int color = a.getColor(R.styleable.DashedLineAttrs_dashedLineColor, 0x333333);
        a.recycle();
        // 目标值虚线
        mAimDashedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAimDashedPaint.setStyle(Paint.Style.STROKE);
        mAimDashedPaint.setColor(color);
        mAimDashedPaint.setStrokeWidth(1);
        PathEffect pathEffect = new DashPathEffect(new float[]{9, 3}, 1);
        mAimDashedPaint.setPathEffect(pathEffect);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mWidth == 0 || mHeight == 0)
            return;
        Path path = new Path();
        path.moveTo(0, mCenterY);
        path.lineTo(mWidth, mCenterY);
        canvas.drawPath(path, mAimDashedPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        mCenterX = mWidth / 2;
        mCenterY = mHeight / 2;
    }
}
