package com.blestep.sportsbracelet.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SmsStateReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// Object[] pduses = (Object[]) intent.getExtras().get("pdus");
		// for (Object pdus : pduses) {
		// byte[] pdusmessage = (byte[]) pdus;
		// SmsMessage sms = SmsMessage.createFromPdu(pdusmessage);
		// String mobile = sms.getOriginatingAddress();// 发送短信的手机号码
		// LogModule.d("2222来短信号码:" + mobile);
		// // log:来短信号码:+8618801283616
		//
		// }

	}

}
