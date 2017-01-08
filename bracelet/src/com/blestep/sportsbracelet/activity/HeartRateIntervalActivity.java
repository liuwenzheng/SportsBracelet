package com.blestep.sportsbracelet.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.base.BaseActivity;
import com.blestep.sportsbracelet.utils.SPUtiles;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @Date 2017/1/7
 * @Author wenzheng.liu
 * @Description 设置心率间隔
 * @ClassPath com.blestep.sportsbracelet.activity.HeartRateIntervalActivity
 */
public class HeartRateIntervalActivity extends BaseActivity {

    @Bind(R.id.rb_heart_rate_close)
    RadioButton rbHeartRateClose;
    @Bind(R.id.rb_heart_rate_10)
    RadioButton rbHeartRate10;
    @Bind(R.id.rb_heart_rate_20)
    RadioButton rbHeartRate20;
    @Bind(R.id.rb_heart_rate_30)
    RadioButton rbHeartRate30;
    @Bind(R.id.rg_heart_rate)
    RadioGroup rgHeartRate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate_interval);
        ButterKnife.bind(this);
        SPUtiles.getInstance(this);
        int interval = SPUtiles.getIntValue(BTConstants.SP_KEY_HEART_RATE_INTERVAL, 2);
        switch (interval) {
            case 0:
                rbHeartRateClose.setChecked(true);
                break;
            case 1:
                rbHeartRate10.setChecked(true);
                break;
            case 2:
                rbHeartRate20.setChecked(true);
                break;
            case 3:
                rbHeartRate30.setChecked(true);
                break;
        }
        rgHeartRate.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                String interval = null;
                switch (checkedId) {
                    case R.id.rb_heart_rate_close:
                        SPUtiles.setIntValue(BTConstants.SP_KEY_HEART_RATE_INTERVAL, 0);
                        interval = rbHeartRateClose.getText().toString();
                        break;
                    case R.id.rb_heart_rate_10:
                        SPUtiles.setIntValue(BTConstants.SP_KEY_HEART_RATE_INTERVAL, 1);
                        interval = rbHeartRate10.getText().toString();
                        break;
                    case R.id.rb_heart_rate_20:
                        SPUtiles.setIntValue(BTConstants.SP_KEY_HEART_RATE_INTERVAL, 2);
                        interval = rbHeartRate20.getText().toString();
                        break;
                    case R.id.rb_heart_rate_30:
                        SPUtiles.setIntValue(BTConstants.SP_KEY_HEART_RATE_INTERVAL, 3);
                        interval = rbHeartRate30.getText().toString();
                        break;
                }
                back(interval);
            }
        });
    }


    @OnClick(R.id.iv_back)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
        }
    }

    private void back(String interval) {
        Intent intent = new Intent();
        intent.putExtra("interval", interval);
        setResult(RESULT_OK, intent);
        finish();
    }
}
