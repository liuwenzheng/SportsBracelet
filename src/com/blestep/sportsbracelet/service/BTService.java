package com.blestep.sportsbracelet.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.blestep.sportsbracelet.AppConstants;
import com.blestep.sportsbracelet.entity.BleDevice;
import com.blestep.sportsbracelet.module.BTModule;
import com.blestep.sportsbracelet.module.LogModule;
import com.blestep.sportsbracelet.utils.SPUtiles;
import com.blestep.sportsbracelet.utils.Utils;

public class BTService extends Service implements LeScanCallback {
	private boolean mIsStartScan = false;
	private static final long SCAN_PERIOD = 5000;
	private static final int GATT_ERROR_TIMEOUT = 133;

	public static Handler mHandler;
	// private ArrayList<BleDevice> mDevices;
	private BluetoothGatt mBluetoothGatt;

	@Override
	public void onCreate() {
		mHandler = new Handler(getApplicationContext().getMainLooper());
		LogModule.d("创建BTService...onCreate");
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		LogModule.d("启动BTService...onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}

	private IBinder mBinder = new LocalBinder();

	@Override
	public IBinder onBind(Intent intent) {
		LogModule.d("绑定BTService...onBind");
		return mBinder;
	}

	/**
	 * 搜索手环
	 */
	public void scanDevice() {
		// String address =
		// SPUtiles.getStringValue(SPUtiles.SP_KEY_DEVICE_ADDRESS, "");
		// mDevices = new ArrayList<BleDevice>();
		if (!mIsStartScan) {
			mIsStartScan = true;
			BTModule.scanDevice(this);
			// Stops scanning after a pre-defined scan period.
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (mIsStartScan) {
						LogModule.i(SCAN_PERIOD / 1000 + "s后停止扫描");
						BTModule.mBluetoothAdapter.stopLeScan(BTService.this);
						mIsStartScan = false;
						Intent intent = new Intent(AppConstants.ACTION_BLE_DEVICES_DATA_END);
						// intent.putExtra("devices", mDevices);
						sendBroadcast(intent);
					}
				}
			}, SCAN_PERIOD);
		} else {
			LogModule.i("正在扫描中...");
		}

	}

	/**
	 * 连接手环
	 */
	public void connectBle(String address) {
		final BluetoothDevice device = BTModule.mBluetoothAdapter.getRemoteDevice(address);
		if (device == null) {
			return;
		} else {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					mBluetoothGatt = device.connectGatt(BTService.this, false, mGattCallback);
				}
			});

		}
	}

	@Override
	public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
		if (device != null) {
			if (Utils.isEmpty(device.getName())) {
				return;
			}
			BleDevice bleDevice = new BleDevice();
			bleDevice.name = device.getName();
			bleDevice.address = device.getAddress();
			Intent intent = new Intent(AppConstants.ACTION_BLE_DEVICES_DATA);
			intent.putExtra("device", bleDevice);
			sendBroadcast(intent);
			// mDevices.add(bleDevice);
		}
	}

	private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		// private int count;

		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			LogModule.d("onConnectionStateChange...status:" + status + "...newState:" + newState);
			switch (newState) {
			case BluetoothProfile.STATE_CONNECTED:
				if (status == GATT_ERROR_TIMEOUT) {
					if (mBluetoothGatt != null) {
						mBluetoothGatt.close();
						mBluetoothGatt = null;
					}
					Intent intent = new Intent(AppConstants.ACTION_CONN_STATUS_TIMEOUT);
					sendBroadcast(intent);
				} else {
					mBluetoothGatt.discoverServices();
				}
				break;
			case BluetoothProfile.STATE_DISCONNECTED:
				if (mBluetoothGatt != null) {
					mBluetoothGatt.close();
					mBluetoothGatt = null;
				}
				Intent intent = new Intent(AppConstants.ACTION_CONN_STATUS_DISCONNECTED);
				sendBroadcast(intent);
				break;
			}
		};

		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			LogModule.d("onServicesDiscovered...status:" + status);
			if (status == BluetoothGatt.GATT_SUCCESS) {
				BTModule.setCharacteristicNotify(mBluetoothGatt);
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						Intent intent = new Intent(AppConstants.ACTION_DISCOVER_SUCCESS);
						sendBroadcast(intent);
					}
				}, 1000);
			} else {
				Intent intent = new Intent(AppConstants.ACTION_DISCOVER_FAILURE);
				sendBroadcast(intent);
			}
		};

		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			LogModule.d("onCharacteristicRead...");
		};

		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			LogModule.d("onCharacteristicWrite...");
		};

		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			LogModule.d("onCharacteristicChanged...");
			// BTModule.setCharacteristicNotify(mBluetoothGatt);
			byte[] data = characteristic.getValue();
			String[] formatDatas = Utils.formatData(data, characteristic);
			// StringBuilder stringBuilder = new
			// StringBuilder(formatDatas.length);
			// for (String string : formatDatas)
			// stringBuilder.append(string + " ");
			// LogModule.i("转化后：" + stringBuilder.toString());
			// 获取总记录数
			int header = Integer.valueOf(Utils.decodeToString(formatDatas[0]));
			if (header == AppConstants.HEADER_BACK_ACK) {
				return;
			}
			if (header == AppConstants.HEADER_BACK_RECORD) {
				// count = 0;
				// int stepRecord = Integer.valueOf(formatDatas[1]);
				// int sleepRecord = Integer.valueOf(formatDatas[2]);
				// 保存电量
				int battery = Integer.valueOf(Utils.decodeToString(formatDatas[3]));
				SPUtiles.setIntValue(SPUtiles.SP_KEY_BATTERY, battery);
				// count = stepRecord;
				// LogModule.i("手环中的记录总数为：" + count);
				// Intent intent = new Intent(AppConstants.ACTION_LOG);
				// intent.putExtra("log", "手环中的记录总数为：" + count);
				// sendBroadcast(intent);
				return;
			}
			// count--;
			BTModule.saveBleData(formatDatas, getApplicationContext());
			// LogModule.i(count + "...");
			// if (count == 0) {
			// LogModule.i("延迟1s发送广播更新数据");
			// mHandler.postDelayed(new Runnable() {
			// @Override
			// public void run() {
			// Intent intent = new Intent(AppConstants.ACTION_REFRESH_DATA);
			// sendBroadcast(intent);
			// }
			// }, 1000);
			// }
		};
	};

	/**
	 * 同步时间
	 */
	public void synTimeData() {
		BTModule.setCurrentTime(mBluetoothGatt);
	}

	/**
	 * 同步用户数据
	 */
	public void synUserInfoData() {
		BTModule.setUserInfo(mBluetoothGatt);
	}

	/**
	 * 获取手环数据
	 */
	public void getSportData() {
		BTModule.getBatteryData(mBluetoothGatt);
		BTModule.getStepData(mBluetoothGatt);
	}

	/**
	 * 清空手环数据
	 */
	public void clearData() {
		BTModule.clearData(mBluetoothGatt);
	}

	/**
	 * 是否连接手环
	 * 
	 * @return
	 */
	public boolean isConnDevice() {
		BluetoothManager bluetoothManager = (BluetoothManager) getApplicationContext().getSystemService(
				Context.BLUETOOTH_SERVICE);
		int connState = bluetoothManager.getConnectionState(BTModule.mBluetoothAdapter.getRemoteDevice(SPUtiles
				.getStringValue(SPUtiles.SP_KEY_DEVICE_ADDRESS, null)), BluetoothProfile.GATT);
		if (connState == BluetoothProfile.STATE_CONNECTED) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean onUnbind(Intent intent) {
		LogModule.d("解绑BTService...onUnbind");
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy() {
		LogModule.d("销毁BTService...onDestroy");
		if (mBluetoothGatt != null) {
			mBluetoothGatt.close();
			mBluetoothGatt = null;
		}
		super.onDestroy();
	}

	public class LocalBinder extends Binder {
		public BTService getService() {
			return BTService.this;
		}
	}
}
