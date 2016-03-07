package com.blestep.sportsbracelet.fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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
	private int BAR_STEP_MAX = 100;
	private int BAR_STEP_AIM = 100;
	private ArrayList<Step> mSteps;
	private ArrayList<Step> mStepsSort;
	private SimpleDateFormat mSdf;
	private Calendar mCalendar;
	private TextView tv_history_step_daily, tv_history_step_sum;

	private static Runnable mEndAction = new Runnable() {
		@Override
		public void run() {

		}
	};

	private View mView;
	private HistoryActivity mActivity;
	private BarChartView bcv_step;
	private TextView mBarTooltip;

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
			initData(7, mActivity.selectHistoryUnit);
			break;
		case HistoryActivity.DATA_UNIT_WEEK:
			initData(7, mActivity.selectHistoryUnit);
			break;
		case HistoryActivity.DATA_UNIT_MONTH:
			initData(12, mActivity.selectHistoryUnit);
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
			initData(7, event.selectHistoryUnit);
			break;
		case HistoryActivity.DATA_UNIT_WEEK:
			initData(7, event.selectHistoryUnit);
			break;
		case HistoryActivity.DATA_UNIT_MONTH:
			initData(7, event.selectHistoryUnit);
			break;
		}
	}

	private void initData(int labelsCount, int unit) {
		mLabels = new String[labelsCount];
		mValues = new String[labelsCount];
		mSteps = (ArrayList<Step>) getArguments().getSerializable(
				BTConstants.EXTRA_KEY_HISTORY);
		BAR_STEP_AIM = SPUtiles.getIntValue(BTConstants.SP_KEY_STEP_AIM, 100);
		// 找到最大的，与目标值对比
		mStepsSort = new ArrayList<Step>();
		mStepsSort.addAll(mSteps);
		Collections.sort(mStepsSort, new StepCompare());
		if (Integer.valueOf(mStepsSort.get(0).count) >= BAR_STEP_AIM) {
			BAR_STEP_MAX = Integer.valueOf(mStepsSort.get(0).count);
		} else {
			BAR_STEP_MAX = BAR_STEP_AIM;
		}
		mSdf = new SimpleDateFormat(BTConstants.PATTERN_MM_DD);
		mCalendar = Calendar.getInstance();
		// 日
		if (unit == HistoryActivity.DATA_UNIT_DAY) {
			for (int i = mLabels.length - 1; i >= 0; i--) {
				if (i == mLabels.length - 1) {
					mLabels[i] = getString(R.string.history_today);
					continue;
				}
				mCalendar.add(Calendar.DAY_OF_MONTH, -1);
				// if (i == mLabels.length - 2) {
				// mLabels[i] = getString(R.string.history_yesterday);
				// continue;
				// }
				mLabels[i] = mSdf.format(mCalendar.getTime());
			}
		}
		// 周
		if (unit == HistoryActivity.DATA_UNIT_WEEK) {
			Calendar monday = Calendar.getInstance();
			monday.setFirstDayOfWeek(Calendar.MONDAY);
			monday.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

			for (int i = mLabels.length - 1; i >= 0; i--) {
				if (i == mLabels.length - 1) {
					mLabels[i] = getString(R.string.history_this_week);
					continue;
				}
				monday.add(Calendar.WEEK_OF_MONTH, -1);
				mLabels[i] = getString(R.string.history_week_number,
						monday.get(Calendar.WEEK_OF_YEAR));
			}
		}
		// 月
		if (unit == HistoryActivity.DATA_UNIT_MONTH) {
			for (int i = mLabels.length - 1; i >= 0; i--) {
				if (i == mLabels.length - 1) {
					mLabels[i] = getString(R.string.history_this_month);
					continue;
				}
				mCalendar.add(Calendar.MONTH, -1);
				mLabels[i] = getString(R.string.history_month_number,
						mCalendar.get(Calendar.MONTH) + 1);
			}
		}
		updateBarChart(mLabels.length);
		int stepSum = 0;
		for (int i = 0; i < mValues.length; i++) {
			if (Utils.isEmpty(mValues[i])) {
				stepSum += 0;
			} else {
				stepSum += Integer.valueOf(mValues[i]);
			}
		}
		tv_history_step_sum.setText(stepSum + "");
		tv_history_step_daily.setText((int) (stepSum / mValues.length) + "");

	}

	private void initView() {
		bcv_step = (BarChartView) mView.findViewById(R.id.bcv_step);
		tv_history_step_daily = (TextView) mView
				.findViewById(R.id.tv_history_step_daily);
		tv_history_step_sum = (TextView) mView
				.findViewById(R.id.tv_history_step_sum);
		bcv_step.setOnEntryClickListener(this);
		mView.findViewById(R.id.btn_history_unit_day).setOnClickListener(this);
		mView.findViewById(R.id.btn_history_unit_week).setOnClickListener(this);
		mView.findViewById(R.id.btn_history_unit_month)
				.setOnClickListener(this);
	}

	@Override
	public void onClick(int setIndex, int entryIndex, Rect rect) {
		if (mBarTooltip == null)
			showBarTooltip(entryIndex, rect);
		else
			dismissBarTooltip(entryIndex, rect);

	}

	public void updateBarChart(int nPoints) {
		bcv_step.reset();
		BarSet data = new BarSet();
		int index = mSteps.size();
		int start = 0;
		if (index < nPoints) {
			start = nPoints - index;
		} else {
			start = index - nPoints;
		}
		// TODO
		for (int i = 0; i < nPoints; i++) {
			Bar bar;
			if (index < nPoints && i < start) {
				bar = new Bar(mLabels[i], 0f);
				mValues[i] = 0 + "";
			} else {
				if (index < nPoints) {
					bar = new Bar(mLabels[i], Integer.valueOf(mSteps.get(i
							- start).count));
					mValues[i] = mSteps.get(i - start).count;
				} else {
					bar = new Bar(mLabels[i], Integer.valueOf(mSteps.get(i
							+ start).count));
					mValues[i] = mSteps.get(i + start).count;
				}
			}
			data.addBar(bar);
		}
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
		// 运动目标值
		bcv_step.setmThresholdText(BAR_STEP_AIM + "");
		bcv_step.setBorderSpacing(0).setGrid(null).setHorizontalGrid(null)
				.setVerticalGrid(null)
				.setYLabels(YController.LabelPosition.NONE).setYAxis(false)
				.setXLabels(XController.LabelPosition.OUTSIDE).setXAxis(true)
				.setMaxAxisValue(BAR_STEP_MAX, 1)
				.setThresholdLine(BAR_STEP_AIM, DataRetriever.randPaint())
				.animate(DataRetriever.randAnimation(mEndAction, nPoints));
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
		}
	}

}
