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
import android.view.ViewGroup;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.activity.HistoryActivity;
import com.blestep.sportsbracelet.db.DBTools;
import com.blestep.sportsbracelet.entity.Step;
import com.blestep.sportsbracelet.module.LogModule;
import com.blestep.sportsbracelet.utils.DataRetriever;
import com.blestep.sportsbracelet.utils.SPUtiles;
import com.db.chart.Tools;
import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.Bar;
import com.db.chart.model.BarSet;
import com.db.chart.view.BarChartView;
import com.db.chart.view.XController;
import com.db.chart.view.YController;

public class HistoryTab01 extends Fragment implements OnEntryClickListener {
	private static String mLabels[];
	private static String mValues[];
	private static int BAR_MAX = 100;
	private static int BAR_AIM = 0;
	private ArrayList<Step> mSteps;
	private ArrayList<Step> mStepsSort;
	private SimpleDateFormat mSdf;
	private Calendar mCalendar;

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
		LogModule.i("onActivityCreated");
		mActivity = (HistoryActivity) getActivity();
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onResume() {
		LogModule.i("onResume");
		super.onResume();
	}

	@Override
	public void onPause() {
		LogModule.i("onPause");
		super.onPause();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.history_tab_01, container, false);
		initView();
		initData();
		return mView;
	}

	private void initData() {
		mLabels = new String[7];
		mValues = new String[7];
		mSteps = DBTools.getInstance(mActivity).selectAllStep();
		BAR_AIM = SPUtiles.getIntValue(BTConstants.SP_KEY_STEP_AIM, 100);
		// 找到最大的，与目标值对比
		mStepsSort = new ArrayList<Step>();
		mStepsSort.addAll(mSteps);
		Collections.sort(mStepsSort, new StepCompare());
		if (Integer.valueOf(mSteps.get(0).count) >= BAR_AIM) {
			BAR_MAX = Integer.valueOf(mSteps.get(0).count);
		} else {
			BAR_MAX = BAR_AIM;
		}
		mSdf = new SimpleDateFormat(BTConstants.PATTERN_MM_DD);
		mCalendar = Calendar.getInstance();

		for (int i = mLabels.length - 1; i >= 0; i--) {
			if (i == mLabels.length - 1) {
				mLabels[i] = getString(R.string.history_today);
				continue;
			}
			mCalendar.add(Calendar.DAY_OF_MONTH, -1);
			if (i == mLabels.length - 2) {
				mLabels[i] = getString(R.string.history_yesterday);
				continue;
			}
			mLabels[i] = mSdf.format(mCalendar.getTime());
		}
		updateBarChart(mLabels.length);
	}

	private void initView() {
		bcv_step = (BarChartView) mView.findViewById(R.id.bcv_step);
		bcv_step.setOnEntryClickListener(this);
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
		for (int j = 0; j < nPoints; j++) {
			Bar bar;
			if (j < start) {
				bar = new Bar(mLabels[j], 0f);
				mValues[j] = 0 + "";
			} else {
				if (index < nPoints) {
					bar = new Bar(mLabels[j], Integer.valueOf(mSteps.get(j
							- start).count));
					mValues[j] = mSteps.get(j - start).count;
				} else {
					bar = new Bar(mLabels[j], Integer.valueOf(mSteps.get(j
							+ start).count));
					mValues[j] = mSteps.get(j + start).count;
				}
			}
			data.addBar(bar);
		}

		data.setColor(getResources().getColor(R.color.blue_b4efff));
		bcv_step.addData(data);

		bcv_step.setBarSpacing((int) Tools.fromDpToPx(50));
		bcv_step.setSetSpacing(0);
		bcv_step.setBarBackground(false);
		bcv_step.setRoundCorners(0);
		// 运动目标值
		bcv_step.setmThresholdText(BAR_AIM + "");
		bcv_step.setBorderSpacing(0).setGrid(null).setHorizontalGrid(null)
				.setVerticalGrid(null)
				.setYLabels(YController.LabelPosition.NONE).setYAxis(false)
				.setXLabels(XController.LabelPosition.OUTSIDE).setXAxis(true)
				.setMaxAxisValue(BAR_MAX, BAR_MAX / 10)
				.setThresholdLine(BAR_AIM, DataRetriever.randPaint())
				.animate(DataRetriever.randAnimation(mEndAction, nPoints));
	}

	private void showBarTooltip(int index, Rect rect) {

		mBarTooltip = (TextView) mActivity.getLayoutInflater().inflate(
				R.layout.tooltip, null);
		mBarTooltip.setText("" + mValues[index]);

		LayoutParams layoutParams = new LayoutParams(rect.width()
				+ (int) Tools.fromDpToPx(40), LayoutParams.WRAP_CONTENT);
		layoutParams.leftMargin = rect.left - (int) Tools.fromDpToPx(20);
		layoutParams.topMargin = rect.top - (int) Tools.fromDpToPx(40);
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
				return 1;
			} else if (Integer.valueOf(lhs.count) < Integer.valueOf(rhs.count)) {
				return -1;
			}
			return 0;
		}

	}
}
