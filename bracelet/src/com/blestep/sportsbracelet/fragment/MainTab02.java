package com.blestep.sportsbracelet.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.activity.MainActivity;
import com.blestep.sportsbracelet.module.LogModule;
import com.blestep.sportsbracelet.view.CircleProgressView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainTab02 extends Fragment implements CircleProgressView.ICircleProgressValue {
    CircleProgressView circleView;
    TextView tvSleep;
    TextView tvSleepSoberTime;
    TextView tvSleepLightTime;
    TextView tvSleepDeepTime;
    private View mView;
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

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.main_tab_02, container,
                false);
        circleView = ButterKnife.findById(mView, R.id.circleView);
        tvSleep = ButterKnife.findById(mView, R.id.tv_sleep);
        tvSleepSoberTime = ButterKnife.findById(mView, R.id.tv_sleep_sober_time);
        tvSleepLightTime = ButterKnife.findById(mView, R.id.tv_sleep_light_time);
        tvSleepDeepTime = ButterKnife.findById(mView, R.id.tv_sleep_deep_time);
        initData();
        return mView;
    }

    @Override
    public void getProgressValue(int value) {
        tvSleep.setText(sleepDurationCount(value));
    }

    private SpannableString sleepDuration(int value) {
        int hour = Integer.valueOf(value) / 60;
        int min = Integer.valueOf(value) % 60;
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
        return spannableString;
    }

    private SpannableString sleepDurationCount(int value) {
        int hour = Integer.valueOf(value) / 60;
        int min = Integer.valueOf(value) % 60;
        int hourLength = (hour + " ").length();
        int minLength = (" " + min + " ").length();
        SpannableString spannableString = new SpannableString(getString(
                R.string.step_duration_unit, hour + " ", " " + min + " "));
        spannableString.setSpan(new ForegroundColorSpan(getResources()
                        .getColor(R.color.grey_545454)), hourLength, hourLength + 1,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new AbsoluteSizeSpan(36), hourLength, hourLength + 1,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(getResources()
                        .getColor(R.color.grey_545454)), hourLength + 1 + minLength,
                spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new AbsoluteSizeSpan(36), hourLength + 1 + minLength,
                spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    private void initData() {
        circleView.setMaxValue(600);
        circleView.setValueAnimated(480);
        circleView.setmProgressValue(this);
        tvSleepSoberTime.setText(sleepDuration(80));
        tvSleepLightTime.setText(sleepDuration(210));
        tvSleepDeepTime.setText(sleepDuration(190));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
