package com.blestep.sportsbracelet;

import android.app.Application;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import com.blestep.sportsbracelet.module.BtModule;

public class BaseApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		// TODO 创建数据库
		// 连接蓝牙
		BluetoothManager bluetoothManager = (BluetoothManager) getApplicationContext().getSystemService(
				Context.BLUETOOTH_SERVICE);
		BtModule.mBluetoothAdapter = bluetoothManager.getAdapter();
	}
}
