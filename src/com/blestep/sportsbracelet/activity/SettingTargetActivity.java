package com.blestep.sportsbracelet.activity;

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
