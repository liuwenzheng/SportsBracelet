package com.blestep.sportsbracelet.activity;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
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

public class AlarmActivity extends BaseActivity implements OnClickListener {
	private ListView lv_alarm;
	private ArrayList<Alarm> mAlarms = new ArrayList<Alarm>();
	private AlarmAdapter mAdapter;

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
		mAdapter = new AlarmAdapter();
		lv_alarm.setAdapter(mAdapter);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
		case R.id.iv_back:
			finish();
			break;
		case R.id.tv_alarm_edit:
			startActivity(new Intent(this, AlarmEditActivity.class));
			break;

		default:
			break;
		}

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
				holder.cb_alarm_item_switch = (CheckBox) convertView
						.findViewById(R.id.cb_alarm_item_switch);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tv_alarm_item_name.setText(alarm.name);
			holder.tv_alarm_item_time.setText(alarm.time);
			holder.cb_alarm_item_switch
					.setChecked(alarm.state.equals("1") ? true : false);
			holder.cb_alarm_item_switch
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							if (isChecked) {
								alarm.state = isChecked ? "1" : "0";
								DBTools.getInstance(AlarmActivity.this)
										.updateAlarm(alarm);
							}
						}
					});
			return convertView;
		}

		class ViewHolder {
			TextView tv_alarm_item_name;
			TextView tv_alarm_item_time;
			CheckBox cb_alarm_item_switch;
		}
	}
}
