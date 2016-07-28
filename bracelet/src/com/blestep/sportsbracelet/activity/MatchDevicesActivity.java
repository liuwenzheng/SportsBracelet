package com.blestep.sportsbracelet.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.base.BaseActivity;
import com.blestep.sportsbracelet.entity.BleDevice;
import com.blestep.sportsbracelet.module.BTModule;
import com.blestep.sportsbracelet.module.LogModule;
import com.blestep.sportsbracelet.service.BTService;
import com.blestep.sportsbracelet.utils.SPUtiles;
import com.blestep.sportsbracelet.utils.ToastUtils;
import com.blestep.sportsbracelet.utils.Utils;
import com.blestep.sportsbracelet.view.BottomNavView;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class MatchDevicesActivity extends BaseActivity {

    @Bind(R.id.tv_match_tips_1)
    TextView tv_match_tips_1;
    @Bind(R.id.tv_match_tips_2)
    TextView tv_match_tips_2;
    @Bind(R.id.tv_match_tips_3)
    TextView tv_match_tips_3;
    @Bind(R.id.tv_match_tips_4)
    TextView tv_match_tips_4;
    @Bind(R.id.rl_match_auto)
    RelativeLayout rl_match_auto;
    @Bind(R.id.iv_match_loading)
    ImageView iv_match_loading;
    @Bind(R.id.bnv_nav)
    BottomNavView bnv_nav;
    @Bind(R.id.frame_match_loading)
    FrameLayout frame_match_loading;
    @Bind(R.id.lv_match_devices)
    ListView lv_match_devices;
    @Bind(R.id.tv_match_tips_failure)
    TextView tv_match_tips_failure;

    private BTService mBtService;
    private DeviceAdapter mAdapter;
    private ArrayList<BleDevice> mDevices;
    private ProgressDialog mDialog;
    private int mPosition = -1;
    private int mScanTimes = 0;
    private boolean mIsScanContinue = false;
    private BleDevice mScanDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.match_devices_layout);
        ButterKnife.bind(this);
        bnv_nav.setPreEnable(true);
        bnv_nav.setNextText("重试");
        bnv_nav.setListener(this);
        mDevices = new ArrayList<>();
        mAdapter = new DeviceAdapter();
        lv_match_devices.setAdapter(mAdapter);
        bindService(new Intent(this, BTService.class), mServiceConnection, BIND_AUTO_CREATE);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BTConstants.ACTION_BLE_DEVICES_DATA);
        filter.addAction(BTConstants.ACTION_BLE_DEVICES_DATA_END);
        filter.addAction(BTConstants.ACTION_CONN_STATUS_TIMEOUT);
        filter.addAction(BTConstants.ACTION_CONN_STATUS_DISCONNECTED);
        filter.addAction(BTConstants.ACTION_DISCOVER_SUCCESS);
        filter.addAction(BTConstants.ACTION_DISCOVER_FAILURE);
        registerReceiver(mReceiver, filter);
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

    @Override
    protected void onDestroy() {
        // 注销广播接收器
        unregisterReceiver(mReceiver);
        unbindService(mServiceConnection);
        super.onDestroy();
    }

    @OnItemClick(R.id.lv_match_devices)
    public void onItemClick(int position) {
        mPosition = position;
        for (int i = 0; i < mDevices.size(); i++) {
            mDevices.get(i).isChecked = false;
        }
        mDevices.get(position).isChecked = true;
        mAdapter.notifyDataSetChanged();
        // 开始配对
        if (mDevices != null && mDevices.size() == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.setting_device_search_repeat);
            builder.setPositiveButton(R.string.setting_device_search_confirm,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            showMatchAutoUI();
                            startScanDevice();
                            dialog.dismiss();
                        }
                    });
            builder.show();
            return;
        }
        if (mPosition == -1) {
            ToastUtils.showToast(this, R.string.setting_device_select_tips);
            return;
        }
        LogModule.i("选中设备mac地址:" + mDevices.get(mPosition).address);
        // 将选中地址缓存
        mBtService.connectBle(mDevices.get(mPosition).address);
        mDialog = ProgressDialog.show(MatchDevicesActivity.this, null, getString(R.string.setting_device), false, false);
    }

    @Override
    public void onNextClick() {
        super.onNextClick();
        showMatchAutoUI();
        startScanDevice();
    }

    @Override
    public void onPreClick() {
        super.onPreClick();
        finish();
    }

    @OnClick(R.id.rl_match_auto)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_match_auto:
                // 开始配对
                showMatchAutoUI();
                startScanDevice();
                break;
        }
    }

    private void showInitUI() {
        rl_match_auto.setVisibility(View.VISIBLE);
        frame_match_loading.setVisibility(View.GONE);
        tv_match_tips_1.setVisibility(View.VISIBLE);
        tv_match_tips_2.setVisibility(View.VISIBLE);
        tv_match_tips_3.setVisibility(View.VISIBLE);
        tv_match_tips_4.setVisibility(View.VISIBLE);
        tv_match_tips_failure.setVisibility(View.GONE);
        bnv_nav.setNextEnable(false);
        lv_match_devices.setVisibility(View.GONE);
    }

    private void showMatchAutoUI() {
        rl_match_auto.setVisibility(View.GONE);
        frame_match_loading.setVisibility(View.VISIBLE);
        tv_match_tips_1.setVisibility(View.VISIBLE);
        tv_match_tips_2.setVisibility(View.VISIBLE);
        tv_match_tips_3.setVisibility(View.GONE);
        tv_match_tips_4.setVisibility(View.GONE);
        tv_match_tips_failure.setVisibility(View.GONE);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.match_loading_rotate);
        iv_match_loading.setAnimation(animation);
        animation.start();
        bnv_nav.setNextEnable(false);
        lv_match_devices.setVisibility(View.GONE);
    }

    private void showMatchManualUI() {
        for (int i = 0; i < mDevices.size(); i++) {
            mDevices.get(i).isChecked = false;
        }
        mAdapter.notifyDataSetChanged();
        frame_match_loading.setVisibility(View.GONE);
        tv_match_tips_1.setVisibility(View.GONE);
        tv_match_tips_2.setVisibility(View.GONE);
        tv_match_tips_3.setVisibility(View.GONE);
        tv_match_tips_4.setVisibility(View.GONE);
        tv_match_tips_failure.setVisibility(View.VISIBLE);
        lv_match_devices.setVisibility(View.VISIBLE);
        bnv_nav.setNextEnable(true);
    }

    private void startScanDevice() {
        mScanTimes = 0;
        mIsScanContinue = false;
        mScanDevice = null;
        LogModule.d("开始扫描..." + mScanTimes);
        mBtService.scanDevice();
        mScanTimes++;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (BTConstants.ACTION_BLE_DEVICES_DATA.equals(intent.getAction())) {
                    BleDevice bleDevice = (BleDevice) intent.getExtras().getSerializable("device");
                    for (BleDevice device : mDevices) {
                        if (device.address.equals(bleDevice.address)) {
                            return;
                        }
                    }

                    mDevices.add(bleDevice);
                    Collections.sort(mDevices);
                    if (!mIsScanContinue) {
                        mAdapter.notifyDataSetChanged();
                    }
                    // 判断是否有正在敲击的手环，有则开始配对
                    if (Utils.isNotEmpty(bleDevice.name) && bleDevice.name.lastIndexOf("-D") > 0) {
                        LogModule.d("可以配对...");
                        mIsScanContinue = true;
                        mScanDevice = bleDevice;
                        if (mDialog != null) {
                            mDialog.dismiss();
                        }
                        mBtService.connectBle(bleDevice.address);
                        mPosition = 0;
                    }
                }
                if (BTConstants.ACTION_BLE_DEVICES_DATA_END.equals(intent
                        .getAction())) {
                    LogModule.d("结束扫描..." + mScanTimes);
                    if (!mIsScanContinue) {
                        if (mScanTimes < 4) {
                            mScanTimes++;
                            LogModule.d("开始扫描..." + mScanTimes);
                            mDevices.clear();
                            mBtService.scanDevice();
                            mAdapter.notifyDataSetChanged();
                            return;
                        }
                        if (mDialog != null) {
                            mDialog.dismiss();
                        }
                        showMatchManualUI();
                        mAdapter.notifyDataSetChanged();
                    }
                }
                if (BTConstants.ACTION_CONN_STATUS_TIMEOUT.equals(intent.getAction())
                        || BTConstants.ACTION_CONN_STATUS_DISCONNECTED.equals(intent.getAction())
                        || BTConstants.ACTION_DISCOVER_FAILURE.equals(intent.getAction())) {
                    LogModule.d("配对失败...");
                    if (mDialog != null) {
                        mDialog.dismiss();
                    }
                    showMatchManualUI();
                }
                if (BTConstants.ACTION_DISCOVER_SUCCESS.equals(intent
                        .getAction())) {
                    LogModule.d("配对成功...");
                    ToastUtils.showToast(MatchDevicesActivity.this, R.string.setting_device_conn_success);
                    if (mDialog != null) {
                        mDialog.dismiss();
                    }
                    if (mPosition == -1) {
                        return;
                    }
                    SPUtiles.setStringValue(BTConstants.SP_KEY_DEVICE_ADDRESS,
                            mScanDevice == null ? mDevices.get(mPosition).address : mScanDevice.address);
                    SPUtiles.setStringValue(BTConstants.SP_KEY_DEVICE_NAME,
                            mScanDevice == null ? mDevices.get(mPosition).name : mScanDevice.name.substring(0, mScanDevice.name.indexOf("-D")));
                    // showInitUI();
                    startActivity(new Intent(MatchDevicesActivity.this, UserInfoLayoutActivity.class));
                    finishActivities(ActivateBraceletActivity.class, BluetoothOpenActivity.class, MatchDevicesActivity.class);
                }
            }

        }
    };
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogModule.d("连接服务onServiceConnected...");
            mBtService = ((BTService.LocalBinder) service).getService();
            if (mBtService.mBluetoothGatt != null) {
                mBtService.disConnectBle();
            }
            // 开启蓝牙
            if (!BTModule.isBluetoothOpen()) {
                BTModule.openBluetooth(MatchDevicesActivity.this);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogModule.d("断开服务onServiceDisconnected...");
            mBtService = null;
        }
    };

    public class DeviceAdapter extends BaseAdapter {

        @Override
        public int getCount() {

            return mDevices.size();
        }

        @Override
        public Object getItem(int position) {

            return mDevices.get(position);
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            BleDevice bleDevice = mDevices.get(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(MatchDevicesActivity.this).inflate(R.layout.match_devices_list_item, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tv_device_name.setText(bleDevice.name);
            holder.tv_device_mac.setText(bleDevice.address);
            if (bleDevice.isChecked) {
                holder.iv_device_checked.setImageResource(R.drawable.match_device_checked_true);
            } else {
                holder.iv_device_checked.setImageResource(R.drawable.match_device_checked_false);
            }
            return convertView;
        }

        class ViewHolder {
            @Bind(R.id.tv_device_name)
            TextView tv_device_name;
            @Bind(R.id.tv_device_mac)
            TextView tv_device_mac;
            @Bind(R.id.iv_device_checked)
            ImageView iv_device_checked;

            public ViewHolder(View v) {
                ButterKnife.bind(this, v);
            }
        }
    }
}
