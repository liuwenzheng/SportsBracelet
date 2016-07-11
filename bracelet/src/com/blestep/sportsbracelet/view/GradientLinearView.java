package com.blestep.sportsbracelet.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;

import com.blestep.sportsbracelet.R;

/**
 * Created by wenzheng.liu on 2016/7/11.
 */
public class GradientLinearView extends View {

    private LinearGradient gradinet;
    private Paint paint;
    private int width;
    private int height;
    private int start;
    private int end;

    public GradientLinearView(Context context) {
        super(context);
        getDisplay(context);
    }

    public GradientLinearView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getDisplay(context);
    }

    private void getDisplay(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        width = wm.getDefaultDisplay().getWidth();
        height = wm.getDefaultDisplay().getHeight();
        start = getResources().getColor(R.color.guide1_start_3d8400);
        end = getResources().getColor(R.color.guide1_end_7dcd00);
    }

    public void setGradient(int start, int end) {
        this.start = start;
        this.end = end;
        gradinet = new LinearGradient(0, 0, width, height, start, end, Shader.TileMode.MIRROR);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (paint == null) {
            paint = new Paint();
        }
        if (gradinet == null) {
            gradinet = new LinearGradient(0, 0, width, height, start, end, Shader.TileMode.MIRROR);
        }
        paint.setShader(gradinet);
        canvas.drawRect(0, 0, width, height, paint);
    }

}
