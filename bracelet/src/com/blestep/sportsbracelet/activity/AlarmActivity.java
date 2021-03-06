package com.blestep.sportsbracelet.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;

import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.base.BaseActivity;
import com.blestep.sportsbracelet.db.DBTools;
import com.blestep.sportsbracelet.entity.Alarm;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

public class AlarmActivity extends BaseActivity implements OnClickListener {
    private ListView lv_alarm;
    private ArrayList<Alarm> mAlarms = new ArrayList<>();
    private AlarmAdapter mAdapter;
    private String[] mAlarmTypes;
    private String[] mAlarmDates;
    private String mDefaultDatas;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_page);
        initView();
        initListener();
        initData();
    }

    @Override
    protected void onResume() {
        mAlarms = DBTools.getInstance(this).selectAllAlarm();
        mAdapter.notifyDataSetChanged();
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    private void initView() {
        lv_alarm = (ListView) findViewById(R.id.lv_alarm);
    }

    private void initListener() {
        findViewById(R.id.iv_back).setOnClickListener(this);
        findViewById(R.id.tv_alarm_edit).setOnClickListener(this);
    }

    private void initData() {
        mAlarms = DBTools.getInstance(this).selectAllAlarm();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < mAlarms.size(); i++) {
            builder.append(mAlarms.get(i));
        }
        mDefaultDatas = builder.toString();
        mAlarmTypes = getResources().getStringArray(R.array.alarm_types);
        mAlarmDates = getResources().getStringArray(R.array.alarm_period);
        mAdapter = new AlarmAdapter();
        lv_alarm.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                backToHome();
                break;
            case R.id.tv_alarm_edit:
                startActivity(new Intent(this, AlarmEditActivity.class));
                break;

            default:
                break;
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backToHome();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void backToHome() {
        if (!TextUtils.isEmpty(mDefaultDatas)) {
            mAlarms = DBTools.getInstance(this).selectAllAlarm();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < mAlarms.size(); i++) {
                builder.append(mAlarms.get(i));
            }
            if (!mDefaultDatas.equals(builder.toString())) {
                // 有值更改
                setResult(RESULT_OK);
                this.finish();
                return;
            }
        }
        setResult(RESULT_CANCELED);
        this.finish();
    }

    class AlarmAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mAlarms.size();
        }

        @Override
        public Object getItem(int position) {
            return mAlarms.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Alarm alarm = mAlarms.get(position);
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = AlarmActivity.this.getLayoutInflater().inflate(
                        R.layout.alarm_list_item, null);
                holder.tv_alarm_item_name = (TextView) convertView
                        .findViewById(R.id.tv_alarm_item_name);
                holder.tv_alarm_item_time = (TextView) convertView
                        .findViewById(R.id.tv_alarm_item_time);
                holder.tv_alarm_item_period = (TextView) convertView
                        .findViewById(R.id.tv_alarm_item_period);
                holder.tv_alarm_item_type = (TextView) convertView
                        .findViewById(R.id.tv_alarm_item_type);
                holder.cb_alarm_item_switch = (CheckBox) convertView
                        .findViewById(R.id.cb_alarm_item_switch);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tv_alarm_item_name.setText(alarm.name);
            holder.tv_alarm_item_time.setText(alarm.time);
            // 拆分state
            // 拆分周期
            final String state = alarm.state;
            StringBuilder sb = new StringBuilder();
            if ("1111111".equals(state.substring(1, state.length()))) {
                sb.append(getString(R.string.alarm_every_day));
            } else if ("11111".equals(state.substring(3, state.length()))
                    && "00".equals(state.substring(1, 3))) {
                sb.append(getString(R.string.alarm_weekdays));
            } else if ("00000".equals(state.substring(3, state.length()))
                    && "11".equals(state.substring(1, 3))) {
                sb.append(getString(R.string.alarm_weekends));
            } else {
                for (int i = 1; i < state.length(); i++) {
                    if ("1".equals(state.substring(i, i + 1))) {
                        sb.append(mAlarmDates[i - 1]);
                        if (i < state.length()) {
                            sb.append(" ");
                        }
                    }
                }
            }
            holder.tv_alarm_item_period.setText(sb.toString());
            holder.tv_alarm_item_type.setText(mAlarmTypes[Integer.parseInt(alarm.type)]);
            holder.cb_alarm_item_switch
                    .setChecked(state.substring(0, 1).equals("1") ? true : false);
            holder.cb_alarm_item_switch
                    .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                        @Override
                        public void onCheckedChanged(CompoundButton buttonView,
                                                     boolean isChecked) {
                            String checked = isChecked ? "1" : "0";
                            alarm.state = checked + state.substring(1, state.length());
                            DBTools.getInstance(AlarmActivity.this)
                                    .updateAlarm(alarm);

                        }
                    });
            return convertView;
        }

        class ViewHolder {
            TextView tv_alarm_item_name;
            TextView tv_alarm_item_time;
            TextView tv_alarm_item_period;
            TextView tv_alarm_item_type;
            CheckBox cb_alarm_item_switch;
        }
    }
}
