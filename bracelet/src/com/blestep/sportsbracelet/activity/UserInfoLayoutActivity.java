package com.blestep.sportsbracelet.activity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.base.BaseActivity;
import com.blestep.sportsbracelet.module.UserUnitManagerModule;
import com.blestep.sportsbracelet.utils.SPUtiles;
import com.blestep.sportsbracelet.utils.ToastUtils;
import com.blestep.sportsbracelet.utils.Utils;
import com.blestep.sportsbracelet.view.CustomDialog;
import com.jp.wheelview.WheelView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserInfoLayoutActivity extends BaseActivity implements UserUnitManagerModule.OnUnitFinishedListener {

    @Bind(R.id.rl_user_titlebar)
    RelativeLayout rl_user_titlebar;
    @Bind(R.id.iv_back)
    ImageView iv_back;
    @Bind(R.id.tv_user_confirm)
    TextView tv_user_confirm;
    @Bind(R.id.iv_user_header)
    ImageView iv_user_header;
    @Bind(R.id.et_user_name)
    EditText et_user_name;
    @Bind(R.id.iv_user_sex)
    ImageView iv_user_sex;
    @Bind(R.id.tv_user_sex)
    TextView tv_user_sex;
    @Bind(R.id.iv_user_height)
    ImageView iv_user_height;
    @Bind(R.id.tv_user_height)
    TextView tv_user_height;
    @Bind(R.id.iv_user_weight)
    ImageView iv_user_weight;
    @Bind(R.id.tv_user_weight)
    TextView tv_user_weight;
    @Bind(R.id.iv_user_birthday)
    ImageView iv_user_birthday;
    @Bind(R.id.tv_user_birthday)
    TextView tv_user_birthday;
    @Bind(R.id.btn_next)
    Button btn_next;
    private UserUnitManagerModule module;
    private DatePickerDialog mDialog;
    private Calendar mCalendar;
    private SimpleDateFormat sdf = new SimpleDateFormat(BTConstants.PATTERN_YYYY_MM_DD);
    private String mDefaultUserInfoStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_info_layout);
        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
        String name = SPUtiles.getStringValue(BTConstants.SP_KEY_USER_NAME, "");
        // 姓名
        et_user_name.setText(name);
        if (Utils.isEmpty(name)) {
            // 个人设置引导页
            rl_user_titlebar.setVisibility(View.GONE);
            btn_next.setVisibility(View.VISIBLE);
        } else {
            // 个人设置修改页
            rl_user_titlebar.setVisibility(View.VISIBLE);
            btn_next.setVisibility(View.GONE);
            tv_user_confirm.setEnabled(isFillin());
        }
        et_user_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                setBtnEnable();
            }
        });
        // 性别
        int gender = SPUtiles.getIntValue(BTConstants.SP_KEY_USER_GENDER, -1);
        if (gender == 0) {
            iv_user_header.setImageResource(R.drawable.user_head_male);
            iv_user_sex.setImageResource(R.drawable.user_sex_male);
            tv_user_sex.setText(R.string.user_gender_male);
            tv_user_sex.setTextColor(getResources().getColor(R.color.blue_0099ff));
        } else if (gender == 1) {
            iv_user_header.setImageResource(R.drawable.user_head_female);
            iv_user_sex.setImageResource(R.drawable.user_sex_female);
            tv_user_sex.setText(R.string.user_gender_female);
            tv_user_sex.setTextColor(getResources().getColor(R.color.red_fe82b5));
        }
        module = new UserUnitManagerModule();
        boolean isBritish = SPUtiles.getBooleanValue(BTConstants.SP_KEY_IS_BRITISH_UNIT, false);
        // 身高
        int height = SPUtiles.getIntValue(BTConstants.SP_KEY_USER_HEIGHT, 0);
        if (gender == 0) {
            iv_user_height.setImageResource(R.drawable.user_height_male);
            tv_user_height.setTextColor(getResources().getColor(R.color.blue_0099ff));
        } else if (gender == 1) {
            iv_user_height.setImageResource(R.drawable.user_height_female);
            tv_user_height.setTextColor(getResources().getColor(R.color.red_fe82b5));
        }
        if (height != 0) {
            tv_user_height.setTag(height + "");
            if (isBritish) {
                tv_user_height.setText(String.format("%s'%s''%s",
                        UserUnitManagerModule.cmToFt(height),
                        UserUnitManagerModule.cmToIn(height),
                        getString(R.string.setting_userinfo_height_unit_british)));
            } else {
                tv_user_height.setText(String.format("%s%s", height,
                        getString(R.string.setting_userinfo_height_unit)));
            }
        } else {
            tv_user_height.setTag("175");
        }
        // 体重
        int weight = SPUtiles.getIntValue(BTConstants.SP_KEY_USER_WEIGHT, 0);
        if (weight != 0) {
            iv_user_weight.setImageResource(R.drawable.user_weight);
            tv_user_weight.setTag(weight + "");
            tv_user_weight.setTextColor(getResources().getColor(R.color.green_10b46c));
            if (isBritish) {
                tv_user_weight.setText(String.format("%s%s",
                        UserUnitManagerModule.kgToLb(weight),
                        getString(R.string.setting_userinfo_weight_unit_british)));
            } else {
                tv_user_weight.setText(String.format("%s%s", weight,
                        getString(R.string.setting_userinfo_weight_unit)));
            }
        } else {
            tv_user_weight.setTag("75");
        }
        // 生日
        String birthday = SPUtiles.getStringValue(BTConstants.SP_KEY_USER_BIRTHDAT, "");
        if (Utils.isNotEmpty(birthday)) {
            iv_user_birthday.setImageResource(R.drawable.user_birthday);
            tv_user_birthday.setText(birthday);
            tv_user_birthday.setTextColor(getResources().getColor(R.color.orange_ff9c00));
        } else {
            birthday = "1985-06-01";
        }
        mCalendar = Calendar.getInstance();
        Date date;
        try {
            date = sdf.parse(birthday);
            mCalendar.setTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        mDialog = new DatePickerDialog(this, R.style.AppTheme_Dialog,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        mCalendar.set(Calendar.YEAR, year);
                        mCalendar.set(Calendar.MONTH, monthOfYear);
                        mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        tv_user_birthday.setText(sdf.format(mCalendar.getTime()));
                        tv_user_birthday.setTextColor(getResources().getColor(R.color.orange_ff9c00));
                        iv_user_birthday.setImageResource(R.drawable.user_birthday);
                        SPUtiles.setStringValue(BTConstants.SP_KEY_USER_BIRTHDAT, sdf.format(mCalendar.getTime()));
                        setBtnEnable();
                    }
                }, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                mCalendar.get(Calendar.DAY_OF_MONTH));
        StringBuilder builder = new StringBuilder();
        builder.append(name).append(gender).append(weight).append(height).append(birthday);
        mDefaultUserInfoStr = builder.toString();
    }

    private void setBtnEnable() {
        btn_next.setEnabled(isFillin());
        tv_user_confirm.setEnabled(isFillin());
    }

    @OnClick({R.id.iv_back, R.id.tv_user_confirm, R.id.iv_user_sex, R.id.iv_user_height,
            R.id.iv_user_weight, R.id.iv_user_birthday, R.id.btn_next})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                backToHome();
                break;
            case R.id.tv_user_confirm:
                // 返回首页
                saveUserData();
                backToHome();
                break;
            case R.id.iv_user_sex:
                View v = getLayoutInflater().inflate(R.layout.wheelview_gender, null);
                final WheelView wv_gender = ButterKnife.findById(v, R.id.wv_gender);
                initGender(wv_gender);
                CustomDialog.Builder builder = new CustomDialog.Builder(this);
                builder.setContentView(v);
                builder.setTitle(getString(R.string.user_gender));
                builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        int gender = wv_gender.getSelected();
                        tv_user_sex.setText(wv_gender.getSelectedText());
                        if (gender == 0) {
                            iv_user_header.setImageResource(R.drawable.user_head_male);
                            iv_user_sex.setImageResource(R.drawable.user_sex_male);
                            tv_user_sex.setTextColor(getResources().getColor(R.color.blue_0099ff));
                            iv_user_height.setImageResource(R.drawable.user_height_male);
                            tv_user_height.setTextColor(getResources().getColor(R.color.blue_0099ff));
                        } else if (gender == 1) {
                            iv_user_header.setImageResource(R.drawable.user_head_female);
                            iv_user_sex.setImageResource(R.drawable.user_sex_female);
                            tv_user_sex.setTextColor(getResources().getColor(R.color.red_fe82b5));
                            iv_user_height.setImageResource(R.drawable.user_height_female);
                            tv_user_height.setTextColor(getResources().getColor(R.color.red_fe82b5));
                        }
                        SPUtiles.setIntValue(BTConstants.SP_KEY_USER_GENDER, gender);
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                break;
            case R.id.iv_user_height:
                module.createHeightDialog(this);
                break;
            case R.id.iv_user_weight:
                module.createWeightDialog(this);
                break;
            case R.id.iv_user_birthday:
                mDialog.show();
                break;
            case R.id.btn_next:
                // 跳转目标页
                saveUserData();
                startActivity(new Intent(this, TargetLayoutActivity.class));
                break;
        }
    }

    private void saveUserData() {
        // 计算年龄
        Calendar current = Calendar.getInstance();
        try {
            Date birthday = sdf.parse(tv_user_birthday.getText().toString());
            Calendar birthdayCalendar = Calendar.getInstance();
            birthdayCalendar.setTime(birthday);
            int age = current.get(Calendar.YEAR) - birthdayCalendar.get(Calendar.YEAR);
            if (age < 5 || age > 99) {
                ToastUtils.showToast(this, R.string.userinfo_age_size);
                return;
            }
            SPUtiles.setIntValue(BTConstants.SP_KEY_USER_AGE, age);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SPUtiles.setStringValue(BTConstants.SP_KEY_USER_NAME, et_user_name.getText().toString());
    }


    private void initGender(WheelView wv_gender) {
        wv_gender.setData(createGender());
        wv_gender.setDefault(SPUtiles.getIntValue(BTConstants.SP_KEY_USER_GENDER, 0));
    }

    private ArrayList<String> createGender() {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i <= 1; i++) {
            list.add(i == 0 ? getString(R.string.user_gender_male) : getString(R.string.user_gender_female));
        }
        return list;
    }

    private boolean isFillin() {
        if (Utils.isNotEmpty(SPUtiles.getStringValue(BTConstants.SP_KEY_USER_BIRTHDAT, ""))
                && SPUtiles.getIntValue(BTConstants.SP_KEY_USER_WEIGHT, 0) != 0
                && SPUtiles.getIntValue(BTConstants.SP_KEY_USER_HEIGHT, 0) != 0
                && SPUtiles.getIntValue(BTConstants.SP_KEY_USER_GENDER, -1) != -1
                && Utils.isNotEmpty(et_user_name.getText().toString())) {
            return true;
        }
        return false;
    }

    @Override
    public void heightFinished() {
        SPUtiles.setIntValue(BTConstants.SP_KEY_USER_HEIGHT, Integer.parseInt(tv_user_height.getTag().toString()));
        setBtnEnable();
    }

    @Override
    public void weightFinished() {
        iv_user_weight.setImageResource(R.drawable.user_weight);
        tv_user_weight.setTextColor(getResources().getColor(R.color.green_10b46c));
        SPUtiles.setIntValue(BTConstants.SP_KEY_USER_WEIGHT, Integer.parseInt(tv_user_weight.getTag().toString()));
        setBtnEnable();
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
        String name = SPUtiles.getStringValue(BTConstants.SP_KEY_USER_NAME, "");
        int gender = SPUtiles.getIntValue(BTConstants.SP_KEY_USER_GENDER, -1);
        int height = SPUtiles.getIntValue(BTConstants.SP_KEY_USER_HEIGHT, 0);
        int weight = SPUtiles.getIntValue(BTConstants.SP_KEY_USER_WEIGHT, 0);
        String birthday = SPUtiles.getStringValue(BTConstants.SP_KEY_USER_BIRTHDAT, "");
        StringBuilder builder = new StringBuilder();
        builder.append(name).append(gender).append(weight).append(height).append(birthday);
        if (builder.toString().equals(mDefaultUserInfoStr)) {
            setResult(RESULT_CANCELED);
            this.finish();
        } else {
            // 有值更改
            setResult(RESULT_OK);
            this.finish();
        }

    }
}
