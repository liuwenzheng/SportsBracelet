package com.blestep.sportsbracelet.activity;

import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.blestep.sportsbracelet.R;
import com.blestep.sportsbracelet.base.BaseActivity;
import com.blestep.sportsbracelet.module.BtModule;
import com.blestep.sportsbracelet.module.LogModule;
import com.blestep.sportsbracelet.utils.Utils;
import com.blestep.sportsbracelet.view.CircleProgressView;

public class SplashActivity extends BaseActivity implements LeScanCallback {

	private CircleProgressView circleView;
	private boolean mIsStartScan = false;
	private static final long SCAN_PERIOD = 10000;

	private BluetoothGatt mBluetoothGatt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		circleView = (CircleProgressView) findViewById(R.id.circleView);
		circleView.setMaxValue(100);
		circleView.setValueAnimated(45);
		if (!BtModule.isBluetoothOpen()) {
			BtModule.openBluetooth(this);
		} else {
			scanDevice();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case BtModule.REQUEST_ENABLE_BT:
				scanDevice();
				break;

			default:
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 搜索手环
	 */
	private void scanDevice() {
		BtModule.scanDevice(this);
		// Stops scanning after a pre-defined scan period.
		BtModule.mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (mIsStartScan) {
					// TODO 没找到手环，继续找呗
					BtModule.mBluetoothAdapter.stopLeScan(SplashActivity.this);
					mIsStartScan = false;
				}
			}
		}, SCAN_PERIOD);
	}

	private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			LogModule.i("onConnectionStateChange...newState:" + newState);
			mBluetoothGatt.discoverServices();
		};

		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			LogModule.i("onServicesDiscovered...status:" + status);
			BtModule.setCurrentTime(mBluetoothGatt);
			BtModule.getCurrentStepData(mBluetoothGatt);
			// BtModule.shakeBand(mBluetoothGatt);			
		};

		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			LogModule.i("onCharacteristicRead...");
		};

		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			LogModule.i("onCharacteristicWrite...");
		};

		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			LogModule.i("onCharacteristicChanged...");
		};
	};

	@Override
	public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
		if (device != null && Utils.isNotEmpty(device.getName()) && device.getName().equals(BtModule.BARCELET_BT_NAME)) {
			// TODO 找到设备
			LogModule.e("找到手环！");
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// tv_device_name.setText(device.getName());
					((TextView) findViewById(R.id.tv_device_name)).setText(device.getName());
					mBluetoothGatt = device.connectGatt(SplashActivity.this, false, mGattCallback);
				}
			});
			BtModule.mBluetoothAdapter.stopLeScan(this);
			mIsStartScan = false;
		}
	}

	@Override
	protected void onDestroy() {
		if (mBluetoothGatt != null) {
			mBluetoothGatt.close();
			mBluetoothGatt = null;
		}
		super.onDestroy();
	}
}
