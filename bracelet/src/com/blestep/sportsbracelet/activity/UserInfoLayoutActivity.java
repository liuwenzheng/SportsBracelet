package com.blestep.sportsbracelet.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.base.BaseActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

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

    }

}
