package com.blestep.sportsbracelet.utils;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;

import com.blestep.sportsbracelet.module.LogModule;

public class Utils {

	/**
	 * 根据手机分辨率把dp转换成px(像素)
	 * 
	 * @param context
	 * @param dpValue
	 * @return
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机分辨率把px转换成dp
	 * 
	 * @param context
	 * @param pxValue
	 * @return
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * 格式化手环返回的数据
	 * 
	 * @param data
	 * @param characteristic
	 * @return
	 */
	public static String[] formatData(byte[] data,
			BluetoothGattCharacteristic characteristic) {
		if (data != null && data.length > 0) {
			StringBuilder stringBuilder = new StringBuilder(data.length);
			for (byte byteChar : data)
				stringBuilder.append(String.format("%02X ", byteChar));
			LogModule.i("16位进制数：" + stringBuilder.toString());
			String[] datas = stringBuilder.toString().split(" ");
			return datas;
		} else {
			int flag = characteristic.getProperties();
			int format = -1;
			if ((flag & 0x01) != 0) {
				format = BluetoothGattCharacteristic.FORMAT_UINT16;
				LogModule.i("Heart rate format UINT16.");
			} else {
				format = BluetoothGattCharacteristic.FORMAT_UINT8;
				LogModule.i("Heart rate format UINT8.");
			}
			int heartRate = characteristic.getIntValue(format, 1);
			LogModule.i(String.format("Received heart rate: %d", heartRate));
			return null;
		}
	}

	/**
	 * 16进制数组转10进制数组
	 * 
	 * @param data
	 * @return
	 */
	public static String[] decode(String data) {
		String[] datas = data.split(" ");
		String[] stringDatas = new String[datas.length];
		for (int i = 0; i < datas.length; i++) {
			stringDatas[i] = Integer.toString(Integer.valueOf(datas[i], 16));
		}
		return stringDatas;
	}

	/**
	 * 10进制转16进制
	 * 
	 * @param data
	 * @return
	 */
	public static String decodeToHex(String data) {
		String string = Integer.toHexString(Integer.valueOf(data));
		return string;
	}

	/**
	 * 16进制转10进制
	 * 
	 * @param data
	 * @return
	 */
	public static String decodeToString(String data) {
		String string = Integer.toString(Integer.valueOf(data, 16));
		return string;
	}

	public static boolean isEmpty(String s) {
		return s == null || s.length() == 0 || s.trim().equals("")
				|| s.trim().equals("null");
	}

	public static boolean isNotEmpty(String s) {
		return s != null && s.length() != 0 && !s.trim().equals("")
				&& !s.trim().equals("null");
	}
}
