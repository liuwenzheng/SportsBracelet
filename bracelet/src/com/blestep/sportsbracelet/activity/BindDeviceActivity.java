package com.blestep.sportsbracelet.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.blestep.sportsbracelet.BTConstants;
import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.base.BaseActivity;
import com.blestep.sportsbracelet.entity.BleDevice;
import com.blestep.sportsbracelet.module.BTModule;
import com.blestep.sportsbracelet.module.LogModule;
import com.blestep.sportsbracelet.service.BTService;
import com.blestep.sportsbracelet.service.BTService.LocalBinder;
import com.blestep.sportsbracelet.utils.SPUtiles;
import com.blestep.sportsbracelet.utils.ToastUtils;
import com.blestep.sportsbracelet.utils.Utils;
import com.umeng.analytics.MobclickAgent;

public class BindDeviceActivity extends BaseActivity implements
		OnClickListener, OnItemClickListener {
	private ListView lv_setting_device;
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
		setContentView(R.layout.bind_device_page);
		initView();
		initListener();
		initData();
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
	protected void onDestroy() {
		// 注销广播接收器
		unregisterReceiver(mReceiver);
		unbindService(mServiceConnection);
		super.onDestroy();
	}

	private void initListener() {
		findViewById(R.id.btn_bind_finish).setOnClickListener(this);
		findViewById(R.id.tv_bind_refresh).setOnClickListener(this);
		findViewById(R.id.iv_back).setOnClickListener(this);
		lv_setting_device.setOnItemClickListener(this);
	}

	private void initData() {
		mDevices = new ArrayList<BleDevice>();
		mAdapter = new DeviceAdapter();
		lv_setting_device.setAdapter(mAdapter);
		bindService(new Intent(this, BTService.class), mServiceConnection,
				BIND_AUTO_CREATE);

	}

	private void initView() {
		lv_setting_device = (ListView) findViewById(R.id.lv_setting_device);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_back:
			finish();
			break;
		case R.id.tv_bind_refresh:
			mDevices.clear();
			mBtService.scanDevice();
			mDialog = ProgressDialog.show(BindDeviceActivity.this, null,
					getString(R.string.setting_device_search), false, false);
			break;
		case R.id.btn_bind_finish:
			startScanDevice();
			// if (mDevices != null && mDevices.size() == 0) {
			// AlertDialog.Builder builder = new Builder(this);
			// builder.setMessage(R.string.setting_device_search_repeat);
			// builder.setPositiveButton(
			// R.string.setting_device_search_confirm,
			// new DialogInterface.OnClickListener() {
			//
			// @Override
			// public void onClick(DialogInterface dialog,
			// int which) {
			// mBtService.scanDevice();
			// mDialog = ProgressDialog
			// .show(BindDeviceActivity.this,
			// null,
			// getString(R.string.setting_device_search),
			// false, false);
			// dialog.dismiss();
			// }
			// });
			// builder.show();
			// return;
			// }
			// if (mPosition == -1) {
			// ToastUtils.showToast(this, R.string.setting_device_select_tips);
			// return;
			// }
			// LogModule.i("选中设备mac地址:" + mDevices.get(mPosition).address);
			// mBtService.disConnectBle();
			// // 将选中地址缓存
			// mBtService.connectBle(mDevices.get(mPosition).address);
			// mDialog = ProgressDialog.show(BindDeviceActivity.this, null,
			// getString(R.string.setting_device), false, false);
			break;

		default:
			break;
		}
	}

	private void startScanDevice() {
		mScanTimes = 0;
		mIsScanContinue = false;
		LogModule.d("开始扫描..." + mScanTimes);
		mBtService.scanDevice();
		mDialog = ProgressDialog.show(BindDeviceActivity.this, null,
				getString(R.string.setting_device_search), false, false);
		mScanTimes++;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		mPosition = position;
		for (int i = 0; i < mDevices.size(); i++) {
			mDevices.get(i).isChecked = false;
		}
		mDevices.get(position).isChecked = true;
		mAdapter.notifyDataSetChanged();
		// 开始配对
		if (mDevices != null && mDevices.size() == 0) {
			AlertDialog.Builder builder = new Builder(this);
			builder.setMessage(R.string.setting_device_search_repeat);
			builder.setPositiveButton(R.string.setting_device_search_confirm,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
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
		mBtService.disConnectBle();
		// 将选中地址缓存
		mBtService.connectBle(mDevices.get(mPosition).address);
		mDialog = ProgressDialog.show(BindDeviceActivity.this, null,
				getString(R.string.setting_device), false, false);
	}

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
			ViewHolder holder = null;
			BleDevice bleDevice = mDevices.get(position);
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(BindDeviceActivity.this)
						.inflate(R.layout.setting_device_list_item, null);
				holder.tv_device_name = (TextView) convertView
						.findViewById(R.id.tv_device_name);
				holder.tv_device_mac = (TextView) convertView
						.findViewById(R.id.tv_device_mac);
				holder.iv_device_checked = (ImageView) convertView
						.findViewById(R.id.iv_device_checked);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tv_device_name.setText(bleDevice.name);
			holder.tv_device_mac.setText(bleDevice.address);
			if (bleDevice.isChecked) {
				holder.iv_device_checked
						.setImageResource(R.drawable.setting_device_checked_true);
			} else {
				holder.iv_device_checked
						.setImageResource(R.drawable.setting_device_checked_false);
			}
			return convertView;
		}

		class ViewHolder {
			TextView tv_device_name;
			TextView tv_device_mac;
			ImageView iv_device_checked;
		}
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				if (BTConstants.ACTION_BLE_DEVICES_DATA.equals(intent
						.getAction())) {
					BleDevice bleDevice = (BleDevice) intent.getExtras()
							.getSerializable("device");
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
					if (Utils.isNotEmpty(bleDevice.name)
							&& bleDevice.name.lastIndexOf("-D") > 0) {
						LogModule.d("可以配对...");
						mIsScanContinue = true;
						mScanDevice = bleDevice;
						if (mDialog != null) {
							mDialog.dismiss();
						}
						mBtService.connectBle(bleDevice.address);
						mDialog = ProgressDialog.show(
								BindDeviceActivity.this, null,
								getString(R.string.setting_device), false,
								false);
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
						mAdapter.notifyDataSetChanged();
					}
				}
				if (BTConstants.ACTION_CONN_STATUS_TIMEOUT.equals(intent
						.getAction())
						|| BTConstants.ACTION_CONN_STATUS_DISCONNECTED
								.equals(intent.getAction())
						|| BTConstants.ACTION_DISCOVER_FAILURE.equals(intent
								.getAction())) {
					LogModule.d("配对失败...");
					ToastUtils.showToast(BindDeviceActivity.this,
							R.string.setting_device_conn_failure);
					if (mDialog != null) {
						mDialog.dismiss();
					}
					BindDeviceActivity.this.finish();
				}
				if (BTConstants.ACTION_DISCOVER_SUCCESS.equals(intent
						.getAction())) {
					LogModule.d("配对成功...");
					ToastUtils.showToast(BindDeviceActivity.this,
							R.string.setting_device_conn_success);
					if (mDialog != null) {
						mDialog.dismiss();
					}
					SPUtiles.setStringValue(
							BTConstants.SP_KEY_DEVICE_ADDRESS,
							mScanDevice == null ? mDevices.get(mPosition).address
									: mScanDevice.address);
					SPUtiles.setStringValue(
							BTConstants.SP_KEY_DEVICE_NAME,
							mScanDevice == null ? mDevices.get(mPosition).name
									: mScanDevice.name.substring(0,
											mScanDevice.name.indexOf("-D")));
					// startActivity(new Intent(BindDeviceActivity.this,
					// SettingUserInfoActivity.class));
					BindDeviceActivity.this.finish();
				}
			}

		}
	};
	private ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LogModule.d("连接服务onServiceConnected...");
			mBtService = ((LocalBinder) service).getService();
			// 开启蓝牙
			if (!BTModule.isBluetoothOpen()) {
				BTModule.openBluetooth(BindDeviceActivity.this);
			} else {
				// LogModule.d("开始扫描..." + mScanTimes);
				// mBtService.scanDevice();
				// mDialog = ProgressDialog
				// .show(SettingDeviceActivity.this, null,
				// getString(R.string.setting_device_search),
				// false, false);
				// mScanTimes++;
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			LogModule.d("断开服务onServiceDisconnected...");
			mBtService = null;
		}
	};
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
}
