package com.blestep.sportsbracelet.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.base.BaseActivity;
import com.umeng.analytics.MobclickAgent;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AlarmTypeActivity extends BaseActivity {

    @Bind(R.id.rg_alarm_type)
    RadioGroup rgAlarmType;
    private int mType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_type_page);
        ButterKnife.bind(this);
        if (getIntent() != null && getIntent().getExtras() != null) {
            mType = getIntent().getIntExtra("alarm_type", 3);
        }
        for (int i = 0; i < rgAlarmType.getChildCount(); i++) {
            if (rgAlarmType.getChildAt(i) instanceof RadioButton
                    && Integer.parseInt((String) rgAlarmType.getChildAt(i).getTag()) == mType) {
                ((RadioButton) rgAlarmType.getChildAt(i)).setChecked(true);
            }
        }
        rgAlarmType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                mType = Integer.parseInt((String) radioGroup.findViewById(i).getTag());
                Intent intent = new Intent();
                intent.putExtra("alarm_type", mType);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
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

    @OnClick(R.id.iv_back)
    public void onClick() {
        finish();
    }
}
