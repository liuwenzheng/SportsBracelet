package com.blestep.sportsbracelet.module;

import java.util.Calendar;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

public class BtModule {
	public static BluetoothAdapter mBluetoothAdapter;
	public static final int REQUEST_ENABLE_BT = 1001;
	public static final String BARCELET_BT_NAME = "J-Band";
	public static final UUID SERVIE_UUID = UUID.fromString("0000ffc0-0000-1000-8000-00805f9b34fb");
	public static final UUID RED_LIGHT_CONTROL_UUID = UUID.fromString("0000ffc2-0000-1000-8000-00805f9b34fb");
	public static final UUID RED_LIGHT_CONTROL_UUID_TWO = UUID.fromString("0000ffc1-0000-1000-8000-00805f9b34fb");
	public static Handler mHandler = new Handler();

	// Stops scanning after 10 seconds.
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
		writeLlsAlertLevel(mBluetoothGatt, byteArray);

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
		writeLlsAlertLevel(mBluetoothGatt, byteArray);

	}

	public static void writeLlsAlertLevel(BluetoothGatt mBluetoothGatt, byte[] byteArray) {

		// Log.i("iDevice", iDevice);
		BluetoothGattService linkLossService = mBluetoothGatt.getService(SERVIE_UUID);
		Log.e("linkLossService", "" + linkLossService);
		if (linkLossService == null) {
			return;
		}
		// enableBattNoti(iDevice);
		BluetoothGattCharacteristic alertLevel = null;
		// switch (iAlertLevel) {
		// case 1: // red
		// alertLevel =
		// linkLossService.getCharacteristic(RED_LIGHT_CONTROL_UUID);
		// break;
		// case 2:
		alertLevel = linkLossService.getCharacteristic(RED_LIGHT_CONTROL_UUID_TWO);
		// break;
		// }
		if (alertLevel == null) {
			return;
		}
		// boolean status = false;
		// int storedLevel = alertLevel.getWriteType();

		alertLevel.setValue(byteArray);

		alertLevel.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
		// status = mBluetoothGatt.writeCharacteristic(alertLevel);
		mBluetoothGatt.writeCharacteristic(alertLevel);
	}
}
