package com.blestep.sportsbracelet.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.base.BaseActivity;
import com.umeng.analytics.MobclickAgent;

public class SettingBraceletActivity extends BaseActivity implements
		OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_bracelet);
		initView();
		initListener();
		initData();
	}

	private void initListener() {
		findViewById(R.id.tv_setting_pre).setOnClickListener(this);
		findViewById(R.id.tv_setting_next).setOnClickListener(this);
	}

	private void initData() {
	}

	private void initView() {
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_setting_next:
			startActivity(new Intent(this, SettingDeviceActivity.class));
			this.finish();
			break;
		case R.id.tv_setting_pre:
			startActivity(new Intent(this, SettingBluetoothActivity.class));
			this.finish();
			break;

		default:
			break;
		}
	}

	@Override
	public void onBackPressed() {
		startActivity(new Intent(this, SettingBluetoothActivity.class));
		this.finish();
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
