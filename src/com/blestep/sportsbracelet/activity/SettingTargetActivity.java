package com.blestep.sportsbracelet.activity;

import java.math.BigDecimal;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.base.BaseActivity;
import com.blestep.sportsbracelet.module.LogModule;
import com.blestep.sportsbracelet.view.CircularSeekBar;
import com.blestep.sportsbracelet.view.CircularSeekBar.OnSeekChangeListener;

public class SettingTargetActivity extends BaseActivity implements OnClickListener {
	private CircularSeekBar circularSeekbar;
	private static final int STEP_UNIT = 200;
	private static final int WEIGHT = 75;
	private static final int HEIGHT = 175;
	private static final int AGE = 26;
	private static final int WALK = 255;
	private static final int RUN = 500;
	private static final int BIKE = 655;
	private static final float CALORIES_PARAMS_1 = 0.000693f;
	private static final float CALORIES_PARAMS_2 = 0.005895f;
	private static final int CALORIES_PARAMS_3 = 15;
	private static int SEX = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_target);
		circularSeekbar = (CircularSeekBar) findViewById(R.id.csb_target);
		circularSeekbar.setMaxProgress(100);
		circularSeekbar.setProgress(0);
		circularSeekbar.setBarWidth(10);
		circularSeekbar.setRingBackgroundColor(getResources().getColor(R.color.blue_17779d));
		circularSeekbar.setProgressColor(getResources().getColor(R.color.red_FF4400));
		circularSeekbar.setBackGroundColor(getResources().getColor(R.color.white_ffffff));
		circularSeekbar.invalidate();
		circularSeekbar.setSeekBarChangeListener(new OnSeekChangeListener() {

			@Override
			public void onProgressChange(CircularSeekBar view, int newProgress) {
				LogModule.d("Progress:" + view.getProgress() * STEP_UNIT + "/" + view.getMaxProgress() * STEP_UNIT);

				float activity_consumed = (CALORIES_PARAMS_1 * (WEIGHT - CALORIES_PARAMS_3) + CALORIES_PARAMS_2)
						* (view.getProgress() * STEP_UNIT);

				// BigDecimal param1 = new
				// BigDecimal(CALORIES_PARAMS_1).multiply(new BigDecimal(WEIGHT
				// - CALORIES_PARAMS_3));
				// BigDecimal param2 = param1.add(new
				// BigDecimal(CALORIES_PARAMS_2));
				// BigDecimal activity_consumed = param2.multiply(new
				// BigDecimal(view.getProgress() * STEP_UNIT));

				// BigDecimal weight = new BigDecimal(10).multiply(new
				// BigDecimal(WEIGHT));
				// BigDecimal height = new BigDecimal(6.25).multiply(new
				// BigDecimal(HEIGHT));
				// BigDecimal age = new BigDecimal(5).multiply(new
				// BigDecimal(AGE));
				// BigDecimal param3;
				float rest_consumed;
				if (SEX == 1) {
					rest_consumed = (float) (1.15 * ((10 * WEIGHT) + (6.25 * HEIGHT) - (5 * AGE) + 5));
					// param3 = weight.add(height).subtract(age).add(new
					// BigDecimal(5));
				} else {
					rest_consumed = (float) (1.15 * ((10 * WEIGHT) + (6.25 * HEIGHT) - (5 * AGE) - 161));

					// param3 = weight.add(height).subtract(age).subtract(new
					// BigDecimal(161));
				}
				int result = new BigDecimal(activity_consumed + rest_consumed).setScale(0, BigDecimal.ROUND_HALF_UP)
						.intValue();
				// BigDecimal rest_consumed = param3.multiply(new
				// BigDecimal(1.15));
				// BigDecimal result = activity_consumed.add(rest_consumed);
				// int strResult = result.setScale(0,
				// BigDecimal.ROUND_HALF_UP).intValue();
				LogModule.d(result + "");
				// int walk = (((new BigDecimal(strResult).divide(new
				// BigDecimal(24), 2)).divide(new BigDecimal(WALK), 2))
				// .multiply(new BigDecimal(60))).setScale(0,
				// BigDecimal.ROUND_HALF_UP).intValue();
				// int run = (((new BigDecimal(strResult).divide(new
				// BigDecimal(24), 2)).divide(new BigDecimal(RUN), 2))
				// .multiply(new BigDecimal(60))).setScale(0,
				// BigDecimal.ROUND_HALF_UP).intValue();
				// int bike = (((new BigDecimal(strResult).divide(new
				// BigDecimal(24), 2)).divide(new BigDecimal(BIKE), 2))
				// .multiply(new BigDecimal(60))).setScale(0,
				// BigDecimal.ROUND_HALF_UP).intValue();
				// LogModule.d(walk + "");
				// LogModule.d(run + "");
				// LogModule.d(bike + "");
			}
		});
		initView();
		initListener();
		initData();
	}

	private void initListener() {
	}

	private void initData() {
	}

	private void initView() {
	}

	@Override
	public void onClick(View v) {

	}

}
