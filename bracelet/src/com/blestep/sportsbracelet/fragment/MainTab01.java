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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.activity.HistoryActivity;
import com.blestep.sportsbracelet.activity.HistoryStepActivity;
import com.blestep.sportsbracelet.activity.MainActivity;
import com.blestep.sportsbracelet.db.DBTools;
import com.blestep.sportsbracelet.entity.Step;
import com.blestep.sportsbracelet.module.LogModule;
import com.blestep.sportsbracelet.module.UnitManagerModule;
import com.blestep.sportsbracelet.utils.SPUtiles;
import com.blestep.sportsbracelet.utils.Utils;
import com.blestep.sportsbracelet.view.CircleProgressView;
import com.blestep.sportsbracelet.view.CircleProgressView.ICircleProgressValue;

public class MainTab01 extends Fragment implements ICircleProgressValue,
        OnClickListener {
    private View mView;
    private CircleProgressView circleView;
    private RelativeLayout rl_step_history;
    private TextView tv_step, tv_calorie, tv_step_duration,
            tv_step_distance, tv_step_distance_unit;
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
        tv_calorie.setText("0");
        circleView.setmProgressValue(this);
    }

    private void initView() {
        circleView = (CircleProgressView) mView.findViewById(R.id.circleView);

        rl_step_history = (RelativeLayout) mView.findViewById(R.id.rl_step_history);
        rl_step_history.setOnClickListener(this);
        tv_step = (TextView) mView.findViewById(R.id.tv_step);
        tv_calorie = (TextView) mView.findViewById(R.id.tv_calorie);
        tv_step_duration = (TextView) mView.findViewById(R.id.tv_step_duration);
        setStepDuration(0, 0);
    }

    private void setStepDuration(int hour, int min) {
        int hourLength = (hour + " ").length();
        int minLength = (" " + min + " ").length();
        SpannableString spannableString = new SpannableString(getString(
                R.string.step_duration_unit, hour + " ", " " + min + " "));
        spannableString.setSpan(new ForegroundColorSpan(getResources()
                        .getColor(R.color.white_ffffff)), hourLength, hourLength + 1,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(getResources()
                        .getColor(R.color.white_ffffff)), hourLength + 1 + minLength,
                spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_step_duration.setText(spannableString);
        tv_step_distance = (TextView) mView.findViewById(R.id.tv_step_distance);
        tv_step_distance_unit = (TextView) mView.findViewById(R.id.tv_step_distance_unit);
    }

    public void updateView() {
        Step step = DBTools.getInstance(mainActivity).selectCurrentStep();
        if (step != null) {
            circleView.setValueAnimated(Float.valueOf(step.count));
            String duration = step.duration;
            if (Utils.isNotEmpty(duration)) {
                int hour = Integer.valueOf(duration) / 60;
                int min = Integer.valueOf(duration) % 60;
                setStepDuration(hour, min);
            }
            boolean isBritish = SPUtiles.getBooleanValue(BTConstants.SP_KEY_IS_BRITISH_UNIT, false);
            String distance = step.distance;
            if (!isBritish) {
                tv_step_distance.setText(distance);
                tv_step_distance_unit.setText(getString(R.string.step_distance_unit));
            } else {
                float distance_british = Float.valueOf(distance);
                tv_step_distance.setText(UnitManagerModule.kmToMi(distance_british) + "");
                tv_step_distance_unit.setText(getString(R.string.step_distance_unit_british));
            }
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
            case R.id.rl_step_history:
                startActivity(new Intent(mainActivity, HistoryStepActivity.class));
                mainActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;

            default:
                break;
        }

    }

}
