package com.blestep.sportsbracelet.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.blestep.sportsbracelet.R;

import butterknife.Bind;
import butterknife.ButterKnife;


public class LoginActivity extends Activity {


    @Bind(R.id.ll_bg)
    LinearLayout ll_bg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);
        ButterKnife.bind(this);
        if (getIntent() != null && getIntent().getExtras() != null) {
            int index = getIntent().getIntExtra("bg_index", 0);
            if (index == 1) {
                ll_bg.setBackgroundResource(R.drawable.guide_step_bg);
            } else if (index == 2) {
                ll_bg.setBackgroundResource(R.drawable.guide_calories_bg);
            } else if (index == 3) {
                ll_bg.setBackgroundResource(R.drawable.guide_alarm_bg);
            } else if (index == 4) {
                ll_bg.setBackgroundResource(R.drawable.guide_cloud_bg);
            }
        }

    }

}
