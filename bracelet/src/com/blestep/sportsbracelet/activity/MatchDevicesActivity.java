package com.blestep.sportsbracelet.activity;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.base.BaseActivity;
import com.blestep.sportsbracelet.view.BottomNavView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MatchDevicesActivity extends BaseActivity {

    @Bind(R.id.tv_match_tips_1)
    TextView tv_match_tips_1;
    @Bind(R.id.tv_match_tips_2)
    TextView tv_match_tips_2;
    @Bind(R.id.tv_match_tips_3)
    TextView tv_match_tips_3;
    @Bind(R.id.tv_match_tips_4)
    TextView tv_match_tips_4;
    @Bind(R.id.rl_match_auto)
    RelativeLayout rl_match_auto;
    @Bind(R.id.iv_match_loading)
    ImageView iv_match_loading;
    @Bind(R.id.bnv_nav)
    BottomNavView bnv_nav;
    @Bind(R.id.frame_match_loading)
    FrameLayout frame_match_loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.match_devices_layout);
        ButterKnife.bind(this);
        bnv_nav.setPreEnable(true);
        bnv_nav.setListener(this);
    }

    @Override
    public void onNextClick() {
        super.onNextClick();
        // TODO: 2016/7/25 跳转个人信息设置页面
    }

    @Override
    public void onPreClick() {
        super.onPreClick();
        finish();
    }

    @OnClick(R.id.rl_match_auto)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_match_auto:
                // 开始配对
                startMatchAuto();
                break;
        }
    }

    private void startMatchAuto() {
        rl_match_auto.setVisibility(View.GONE);
        frame_match_loading.setVisibility(View.VISIBLE);
        tv_match_tips_3.setVisibility(View.GONE);
        tv_match_tips_4.setVisibility(View.GONE);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.match_loading_rotate);
        iv_match_loading.setAnimation(animation);
        animation.start();
    }
}
