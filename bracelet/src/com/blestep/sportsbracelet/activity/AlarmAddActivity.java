package com.blestep.sportsbracelet.activity;

import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_add_page);
        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
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
            et_alarm_add_name.setText(mAlarm.name);
            tv_alarm_add_time.setText(mAlarm.time);
        } else {
            tv_alarm_add_time.setText("00:00");
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
                if (DBTools.getInstance(this).selectAllAlarm().size() == 5) {
                    ToastUtils.showToast(this, R.string.alarm_add_count_max);
                    return;
                }
                mAlarm.name = et_alarm_add_name.getText().toString();
                mAlarm.time = tv_alarm_add_time.getText().toString();
                if (mIsEdit) {
                    DBTools.getInstance(this).updateAlarm(mAlarm);
                } else {
                    mAlarm.state = "1";
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
                break;
            case R.id.ll_alarm_add_period:
                break;
        }
    }
}
