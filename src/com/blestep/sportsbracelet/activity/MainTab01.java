package com.blestep.sportsbracelet.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.module.LogModule;
import com.blestep.sportsbracelet.view.CircleProgressView;
import com.blestep.sportsbracelet.view.CircleProgressView.ICircleProgressValue;

public class MainTab01 extends Fragment implements ICircleProgressValue {
	private View mView;
	private CircleProgressView circleView;
	private Button btn_step_history;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
	}
	@Override
	public void onResume() {
		LogModule.i("onResume");
		super.onResume();
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.main_tab_01, container, false);
		initView();

		return mView;
	}

	private void initView() {
		circleView = (CircleProgressView) mView.findViewById(R.id.circleView);
		circleView.setMaxValue(100);
		circleView.setValueAnimated(80);
		circleView.setmProgressValue(this);
		btn_step_history = (Button) mView.findViewById(R.id.btn_step_history);
	}

	@Override
	public void getProgressValue(int value) {
		LogModule.i(value + "");
	}

}
