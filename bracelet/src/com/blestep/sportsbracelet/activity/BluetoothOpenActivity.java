package com.blestep.sportsbracelet.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.base.BaseActivity;
import com.blestep.sportsbracelet.module.BTModule;
import com.blestep.sportsbracelet.view.BottomNavView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BluetoothOpenActivity extends BaseActivity {

    @Bind(R.id.rl_bluetooth_ring)
    RelativeLayout rl_bluetooth_ring;
    @Bind(R.id.iv_bluetooth_icon)
    ImageView iv_bluetooth_icon;
    @Bind(R.id.tv_open_bluetooth)
    TextView tv_open_bluetooth;
    @Bind(R.id.bnv_nav)
    BottomNavView bnv_nav;
    @Bind(R.id.tv_bluetooth_status)
    TextView tv_bluetooth_status;
    @Bind(R.id.tv_bluetooth_desc)
    TextView tv_bluetooth_desc;

    private BluetoothReceiver mReceiver;

    class BluetoothReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        setBluetoothClose();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        setBluetoothOpen();
                        break;
                }
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_open_layout);
        ButterKnife.bind(this);
        bnv_nav.setPreEnable(true);
        bnv_nav.setListener(this);
        if (BTModule.isBluetoothOpen()) {
            setBluetoothOpen();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        mReceiver = new BluetoothReceiver();
        registerReceiver(mReceiver, filter);
    }

    private void setBluetoothClose() {
        tv_bluetooth_status.setText("蓝牙未开启");
        tv_bluetooth_desc.setText("您的手机蓝牙未打开，请打开后点击下一步");
        rl_bluetooth_ring.setBackgroundResource(R.drawable.bluetooth_ring_close);
        iv_bluetooth_icon.setImageResource(R.drawable.bluetooth_close);
        tv_open_bluetooth.setVisibility(View.VISIBLE);
        bnv_nav.setNextEnable(false);
    }

    private void setBluetoothOpen() {
        tv_bluetooth_status.setText("蓝牙开启");
        tv_bluetooth_desc.setText("在手机设置中，打开 蓝牙设备,以便可以与手环进行连接");
        rl_bluetooth_ring.setBackgroundResource(R.drawable.bluetooth_ring_open);
        iv_bluetooth_icon.setImageResource(R.drawable.bluetooth_open);
        tv_open_bluetooth.setVisibility(View.GONE);
        bnv_nav.setNextEnable(true);
    }

    @Override
    public void onNextClick() {
        super.onNextClick();
        startActivity(new Intent(this, MatchDevicesActivity.class));
    }

    @Override
    public void onPreClick() {
        super.onPreClick();
        finish();
    }

    @OnClick(R.id.rl_bluetooth_ring)
    public void onClick() {
        if (!BTModule.isBluetoothOpen()) {
            BTModule.openBluetooth(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
