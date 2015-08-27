package com.blestep.sportsbracelet.module;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class BtModule {
	public static BluetoothAdapter mBluetoothAdapter;
	public static final int REQUEST_ENABLE_BT = 1001;
	public static final String BARCELET_BT_NAME = "J-Band";

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
}
