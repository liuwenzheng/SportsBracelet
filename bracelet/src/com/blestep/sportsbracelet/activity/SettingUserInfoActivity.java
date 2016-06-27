package com.blestep.sportsbracelet.activity;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.base.BaseActivity;
import com.blestep.sportsbracelet.module.UnitManagerModule;
import com.blestep.sportsbracelet.utils.SPUtiles;
import com.blestep.sportsbracelet.utils.ToastUtils;
import com.blestep.sportsbracelet.utils.Utils;
import com.umeng.analytics.MobclickAgent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingUserInfoActivity extends BaseActivity {
    @Bind(R.id.et_setting_userinfo_name)
    EditText et_setting_userinfo_name;
    @Bind(R.id.rg_setting_userinfo_sex)
    RadioGroup rg_setting_userinfo_sex;
    @Bind(R.id.tv_setting_userinfo_birthday)
    TextView tv_setting_userinfo_birthday;
    @Bind(R.id.tv_setting_userinfo_height)
    TextView tv_setting_userinfo_height;
    @Bind(R.id.tv_setting_userinfo_weight)
    TextView tv_setting_userinfo_weight;
    @Bind(R.id.iv_setting_userinfo_icon)
    ImageView iv_setting_userinfo_icon;
    private DatePickerDialog mDialog;
    private Calendar mCalendar;
    private SimpleDateFormat sdf = new SimpleDateFormat(
            BTConstants.PATTERN_YYYY_MM_DD);
    private UnitManagerModule module;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_userinfo);
        ButterKnife.bind(this);
        initListener();
        initData();
    }

    private void initListener() {
        rg_setting_userinfo_sex
                .setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        switch (checkedId) {
                            case R.id.rb_userinfo_male:
                                iv_setting_userinfo_icon.setImageResource(R.drawable.pic_male);
                                break;
                            case R.id.rb_userinfo_female:
                                iv_setting_userinfo_icon.setImageResource(R.drawable.pic_female);
                                break;

                            default:
                                break;
                        }

                    }
                });
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
        module = new UnitManagerModule();
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

    @OnClick({R.id.tv_setting_userinfo_height, R.id.tv_setting_userinfo_weight, R.id.tv_setting_userinfo_birthday, R.id.btn_setting_next})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_setting_userinfo_height:
                module.createHeightDialog(this);
                break;
            case R.id.tv_setting_userinfo_weight:
                module.createWeightDialog(this);
                break;
            case R.id.tv_setting_userinfo_birthday:
                mDialog.show();
                break;
            case R.id.btn_setting_next:
                if (Utils.isEmpty(et_setting_userinfo_name.getText().toString())) {
                    ToastUtils.showToast(this,
                            R.string.setting_userinfo_name_not_null);
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
                        .valueOf(tv_setting_userinfo_height.getTag().toString()));
                SPUtiles.setIntValue(BTConstants.SP_KEY_USER_WEIGHT, Integer
                        .valueOf(tv_setting_userinfo_weight.getTag().toString()));

                SPUtiles.setIntValue(BTConstants.SP_KEY_USER_GENDER, Integer
                        .valueOf((String) findViewById(
                                rg_setting_userinfo_sex.getCheckedRadioButtonId())
                                .getTag()));
                startActivity(new Intent(this, SettingTargetActivity.class));
                this.finish();
                break;
        }
    }
}
