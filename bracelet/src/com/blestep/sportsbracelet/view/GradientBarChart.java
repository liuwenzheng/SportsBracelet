package com.blestep.sportsbracelet.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * @Author lwz
 * @Date 2016/9/25 0025
 * @Describe
 */
public class GradientBarChart extends View {
    // 柱子宽度
    private int mBarWidth = 32;
    // 柱子间隔
    private int mBarSpace = 48;
    // X轴颜色
    private int mAxisColor = 0x007275;
    // X轴宽度
    private int mAxisWidth = 2;
    // 标注字体大小
    private int mLabelSize = 20;
    // 标注字体高度
    private int mLabelHeight;
    // 标注字体颜色
    private int mLabelColor = 0x333333;
    // 标注和轴间距
    private int mLabelAxisSpace = 24;
    // 目标值字体高度
    private int mAimLabelHeight;
    // 控件参数
    private int mWidth;
    private int mHeight;
    private int mCenterX;
    private int mBarAreaHeight;
    /**
     * 画笔
     *
     * @param context
     */
    private Paint mBarPaint;
    private Paint mBarGadientPaint;
    private Paint mAxisPaint;
    private Paint mLabelPaint;
    private Paint mAimLabelPaint;
    private Paint mAimDashedPaint;

    /**
     * 数据
     *
     * @param context
     */
    private int mAimValue = 10000;
    private ArrayList<Integer> mBarDatas = new ArrayList<>();
    private int mMaxStepValue = 0;

    public GradientBarChart(Context context) {
        this(context, null);
    }

    public GradientBarChart(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GradientBarChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initParam(context, attrs, defStyleAttr);
    }

    private void initParam(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GradientBarChartAttrs);
        mBarWidth = (int) a.getDimension(R.styleable.GradientBarChartAttrs_gbcBarWidth, mBarWidth);
        mBarSpace = (int) a.getDimension(R.styleable.GradientBarChartAttrs_gbcBarSpace, mBarSpace);
        mAxisColor = a.getColor(R.styleable.GradientBarChartAttrs_gbcAxisColor, mAxisColor);
        mAxisWidth = (int) a.getDimension(R.styleable.GradientBarChartAttrs_gbcAxisWidth, mAxisWidth);
        mLabelSize = (int) a.getDimension(R.styleable.GradientBarChartAttrs_gbcLabelSize, mLabelSize);
        mLabelColor = a.getColor(R.styleable.GradientBarChartAttrs_gbcLabelColor, mLabelColor);
        mLabelAxisSpace = (int) a.getDimension(R.styleable.GradientBarChartAttrs_gbcLabelAxisSpace, mLabelAxisSpace);
        a.recycle();
        // 纯色柱子
        mBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBarPaint.setColor(ContextCompat.getColor(context, R.color.blue_00d4da));
        // 渐变柱子
        mBarGadientPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // X轴
        mAxisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAxisPaint.setColor(mAxisColor);
        mAxisPaint.setStrokeWidth(mAxisWidth);
        // 标签
        mLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        mLabelPaint.setColor(mLabelColor);
        mLabelPaint.setTextSize(mLabelSize);
        mLabelHeight = (int) (mLabelPaint.descent() + mLabelPaint.ascent());
        // 目标值
        mAimLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        mAimLabelPaint.setColor(ContextCompat.getColor(context, R.color.grey_999999));
        mAimLabelPaint.setTextSize(getResources().getDimension(R.dimen.aimLabelSize));
        mAimLabelHeight = (int) (mAimLabelPaint.descent() + mAimLabelPaint.ascent());

        // 目标值虚线
        mAimDashedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAimDashedPaint.setStyle(Paint.Style.STROKE);
        mAimDashedPaint.setColor(ContextCompat.getColor(context, R.color.grey_e5e5e5));
        mAimDashedPaint.setStrokeWidth(1);
        PathEffect pathEffect = new DashPathEffect(new float[]{9, 3}, 1);
        mAimDashedPaint.setPathEffect(pathEffect);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mWidth == 0 || mHeight == 0)
            return;
        drawAxis(canvas);
        drawLabels(canvas);
        if (!mBarDatas.isEmpty())
            drawBar(canvas);
        if (mAimValue != 0)
            drawAim(canvas);
    }

    private void drawBar(Canvas canvas) {
        // 拿到最大值
        for (int i = 0; i < mBarDatas.size(); i++) {
            if (mBarDatas.get(i) > mMaxStepValue)
                mMaxStepValue = mBarDatas.get(i);
        }
        int maxHeight = mHeight - mAimLabelHeight;

    }

    private void drawAim(Canvas canvas) {
        if (mAimValue > mMaxStepValue) {
            canvas.drawLine(0, mAimLabelHeight, mWidth, mAimLabelHeight, mAimDashedPaint);
            canvas.drawText(mAimValue + getResources().getString(R.string.setting_target_step), 0, 0, mAimLabelPaint);
        } else {
            int aimY = mHeight - (mAimValue / mMaxStepValue * (mHeight - mAimLabelHeight));
            canvas.drawLine(0, aimY, mWidth, aimY, mAimDashedPaint);
            canvas.drawText(mAimValue + getResources().getString(R.string.setting_target_step), 0, aimY - mAimLabelHeight, mAimLabelPaint);
        }
    }

    private void drawLabels(Canvas canvas) {
        // 获取数据
        ArrayList<String> labels = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat(BTConstants.PATTERN_MM_DD);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        for (int i = 0; i < 7; i++) {
            String date;
            if (i == 6) {
                date = getResources().getString(R.string.history_today);
            } else {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                date = sdf.format(calendar.getTime());
            }
            labels.add(date);
        }
        // 标签宽度
        int labelWidth = (int) mLabelPaint.measureText(labels.get(0));
        // 标签间隔
        int labelSpace = mBarSpace - (labelWidth - mBarWidth);
        // 标签起始x点
        int labelX = (int) (mCenterX - labelWidth * (3 + 0.5) - 3 * labelSpace);
        int labelY = mBarAreaHeight + mLabelAxisSpace;
        for (String label : labels) {
            canvas.drawText(label, labelX, labelY, mLabelPaint);
            labelX += labelWidth + labelSpace;
        }
    }

    private void drawAxis(Canvas canvas) {
        mBarAreaHeight = mHeight - mLabelHeight - mLabelAxisSpace;
        canvas.drawLine(0, mBarAreaHeight, mWidth, mBarAreaHeight, mAxisPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        mCenterX = mWidth / 2;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    static class StepBar {
        public String label;
        public String count;
    }

    public void setAimValue(int aim) {
        this.mAimValue = aim;
        invalidate();
    }

    public void setDatas(ArrayList<Integer> bars) {
        this.mBarDatas = bars;
        invalidate();
    }
}
