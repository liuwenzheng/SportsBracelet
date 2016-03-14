package com.blestep.sportsbracelet.fragment;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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
import com.blestep.sportsbracelet.utils.SPUtiles;
import com.blestep.sportsbracelet.utils.Utils;
import com.db.chart.Tools;
import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.Bar;
import com.db.chart.model.BarSet;
import com.db.chart.view.BarChartView;
import com.db.chart.view.XController;
import com.db.chart.view.YController;

import de.greenrobot.event.EventBus;

public class HistoryStepCount extends Fragment implements OnEntryClickListener,
		OnClickListener {
	private static final String TAG = HistoryStepCount.class.getSimpleName();
	private String mLabels[];
	private String mValues[];
	private SimpleDateFormat mSdf;
	private Calendar mCalendar;
	private TextView history_step_daily, tv_history_step_daily,
			tv_history_step_sum;
	private View mView;
	private HistoryActivity mActivity;
	private BarChartView bcv_step;
	private TextView mBarTooltip;
	private RelativeLayout rl_pre_and_next;
	private boolean mIsPreDayShow;
	private boolean mIsPreWeekShow;
	private boolean mIsNextDayShow;
	private boolean mIsNextWeekShow;
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
				mView.findViewById(R.id.btn_history_unit_day).setEnabled(true);
				mView.findViewById(R.id.btn_history_unit_week).setEnabled(true);
				mView.findViewById(R.id.btn_history_unit_month)
						.setEnabled(true);
				if (mIsPreDayShow || mIsPreWeekShow) {
					mView.findViewById(R.id.btn_pre).setEnabled(true);
				} else {
					mView.findViewById(R.id.btn_pre).setEnabled(false);
				}
				if (mIsNextDayShow || mIsNextWeekShow) {
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
		mView = inflater.inflate(R.layout.history_step_count, container, false);
		initView();
		switch (mActivity.selectHistoryUnit) {
		case HistoryActivity.DATA_UNIT_DAY:
			initData(HistoryActivity.COUNT_NUMBER_DAY,
					mActivity.selectHistoryUnit, mActivity.mLastDayCalendar);
			break;
		case HistoryActivity.DATA_UNIT_WEEK:
			initData(HistoryActivity.COUNT_NUMBER_WEEK,
					mActivity.selectHistoryUnit, mActivity.mLastWeekCalendar);
			break;
		case HistoryActivity.DATA_UNIT_MONTH:
			initData(HistoryActivity.COUNT_NUMBER_MONTH,
					mActivity.selectHistoryUnit, null);
			break;
		}

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
		switch (event.selectHistoryUnit) {
		case HistoryActivity.DATA_UNIT_DAY:
			initData(HistoryActivity.COUNT_NUMBER_DAY, event.selectHistoryUnit,
					null);
			break;
		case HistoryActivity.DATA_UNIT_WEEK:
			initData(HistoryActivity.COUNT_NUMBER_WEEK,
					event.selectHistoryUnit, null);
			break;
		case HistoryActivity.DATA_UNIT_MONTH:
			initData(HistoryActivity.COUNT_NUMBER_MONTH,
					event.selectHistoryUnit, null);
			break;
		}
	}

	private void initData(int labelsCount, int unit, Calendar calendar) {
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
				mCalendar.add(Calendar.DAY_OF_MONTH, -1);
				mLabels[i] = mSdf.format(mCalendar.getTime());
			}
			updateBarChartByDay(labelsCount, calendar == null ? null
					: (Calendar) calendar.clone());
		}
		// 周
		if (unit == HistoryActivity.DATA_UNIT_WEEK) {
			Calendar monday = (Calendar) mCalendar.clone();
			monday.setFirstDayOfWeek(Calendar.MONDAY);
			monday.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			for (int i = labelsCount - 1; i >= 0; i--) {
				if (monday.get(Calendar.WEEK_OF_YEAR) == mActivity.mTodayCalendar
						.get(Calendar.WEEK_OF_YEAR)) {
					mLabels[i] = getString(R.string.history_this_week);
					monday.add(Calendar.WEEK_OF_MONTH, -1);
					continue;
				}
				monday.add(Calendar.WEEK_OF_MONTH, -1);
				mLabels[i] = getString(R.string.history_week_number,
						monday.get(Calendar.WEEK_OF_YEAR));
			}
			updateBarChartByWeek(labelsCount, calendar == null ? null
					: (Calendar) calendar.clone());
		}
		// 月
		if (unit == HistoryActivity.DATA_UNIT_MONTH) {
			for (int i = labelsCount - 1; i >= 0; i--) {
				if (i == labelsCount - 1) {
					mLabels[i] = getString(R.string.history_this_month);
					continue;
				}
				mCalendar.add(Calendar.MONTH, -1);
				mLabels[i] = getString(R.string.history_month_number,
						mCalendar.get(Calendar.MONTH) + 1);
			}
			updateBarChartByMonth(labelsCount);
		}
		// 计算总步数和均步数
		int stepSum = 0;
		for (int i = 0; i < labelsCount; i++) {
			if (Utils.isEmpty(mValues[i])) {
				stepSum += 0;
			} else {
				stepSum += Integer.valueOf(mValues[i]);
			}
		}
		tv_history_step_sum.setText(stepSum + "");
		tv_history_step_daily.setText((int) (stepSum / mValues.length) + "");
		if (unit == HistoryActivity.DATA_UNIT_DAY) {
			history_step_daily.setText(getString(R.string.history_step_daily));
		}
		if (unit == HistoryActivity.DATA_UNIT_WEEK) {
			history_step_daily.setText(getString(R.string.history_step_week));
		}
		if (unit == HistoryActivity.DATA_UNIT_MONTH) {
			history_step_daily.setText(getString(R.string.history_step_month));
		}

	}

	/**
	 * 判断是否可点击前一天
	 * 
	 * @param labelsCount
	 */
	private void isPreDayEnable(Calendar calendar) {
		// 前一天是否可点击
		mActivity.mLastDayCalendar = (Calendar) calendar.clone();
		mActivity.mLastDayCalendar.add(Calendar.DAY_OF_MONTH,
				-HistoryActivity.COUNT_NUMBER_DAY);
		if (mActivity.mStepsMap.get(Utils.calendar2strDate(
				mActivity.mLastDayCalendar, BTConstants.PATTERN_YYYY_MM_DD)) != null) {
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
		mActivity.mLastDayCalendar.add(Calendar.DAY_OF_MONTH,
				HistoryActivity.COUNT_NUMBER_DAY);
		if (mActivity.mLastDayCalendar.getTime().compareTo(calendar.getTime()) >= 0) {
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
		bcv_step.reset();
		BarSet data = new BarSet();
		rl_pre_and_next.setVisibility(View.VISIBLE);
		if (calendar == null) {
			calendar = mActivity.mTodayCalendar;
		}
		calendar.add(Calendar.DAY_OF_MONTH, 1 - labelsCount);
		ArrayList<Step> stepsSort = new ArrayList<Step>();
		for (int i = 0; i < labelsCount; i++) {
			int dayStep = 0;
			if (mActivity.mStepsMap.get(Utils.calendar2strDate(calendar,
					BTConstants.PATTERN_YYYY_MM_DD)) != null) {
				dayStep = Integer.valueOf(mActivity.mStepsMap.get(Utils
						.calendar2strDate(calendar,
								BTConstants.PATTERN_YYYY_MM_DD)).count);
				stepsSort.add(mActivity.mStepsMap.get(Utils.calendar2strDate(
						calendar, BTConstants.PATTERN_YYYY_MM_DD)));
			} else {
				stepsSort.add(new Step("0"));
			}
			mValues[i] = dayStep + "";
			Bar bar = new Bar(mLabels[i], dayStep);
			data.addBar(bar);
			calendar.add(Calendar.DAY_OF_MONTH, 1);
		}
		// 找到最大的，与目标值对比
		int barStepMax = 100;
		int barStepAim = SPUtiles.getIntValue(BTConstants.SP_KEY_STEP_AIM, 100);
		Collections.sort(stepsSort, new StepCompare());
		if (Integer.valueOf(stepsSort.get(0).count) >= barStepAim) {
			barStepMax = Integer.valueOf(stepsSort.get(0).count);
		} else {
			barStepMax = barStepAim;
		}

		data.setColor(getResources().getColor(R.color.blue_b4efff));
		bcv_step.addData(data);
		bcv_step.setBarSpacing((int) Tools.fromDpToPx(50));
		bcv_step.setSetSpacing(0);
		bcv_step.setBarBackground(false);
		bcv_step.setRoundCorners(0);
		// 运动目标值
		bcv_step.setmThresholdText(barStepAim + "");
		bcv_step.setBorderSpacing(0).setGrid(null).setHorizontalGrid(null)
				.setVerticalGrid(null)
				.setYLabels(YController.LabelPosition.NONE).setYAxis(false)
				.setXLabels(XController.LabelPosition.OUTSIDE).setXAxis(true)
				.setMaxAxisValue(barStepMax, 1)
				.setThresholdLine(barStepAim, DataRetriever.randPaint())
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
		mActivity.mLastWeekCalendar.setFirstDayOfWeek(Calendar.MONDAY);
		mActivity.mLastWeekCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		mActivity.mLastWeekCalendar.add(Calendar.WEEK_OF_MONTH,
				HistoryActivity.COUNT_NUMBER_WEEK);
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		if (mActivity.mLastWeekCalendar.getTime().compareTo(calendar.getTime()) >= 0) {
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
		mActivity.mLastWeekCalendar = (Calendar) calendar.clone();
		// 拿到当天所在周的周一
		mActivity.mLastWeekCalendar.setFirstDayOfWeek(Calendar.MONDAY);
		mActivity.mLastWeekCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		mActivity.mLastWeekCalendar.add(Calendar.WEEK_OF_MONTH,
				-HistoryActivity.COUNT_NUMBER_WEEK);
		int weekCount = 0;
		// 一周7天
		for (int i = 0; i < 7; i++) {
			if (mActivity.mStepsMap.get(Utils.calendar2strDate(calendar,
					BTConstants.PATTERN_YYYY_MM_DD)) != null) {
				weekCount += Integer.valueOf(mActivity.mStepsMap.get(Utils
						.calendar2strDate(calendar,
								BTConstants.PATTERN_YYYY_MM_DD)).count);
			}
			calendar.add(Calendar.DAY_OF_MONTH, 1);
		}
		if (weekCount > 0) {
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
		bcv_step.reset();
		BarSet data = new BarSet();
		rl_pre_and_next.setVisibility(View.VISIBLE);
		// 拿到最新的数据开始计算日期
		if (calendar == null) {
			calendar = mActivity.mTodayCalendar;
		}
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		// 拿到当天所在周的周一
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		calendar.add(Calendar.WEEK_OF_MONTH, 1 - labelsCount);
		int[] sortData = new int[labelsCount];
		for (int i = 0; i < labelsCount; i++) {
			int weekCount = 0;
			// 一周7天
			for (int j = 0; j < 7; j++) {
				if (mActivity.mStepsMap.get(Utils.calendar2strDate(calendar,
						BTConstants.PATTERN_YYYY_MM_DD)) != null) {
					weekCount += Integer.valueOf(mActivity.mStepsMap.get(Utils
							.calendar2strDate(calendar,
									BTConstants.PATTERN_YYYY_MM_DD)).count);
				}
				calendar.add(Calendar.DAY_OF_MONTH, 1);
			}
			Bar bar = new Bar(mLabels[i], weekCount);
			mValues[i] = weekCount + "";
			sortData[i] = weekCount;
			data.addBar(bar);
		}
		Arrays.sort(sortData);
		int barStepMax = sortData[labelsCount - 1];
		// TEST
		// int stepValue[] = { 10000, 5000, 2500, 1250, 2000, 4000, 8000 };
		// for (int i = 0; i < nPoints; i++) {
		// Bar bar = new Bar(mLabels[i], stepValue[i]);
		// mValues[i] = stepValue[i] + "";
		// data.addBar(bar);
		// }
		data.setColor(getResources().getColor(R.color.blue_b4efff));
		bcv_step.addData(data);

		bcv_step.setBarSpacing((int) Tools.fromDpToPx(50));
		bcv_step.setSetSpacing(0);
		bcv_step.setBarBackground(false);
		bcv_step.setRoundCorners(0);
		bcv_step.setBorderSpacing(0).setGrid(null).setHorizontalGrid(null)
				.setVerticalGrid(null)
				.setYLabels(YController.LabelPosition.NONE).setYAxis(false)
				.setXLabels(XController.LabelPosition.OUTSIDE).setXAxis(true)
				.setMaxAxisValue(barStepMax, 1)
				.animate(DataRetriever.randAnimation(mEndAction, labelsCount));
		isPreWeekEnable(calendar);
		isNextWeekEnable(calendar);
	}

	/**
	 * 计算以月为单位的运动量
	 * 
	 * @param labelsCount
	 */
	private void updateBarChartByMonth(int labelsCount) {
		bcv_step.reset();
		BarSet data = new BarSet();
		rl_pre_and_next.setVisibility(View.INVISIBLE);
		// 拿到最新的数据开始计算日期
		Step step = mActivity.mSteps.get(mActivity.mSteps.size() - 1);
		Calendar calendar = Utils.strDate2Calendar(step.date,
				BTConstants.PATTERN_YYYY_MM_DD);
		// 拿到当天所在月的第一天
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.add(Calendar.MONTH, 1 - labelsCount);
		int[] sortData = new int[labelsCount];
		for (int i = 0; i < labelsCount; i++) {
			int monthCount = 0;
			// 计算当月有多少天
			int daysInMonth = calendar.getActualMaximum(Calendar.DATE);
			for (int j = 0; j < daysInMonth; j++) {
				if (mActivity.mStepsMap.get(Utils.calendar2strDate(calendar,
						BTConstants.PATTERN_YYYY_MM_DD)) != null) {
					monthCount += Integer.valueOf(mActivity.mStepsMap.get(Utils
							.calendar2strDate(calendar,
									BTConstants.PATTERN_YYYY_MM_DD)).count);
				}
				calendar.add(Calendar.DAY_OF_MONTH, 1);
			}
			Bar bar = new Bar(mLabels[i], monthCount);
			mValues[i] = monthCount + "";
			sortData[i] = monthCount;
			data.addBar(bar);
		}
		Arrays.sort(sortData);
		int barStepMax = sortData[labelsCount - 1];
		// TEST
		// int stepValue[] = { 10000, 5000, 2500, 1250, 2000, 4000, 8000 };
		// for (int i = 0; i < nPoints; i++) {
		// Bar bar = new Bar(mLabels[i], stepValue[i]);
		// mValues[i] = stepValue[i] + "";
		// data.addBar(bar);
		// }
		data.setColor(getResources().getColor(R.color.blue_b4efff));
		bcv_step.addData(data);

		bcv_step.setBarSpacing((int) Tools.fromDpToPx(20));
		bcv_step.setSetSpacing(0);
		bcv_step.setBarBackground(false);
		bcv_step.setRoundCorners(0);
		bcv_step.setBorderSpacing(0).setGrid(null).setHorizontalGrid(null)
				.setVerticalGrid(null)
				.setYLabels(YController.LabelPosition.NONE).setYAxis(false)
				.setXLabels(XController.LabelPosition.OUTSIDE).setXAxis(true)
				.setMaxAxisValue(barStepMax, 1)
				.animate(DataRetriever.randAnimation(mEndAction, labelsCount));
	}

	private void initView() {
		bcv_step = (BarChartView) mView.findViewById(R.id.bcv_step);
		history_step_daily = (TextView) mView
				.findViewById(R.id.history_step_daily);
		tv_history_step_daily = (TextView) mView
				.findViewById(R.id.tv_history_step_daily);
		tv_history_step_sum = (TextView) mView
				.findViewById(R.id.tv_history_step_sum);
		bcv_step.setOnEntryClickListener(this);
		mView.findViewById(R.id.btn_history_unit_day).setOnClickListener(this);
		mView.findViewById(R.id.btn_history_unit_week).setOnClickListener(this);
		mView.findViewById(R.id.btn_history_unit_month)
				.setOnClickListener(this);
		mView.findViewById(R.id.btn_pre).setOnClickListener(this);
		mView.findViewById(R.id.btn_next).setOnClickListener(this);
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

		bcv_step.showTooltip(mBarTooltip);
		bcv_step.invalidate();
	}

	private void dismissBarTooltip(final int index, final Rect rect) {

		bcv_step.dismissTooltip(mBarTooltip);
		mBarTooltip = null;
		if (index != -1)
			showBarTooltip(index, rect);
	}

	private class StepCompare implements Comparator<Step> {

		@Override
		public int compare(Step lhs, Step rhs) {
			if (Integer.valueOf(lhs.count) > Integer.valueOf(rhs.count)) {
				return -1;
			} else if (Integer.valueOf(lhs.count) < Integer.valueOf(rhs.count)) {
				return 1;
			}
			return 0;
		}

	}

	@Override
	public void onClick(View v) {
		mView.findViewById(R.id.btn_history_unit_day).setEnabled(false);
		mView.findViewById(R.id.btn_history_unit_week).setEnabled(false);
		mView.findViewById(R.id.btn_history_unit_month).setEnabled(false);
		mView.findViewById(R.id.btn_pre).setEnabled(false);
		mView.findViewById(R.id.btn_next).setEnabled(false);
		switch (v.getId()) {
		case R.id.btn_history_unit_day:
			EventBus.getDefault().postSticky(
					new HistoryChangeUnitClick(HistoryActivity.DATA_UNIT_DAY));
			break;
		case R.id.btn_history_unit_week:
			EventBus.getDefault().postSticky(
					new HistoryChangeUnitClick(HistoryActivity.DATA_UNIT_WEEK));
			break;
		case R.id.btn_history_unit_month:
			EventBus.getDefault()
					.postSticky(
							new HistoryChangeUnitClick(
									HistoryActivity.DATA_UNIT_MONTH));
			break;
		case R.id.btn_pre:
			// 前一天/周
			if (mActivity.selectHistoryUnit == HistoryActivity.DATA_UNIT_DAY) {
				mActivity.mLastDayCalendar.add(Calendar.DAY_OF_MONTH,
						-HistoryActivity.COUNT_NUMBER_DAY);
				initData(HistoryActivity.COUNT_NUMBER_DAY,
						mActivity.selectHistoryUnit, mActivity.mLastDayCalendar);
			}
			if (mActivity.selectHistoryUnit == HistoryActivity.DATA_UNIT_WEEK) {
				mActivity.mLastWeekCalendar.add(Calendar.WEEK_OF_MONTH,
						-HistoryActivity.COUNT_NUMBER_WEEK);
				initData(HistoryActivity.COUNT_NUMBER_WEEK,
						mActivity.selectHistoryUnit,
						mActivity.mLastWeekCalendar);
			}

			break;
		case R.id.btn_next:
			// 后一天/周
			if (mActivity.selectHistoryUnit == HistoryActivity.DATA_UNIT_DAY) {
				mActivity.mLastDayCalendar.add(Calendar.DAY_OF_MONTH,
						HistoryActivity.COUNT_NUMBER_DAY);
				initData(HistoryActivity.COUNT_NUMBER_DAY,
						mActivity.selectHistoryUnit, mActivity.mLastDayCalendar);
			}
			if (mActivity.selectHistoryUnit == HistoryActivity.DATA_UNIT_WEEK) {
				mActivity.mLastWeekCalendar.add(Calendar.WEEK_OF_MONTH,
						HistoryActivity.COUNT_NUMBER_WEEK);
				initData(HistoryActivity.COUNT_NUMBER_WEEK,
						mActivity.selectHistoryUnit,
						mActivity.mLastWeekCalendar);
			}
			break;
		}
	}
}
