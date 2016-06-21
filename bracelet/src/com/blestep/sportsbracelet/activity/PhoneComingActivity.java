package com.blestep.sportsbracelet.activity;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.base.BaseActivity;
import com.blestep.sportsbracelet.utils.SPUtiles;
import com.blestep.sportsbracelet.utils.Utils;
import com.umeng.analytics.MobclickAgent;

public class PhoneComingActivity extends BaseActivity implements
		OnClickListener, OnCheckedChangeListener {

	private CheckBox cb_allow_phone_coming_alert,
			cb_allow_phone_coming_alert_contacts,
			cb_allow_phone_coming_nodisturb_time;
	private TextView tv_coming_nodisturb_start_time,
			tv_coming_nodisturb_end_time;
	private LinearLayout ll_allow_phone_coming_nodisturb_time;
	private TimePickerDialog mDialog;
	private Calendar mCalendar = Calendar.getInstance();
	private SimpleDateFormat mSdf;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.phone_coming_page);
		initView();
		initListener();
		initData();
	}

	private void initView() {
		findViewById(R.id.iv_back).setOnClickListener(this);
		cb_allow_phone_coming_alert = (CheckBox) findViewById(R.id.cb_allow_phone_coming_alert);
		cb_allow_phone_coming_alert_contacts = (CheckBox) findViewById(R.id.cb_allow_phone_coming_alert_contacts);
		cb_allow_phone_coming_nodisturb_time = (CheckBox) findViewById(R.id.cb_allow_phone_coming_nodisturb_time);
		tv_coming_nodisturb_start_time = (TextView) findViewById(R.id.tv_coming_nodisturb_start_time);
		tv_coming_nodisturb_end_time = (TextView) findViewById(R.id.tv_coming_nodisturb_end_time);
		ll_allow_phone_coming_nodisturb_time = (LinearLayout) findViewById(R.id.ll_allow_phone_coming_nodisturb_time);
		if (cb_allow_phone_coming_nodisturb_time.isChecked()) {
			ll_allow_phone_coming_nodisturb_time.setVisibility(View.VISIBLE);
		} else {
			ll_allow_phone_coming_nodisturb_time.setVisibility(View.GONE);
		}
		if (cb_allow_phone_coming_alert.isChecked()) {
			cb_allow_phone_coming_alert_contacts.setEnabled(true);
			cb_allow_phone_coming_nodisturb_time.setEnabled(true);
		} else {
			cb_allow_phone_coming_alert_contacts.setEnabled(false);
			cb_allow_phone_coming_nodisturb_time.setEnabled(false);
		}
	}

	private void initListener() {
		cb_allow_phone_coming_alert.setOnCheckedChangeListener(this);
		cb_allow_phone_coming_alert_contacts.setOnCheckedChangeListener(this);
		cb_allow_phone_coming_nodisturb_time.setOnCheckedChangeListener(this);
		tv_coming_nodisturb_start_time.setOnClickListener(this);
		tv_coming_nodisturb_end_time.setOnClickListener(this);
	}

	private void initData() {
		mSdf = new SimpleDateFormat(BTConstants.PATTERN_HH_MM);
		cb_allow_phone_coming_alert.setChecked(SPUtiles.getBooleanValue(
				BTConstants.SP_KEY_COMING_PHONE_ALERT, true));
		cb_allow_phone_coming_alert_contacts.setChecked(SPUtiles
				.getBooleanValue(
						BTConstants.SP_KEY_COMING_PHONE_CONTACTS_ALERT, false));
		cb_allow_phone_coming_nodisturb_time
				.setChecked(SPUtiles.getBooleanValue(
						BTConstants.SP_KEY_COMING_PHONE_NODISTURB_ALERT, false));
		tv_coming_nodisturb_start_time.setText(SPUtiles.getStringValue(
				BTConstants.SP_KEY_COMING_PHONE_NODISTURB_START_TIME, "00:00"));
		tv_coming_nodisturb_end_time.setText(SPUtiles.getStringValue(
				BTConstants.SP_KEY_COMING_PHONE_NODISTURB_END_TIME, "00:00"));

	}

	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
		case R.id.iv_back:
			finish();
			break;
		case R.id.tv_coming_nodisturb_start_time:
			String start_time = tv_coming_nodisturb_start_time.getText()
					.toString();
			if (Utils.isNotEmpty(start_time)) {
				mDialog = new TimePickerDialog(
						this,
						R.style.AppTheme_Dialog,
						new OnTimeSetListener() {

							@Override
							public void onTimeSet(TimePicker view,
									int hourOfDay, int minute) {
								mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
								mCalendar.set(Calendar.MINUTE, minute);
								tv_coming_nodisturb_start_time.setText(mSdf
										.format(mCalendar.getTime()));
								SPUtiles.setStringValue(
										BTConstants.SP_KEY_COMING_PHONE_NODISTURB_START_TIME,
										mSdf.format(mCalendar.getTime()));
								mDialog.dismiss();
							}
						}, Integer.valueOf(start_time.split(":")[0]),
						Integer.valueOf(start_time.split(":")[1]), true);
				mDialog.show();
			}
			break;
		case R.id.tv_coming_nodisturb_end_time:
			String end_time = tv_coming_nodisturb_end_time.getText().toString();
			mDialog = new TimePickerDialog(
					this,
					R.style.AppTheme_Dialog,
					new OnTimeSetListener() {

						@Override
						public void onTimeSet(TimePicker view, int hourOfDay,
								int minute) {
							mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
							mCalendar.set(Calendar.MINUTE, minute);
							tv_coming_nodisturb_end_time.setText(mSdf
									.format(mCalendar.getTime()));
							SPUtiles.setStringValue(
									BTConstants.SP_KEY_COMING_PHONE_NODISTURB_END_TIME,
									mSdf.format(mCalendar.getTime()));
							mDialog.dismiss();
						}
					}, Integer.valueOf(end_time.split(":")[0]),
					Integer.valueOf(end_time.split(":")[1]), true);
			mDialog.show();
			break;

		default:
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.cb_allow_phone_coming_alert:
			if (isChecked) {
				cb_allow_phone_coming_alert_contacts.setEnabled(true);
				cb_allow_phone_coming_nodisturb_time.setEnabled(true);
			} else {
				cb_allow_phone_coming_alert_contacts.setEnabled(false);
				cb_allow_phone_coming_nodisturb_time.setEnabled(false);
				cb_allow_phone_coming_alert_contacts.setChecked(isChecked);
				cb_allow_phone_coming_nodisturb_time.setChecked(isChecked);
				SPUtiles.setBooleanValue(
						BTConstants.SP_KEY_COMING_PHONE_CONTACTS_ALERT,
						isChecked);
				SPUtiles.setBooleanValue(
						BTConstants.SP_KEY_COMING_PHONE_NODISTURB_ALERT,
						isChecked);
			}
			SPUtiles.setBooleanValue(BTConstants.SP_KEY_COMING_PHONE_ALERT,
					isChecked);
			break;
		case R.id.cb_allow_phone_coming_alert_contacts:
			SPUtiles.setBooleanValue(
					BTConstants.SP_KEY_COMING_PHONE_CONTACTS_ALERT, isChecked);
			break;
		case R.id.cb_allow_phone_coming_nodisturb_time:
			if (isChecked) {
				ll_allow_phone_coming_nodisturb_time
						.setVisibility(View.VISIBLE);
			} else {
				ll_allow_phone_coming_nodisturb_time.setVisibility(View.GONE);
			}
			SPUtiles.setBooleanValue(
					BTConstants.SP_KEY_COMING_PHONE_NODISTURB_ALERT, isChecked);
			break;

		default:
			break;
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
}
