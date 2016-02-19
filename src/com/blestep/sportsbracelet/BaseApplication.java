package com.blestep.sportsbracelet;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import android.app.Application;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.blestep.sportsbracelet.db.DBTools;
import com.blestep.sportsbracelet.module.BTModule;
import com.blestep.sportsbracelet.service.BTService;
import com.blestep.sportsbracelet.utils.IOUtils;
import com.blestep.sportsbracelet.utils.SPUtiles;
import com.umeng.analytics.MobclickAgent;

public class BaseApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		MobclickAgent.setCatchUncaughtExceptions(true);
		// 初始化数据库
		DBTools.getInstance(getApplicationContext());
		// 初始化SharedPreference
		SPUtiles.getInstance(getApplicationContext());
		// 启动蓝牙服务
		startService(new Intent(this, BTService.class));
		// 初始化蓝牙适配器
		BluetoothManager bluetoothManager = (BluetoothManager) getApplicationContext()
				.getSystemService(Context.BLUETOOTH_SERVICE);
		BTModule.mBluetoothAdapter = bluetoothManager.getAdapter();
		Thread.setDefaultUncaughtExceptionHandler(new BTUncaughtExceptionHandler());
	}

	public class BTUncaughtExceptionHandler implements
			Thread.UncaughtExceptionHandler {
		private static final String LOGTAG = "BTUncaughtExceptionHandler";

		@Override
		public void uncaughtException(Thread thread, Throwable ex) {
			// 读取stacktrace信息
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			ex.printStackTrace(printWriter);
			StringBuffer errorReport = new StringBuffer();
			// 获取packagemanager的实例
			PackageManager packageManager = getPackageManager();
			// getPackageName()是你当前类的包名，0代表是获取版本信息
			PackageInfo packInfo = null;
			try {
				packInfo = packageManager.getPackageInfo(getPackageName(), 0);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			if (packInfo != null) {
				String version = packInfo.versionName;
				errorReport.append(version);
				errorReport.append("\r\n");
			}
			errorReport.append(result.toString());
			IOUtils.setCrashLog(errorReport.toString());
			Log.i(LOGTAG, "uncaughtException errorReport=" + errorReport);
			// 友盟上传报错信息
			MobclickAgent.reportError(getApplicationContext(), ex);
			MobclickAgent.onKillProcess(getApplicationContext());
			android.os.Process.killProcess(android.os.Process.myPid());
		}
	}
}
