package com.blestep.sportsbracelet.activity;

import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;
import android.widget.RelativeLayout;

import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.base.BaseActivity;
import com.blestep.sportsbracelet.view.BottomNavView;

import butterknife.Bind;
import butterknife.ButterKnife;

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
    @Bind(R.id.bnv_nav)
    BottomNavView bnv_nav;
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
}
