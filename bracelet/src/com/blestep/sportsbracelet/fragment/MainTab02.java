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
import android.widget.TextView;

import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.activity.MainActivity;
import com.blestep.sportsbracelet.db.DBTools;
import com.blestep.sportsbracelet.entity.Sleep;
import com.blestep.sportsbracelet.module.LogModule;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainTab02 extends Fragment {

    @Bind(R.id.tv_asleep_druation)
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
    private MainActivity mainActivity;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        LogModule.i(this.getClass().getSimpleName() + "-->onActivityCreated");
        mainActivity = (MainActivity) getActivity();
        super.onActivityCreated(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View messageLayout = inflater.inflate(R.layout.main_tab_02, container,
                false);
        ButterKnife.bind(this, messageLayout);
        updateView();
        return messageLayout;
    }

    public void updateView() {
        Sleep sleep = DBTools.getInstance(mainActivity).selectCurrentSleep();
        if (sleep == null) {
            tv_asleep_druation.setText(setSleepDuration(0, 0));
            tv_awake_duration.setText(setSleepDuration(0, 0));
            tv_deep_sleep.setText(setSleepDuration(0, 0));
            tv_light_sleep.setText(setSleepDuration(0, 0));
            tv_bed_time.setText("00:00");
            tv_wake_up_time.setText("00:00");
        } else {
            int light = Integer.parseInt(sleep.light);
            int deep = Integer.parseInt(sleep.deep);
            int awake = Integer.parseInt(sleep.awake);
            int asleep = light + deep + awake;
            tv_asleep_druation.setText(setSleepDuration(asleep / 60, asleep % 60));
            tv_awake_duration.setText(setSleepDuration(awake / 60, awake % 60));
            tv_deep_sleep.setText(setSleepDuration(deep / 60, deep % 60));
            tv_light_sleep.setText(setSleepDuration(light / 60, light % 60));
            tv_bed_time.setText(sleep.start.substring(sleep.start.length() - 5, sleep.start.length()));
            tv_wake_up_time.setText(sleep.end.substring(sleep.end.length() - 5, sleep.end.length()));
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
}
