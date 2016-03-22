package com.blestep.sportsbracelet.fragment;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.activity.HistoryActivity;
import com.blestep.sportsbracelet.entity.Step;
import com.blestep.sportsbracelet.event.HistoryChangeUnitClick;
import com.blestep.sportsbracelet.module.LogModule;
import com.blestep.sportsbracelet.utils.DataRetriever;
import com.blestep.sportsbracelet.utils.Utils;
import com.db.chart.Tools;
import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.Bar;
import com.db.chart.model.BarSet;
import com.db.chart.view.BarChartView;
import com.db.chart.view.XController;
import com.db.chart.view.YController;

import de.greenrobot.event.EventBus;

public class HistoryStepDistance extends Fragment implements
		OnEntryClickListener, OnCheckedChangeListener {
	private static final String TAG = HistoryStepDistance.class.getSimpleName();
	private String mLabels[];
	private String mValues[];
	private SimpleDateFormat mSdf;
	private Calendar mCalendar;
	private TextView history_distance_daily, tv_history_distance_daily,
			tv_history_distance_sum;
	private View mView;
	private HistoryActivity mActivity;
	private BarChartView bcv_distance;
	private TextView mBarTooltip;
	private RelativeLayout rl_pre_and_next;
	private RadioGroup rg_history_bottom_tab_parent;
	private boolean mIsPreDayShow;
	private boolean mIsPreWeekShow;
	private boolean mIsPreYearShow;
	private boolean mIsNextDayShow;
	private boolean mIsNextWeekShow;
	private boolean mIsNextYearShow;

	public Calendar mLastDayCalendar;// 上一周的最后一天
	public Calendar mLastWeekCalendar;// 7周前的周一
	public Calendar mLastMonthCalendar;// 一年前的今天
	// 手势
	private GestureDetectorCompat mDetector;
	/**
	 * 绘图完成后的操作
	 */
	private Runnable mEndAction = new Runnable() {
		@Override
		public void run() {
			mHandler.sendEmptyMessage(0);
		}
	};
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				// 将按钮都释放开
				mView.findViewById(R.id.rb_history_unit_day).setEnabled(true);
				mView.findViewById(R.id.rb_history_unit_week).setEnabled(true);
				mView.findViewById(R.id.rb_history_unit_month).setEnabled(true);
				mView.findViewById(R.id.rb_history_unit_year).setEnabled(true);
				if (mIsPreDayShow || mIsPreWeekShow || mIsPreYearShow) {
					mView.findViewById(R.id.btn_pre).setEnabled(true);
				} else {
					mView.findViewById(R.id.btn_pre).setEnabled(false);
				}
				if (mIsNextDayShow || mIsNextWeekShow || mIsNextYearShow) {
					mView.findViewById(R.id.btn_next).setEnabled(true);
				} else {
					mView.findViewById(R.id.btn_next).setEnabled(false);
				}
				break;

			default:
				break;
			}
		};
	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		LogModule.i(TAG + "onActivityCreated");
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onResume() {
		LogModule.i(TAG + "onResume");
		super.onResume();
	}

	@Override
	public void onPause() {
		LogModule.i(TAG + "onPause");
		super.onPause();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LogModule.i(TAG + "onCreateView");
		EventBus.getDefault().register(this);
		mActivity = (HistoryActivity) getActivity();
		LogModule.i(TAG + "onCreateView-->" + mActivity.selectHistoryUnit);
		mView = inflater.inflate(R.layout.history_step_distance, container,
				false);
		initView();
		mIsPreDayShow = false;
		mIsNextDayShow = false;
		mIsPreWeekShow = false;
		mIsNextWeekShow = false;
		mIsPreYearShow = false;
		mIsNextYearShow = false;

		initData(HistoryActivity.COUNT_NUMBER_DAY, mActivity.selectHistoryUnit,
				mLastDayCalendar, null);
		rg_history_bottom_tab_parent.check(R.id.rb_history_unit_day);

		mDetector = new GestureDetectorCompat(mActivity,
				new GestureDetector.SimpleOnGestureListener() {
					@Override
					public boolean onFling(MotionEvent e1, MotionEvent e2,
							float velocityX, float velocityY) {
						if (Math.abs(e1.getX() - e2.getX()) > 100
								&& velocityX > 200) {
							// 前
							onFlingPre();

						}
						if (Math.abs(e1.getX() - e2.getX()) > 100
								&& velocityX < -200) {
							// 后
							onFlingNext();
						}
						return true;
					}
				});
		bcv_distance.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mDetector.onTouchEvent(event);
				return false;
			}
		});
		rg_history_bottom_tab_parent.setOnCheckedChangeListener(this);
		return mView;
	}

	@Override
	public void onDestroyView() {
		LogModule.i(TAG + "onDestroyView");
		EventBus.getDefault().unregister(this);
		super.onDestroyView();
	}

	public void onEvent(HistoryChangeUnitClick event) {
		LogModule.i(TAG + "onEvent-->" + event.selectHistoryUnit);
		mIsPreDayShow = false;
		mIsNextDayShow = false;
		mIsPreWeekShow = false;
		mIsNextWeekShow = false;
		mIsPreYearShow = false;
		mIsNextYearShow = false;
		rg_history_bottom_tab_parent.setOnCheckedChangeListener(null);
		switch (event.selectHistoryUnit) {
		case HistoryActivity.DATA_UNIT_DAY:
			initData(HistoryActivity.COUNT_NUMBER_DAY, event.selectHistoryUnit,
					null, null);
			rg_history_bottom_tab_parent.check(R.id.rb_history_unit_day);
			break;
		case HistoryActivity.DATA_UNIT_WEEK:
			initData(HistoryActivity.COUNT_NUMBER_WEEK,
					event.selectHistoryUnit, null, null);
			rg_history_bottom_tab_parent.check(R.id.rb_history_unit_week);
			break;
		case HistoryActivity.DATA_UNIT_MONTH:
			initData(HistoryActivity.COUNT_NUMBER_MONTH,
					event.selectHistoryUnit, null, null);
			rg_history_bottom_tab_parent.check(R.id.rb_history_unit_month);
			break;
		case HistoryActivity.DATA_UNIT_YEAR:
			initData(HistoryActivity.COUNT_NUMBER_YEAR,
					event.selectHistoryUnit, null, event);
			rg_history_bottom_tab_parent.check(R.id.rb_history_unit_year);
			break;
		}
		rg_history_bottom_tab_parent.setOnCheckedChangeListener(this);
	}

	private void initData(int labelsCount, int unit, Calendar calendar,
			HistoryChangeUnitClick event) {
		mLabels = new String[labelsCount];
		mValues = new String[labelsCount];
		mSdf = new SimpleDateFormat(BTConstants.PATTERN_MM_DD);
		if (calendar == null) {
			mCalendar = (Calendar) mActivity.mTodayCalendar.clone();
		} else {
			mCalendar = (Calendar) calendar.clone();
		}
		// 日
		if (unit == HistoryActivity.DATA_UNIT_DAY) {
			for (int i = labelsCount - 1; i >= 0; i--) {
				if (mCalendar.getTime().compareTo(
						mActivity.mTodayCalendar.getTime()) >= 0) {
					mLabels[i] = getString(R.string.history_today);
					mCalendar.add(Calendar.DAY_OF_MONTH, -1);
					continue;
				}
				mLabels[i] = mSdf.format(mCalendar.getTime());
				mCalendar.add(Calendar.DAY_OF_MONTH, -1);
			}
			updateBarChartByDay(labelsCount, calendar == null ? null
					: (Calendar) calendar.clone());
		}
		// 周
		if (unit == HistoryActivity.DATA_UNIT_WEEK) {
			Calendar monday = (Calendar) mCalendar.clone();
			if (monday.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
				monday.add(Calendar.DAY_OF_MONTH, -1);
			}
			monday.setFirstDayOfWeek(Calendar.MONDAY);
			monday.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			for (int i = labelsCount - 1; i >= 0; i--) {
				if (monday.get(Calendar.WEEK_OF_YEAR) == mActivity.mTodayCalendar
						.get(Calendar.WEEK_OF_YEAR)) {
					mLabels[i] = getString(R.string.history_this_week);
					monday.add(Calendar.WEEK_OF_MONTH, -1);
					continue;
				}
				mLabels[i] = getString(R.string.history_week_number,
						monday.get(Calendar.WEEK_OF_YEAR));
				monday.add(Calendar.WEEK_OF_MONTH, -1);
			}
			updateBarChartByWeek(labelsCount, calendar == null ? null
					: (Calendar) calendar.clone());
		}
		// 月
		if (unit == HistoryActivity.DATA_UNIT_MONTH) {
			Calendar firstDay = (Calendar) mCalendar.clone();
			firstDay.set(Calendar.DAY_OF_MONTH, 1);
			for (int i = labelsCount - 1; i >= 0; i--) {
				if (firstDay.get(Calendar.MONTH) == mActivity.mTodayCalendar
						.get(Calendar.MONTH)) {
					mLabels[i] = getString(R.string.history_this_month);
					firstDay.add(Calendar.MONTH, -1);
					continue;
				}
				mLabels[i] = getString(R.string.history_month_number,
						firstDay.get(Calendar.MONTH) + 1);
				firstDay.add(Calendar.MONTH, -1);
			}
			updateBarChartByMonth(labelsCount, calendar == null ? null
					: (Calendar) calendar.clone());
		}
		// 年
		if (unit == HistoryActivity.DATA_UNIT_YEAR) {
			mValues = event.valuesDistance;
			updateBarChartByYear(event);
		}
		float stepSum = 0;
		for (int i = 0; i < mValues.length; i++) {
			if (Utils.isEmpty(mValues[i])) {
				stepSum += 0;
			} else {
				stepSum += Float.valueOf(mValues[i]);
			}
		}
		tv_history_distance_sum.setText(new BigDecimal(stepSum).setScale(2,
				BigDecimal.ROUND_HALF_UP).floatValue()
				+ "");
		tv_history_distance_daily.setText(new BigDecimal(stepSum
				/ mValues.length).setScale(2, BigDecimal.ROUND_HALF_UP)
				.floatValue() + "");
		if (unit == HistoryActivity.DATA_UNIT_DAY) {
			history_distance_daily
					.setText(getString(R.string.history_distance_daily));
		}
		if (unit == HistoryActivity.DATA_UNIT_WEEK) {
			history_distance_daily
					.setText(getString(R.string.history_distance_week));
		}
		if (unit == HistoryActivity.DATA_UNIT_MONTH) {
			history_distance_daily
					.setText(getString(R.string.history_distance_month));
		}
		if (unit == HistoryActivity.DATA_UNIT_YEAR) {
			history_distance_daily
					.setText(getString(R.string.history_distance_year));
		}
	}

	/**
	 * 判断是否可点击前一天
	 * 
	 * @param labelsCount
	 */
	private void isPreDayEnable(Calendar calendar) {
		// 前一天是否可点击
		mLastDayCalendar = (Calendar) calendar.clone();
		mLastDayCalendar.add(Calendar.DAY_OF_MONTH,
				-HistoryActivity.COUNT_NUMBER_DAY);
		if (mActivity.mStepsMap.get(Utils.calendar2strDate(mLastDayCalendar,
				BTConstants.PATTERN_YYYY_MM_DD)) != null
				&& mLastDayCalendar.getTime().compareTo(
						mActivity.m7YearAgoCalendar.getTime()) >= 0) {
			mIsPreDayShow = true;
		} else {
			mIsPreDayShow = false;
		}
	}

	/**
	 * 判断是否可点击后一天
	 * 
	 * @param calendar
	 * 
	 * @param labelsCount
	 */
	private void isNextDayEnable(Calendar calendar) {
		mLastDayCalendar.add(Calendar.DAY_OF_MONTH,
				HistoryActivity.COUNT_NUMBER_DAY);
		if (mLastDayCalendar.getTime().compareTo(
				mActivity.mTodayCalendar.getTime()) >= 0) {
			mIsNextDayShow = false;
		} else {
			mIsNextDayShow = true;
		}
	}

	/**
	 * 计算以日为单位的运动量
	 * 
	 * @param labelsCount
	 */
	private void updateBarChartByDay(int labelsCount, Calendar calendar) {
		// 构建柱状图
		bcv_distance.reset();
		BarSet data = new BarSet();
		rl_pre_and_next.setVisibility(View.VISIBLE);
		if (calendar == null) {
			calendar = (Calendar) mActivity.mTodayCalendar.clone();
		}
		calendar.add(Calendar.DAY_OF_MONTH, 1 - labelsCount);
		ArrayList<Step> stepsSort = new ArrayList<Step>();

		for (int i = 0; i < labelsCount; i++) {
			float dayStep = 0;
			if (mActivity.mStepsMap.get(Utils.calendar2strDate(calendar,
					BTConstants.PATTERN_YYYY_MM_DD)) != null) {
				dayStep = Float.valueOf(mActivity.mStepsMap.get(Utils
						.calendar2strDate(calendar,
								BTConstants.PATTERN_YYYY_MM_DD)).distance);
				stepsSort.add(mActivity.mStepsMap.get(Utils.calendar2strDate(
						calendar, BTConstants.PATTERN_YYYY_MM_DD)));
			} else {
				stepsSort.add(new Step("0", "0", "0"));
			}
			mValues[i] = dayStep + "";
			Bar bar = new Bar(mLabels[i], dayStep);
			data.addBar(bar);
			calendar.add(Calendar.DAY_OF_MONTH, 1);
		}
		// 找到最大的，与目标值对比
		int barStepMax = 1;
		Collections.sort(stepsSort, new StepCompare());
		if (Float.valueOf(stepsSort.get(0).distance) >= barStepMax) {
			barStepMax = Float.valueOf(stepsSort.get(0).distance).intValue() + 1;
		}
		data.setColor(getResources().getColor(R.color.blue_b4efff));
		bcv_distance.addData(data);
		bcv_distance.setBarSpacing((int) Tools.fromDpToPx(50));
		bcv_distance.setSetSpacing(0);
		bcv_distance.setBarBackground(false);
		bcv_distance.setRoundCorners(0);
		bcv_distance.setBorderSpacing(0).setGrid(null).setHorizontalGrid(null)
				.setVerticalGrid(null)
				.setYLabels(YController.LabelPosition.NONE).setYAxis(false)
				.setXLabels(XController.LabelPosition.OUTSIDE).setXAxis(true)
				.setMaxAxisValue(barStepMax, 1)
				.animate(DataRetriever.randAnimation(mEndAction, labelsCount));
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		isPreDayEnable(calendar);
		isNextDayEnable(calendar);
	}

	/**
	 * 是否可点击后一周
	 * 
	 * @param calendar
	 */
	private void isNextWeekEnable(Calendar calendar) {
		mLastWeekCalendar.setFirstDayOfWeek(Calendar.MONDAY);
		mLastWeekCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		mLastWeekCalendar.add(Calendar.WEEK_OF_MONTH,
				HistoryActivity.COUNT_NUMBER_WEEK);
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		if (mLastWeekCalendar.getTime().compareTo(calendar.getTime()) >= 0) {
			mIsNextDayShow = false;
		} else {
			mIsNextDayShow = true;
		}
	}

	/**
	 * 是否可点击前一周
	 * 
	 * @param calendar
	 */
	private void isPreWeekEnable(Calendar calendar) {
		// 前一周是否可点击
		mLastWeekCalendar = (Calendar) calendar.clone();
		// 拿到当天所在周的周一
		mLastWeekCalendar.setFirstDayOfWeek(Calendar.MONDAY);
		mLastWeekCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		mLastWeekCalendar.add(Calendar.WEEK_OF_MONTH,
				-HistoryActivity.COUNT_NUMBER_WEEK);
		int weekCount = 0;
		// 一周7天
		for (int i = 0; i < 7; i++) {
			if (mActivity.mStepsMap.get(Utils.calendar2strDate(
					mLastWeekCalendar, BTConstants.PATTERN_YYYY_MM_DD)) != null) {
				weekCount += Float.valueOf(mActivity.mStepsMap.get(Utils
						.calendar2strDate(mLastWeekCalendar,
								BTConstants.PATTERN_YYYY_MM_DD)).distance);
			}
			mLastWeekCalendar.add(Calendar.DAY_OF_MONTH, 1);
		}
		mLastWeekCalendar.add(Calendar.WEEK_OF_MONTH, -1);
		if (weekCount > 0
				&& mLastWeekCalendar.getTime().compareTo(
						mActivity.m7YearAgoCalendar.getTime()) >= 0) {
			mIsPreWeekShow = true;
		} else {
			mIsPreWeekShow = false;
		}
	}

	/**
	 * 计算以周为单位的运动量
	 * 
	 * @param labelsCount
	 * @param calendar
	 */
	private void updateBarChartByWeek(int labelsCount, Calendar calendar) {
		bcv_distance.reset();
		BarSet data = new BarSet();
		rl_pre_and_next.setVisibility(View.VISIBLE);
		// 拿到最新的数据开始计算日期
		if (calendar == null) {
			calendar = (Calendar) mActivity.mTodayCalendar.clone();
		}
		if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
			calendar.add(Calendar.DAY_OF_MONTH, -1);
		}
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		// 拿到当天所在周的周一
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		calendar.add(Calendar.WEEK_OF_MONTH, 1 - labelsCount);
		float[] sortData = new float[labelsCount];
		for (int i = 0; i < labelsCount; i++) {
			float weekCount = 0;
			// 一周7天
			for (int j = 0; j < 7; j++) {
				if (mActivity.mStepsMap.get(Utils.calendar2strDate(calendar,
						BTConstants.PATTERN_YYYY_MM_DD)) != null) {
					weekCount += Float.valueOf(mActivity.mStepsMap.get(Utils
							.calendar2strDate(calendar,
									BTConstants.PATTERN_YYYY_MM_DD)).distance);
				}
				calendar.add(Calendar.DAY_OF_MONTH, 1);
			}
			Bar bar = new Bar(mLabels[i], weekCount);
			mValues[i] = weekCount + "";
			sortData[i] = weekCount;
			data.addBar(bar);
		}
		Arrays.sort(sortData);
		// 找到最大的，与目标值对比
		int barStepMax = 1;
		if (sortData[labelsCount - 1] >= barStepMax) {
			barStepMax = Float.valueOf(sortData[labelsCount - 1]).intValue() + 1;
		}
		data.setColor(getResources().getColor(R.color.blue_b4efff));
		bcv_distance.addData(data);

		bcv_distance.setBarSpacing((int) Tools.fromDpToPx(50));
		bcv_distance.setSetSpacing(0);
		bcv_distance.setBarBackground(false);
		bcv_distance.setRoundCorners(0);
		bcv_distance.setBorderSpacing(0).setGrid(null).setHorizontalGrid(null)
				.setVerticalGrid(null)
				.setYLabels(YController.LabelPosition.NONE).setYAxis(false)
				.setXLabels(XController.LabelPosition.OUTSIDE).setXAxis(true)
				.setMaxAxisValue(barStepMax, 1)
				.animate(DataRetriever.randAnimation(mEndAction, labelsCount));
		calendar.add(Calendar.WEEK_OF_MONTH, -1);
		isPreWeekEnable(calendar);
		isNextWeekEnable(calendar);
	}

	/**
	 * 是否可点击后一年
	 * 
	 * @param calendar
	 */
	private void isNextMonthEnable(Calendar calendar) {
		mLastMonthCalendar.set(Calendar.DAY_OF_MONTH, 1);
		mLastMonthCalendar.add(Calendar.MONTH,
				HistoryActivity.COUNT_NUMBER_MONTH);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		if (mLastMonthCalendar.getTime().compareTo(calendar.getTime()) >= 0) {
			mIsNextYearShow = false;
		} else {
			mIsNextYearShow = true;
		}
	}

	/**
	 * 是否可点击前一年
	 * 
	 * @param calendar
	 */
	private void isPreMonthEnable(Calendar calendar) {
		// 前一周是否可点击
		mLastMonthCalendar = (Calendar) calendar.clone();
		// 拿到当天所在周的周一
		mLastMonthCalendar.set(Calendar.DAY_OF_MONTH, 1);
		mLastMonthCalendar.add(Calendar.MONTH,
				-HistoryActivity.COUNT_NUMBER_MONTH);
		int monthCount = 0;
		// 计算当月有多少天
		int daysInMonth = calendar.getActualMaximum(Calendar.DATE);
		for (int i = 0; i < daysInMonth; i++) {
			if (mActivity.mStepsMap.get(Utils.calendar2strDate(
					mLastMonthCalendar, BTConstants.PATTERN_YYYY_MM_DD)) != null) {
				monthCount += Float.valueOf(mActivity.mStepsMap.get(Utils
						.calendar2strDate(mLastMonthCalendar,
								BTConstants.PATTERN_YYYY_MM_DD)).distance);
			}
			mLastMonthCalendar.add(Calendar.DAY_OF_MONTH, 1);
		}
		mLastMonthCalendar.add(Calendar.MONTH, -1);
		if (monthCount > 0
				&& mLastMonthCalendar.getTime().compareTo(
						mActivity.m7YearAgoCalendar.getTime()) >= 0) {
			mIsPreYearShow = true;
		} else {
			mIsPreYearShow = false;
		}

	}

	/**
	 * 计算以月为单位的运动量
	 * 
	 * @param labelsCount
	 * @param calendar
	 */
	private void updateBarChartByMonth(int labelsCount, Calendar calendar) {
		bcv_distance.reset();
		BarSet data = new BarSet();
		rl_pre_and_next.setVisibility(View.VISIBLE);
		// 拿到最新的数据开始计算日期
		if (calendar == null) {
			calendar = (Calendar) mActivity.mTodayCalendar.clone();
		}
		// 拿到当天所在月的第一天
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.add(Calendar.MONTH, 1 - labelsCount);
		float[] sortData = new float[labelsCount];
		for (int i = 0; i < labelsCount; i++) {
			int monthCount = 0;
			// 计算当月有多少天
			int daysInMonth = calendar.getActualMaximum(Calendar.DATE);
			for (int j = 0; j < daysInMonth; j++) {
				if (mActivity.mStepsMap.get(Utils.calendar2strDate(calendar,
						BTConstants.PATTERN_YYYY_MM_DD)) != null) {
					monthCount += Float.valueOf(mActivity.mStepsMap.get(Utils
							.calendar2strDate(calendar,
									BTConstants.PATTERN_YYYY_MM_DD)).distance);
				}
				calendar.add(Calendar.DAY_OF_MONTH, 1);
			}
			Bar bar = new Bar(mLabels[i], monthCount);
			mValues[i] = monthCount + "";
			sortData[i] = monthCount;
			data.addBar(bar);
		}
		Arrays.sort(sortData);
		// 找到最大的，与目标值对比
		int barStepMax = 1;
		if (sortData[labelsCount - 1] >= barStepMax) {
			barStepMax = Float.valueOf(sortData[labelsCount - 1]).intValue() + 1;
		}
		data.setColor(getResources().getColor(R.color.blue_b4efff));
		bcv_distance.addData(data);

		bcv_distance.setBarSpacing((int) Tools.fromDpToPx(20));
		bcv_distance.setSetSpacing(0);
		bcv_distance.setBarBackground(false);
		bcv_distance.setRoundCorners(0);
		bcv_distance.setBorderSpacing(0).setGrid(null).setHorizontalGrid(null)
				.setVerticalGrid(null)
				.setYLabels(YController.LabelPosition.NONE).setYAxis(false)
				.setXLabels(XController.LabelPosition.OUTSIDE).setXAxis(true)
				.setMaxAxisValue(barStepMax, 1)
				.animate(DataRetriever.randAnimation(mEndAction, labelsCount));
		calendar.add(Calendar.MONTH, -1);
		isPreMonthEnable(calendar);
		isNextMonthEnable(calendar);
	}

	private void updateBarChartByYear(HistoryChangeUnitClick event) {
		bcv_distance.reset();
		bcv_distance.addData(event.dataDistance);
		bcv_distance.setBarSpacing((int) Tools.fromDpToPx(50));
		bcv_distance.setSetSpacing(0);
		bcv_distance.setBarBackground(false);
		bcv_distance.setRoundCorners(0);
		bcv_distance
				.setBorderSpacing(0)
				.setGrid(null)
				.setHorizontalGrid(null)
				.setVerticalGrid(null)
				.setYLabels(YController.LabelPosition.NONE)
				.setYAxis(false)
				.setXLabels(XController.LabelPosition.OUTSIDE)
				.setXAxis(true)
				.setMaxAxisValue(event.barDistanceMax, 1)
				.animate(
						DataRetriever.randAnimation(mEndAction,
								mActivity.COUNT_NUMBER_YEAR));
	}

	private void initView() {
		bcv_distance = (BarChartView) mView.findViewById(R.id.bcv_distance);
		history_distance_daily = (TextView) mView
				.findViewById(R.id.history_distance_daily);
		tv_history_distance_daily = (TextView) mView
				.findViewById(R.id.tv_history_distance_daily);
		tv_history_distance_sum = (TextView) mView
				.findViewById(R.id.tv_history_distance_sum);
		bcv_distance.setOnEntryClickListener(this);
		rg_history_bottom_tab_parent = (RadioGroup) mView
				.findViewById(R.id.rg_history_bottom_tab_parent);
		rl_pre_and_next = (RelativeLayout) mView
				.findViewById(R.id.rl_pre_and_next);
		mView.findViewById(R.id.btn_pre).setEnabled(false);
		mView.findViewById(R.id.btn_next).setEnabled(false);
	}

	@Override
	public void onClick(int setIndex, int entryIndex, Rect rect) {
		if (mBarTooltip == null)
			showBarTooltip(entryIndex, rect);
		else
			dismissBarTooltip(entryIndex, rect);

	}

	private void showBarTooltip(int index, Rect rect) {

		mBarTooltip = (TextView) mActivity.getLayoutInflater().inflate(
				R.layout.tooltip, null);
		mBarTooltip.setText("" + mValues[index]);

		LayoutParams layoutParams = new LayoutParams(rect.width()
				+ (int) Tools.fromDpToPx(40), LayoutParams.WRAP_CONTENT);
		layoutParams.leftMargin = rect.left - (int) Tools.fromDpToPx(20);
		layoutParams.topMargin = rect.top - (int) Tools.fromDpToPx(20);
		mBarTooltip.setLayoutParams(layoutParams);

		bcv_distance.showTooltip(mBarTooltip);
		bcv_distance.invalidate();
	}

	private void dismissBarTooltip(final int index, final Rect rect) {

		bcv_distance.dismissTooltip(mBarTooltip);

		mBarTooltip = null;
		if (index != -1)
			showBarTooltip(index, rect);
	}

	private class StepCompare implements Comparator<Step> {

		@Override
		public int compare(Step lhs, Step rhs) {
			if (Float.valueOf(lhs.distance) > Float.valueOf(rhs.distance)) {
				return -1;
			} else if (Float.valueOf(lhs.distance) < Float
					.valueOf(rhs.distance)) {
				return 1;
			}
			return 0;
		}

	}

	private void onFlingPre() {
		// 前一天/周/年
		if (mIsPreDayShow
				&& mActivity.selectHistoryUnit == HistoryActivity.DATA_UNIT_DAY) {
			mLastDayCalendar.add(Calendar.DAY_OF_MONTH,
					-HistoryActivity.COUNT_NUMBER_DAY);
			initData(HistoryActivity.COUNT_NUMBER_DAY,
					mActivity.selectHistoryUnit, mLastDayCalendar, null);
		}
		if (mIsPreWeekShow
				&& mActivity.selectHistoryUnit == HistoryActivity.DATA_UNIT_WEEK) {
			mLastWeekCalendar.add(Calendar.WEEK_OF_MONTH,
					-HistoryActivity.COUNT_NUMBER_WEEK);
			initData(HistoryActivity.COUNT_NUMBER_WEEK,
					mActivity.selectHistoryUnit, mLastWeekCalendar, null);
		}
		if (mIsPreYearShow
				&& mActivity.selectHistoryUnit == HistoryActivity.DATA_UNIT_MONTH) {
			mLastWeekCalendar.add(Calendar.MONTH,
					-HistoryActivity.COUNT_NUMBER_MONTH);
			initData(HistoryActivity.COUNT_NUMBER_MONTH,
					mActivity.selectHistoryUnit, mLastMonthCalendar, null);
		}
	}

	private void onFlingNext() {
		// 后一天/周/年
		if (mIsNextDayShow
				&& mActivity.selectHistoryUnit == HistoryActivity.DATA_UNIT_DAY) {
			mLastDayCalendar.add(Calendar.DAY_OF_MONTH,
					HistoryActivity.COUNT_NUMBER_DAY);
			initData(HistoryActivity.COUNT_NUMBER_DAY,
					mActivity.selectHistoryUnit, mLastDayCalendar, null);
		}
		if (mIsNextWeekShow
				&& mActivity.selectHistoryUnit == HistoryActivity.DATA_UNIT_WEEK) {
			mLastWeekCalendar.add(Calendar.WEEK_OF_MONTH,
					HistoryActivity.COUNT_NUMBER_WEEK);
			initData(HistoryActivity.COUNT_NUMBER_WEEK,
					mActivity.selectHistoryUnit, mLastWeekCalendar, null);
		}
		if (mIsNextYearShow
				&& mActivity.selectHistoryUnit == HistoryActivity.DATA_UNIT_MONTH) {
			mLastWeekCalendar.add(Calendar.MONTH,
					HistoryActivity.COUNT_NUMBER_MONTH);
			initData(HistoryActivity.COUNT_NUMBER_MONTH,
					mActivity.selectHistoryUnit, mLastMonthCalendar, null);
		}

	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		mView.findViewById(R.id.rb_history_unit_day).setEnabled(false);
		mView.findViewById(R.id.rb_history_unit_week).setEnabled(false);
		mView.findViewById(R.id.rb_history_unit_month).setEnabled(false);
		mView.findViewById(R.id.rb_history_unit_year).setEnabled(false);
		switch (checkedId) {
		case R.id.rb_history_unit_day:
			EventBus.getDefault().postSticky(
					new HistoryChangeUnitClick(HistoryActivity.DATA_UNIT_DAY));
			break;
		case R.id.rb_history_unit_week:
			EventBus.getDefault().postSticky(
					new HistoryChangeUnitClick(HistoryActivity.DATA_UNIT_WEEK));
			break;
		case R.id.rb_history_unit_month:
			EventBus.getDefault()
					.postSticky(
							new HistoryChangeUnitClick(
									HistoryActivity.DATA_UNIT_MONTH));
			break;
		case R.id.rb_history_unit_year:
			EventBus.getDefault().postSticky(
					new HistoryChangeUnitClick(HistoryActivity.DATA_UNIT_YEAR));
			break;
		}
	}
}
