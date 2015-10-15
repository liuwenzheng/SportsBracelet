package com.blestep.sportsbracelet.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.db.DBTools;
import com.blestep.sportsbracelet.entity.Step;
import com.blestep.sportsbracelet.module.LogModule;
import com.blestep.sportsbracelet.utils.SPUtiles;
import com.blestep.sportsbracelet.view.CircleProgressView;
import com.blestep.sportsbracelet.view.CircleProgressView.ICircleProgressValue;

public class MainTab01 extends Fragment implements ICircleProgressValue {
	private View mView;
	private CircleProgressView circleView;
	private Button btn_step_history;
	private TextView tv_step, tv_calorie, step_heart_rate, tv_step_duration, tv_step_distance;
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
		super.onResume();
	}

	@Override
	public void onPause() {
		LogModule.i("onPause");
		super.onPause();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.main_tab_01, container, false);
		initView();
		return mView;
	}

	private void initView() {
		circleView = (CircleProgressView) mView.findViewById(R.id.circleView);
		circleView.setMaxValue(SPUtiles.getIntValue(SPUtiles.SP_KEY_STEP_AIM, 100));
		circleView.setValueAnimated(0);
		circleView.setmProgressValue(this);
		btn_step_history = (Button) mView.findViewById(R.id.btn_step_history);
		tv_step = (TextView) mView.findViewById(R.id.tv_step);
		tv_calorie = (TextView) mView.findViewById(R.id.tv_calorie);
		step_heart_rate = (TextView) mView.findViewById(R.id.step_heart_rate);
		tv_step_duration = (TextView) mView.findViewById(R.id.tv_step_duration);
		tv_step_distance = (TextView) mView.findViewById(R.id.tv_step_distance);
	}

	public void updateView() {
		Step step = DBTools.getInstance(mainActivity).selectCurrentStep();
		if (step != null) {
			float count = Float.valueOf(step.count);
			circleView.setValueAnimated(Float.valueOf(step.count));
			tv_step.setText(count + "");
			String duration = step.duration;
			tv_step_duration.setText(duration);
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

}
