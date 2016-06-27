package com.blestep.sportsbracelet.activity;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.os.Bundle;
import android.view.View;
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

public class UserInfoActivity extends BaseActivity {

    @Bind(R.id.et_userinfo_name)
    EditText et_userinfo_name;
    @Bind(R.id.rg_userinfo_sex)
    RadioGroup rg_userinfo_sex;
    @Bind(R.id.tv_userinfo_birthday)
    TextView tv_userinfo_birthday;
    @Bind(R.id.tv_userinfo_height)
    TextView tv_userinfo_height;
    @Bind(R.id.tv_height_unit)
    TextView tv_height_unit;
    @Bind(R.id.tv_userinfo_weight)
    TextView tv_userinfo_weight;
    @Bind(R.id.tv_weight_unit)
    TextView tv_weight_unit;
    @Bind(R.id.iv_userinfo_icon)
    ImageView iv_userinfo_icon;
    private DatePickerDialog mDialog;
    private Calendar mCalendar;
    private SimpleDateFormat sdf = new SimpleDateFormat(
            BTConstants.PATTERN_YYYY_MM_DD);
    private UnitManagerModule module;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userinfo_page);
        ButterKnife.bind(this);
        initListener();
        initData();
    }


    private void initListener() {
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
        module = new UnitManagerModule();
        SPUtiles.getInstance(this);
        boolean isBritish = SPUtiles.getBooleanValue(BTConstants.SP_KEY_IS_BRITISH_UNIT, false);
        int height = SPUtiles.getIntValue(BTConstants.SP_KEY_USER_HEIGHT, 175);
        int weight = SPUtiles.getIntValue(BTConstants.SP_KEY_USER_WEIGHT, 75);
        tv_userinfo_height.setTag(height + "");
        tv_userinfo_weight.setTag(weight + "");
        if (!isBritish) {
            tv_userinfo_height.setText(height + "");
            tv_height_unit.setText(getString(R.string.setting_userinfo_height_unit));
            tv_userinfo_weight.setText(weight + "");
            tv_weight_unit.setText(getString(R.string.setting_userinfo_weight_unit));
        } else {
            tv_userinfo_height.setText(String.format("%s'%s''", UnitManagerModule.cmToFt(Integer.valueOf(height)), UnitManagerModule.cmToIn(Integer.valueOf(height))));
            tv_height_unit.setText(getString(R.string.setting_userinfo_height_unit_british));
            tv_userinfo_weight.setText(UnitManagerModule.kgToLb(Integer.valueOf(weight)) + "");
            tv_weight_unit.setText(getString(R.string.setting_userinfo_weight_unit_british));
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

    @OnClick({R.id.iv_back, R.id.rg_userinfo_sex, R.id.tv_userinfo_birthday, R.id.tv_userinfo_height, R.id.tv_userinfo_weight, R.id.tv_userinfo_confirm})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                this.finish();
                break;
            case R.id.tv_userinfo_birthday:
                mDialog.show();
                break;
            case R.id.tv_userinfo_height:
                module.createHeightDialog(this);
                break;
            case R.id.tv_userinfo_weight:
                module.createWeightDialog(this);
                break;
            case R.id.tv_userinfo_confirm:
                if (Utils.isEmpty(et_userinfo_name.getText().toString())) {
                    ToastUtils.showToast(this, R.string.userinfo_name_not_null);
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
                        Integer.valueOf(tv_userinfo_height.getTag().toString()));
                SPUtiles.setIntValue(BTConstants.SP_KEY_USER_WEIGHT,
                        Integer.valueOf(tv_userinfo_height.getTag().toString()));

                SPUtiles.setIntValue(
                        BTConstants.SP_KEY_USER_GENDER,
                        Integer.valueOf((String) findViewById(
                                rg_userinfo_sex.getCheckedRadioButtonId()).getTag()));
                this.finish();
                break;
        }
    }
}
