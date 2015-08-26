package com.blestep.sportsbracelet.activity;

import android.os.Bundle;

import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.base.BaseActivity;
import com.blestep.sportsbracelet.view.CircleProgressView;

public class SplashActivity extends BaseActivity {

	private CircleProgressView circleView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		circleView = (CircleProgressView) findViewById(R.id.circleView);
		circleView.setMaxValue(100);
		circleView.setValueAnimated(45);
	}
}
