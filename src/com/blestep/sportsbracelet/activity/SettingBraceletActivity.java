package com.blestep.sportsbracelet.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.base.BaseActivity;

public class SettingBraceletActivity extends BaseActivity implements OnClickListener {

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
			break;
		case R.id.tv_setting_pre:
			this.finish();
			break;

		default:
			break;
		}
	}

}
