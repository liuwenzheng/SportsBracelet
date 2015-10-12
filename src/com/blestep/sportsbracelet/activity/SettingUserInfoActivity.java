package com.blestep.sportsbracelet.activity;

import java.text.SimpleDateFormat;
import java.util.Calendar;

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

import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.base.BaseActivity;
import com.blestep.sportsbracelet.utils.SPUtiles;
import com.blestep.sportsbracelet.utils.ToastUtils;
import com.blestep.sportsbracelet.utils.Utils;

public class SettingUserInfoActivity extends BaseActivity implements OnClickListener, OnCheckedChangeListener {
	private EditText et_setting_userinfo_name, et_setting_userinfo_height, et_setting_userinfo_weight;
	private ImageView iv_setting_userinfo_icon;
	private RadioGroup rg_setting_userinfo_sex;
	private TextView tv_setting_userinfo_birthday;
	private Button btn_setting_next;
	private DatePickerDialog mDialog;
	private Calendar mCalendar;

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
		mDialog = new DatePickerDialog(this, R.style.AppTheme_Dialog, new OnDateSetListener() {

			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				mCalendar.set(Calendar.YEAR, year);
				mCalendar.set(Calendar.MONTH, monthOfYear);
				mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				tv_setting_userinfo_birthday.setText(sdf.format(mCalendar.getTime()));
			}
		}, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
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
				ToastUtils.showToast(this, "昵称不能为空！");
				return;
			}
			if (Utils.isEmpty(et_setting_userinfo_height.getText().toString())) {
				ToastUtils.showToast(this, "身高不能为空！");
				return;
			}
			if (Integer.valueOf(et_setting_userinfo_height.getText().toString()) < 100
					|| Integer.valueOf(et_setting_userinfo_height.getText().toString()) > 200) {
				ToastUtils.showToast(this, "身高应在100-200cm范围内");
				return;
			}
			if (Utils.isEmpty(et_setting_userinfo_weight.getText().toString())) {
				ToastUtils.showToast(this, "体重不能为空！");
				return;
			}
			if (Integer.valueOf(et_setting_userinfo_weight.getText().toString()) < 30
					|| Integer.valueOf(et_setting_userinfo_weight.getText().toString()) > 150) {
				ToastUtils.showToast(this, "体重应在30-150kg范围内");
				return;
			}
			SPUtiles.setStringValue(SPUtiles.SP_KEY_USER_NAME, et_setting_userinfo_name.getText().toString());
			SPUtiles.setIntValue(SPUtiles.SP_KEY_USER_HEIGHT,
					Integer.valueOf(et_setting_userinfo_height.getText().toString()));
			SPUtiles.setIntValue(SPUtiles.SP_KEY_USER_WEIGHT,
					Integer.valueOf(et_setting_userinfo_weight.getText().toString()));

			SPUtiles.setIntValue(SPUtiles.SP_KEY_USER_GENDER,
					Integer.valueOf((String) findViewById(rg_setting_userinfo_sex.getCheckedRadioButtonId()).getTag()));
			startActivity(new Intent(this, SettingTargetActivity.class));
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

}
