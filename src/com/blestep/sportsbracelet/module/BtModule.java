package com.blestep.sportsbracelet.module;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class BtModule {
	public static BluetoothAdapter mBluetoothAdapter;
	public static BluetoothGattCharacteristic mNotifyCharacteristic;
	public static final int REQUEST_ENABLE_BT = 1001;
	public static final String BARCELET_BT_NAME = "J-Band";
	public static final UUID SERVIE_UUID = UUID.fromString("0000ffc0-0000-1000-8000-00805f9b34fb");
	public static final UUID CHARACTERISTIC_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

	/**
	 * Write, APP send command to wristbands using this characteristic
	 */
	public static final UUID CHARACTERISTIC_UUID_WRITE = UUID.fromString("0000ffc1-0000-1000-8000-00805f9b34fb");
	/**
	 * Notify, wristbands send data to APP using this characteristic
	 */
	public static final UUID CHARACTERISTIC_UUID_NOTIFY = UUID.fromString("0000ffc2-0000-1000-8000-00805f9b34fb");
	public static Handler mHandler = new Handler();

	/**
	 * 
	 * @return
	 */
	public static boolean isBluetoothOpen() {
		if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
			return false;
		}
		return true;
	}

	/**
	 * 打开蓝牙
	 * 
	 * @param context
	 */
	public static void openBluetooth(Context context) {
		// Ensures Bluetooth is available on the device and it is enabled. If
		// not,
		// displays a dialog requesting user permission to enable Bluetooth.
		Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		((Activity) context).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
	}

	public BtModule() {
	}

	/**
	 * 搜索手环
	 */
	public static void scanDevice(LeScanCallback mLeScanCallback) {
		mBluetoothAdapter.startLeScan(mLeScanCallback);
	}

	/**
	 * 设置手环当前时间
	 * 
	 * @param mBluetoothGatt
	 * 
	 */
	public static void setCurrentTime(BluetoothGatt mBluetoothGatt) {
		// 取得手机当前时间，并设置到手环上
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int date = calendar.get(Calendar.DATE);
		int hour = calendar.get(Calendar.HOUR);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);
		byte[] byteArray = new byte[7];
		byteArray[0] = 0x11;
		byteArray[1] = (byte) (year - 2000);
		byteArray[2] = (byte) month;
		byteArray[3] = (byte) date;
		byteArray[4] = (byte) hour;
		byteArray[5] = (byte) minute;
		byteArray[6] = (byte) second;
		writeCharacteristicData(mBluetoothGatt, byteArray);

	}

	/**
	 * 获取当前步数
	 * 
	 * @param mBluetoothGatt
	 * 
	 */
	public static void getCurrentStepData(BluetoothGatt mBluetoothGatt) {
		byte[] byteArray = new byte[2];
		byteArray[0] = 0x16;
		byteArray[1] = 0x01;
		writeCharacteristicData(mBluetoothGatt, byteArray);
	}

	/**
	 * 震动
	 * 
	 * @param mBluetoothGatt
	 * 
	 */
	public static void shakeBand(BluetoothGatt mBluetoothGatt) {
		byte[] byteArray = new byte[5];
		byteArray[0] = 0x17;
		byteArray[1] = 0x02;
		byteArray[2] = 0x02;
		byteArray[3] = 0x01;
		byteArray[4] = 0x01;
		writeCharacteristicData(mBluetoothGatt, byteArray);
	}

	public static void setCharacteristicNotify(BluetoothGatt mBluetoothGatt) {
		List<BluetoothGattService> gattServices = mBluetoothGatt.getServices();
		if (gattServices == null)
			return;
		String uuid = null;
		// 遍历所有服务，找到手环的服务
		for (BluetoothGattService gattService : gattServices) {
			uuid = gattService.getUuid().toString();
			if (uuid.startsWith("0000ffc0")) {
				List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
				// 遍历所有特征，找到发出的特征
				for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
					uuid = gattCharacteristic.getUuid().toString();
					if (uuid.startsWith("0000ffc2")) {
						int charaProp = gattCharacteristic.getProperties();
						if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {

							if (mNotifyCharacteristic != null) {
								setCharacteristicNotification(mBluetoothGatt, mNotifyCharacteristic, false);
								mNotifyCharacteristic = null;
							}
							mBluetoothGatt.readCharacteristic(gattCharacteristic);
						}
						if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
							mNotifyCharacteristic = gattCharacteristic;
							setCharacteristicNotification(mBluetoothGatt, gattCharacteristic, true);
						}
					}
				}
			}
		}
	}

	/**
	 * Enables or disables notification on a give characteristic.
	 * 
	 * @param characteristic
	 *            Characteristic to act on.
	 * @param enabled
	 *            If true, enable notification. False otherwise.
	 */
	public static void setCharacteristicNotification(BluetoothGatt mBluetoothGatt,
			BluetoothGattCharacteristic characteristic, boolean enabled) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			return;
		}
		mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
		/**
		 * 打开数据FFF4
		 */
		// This is specific to Heart Rate Measurement.
		if (CHARACTERISTIC_UUID_NOTIFY.equals(characteristic.getUuid())) {
			BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CHARACTERISTIC_DESCRIPTOR_UUID);
			descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			mBluetoothGatt.writeDescriptor(descriptor);
		}
	}

	public static void writeCharacteristicData(BluetoothGatt mBluetoothGatt, byte[] byteArray) {
		BluetoothGattService service = mBluetoothGatt.getService(SERVIE_UUID);
		LogModule.i("writeCharacteristicData...service:" + service);
		if (service == null) {
			return;
		}
		BluetoothGattCharacteristic characteristic = null;
		characteristic = service.getCharacteristic(CHARACTERISTIC_UUID_WRITE);
		LogModule.i("writeCharacteristicData...characteristic:" + characteristic);
		if (characteristic == null) {
			return;
		}
		characteristic.setValue(byteArray);
		characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
		mBluetoothGatt.writeCharacteristic(characteristic);
	}

	// public static void readCharacteristicData(BluetoothGatt mBluetoothGatt,
	// byte[] byteArray) {
	// BluetoothGattService service = mBluetoothGatt.getService(SERVIE_UUID);
	// LogModule.i("readCharacteristicData...service:" + service);
	// if (service == null) {
	// return;
	// }
	// BluetoothGattCharacteristic characteristic = null;
	// characteristic = service.getCharacteristic(CHARACTERISTIC_UUID_NOTIFY);
	// LogModule.i("readCharacteristicData...characteristic:" + characteristic);
	// if (characteristic == null) {
	// return;
	// }
	// characteristic.setValue(byteArray);
	// characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
	// mBluetoothGatt.readCharacteristic(characteristic);
	// }

}
