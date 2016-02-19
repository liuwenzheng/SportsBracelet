package com.blestep.sportsbracelet.activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.base.BaseActivity;
import com.blestep.sportsbracelet.utils.SPUtiles;
import com.blestep.sportsbracelet.utils.ToastUtils;
import com.blestep.sportsbracelet.utils.Utils;
import com.umeng.analytics.MobclickAgent;

public class SettingUserInfoActivity extends BaseActivity implements
		OnClickListener, OnCheckedChangeListener {
	private EditText et_setting_userinfo_name, et_setting_userinfo_height,
			et_setting_userinfo_weight;
	private ImageView iv_setting_userinfo_icon;
	private RadioGroup rg_setting_userinfo_sex;
	private TextView tv_setting_userinfo_birthday;
	private Button btn_setting_next;
	private DatePickerDialog mDialog;
	private Calendar mCalendar;
	private SimpleDateFormat sdf = new SimpleDateFormat(
			BTConstants.PATTERN_YYYY_MM_DD);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_userinfo);
		initView();
		initListener();
		initData();
	}

	private void initListener() {
		rg_setting_userinfo_sex.setOnCheckedChangeListener(this);
		tv_setting_userinfo_birthday.setOnClickListener(this);
		btn_setting_next.setOnClickListener(this);
	}

	private void initData() {
		((RadioButton) rg_setting_userinfo_sex.getChildAt(0)).setChecked(true);
		mCalendar = Calendar.getInstance();
		try {
			Date date = sdf.parse(tv_setting_userinfo_birthday.getText()
					.toString());
			mCalendar.setTime(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		mDialog = new DatePickerDialog(this, R.style.AppTheme_Dialog,
				new OnDateSetListener() {

					@Override
					public void onDateSet(DatePicker view, int year,
							int monthOfYear, int dayOfMonth) {
						mCalendar.set(Calendar.YEAR, year);
						mCalendar.set(Calendar.MONTH, monthOfYear);
						mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
						tv_setting_userinfo_birthday.setText(sdf
								.format(mCalendar.getTime()));
					}
				}, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
				mCalendar.get(Calendar.DAY_OF_MONTH));
	}

	private void initView() {
		et_setting_userinfo_name = (EditText) findViewById(R.id.et_setting_userinfo_name);
		et_setting_userinfo_height = (EditText) findViewById(R.id.et_setting_userinfo_height);
		et_setting_userinfo_weight = (EditText) findViewById(R.id.et_setting_userinfo_weight);
		rg_setting_userinfo_sex = (RadioGroup) findViewById(R.id.rg_setting_userinfo_sex);
		tv_setting_userinfo_birthday = (TextView) findViewById(R.id.tv_setting_userinfo_birthday);
		btn_setting_next = (Button) findViewById(R.id.btn_setting_next);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_setting_userinfo_birthday:
			mDialog.show();
			break;
		case R.id.btn_setting_next:
			// TODO 判断，存储，跳转
			if (Utils.isEmpty(et_setting_userinfo_name.getText().toString())) {
				ToastUtils.showToast(this,
						R.string.setting_userinfo_name_not_null);
				return;
			}
			if (Utils.isEmpty(et_setting_userinfo_height.getText().toString())) {
				ToastUtils.showToast(this,
						R.string.setting_userinfo_height_not_null);
				return;
			}
			if (Integer
					.valueOf(et_setting_userinfo_height.getText().toString()) < 100
					|| Integer.valueOf(et_setting_userinfo_height.getText()
							.toString()) > 200) {
				ToastUtils.showToast(this,
						R.string.setting_userinfo_height_size);
				return;
			}
			if (Utils.isEmpty(et_setting_userinfo_weight.getText().toString())) {
				ToastUtils.showToast(this,
						R.string.setting_userinfo_weight_not_null);
				return;
			}
			if (Integer
					.valueOf(et_setting_userinfo_weight.getText().toString()) < 30
					|| Integer.valueOf(et_setting_userinfo_weight.getText()
							.toString()) > 150) {
				ToastUtils.showToast(this,
						R.string.setting_userinfo_weight_size);
				return;
			}
			// 计算年龄
			Calendar current = Calendar.getInstance();
			try {
				Date birthday = sdf.parse(tv_setting_userinfo_birthday
						.getText().toString());
				Calendar birthdayCalendar = Calendar.getInstance();
				birthdayCalendar.setTime(birthday);
				int age = current.get(Calendar.YEAR)
						- birthdayCalendar.get(Calendar.YEAR);
				if (age < 5 || age > 99) {
					ToastUtils.showToast(this,
							R.string.setting_userinfo_age_size);
					return;
				}
				SPUtiles.setIntValue(BTConstants.SP_KEY_USER_AGE, age);
				SPUtiles.setStringValue(BTConstants.SP_KEY_USER_BIRTHDAT,
						tv_setting_userinfo_birthday.getText().toString());
			} catch (ParseException e) {
				e.printStackTrace();
			}

			SPUtiles.setStringValue(BTConstants.SP_KEY_USER_NAME,
					et_setting_userinfo_name.getText().toString());
			SPUtiles.setIntValue(BTConstants.SP_KEY_USER_HEIGHT, Integer
					.valueOf(et_setting_userinfo_height.getText().toString()));
			SPUtiles.setIntValue(BTConstants.SP_KEY_USER_WEIGHT, Integer
					.valueOf(et_setting_userinfo_weight.getText().toString()));

			SPUtiles.setIntValue(BTConstants.SP_KEY_USER_GENDER, Integer
					.valueOf((String) findViewById(
							rg_setting_userinfo_sex.getCheckedRadioButtonId())
							.getTag()));
			startActivity(new Intent(this, SettingTargetActivity.class));
			this.finish();
			break;

		default:
			break;
		}

	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.rb_setting_userinfo_male:
			break;
		case R.id.rb_setting_userinfo_female:
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
