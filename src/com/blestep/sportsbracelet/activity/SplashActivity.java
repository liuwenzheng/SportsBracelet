package com.blestep.sportsbracelet.activity;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.base.BaseActivity;
import com.blestep.sportsbracelet.utils.SPUtiles;
import com.umeng.analytics.MobclickAgent;

public class SplashActivity extends BaseActivity implements
		OnPageChangeListener, OnClickListener {
	private ViewPager vp_splash;
	private RadioGroup rg_splash;
	private SplashPagerAdapter mAdapter;
	private View splash_item_one, splash_item_two, splash_item_three,
			splash_item_four;
	private ArrayList<View> mViews;
	private Button btn_enter, btn_splash_item_one_pass,
			btn_splash_item_two_pass;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MobclickAgent.setDebugMode(true);
		if (!SPUtiles.getBooleanValue(BTConstants.SP_KEY_IS_FIRST_OPEN, true)) {
			setContentView(R.layout.splash_pass);
			new Thread() {
				public void run() {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					startActivity(new Intent(SplashActivity.this,
							MainActivity.class));
					SplashActivity.this.finish();
				};
			}.start();
			return;
		}
		setContentView(R.layout.splash);

		initView();
		initData();
		initListener();
	}

	private void initListener() {
		vp_splash.setOnPageChangeListener(this);
		// btn_enter.setOnClickListener(this);
		btn_splash_item_one_pass.setOnClickListener(this);
		btn_splash_item_two_pass.setOnClickListener(this);
	}

	private void initData() {
		mViews = new ArrayList<View>();
		mViews.add(splash_item_one);
		mViews.add(splash_item_two);
		// mViews.add(splash_item_three);
		// mViews.add(splash_item_four);
		mAdapter = new SplashPagerAdapter();
		vp_splash.setAdapter(mAdapter);
	}

	private void initView() {
		vp_splash = (ViewPager) findViewById(R.id.vp_splash);
		rg_splash = (RadioGroup) findViewById(R.id.rg_splash);
		((RadioButton) rg_splash.getChildAt(0)).setChecked(true);
		splash_item_one = LayoutInflater.from(this).inflate(
				R.layout.splash_item_one, null);
		btn_splash_item_one_pass = (Button) splash_item_one
				.findViewById(R.id.btn_splash_item_one_pass);
		splash_item_two = LayoutInflater.from(this).inflate(
				R.layout.splash_item_two, null);
		btn_splash_item_two_pass = (Button) splash_item_two
				.findViewById(R.id.btn_splash_item_two_pass);
		// splash_item_three =
		// LayoutInflater.from(this).inflate(R.layout.splash_item_three, null);
		// splash_item_four =
		// LayoutInflater.from(this).inflate(R.layout.splash_item_four, null);
		// btn_enter = (Button) splash_item_four.findViewById(R.id.btn_enter);

	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int position) {
		((RadioButton) rg_splash.getChildAt(position)).setChecked(true);
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		switch (v.getId()) {
		// case R.id.btn_enter:
		// Intent intent = new Intent(this, StepActivity.class);
		// startActivity(intent);
		// this.finish();
		// break;
		case R.id.btn_splash_item_one_pass:
			intent = new Intent(this, SettingBluetoothActivity.class);
			startActivity(intent);
			this.finish();
			break;

		case R.id.btn_splash_item_two_pass:
			intent = new Intent(this, SettingBluetoothActivity.class);
			startActivity(intent);
			this.finish();
			break;

		default:
			break;
		}

	}

	class SplashPagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return mViews.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			((ViewPager) vp_splash).addView(mViews.get(position));
			return mViews.get(position);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) vp_splash).removeView(mViews.get(position));
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
