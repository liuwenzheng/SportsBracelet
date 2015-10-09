package com.blestep.sportsbracelet.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.base.BaseActivity;

public class SettingBluetoothActivity extends BaseActivity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_bluetooth);
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
