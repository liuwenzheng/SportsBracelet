package com.blestep.sportsbracelet.activity;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.fragment.HistoryTab01;

public class HistoryActivity extends FragmentActivity implements
		OnClickListener, OnPageChangeListener, OnCheckedChangeListener {
	private ViewPager vp_history;
	private FragmentPagerAdapter mAdapter;
	private List<Fragment> mFragments = new ArrayList<Fragment>();
	private HistoryTab01 tab01;
	private HistoryTab01 tab02;
	private HistoryTab01 tab03;

	private RadioGroup rg_history_tab;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history_page);
		initView();
		initListener();
		initData();
	}

	private void initView() {
		vp_history = (ViewPager) findViewById(R.id.vp_history);
		rg_history_tab = (RadioGroup) findViewById(R.id.rg_history_tab);
		initViewPager();
	}

	private void initListener() {
		vp_history.setOnPageChangeListener(this);
		rg_history_tab.setOnCheckedChangeListener(this);
		findViewById(R.id.tv_history_back).setOnClickListener(this);
	}

	private void initData() {
	}

	private void initViewPager() {
		tab01 = new HistoryTab01();
		tab02 = new HistoryTab01();
		tab03 = new HistoryTab01();
		mFragments.add(tab01);
		mFragments.add(tab02);
		mFragments.add(tab03);
		/**
		 * 初始化Adapter
		 */
		mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
			@Override
			public int getCount() {
				return mFragments.size();
			}

			@Override
			public Fragment getItem(int arg0) {
				return mFragments.get(arg0);
			}
		};
		vp_history.setAdapter(mAdapter);
	}

	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
		case R.id.tv_history_back:
			finish();
			overridePendingTransition(R.anim.page_up_in, R.anim.page_down_out);
			break;

		default:
			break;
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.rb_history_tab_step:
			vp_history.setCurrentItem(0);
			break;
		case R.id.rb_history_tab_calorie:
			vp_history.setCurrentItem(1);
			break;
		case R.id.rb_history_tab_distance:
			vp_history.setCurrentItem(2);
			break;

		default:
			break;
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int position) {
		((RadioButton) rg_history_tab.getChildAt(position * 2))
				.setChecked(true);
	}

}
