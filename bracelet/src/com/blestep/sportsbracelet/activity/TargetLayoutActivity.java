package com.blestep.sportsbracelet.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.base.BaseActivity;
import com.blestep.sportsbracelet.module.LogModule;
import com.blestep.sportsbracelet.utils.SPUtiles;
import com.blestep.sportsbracelet.utils.ToastUtils;

import java.math.BigDecimal;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class TargetLayoutActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener {

    @Bind(R.id.iv_back)
    ImageView iv_back;
    @Bind(R.id.tv_target_steps_value)
    TextView tv_target_steps_value;
    @Bind(R.id.tv_target_calorie_value)
    TextView tv_target_calorie_value;
    @Bind(R.id.sb_target)
    SeekBar sb_target;
    @Bind(R.id.tv_target_walk)
    TextView tv_target_walk;
    @Bind(R.id.tv_target_run)
    TextView tv_target_run;
    @Bind(R.id.tv_target_bike)
    TextView tv_target_bike;
    private static final int STEP_UNIT = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.target_layout);
        ButterKnife.bind(this);
        tv_target_walk.setText(getString(R.string.setting_target_minutes, 0));
        tv_target_run.setText(getString(R.string.setting_target_minutes, 0));
        tv_target_bike.setText(getString(R.string.setting_target_minutes, 0));
        int aim = SPUtiles.getIntValue(BTConstants.SP_KEY_STEP_AIM, 0);
        sb_target.setOnSeekBarChangeListener(this);
        if (aim != 0) {
            tv_target_steps_value.setText(aim + "");
            sb_target.setProgress(aim / STEP_UNIT);
        }
    }

    @OnClick({R.id.iv_back, R.id.btn_target_finish})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.btn_target_finish:

                if (Integer.valueOf(tv_target_steps_value.getText().toString()) < STEP_UNIT) {
                    ToastUtils.showToast(this, getString(R.string.setting_target_min));
                    return;
                }
                SPUtiles.setIntValue(BTConstants.SP_KEY_STEP_AIM, Integer.valueOf(tv_target_steps_value.getText().toString()));
                SPUtiles.setBooleanValue(BTConstants.SP_KEY_IS_FIRST_OPEN, false);
                // TODO: 2016/7/28 跳转主页面
                startActivity(new Intent(this, MainActivity.class));
                finishActivities(GuideActivity.class, ActivateBraceletActivity.class,
                        BluetoothOpenActivity.class, MatchDevicesActivity.class,
                        UserInfoLayoutActivity.class, this.getClass());
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        LogModule.d("Progress:" + progress * STEP_UNIT + "/" + 100 * STEP_UNIT);
        float activity_consumed = (0.000693f * (SPUtiles.getIntValue(
                BTConstants.SP_KEY_USER_WEIGHT, 75) - 15) + 0.005895f)
                * (progress * STEP_UNIT);
        int result = new BigDecimal(activity_consumed).setScale(0,
                BigDecimal.ROUND_HALF_UP).intValue();

        tv_target_steps_value.setText(progress * STEP_UNIT + "");
        tv_target_calorie_value.setText(result + "");

        tv_target_walk.setText(getString(R.string.setting_target_minutes,
                new BigDecimal(result / 255f * 60).setScale(0, BigDecimal.ROUND_HALF_UP).intValue()));
        tv_target_run.setText(getString(R.string.setting_target_minutes,
                new BigDecimal(result / 500f * 60).setScale(0, BigDecimal.ROUND_HALF_UP).intValue()));
        tv_target_bike.setText(getString(R.string.setting_target_minutes,
                new BigDecimal(result / 655f * 60).setScale(0, BigDecimal.ROUND_HALF_UP).intValue()));

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
