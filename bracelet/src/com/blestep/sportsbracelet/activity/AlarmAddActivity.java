package com.blestep.sportsbracelet.activity;

import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.base.BaseActivity;
import com.blestep.sportsbracelet.db.DBTools;
import com.blestep.sportsbracelet.entity.Alarm;
import com.blestep.sportsbracelet.utils.ToastUtils;
import com.blestep.sportsbracelet.utils.Utils;
import com.umeng.analytics.MobclickAgent;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AlarmAddActivity extends BaseActivity {

    @Bind(R.id.et_alarm_add_name)
    EditText et_alarm_add_name;
    @Bind(R.id.tv_alarm_add_type)
    TextView tv_alarm_add_type;
    @Bind(R.id.tv_alarm_add_period)
    TextView tv_alarm_add_period;
    @Bind(R.id.tv_alarm_add_time)
    TextView tv_alarm_add_time;
    private Alarm mAlarm;
    private boolean mIsEdit = false;
    private TimePickerDialog mDialog;
    private Calendar mCalendar = Calendar.getInstance();
    private SimpleDateFormat mSdf;
    public static final int REQUEST_CODE_TYPE = 100;
    public static final int REQUEST_CODE_PERIOD = 101;
    private int mType;
    private String[] mAlarmTypes;
    private String mPeriod;
    private String[] mAlarmDates;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_add_page);
        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
        mAlarmTypes = getResources().getStringArray(R.array.alarm_types);
        mAlarmDates = getResources().getStringArray(R.array.alarm_period);
        mSdf = new SimpleDateFormat(BTConstants.PATTERN_HH_MM);
        if (getIntent() != null && getIntent().getExtras() != null) {
            mAlarm = (Alarm) getIntent().getExtras().getSerializable(
                    BTConstants.EXTRA_KEY_ALARM);
            mIsEdit = true;
        } else {
            mAlarm = new Alarm();
            mIsEdit = false;
        }
        if (mIsEdit) {
            mType = Integer.parseInt(mAlarm.type);
            mPeriod = mAlarm.state;
            et_alarm_add_name.setText(mAlarm.name);
            tv_alarm_add_time.setText(mAlarm.time);
        } else {
            mType = 3;
            mPeriod = "11111111";
            et_alarm_add_name.setText(getString(R.string.alarm_title));
            tv_alarm_add_time.setText(mSdf.format(Calendar.getInstance().getTime()));
        }
        setPeriodText();
        tv_alarm_add_type.setText(mAlarmTypes[mType]);
    }

    private void setPeriodText() {
        StringBuilder sb = new StringBuilder();
        if ("1111111".equals(mPeriod.substring(1, mPeriod.length()))) {
            sb.append(getString(R.string.alarm_every_day));
        } else if ("11111".equals(mPeriod.substring(3, mPeriod.length()))
                && "00".equals(mPeriod.substring(1, 3))) {
            sb.append(getString(R.string.alarm_weekdays));
        } else if ("00000".equals(mPeriod.substring(3, mPeriod.length()))
                && "11".equals(mPeriod.substring(1, 3))) {
            sb.append(getString(R.string.alarm_weekends));
        } else {
            for (int i = 1; i < mPeriod.length(); i++) {
                if ("1".equals(mPeriod.substring(i, i + 1))) {
                    sb.append(mAlarmDates[i - 1]);
                    if (i < mPeriod.length()) {
                        sb.append(" ");
                    }
                }
            }
        }
        tv_alarm_add_period.setText(sb.toString());
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

    @OnClick({R.id.iv_back, R.id.tv_alarm_finish, R.id.tv_alarm_add_time, R.id.ll_alarm_add_type, R.id.ll_alarm_add_period})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_alarm_finish:
                if (Utils.isEmpty(et_alarm_add_name.getText().toString())) {
                    ToastUtils.showToast(this, R.string.alarm_add_name_null);
                    return;
                }
                if (DBTools.getInstance(this).selectAllAlarm().size() == 8) {
                    ToastUtils.showToast(this, R.string.alarm_add_count_max);
                    return;
                }
                mAlarm.name = et_alarm_add_name.getText().toString();
                mAlarm.time = tv_alarm_add_time.getText().toString();
                mAlarm.type = String.valueOf(mType);
                mAlarm.state = mPeriod;
                if (mIsEdit) {
                    DBTools.getInstance(this).updateAlarm(mAlarm);
                } else {
                    DBTools.getInstance(this).insertAlarm(mAlarm);
                }
                ToastUtils.showToast(this, R.string.alarm_add_success);
                finish();
                break;
            case R.id.tv_alarm_add_time:
                String alarm_time = tv_alarm_add_time.getText().toString();
                if (Utils.isNotEmpty(alarm_time)) {
                    mDialog = new TimePickerDialog(this, R.style.AppTheme_Dialog,
                            new OnTimeSetListener() {

                                @Override
                                public void onTimeSet(TimePicker view,
                                                      int hourOfDay, int minute) {
                                    mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    mCalendar.set(Calendar.MINUTE, minute);
                                    tv_alarm_add_time.setText(mSdf.format(mCalendar
                                            .getTime()));
                                    mDialog.dismiss();
                                }
                            }, Integer.parseInt(alarm_time.split(":")[0]),
                            Integer.parseInt(alarm_time.split(":")[1]), true);
                    mDialog.show();
                }
                break;
            case R.id.ll_alarm_add_type:
                Intent intent = new Intent(this, AlarmTypeActivity.class);
                intent.putExtra("alarm_type", mType);
                startActivityForResult(intent, REQUEST_CODE_TYPE);
                break;
            case R.id.ll_alarm_add_period:
                Intent i = new Intent(this, AlarmPeriodActivity.class);
                i.putExtra("alarm_period", mPeriod);
                startActivityForResult(i, REQUEST_CODE_PERIOD);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_TYPE:
                    mType = data.getIntExtra("alarm_type", 3);
                    tv_alarm_add_type.setText(mAlarmTypes[mType]);
                    break;
                case REQUEST_CODE_PERIOD:
                    mPeriod = data.getStringExtra("alarm_period");
                    setPeriodText();
                    break;
            }
        }
    }
}
