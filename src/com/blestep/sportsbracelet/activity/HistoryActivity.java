package com.blestep.sportsbracelet.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.db.DBTools;
import com.blestep.sportsbracelet.entity.Step;
import com.blestep.sportsbracelet.event.HistoryChangeUnitClick;
import com.blestep.sportsbracelet.fragment.HistoryStepCalorie;
import com.blestep.sportsbracelet.fragment.HistoryStepCount;
import com.blestep.sportsbracelet.fragment.HistoryStepDistance;
import com.blestep.sportsbracelet.utils.Utils;
import com.blestep.sportsbracelet.view.ControlScrollViewPager;
import com.umeng.analytics.MobclickAgent;

import de.greenrobot.event.EventBus;

public class HistoryActivity extends FragmentActivity implements
		OnClickListener, OnPageChangeListener, OnCheckedChangeListener {
	public static final int DATA_UNIT_DAY = 0;
	public static final int DATA_UNIT_WEEK = 1;
	public static final int DATA_UNIT_MONTH = 2;
	public static final int DATA_UNIT_YEAR = 3;
	public static final int COUNT_NUMBER_DAY = 7;
	public static final int COUNT_NUMBER_WEEK = 7;
	public static final int COUNT_NUMBER_MONTH = 12;
	public static final int COUNT_NUMBER_YEAR = 7;
	public int selectHistoryUnit = 0;

	private ControlScrollViewPager vp_history;
	private FragmentPagerAdapter mAdapter;
	private List<Fragment> mFragments = new ArrayList<Fragment>();
	private HistoryStepCount tab01;
	private HistoryStepCalorie tab02;
	private HistoryStepDistance tab03;

	private RadioGroup rg_history_tab;

	public ArrayList<Step> mSteps;
	public HashMap<String, Step> mStepsMap;
	public Calendar mLastDayCalendar;// 上一周的最后一天
	public Calendar mLastWeekCalendar;// 7周前的周一
	public Calendar mLastMonthCalendar;// 一年前的今天
	public Calendar mTodayCalendar;// 今天
	public Calendar m7YearAgoCalendar;// 7年前的今天

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history_page);
		EventBus.getDefault().register(this);
		initView();
		initListener();
		initData();
	}

	private void initView() {
		vp_history = (ControlScrollViewPager) findViewById(R.id.vp_history);
		vp_history.setScrollable(false);
		rg_history_tab = (RadioGroup) findViewById(R.id.rg_history_tab);
		initViewPager();
	}

	private void initListener() {
		vp_history.setOnPageChangeListener(this);
		rg_history_tab.setOnCheckedChangeListener(this);
		findViewById(R.id.tv_history_back).setOnClickListener(this);
	}

	private void initData() {
		// 拿到最新的数据开始计算日期
		Step step = mSteps.get(mSteps.size() - 1);
		mTodayCalendar = Utils.strDate2Calendar(step.date,
				BTConstants.PATTERN_YYYY_MM_DD);
		m7YearAgoCalendar = (Calendar) mTodayCalendar.clone();
		m7YearAgoCalendar.add(Calendar.YEAR, -6);
	}

	private void initViewPager() {
		mSteps = DBTools.getInstance(this).selectAllStep();
		if (mSteps.size() == 0) {
			finish();
		}
		mStepsMap = new HashMap<String, Step>();
		for (Step step : mSteps) {
			mStepsMap.put(step.date, step);
		}
		Bundle bundle = new Bundle();
		bundle.putSerializable(BTConstants.EXTRA_KEY_HISTORY, mSteps);
		tab01 = new HistoryStepCount();
		tab02 = new HistoryStepCalorie();
		tab03 = new HistoryStepDistance();

		tab01.setArguments(bundle);
		tab02.setArguments(bundle);
		tab03.setArguments(bundle);

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

			@Override
			public Object instantiateItem(ViewGroup container, int position) {

				return super.instantiateItem(container, position);
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
	public void onBackPressed() {

		super.onBackPressed();
		overridePendingTransition(R.anim.page_up_in, R.anim.page_down_out);
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

	@Override
	protected void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	public void onEvent(HistoryChangeUnitClick event) {
		this.selectHistoryUnit = event.selectHistoryUnit;
	}
}
