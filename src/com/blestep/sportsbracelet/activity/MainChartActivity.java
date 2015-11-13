package com.blestep.sportsbracelet.activity;

import android.animation.TimeInterpolator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.utils.DataRetriever;
import com.blestep.sportsbracelet.utils.Utils;
import com.db.chart.Tools;
import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.Bar;
import com.db.chart.model.BarSet;
import com.db.chart.view.BarChartView;
import com.db.chart.view.XController;
import com.db.chart.view.YController;

public class MainChartActivity extends Activity {

	private final static String[] mLabels = { "11/02", "11/03", "11/04", "11/05", "11/06", "11/07", "11/08", "11/09" };
	private final TimeInterpolator enterInterpolator = new DecelerateInterpolator(1.5f);
	private final TimeInterpolator exitInterpolator = new AccelerateInterpolator();

	private final static float BAR_MAX = 10;
	private final static float BAR_MIN = 2;
	private static BarChartView mBarChart;
	private TextView mBarTooltip;
	private TextView mBarToolBg;

	private static Button mButton;

	private static Runnable mEndAction = new Runnable() {
		@Override
		public void run() {
			mButton.setEnabled(true);
			mButton.setText("PLAY ME");
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_chart);

		mButton = (Button) findViewById(R.id.button);
		mButton.setTypeface(Typeface.createFromAsset(getAssets(), "Roboto-Regular.ttf"));

		mButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				mBarChart.dismissAllTooltips();
				mBarTooltip = null;

				mButton.setEnabled(false);
				mButton.setText("I'M PLAYING...");

				updateBarChart(1, 7);

			}
		});
		initBarChart();
		updateBarChart(1, 7);
	}

	/*------------------------------------*
	 *              BARCHART              *
	 *------------------------------------*/

	private void initBarChart() {

		final OnEntryClickListener barEntryListener = new OnEntryClickListener() {

			@Override
			public void onClick(int setIndex, int entryIndex, Rect rect) {

				if (mBarTooltip == null)
					showBarTooltip(entryIndex, rect);
				else
					dismissBarTooltip(entryIndex, rect);
			}
		};

		final OnClickListener barClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// if (mBarTooltip != null)
				// dismissBarTooltip(-1, null);
			}
		};

		mBarChart = (BarChartView) findViewById(R.id.barchart);
		mBarChart.setOnEntryClickListener(barEntryListener);
		mBarChart.setOnClickListener(barClickListener);
	}

	public void updateBarChart(int nSets, int nPoints) {

		BarSet data;
		Bar bar;
		mBarChart.reset();

		for (int i = 0; i < nSets; i++) {

			data = new BarSet();
			for (int j = 0; j < nPoints; j++) {
				if (j == 2) {
					bar = new Bar(mLabels[j], 2f);
				} else {
					bar = new Bar(mLabels[j], 10f);
				}// bar.setColor(Color.parseColor(getColor(j)));
				data.addBar(bar);
			}

			data.setColor(Color.parseColor(DataRetriever.getColor(i)));
			mBarChart.addData(data);
		}

		mBarChart.setBarSpacing((int) Tools.fromDpToPx(50));
		mBarChart.setSetSpacing(0);
		mBarChart.setBarBackground(false);
		mBarChart.setBarBackgroundColor(Color.parseColor("#37474f"));
		mBarChart.setRoundCorners(0);

		mBarChart.setBorderSpacing(0).setGrid(null).setHorizontalGrid(null).setVerticalGrid(null)
				.setYLabels(YController.LabelPosition.NONE).setYAxis(false)
				.setXLabels(XController.LabelPosition.OUTSIDE).setXAxis(true)
				.setThresholdLine(4, DataRetriever.randPaint()).setMaxAxisValue((int) BAR_MAX, 2)
				.animate(DataRetriever.randAnimation(mEndAction, nPoints));
	}

	@SuppressLint("NewApi")
	private void showBarTooltip(int index, Rect rect) {

		mBarTooltip = (TextView) getLayoutInflater().inflate(R.layout.tooltip, null);
		mBarTooltip.setText("" + mLabels[index]);

		LayoutParams layoutParams = new LayoutParams(rect.width() + (int) Tools.fromDpToPx(40),
				LayoutParams.WRAP_CONTENT);
		layoutParams.leftMargin = rect.left - (int) Tools.fromDpToPx(20);
		layoutParams.topMargin = rect.top - (int) Tools.fromDpToPx(40);
		mBarTooltip.setLayoutParams(layoutParams);

		// mBarToolBg = (TextView) getLayoutInflater().inflate(R.layout.tooltip,
		// null);
		// mBarToolBg.setBackgroundColor(Color.parseColor("#4dc1c6"));
		// LayoutParams lp = new LayoutParams(rect.width(), rect.height());
		// lp.leftMargin = rect.left;
		// lp.topMargin = rect.top;
		// mBarToolBg.setLayoutParams(lp);

		// if (android.os.Build.VERSION.SDK_INT >=
		// Build.VERSION_CODES.HONEYCOMB_MR1) {
		// mBarTooltip.setAlpha(0);
		// mBarTooltip.setScaleY(0);
		// mBarTooltip.animate().setDuration(200).alpha(1).scaleY(1).setInterpolator(enterInterpolator);
		// }
		// mBarChart.addView(mBarToolBg);
		mBarChart.showTooltip(mBarTooltip);
		mBarChart.invalidate();
	}

	@SuppressLint("NewApi")
	private void dismissBarTooltip(final int index, final Rect rect) {

		// if (android.os.Build.VERSION.SDK_INT >=
		// Build.VERSION_CODES.JELLY_BEAN) {
		// mBarTooltip.animate().setDuration(100).scaleY(0).alpha(0).setInterpolator(exitInterpolator)
		// .withEndAction(new Runnable() {
		// @Override
		// public void run() {
		// mBarChart.removeView(mBarTooltip);
		// mBarTooltip = null;
		// if (index != -1)
		// showBarTooltip(index, rect);
		// }
		// });
		// } else {
		mBarChart.dismissTooltip(mBarTooltip);
		// mBarChart.dismissTooltip(mBarToolBg);

		mBarTooltip = null;
		mBarToolBg = null;
		if (index != -1)
			showBarTooltip(index, rect);
		// }
	}

}
