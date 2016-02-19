package com.blestep.sportsbracelet.activity;

import java.math.BigDecimal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.base.BaseActivity;
import com.blestep.sportsbracelet.module.LogModule;
import com.blestep.sportsbracelet.utils.SPUtiles;
import com.blestep.sportsbracelet.utils.ToastUtils;
import com.blestep.sportsbracelet.view.CircularSeekBar;
import com.blestep.sportsbracelet.view.CircularSeekBar.OnSeekChangeListener;
import com.umeng.analytics.MobclickAgent;

public class SettingTargetActivity extends BaseActivity implements
		OnClickListener {
	private CircularSeekBar circularSeekbar;
	private TextView tv_target_status, tv_calorie, tv_step, tv_target_walk,
			tv_target_run, tv_target_bike;
	private Button btn_target_finish;

	private static final int STEP_UNIT = 200;

	private static final int RECOMMEND_MIN = 7000;
	private static final int RECOMMEND_MAX = 15000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_target);
		initView();
		initListener();
		initData();
	}

	private void initListener() {

		circularSeekbar.setSeekBarChangeListener(new OnSeekChangeListener() {

			@Override
			public void onProgressChange(CircularSeekBar view, int newProgress) {
				LogModule.d("Progress:" + view.getProgress() * STEP_UNIT + "/"
						+ view.getMaxProgress() * STEP_UNIT);
				float activity_consumed = (0.000693f * (SPUtiles.getIntValue(
						BTConstants.SP_KEY_USER_WEIGHT, 75) - 15) + 0.005895f)
						* (view.getProgress() * STEP_UNIT);
				int result = new BigDecimal(activity_consumed).setScale(0,
						BigDecimal.ROUND_HALF_UP).intValue();

				tv_step.setText(view.getProgress() * STEP_UNIT + "");
				tv_calorie.setText(result + "");
				// 状态
				if (view.getProgress() * STEP_UNIT < RECOMMEND_MIN) {
					tv_target_status
							.setText(getString(R.string.setting_target_relaxed));
				} else if (view.getProgress() * STEP_UNIT > RECOMMEND_MAX) {
					tv_target_status
							.setText(getString(R.string.setting_target_active));
				} else {
					tv_target_status
							.setText(getString(R.string.setting_target_recommend));
				}

				tv_target_walk.setText(getString(
						R.string.setting_target_minutes,
						new BigDecimal(result / 255f * 60).setScale(0,
								BigDecimal.ROUND_HALF_UP).intValue()));
				tv_target_run.setText(getString(
						R.string.setting_target_minutes,
						new BigDecimal(result / 500f * 60).setScale(0,
								BigDecimal.ROUND_HALF_UP).intValue()));
				tv_target_bike.setText(getString(
						R.string.setting_target_minutes,
						new BigDecimal(result / 655f * 60).setScale(0,
								BigDecimal.ROUND_HALF_UP).intValue()));

			}
		});
		btn_target_finish.setOnClickListener(this);
	}

	private void initData() {
	}

	private void initView() {
		circularSeekbar = (CircularSeekBar) findViewById(R.id.csb_target);
		circularSeekbar.setMaxProgress(100);
		circularSeekbar.setProgress(0);
		circularSeekbar.setBarWidth(20);
		circularSeekbar.setRingBackgroundColor(getResources().getColor(
				R.color.grey_f2f2f2));
		circularSeekbar.setProgressColor(getResources().getColor(
				R.color.blue_97e5fb));
		circularSeekbar.setBackGroundColor(getResources().getColor(
				R.color.white_ffffff));
		circularSeekbar.invalidate();
		tv_target_status = (TextView) findViewById(R.id.tv_target_status);
		tv_calorie = (TextView) findViewById(R.id.tv_calorie);
		tv_step = (TextView) findViewById(R.id.tv_step);
		tv_target_walk = (TextView) findViewById(R.id.tv_target_walk);
		tv_target_run = (TextView) findViewById(R.id.tv_target_run);
		tv_target_bike = (TextView) findViewById(R.id.tv_target_bike);
		btn_target_finish = (Button) findViewById(R.id.btn_target_finish);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_target_finish:
			// ToastUtils.showToast(this, "设置目标为：" +
			// tv_step.getText().toString());
			if (Integer.valueOf(tv_step.getText().toString()) < 200) {
				ToastUtils.showToast(this, getString(R.string.setting_target_min));
				return;
			}

			SPUtiles.setIntValue(BTConstants.SP_KEY_STEP_AIM,
					Integer.valueOf(tv_step.getText().toString()));
			SPUtiles.setFloatValue(BTConstants.SP_KEY_STEP_AIM_POINT_X,
					circularSeekbar.markPointX);
			SPUtiles.setFloatValue(BTConstants.SP_KEY_STEP_AIM_POINT_Y,
					circularSeekbar.markPointY);
			SPUtiles.setStringValue(BTConstants.SP_KEY_STEP_AIM_CALORIE,
					tv_calorie.getText().toString());
			SPUtiles.setStringValue(BTConstants.SP_KEY_STEP_AIM_CALORIE_WALK,
					tv_target_walk.getText().toString());
			SPUtiles.setStringValue(BTConstants.SP_KEY_STEP_AIM_CALORIE_RUN,
					tv_target_run.getText().toString());
			SPUtiles.setStringValue(BTConstants.SP_KEY_STEP_AIM_CALORIE_BIKE,
					tv_target_bike.getText().toString());
			SPUtiles.setStringValue(BTConstants.SP_KEY_STEP_AIM_STATE,
					tv_target_status.getText().toString());
			SPUtiles.setBooleanValue(BTConstants.SP_KEY_IS_FIRST_OPEN, false);
			startActivity(new Intent(this, MainActivity.class));
			this.finish();
			break;

		default:
			break;
		}
	}
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}
