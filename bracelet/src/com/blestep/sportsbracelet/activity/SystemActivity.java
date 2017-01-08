package com.blestep.sportsbracelet.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.utils.SPUtiles;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SystemActivity extends Activity {


    @Bind(R.id.tv_unit)
    TextView tvUnit;
    @Bind(R.id.tv_time)
    TextView tv_time;
    @Bind(R.id.tv_light)
    TextView tv_light;
    @Bind(R.id.tv_heart_rate)
    TextView tvHeartRate;
    private boolean defaultUnit;
    private int defaultTime;
    private int defaultLight;
    private int defaultInterval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system);
        ButterKnife.bind(this);
        defaultUnit = SPUtiles.getBooleanValue(BTConstants.SP_KEY_IS_BRITISH_UNIT, false);
        defaultTime = SPUtiles.getIntValue(BTConstants.SP_KEY_TIME_SYSTEM, 0);
        defaultLight = SPUtiles.getIntValue(BTConstants.SP_KEY_LIGHT_SYSTEM, 1);
        defaultInterval = SPUtiles.getIntValue(BTConstants.SP_KEY_HEART_RATE_INTERVAL, 2);
        tvUnit.setText(SPUtiles.getBooleanValue(BTConstants.SP_KEY_IS_BRITISH_UNIT, false) ? getString(R.string.bracelet_unit_british) : getString(R.string.bracelet_unit_metric));
        tv_time.setText(SPUtiles.getIntValue(BTConstants.SP_KEY_TIME_SYSTEM, 0) == 0 ?
                getString(R.string.bracelet_time_format_24) : getString(R.string.bracelet_time_format_12));
        tv_light.setText(SPUtiles.getIntValue(BTConstants.SP_KEY_LIGHT_SYSTEM, 1) == 0 ?
                getString(R.string.bracelet_light_on) : getString(R.string.bracelet_light_off));
        String interval = null;
        if (defaultInterval == 0) {
            interval = "关闭";
        } else if (defaultInterval == 1) {
            interval = "10分钟";
        } else if (defaultInterval == 2) {
            interval = "20分钟";
        } else if (defaultInterval == 3) {
            interval = "30分钟";
        }
        tvHeartRate.setText(interval);
    }

    @OnClick({R.id.iv_back, R.id.tv_reset, R.id.ll_unit, R.id.ll_time, R.id.ll_light, R.id.ll_heart_heart_rate})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                backToHome();
                break;
            case R.id.tv_reset:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.bracelet_reset_alert);
                builder.setPositiveButton(R.string.bracelet_reset_alert_confirm,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent();
                                intent.putExtra("reset", true);
                                setResult(RESULT_OK, intent);
                                dialog.dismiss();
                                SystemActivity.this.finish();
                            }
                        });
                builder.setNegativeButton(R.string.bracelet_reset_alert_cancel,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builder.show();
                break;
            case R.id.ll_unit:
                SPUtiles.getInstance(this);
                SPUtiles.setBooleanValue(BTConstants.SP_KEY_IS_BRITISH_UNIT,
                        !SPUtiles.getBooleanValue(BTConstants.SP_KEY_IS_BRITISH_UNIT, false));
                tvUnit.setText(SPUtiles.getBooleanValue(BTConstants.SP_KEY_IS_BRITISH_UNIT, false) ?
                        getString(R.string.bracelet_unit_british) : getString(R.string.bracelet_unit_metric));
                break;
            case R.id.ll_time:
                SPUtiles.getInstance(this);
                SPUtiles.setIntValue(BTConstants.SP_KEY_TIME_SYSTEM,
                        SPUtiles.getIntValue(BTConstants.SP_KEY_TIME_SYSTEM, 0) == 0 ? 1 : 0);
                tv_time.setText(SPUtiles.getIntValue(BTConstants.SP_KEY_TIME_SYSTEM, 0) == 0 ?
                        getString(R.string.bracelet_time_format_24) : getString(R.string.bracelet_time_format_12));
                break;
            case R.id.ll_light:
                SPUtiles.getInstance(this);
                SPUtiles.setIntValue(BTConstants.SP_KEY_LIGHT_SYSTEM,
                        SPUtiles.getIntValue(BTConstants.SP_KEY_LIGHT_SYSTEM, 1) == 0 ? 1 : 0);
                tv_light.setText(SPUtiles.getIntValue(BTConstants.SP_KEY_LIGHT_SYSTEM, 1) == 0 ?
                        getString(R.string.bracelet_light_on) : getString(R.string.bracelet_light_off));
                break;
            case R.id.ll_heart_heart_rate:
                startActivityForResult(new Intent(this, HeartRateIntervalActivity.class), BTConstants.REQUEST_CODE_HEART_RATE_INTERVAL);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BTConstants.REQUEST_CODE_HEART_RATE_INTERVAL) {
            if (resultCode == RESULT_OK) {
                String interval = data.getStringExtra("interval");
                tvHeartRate.setText(interval);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backToHome();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void backToHome() {
        if (defaultUnit == SPUtiles.getBooleanValue(BTConstants.SP_KEY_IS_BRITISH_UNIT, false)
                && defaultTime == SPUtiles.getIntValue(BTConstants.SP_KEY_TIME_SYSTEM, 0)
                && defaultLight == SPUtiles.getIntValue(BTConstants.SP_KEY_LIGHT_SYSTEM, 1)
                && defaultInterval == SPUtiles.getIntValue(BTConstants.SP_KEY_HEART_RATE_INTERVAL, 2)) {
            setResult(RESULT_CANCELED);
            this.finish();
        } else {
            // 有值更改
            setResult(RESULT_OK);
            this.finish();
        }

    }
}
