package com.blestep.sportsbracelet.receiver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.blestep.sportsbracelet.module.LogModule;

public class PhoneStateReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		// 如果是来电
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Service.TELEPHONY_SERVICE);

		switch (tm.getCallState()) {
		case TelephonyManager.CALL_STATE_RINGING:
			// 来电
			// String incoming_number =
			// intent.getStringExtra("incoming_number");
			// LogModule.d("来电号码:" + incoming_number);
			// log:来电号码:18801283616
			break;
		case TelephonyManager.CALL_STATE_OFFHOOK:
			// 摘机（正在通话中）
			break;

		case TelephonyManager.CALL_STATE_IDLE:
			// 空闲
			break;
		}
	}

}
