package com.blestep.sportsbracelet.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.entity.Sleep;
import com.blestep.sportsbracelet.utils.Utils;

/**
 * 睡眠状态视图
 */
public class SleepStatusView extends View {
    private int mWidth;
    private float mStatusWidth;
    private int mHeight;
    private Sleep mSleep;
    /**
     * 画笔
     */
    private Paint mBgPaint;
    private Paint mStatusPaint;

    public SleepStatusView(Context context) {
        this(context, null);
    }

    public SleepStatusView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SleepStatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initParam(context, attrs, defStyleAttr);
    }

    private void initParam(Context context, AttributeSet attrs, int defStyleAttr) {
        mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mStatusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgPaint.setColor(ContextCompat.getColor(context, R.color.blue_d8a86f));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mWidth == 0 || mHeight == 0)
            return;
        if (mSleep != null) {
            // 红色背景，默认清醒
            canvas.drawRect(0, 0, mWidth, mHeight, mBgPaint);
            // int intervalMin = Utils.getIntervalMin(mSleep.start, mSleep.end, BTConstants.PATTERN_YYYY_MM_DD_HH_MM);
            String record = mSleep.record;
            if (TextUtils.isEmpty(record)) {
                return;
            }
            // record的长度/2再乘以20就是总睡眠时间，再/5就是5分钟睡眠长度
            mStatusWidth = (float) mWidth / (record.length() * 2);
            float statusX = 0;
            for (int i = 0, length = record.length(); i < length; i += 2) {
                String hex = record.substring(i, i + 2);
                // 转换为二进制
                String binary = Utils.hexString2binaryString(hex);
                for (int j = binary.length(); j > 0; ) {
                    if (statusX >= mWidth) {
                        break;
                    }
                    j -= 2;
                    String status = binary.substring(j, j + 2);
                    if ("01".equals(status)) {
                        // 浅睡
                        mStatusPaint.setColor(ContextCompat.getColor(getContext(), R.color.blue_01a1bc));
                        canvas.drawRect(statusX, 0, statusX + mStatusWidth, mHeight, mStatusPaint);
                    } else if ("10".equals(status)) {
                        // 深睡
                        mStatusPaint.setColor(ContextCompat.getColor(getContext(), R.color.blue_006a94));
                        canvas.drawRect(statusX, 0, statusX + mStatusWidth, mHeight, mStatusPaint);
                    }
                    statusX += mStatusWidth;
                }
            }
        }
    }

    public void setData(Sleep sleep) {
        this.mSleep = sleep;
        invalidate();
    }
}
