package com.blestep.sportsbracelet.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.base.BaseActivity;
import com.blestep.sportsbracelet.utils.SPUtiles;
import com.blestep.sportsbracelet.utils.Utils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserInfoLayoutActivity extends BaseActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_info_layout);
        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
        String name = SPUtiles.getStringValue(BTConstants.SP_KEY_USER_NAME, "");
        if (Utils.isEmpty(name)) {
            // 个人设置引导页
            rl_user_titlebar.setVisibility(View.GONE);
            btn_next.setVisibility(View.VISIBLE);
        } else {
            // 个人设置修改页
            rl_user_titlebar.setVisibility(View.VISIBLE);
            btn_next.setVisibility(View.GONE);
        }
        // 姓名
        et_user_name.setText(name);
        // 性别
        int gender = SPUtiles.getIntValue(BTConstants.SP_KEY_USER_GENDER, -1);
        if (gender == 0) {
            iv_user_header.setImageResource(R.drawable.user_head_male);
            iv_user_sex.setImageResource(R.drawable.user_sex_male);
            tv_user_sex.setText("男");
        } else {
            iv_user_header.setImageResource(R.drawable.user_head_female);
            iv_user_sex.setImageResource(R.drawable.user_sex_female);
            tv_user_sex.setText("女");
        }
        // 身高
    }


    @OnClick({R.id.iv_back, R.id.tv_user_confirm, R.id.iv_user_sex, R.id.iv_user_height, R.id.iv_user_weight, R.id.iv_user_birthday, R.id.btn_next})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_user_confirm:
                // TODO: 2016/7/27 返回首页 
                break;
            case R.id.iv_user_sex:
                break;
            case R.id.iv_user_height:
                break;
            case R.id.iv_user_weight:
                break;
            case R.id.iv_user_birthday:
                break;
            case R.id.btn_next:
                // TODO: 2016/7/27 跳转目标页
                break;
        }
    }
}
