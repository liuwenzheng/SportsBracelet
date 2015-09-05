package com.blestep.sportsbracelet;

import android.app.Application;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;

import com.blestep.sportsbracelet.db.DBTools;
import com.blestep.sportsbracelet.module.BtModule;
import com.blestep.sportsbracelet.service.BTService;
import com.blestep.sportsbracelet.utils.SPUtiles;

public class BaseApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		// 初始化数据库
		DBTools.getInstance(getApplicationContext());
		// 初始化SharedPreference
		SPUtiles.getInstance(getApplicationContext());
		// 启动蓝牙服务
		startService(new Intent(this, BTService.class));
		// 初始化蓝牙适配器
		BluetoothManager bluetoothManager = (BluetoothManager) getApplicationContext().getSystemService(
				Context.BLUETOOTH_SERVICE);
		BtModule.mBluetoothAdapter = bluetoothManager.getAdapter();
	}
}
