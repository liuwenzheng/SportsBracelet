package com.blestep.sportsbracelet.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.activity.HistorySleepActivity;
import com.blestep.sportsbracelet.activity.MainActivity;
import com.blestep.sportsbracelet.db.DBTools;
import com.blestep.sportsbracelet.entity.Sleep;
import com.blestep.sportsbracelet.module.LogModule;
import com.blestep.sportsbracelet.utils.Utils;
import com.blestep.sportsbracelet.view.SleepStatusView;

import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainTabSleep extends Fragment {

    @Bind(R.id.tv_asleep_duration)
    TextView tv_asleep_druation;
    @Bind(R.id.tv_deep_sleep)
    TextView tv_deep_sleep;
    @Bind(R.id.tv_light_sleep)
    TextView tv_light_sleep;
    @Bind(R.id.tv_bed_time)
    TextView tv_bed_time;
    @Bind(R.id.tv_wake_up_time)
    TextView tv_wake_up_time;
    @Bind(R.id.tv_awake_duration)
    TextView tv_awake_duration;
    @Bind(R.id.tv_sleep_start_time)
    TextView tv_sleep_start_time;
    @Bind(R.id.tv_sleep_end_time)
    TextView tv_sleep_end_time;
    @Bind(R.id.ssv_sleep_status)
    SleepStatusView ssv_sleep_status;
    @Bind(R.id.tv_sleep_date)
    TextView tv_sleep_date;
    @Bind(R.id.tv_sleep_date_pre)
    TextView tv_sleep_date_pre;
    @Bind(R.id.tv_sleep_date_next)
    TextView tv_sleep_date_next;
    private MainActivity mainActivity;
    private Calendar mCalendar;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        LogModule.i(this.getClass().getSimpleName() + "-->onActivityCreated");
        mainActivity = (MainActivity) getActivity();
        super.onActivityCreated(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View messageLayout = inflater.inflate(R.layout.main_tab_sleep, container,
                false);
        ButterKnife.bind(this, messageLayout);
        updateView(Calendar.getInstance());
        return messageLayout;
    }

    public void updateView(Calendar calendar) {
        if (mainActivity == null)
            return;
        mCalendar = (Calendar) calendar.clone();
        Sleep sleep = DBTools.getInstance(mainActivity).selectSleep(calendar);
        String date = Utils.calendar2strDate(calendar, BTConstants.PATTERN_MM_DD_2);
        tv_sleep_date.setText(date);
        if (Utils.calInterval(calendar.getTime(), Calendar.getInstance().getTime()) == 0) {
            tv_sleep_date_next.setTextColor(ContextCompat.getColor(mainActivity, R.color.grey_758e9a));
        } else {
            tv_sleep_date_next.setTextColor(ContextCompat.getColor(mainActivity, R.color.white_ffffff));
        }
        if (sleep == null) {
            tv_asleep_druation.setText(setSleepDuration(0, 0));
            tv_awake_duration.setText(setSleepDuration(0, 0));
            tv_deep_sleep.setText(setSleepDuration(0, 0));
            tv_light_sleep.setText(setSleepDuration(0, 0));
            tv_bed_time.setText("00:00");
            tv_wake_up_time.setText("00:00");
            tv_sleep_start_time.setText("00:00");
            tv_sleep_end_time.setText("00:00");
            ssv_sleep_status.setData(sleep);
        } else {
            int light = Integer.parseInt(sleep.light);
            int deep = Integer.parseInt(sleep.deep);
            int awake = Integer.parseInt(sleep.awake);
            int asleep = light + deep;
            tv_asleep_druation.setText(setSleepDuration(asleep / 60, asleep % 60));
            tv_awake_duration.setText(setSleepDuration(awake / 60, awake % 60));
            tv_deep_sleep.setText(setSleepDuration(deep / 60, deep % 60));
            tv_light_sleep.setText(setSleepDuration(light / 60, light % 60));
            tv_bed_time.setText(sleep.start.substring(sleep.start.length() - 5, sleep.start.length()));
            tv_sleep_start_time.setText(sleep.start.substring(sleep.start.length() - 5, sleep.start.length()));
            tv_wake_up_time.setText(sleep.end.substring(sleep.end.length() - 5, sleep.end.length()));
            tv_sleep_end_time.setText(sleep.end.substring(sleep.end.length() - 5, sleep.end.length()));
            ssv_sleep_status.setData(sleep);
        }
    }

    private SpannableString setSleepDuration(int hour, int min) {
        int hourLength = (hour + " ").length();
        int minLength = (" " + min + " ").length();
        int hourUnit = getString(R.string.sleep_duration_unit_hour).length();
        SpannableString spannableString = new SpannableString(String.format("%s%s%s%s",
                hour + " ", getString(R.string.sleep_duration_unit_hour),
                " " + min + " ", getString(R.string.sleep_duration_unit_min)));
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.grey_666666)),
                0, hourLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new AbsoluteSizeSpan(15, true),
                0, hourLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.grey_666666)),
                hourLength + hourUnit, hourLength + hourUnit + minLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new AbsoluteSizeSpan(15, true),
                hourLength + hourUnit, hourLength + hourUnit + minLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick({R.id.tv_sleep_date_pre, R.id.tv_sleep_date_next, R.id.tv_sleep_history})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_sleep_date_pre:
                mCalendar.add(Calendar.DAY_OF_MONTH, -1);
                updateView(mCalendar);
                break;
            case R.id.tv_sleep_date_next:
                if (Utils.calInterval(mCalendar.getTime(), Calendar.getInstance().getTime()) == 0) {
                    return;
                }
                mCalendar.add(Calendar.DAY_OF_MONTH, 1);
                updateView(mCalendar);
                break;
            case R.id.tv_sleep_history:
                // 打开睡眠历史
                startActivityForResult(new Intent(mainActivity, HistorySleepActivity.class), BTConstants.REQUEST_CODE_HISTORY);
                mainActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BTConstants.REQUEST_CODE_HISTORY) {
            mainActivity.mNeedRefreshData = false;
        }
    }
}
