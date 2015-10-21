package com.blestep.sportsbracelet.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.base.BaseActivity;

public class PhoneComingActivity extends BaseActivity implements OnClickListener, OnCheckedChangeListener {

	private CheckBox cb_allow_phone_coming_alert, cb_allow_phone_coming_alert_contacts,
			cb_allow_phone_coming_nodisturb_time;
	private TextView tv_coming_nodisturb_start_time, tv_coming_nodisturb_end_time;
	private LinearLayout ll_allow_phone_coming_nodisturb_time;

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
	}

	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
		case R.id.iv_back:
			finish();
			break;
		case R.id.tv_coming_nodisturb_start_time:

			break;
		case R.id.tv_coming_nodisturb_end_time:

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
			}
			break;
		case R.id.cb_allow_phone_coming_alert_contacts:

			break;
		case R.id.cb_allow_phone_coming_nodisturb_time:
			if (isChecked) {
				ll_allow_phone_coming_nodisturb_time.setVisibility(View.VISIBLE);
			} else {
				ll_allow_phone_coming_nodisturb_time.setVisibility(View.GONE);
			}
			break;

		default:
			break;
		}
	}

}
