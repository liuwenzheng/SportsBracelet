package com.blestep.sportsbracelet.activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
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
import com.blestep.sportsbracelet.module.LogModule;
import com.blestep.sportsbracelet.service.BTService;
import com.blestep.sportsbracelet.service.BTService.LocalBinder;
import com.blestep.sportsbracelet.utils.SPUtiles;
import com.blestep.sportsbracelet.utils.ToastUtils;
import com.blestep.sportsbracelet.utils.Utils;
import com.umeng.analytics.MobclickAgent;

public class UserInfoActivity extends BaseActivity implements OnClickListener {

	private BTService mBtService;
	private TextView tv_userinfo_confirm, tv_userinfo_birthday;
	private ImageView iv_userinfo_icon;
	private EditText et_userinfo_name, et_userinfo_height, et_userinfo_weight;
	private RadioGroup rg_userinfo_sex;
	private DatePickerDialog mDialog;
	private Calendar mCalendar;
	private SimpleDateFormat sdf = new SimpleDateFormat(
			BTConstants.PATTERN_YYYY_MM_DD);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.userinfo_page);
		initView();
		initListener();
		initData();
	}

	private void initView() {
		tv_userinfo_confirm = (TextView) findViewById(R.id.tv_userinfo_confirm);
		tv_userinfo_birthday = (TextView) findViewById(R.id.tv_userinfo_birthday);
		iv_userinfo_icon = (ImageView) findViewById(R.id.iv_userinfo_icon);
		et_userinfo_name = (EditText) findViewById(R.id.et_userinfo_name);
		et_userinfo_height = (EditText) findViewById(R.id.et_userinfo_height);
		et_userinfo_weight = (EditText) findViewById(R.id.et_userinfo_weight);
		rg_userinfo_sex = (RadioGroup) findViewById(R.id.rg_userinfo_sex);
	}

	private void initListener() {
		tv_userinfo_birthday.setOnClickListener(this);
		findViewById(R.id.iv_back).setOnClickListener(this);
		tv_userinfo_confirm.setOnClickListener(this);
		rg_userinfo_sex
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						switch (checkedId) {
						case R.id.rb_userinfo_male:
							iv_userinfo_icon
									.setImageResource(R.drawable.pic_male);
							break;
						case R.id.rb_userinfo_female:
							iv_userinfo_icon
									.setImageResource(R.drawable.pic_female);
							break;

						default:
							break;
						}

					}
				});
	}

	private void initData() {
		bindService(new Intent(this, BTService.class), mServiceConnection,
				BIND_AUTO_CREATE);
		et_userinfo_name.setText(SPUtiles.getStringValue(
				BTConstants.SP_KEY_USER_NAME, ""));
		int gender = SPUtiles.getIntValue(BTConstants.SP_KEY_USER_GENDER, 0);
		if (gender == 0) {
			iv_userinfo_icon.setImageResource(R.drawable.pic_male);
			((RadioButton) rg_userinfo_sex.getChildAt(0)).setChecked(true);
		} else {
			iv_userinfo_icon.setImageResource(R.drawable.pic_female);
			((RadioButton) rg_userinfo_sex.getChildAt(1)).setChecked(true);
		}

		tv_userinfo_birthday.setText(SPUtiles.getStringValue(
				BTConstants.SP_KEY_USER_BIRTHDAT, "1989-01-01"));
		mCalendar = Calendar.getInstance();
		Date date;
		try {
			date = sdf.parse(tv_userinfo_birthday.getText().toString());
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
						tv_userinfo_birthday.setText(sdf.format(mCalendar
								.getTime()));
					}
				}, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
				mCalendar.get(Calendar.DAY_OF_MONTH));
		et_userinfo_height.setText(SPUtiles.getIntValue(
				BTConstants.SP_KEY_USER_HEIGHT, 175) + "");
		et_userinfo_weight.setText(SPUtiles.getIntValue(
				BTConstants.SP_KEY_USER_WEIGHT, 75) + "");
	}

	@Override
	protected void onDestroy() {
		unbindService(mServiceConnection);
		super.onDestroy();
	}

	private ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LogModule.d("连接服务onServiceConnected...");
			mBtService = ((LocalBinder) service).getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			LogModule.d("断开服务onServiceDisconnected...");
			mBtService = null;
		}
	};

	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
		case R.id.iv_back:
			this.finish();
			break;
		case R.id.tv_userinfo_confirm:
			if (Utils.isEmpty(et_userinfo_name.getText().toString())) {
				ToastUtils.showToast(this, R.string.userinfo_name_not_null);
				return;
			}
			if (Utils.isEmpty(et_userinfo_height.getText().toString())) {
				ToastUtils.showToast(this, R.string.userinfo_height_not_null);
				return;
			}
			if (Integer.valueOf(et_userinfo_height.getText().toString()) < 100
					|| Integer.valueOf(et_userinfo_height.getText().toString()) > 200) {
				ToastUtils.showToast(this, R.string.userinfo_height_size);
				return;
			}
			if (Utils.isEmpty(et_userinfo_weight.getText().toString())) {
				ToastUtils.showToast(this, R.string.userinfo_weight_not_null);
				return;
			}
			if (Integer.valueOf(et_userinfo_weight.getText().toString()) < 30
					|| Integer.valueOf(et_userinfo_weight.getText().toString()) > 150) {
				ToastUtils.showToast(this, R.string.userinfo_weight_size);
				return;
			}
			// 计算年龄
			Calendar current = Calendar.getInstance();
			try {
				Date birthday = sdf.parse(tv_userinfo_birthday.getText()
						.toString());
				Calendar birthdayCalendar = Calendar.getInstance();
				birthdayCalendar.setTime(birthday);
				int age = current.get(Calendar.YEAR)
						- birthdayCalendar.get(Calendar.YEAR);
				if (age < 5 || age > 99) {
					ToastUtils.showToast(this, R.string.userinfo_age_size);
					return;
				}
				SPUtiles.setIntValue(BTConstants.SP_KEY_USER_AGE, age);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			SPUtiles.setStringValue(BTConstants.SP_KEY_USER_NAME,
					et_userinfo_name.getText().toString());
			SPUtiles.setIntValue(BTConstants.SP_KEY_USER_HEIGHT,
					Integer.valueOf(et_userinfo_height.getText().toString()));
			SPUtiles.setIntValue(BTConstants.SP_KEY_USER_WEIGHT,
					Integer.valueOf(et_userinfo_weight.getText().toString()));

			SPUtiles.setIntValue(
					BTConstants.SP_KEY_USER_GENDER,
					Integer.valueOf((String) findViewById(
							rg_userinfo_sex.getCheckedRadioButtonId()).getTag()));
			this.finish();
			break;
		case R.id.tv_userinfo_birthday:
			mDialog.show();
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
