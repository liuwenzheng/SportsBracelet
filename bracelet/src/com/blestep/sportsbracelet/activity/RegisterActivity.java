package com.blestep.sportsbracelet.activity;

import android.os.Bundle;
import android.app.Activity;

import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.base.BaseActivity;

import butterknife.ButterKnife;


public class RegisterActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_page);
        ButterKnife.bind(this);

    }

}
