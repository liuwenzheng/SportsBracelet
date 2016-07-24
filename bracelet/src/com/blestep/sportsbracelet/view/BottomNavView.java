package com.blestep.sportsbracelet.view;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.blestep.sportsbracelet.R;

import butterknife.ButterKnife;

/**
 * @Author lwz
 * @Date 2016/7/21 0021
 * @Describe 导航页的底部布局，可控制按钮是否可点击和显示
 */
public class BottomNavView extends RelativeLayout implements View.OnClickListener {
    private Button btn_pre, btn_next;
    private Context mContext;
    private OnBottomNavClickListener listener;

    public BottomNavView(Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        btn_pre = ButterKnife.findById(this, R.id.btn_pre);
        btn_next = ButterKnife.findById(this, R.id.btn_next);
    }

    public void setPreVisible(boolean visible) {
        btn_pre.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setNextVisible(boolean visible) {
        btn_next.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setPreEnable(boolean enable) {
        btn_pre.setEnabled(enable);
        btn_pre.setOnClickListener(enable ? this : null);
    }

    public void setNextEnable(boolean enable) {
        btn_next.setEnabled(enable);
        btn_pre.setOnClickListener(enable ? this : null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_pre:
                if (listener != null) {
                    listener.onPreClick();
                }
                break;
            case R.id.btn_next:
                if (listener != null) {
                    listener.onNextClick();
                }
                break;
        }

    }

    public void setListener(OnBottomNavClickListener listener) {
        this.listener = listener;
    }

    interface OnBottomNavClickListener {
        void onPreClick();

        void onNextClick();
    }
}
