package com.blestep.sportsbracelet.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.base.BaseActivity;
import com.umeng.analytics.MobclickAgent;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AlarmPeriodActivity extends BaseActivity {


    @Bind(R.id.cb_alarm_period_sunday)
    CheckBox cbAlarmPeriodSunday;
    @Bind(R.id.cb_alarm_period_saturday)
    CheckBox cbAlarmPeriodSaturday;
    @Bind(R.id.cb_alarm_period_friday)
    CheckBox cbAlarmPeriodFriday;
    @Bind(R.id.cb_alarm_period_thursday)
    CheckBox cbAlarmPeriodThursday;
    @Bind(R.id.cb_alarm_period_wednesday)
    CheckBox cbAlarmPeriodWednesday;
    @Bind(R.id.cb_alarm_period_tuesday)
    CheckBox cbAlarmPeriodTuesday;
    @Bind(R.id.cb_alarm_period_monday)
    CheckBox cbAlarmPeriodMonday;

    private String mPeriod;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_period_page);
        ButterKnife.bind(this);
        if (getIntent() != null && getIntent().getExtras() != null) {
            mPeriod = getIntent().getStringExtra("alarm_period");
        }
        for (int i = 1; i < mPeriod.length(); i++) {
            if ("1".equals(mPeriod.substring(i, i + 1))) {
                if (i == 1)
                    cbAlarmPeriodSunday.setChecked(true);
                if (i == 2)
                    cbAlarmPeriodSaturday.setChecked(true);
                if (i == 3)
                    cbAlarmPeriodFriday.setChecked(true);
                if (i == 4)
                    cbAlarmPeriodThursday.setChecked(true);
                if (i == 5)
                    cbAlarmPeriodWednesday.setChecked(true);
                if (i == 6)
                    cbAlarmPeriodTuesday.setChecked(true);
                if (i == 7)
                    cbAlarmPeriodMonday.setChecked(true);
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @OnClick({R.id.iv_back, R.id.tv_alarm_finish})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_alarm_finish:
                StringBuilder sb = new StringBuilder(mPeriod.substring(0, 1));
                sb.append(cbAlarmPeriodSunday.isChecked() ? "1" : "0");
                sb.append(cbAlarmPeriodSaturday.isChecked() ? "1" : "0");
                sb.append(cbAlarmPeriodFriday.isChecked() ? "1" : "0");
                sb.append(cbAlarmPeriodThursday.isChecked() ? "1" : "0");
                sb.append(cbAlarmPeriodWednesday.isChecked() ? "1" : "0");
                sb.append(cbAlarmPeriodTuesday.isChecked() ? "1" : "0");
                sb.append(cbAlarmPeriodMonday.isChecked() ? "1" : "0");
                mPeriod = sb.toString();
                Intent intent = new Intent();
                intent.putExtra("alarm_period", mPeriod);
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }
}
