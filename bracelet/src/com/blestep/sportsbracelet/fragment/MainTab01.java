package com.blestep.sportsbracelet.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.activity.HistoryStepActivity;
import com.blestep.sportsbracelet.activity.MainActivity;
import com.blestep.sportsbracelet.db.DBTools;
import com.blestep.sportsbracelet.entity.Step;
import com.blestep.sportsbracelet.module.LogModule;
import com.blestep.sportsbracelet.module.UnitManagerModule;
import com.blestep.sportsbracelet.utils.SPUtiles;
import com.blestep.sportsbracelet.utils.Utils;
import com.blestep.sportsbracelet.utils.WaveHelper;
import com.blestep.sportsbracelet.view.CircleProgressView;
import com.blestep.sportsbracelet.view.CircleProgressView.ICircleProgressValue;
import com.blestep.sportsbracelet.view.GradientBarChart;
import com.gelitenight.waveview.library.WaveView;

import java.util.ArrayList;
import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainTab01 extends Fragment implements ICircleProgressValue,
        OnClickListener {
    @Bind(R.id.circleView)
    CircleProgressView circleView;
    @Bind(R.id.tv_step)
    TextView tv_step;
    @Bind(R.id.tv_calorie)
    TextView tv_calorie;
    @Bind(R.id.tv_step_duration)
    TextView tv_step_duration;
    @Bind(R.id.tv_step_distance)
    TextView tv_step_distance;
    @Bind(R.id.tv_step_distance_unit)
    TextView tv_step_distance_unit;
    @Bind(R.id.wave)
    WaveView wave;
    @Bind(R.id.gbc_step_week_history)
    GradientBarChart gbc_step_week_history;
    private View mView;
    private MainActivity mainActivity;
    private WaveHelper mWaveHelper;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        LogModule.i("onActivityCreated");
        mainActivity = (MainActivity) getActivity();
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        LogModule.i(this.getClass().getSimpleName() + "onResume");
        super.onResume();
        initData();
        wave.setShapeType(WaveView.ShapeType.SQUARE);
        wave.setWaveColor(ContextCompat.getColor(mainActivity, R.color.blue_82f0f3),
                ContextCompat.getColor(mainActivity, R.color.white_ffffff));
        mWaveHelper.start();
    }

    @Override
    public void onPause() {
        LogModule.i(this.getClass().getSimpleName() + "onPause");
        super.onPause();
        mWaveHelper.cancel();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.main_tab_01, container, false);
        ButterKnife.bind(this, mView);
        mWaveHelper = new WaveHelper(wave);
        return mView;
    }

    private void initData() {
        circleView.setMaxValue(SPUtiles.getIntValue(
                BTConstants.SP_KEY_STEP_AIM, 100));
        circleView.setValue(0);
        tv_calorie.setText("0");
        circleView.setmProgressValue(this);
        setStepDuration(0, 0);
        gbc_step_week_history.setAimValue(SPUtiles.getIntValue(
                BTConstants.SP_KEY_STEP_AIM, 100));
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
            circleView.setValueAnimated(Float.parseFloat(step.count));
            String duration = step.duration;
            if (Utils.isNotEmpty(duration)) {
                int hour = Integer.parseInt(duration) / 60;
                int min = Integer.parseInt(duration) % 60;
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
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -6);
            ArrayList<Integer> datas = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                Step s = DBTools.getInstance(mainActivity).selectStep(calendar);
                if (s == null) {
                    datas.add(0);
                } else {
                    datas.add(Integer.parseInt(s.count));
                }
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
            gbc_step_week_history.setDatas(datas);
        }
    }

    @Override
    public void getProgressValue(int value) {
        tv_step.setText(value + "");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick(R.id.rl_step_history)
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
