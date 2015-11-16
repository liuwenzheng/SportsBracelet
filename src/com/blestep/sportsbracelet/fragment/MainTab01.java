package com.blestep.sportsbracelet.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.activity.HistoryActivity;
import com.blestep.sportsbracelet.activity.MainActivity;
import com.blestep.sportsbracelet.db.DBTools;
import com.blestep.sportsbracelet.entity.Step;
import com.blestep.sportsbracelet.module.LogModule;
import com.blestep.sportsbracelet.utils.SPUtiles;
import com.blestep.sportsbracelet.utils.Utils;
import com.blestep.sportsbracelet.view.CircleProgressView;
import com.blestep.sportsbracelet.view.CircleProgressView.ICircleProgressValue;

public class MainTab01 extends Fragment implements ICircleProgressValue,
		OnClickListener {
	private View mView;
	private CircleProgressView circleView;
	private Button btn_step_history;
	private TextView tv_step, tv_calorie, step_heart_rate, tv_step_duration,
			tv_step_distance;
	private MainActivity mainActivity;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		LogModule.i("onActivityCreated");
		mainActivity = (MainActivity) getActivity();
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onResume() {
		LogModule.i("onResume");
		initData();
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
		mView = inflater.inflate(R.layout.main_tab_01, container, false);
		initView();
		return mView;
	}

	private void initData() {
		circleView.setMaxValue(SPUtiles.getIntValue(
				BTConstants.SP_KEY_STEP_AIM, 100));
		circleView.setValue(0);
		circleView.setmProgressValue(this);
	}

	private void initView() {
		circleView = (CircleProgressView) mView.findViewById(R.id.circleView);

		btn_step_history = (Button) mView.findViewById(R.id.btn_step_history);
		btn_step_history.setOnClickListener(this);
		tv_step = (TextView) mView.findViewById(R.id.tv_step);
		tv_calorie = (TextView) mView.findViewById(R.id.tv_calorie);
		step_heart_rate = (TextView) mView.findViewById(R.id.step_heart_rate);
		tv_step_duration = (TextView) mView.findViewById(R.id.tv_step_duration);
		setStepDuration(0, 0);
	}

	private void setStepDuration(int hour, int min) {
		int hourLength = (hour + " ").length();
		int minLength = (" " + min + " ").length();
		SpannableString spannableString = new SpannableString(getString(
				R.string.step_duration_unit, hour + " ", " " + min + " "));
		spannableString.setSpan(new ForegroundColorSpan(getResources()
				.getColor(R.color.grey_d5d5d5)), hourLength, hourLength + 1,
				Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
		spannableString.setSpan(new ForegroundColorSpan(getResources()
				.getColor(R.color.grey_d5d5d5)), hourLength + 1 + minLength,
				spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		tv_step_duration.setText(spannableString);
		tv_step_distance = (TextView) mView.findViewById(R.id.tv_step_distance);
	}

	public void updateView() {
		Step step = DBTools.getInstance(mainActivity).selectCurrentStep();
		if (step != null) {
			int count = Integer.valueOf(step.count);
			circleView.setValueAnimated(Float.valueOf(step.count));
			tv_step.setText(0 + "");
			String duration = step.duration;
			if (Utils.isNotEmpty(duration)) {
				int hour = Integer.valueOf(duration) / 60;
				int min = Integer.valueOf(duration) % 60;
				setStepDuration(hour, min);
			}
			String distance = step.distance;
			tv_step_distance.setText(distance);
			String calories = step.calories;
			tv_calorie.setText(calories);
		}
	}

	@Override
	public void getProgressValue(int value) {
		tv_step.setText(value + "");
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_step_history:
			startActivity(new Intent(mainActivity, HistoryActivity.class));
			mainActivity.overridePendingTransition(R.anim.page_down_in,
					R.anim.page_up_out);
			break;

		default:
			break;
		}

	}

}
