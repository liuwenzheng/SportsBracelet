package com.blestep.sportsbracelet.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.base.BaseActivity;
import com.blestep.sportsbracelet.entity.HeartRate;
import com.blestep.sportsbracelet.utils.SPUtiles;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HeartRateDailyActivity extends BaseActivity {

    @Bind(R.id.tv_heart_rate_present)
    TextView tvHeartRatePresent;
    @Bind(R.id.tv_max)
    TextView tvMax;
    @Bind(R.id.tv_high)
    TextView tvHigh;
    @Bind(R.id.tv_low)
    TextView tvLow;
    @Bind(R.id.tv_min)
    TextView tvMin;
    @Bind(R.id.tv_end)
    TextView tvEnd;
    @Bind(R.id.rl_header_rate)
    RelativeLayout rlHeaderRate;
    private HeartRate rate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate_daily);
        ButterKnife.bind(this);
        if (getIntent() != null && getIntent().getExtras() != null) {
            rate = (HeartRate) getIntent().getSerializableExtra("heartRate");
            tvHeartRatePresent.setText(rate.value);
            int age = SPUtiles.getIntValue(BTConstants.SP_KEY_USER_AGE, 30);
            int max = 220 - age;
            int high = (int) Math.round(max * 0.85);
            int low = (int) Math.round(max * 0.7);
            int min = (int) Math.round(max * 0.5);
            int end = (int) Math.round(max * 0.4);
            tvMax.setText(max + "");
            tvHigh.setText(high + "");
            tvLow.setText(low + "");
            tvMin.setText(min + "");
            tvEnd.setText(end + "");
            int levelHeight = getResources().getDimensionPixelSize(R.dimen.heart_rate_level_height);
            int value = Integer.valueOf(rate.value);
            int marginTop = levelHeight;
            if (value <= max && value > high) {
                marginTop += (max - value) * levelHeight / (max - high);
            } else if (value <= high && value > low) {
                marginTop += levelHeight;
                marginTop += (high - value) * levelHeight / (high - low);
            } else if (value <= low && value > min) {
                marginTop += levelHeight * 2;
                marginTop += (low - value) * levelHeight / (low - min);
            } else if (value <= min && value > end) {
                marginTop += levelHeight * 3;
                marginTop += (min - value) * levelHeight / (min - end);
            } else {
                marginTop += 4 * levelHeight;
            }
            ImageView view = new ImageView(this);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
            lp.setMargins(0, marginTop, 0, 0);
            view.setLayoutParams(lp);
            view.setImageResource(R.drawable.heart_rate_present);
            rlHeaderRate.addView(view);
        } else {
            finishActivityAnim();
        }
    }


    @OnClick(R.id.iv_back)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finishActivityAnim();
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
