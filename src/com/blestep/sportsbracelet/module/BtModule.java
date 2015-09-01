package com.blestep.sportsbracelet.module;

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
import android.text.format.Time;
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

	// send current date time;
	public void sendDate(BluetoothGatt mBluetoothGatt) {

		Time t = new Time(); // or Time t=new Time("GMT+8"); 加上Time Zone资料。
		t.setToNow(); // 取得系统时间。
		int year = t.year - 2000;
		int month = t.month + 1;
		int date = t.monthDay;
		int hour = t.hour; // 0-23
		int minute = t.minute;
		int second = t.second;
		byte[] bb = new byte[7];
		bb[0] = 0x11;
		bb[1] = (byte) year;
		bb[2] = (byte) month;
		bb[3] = (byte) date;
		bb[4] = (byte) hour;
		bb[5] = (byte) minute;
		bb[6] = (byte) second;
		writeLlsAlertLevel(mBluetoothGatt, bb);

	}

	public void writeLlsAlertLevel(BluetoothGatt mBluetoothGatt, byte[] bb) {

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
		boolean status = false;
		int storedLevel = alertLevel.getWriteType();

		alertLevel.setValue(bb);

		alertLevel.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
		status = mBluetoothGatt.writeCharacteristic(alertLevel);
	}
}
