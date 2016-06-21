package com.blestep.sportsbracelet.activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.base.BaseActivity;
import com.umeng.analytics.MobclickAgent;

public class AboutActivity extends BaseActivity implements OnClickListener {
	private TextView tv_app_version, tv_about_firmware;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_page);
		initView();
		initListener();
		initData();
	}

	private void initView() {
		tv_app_version = (TextView) findViewById(R.id.tv_app_version);
		tv_about_firmware = (TextView) findViewById(R.id.tv_about_firmware);
	}

	private void initListener() {
		findViewById(R.id.iv_back).setOnClickListener(this);
	}

	private void initData() {
		// 获取packagemanager的实例
		PackageManager packageManager = getPackageManager();
		// getPackageName()是你当前类的包名，0代表是获取版本信息
		PackageInfo packInfo = null;
		try {
			packInfo = packageManager.getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (packInfo != null) {
			String version = packInfo.versionName;
			tv_app_version.setText("v" + version);
		}
	}

	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
		case R.id.iv_back:
			finish();
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
