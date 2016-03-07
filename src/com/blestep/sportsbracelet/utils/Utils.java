package com.blestep.sportsbracelet.utils;

import java.util.Calendar;
import java.util.Date;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;

import com.blestep.sportsbracelet.BTConstants;
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

	/**
	 * 计算两个日期的时间间隔
	 * 
	 * @param sDate开始时间
	 * @param eDate结束时间
	 * @param type间隔类型
	 *            ("Y/y"--年 "M/m"--月 "D/d"--日)
	 * @return interval时间间隔
	 * */
	public static int calInterval(Date sDate, Date eDate) {
		// 时间间隔，初始为0
		int interval = 0;

		/* 比较两个日期的大小，如果开始日期更大，则交换两个日期 */
		// 标志两个日期是否交换过
		boolean reversed = false;
		if (compareDate(sDate, eDate) > 0) {
			Date dTest = sDate;
			sDate = eDate;
			eDate = dTest;
			// 修改交换标志
			reversed = true;
		}

		/* 将两个日期赋给日历实例，并获取年、月、日相关字段值 */
		Calendar sCalendar = Calendar.getInstance();
		sCalendar.setTime(sDate);
		int sYears = sCalendar.get(Calendar.YEAR);
		int sMonths = sCalendar.get(Calendar.MONTH);
		int sDays = sCalendar.get(Calendar.DAY_OF_YEAR);

		Calendar eCalendar = Calendar.getInstance();
		eCalendar.setTime(eDate);
		int eYears = eCalendar.get(Calendar.YEAR);
		int eMonths = eCalendar.get(Calendar.MONTH);
		int eDays = eCalendar.get(Calendar.DAY_OF_YEAR);

		// 年
		if (cTrim(BTConstants.PATTERN_YYYY_MM_DD).equals("Y")
				|| cTrim(BTConstants.PATTERN_YYYY_MM_DD).equals("y")) {
			interval = eYears - sYears;
			if (eMonths < sMonths) {
				--interval;
			}
		}
		// 月
		else if (cTrim(BTConstants.PATTERN_YYYY_MM_DD).equals("M")
				|| cTrim(BTConstants.PATTERN_YYYY_MM_DD).equals("m")) {
			interval = 12 * (eYears - sYears);
			interval += (eMonths - sMonths);
		}
		// 日
		else if (cTrim(BTConstants.PATTERN_YYYY_MM_DD).equals("D")
				|| cTrim(BTConstants.PATTERN_YYYY_MM_DD).equals("d")) {
			interval = 365 * (eYears - sYears);
			interval += (eDays - sDays);
			// 除去闰年天数
			while (sYears < eYears) {
				if (isLeapYear(sYears)) {
					--interval;
				}
				++sYears;
			}
		}
		// 如果开始日期更大，则返回负值
		if (reversed) {
			interval = -interval;
		}
		// 返回计算结果
		return interval;
	}

	/**
	 * 比较两个Date类型的日期大小
	 * 
	 * @param sDate开始时间
	 * @param eDate结束时间
	 * @return result返回结果(0--相同 1--前者大 2--后者大)
	 * */
	private static int compareDate(Date sDate, Date eDate) {
		int result = 0;
		// 将开始时间赋给日历实例
		Calendar sC = Calendar.getInstance();
		sC.setTime(sDate);
		// 将结束时间赋给日历实例
		Calendar eC = Calendar.getInstance();
		eC.setTime(eDate);
		// 比较
		result = sC.compareTo(eC);
		// 返回结果
		return result;
	}

	/**
	 * 字符串去除两头空格，如果为空，则返回""，如果不空，则返回该字符串去掉前后空格
	 * 
	 * @param tStr输入字符串
	 * @return 如果为空，则返回""，如果不空，则返回该字符串去掉前后空格
	 */
	private static String cTrim(String tStr) {
		String ttStr = "";
		if (tStr == null) {
		} else {
			ttStr = tStr.trim();
		}
		return ttStr;
	}

	/**
	 * 判定某个年份是否是闰年
	 * 
	 * @param year待判定的年份
	 * @return 判定结果
	 * */
	private static boolean isLeapYear(int year) {
		return (year % 400 == 0 || (year % 4 == 0 && year % 100 != 0));
	}
}
