package com.blestep.sportsbracelet.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.blestep.sportsbracelet.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class LoginActivity extends Activity {


    @Bind(R.id.rl_bg)
    RelativeLayout rl_bg;
    @Bind(R.id.et_user_name)
    EditText et_user_name;
    @Bind(R.id.et_password)
    EditText et_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);
        ButterKnife.bind(this);
        if (getIntent() != null && getIntent().getExtras() != null) {
            int index = getIntent().getIntExtra("bg_index", 0);
            if (index == 1) {
                rl_bg.setBackgroundResource(R.drawable.guide_step_bg);
            } else if (index == 2) {
                rl_bg.setBackgroundResource(R.drawable.guide_calories_bg);
            } else if (index == 3) {
                rl_bg.setBackgroundResource(R.drawable.guide_alarm_bg);
            } else if (index == 4) {
                rl_bg.setBackgroundResource(R.drawable.guide_cloud_bg);
            }
        }

    }

    @OnClick({R.id.frame_close, R.id.tv_forget_password, R.id.btn_login})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.frame_close:
                finishActivityAnim();
                break;
            case R.id.tv_forget_password:
                break;
            case R.id.btn_login:
                startActivity(new Intent(this, ActivateBraceletActivity.class));
                finish();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finishActivityAnim();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void finishActivityAnim() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
