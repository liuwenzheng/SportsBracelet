package com.blestep.sportsbracelet.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.blestep.sportsbracelet.module.LogModule;
import com.blestep.sportsbracelet.utils.SPUtiles;
import com.blestep.sportsbracelet.utils.Utils;
import com.blestep.sportsbracelet.view.ControlScrollViewPager;
import com.db.chart.model.Bar;
import com.db.chart.model.BarSet;
import com.umeng.analytics.MobclickAgent;

import de.greenrobot.event.EventBus;

public class HistoryActivity extends FragmentActivity implements
		OnClickListener, OnPageChangeListener, OnCheckedChangeListener {
	private static final String TAG = HistoryActivity.class.getSimpleName();
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
	public Calendar mTodayCalendar;// 今天
	public Calendar m7YearAgoCalendar;// 7年前的今天
	public Calendar m3WeekAgoCalendar;// 3周前的今天
	public Calendar m3MonthAgoCalendar;// 3月前的今天
	public Calendar m3YearAgoCalendar;// 3年前的今天
	private SimpleDateFormat mSdf;

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
		m7YearAgoCalendar.add(Calendar.YEAR, -7);
		m3WeekAgoCalendar = (Calendar) mTodayCalendar.clone();
		m3WeekAgoCalendar.add(Calendar.DAY_OF_MONTH, -21);
		m3MonthAgoCalendar = (Calendar) mTodayCalendar.clone();
		m3MonthAgoCalendar.add(Calendar.WEEK_OF_YEAR, -21);
		m3YearAgoCalendar = (Calendar) mTodayCalendar.clone();
		m3YearAgoCalendar.add(Calendar.YEAR, -3);

		mSdf = new SimpleDateFormat(BTConstants.PATTERN_MM_DD);
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
		vp_history.setOffscreenPageLimit(2);
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
		if (event.selectHistoryUnit == DATA_UNIT_DAY) {
			getDayData(null, event);
		} else if (event.selectHistoryUnit == DATA_UNIT_WEEK) {
			getWeekData(null, event);
		} else if (event.selectHistoryUnit == DATA_UNIT_MONTH) {
			getMonthData(null, event);
		} else if (event.selectHistoryUnit == DATA_UNIT_YEAR) {
			getYearData(event);
		}
		LogModule.i(TAG + "onEvent-->" + event.selectHistoryUnit);
	}

	public HistoryChangeUnitClick getDayData(Calendar calendar,
			HistoryChangeUnitClick event) {
		BarSet dataCount = new BarSet();
		BarSet dataCalorie = new BarSet();
		BarSet dataDistance = new BarSet();

		String[] labels = new String[COUNT_NUMBER_DAY];
		String[] valueCount = new String[COUNT_NUMBER_DAY];
		String[] valueCalorie = new String[COUNT_NUMBER_DAY];
		String[] valueDistance = new String[COUNT_NUMBER_DAY];
		Calendar calendarLabels;
		if (calendar == null) {
			calendarLabels = (Calendar) mTodayCalendar.clone();
		} else {
			calendarLabels = (Calendar) calendar.clone();
		}
		for (int i = COUNT_NUMBER_DAY - 1; i >= 0; i--) {
			if (calendarLabels.getTime().compareTo(mTodayCalendar.getTime()) >= 0) {
				labels[i] = getString(R.string.history_today);
				calendarLabels.add(Calendar.DAY_OF_MONTH, -1);
				continue;
			}
			labels[i] = mSdf.format(calendarLabels.getTime());
			calendarLabels.add(Calendar.DAY_OF_MONTH, -1);
		}
		// 拿到最新的数据开始计算日期
		if (calendar == null) {
			calendar = (Calendar) mTodayCalendar.clone();
		}
		// 拿到当天所在月的第一天
		calendar.add(Calendar.DAY_OF_MONTH, 1 - COUNT_NUMBER_DAY);
		int[] sortDataCount = new int[COUNT_NUMBER_DAY];
		int[] sortDataCalorie = new int[COUNT_NUMBER_DAY];
		float[] sortDataDistance = new float[COUNT_NUMBER_DAY];
		for (int i = 0; i < COUNT_NUMBER_DAY; i++) {
			int dayCount = 0;
			int dayCalorie = 0;
			float dayDistance = 0;
			if (mStepsMap.get(Utils.calendar2strDate(calendar,
					BTConstants.PATTERN_YYYY_MM_DD)) != null) {
				dayCount = Integer.valueOf(mStepsMap.get(Utils
						.calendar2strDate(calendar,
								BTConstants.PATTERN_YYYY_MM_DD)).count);
				dayCalorie = Integer.valueOf(mStepsMap.get(Utils
						.calendar2strDate(calendar,
								BTConstants.PATTERN_YYYY_MM_DD)).calories);
				dayDistance = Float.valueOf(mStepsMap.get(Utils
						.calendar2strDate(calendar,
								BTConstants.PATTERN_YYYY_MM_DD)).distance);
				sortDataCount[i] = dayCount;
				sortDataCalorie[i] = dayCalorie;
				sortDataDistance[i] = dayDistance;
			}
			Bar barCount = new Bar(labels[i], dayCount);
			Bar barCalorie = new Bar(labels[i], dayCalorie);
			Bar barDistance = new Bar(labels[i], dayDistance);
			valueCount[i] = dayCount + "";
			valueCalorie[i] = dayCalorie + "";
			valueDistance[i] = dayDistance + "";
			sortDataCount[i] = dayCount;
			sortDataCalorie[i] = dayCalorie;
			sortDataDistance[i] = dayDistance;
			dataCount.addBar(barCount);
			dataCalorie.addBar(barCalorie);
			dataDistance.addBar(barDistance);
			calendar.add(Calendar.DAY_OF_MONTH, 1);
		}
		Arrays.sort(sortDataCount);
		Arrays.sort(sortDataCalorie);
		Arrays.sort(sortDataDistance);
		int barCountMax = 100;
		int barCalorieMax = 100;
		int barDistanceMax = 1;
		int barStepAim = SPUtiles.getIntValue(BTConstants.SP_KEY_STEP_AIM, 100);
		if (sortDataCount[COUNT_NUMBER_DAY - 1] >= barStepAim) {
			barCountMax = sortDataCount[COUNT_NUMBER_DAY - 1];
		} else {
			barCountMax = barStepAim;
		}
		if (sortDataCalorie[COUNT_NUMBER_DAY - 1] >= barCalorieMax) {
			barCalorieMax = sortDataCalorie[COUNT_NUMBER_DAY - 1];
		}
		if (sortDataDistance[COUNT_NUMBER_DAY - 1] >= barDistanceMax) {
			barDistanceMax = Float.valueOf(
					sortDataDistance[COUNT_NUMBER_DAY - 1]).intValue() + 1;
		}
		dataCount.setColor(getResources().getColor(R.color.blue_b4efff));
		dataCalorie.setColor(getResources().getColor(R.color.blue_b4efff));
		dataDistance.setColor(getResources().getColor(R.color.blue_b4efff));
		event.dataCount = dataCount;
		event.dataCalorie = dataCalorie;
		event.dataDistance = dataDistance;
		event.valuesCount = valueCount;
		event.valuesCalorie = valueCalorie;
		event.valuesDistance = valueDistance;
		event.barCountMax = barCountMax;
		event.barCalorieMax = barCalorieMax;
		event.barDistanceMax = barDistanceMax;
		return event;
	}

	private void getYearData(HistoryChangeUnitClick event) {
		BarSet dataCount = new BarSet();
		BarSet dataCalorie = new BarSet();
		BarSet dataDistance = new BarSet();

		String[] labels = new String[COUNT_NUMBER_YEAR];
		String[] valueCount = new String[COUNT_NUMBER_YEAR];
		String[] valueCalorie = new String[COUNT_NUMBER_YEAR];
		String[] valueDistance = new String[COUNT_NUMBER_YEAR];
		Calendar calendarLabels = (Calendar) mTodayCalendar.clone();
		for (int i = COUNT_NUMBER_YEAR - 1; i >= 0; i--) {
			labels[i] = calendarLabels.get(Calendar.YEAR) + "";
			calendarLabels.add(Calendar.YEAR, -1);
		}
		Calendar calendar = (Calendar) mTodayCalendar.clone();
		// 拿到当天所在年的第一天
		calendar.set(Calendar.DAY_OF_YEAR, 1);
		calendar.add(Calendar.YEAR, 1 - COUNT_NUMBER_YEAR);
		int[] sortDataCount = new int[COUNT_NUMBER_YEAR];
		int[] sortDataCalorie = new int[COUNT_NUMBER_YEAR];
		int[] sortDataDistance = new int[COUNT_NUMBER_YEAR];
		for (int i = 0; i < COUNT_NUMBER_YEAR; i++) {
			int yearCount = 0;
			int yearCalorie = 0;
			int yearDistance = 0;
			// 计算当年有多少天
			int daysInYear = calendar.getActualMaximum(Calendar.DAY_OF_YEAR);
			for (int j = 0; j < daysInYear; j++) {
				if (mStepsMap.get(Utils.calendar2strDate(calendar,
						BTConstants.PATTERN_YYYY_MM_DD)) != null) {
					yearCount += Integer.valueOf(mStepsMap.get(Utils
							.calendar2strDate(calendar,
									BTConstants.PATTERN_YYYY_MM_DD)).count);
					yearCalorie += Integer.valueOf(mStepsMap.get(Utils
							.calendar2strDate(calendar,
									BTConstants.PATTERN_YYYY_MM_DD)).calories);
					yearDistance += Float.valueOf(mStepsMap.get(Utils
							.calendar2strDate(calendar,
									BTConstants.PATTERN_YYYY_MM_DD)).distance);
				}
				calendar.add(Calendar.DAY_OF_MONTH, 1);
			}
			Bar barCount = new Bar(labels[i], yearCount);
			Bar barCalorie = new Bar(labels[i], yearCalorie);
			Bar barDistance = new Bar(labels[i], yearDistance);
			valueCount[i] = yearCount + "";
			valueCalorie[i] = yearCalorie + "";
			valueDistance[i] = yearDistance + "";
			sortDataCount[i] = yearCount;
			sortDataCalorie[i] = yearCalorie;
			sortDataDistance[i] = yearDistance;
			dataCount.addBar(barCount);
			dataCalorie.addBar(barCalorie);
			dataDistance.addBar(barDistance);
		}
		Arrays.sort(sortDataCount);
		Arrays.sort(sortDataCalorie);
		Arrays.sort(sortDataDistance);
		int barCountMax = 100;
		int barCalorieMax = 100;
		int barDistanceMax = 1;
		if (sortDataCount[COUNT_NUMBER_YEAR - 1] >= barCountMax) {
			barCountMax = sortDataCount[COUNT_NUMBER_YEAR - 1];
		}
		if (sortDataCalorie[COUNT_NUMBER_YEAR - 1] >= barCalorieMax) {
			barCalorieMax = sortDataCalorie[COUNT_NUMBER_YEAR - 1];
		}
		if (sortDataDistance[COUNT_NUMBER_YEAR - 1] >= barDistanceMax) {
			barDistanceMax = Float.valueOf(
					sortDataDistance[COUNT_NUMBER_YEAR - 1]).intValue() + 1;
		}
		dataCount.setColor(getResources().getColor(R.color.blue_b4efff));
		dataCalorie.setColor(getResources().getColor(R.color.blue_b4efff));
		dataDistance.setColor(getResources().getColor(R.color.blue_b4efff));
		event.dataCount = dataCount;
		event.dataCalorie = dataCalorie;
		event.dataDistance = dataDistance;
		event.valuesCount = valueCount;
		event.valuesCalorie = valueCalorie;
		event.valuesDistance = valueDistance;
		event.barCountMax = barCountMax;
		event.barCalorieMax = barCalorieMax;
		event.barDistanceMax = barDistanceMax;
	}

	public HistoryChangeUnitClick getMonthData(Calendar calendar,
			HistoryChangeUnitClick event) {
		BarSet dataCount = new BarSet();
		BarSet dataCalorie = new BarSet();
		BarSet dataDistance = new BarSet();

		String[] labels = new String[COUNT_NUMBER_MONTH];
		String[] valueCount = new String[COUNT_NUMBER_MONTH];
		String[] valueCalorie = new String[COUNT_NUMBER_MONTH];
		String[] valueDistance = new String[COUNT_NUMBER_MONTH];
		Calendar calendarLabels;
		if (calendar == null) {
			calendarLabels = (Calendar) mTodayCalendar.clone();
		} else {
			calendarLabels = (Calendar) calendar.clone();
		}
		for (int i = COUNT_NUMBER_MONTH - 1; i >= 0; i--) {
			if (calendarLabels.get(Calendar.YEAR) == mTodayCalendar
					.get(Calendar.YEAR)
					&& calendarLabels.get(Calendar.MONTH) == mTodayCalendar
							.get(Calendar.MONTH)) {
				labels[i] = getString(R.string.history_this_month);
				calendarLabels.add(Calendar.MONTH, -1);
				continue;
			}
			labels[i] = getString(R.string.history_month_number,
					calendarLabels.get(Calendar.MONTH) + 1);
			calendarLabels.add(Calendar.MONTH, -1);
		}
		// 拿到最新的数据开始计算日期
		if (calendar == null) {
			calendar = (Calendar) mTodayCalendar.clone();
		}
		// 拿到当天所在月的第一天
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.add(Calendar.MONTH, 1 - COUNT_NUMBER_MONTH);
		int[] sortDataCount = new int[COUNT_NUMBER_MONTH];
		int[] sortDataCalorie = new int[COUNT_NUMBER_MONTH];
		int[] sortDataDistance = new int[COUNT_NUMBER_MONTH];
		for (int i = 0; i < COUNT_NUMBER_MONTH; i++) {
			int monthCount = 0;
			int monthCalorie = 0;
			int monthDistance = 0;
			// 计算当月有多少天
			int daysInMonth = calendar.getActualMaximum(Calendar.DATE);
			for (int j = 0; j < daysInMonth; j++) {
				if (mStepsMap.get(Utils.calendar2strDate(calendar,
						BTConstants.PATTERN_YYYY_MM_DD)) != null) {
					monthCount += Integer.valueOf(mStepsMap.get(Utils
							.calendar2strDate(calendar,
									BTConstants.PATTERN_YYYY_MM_DD)).count);
					monthCalorie += Integer.valueOf(mStepsMap.get(Utils
							.calendar2strDate(calendar,
									BTConstants.PATTERN_YYYY_MM_DD)).calories);
					monthDistance += Float.valueOf(mStepsMap.get(Utils
							.calendar2strDate(calendar,
									BTConstants.PATTERN_YYYY_MM_DD)).distance);
				}
				calendar.add(Calendar.DAY_OF_MONTH, 1);
			}
			Bar barCount = new Bar(labels[i], monthCount);
			Bar barCalorie = new Bar(labels[i], monthCalorie);
			Bar barDistance = new Bar(labels[i], monthDistance);
			valueCount[i] = monthCount + "";
			valueCalorie[i] = monthCalorie + "";
			valueDistance[i] = monthDistance + "";
			sortDataCount[i] = monthCount;
			sortDataCalorie[i] = monthCalorie;
			sortDataDistance[i] = monthDistance;
			dataCount.addBar(barCount);
			dataCalorie.addBar(barCalorie);
			dataDistance.addBar(barDistance);
		}
		Arrays.sort(sortDataCount);
		Arrays.sort(sortDataCalorie);
		Arrays.sort(sortDataDistance);
		int barCountMax = 100;
		int barCalorieMax = 100;
		int barDistanceMax = 1;
		if (sortDataCount[COUNT_NUMBER_MONTH - 1] >= barCountMax) {
			barCountMax = sortDataCount[COUNT_NUMBER_MONTH - 1];
		}
		if (sortDataCalorie[COUNT_NUMBER_MONTH - 1] >= barCalorieMax) {
			barCalorieMax = sortDataCalorie[COUNT_NUMBER_MONTH - 1];
		}
		if (sortDataDistance[COUNT_NUMBER_MONTH - 1] >= barDistanceMax) {
			barDistanceMax = Float.valueOf(
					sortDataDistance[COUNT_NUMBER_MONTH - 1]).intValue() + 1;
		}
		dataCount.setColor(getResources().getColor(R.color.blue_b4efff));
		dataCalorie.setColor(getResources().getColor(R.color.blue_b4efff));
		dataDistance.setColor(getResources().getColor(R.color.blue_b4efff));
		event.dataCount = dataCount;
		event.dataCalorie = dataCalorie;
		event.dataDistance = dataDistance;
		event.valuesCount = valueCount;
		event.valuesCalorie = valueCalorie;
		event.valuesDistance = valueDistance;
		event.barCountMax = barCountMax;
		event.barCalorieMax = barCalorieMax;
		event.barDistanceMax = barDistanceMax;
		return event;
	}

	public HistoryChangeUnitClick getWeekData(Calendar calendar,
			HistoryChangeUnitClick event) {
		BarSet dataCount = new BarSet();
		BarSet dataCalorie = new BarSet();
		BarSet dataDistance = new BarSet();

		String[] labels = new String[COUNT_NUMBER_WEEK];
		String[] valueCount = new String[COUNT_NUMBER_WEEK];
		String[] valueCalorie = new String[COUNT_NUMBER_WEEK];
		String[] valueDistance = new String[COUNT_NUMBER_WEEK];
		Calendar calendarLabels;
		Calendar weekDayToday = (Calendar) mTodayCalendar.clone();
		boolean isSunday = false;
		if (weekDayToday.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
			isSunday = true;
		}
		if (calendar == null) {
			calendarLabels = (Calendar) mTodayCalendar.clone();
		} else {
			calendarLabels = (Calendar) calendar.clone();
		}
		// calendarLabels.setFirstDayOfWeek(Calendar.MONDAY);
		// calendarLabels.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		// weekDayToday.setFirstDayOfWeek(Calendar.MONDAY);
		// weekDayToday.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		for (int i = COUNT_NUMBER_WEEK - 1; i >= 0; i--) {
			if (calendarLabels.get(Calendar.YEAR) == weekDayToday
					.get(Calendar.YEAR)
					&& calendarLabels.get(Calendar.WEEK_OF_YEAR) == weekDayToday
							.get(Calendar.WEEK_OF_YEAR)) {
				labels[i] = getString(R.string.history_this_week);
				calendarLabels.add(Calendar.WEEK_OF_YEAR, -1);
				continue;
			}
			if (isSunday) {
				labels[i] = getString(R.string.history_week_number,
						calendarLabels.get(Calendar.WEEK_OF_YEAR) - 1);
			} else {
				labels[i] = getString(R.string.history_week_number,
						calendarLabels.get(Calendar.WEEK_OF_YEAR));
			}
			calendarLabels.add(Calendar.WEEK_OF_YEAR, -1);
		}
		// 拿到最新的数据开始计算日期
		if (calendar == null) {
			calendar = (Calendar) weekDayToday.clone();
		}
		// 拿到当天所在周的周一
		if (isSunday) {
			calendar.add(Calendar.DAY_OF_MONTH, 1);
			calendar.setFirstDayOfWeek(Calendar.MONDAY);
			calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			calendar.add(Calendar.WEEK_OF_YEAR, -COUNT_NUMBER_WEEK);
		} else {
			calendar.setFirstDayOfWeek(Calendar.MONDAY);
			calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			calendar.add(Calendar.WEEK_OF_YEAR, 1 - COUNT_NUMBER_WEEK);
		}
		int[] sortDataCount = new int[COUNT_NUMBER_WEEK];
		int[] sortDataCalorie = new int[COUNT_NUMBER_WEEK];
		int[] sortDataDistance = new int[COUNT_NUMBER_WEEK];
		for (int i = 0; i < COUNT_NUMBER_WEEK; i++) {
			int weekCount = 0;
			int weekCalorie = 0;
			int weekDistance = 0;
			// 计算当月有多少天
			for (int j = 0; j < COUNT_NUMBER_WEEK; j++) {
				if (mStepsMap.get(Utils.calendar2strDate(calendar,
						BTConstants.PATTERN_YYYY_MM_DD)) != null) {
					weekCount += Integer.valueOf(mStepsMap.get(Utils
							.calendar2strDate(calendar,
									BTConstants.PATTERN_YYYY_MM_DD)).count);
					weekCalorie += Integer.valueOf(mStepsMap.get(Utils
							.calendar2strDate(calendar,
									BTConstants.PATTERN_YYYY_MM_DD)).calories);
					weekDistance += Float.valueOf(mStepsMap.get(Utils
							.calendar2strDate(calendar,
									BTConstants.PATTERN_YYYY_MM_DD)).distance);
				}
				calendar.add(Calendar.DAY_OF_MONTH, 1);
			}
			Bar barCount = new Bar(labels[i], weekCount);
			Bar barCalorie = new Bar(labels[i], weekCalorie);
			Bar barDistance = new Bar(labels[i], weekDistance);
			valueCount[i] = weekCount + "";
			valueCalorie[i] = weekCalorie + "";
			valueDistance[i] = weekDistance + "";
			sortDataCount[i] = weekCount;
			sortDataCalorie[i] = weekCalorie;
			sortDataDistance[i] = weekDistance;
			dataCount.addBar(barCount);
			dataCalorie.addBar(barCalorie);
			dataDistance.addBar(barDistance);
		}
		Arrays.sort(sortDataCount);
		Arrays.sort(sortDataCalorie);
		Arrays.sort(sortDataDistance);
		int barCountMax = 100;
		int barCalorieMax = 100;
		int barDistanceMax = 1;
		if (sortDataCount[COUNT_NUMBER_WEEK - 1] >= barCountMax) {
			barCountMax = sortDataCount[COUNT_NUMBER_WEEK - 1];
		}
		if (sortDataCalorie[COUNT_NUMBER_WEEK - 1] >= barCalorieMax) {
			barCalorieMax = sortDataCalorie[COUNT_NUMBER_WEEK - 1];
		}
		if (sortDataDistance[COUNT_NUMBER_WEEK - 1] >= barDistanceMax) {
			barDistanceMax = Float.valueOf(
					sortDataDistance[COUNT_NUMBER_WEEK - 1]).intValue() + 1;
		}
		dataCount.setColor(getResources().getColor(R.color.blue_b4efff));
		dataCalorie.setColor(getResources().getColor(R.color.blue_b4efff));
		dataDistance.setColor(getResources().getColor(R.color.blue_b4efff));
		event.dataCount = dataCount;
		event.dataCalorie = dataCalorie;
		event.dataDistance = dataDistance;
		event.valuesCount = valueCount;
		event.valuesCalorie = valueCalorie;
		event.valuesDistance = valueDistance;
		event.barCountMax = barCountMax;
		event.barCalorieMax = barCalorieMax;
		event.barDistanceMax = barDistanceMax;
		return event;
	}
}
