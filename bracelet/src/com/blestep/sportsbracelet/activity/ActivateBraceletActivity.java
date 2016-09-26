package com.blestep.sportsbracelet.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.base.BaseActivity;
import com.blestep.sportsbracelet.view.BottomNavView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ActivateBraceletActivity extends BaseActivity {

    @Bind(R.id.iv_activate_hand)
    ImageView iv_activate_hand;
    @Bind(R.id.bnv_nav)
    BottomNavView bnv_nav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activate_bracelet_layout);
        ButterKnife.bind(this);
        bnv_nav.setPreVisible(false);
        bnv_nav.setNextEnable(true);
        bnv_nav.setListener(this);
        final Animation animation1 = AnimationUtils.loadAnimation(this, R.anim.activate_hand_translate);
        final Animation animation2 = AnimationUtils.loadAnimation(this, R.anim.activate_hand_translate_stop);
        iv_activate_hand.setAnimation(animation1);
        animation1.start();
        animation1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                iv_activate_hand.setAnimation(animation2);
                animation2.start();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animation2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                iv_activate_hand.setAnimation(animation1);
                animation1.start();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    @Override
    public void onNextClick() {
        startActivity(new Intent(this, BluetoothOpenActivity.class));
    }
}
